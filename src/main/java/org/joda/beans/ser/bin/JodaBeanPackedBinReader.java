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
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.collect.grid.DenseGrid;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;
import org.joda.collect.grid.SparseGrid;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultiset;

/**
 * Reads the Joda-Bean BeanPack binary format with strings deduplicated by reference.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
final class JodaBeanPackedBinReader extends BeanPack {

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<BinHandler> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected BinHandler computeValue(Class<?> type) {
            return BaseBinHandlers.INSTANCE.createHandler(type);
        }
    };

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
    /**
     * The type definitions.
     */
    private final List<Class<?>> typeDefinitions = new ArrayList<>();
    /**
     * The bean definitions.
     */
    private final Map<Class<?>, List<MetaProperty<?>>> beanDefinitions = new IdentityHashMap<>();
    /**
     * The value definitions.
     */
    private final List<Object> valueDefinitions = new ArrayList<>();

    //-----------------------------------------------------------------------
    // creates an instance
    JodaBeanPackedBinReader(JodaBeanSer settings, DataInputStream input) {
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
        basePackage = acceptStringOrNull();
        var parsed = parseObject(ResolvedType.from(declaredType), true);
        return declaredType.cast(parsed);
    }

    //-------------------------------------------------------------------------
    // parses an object, determining how to parse based on the input data
    Object parseObject(ResolvedType declaredType, boolean isBeanDefinition) throws Exception {
        var typeByte = input.readByte();

        // parse the type code
        return switch (typeByte) {
            case MIN_FIX_MAP -> parseMap(0, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 1 -> parseMap(1, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 2 -> parseMap(2, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 3 -> parseMap(3, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 4 -> parseMap(4, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 5 -> parseMap(5, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 6 -> parseMap(6, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 7 -> parseMap(7, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 8 -> parseMap(8, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 9 -> parseMap(9, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 10 -> parseMap(10, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 11 -> parseMap(11, declaredType, isBeanDefinition);
            case MIN_FIX_MAP + 12 -> parseMap(12, declaredType, isBeanDefinition);
            case MAP_8 -> parseMap(input.readUnsignedByte(), declaredType, isBeanDefinition);
            case MAP_16 -> parseMap(input.readUnsignedShort(), declaredType, isBeanDefinition);
            case MAP_32 -> parseMap(input.readInt(), declaredType, isBeanDefinition);
            case MIN_FIX_ARRAY -> parseArray(0, declaredType);
            case MIN_FIX_ARRAY + 1 -> parseArray(1, declaredType);
            case MIN_FIX_ARRAY + 2 -> parseArray(2, declaredType);
            case MIN_FIX_ARRAY + 3 -> parseArray(3, declaredType);
            case MIN_FIX_ARRAY + 4 -> parseArray(4, declaredType);
            case MIN_FIX_ARRAY + 5 -> parseArray(5, declaredType);
            case MIN_FIX_ARRAY + 6 -> parseArray(6, declaredType);
            case MIN_FIX_ARRAY + 7 -> parseArray(7, declaredType);
            case MIN_FIX_ARRAY + 8 -> parseArray(8, declaredType);
            case MIN_FIX_ARRAY + 9 -> parseArray(9, declaredType);
            case MIN_FIX_ARRAY + 10 -> parseArray(10, declaredType);
            case MIN_FIX_ARRAY + 11 -> parseArray(11, declaredType);
            case MIN_FIX_ARRAY + 12 -> parseArray(12, declaredType);
            case ARRAY_8 -> parseArray(input.readUnsignedByte(), declaredType);
            case ARRAY_16 -> parseArray(input.readUnsignedShort(), declaredType);
            case ARRAY_32 -> parseArray(input.readInt(), declaredType);
            case STR_8 -> parseString(input.readUnsignedByte(), declaredType);
            case STR_16 -> parseString(input.readUnsignedShort(), declaredType);
            case STR_32 -> parseString(input.readInt(), declaredType);
            case NULL -> null;
            case FALSE -> false;
            case TRUE -> true;
            case UNUSED -> parseUnknown();
            case FLOAT_32 -> input.readFloat();
            case DOUBLE_INT -> (double) acceptInt();
            case DOUBLE_64 -> input.readDouble();
            case BYTE_8 -> parseByte(input.readByte(), declaredType);
            case SHORT_16 -> parseShort(input.readShort(), declaredType);
            case INT_8 -> parseInt(input.readByte(), declaredType);
            case INT_16 -> parseInt(input.readShort(), declaredType);
            case INT_32 -> parseInt(input.readInt(), declaredType);
            case LONG_8 -> parseLong(input.readByte(), declaredType);
            case LONG_16 -> parseLong(input.readShort(), declaredType);
            case LONG_32 -> parseLong(input.readInt(), declaredType);
            case LONG_64 -> parseLong(input.readLong(), declaredType);
            case DATE_PACKED -> parseDatePacked();
            case DATE -> parseDate();
            case TIME -> parseTime();
            case INSTANT -> parseInstant();
            case DURATION -> parseDuration();
            case BIN_8 -> parseByteArray(input.readUnsignedByte());
            case BIN_16 -> parseByteArray(input.readUnsignedShort());
            case BIN_32 -> parseByteArray(input.readInt());
            case DOUBLE_ARRAY_8 -> parseDoubleArray(input.readUnsignedByte());
            case DOUBLE_ARRAY_16 -> parseDoubleArray(input.readUnsignedShort());
            case DOUBLE_ARRAY_32 -> parseDoubleArray(input.readInt());
            case BEAN_DEFN -> parseBeanDefinition(declaredType);
            case TYPE_DEFN -> parseTypeDefinition(declaredType, isBeanDefinition);
            case TYPE_REF -> parseTypeRef(declaredType, isBeanDefinition);
            case VALUE_DEFN -> parseValueDefinition(declaredType);
            case VALUE_REF -> parseValueRef();
            default -> typeByte < MIN_FIX_INT ?
                    parseString(typeByte - MIN_FIX_STR, declaredType) :
                    parseInt(typeByte, declaredType);
        };
    }

    //-------------------------------------------------------------------------
    // parse a BeanPack map
    private Object parseMap(int mapSize, ResolvedType declaredType, boolean isBeanDefinition) throws Exception {
        if (Bean.class.isAssignableFrom(declaredType.getRawType())) {
            return parseMapAsBean(mapSize, declaredType.getRawType(), isBeanDefinition);
        } else {
            return parseViaHandler(mapSize, declaredType);
        }
    }

    // parse a BeanPack array
    private Object parseArray(int arraySize, ResolvedType declaredType) throws Exception {
        if (Bean.class.isAssignableFrom(declaredType.getRawType())) {
            return parseArrayAsBean(arraySize, declaredType.getRawType());
        } else {
            return parseViaHandler(arraySize, declaredType);
        }
    }

    // parse a BeanPack string
    private Object parseString(int strLen, ResolvedType declaredType) throws Exception {
        var str = acceptStringBytes(strLen);
//        if (declaredType.getRawType() == String.class || declaredType.getRawType() == Object.class) {
        if (declaredType.getRawType().isAssignableFrom(String.class)) {
            return str;
        } else {
            // Joda-Convert
            return settings.getConverter().convertFromString(declaredType.getRawType(), str);
        }
    }

    Object parseUnknown() {
        throw new IllegalArgumentException("Invalid binary data: Unknown type byte");
    }

    //-------------------------------------------------------------------------
    // parse the type name, validate it, store it in the type cache and parse the actual value
    private Object parseTypeDefinition(ResolvedType declaredType, boolean isBeanDefinition) throws Exception {
        var typeName = acceptString();
        var decodedType = SerTypeMapper.decodeType(typeName, settings, basePackage, knownTypes, Object.class);
        var effectiveType = ResolvedType.from(decodedType);
        validateEffectiveType(declaredType, effectiveType);
        typeDefinitions.add(decodedType);
        return parseObject(effectiveType, isBeanDefinition);
    }

    // parse the int reference, negative for type codes and positive for user types
    private Object parseTypeRef(ResolvedType declaredType, boolean isBeanDefinition) throws Exception {
        var typeRef = acceptInt();
        if (typeRef < 0) {
            var effectiveType = ResolvedType.of(BaseBinHandlers.INSTANCE.classForTypeCode(typeRef));
            return parseObject(effectiveType, isBeanDefinition);
        } else {
            var effectiveType = ResolvedType.of(typeDefinitions.get(typeRef));
            validateEffectiveType(declaredType, effectiveType);
            return parseObject(effectiveType, isBeanDefinition);
        }
    }

    private void validateEffectiveType(ResolvedType declaredType, ResolvedType effectiveType) {
        if (!declaredType.getRawType().isAssignableFrom(effectiveType.getRawType())) {
            throw new IllegalArgumentException(
                    "Invalid binary data: Declared type " + declaredType +
                            " is incompatible with effective type " + effectiveType);
        }
    }

    // parse the actual bean, passing down a flag so that the meta-properties are stored in the bean cache
    private Object parseBeanDefinition(ResolvedType declaredType) throws Exception {
        var beanType = declaredType.getRawType();
        if (!Bean.class.isAssignableFrom(beanType)) {
            throw new IllegalArgumentException("Invalid binary data: Expected bean, but found " + declaredType);
        }
        // parse the meta-property names first
        var propertyCount = acceptInt();
        var deser = settings.getDeserializers().findDeserializer(beanType);
        var metaBean = deser.findMetaBean(beanType);
        var metaProperties = new ArrayList<MetaProperty<?>>(propertyCount);
        for (var i = 0; i < propertyCount; i++) {
            var propName = acceptString();
            var metaProp = deser.findMetaProperty(beanType, metaBean, propName);
            if (metaProp == null || !settings.isSerialized(metaProp)) {
                metaProperties.add(null);
            } else {
                metaProperties.add(metaProp);
            }
        }
        beanDefinitions.put(beanType, metaProperties);
        // now the meta-property names are stored, we can parse the bean values
        var propName = "";
        try {
            var builder = deser.createBuilder(beanType, metaBean);
            for (var i = 0; i < propertyCount; i++) {
                var metaProp = metaProperties.get(i);
                if (metaProp == null) {
                    skipObject();
                } else {
                    propName = metaProp.name();
                    var value = parseObject(metaProp.propertyResolvedType(beanType), false);
                    deser.setValue(builder, metaProp, value);
                }
            }
            propName = "";
            return deser.build(beanType, builder);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse the actual value, store it in the value cache, and return the value
    private Object parseValueDefinition(ResolvedType declaredType) throws Exception {
        var obj = parseObject(declaredType, false);
        valueDefinitions.add(obj);
        return obj;
    }

    // obtain the value reference, query the value cache, and return the value
    private Object parseValueRef() throws Exception {
        var ref = acceptInt();
        var value = valueDefinitions.get(ref);
        if (value == null) {
            throw new IllegalArgumentException("Invalid binary data: Referenced value not found: ref " + ref);
        }
        return value;
    }

    //-------------------------------------------------------------------------
    // parse a BeanPack map that has been identified as a bean
    private Object parseMapAsBean(int mapSize, Class<?> beanType, boolean isDefinition) throws Exception {
        var propName = "";
        try {
            var deser = settings.getDeserializers().findDeserializer(beanType);
            var metaBean = deser.findMetaBean(beanType);
            var builder = deser.createBuilder(beanType, metaBean);
            var metaProperties = new ArrayList<MetaProperty<?>>(mapSize);
            for (var i = 0; i < mapSize; i++) {
                propName = acceptString();
                var metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                if (metaProp == null || metaProp.style().isDerived()) {
                    skipObject();
                    metaProperties.add(null);
                } else {
                    var value = parseObject(metaProp.propertyResolvedType(beanType), false);
                    deser.setValue(builder, metaProp, value);
                    metaProperties.add(metaProp);
                }
            }
            if (isDefinition) {
                beanDefinitions.put(beanType, metaProperties);
            }
            propName = "";
            return deser.build(beanType, builder);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse a BeanPack array that has been identified as a bean (by reference)
    private Object parseArrayAsBean(int arraySize, Class<?> beanType) throws Exception {
        var propName = "";
        try {
            var deser = settings.getDeserializers().findDeserializer(beanType);
            var metaBean = deser.findMetaBean(beanType);
            var builder = deser.createBuilder(beanType, metaBean);
            var metaProperties = beanDefinitions.get(beanType);
            if (metaProperties == null) {
                throw new IllegalArgumentException(
                        "Invalid binary data: Referenced bean not found: " + beanType.getName());
            }
            if (metaProperties.size() != arraySize) {
                throw new IllegalArgumentException(
                        "Invalid binary data: Referenced bean had different number of properties: " + beanType.getName());
            }
            for (var i = 0; i < arraySize; i++) {
                var metaProp = metaProperties.get(i);
                if (metaProp == null) {
                    skipObject();
                } else {
                    propName = metaProp.name();
                    var value = parseObject(metaProp.propertyResolvedType(beanType), false);
                    deser.setValue(builder, metaProp, value);
                }
            }
            propName = "";
            return deser.build(beanType, builder);
        } catch (IOException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse a BeanPack map as a Map
    private Object parseViaHandler(int size, ResolvedType declaredType) throws Exception {
        var handler = LOOKUP.get(declaredType.getRawType());
        return handler.handle(this, declaredType, size);
    }

    //-------------------------------------------------------------------------
    // parse a byte, allowing conversion to other number types
    private Object parseByte(byte value, ResolvedType declaredType) {
        var desiredType = declaredType.getRawType();
        if (desiredType == Byte.class || desiredType == byte.class || desiredType == Object.class) {
            return value;
        } else if (desiredType == Integer.class || desiredType == int.class) {
            return Integer.valueOf(value);
        } else if (desiredType == Long.class || desiredType == long.class) {
            return Long.valueOf(value);
        } else if (desiredType == Double.class || desiredType == double.class) {
            return Double.valueOf(value);
        } else if (desiredType == Float.class || desiredType == float.class) {
            return Float.valueOf(value);
        } else if (desiredType == Short.class || desiredType == short.class) {
            return Short.valueOf(value);
        }
        return value;
    }

    // parse a short, allowing conversion to other number types
    private Object parseShort(short value, ResolvedType declaredType) {
        var desiredType = declaredType.getRawType();
        if (desiredType == Short.class || desiredType == short.class || desiredType == Object.class) {
            return value;
        } else if (desiredType == Integer.class || desiredType == int.class) {
            return Integer.valueOf(value);
        } else if (desiredType == Long.class || desiredType == long.class) {
            return Long.valueOf(value);
        } else if (desiredType == Double.class || desiredType == double.class) {
            return Double.valueOf(value);
        } else if (desiredType == Float.class || desiredType == float.class) {
            return Float.valueOf(value);
        } else if (desiredType == Byte.class || desiredType == byte.class) {
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected byte, but was " + value);
            }
            return Byte.valueOf((byte) value);
        }
        return value;
    }

    // parse an int, allowing conversion to other number types
    private Object parseInt(int value, ResolvedType declaredType) {
        var desiredType = declaredType.getRawType();
        if (desiredType == Integer.class || desiredType == int.class || desiredType == Object.class) {
            return value;
        } else if (desiredType == Long.class || desiredType == long.class) {
            return Long.valueOf(value);
        } else if (desiredType == Double.class || desiredType == double.class) {
            return Double.valueOf(value);
        } else if (desiredType == Float.class || desiredType == float.class) {
            return Float.valueOf(value);
        } else if (desiredType == Short.class || desiredType == short.class) {
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected short, but was " + value);
            }
            return Short.valueOf((short) value);
        } else if (desiredType == Byte.class || desiredType == byte.class) {
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected byte, but was " + value);
            }
            return Byte.valueOf((byte) value);
        }
        return value;
    }

    // parse a long, allowing conversion to other number types
    private Object parseLong(long value, ResolvedType declaredType) {
        var desiredType = declaredType.getRawType();
        if (desiredType == Long.class || desiredType == long.class || desiredType == Object.class) {
            return value;
        } else if (desiredType == Integer.class || desiredType == int.class) {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected int, but was " + value);
            }
            return Integer.valueOf((int) value);
        } else if (desiredType == Double.class || desiredType == double.class) {
            return Double.valueOf(value);
        } else if (desiredType == Float.class || desiredType == float.class) {
            return Float.valueOf(value);
        } else if (desiredType == Short.class || desiredType == short.class) {
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected short, but was " + value);
            }
            return Short.valueOf((short) value);
        } else if (desiredType == Byte.class || desiredType == byte.class) {
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid binary data: Expected byte, but was " + value);
            }
            return Byte.valueOf((byte) value);
        }
        return value;
    }

    //-------------------------------------------------------------------------
    private LocalDate parseDatePacked() throws IOException {
        var packed = input.readUnsignedShort();
        var dom = packed & 31;
        var ym = packed >> 5;
        return LocalDate.of((ym / 12) + 2000, (ym % 12) + 1, dom);
    }

    private LocalDate parseDate() throws IOException {
        var upper = input.readInt();
        var lower = input.readUnsignedByte();
        var year = upper >> 1;
        var month = ((upper & 1) << 3) + (lower >>> 5);
        var dom = lower & 31;
        return LocalDate.of(year, month, dom);
    }

    private LocalTime parseTime() throws IOException {
        var upper = input.readShort();
        var lower = Integer.toUnsignedLong(input.readInt());
        var nod = ((long) upper << 32) + lower;
        return LocalTime.ofNanoOfDay(nod);
    }

    private Instant parseInstant() throws IOException {
        var second = input.readLong();
        var nanos = input.readInt();
        return Instant.ofEpochSecond(second, nanos);
    }

    private Duration parseDuration() throws IOException {
        var seconds = input.readLong();
        var nanos = input.readInt();
        return Duration.ofSeconds(seconds, nanos);
    }

    //-----------------------------------------------------------------------
    // parses a byte array
    private byte[] parseByteArray(int size) throws IOException {
        var bytes = new byte[size];
        input.readFully(bytes);
        return bytes;
    }

    // parses a double array
    private double[] parseDoubleArray(int size) throws IOException {
        var doubles = new double[size];
        for (int i = 0; i < size; i++) {
            doubles[i] = input.readDouble();
        }
        return doubles;
    }

    //-------------------------------------------------------------------------
    private void skipObject() throws IOException {
        MsgPackInput.skipObject(input);  // TODO: need to capture definitions
    }

    private String acceptString() throws IOException {
        return acceptString(input.readByte());
    }

    private String acceptStringOrNull() throws IOException {
        var typeByte = input.readByte();
        return typeByte == NULL ? null : acceptString(typeByte);
    }

    private String acceptString(int typeByte) throws IOException {
        int size;
        if (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) {
            size = typeByte - MIN_FIX_STR;
        } else if (typeByte == STR_8) {
            size = input.readUnsignedByte();
        } else if (typeByte == STR_16) {
            size = input.readUnsignedShort();
        } else if (typeByte == STR_32) {
            size = input.readInt();
        } else {
            throw invalidBinaryData("string", typeByte);
        }
        return acceptStringBytes(size);
    }

    private String acceptStringBytes(int size) throws IOException {
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

    private int acceptInt() throws IOException {
        var typeByte = input.readByte();
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        return switch (typeByte) {
            case INT_8 -> input.readByte();
            case INT_16 -> input.readShort();
            case INT_32 -> input.readInt();
            default -> throw invalidBinaryData("int", typeByte);
        };
    }

    private int acceptMap() throws IOException {
        var typeByte = input.readByte();
        if (typeByte >= MIN_FIX_MAP && typeByte <= MAX_FIX_MAP) {
            return typeByte - MIN_FIX_MAP;
        }
        return switch (typeByte) {
            case MAP_8 -> input.readByte();
            case MAP_16 -> input.readShort();
            case MAP_32 -> input.readInt();
            default -> throw invalidBinaryData("map", typeByte);
        };
    }

    private int acceptArray() throws IOException {
        var typeByte = input.readByte();
        if (typeByte >= MIN_FIX_ARRAY && typeByte <= MAX_FIX_ARRAY) {
            return typeByte - MIN_FIX_ARRAY;
        }
        return switch (typeByte) {
            case ARRAY_8 -> input.readByte();
            case ARRAY_16 -> input.readShort();
            case ARRAY_32 -> input.readInt();
            default -> throw invalidBinaryData("array", typeByte);
        };
    }

    private IllegalArgumentException invalidBinaryData(String expected, int actualByte) {
        return new IllegalArgumentException(
                "Invalid binary data: Expected " + expected + ", but was: 0x" + toHex(actualByte));
    }

    //-------------------------------------------------------------------------
    private static interface BinHandler {
        public abstract Object handle(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception;
    }

    //-------------------------------------------------------------------------
    private static sealed class BaseBinHandlers {

        // an instance loaded dependent on the classpath
        private static final BaseBinHandlers INSTANCE = getInstance();

        private static final BaseBinHandlers getInstance() {
            try {
                ImmutableGrid.of();  // check if class is available
                return new CollectBinHandlers();
            } catch (Exception | LinkageError ex) {
                try {
                    ImmutableMultiset.of();  // check if class is available
                    return new GuavaBinHandlers();
                } catch (Exception | LinkageError ex2) {
                    return new BaseBinHandlers();
                }
            }
        }

        BinHandler createHandler(Class<?> type) {
            if (SortedSet.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseCollection(reader, declType, size, new TreeSet<>());
            }
            if (Set.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseCollection(reader, declType, size, new LinkedHashSet<>());
            }
            if (Collection.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseCollection(reader, declType, size, new ArrayList<>());
            }
            if (SortedMap.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMap(reader, declType, size, new TreeMap<>());
            }
            if (Map.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMap(reader, declType, size, new LinkedHashMap<>());
            }
            if (Optional.class.isAssignableFrom(type)) {
                return BaseBinHandlers::parseOptional;
            }
            if (type.isArray()) {
                return BaseBinHandlers::parseArray;
            }
            throw new IllegalArgumentException("Invalid binary data: Unknown collection: " + type.getName());
        }

        Class<?> classForTypeCode(int typeCode) {
            return switch (typeCode) {
                case TYPE_CODE_LIST -> List.class;
                case TYPE_CODE_SET -> Set.class;
                case TYPE_CODE_MAP -> Map.class;
                case TYPE_CODE_OPTIONAL -> Optional.class;
                default -> throw new IllegalArgumentException("Invalid binary data: Unknown type code: " + typeCode);
            };
        }

        // collection - parsed from a BeanPack array
        private static Collection<?> parseCollection(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size,
                Collection<Object> collection) throws Exception {

            var itemType = declaredType.getArgumentOrDefault(0);
            for (var i = 0; i < size; i++) {
                collection.add(reader.parseObject(itemType, false));
            }
            return collection;
        }

        // map - parsed from a BeanPack map
        static Map<?, ?> parseMap(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size,
                Map<Object, Object> map) throws Exception {

            var keyType = declaredType.getArgumentOrDefault(0);
            var valueType = declaredType.getArgumentOrDefault(1);
            for (var i = 0; i < size; i++) {
                var key = reader.parseObject(keyType, false);
                var value = reader.parseObject(valueType, false);
                map.put(key, value);
            }
            return map;
        }

        // optional - parsed from a BeanPack array, size 0 or 1
        private static Optional<?> parseOptional(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception {

            return switch (size) {
                case 0 -> Optional.empty();
                case 1 -> {
                    var itemType = declaredType.getArgumentOrDefault(0);
                    var value = reader.parseObject(itemType, false);
                    yield Optional.ofNullable(value);
                }
                default -> throw new IllegalArgumentException(
                        "Invalid binary data: Optional must be an array of size zero or one, but was " + size);
            };
        }

        // array - parsed from a BeanPack array
        private static Object parseArray(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception {

            var componentType = declaredType.toComponentType();
            var array = Array.newInstance(componentType.getRawType(), size);
            for (var i = 0; i < size; i++) {
                Array.set(array, i, reader.parseObject(componentType, false));
            }
            return array;
        }
    }

    //-------------------------------------------------------------------------
    private static sealed class GuavaBinHandlers extends BaseBinHandlers {

        @Override
        BinHandler createHandler(Class<?> type) {
            if (SortedMultiset.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMultiset(reader, declType, size, TreeMultiset.create());
            }
            if (Multiset.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMultiset(reader, declType, size, HashMultiset.create());
            }
            if (SetMultimap.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMultimap(reader, declType, size, HashMultimap.create());
            }
            if (Multimap.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMultimap(reader, declType, size, ArrayListMultimap.create());
            }
            if (Table.class.isAssignableFrom(type)) {
                return GuavaBinHandlers::parseTable;
            }
            if (BiMap.class.isAssignableFrom(type)) {
                return (reader, declType, size) -> parseMap(reader, declType, size, HashBiMap.create());
            }
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return GuavaBinHandlers::parseOptional;
            }
            // TODO: immutable collections
            return super.createHandler(type);
        }

        @Override
        Class<?> classForTypeCode(int typeCode) {
            return switch (typeCode) {
                case TYPE_CODE_MULTISET -> Multiset.class;
                case TYPE_CODE_LIST_MULTIMAP -> ListMultimap.class;
                case TYPE_CODE_SET_MULTIMAP -> SetMultimap.class;
                case TYPE_CODE_TABLE -> Table.class;
                case TYPE_CODE_BIMAP -> BiMap.class;
                case TYPE_CODE_GUAVA_OPTIONAL -> com.google.common.base.Optional.class;
                default -> super.classForTypeCode(typeCode);
            };
        }

        // multiset - parsed from a BeanPack map of value to count
        @SuppressWarnings({"rawtypes", "unchecked"})
        private static Multiset<?> parseMultiset(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size,
                Multiset mset) throws Exception {

            var itemType = declaredType.getArgumentOrDefault(0);
            for (var i = 0; i < size; i++) {
                var item = reader.parseObject(itemType, false);
                var count = reader.acceptInt();
                mset.add(item, count);
            }
            return mset;
        }

        // multimap - parsed from a BeanPack map of key to array of values
        private static Multimap<?, ?> parseMultimap(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size,
                Multimap<Object, Object> mmap) throws Exception {

            var keyType = declaredType.getArgumentOrDefault(0);
            var valueType = declaredType.getArgumentOrDefault(1);
            for (var i = 0; i < size; i++) {
                var key = reader.parseObject(keyType, false);
                var valueSize = reader.acceptArray();
                for (var j = 0; j < valueSize; j++) {
                    var value = reader.parseObject(valueType, false);
                    mmap.put(key, value);
                }
            }
            return mmap;
        }

        // table - parsed from a BeanPack map of row to map of column to value
        private static Table<?, ?, ?> parseTable(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception {

            var rowType = declaredType.getArgumentOrDefault(0);
            var columnType = declaredType.getArgumentOrDefault(1);
            var valueType = declaredType.getArgumentOrDefault(2);
            var table = HashBasedTable.create();
            for (var i = 0; i < size; i++) {
                var row = reader.parseObject(rowType, false);
                var colSize = reader.acceptMap();
                for (var j = 0; j < colSize; j++) {
                    var column = reader.parseObject(columnType, false);
                    var value = reader.parseObject(valueType, false);
                    table.put(row, column, value);
                }
            }
            return table;
        }

        // optional - parsed from a BeanPack array, size 0 or 1
        private static com.google.common.base.Optional<?> parseOptional(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception {

            return switch (size) {
                case 0 -> com.google.common.base.Optional.absent();
                case 1 -> {
                    var itemType = declaredType.getArgumentOrDefault(0);
                    var value = reader.parseObject(itemType, false);
                    yield com.google.common.base.Optional.fromNullable(value);
                }
                default -> throw new IllegalArgumentException(
                        "Invalid binary data: Optional must be an array of size zero or one, but was " + size);
            };
        }
    }

    //-------------------------------------------------------------------------
    private static final class CollectBinHandlers extends GuavaBinHandlers {

        @Override
        BinHandler createHandler(Class<?> type) {
            if (Grid.class.isAssignableFrom(type)) {
                return CollectBinHandlers::parseGrid;
            }
            return super.createHandler(type);
        }

        @Override
        Class<?> classForTypeCode(int typeCode) {
            return switch (typeCode) {
                case TYPE_CODE_GRID -> Grid.class;
                default -> super.classForTypeCode(typeCode);
            };
        }

        // grid
        private static Grid<?> parseGrid(
                JodaBeanPackedBinReader reader,
                ResolvedType declaredType,
                int size) throws Exception {

            var valueType = declaredType.getArgumentOrDefault(0);
            if (size != 3) {
                throw new IllegalArgumentException(
                        "Invalid binary data: Grid must be an array of size three, but was " + size);
            }
            var rowCount = reader.acceptInt();
            var colCount = reader.acceptInt();
            var arraySize = reader.acceptArray();
            if (arraySize == rowCount * colCount) {
                // dense
                var grid = DenseGrid.create(rowCount, colCount);
                for (var row = 0; row < rowCount; row++) {
                    for (var col = 0; col < colCount; col++) {
                        var value = reader.parseObject(valueType, false);
                        grid.put(row, col, value);
                    }
                }
                return grid;
            } else {
                // sparse
                var grid = SparseGrid.create(rowCount, colCount);
                for (var i = 0; i < size; i++) {
                    var row = reader.acceptInt();
                    var col = reader.acceptInt();
                    var value = reader.parseObject(valueType, false);
                    grid.put(row, col, value);
                }
                return grid;
            }
        }
    }
}
