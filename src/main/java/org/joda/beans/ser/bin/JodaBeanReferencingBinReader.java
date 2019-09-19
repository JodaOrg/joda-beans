/*
 *  Copyright 2001-present Stephen Colebourne
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.joda.beans.ser.bin;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerIteratorFactory;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from the referencing binary format.
 */
class JodaBeanReferencingBinReader extends AbstractBinReader {

    /**
     * The base package including the trailing dot.
     */
    private String overrideBasePackage;
    /**
     * The classes that have been serialized.
     */
    private ClassInfo[] classes;
    /**
     * The classes for lookup of classInfo when the class is known and unnecessary to serialise.
     */
    private Map<Class<?>, ClassInfo> classMap;
    /**
     * The serialized objects that are repeated and referenced.
     */
    private Object[] refs;
    /**
     * Whether we are ignoring the currently parsing object for creation, but rather trying to read it to check for any
     * references.
     */
    private boolean ignoringObject;
    /**
     * Suppressed exceptions that occurred while parsing.
     * <p>
     * These will most likely come if fields have been deleted from beans and we are still trying to parse them to find 
     * references.
     */
    private List<Exception> suppressedExceptions = new ArrayList<>();

    //-----------------------------------------------------------------------
    // creates an instance
    JodaBeanReferencingBinReader(JodaBeanSer settings, DataInputStream input) {
        super(settings, input);
    }

    //-----------------------------------------------------------------------
    // reads the input stream
    @Override
    <T> T read(Class<T> rootType) {
        try {
            try {
                return parseRemaining(rootType);
            } finally {
                input.close();
            }
        } catch (RuntimeException ex) {
            addSuppressed(ex);
            throw ex;
        } catch (Exception ex) {
            addSuppressed(ex);
            throw new RuntimeException(ex);
        }
    }

    private void addSuppressed(Exception ex) {
        for (Exception se : suppressedExceptions) {
            ex.addSuppressed(se);
        }
    }

