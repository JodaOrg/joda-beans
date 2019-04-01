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
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerDeserializer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from a compact binary format.
 * <p>
 * The binary format is defined by {@link JodaBeanCompactBinWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
public class AbstractBinReader extends MsgPack {

    /**
     * Settings.
     */
    protected final JodaBeanSer settings;
    /**
     * The reader.
     */
    protected DataInputStream input;
    /**
     * The base package including the trailing dot.
     */
    protected String basePackage;
    /**
     * The known types.
     */
    protected Map<String, Class<?>> knownTypes = new HashMap<>();

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     *
     * @param settings  the settings, not null
     */
    AbstractBinReader(final JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    protected Object parseBean(int propertyCount, Class<?> beanType) {
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
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    protected String acceptPropertyName() throws IOException {
        byte typeByte = input.readByte();
        return acceptString(typeByte);
    }

    protected Object parseObject(Class<?> declaredType, MetaProperty<?> metaProp, Class<?> beanType, SerIterable parentIterable, boolean rootType) throws Exception {
        // establish type
        Class<?> effectiveType = declaredType;
        String metaType = null;
        int typeByte = input.readByte();

        if (isMap(typeByte)) {
            input.mark(8);
            int mapSize = acceptMap(typeByte);
            if (mapSize > 0) {
                int typeByteTemp = input.readByte();
                if (typeByteTemp == EXT_8) {
                    int size = input.readUnsignedByte();
                    typeByteTemp = input.readByte();
                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        String typeStr = acceptStringBytes(size);
                        effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
                        return parseBean(declaredType, rootType, effectiveType, mapSize);
                    } else if (typeByteTemp == JODA_TYPE_DATA) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        String typeStr = acceptStringBytes(size);
                        effectiveType = settings.getDeserializers().decodeType(typeStr, settings, basePackage, knownTypes, declaredType);
                        if (declaredType.isAssignableFrom(effectiveType) == false) {
                            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
                        }
                        typeByte = input.readByte();
                    } else if (typeByteTemp == JODA_TYPE_META) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        metaType = acceptStringBytes(size);
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

    protected Object parseBean(Class<?> declaredType, boolean rootType, Class<?> effectiveType, int mapSize) throws Exception {
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

    protected Object parseIterable(int typeByte, SerIterable iterable) throws Exception {
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

    protected Object parseIterableMap(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptMap(typeByte);
        for (int i = 0; i < size; i++) {
            Object key = parseObject(iterable.keyType(), null, null, null, false);
            Object value = parseObject(iterable.valueType(), null, null, iterable, false);
            iterable.add(key, null, value, 1);
        }
        return iterable.build();
    }

    protected Object parseIterableTable(int typeByte, SerIterable iterable) throws Exception {
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

    protected Object parseIterableGrid(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptArray(typeByte);
        int rows = acceptInteger(input.readByte());
        int columns = acceptInteger(input.readByte());
        iterable.dimensions(new int[]{rows, columns});
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

    protected Object parseIterableCounted(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptMap(typeByte);
        for (int i = 0; i < size; i++) {
            Object value = parseObject(iterable.valueType(), null, null, iterable, false);
            int count = acceptInteger(input.readByte());
            iterable.add(null, null, value, count);
        }
        return iterable.build();
    }

    protected Object parseIterableArray(int typeByte, SerIterable iterable) throws Exception {
        int size = acceptArray(typeByte);
        for (int i = 0; i < size; i++) {
            iterable.add(null, null, parseObject(iterable.valueType(), null, null, iterable, false), 1);
        }
        return iterable.build();
    }

    protected Object parseSimple(int typeByte, Class<?> type) throws Exception {
        if (isString(typeByte)) {
            String text = acceptString(typeByte);
            if (type == String.class || type == Object.class) {
                return text;
            }
            return settings.getConverter().convertFromString(type, text);
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
    protected int acceptMap(int typeByte) throws IOException {
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

    protected int acceptArray(int typeByte) throws IOException {
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

    protected String acceptString(int typeByte) throws IOException {
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

    protected String acceptStringBytes(int size) throws IOException {
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

    protected byte[] acceptBinary(int typeByte) throws IOException {
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

    protected int acceptInteger(int typeByte) throws IOException {
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

    protected long acceptLong(int typeByte) throws IOException {
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
