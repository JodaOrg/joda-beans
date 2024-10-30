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
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from both the standard and referencing binary formats.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
abstract class AbstractBinReader extends MsgPack {

    /**
     * Settings.
     */
    final JodaBeanSer settings;  // CSIGNORE
    /**
     * The reader.
     */
    final DataInputStream input;  // CSIGNORE
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private final Map<String, Class<?>> knownTypes = new HashMap<>();

    //-----------------------------------------------------------------------
    // creates an instance
    AbstractBinReader(JodaBeanSer settings, DataInputStream input) {
        this.settings = settings;
        this.input = input;
    }

    //-----------------------------------------------------------------------
    // reads the input stream where the array and version bytes have been read already
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

    <T> T parseRemaining(Class<T> declaredType) throws Exception {
        // the array and version has already been read
        var parsed = parseObject(declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    Object parseBean(int propertyCount, Class<?> beanType) {
        var propName = "";
        try {
            var deser = settings.getDeserializers().findDeserializer(beanType);
            var metaBean = deser.findMetaBean(beanType);
            var builder = deser.createBuilder(beanType, metaBean);
            for (var i = 0; i < propertyCount; i++) {
                // property name
                propName = acceptPropertyName();
                var metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                if (metaProp == null || metaProp.style().isDerived()) {
                    MsgPackInput.skipObject(input);
                } else {
                    var value = parseObject(SerOptional.extractType(metaProp, beanType), metaProp, beanType, null, false);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                }
            }
            propName = "";
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    String acceptPropertyName() throws IOException {
        var typeByte = input.readByte();
        return acceptString(typeByte);
    }

    //-------------------------------------------------------------------------
    // parses an object, determining how to parse based on the input data
    Object parseObject(Class<?> declaredType, MetaProperty<?> metaProp, Class<?> beanType, SerIterable parentIterable, boolean rootType) throws Exception {
        var typeByte = input.readByte();

        // parse metadata
        if (isMap(typeByte)) {
            input.mark(8);
            var mapSize = acceptMap(typeByte);
            if (mapSize > 0) {
                int typeByteTemp = input.readByte();
                if (typeByteTemp == EXT_8) {
                    var size = input.readUnsignedByte();
                    typeByteTemp = input.readByte();
                    if (typeByteTemp == JODA_TYPE_BEAN) {
                        var typeStr = acceptStringBytes(size);
                        var effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
                        return parseObjectAsBean(declaredType, rootType, effectiveType, mapSize);
                    } else if (typeByteTemp == JODA_TYPE_DATA) {
                        if (mapSize != 1) {
                            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
                        }
                        var typeStr = acceptStringBytes(size);
                        var effectiveType = settings.getDeserializers().decodeType(
                                typeStr, settings, basePackage, knownTypes, declaredType);
                        if (!declaredType.isAssignableFrom(effectiveType)) {
                            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
                        }
                        return parseObjectFromInput(input.readByte(), effectiveType, metaProp, beanType, parentIterable);
                    } else if (typeByteTemp == JODA_TYPE_META) {
                        return parseObjectAsCollectionWithMeta(mapSize, size);
                    }
                }
            }
            input.reset();
        }
        // parse based on type
        return parseObjectFromInput(typeByte, declaredType, metaProp, beanType, parentIterable);
    }

    // a bean with an explicit type
    private Object parseObjectAsBean(Class<?> declaredType, boolean rootType, Class<?> effectiveType, int mapSize) throws Exception {
        if (rootType) {
            if (!Bean.class.isAssignableFrom(effectiveType)) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + effectiveType.getName());
            }
            basePackage = effectiveType.getPackage().getName() + ".";
        }
        if (!declaredType.isAssignableFrom(effectiveType)) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " + declaredType.getName() + " and " + effectiveType.getName());
        }
        if (input.readByte() != NIL) {
            throw new IllegalArgumentException("Invalid binary data: Expected null after bean type");
        }
        return parseBean(mapSize - 1, effectiveType);
    }

    // a collection with a meta annotation
    private Object parseObjectAsCollectionWithMeta(int mapSize, int strSize) throws Exception {
        if (mapSize != 1) {
            throw new IllegalArgumentException("Invalid binary data: Expected map size 1, but was: " + mapSize);
        }
        var metaType = acceptStringBytes(strSize);
        var typeByte = input.readByte();
        if (isMap(typeByte) || isArray(typeByte)) {
            var childIterable = settings.getIteratorFactory().createIterable(metaType, settings, knownTypes);
            if (childIterable == null) {
                throw new IllegalArgumentException("Invalid binary data: Invalid metaType: " + metaType);
            }
            return parseIterable(typeByte, childIterable);
        } else {
            throw new IllegalArgumentException("Invalid binary data: MetaType was not followed by a collection: " + metaType);
        }
    }

    private Object parseObjectFromInput(
            int typeByte,
            Class<?> effectiveType,
            MetaProperty<?> metaProp,
            Class<?> beanType,
            SerIterable parentIterable) throws Exception {

        if (typeByte == NIL) {
            return null;
        } else if (Bean.class.isAssignableFrom(effectiveType)) {
            if (isMap(typeByte)) {
                var mapSize = acceptMap(typeByte);
                return parseBean(mapSize, effectiveType);
            } else {
                return parseSimple(typeByte, effectiveType);
            }
        } else if (isMap(typeByte) || isArray(typeByte)) {
            SerIterable childIterable = null;
            if (metaProp != null) {
                childIterable = settings.getIteratorFactory().createIterable(metaProp, beanType);
            } else if (parentIterable != null) {
                childIterable = settings.getIteratorFactory().createIterable(parentIterable);
            }
            if (childIterable == null) {
                throw new IllegalArgumentException("Invalid binary data: Unable to create collection type");
            }
            return parseIterable(typeByte, childIterable);
        } else {
            return parseSimple(typeByte, effectiveType);
        }
    }

    Object parseIterable(int typeByte, SerIterable iterable) throws Exception {
        return switch (iterable.category()) {
            case COLLECTION -> parseIterableArray(typeByte, iterable);
            case COUNTED -> parseIterableCounted(typeByte, iterable);
            case MAP -> parseIterableMap(typeByte, iterable);
            case TABLE -> parseIterableTable(typeByte, iterable);
            case GRID -> parseIterableGrid(typeByte, iterable);
        };
    }

    Object parseIterableMap(int typeByte, SerIterable iterable) throws Exception {
        var size = acceptMap(typeByte);
        for (var i = 0; i < size; i++) {
            var key = parseObject(iterable.keyType(), null, null, null, false);
            var value = parseObject(iterable.valueType(), null, null, iterable, false);
            iterable.add(key, null, value, 1);
        }
        return iterable.build();
    }

    Object parseIterableTable(int typeByte, SerIterable iterable) throws Exception {
        var size = acceptArray(typeByte);
        for (var i = 0; i < size; i++) {
            if (acceptArray(input.readByte()) != 3) {
                throw new IllegalArgumentException("Table must have cell array size 3");
            }
            var key = parseObject(iterable.keyType(), null, null, null, false);
            var column = parseObject(iterable.columnType(), null, null, null, false);
            var value = parseObject(iterable.valueType(), null, null, iterable, false);
            iterable.add(key, column, value, 1);
        }
        return iterable.build();
    }

    Object parseIterableGrid(int typeByte, SerIterable iterable) throws Exception {
        var size = acceptArray(typeByte);
        var rows = acceptInteger(input.readByte());
        var columns = acceptInteger(input.readByte());
        iterable.dimensions(new int[] {rows, columns});
        if ((rows * columns) != (size - 2)) {
            // sparse
            for (var i = 0; i < (size - 2); i++) {
                if (acceptArray(input.readByte()) != 3) {
                    throw new IllegalArgumentException("Grid must have cell array size 3");
                }
                var row = acceptInteger(input.readByte());
                var column = acceptInteger(input.readByte());
                var value = parseObject(iterable.valueType(), null, null, iterable, false);
                iterable.add(row, column, value, 1);
            }
        } else {
            // dense
            for (var row = 0; row < rows; row++) {
                for (var column = 0; column < columns; column++) {
                    var value = parseObject(iterable.valueType(), null, null, iterable, false);
                    iterable.add(row, column, value, 1);
                }
            }
        }
        return iterable.build();
    }

    Object parseIterableCounted(int typeByte, SerIterable iterable) throws Exception {
        var size = acceptMap(typeByte);
        for (var i = 0; i < size; i++) {
            var value = parseObject(iterable.valueType(), null, null, iterable, false);
            var count = acceptInteger(input.readByte());
            iterable.add(null, null, value, count);
        }
        return iterable.build();
    }

    Object parseIterableArray(int typeByte, SerIterable iterable) throws Exception {
        var size = acceptArray(typeByte);
        for (var i = 0; i < size; i++) {
            iterable.add(null, null, parseObject(iterable.valueType(), null, null, iterable, false), 1);
        }
        return iterable.build();
    }

    //-------------------------------------------------------------------------
    Object parseSimple(int typeByte, Class<?> type) throws Exception {
        if (isString(typeByte)) {
            var text = acceptString(typeByte);
            if (type == String.class || type == Object.class) {
                return text;
            }
            return settings.getConverter().convertFromString(type, text);
        }
        if (isIntegral(typeByte)) {
            // ordered from common to less-common
            var value = acceptLong(typeByte);
            if (type == Long.class || type == long.class) {
                return Long.valueOf(value);

            } else if (type == Integer.class || type == int.class) {
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    throw new IllegalArgumentException("Invalid binary data: Expected int, but was " + value);
                }
                return Integer.valueOf((int) value);

            } else if (type == Double.class || type == double.class) {
                // handle case where property type has changed from integral to double
                return Double.valueOf(value);

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

            } else if (type == Float.class || type == float.class) {
                // handle case where property type has changed from integral to float
                return Float.valueOf(value);

            } else {
                if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                    return Long.valueOf(value);
                }
                return Integer.valueOf((int) value);
            }
        }
        return switch (typeByte) {
            case TRUE -> Boolean.TRUE;
            case FALSE -> Boolean.FALSE;
            case FLOAT_32 -> Float.valueOf(input.readFloat());
            case FLOAT_64 -> Double.valueOf(input.readDouble());
            case BIN_8, BIN_16, BIN_32 -> acceptBinary(typeByte);
            default -> throw invalidBinaryData(type.getName(), typeByte);
        };
    }

    //-----------------------------------------------------------------------
    int acceptMap(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_MAP && typeByte <= MAX_FIX_MAP) {
            return (typeByte - MIN_FIX_MAP);
        } else if (typeByte == MAP_16) {
            return input.readUnsignedShort();
        } else if (typeByte == MAP_32) {
            return readPositiveInt("Invalid binary data: Map too large");
        } else {
            throw invalidBinaryData("map", typeByte);
        }
    }

    int acceptArray(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_ARRAY && typeByte <= MAX_FIX_ARRAY) {
            return typeByte - MIN_FIX_ARRAY;
        } else if (typeByte == ARRAY_16) {
            return input.readUnsignedShort();
        } else if (typeByte == ARRAY_32) {
            return readPositiveInt("Invalid binary data: Array too large");
        } else {
            throw invalidBinaryData("array", typeByte);
        }
    }

    String acceptString(int typeByte) throws IOException {
        int size;
        if (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) {
            size = typeByte - MIN_FIX_STR;
        } else if (typeByte == STR_8) {
            size = input.readUnsignedByte();
        } else if (typeByte == STR_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == STR_32) {
            size = readPositiveInt("Invalid binary data: String too large");
        } else {
            throw invalidBinaryData("string", typeByte);
        }
        return acceptStringBytes(size);
    }

    String acceptStringBytes(int size) throws IOException {
        var bytes = new byte[size];
        input.readFully(bytes);
        // inline common ASCII case for much better performance
        var chars = new char[size];
        for (var i = 0; i < size; i++) {
            var b = bytes[i];
            if (b >= 0) {
                chars[i] = (char) b;
            } else {
                return new String(bytes, UTF_8);
            }
        }
        return new String(chars);
    }

    byte[] acceptBinary(int typeByte) throws IOException {
        int size = switch (typeByte) {
            case BIN_8 -> input.readUnsignedByte();
            case BIN_16 -> input.readUnsignedShort();
            case BIN_32 -> readPositiveInt("Invalid binary data: Binary too large");
            default -> throw invalidBinaryData("binary", typeByte);
        };
        var bytes = new byte[size];
        input.readFully(bytes);
        return bytes;
    }

    int acceptInteger(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        return switch (typeByte) {
            case UINT_8 -> input.readUnsignedByte();
            case UINT_16 -> input.readUnsignedShort();
            case UINT_32 -> readPositiveInt("Invalid binary data: Expected int, but was large unsigned int");
            case UINT_64 -> readUnsignedLongAsInt();
            case SINT_8 -> input.readByte();
            case SINT_16 -> input.readShort();
            case SINT_32 -> input.readInt();
            case SINT_64 -> readLongAsInt();
            default -> throw invalidBinaryData("int", typeByte);
        };
    }

    long acceptLong(int typeByte) throws IOException {
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        return switch (typeByte) {
            case UINT_8 -> input.readUnsignedByte();
            case UINT_16 -> input.readUnsignedShort();
            case UINT_32 -> Integer.toUnsignedLong(input.readInt());
            case UINT_64 -> readUnsignedLong();
            case SINT_8 -> input.readByte();
            case SINT_16 -> input.readShort();
            case SINT_32 -> input.readInt();
            case SINT_64 -> input.readLong();
            default -> throw invalidBinaryData("long", typeByte);
        };
    }

    private int readPositiveInt(String msg) throws IOException {
        var val = input.readInt();
        if (val < 0) {
            throw new IllegalArgumentException(msg);
        }
        return val;
    }

    private long readUnsignedLong() throws IOException {
        var val = input.readLong();
        if (val < 0) {
            throw new IllegalArgumentException("Invalid binary data: Expected long, but was large unsigned int");
        }
        return val;
    }

    private int readUnsignedLongAsInt() throws IOException {
        var val = input.readLong();
        if (val < 0 || val > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid binary data: Expected int, but was large unsigned int");
        }
        return (int) val;
    }

    private int readLongAsInt() throws IOException {
        var val = input.readLong();
        if (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Invalid binary data: Expected int, but was large signed int");
        }
        return (int) val;
    }

    private IllegalArgumentException invalidBinaryData(String expected, int actualByte) {
        return new IllegalArgumentException(
                "Invalid binary data: Expected " + expected + ", but was: 0x" + toHex(actualByte));
    }

}