    //-----------------------------------------------------------------------
    // parses the root bean
    @Override
    <T> T parseRemaining(Class<T> declaredType) throws Exception {
        // the array and version has already been read
        overrideBasePackage = declaredType.getPackage().getName() + ".";
        // ref count + class map
        parseClassDescriptions();

        // parse
        Object parsed = parseObject(declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    //-----------------------------------------------------------------------
    // parses the references
    private void parseClassDescriptions() throws Exception {
        int refCount = acceptInteger(input.readByte());
        if (refCount < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected count of references, but was: " + refCount);
        }
        refs = new Object[refCount];

        int classMapSize = acceptMap(input.readByte());
        classes = new ClassInfo[classMapSize]; // Guaranteed non-negative by acceptMap()
        classMap = new HashMap<>(classMapSize);

        for (int position = 0; position < classMapSize; position++) {
            ClassInfo classInfo = parseClassInfo();
            classes[position] = classInfo;
            classMap.put(classInfo.type, classInfo);
        }
    }

    // parses the class information
    private ClassInfo parseClassInfo() throws Exception {
        String className = acceptString(input.readByte());
        int propertyCount = acceptArray(input.readByte());
        if (propertyCount < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected array with 0 to many elements, but was: " + propertyCount);
        }

        MetaProperty<?>[] metaProperties = new MetaProperty<?>[propertyCount];
        Class<?> type;
        try {
            type = SerTypeMapper.decodeType(className, settings, overrideBasePackage, null);
        } catch (ClassNotFoundException e) {
            suppressedExceptions.add(e);
            // need to be able to try and parse properties of unknown beans in case they contain references
            for (int i = 0; i < propertyCount; i++) {
                acceptString(input.readByte());
                metaProperties[i] = null;
            }
            return new ClassInfo(Object.class, className, metaProperties);
        }

        if (ImmutableBean.class.isAssignableFrom(type)) {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(type);
            MetaBean metaBean = deser.findMetaBean(type);
            for (int i = 0; i < propertyCount; i++) {
                String propertyName = acceptString(input.readByte());
                metaProperties[i] = deser.findMetaProperty(type, metaBean, propertyName);
            }
        } else if (propertyCount != 0) {
            throw new IllegalArgumentException("Invalid binary data: Found non immutable bean class that has meta properties defined: " + type.getName() + ", " + propertyCount + " properties");
        }
        return new ClassInfo(type, className, metaProperties);
    }

    // parses the bean using the class information
    private Object parseBean(int propertyCount, ClassInfo classInfo) {
        String propName = "";
        if (classInfo.metaProperties.length != propertyCount) {
            throw new IllegalArgumentException("Invalid binary data: Expected " + classInfo.metaProperties.length + " properties but was: " + propertyCount);
        }
        try {
            if (classInfo.isNotValid()) {
                if (!this.ignoringObject) {
                    throw new IllegalArgumentException("Invalid binary data: Expected a reference to a bean however could not load the bean class definition " + classInfo.name);
                }
                // we couldn't load the class of this bean, probably because it no longer exists
                for (MetaProperty<?> ignored : classInfo.metaProperties) {
                    // try to read and pick up references in case there's anything that will be referenced later
                    parseObject(Object.class, null, classInfo.type, null, false);
                }
                return null;
            }
            SerDeserializer deser = settings.getDeserializers().findDeserializer(classInfo.type);
            MetaBean metaBean = deser.findMetaBean(classInfo.type);
            BeanBuilder<?> builder = deser.createBuilder(classInfo.type, metaBean);
            for (int i = 0; i < classInfo.metaProperties.length; i++) {
                MetaProperty<?> metaProp = classInfo.metaProperties[i];
                if (metaProp == null) {
                    // try to read and pick up references in case there's anything that will be referenced later
                    // but don't set on the object
                    boolean wasIgnoring = this.ignoringObject;
                    this.ignoringObject = true;
                    parseObject(Object.class, null, classInfo.type, null, false);
                    this.ignoringObject = wasIgnoring;
                } else {
                    propName = metaProp.name();
                    Object value = parseObject(SerOptional.extractType(metaProp, classInfo.type), metaProp, classInfo.type, null, false);
                    Object wrappedValue = SerOptional.wrapValue(metaProp, classInfo.type, value);
                    if (wrappedValue != null) {
                        // null is the same as a value not being set
                        // in the case of defaults we want those to take precedence
                        deser.setValue(builder, metaProp, wrappedValue);
                    }
                }
                propName = "";
            }
            return deser.build(classInfo.type, builder);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + classInfo.type.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    Object parseObject(
            Class<?> declaredType,
            MetaProperty<?> metaProp,
            Class<?> beanType,
            SerIterable parentIterable,
            boolean rootType) throws Exception {

        // establish type
        Class<?> effectiveType = declaredType;
        ClassInfo classInfo = null;
        String metaType = null;
        Integer ref = null;
        int typeByte = input.readByte();

        // Unwrap nested references and meta data
        while (isMap(typeByte)) {

            input.mark(18);
            int mapSize = acceptMap(typeByte);
            if (mapSize > 0) {
                int typeByteTemp = input.readByte();

                if (isIntExtension(typeByteTemp)) {

                    int nestedTypeByteTemp = typeByteTemp;
                    typeByteTemp = input.readByte();
                    int reference = acceptIntExtension(nestedTypeByteTemp);

                    if (typeByteTemp == JODA_TYPE_DATA) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        classInfo = classes[reference];
                        if (!declaredType.isAssignableFrom(classInfo.type)) {
                            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + classInfo.type.getName());
                        }
                        typeByte = input.readByte();
                    } else if (typeByteTemp == JODA_TYPE_META) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        Object value = refs[reference];
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException("Invalid binary data: Expected reference to meta type name, but was: " + reference + ", " + value);
                        }
                        metaType = (String) value;
                        typeByte = input.readByte();
                    } else if (typeByteTemp == JODA_TYPE_REF_KEY) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        // Regular object that is re-referenced
                        // ref is the key, so the rest of the object needs to be placed in refs[ref]
                        ref = reference;
                        if (ref < 0 || ref > refs.length) {
                            throw new IllegalArgumentException("Invalid binary data: Expected reference to position less than " + refs.length + ", but was: " + ref);
                        }
                        typeByte = input.readByte();
                    } else {
                        input.reset();
                        break;
                    }
                } else if (typeByteTemp == EXT_8) {
                    int size = input.readUnsignedByte();
                    typeByteTemp = input.readByte();
                    if (typeByteTemp != JODA_TYPE_META) {
                        throw new IllegalArgumentException("Invalid binary data: Expected meta information, but was: 0x" + toHex(typeByteTemp));
                    }
                    if (mapSize != 1) {
                        throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                    }
                    metaType = acceptStringBytes(size);
                    typeByte = input.readByte();
                } else if (isMap(typeByteTemp)) {
                    mapSize = acceptMap(typeByteTemp);
                    typeByteTemp = input.readByte();
                    // Check for nested JODA_TYPE_META with a reference as the key
                    if (isIntExtension(typeByteTemp)) {
                        int nestedTypeByteTemp = typeByteTemp;
                        typeByteTemp = input.readByte();
                        int reference = acceptIntExtension(nestedTypeByteTemp);
                        if (typeByteTemp == JODA_TYPE_REF_KEY) {
                            if (mapSize != 1) {
                                throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                            }
                            typeByteTemp = input.readByte();

                            // Check for nested JODA_TYPE_META
                            if (typeByteTemp == EXT_8) {
                                int size = input.readUnsignedByte();
                                typeByteTemp = input.readByte();
                                // Meta is the only type serialized using EXT_8
                                if (typeByteTemp != JODA_TYPE_META) {
                                    throw new IllegalArgumentException("Invalid binary data: Expected previous metatype, but was: 0x" + toHex(typeByteTemp));
                                }
                                metaType = acceptStringBytes(size);
                                refs[reference] = metaType;
                                typeByte = input.readByte();
                            } else {
                                input.reset();
                                break;
                            }
                        } else {
                            input.reset();
                            break;
                        }
                    } else {
                        input.reset();
                        break;
                    }
                } else {
                    input.reset();
                    break;
                }
            } else {
                input.reset();
                break;
            }
        }

