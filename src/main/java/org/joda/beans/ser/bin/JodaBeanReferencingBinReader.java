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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterable;
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
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
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
        var parsed = parseObject(declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    //-----------------------------------------------------------------------
    // parses the references
    private void parseClassDescriptions() throws Exception {
        var refCount = acceptInteger(input.readByte());
        if (refCount < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected count of references, but was: " + refCount);
        }
        refs = new Object[refCount];

        var classMapSize = acceptMap(input.readByte());
        classes = new ClassInfo[classMapSize]; // Guaranteed non-negative by acceptMap()
        classMap = new HashMap<>(classMapSize);

        for (var position = 0; position < classMapSize; position++) {
            var classInfo = parseClassInfo();
            classes[position] = classInfo;
            classMap.put(classInfo.type, classInfo);
        }
    }

    // parses the class information
    private ClassInfo parseClassInfo() throws Exception {
        var className = acceptString(input.readByte());
        Class<?> type = SerTypeMapper.decodeType(className, settings, overrideBasePackage, null);
        var propertyCount = acceptArray(input.readByte());
        if (propertyCount < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected array with 0 to many elements, but was: " + propertyCount);
        }

        var metaProperties = new MetaProperty<?>[propertyCount];
        if (ImmutableBean.class.isAssignableFrom(type)) {
            var deser = settings.getDeserializers().findDeserializer(type);
            var metaBean = deser.findMetaBean(type);
            for (var i = 0; i < propertyCount; i++) {
                var propertyName = acceptString(input.readByte());
                metaProperties[i] = deser.findMetaProperty(type, metaBean, propertyName);
            }
        } else if (propertyCount != 0) {
            throw new IllegalArgumentException("Invalid binary data: Found non immutable bean class that has meta properties defined: " + type.getName() + ", " + propertyCount + " properties");
        }
        return new ClassInfo(type, metaProperties);
    }

    // parses the bean using the class information
    private Object parseBean(int propertyCount, ClassInfo classInfo) {
        var propName = "";
        if (classInfo.metaProperties.length != propertyCount) {
            throw new IllegalArgumentException("Invalid binary data: Expected " + classInfo.metaProperties.length + " properties but was: " + propertyCount);
        }
        try {
            var deser = settings.getDeserializers().findDeserializer(classInfo.type);
            var metaBean = deser.findMetaBean(classInfo.type);
            BeanBuilder<?> builder = deser.createBuilder(classInfo.type, metaBean);
            for (MetaProperty<?> metaProp : classInfo.metaProperties) {
                if (metaProp == null) {
                    MsgPackInput.skipObject(input);
                } else {
                    propName = metaProp.name();
                    var value = parseObject(SerOptional.extractType(metaProp, classInfo.type), metaProp, classInfo.type, null, false);
                    var wrappedValue = SerOptional.wrapValue(metaProp, classInfo.type, value);
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

        // Unwrap nested references and metadata
        while (isMap(typeByte)) {

            input.mark(18);
            var mapSize = acceptMap(typeByte);
            if (mapSize > 0) {
                int typeByteTemp = input.readByte();

                if (isIntExtension(typeByteTemp)) {

                    var nestedTypeByteTemp = typeByteTemp;
                    typeByteTemp = input.readByte();
                    var reference = acceptIntExtension(nestedTypeByteTemp);

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
                        var value = refs[reference];
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
                    var size = input.readUnsignedByte();
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
                        var nestedTypeByteTemp = typeByteTemp;
                        typeByteTemp = input.readByte();
                        var reference = acceptIntExtension(nestedTypeByteTemp);
                        if (typeByteTemp == JODA_TYPE_REF_KEY) {
                            if (mapSize != 1) {
                                throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                            }
                            typeByteTemp = input.readByte();

                            // Check for nested JODA_TYPE_META
                            if (typeByteTemp == EXT_8) {
                                var size = input.readUnsignedByte();
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
            var arraySize = acceptArray(typeByte);
            if (arraySize > 0) {
                int typeByteTemp = input.readByte();
                if (isIntExtension(typeByteTemp)) {
                    var nestedTypeByteTemp = typeByteTemp;
                    typeByteTemp = input.readByte();
                    var reference = acceptIntExtension(nestedTypeByteTemp);

                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        classInfo = classes[reference];
                        var bean = parseBean(declaredType, rootType, classInfo, arraySize);
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

        if (isIntExtension(typeByte)) {
            input.mark(5);
            int typeByteTemp = input.readByte();
            var reference = acceptIntExtension(typeByte);
            // JODA_TYPE_REF is the only thing serialized in isolation, others are serialized as map keys or the start of an array
            if (typeByteTemp != JODA_TYPE_REF) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was: 0x" + toHex(typeByteTemp));
            }
            var value = refs[reference];
            if (value == null) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was null: " + reference);
            }
            return value;
        }

        if (classInfo != null) {
            effectiveType = classInfo.type;
        }
        var value = parseObject(metaProp, beanType, parentIterable, effectiveType, metaType, typeByte);

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
                var arraySize = acceptArray(typeByte);
                var classInfo = classMap.computeIfAbsent(effectiveType, this::lookupClassInfo);
                return parseBean(arraySize, classInfo);
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

    // looks up information of classes that are not known upfront, e.g. are used by custom de-serializers
    private ClassInfo lookupClassInfo(Class<?> type) {
        var deser = settings.getDeserializers().findDeserializer(type);
        var metaBean = deser.findMetaBean(type);
        if (metaBean == null) {
            throw new RuntimeException("Could not find type: " + type.getName());
        }
        var propertyCount = metaBean.metaPropertyCount();
        MetaProperty<?>[] metaProperties = new MetaProperty[propertyCount];
        var i = 0;
        for (MetaProperty<?> metaProperty : metaBean.metaPropertyIterable()) {
            metaProperties[i++] = metaProperty;
        }
        return new ClassInfo(type, metaProperties);
    }

    private Object parseBean(Class<?> declaredType, boolean rootType, ClassInfo classInfo, int arraySize) {
        if (rootType) {
            if (!Bean.class.isAssignableFrom(classInfo.type)) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + classInfo.type.getName());
            }
            overrideBasePackage = classInfo.type.getPackage().getName() + ".";
        }
        if (!declaredType.isAssignableFrom(classInfo.type)) {
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

        // The metaproperties (empty if not a bean) in the order in which they need to be serialized
        private final MetaProperty<?>[] metaProperties;

        private ClassInfo(Class<?> type, MetaProperty<?>[] metaProperties) {
            this.type = type;
            this.metaProperties = metaProperties;
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
