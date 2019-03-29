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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from a binary format.
 * <p>
 * The binary format is defined by {@link JodaBeanBinWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanCompactBinReader extends AbstractBinReader {

    /**
     * The base package including the trailing dot.
     */
    private String basePackage;

    /**
     * The classes that have been serialized.
     */
    private ClassInfo[] classes;
    /**
     * The classes for lookup of classInfo when the class is known and unnecessary to serialise.
     */
    private Map<Class, ClassInfo> classMap;
    /**
     * The serialized objects that are repeated and referenced.
     */
    private Object[] refs;

    //-----------------------------------------------------------------------

    /**
     * Visualizes the binary data, writing to system out.
     *
     * @param input the input bytes, not null
     * @return the visualization
     */
    public static String visualize(byte[] input) {
        return new MsgPackVisualizer(input).visualizeData();
    }

    //-----------------------------------------------------------------------

    /**
     * Creates an instance.
     *
     * @param settings the settings, not null
     */
    public JodaBeanCompactBinReader(final JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------

    /**
     * Reads and parses to an immutable bean.
     *
     * @param input the input bytes, not null
     * @return the bean, not null
     */
    public ImmutableBean read(final byte[] input) {
        return read(input, ImmutableBean.class);
    }

    /**
     * Reads and parses to an immutable bean.
     *
     * @param <T> the root type
     * @param input the input bytes, not null
     * @param rootType the root type, not null
     * @return the bean, not null
     */
    public <T extends ImmutableBean> T read(final byte[] input, Class<T> rootType) {
        return read(new ByteArrayInputStream(input), rootType);
    }

    /**
     * Reads and parses to an immutable bean.
     *
     * @param input the input reader, not null
     * @return the bean, not null
     */
    public ImmutableBean read(final InputStream input) {
        return read(input, ImmutableBean.class);
    }

    /**
     * Reads and parses to an immutable bean.
     *
     * @param <T> the root type
     * @param input the input stream, not null
     * @param rootType the root type, not null
     * @return the bean, not null
     */
    public <T extends ImmutableBean> T read(final InputStream input, Class<T> rootType) {
        if (input instanceof DataInputStream) {
            this.input = (DataInputStream) input;
        } else {
            this.input = new DataInputStream(input);
        }
        try {
            try {
                return parseRoot(rootType);
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

    /**
     * Parses the root bean.
     *
     * @param declaredType the declared type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    protected <T> T parseRoot(Class<T> declaredType) throws Exception {
        if (!ImmutableBean.class.isAssignableFrom(declaredType)) {
            throw new IllegalArgumentException("Must deserialise to an ImmutableBean instance: " + declaredType.getName());
        }

        // root array
        int typeByte = input.readByte();
        if (typeByte != MIN_FIX_ARRAY + 4) {
            throw new IllegalArgumentException("Invalid binary data: Expected array with 4 elements, but was: 0x" + toHex(typeByte));
        }
        // version
        typeByte = input.readByte();
        if (typeByte != 2) {
            throw new IllegalArgumentException("Invalid binary data: Expected version 2, but was: 0x" + toHex(typeByte));
        }

        basePackage = declaredType.getPackage().getName() + ".";
        // ref count + class map
        parseClassMap();

        // parse
        Object parsed = parseObject(declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    private void parseClassMap() throws Exception {
        int refCount = acceptInteger(input.readByte());
        if (refCount <= 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected count of references, but was: " + refCount);
        }
        this.refs = new Object[refCount];

        int classMapSize = acceptMap(input.readByte());
        classes = new ClassInfo[classMapSize]; // Guaranteed non-negative by acceptMap()
        classMap = new HashMap<>(classMapSize);

        for (int position = 0; position < classMapSize; position++) {
            ClassInfo classInfo = parseClassInfo();
            classes[position] = classInfo;
            classMap.put(classInfo.type, classInfo);
        }

    }

    private ClassInfo parseClassInfo() throws Exception {
        String className = acceptString(input.readByte());
        Class<?> type = SerTypeMapper.decodeType(className, settings, basePackage, null);
        int propertyCount = acceptArray(input.readByte());
        if (propertyCount < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected array with 0 to many elements, but was: " + propertyCount);
        }

        MetaProperty<?>[] metaProperties = new MetaProperty<?>[propertyCount];
        if (ImmutableBean.class.isAssignableFrom(type)) {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(type);
            MetaBean metaBean = deser.findMetaBean(type);
            for (int j = 0; j < propertyCount; j++) {
                String propertyName = acceptString(input.readByte());
                metaProperties[j] = deser.findMetaProperty(type, metaBean, propertyName);
            }
        }
        return new ClassInfo(type, metaProperties);
    }

    protected Object parseBean(int propertyCount, ClassInfo classInfo) {
        String propName = "";
        if (classInfo.metaProperties.length != propertyCount) {
            throw new IllegalArgumentException("Invalid binary data: Expected " + classInfo.metaProperties.length + " properties but was: " + propertyCount);
        }
        try {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(classInfo.type);
            MetaBean metaBean = deser.findMetaBean(classInfo.type);
            BeanBuilder<?> builder = deser.createBuilder(classInfo.type, metaBean);
            for (MetaProperty<?> metaProp : classInfo.metaProperties) {
                if (metaProp == null) {
                    MsgPackInput.skipObject(input);
                } else {
                    propName = metaProp.name();
                    Object value = parseObject(SerOptional.extractType(metaProp, classInfo.type), metaProp, classInfo.type, null, false);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, classInfo.type, value));
                }
            }
            return deser.build(classInfo.type, builder);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + classInfo.type.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    protected Object parseObject(Class<?> declaredType, MetaProperty<?> metaProp, Class<?> beanType, SerIterable parentIterable, boolean rootType) throws Exception {
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
                if (typeByteTemp == FIX_EXT_4) {
                    typeByteTemp = input.readByte();
                    int reference = input.readInt();
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
                    // Check for nested JODA_TYPE_META with a reference the key
                    if (typeByteTemp == FIX_EXT_4) {
                        typeByteTemp = input.readByte();
                        int reference = input.readInt();
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
                if (typeByteTemp == FIX_EXT_4) {
                    typeByteTemp = input.readByte();
                    int reference = input.readInt();
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

        if (typeByte == FIX_EXT_4) {
            input.mark(5);
            int typeByteTemp = input.readByte();
            int reference = input.readInt();
            // JODA_TYPE_REF is the only thing serialized in isolation, others are serialized as map keys or the start of an array
            if (typeByteTemp != JODA_TYPE_REF) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was: 0x" + toHex(typeByteTemp));
            }
            Object value = refs[reference];
            if (value == null) {
                throw new IllegalArgumentException("Invalid binary data: Expected reference to previous object, but was null: " + reference);
            }
            return value;
        }

        if (classInfo != null) {
            effectiveType = classInfo.type;
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

    private Object parseBean(Class<?> declaredType, boolean rootType, ClassInfo classInfo, int arraySize) throws
            Exception {
        if (rootType) {
            if (Bean.class.isAssignableFrom(classInfo.type) == false) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + classInfo.type.getName());
            }
            basePackage = classInfo.type.getPackage().getName() + ".";
        }
        if (declaredType.isAssignableFrom(classInfo.type) == false) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + classInfo.type.getName());
        }
        return parseBean(arraySize - 1, classInfo);
    }

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