        if (isArray(typeByte)) {
            input.mark(11);
            int arraySize = acceptArray(typeByte);
            if (arraySize > 0) {
                int typeByteTemp = input.readByte();
                if (isIntExtension(typeByteTemp)) {
                    int nestedTypeByteTemp = typeByteTemp;
                    typeByteTemp = input.readByte();
                    int reference = acceptIntExtension(nestedTypeByteTemp);

                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        classInfo = classes[reference];
                        Object bean = parseBean(declaredType, rootType, classInfo, arraySize);
                        if (ref != null) {
                            refs[ref] = bean;
                        }
                        return bean;
                    } else {
                        input.reset();
                    }
                } else {
                    input.reset();
                }
            } else {
                input.reset();
            }
        }

        if (classInfo != null) {
            effectiveType = classInfo.type;
        }

        if (isIntExtension(typeByte)) {
            input.mark(5);
            int typeByteTemp = input.readByte();
            int reference = acceptIntExtension(typeByte);
            // JODA_TYPE_REF is the only thing serialized in isolation, others are serialized as map keys or the start of an array
            if (typeByteTemp != JODA_TYPE_REF) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was: 0x" + toHex(typeByteTemp));
            }
            Object value = refs[reference];
            if (value == null && !this.ignoringObject) {
                // only throw if we care about the result
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was null: " + reference);
            }
            if (value != null && classInfo != null && classInfo.isNotValid() && !this.ignoringObject) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but could not deserialize original: " + classInfo.name + ", " + value);
            }
            if (value != null && !(effectiveType.isAssignableFrom(value.getClass())) && value instanceof String) {
                // May have deserialized as String due to the reference being initialized in a now deleted field
                // Regular beans won't hit this as they always declare types
                value = settings.getConverter().convertFromString(effectiveType, (String) value);
                refs[reference] = value;
            }
            return value;
        }
        Object value = parseObject(metaProp, beanType, parentIterable, effectiveType, metaType, typeByte);

        if (ref != null) {
            refs[ref] = value; // This object was keyed and is repeated
        }

        return value;
    }

    private Object parseObject(
            MetaProperty<?> metaProp,
            Class<?> beanType,
            SerIterable parentIterable,
            Class<?> effectiveType,
            String metaType,
            int typeByte) throws Exception {

        // parse based on type
        if (typeByte == NIL) {
            return null;
        }
        if (Bean.class.isAssignableFrom(effectiveType)) {
            if (isArray(typeByte)) {
                int arraySize = acceptArray(typeByte);
                return parseBean(arraySize, classMap.get(effectiveType));
            } else {
                return parseSimple(typeByte, effectiveType);
            }
        } else {
            if (isMap(typeByte) || isArray(typeByte)) {
                SerIterable childIterable = null;
                if (metaType != null) {
                    childIterable = settings.getIteratorFactory().createIterable(metaType, settings, null);
                } else if (metaProp != null) {
                    childIterable = settings.getIteratorFactory().createIterable(metaProp, beanType);
                } else if (parentIterable != null) {
                    childIterable = settings.getIteratorFactory().createIterable(parentIterable);
                } else if (this.ignoringObject && isMap(typeByte)) {
                    childIterable = SerIteratorFactory.map(Object.class, Object.class, Collections.emptyList());
                } else if (this.ignoringObject && isArray(typeByte)) {
                    childIterable = SerIteratorFactory.array(Object.class);
                }
                if (childIterable == null) {
                    throw new IllegalArgumentException("Invalid binary data: Invalid metaType: " + metaType);
                }
                return parseIterable(typeByte, childIterable);
            } else {
                return parseSimple(typeByte, effectiveType);
            }
        }
    }

    private Object parseBean(Class<?> declaredType, boolean rootType, ClassInfo classInfo, int arraySize) {
        if (rootType) {
            if (Bean.class.isAssignableFrom(classInfo.type) == false) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + classInfo.type.getName());
            }
            overrideBasePackage = classInfo.type.getPackage().getName() + ".";
        }
        if (declaredType.isAssignableFrom(classInfo.type) == false) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + classInfo.type.getName());
        }
        return parseBean(arraySize - 1, classInfo);
    }

    //-----------------------------------------------------------------------
    private boolean isIntExtension(int typeByte) {
        return typeByte == MsgPack.FIX_EXT_1 || typeByte == MsgPack.FIX_EXT_2 || typeByte == MsgPack.FIX_EXT_4;
    }

    private int acceptIntExtension(int typeByte) throws IOException {
        if (typeByte == MsgPack.FIX_EXT_1) {
            return input.readUnsignedByte();
        }
        if (typeByte == MsgPack.FIX_EXT_2) {
            return input.readUnsignedShort();
        }
        if (typeByte == MsgPack.FIX_EXT_4) {
            return input.readInt();
        }
        throw new IllegalArgumentException(
                "Invalid binary data: Expected int extension type, but was: 0x" + toHex(typeByte));
    }

    //-----------------------------------------------------------------------
    // The info needed to deserialize instances of a class with a reference to the initially serialized class definition
    private static final class ClassInfo {

        // The class itself
        private final Class<?> type;
        
        // The class name
        private final String name;

        // The metaproperties (empty if not a bean) in the order in which they were serialized
        private final MetaProperty<?>[] metaProperties;

        private ClassInfo(Class<?> type, String name, MetaProperty<?>[] metaProperties) {
            this.type = type;
            this.name = name;
            this.metaProperties = metaProperties;
        }
        
        private boolean isNotValid() {
            // We can never serialize as Object
            return type == Object.class;
        }

        @Override
        public String toString() {
            return "ClassInfo{" +
                    "type=" + type +
                    ", metaProperties=" + Arrays.toString(metaProperties) +
                    '}';
        }
    }

}
