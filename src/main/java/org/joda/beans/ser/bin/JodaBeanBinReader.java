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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
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
public class JodaBeanBinReader extends MsgPack {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * The reader.
     */
    private DataInputStream input;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private Map<String, Class<?>> knownTypes = new HashMap<>();
    /**
     * The known types, used for back reference.
     */
    private ArrayList<Class<?>> knownTypeReferences = new ArrayList<>();
    /**
     * The known deserialized objects, used for back reference.
     */
    private ArrayList<Object> serializedReferences = new ArrayList<>();
    /**
     * The known deserialized immutable objects, used for back reference.
     */
    private ArrayList<Object> serializedImmutableReferences = new ArrayList<>();
    /**
     * The known property names, used for back reference.
     */
    private ArrayList<String> knownPropertyNameReferences = new ArrayList<>();

    //-----------------------------------------------------------------------
    /**
     * Visualizes the binary data, writing to system out.
     * 
     * @param input  the input bytes, not null
     * @return the visualization
     */
    public static String visualize(byte[] input) {
        return new MsgPackVisualizer(input).visualizeData();
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanBinReader(final JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input bytes, not null
     * @return the bean, not null
     */
    public Bean read(final byte[] input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input bytes, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(final byte[] input, Class<T> rootType) {
        return read(new ByteArrayInputStream(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(final InputStream input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input stream, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(final InputStream input, Class<T> rootType) {
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
     * @param declaredType  the declared type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    private <T> T parseRoot(final Class<T> declaredType) throws Exception {
        // root array
        int typeByte = input.readByte();
        if (typeByte != MIN_FIX_ARRAY + 2) {
            throw new IllegalArgumentException("Invalid binary data: Expected array, but was: 0x" + toHex(typeByte));
        }
        // version
        typeByte = input.readByte();
        if (typeByte != 1 && typeByte != 2) {
            throw new IllegalArgumentException("Invalid binary data: Expected version 1 or 2, but was: 0x" + toHex(typeByte));
        }
        // parse
        Object parsed = parseObject(declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    private Object parseBean(int propertyCount, Class<?> beanType) throws Exception {
        String propName = "";
        try {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(beanType);
            MetaBean metaBean = deser.findMetaBean(beanType);
            BeanBuilder<?> builder = deser.createBuilder(beanType, metaBean);
            for (int i = 0; i < propertyCount; i++) {
                // property name
                propName = acceptPropertyName();
                MetaProperty<?> metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                if (metaProp == null || metaProp.style().isDerived()) {
                    MsgPackInput.skipObject(input);
                } else {
                    Object value = parseObject(SerOptional.extractType(metaProp, beanType), metaProp, beanType, null, false);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                }
                propName = "";
            }
            Object bean = deser.build(beanType, builder);
            if (settings.isReferences()) {
                if (bean instanceof ImmutableBean && settings.getImmutableClasses().contains(bean.getClass())) {
                    serializedImmutableReferences.add(bean);
                } else {
                    serializedReferences.add(bean);
                }
            }
            return bean;
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    private String acceptPropertyName() throws IOException {
        String propName;
        byte typeByte = input.readByte();
        if (typeByte == FIX_EXT_4) {
            typeByte = input.readByte();
            if (typeByte != JODA_TYPE_PROP_NAME) {
                throw new IllegalArgumentException("Invalid binary data: Expected previously serialized property name reference but was: 0x" + toHex(typeByte));
            }
            int reference = input.readInt();
            Object ref = serializedReferences.get(reference);
            if (!(ref instanceof String)) {
                throw new IllegalArgumentException("Invalid binary data: Expected previously serialized property name reference but was: 0x" + toHex(typeByte) + ", " + ref.toString());
            }
            propName = (String) ref;
        } else {
            propName = acceptString(typeByte);
            serializedReferences.add(propName);
        }
        return propName;
    }

    private Object parseObject(Class<?> declaredType, MetaProperty<?> metaProp, Class<?> beanType, SerIterable parentIterable, boolean rootType) throws Exception {
        // establish type
        Class<?> effectiveType = declaredType;
        String metaType = null;
        int typeByte = input.readByte();

        // is reference
        if (typeByte == FIX_EXT_4) {
            input.mark(5);
            int typeByteTemp = input.readByte();
            int reference = input.readInt();
            if (typeByteTemp == JODA_TYPE_REF) {
                return serializedReferences.get(reference);
            } else if (typeByteTemp == JODA_TYPE_IMM_REF) {
                return serializedImmutableReferences.get(reference);
            } else {
                input.reset();
            }
        }

        if (isMap(typeByte)) {
            input.mark(8);
            int mapSize = acceptMap(typeByte);
            if (mapSize > 0) {
                int typeByteTemp = input.readByte();
                if (typeByteTemp == FIX_EXT_4) {
                    typeByteTemp = input.readByte();
                    int reference = input.readInt();
                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        effectiveType = knownTypeReferences.get(reference);
                        return parseBean(declaredType, rootType, effectiveType, mapSize);
                    } else if (typeByteTemp == JODA_TYPE_DATA) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        effectiveType = knownTypeReferences.get(reference);
                        if (declaredType.isAssignableFrom(effectiveType) == false) {
                            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
                        }
                        typeByte = input.readByte();
                    } else if (typeByteTemp == JODA_TYPE_META) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        metaType = knownPropertyNameReferences.get(reference);
                        if (metaType == null) {
                            throw new IllegalArgumentException("Invalid binary data: Expected reference to prior metatype name up to " + knownPropertyNameReferences.size() + ": " + reference);
                        }
                        typeByte = input.readByte();
                    } else {
                        input.reset();
                    }
                } else if (typeByteTemp == EXT_8) {
                    int size = input.readUnsignedByte();
                    typeByteTemp = input.readByte();
                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        String typeStr = acceptStringBytes(size);
                        effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
                        knownTypeReferences.add(effectiveType);
                        return parseBean(declaredType, rootType, effectiveType, mapSize);
                    } else if (typeByteTemp == JODA_TYPE_DATA) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        String typeStr = acceptStringBytes(size);
                        effectiveType = settings.getDeserializers().decodeType(typeStr, settings, basePackage, knownTypes, declaredType);
                        knownTypeReferences.add(effectiveType);
                        if (declaredType.isAssignableFrom(effectiveType) == false) {
                            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
                        }
                        typeByte = input.readByte();
                    } else if (typeByteTemp == JODA_TYPE_META) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        metaType = acceptStringBytes(size);
                        knownPropertyNameReferences.add(metaType);
                        typeByte = input.readByte();
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
        // parse based on type
        if (typeByte == NIL) {
            return null;
        }
        if (Bean.class.isAssignableFrom(effectiveType)) {
            if (isMap(typeByte)) {
                int mapSize = acceptMap(typeByte);
                return parseBean(mapSize, effectiveType);
            } else {
                return parseSimple(typeByte, effectiveType);
            }
        } else {
            if (isMap(typeByte) || isArray(typeByte)) {
                SerIterable childIterable = null;
                if (metaType != null) {
                    childIterable = settings.getIteratorFactory().createIterable(metaType, settings, knownTypes);
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

    private Object parseBean(Class<?> declaredType, boolean rootType, Class<?> effectiveType, int mapSize) throws Exception {
        if (rootType) {
            if (Bean.class.isAssignableFrom(effectiveType) == false) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + effectiveType.getName());
            }
            basePackage = effectiveType.getPackage().getName() + ".";
        }
        if (declaredType.isAssignableFrom(effectiveType) == false) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
        }
        if (input.readByte() != NIL) {
            throw new IllegalArgumentException("Invalid binary data: Expected null after bean type");
        }
        return parseBean(mapSize - 1, effectiveType);
    }

    private Object parseIterable(int typeByte, SerIterable iterable) throws Exception {
        if (iterable.category() == SerCategory.MAP) {
            return parseIterableMap(typeByte, iterable);
        } else if (iterable.category() == SerCategory.COUNTED) {
            return parseIterableCounted(typeByte, iterable);
        } else if (iterable.category() == SerCategory.TABLE) {
            return parseIterableTable(typeByte, iterable);
        } else if (iterable.category() == SerCategory.GRID) {
            return parseIterableGrid(typeByte, iterable);
        } else {
            return parseIterableArray(typeByte, iterable);
        }
    }

    private Object parseIterableMap(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptMap(typeByte);
        for (int i = 0; i < size; i++) {
            Object key = parseObject(iterable.keyType(), null, null, null, false);
            Object value = parseObject(iterable.valueType(), null, null, iterable, false);
            iterable.add(key, null, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableTable(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptArray(typeByte);
        for (int i = 0; i < size; i++) {
            if (acceptArray(input.readByte()) != 3) {
                throw new IllegalArgumentException("Table must have cell array size 3");
            }
            Object key = parseObject(iterable.keyType(), null, null, null, false);
            Object column = parseObject(iterable.columnType(), null, null, null, false);
            Object value = parseObject(iterable.valueType(), null, null, iterable, false);
            iterable.add(key, column, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableGrid(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptArray(typeByte);
        int rows = acceptInteger(input.readByte());
        int columns = acceptInteger(input.readByte());
        iterable.dimensions(new int[] {rows, columns});
        if ((rows * columns) != (size - 2)) {
            // sparse
            for (int i = 0; i < (size - 2); i++) {
                if (acceptArray(input.readByte()) != 3) {
                    throw new IllegalArgumentException("Grid must have cell array size 3");
                }
                int row = acceptInteger(input.readByte());
                int column = acceptInteger(input.readByte());
                Object value = parseObject(iterable.valueType(), null, null, iterable, false);
                iterable.add(row, column, value, 1);
            }
        } else {
            // dense
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    Object value = parseObject(iterable.valueType(), null, null, iterable, false);
                    iterable.add(row, column, value, 1);
                }
            }
        }
        return iterable.build();
    }

    private Object parseIterableCounted(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptMap(typeByte);
        for (int i = 0; i < size; i++) {
            Object value = parseObject(iterable.valueType(), null, null, iterable, false);
            int count = acceptInteger(input.readByte());
            iterable.add(null, null, value, count);
        }
        return iterable.build();
    }

    private Object parseIterableArray(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptArray(typeByte);
        for (int i = 0; i < size; i++) {
            iterable.add(null, null, parseObject(iterable.valueType(), null, null, iterable, false), 1);
        }
        return iterable.build();
    }

    private Object parseSimple(int typeByte, Class<?> type) throws Exception {
        if (typeByte == FIX_EXT_4) {
            byte tempTypeByte = input.readByte();
            if (tempTypeByte == JODA_TYPE_REF) {
                int reference = input.readInt();
                return serializedReferences.get(reference);
            } else if (tempTypeByte == JODA_TYPE_IMM_REF) {
                int reference = input.readInt();
                return serializedImmutableReferences.get(reference);
            } else {
                throw new IllegalArgumentException("Invalid binary data: Expected reference, but was 0x" + toHex(tempTypeByte));
            }
        }
        if (isString(typeByte)) {
            String text = acceptString(typeByte);
            if (type == String.class || type == Object.class) {
                if (settings.getImmutableClasses().contains(String.class)) {
                    serializedImmutableReferences.add(text);
                } else {
                    serializedReferences.add(text);
                }
                return text;
            }
            Object result = settings.getConverter().convertFromString(type, text);
            if (settings.getImmutableClasses().contains(type)) {
                serializedImmutableReferences.add(result);
            } else {
                serializedReferences.add(result);
            }
            return result;
        }
        if (isIntegral(typeByte)) {
            long value = acceptLong(typeByte);
            if (type == Long.class || type == long.class) {
                return Long.valueOf(value);

            } else if (type == Short.class || type == short.class) {
                if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected byte, but was " + value);
                }
                return Short.valueOf((short) value);

            } else if (type == Byte.class || type == byte.class) {
                if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected byte, but was " + value);
                }
                return Byte.valueOf((byte) value);

            } else if (type == Double.class || type == double.class) {
                // handle case where property type has changed from integral to double
                return Double.valueOf((double) value);

            } else if (type == Float.class || type == float.class) {
                // handle case where property type has changed from integral to float
                return Float.valueOf((float) value);

            } else {
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected int, but was " + value);
                }
                return Integer.valueOf((int) value);
            }
        }
        switch (typeByte) {
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            case FLOAT_32:
                return Float.valueOf(input.readFloat());
            case FLOAT_64:
                return Double.valueOf(input.readDouble());
            case BIN_8:
            case BIN_16:
            case BIN_32:
                return acceptBinary(typeByte);
        }
        throw new IllegalArgumentException("Invalid binary data: Expected " + type.getName() + ", but was: 0x" + toHex(typeByte));
    }

    //-----------------------------------------------------------------------
    private int acceptMap(int typeByte) throws IOException {
        int size;
        if (typeByte >= MIN_FIX_MAP && typeByte <= MAX_FIX_MAP) {
            size = (typeByte - MIN_FIX_MAP);
        } else if (typeByte == MAP_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == MAP_32) {
            size = input.readInt();
            if (size < 0) {
                throw new IllegalArgumentException("Invalid binary data: Map too large");
            }
        } else {
            throw new IllegalArgumentException("Invalid binary data: Expected map, but was: 0x" + toHex(typeByte));
        }
        return size;
    }

    private int acceptArray(int typeByte) throws IOException {
        int size;
        if (typeByte >= MIN_FIX_ARRAY && typeByte <= MAX_FIX_ARRAY) {
            size = (typeByte - MIN_FIX_ARRAY);
        } else if (typeByte == ARRAY_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == ARRAY_32) {
            size = input.readInt();
            if (size < 0) {
                throw new IllegalArgumentException("Invalid binary data: Array too large");
            }
        } else {
            throw new IllegalArgumentException("Invalid binary data: Expected array, but was: 0x" + toHex(typeByte));
        }
        return size;
    }

    private String acceptString(int typeByte) throws IOException {
        int size;
        if (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) {
            size = (typeByte - MIN_FIX_STR);
        } else if (typeByte == STR_8) {
            size = input.readUnsignedByte();
        } else if (typeByte == STR_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == STR_32) {
            size = input.readInt();
            if (size < 0) {
                throw new IllegalArgumentException("Invalid binary data: String too large");
            }
        } else {
            throw new IllegalArgumentException("Invalid binary data: Expected string, but was: 0x" + toHex(typeByte));
        }
        return acceptStringBytes(size);
    }

    private String acceptStringBytes(int size) throws IOException {
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        // inline common ASCII case for much better performance
        char[] chars = new char[size];
        for (int i = 0; i < size; i++) {
            byte b = bytes[i];
            if (b >= 0) {
                chars[i] = (char) b;
            } else {
                return new String(bytes, UTF_8);
            }
        }
        return new String(chars);
    }

    private byte[] acceptBinary(int typeByte) throws IOException {
        int size;
        if (typeByte == BIN_8) {
            size = input.readUnsignedByte();
        } else if (typeByte == BIN_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == BIN_32) {
            size = input.readInt();
            if (size < 0) {
                throw new IllegalArgumentException("Invalid binary data: Binary too large");
            }
        } else {
            throw new IllegalArgumentException("Invalid binary data: Expected binary, but was: 0x" + toHex(typeByte));
        }
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        return bytes;
    }

    private int acceptInteger(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        switch (typeByte) {
            case UINT_8:
                return input.readUnsignedByte();
            case UINT_16:
                return input.readUnsignedShort();
            case UINT_32: {
                int val = input.readInt();
                if (val < 0) {
                    throw new IllegalArgumentException("Invalid binary data: Expected int, but was large unsigned int");
                }
                return val;
            }
            case UINT_64: {
                long val = input.readLong();
                if (val < 0 || val > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected int, but was large unsigned int");
                }
                return (int) val;
            }
            case SINT_8:
                return input.readByte();
            case SINT_16:
                return input.readShort();
            case SINT_32:
                return input.readInt();
            case SINT_64: {
                long val = input.readLong();
                if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected int, but was large signed int");
                }
                return (int) val;
            }
        }
        throw new IllegalArgumentException("Invalid binary data: Expected int, but was: 0x" + toHex(typeByte));
    }

    private long acceptLong(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        switch (typeByte) {
            case UINT_8:
                return input.readUnsignedByte();
            case UINT_16:
                return input.readUnsignedShort();
            case UINT_32: {
                return ((long) input.readInt()) & 0xFFFFFFFFL;
            }
            case UINT_64: {
                long val = input.readLong();
                if (val < 0) {
                    throw new IllegalArgumentException("Invalid binary data: Expected long, but was large unsigned int");
                }
                return val;
            }
            case SINT_8:
                return input.readByte();
            case SINT_16:
                return input.readShort();
            case SINT_32:
                return input.readInt();
            case SINT_64: {
                return input.readLong();
            }
        }
        throw new IllegalArgumentException("Invalid binary data: Expected long, but was: 0x" + toHex(typeByte));
    }

}
