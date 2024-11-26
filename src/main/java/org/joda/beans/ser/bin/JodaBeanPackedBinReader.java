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
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Reads the Joda-Bean BeanPack binary format with strings deduplicated by reference.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
final class JodaBeanPackedBinReader extends BeanPack {

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
//            case DATE_PACKED -> datePacked();
//            case DATE -> date();
//            case TIME -> time();
//            case INSTANT -> instant();
//            case DURATION -> duration();
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
            return parseMapAsCollection(mapSize, declaredType);
        }
    }

    // parse a BeanPack array
    private Object parseArray(int arraySize, ResolvedType declaredType) throws Exception {
        if (Bean.class.isAssignableFrom(declaredType.getRawType())) {
            return parseArrayAsBean(arraySize, declaredType.getRawType());
        } else {
            return parseArrayAsList(arraySize, declaredType);
        }
    }

    // parse a BeanPack string
    private Object parseString(int strLen, ResolvedType declaredType) throws Exception {
        var str = acceptStringBytes(strLen);
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
        if (basePackage == null) {  // TODO: move base package to root
            basePackage = decodedType.getPackage().getName() + ".";
        }
        var effectiveType = ResolvedType.from(decodedType);
        validateEffectiveType(declaredType, effectiveType);
        typeDefinitions.add(decodedType);
        return parseObject(effectiveType, isBeanDefinition);
    }

    // parse the int reference, negative for type codes and positive for user types
    private Object parseTypeRef(ResolvedType declaredType, boolean isBeanDefinition) throws Exception {
        var typeRef = acceptInt();
        if (typeRef < 0) {
            // TODO: handle properly
            var effectiveType = switch (typeRef) {
                case TYPE_CODE_LIST -> ResolvedType.of(List.class);
                case TYPE_CODE_SET -> ResolvedType.of(Set.class);
                case TYPE_CODE_MAP -> ResolvedType.of(Map.class);
                case TYPE_CODE_OPTIONAL -> ResolvedType.of(Optional.class);
                default -> throw new IllegalArgumentException("Invalid binary data: Unexpected type code: " + typeRef);
            };
            return parseObject(effectiveType, isBeanDefinition);
        } else {
            var effectiveType = ResolvedType.of(typeDefinitions.get(typeRef));
            validateEffectiveType(declaredType, effectiveType);
            return parseObject(effectiveType, isBeanDefinition);
        }
    }

    private void validateEffectiveType(ResolvedType declaredType, ResolvedType effectiveType) {
        if (!declaredType.getRawType().isAssignableFrom(effectiveType.getRawType())) {
            throw new IllegalArgumentException("Invalid binary data: Specified type is incompatible with declared type: " +
                    declaredType + " and " + effectiveType);
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
                    MsgPackInput.skipObject(input);  // TODO: need to capture definitions
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
                    MsgPackInput.skipObject(input);  // TODO: need to capture definitions
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
                    MsgPackInput.skipObject(input);  // TODO: need to capture definitions
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
    private Object parseMapAsCollection(int mapSize, ResolvedType effectiveType) throws Exception {
        var keyType = effectiveType.getArgumentOrDefault(0);
        var valueType = effectiveType.getArgumentOrDefault(1);
        var map = new LinkedHashMap<>();
        for (var i = 0; i < mapSize; i++) {
            var key = parseObject(keyType, false);
            var value = parseObject(valueType, false);
            map.put(key, value);
        }
        return map;
    }

    // parse a BeanPack array as a List
    private Object parseArrayAsList(int arraySize, ResolvedType effectiveType) throws Exception {
        var itemType = effectiveType.getArgumentOrDefault(0);
        var list = new ArrayList<>();
        for (var i = 0; i < arraySize; i++) {
            var item = parseObject(itemType, false);
            list.add(item);
        }
        return list;
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
    private String acceptString() throws IOException {
        var typeByte = input.readByte();
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

    private IllegalArgumentException invalidBinaryData(String expected, int actualByte) {
        return new IllegalArgumentException(
                "Invalid binary data: Expected " + expected + ", but was: 0x" + toHex(actualByte));
    }

}
