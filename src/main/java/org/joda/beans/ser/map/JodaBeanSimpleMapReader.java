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
package org.joda.beans.ser.map;

import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerIteratorFactory;
import org.joda.beans.ser.SerOptional;

/**
 * Provides the ability for a Joda-Bean to read from a JSON-like in memory {@code Map}.
 * <p>
 * The format is defined by {@link JodaBeanSimpleMapWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
public class JodaBeanSimpleMapReader {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanSimpleMapReader(JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the map input, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(Map<String, Object> input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        JodaBeanUtils.notNull(rootType, "rootType");
        try {
            return parseRoot(input, rootType);
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
     * @param input  the map input, not null
     * @param declaredType  the declared type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    private <T> T parseRoot(Map<String, Object> input, Class<T> declaredType) {
        var parsed = parseBean(input, declaredType);
        return declaredType.cast(parsed);
    }

    // parse a bean, event after object start passed in
    private Object parseBean(Map<String, Object> input, Class<?> beanType) {
        var propName = "";
        try {
            var deser = settings.getDeserializers().findDeserializer(beanType);
            var metaBean = deser.findMetaBean(beanType);
            var builder = deser.createBuilder(beanType, metaBean);
            for (var entry : input.entrySet()) {
                // property name
                propName = entry.getKey();
                var metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                // ignore unknown properties
                if (metaProp != null && !metaProp.style().isDerived()) {
                    var declaredType = SerOptional.extractType(metaProp, beanType);
                    var value = parseProperty(entry.getValue(), declaredType, metaProp, beanType);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                }
            }
            propName = "";
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse an object defined by a meta-property, event passed in
    private Object parseProperty(
            Object input,
            Class<?> declaredType,
            MetaProperty<?> metaProp,
            Class<?> beanType) {

        // parse based on type
        if (Bean.class.isAssignableFrom(declaredType)) {
            return parseObjectAsBean(input, declaredType);
        } else if (input instanceof List || input instanceof Map) {
            var childIterable = settings.getIteratorFactory().createIterable(metaProp, beanType, true);
            return childIterable != null ?
                    parseIterable(input, childIterable) :
                    parseObjectAsCollection(input, declaredType);
        } else {
            return parseSimple(input, declaredType);
        }
    }

    // parse an object within a collection
    private Object parseObject(
            Object input,
            Class<?> declaredType,
            SerIterable parentIterableOrNull) {

        if (Bean.class.isAssignableFrom(declaredType)) {
            return parseObjectAsBean(input, declaredType);
        } else if (input instanceof List || input instanceof Map) {
            var childIterable = parentIterableOrNull != null ?
                    settings.getIteratorFactory().createIterable(parentIterableOrNull) :
                    null;
            return childIterable != null ?
                    parseIterable(input, childIterable) :
                    parseObjectAsCollection(input, declaredType);
        } else {
            return parseSimple(input, declaredType);
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseObjectAsBean(Object input, Class<?> declaredType) {
        if (input instanceof Map) {
            return parseBean((Map<String, Object>) input, declaredType);
        } else {
            return parseSimple(input, declaredType);
        }
    }

    private Object parseObjectAsCollection(Object input, Class<?> declaredType) {
        if (input instanceof List<?> inputList) {
            if (declaredType.isArray()) {
                return parseIterableArray(inputList, SerIteratorFactory.array(declaredType.getComponentType()));
            } else {
                return parseIterableArray(inputList, SerIteratorFactory.list(Object.class, List.of()));
            }
        } else {
            return parseIterableMap(input, SerIteratorFactory.map(String.class, Object.class, List.of()));
        }
    }

    //-----------------------------------------------------------------------
    private Object parseIterable(Object input, SerIterable iterable) {
        return switch (iterable.category()) {
            case COLLECTION -> parseIterableArray((List<?>) input, iterable);
            case COUNTED -> parseIterableCounted((List<?>) input, iterable);
            case MAP -> parseIterableMap(input, iterable);
            case TABLE -> parseIterableTable((List<?>) input, iterable);
            case GRID -> parseIterableGrid((List<?>) input, iterable);
        };
    }

    @SuppressWarnings("unchecked")
    private Object parseIterableMap(Object input, SerIterable iterable) {
        if (input instanceof Map) {
            for (var entry : ((Map<String, Object>) input).entrySet()) {
                var keyStr = entry.getKey();
                var key = convertText(keyStr, iterable.keyType());
                var value = parseObject(entry.getValue(), iterable.valueType(), iterable);
                iterable.add(key, null, value, 1);
            }
            return iterable.build();
        } else if (input instanceof List<?> inputList) {
            for (var inputVal : inputList) {
                var inputData = (List<?>) inputVal;
                if (inputData.size() != 3) {
                    throw new IllegalArgumentException("Expected table iterable to have entries of size 3");
                }
                var key = parseObject(inputData.get(0), iterable.keyType(), null);
                var value = parseObject(inputData.get(2), iterable.valueType(), iterable);
                iterable.add(key, null, value, 1);
            }
            return iterable.build();
        } else {
            throw new IllegalArgumentException("Invalid data: Expected List or Map but found " + input);
        }
    }

    private Object parseIterableTable(List<?> input, SerIterable iterable) {
        for (var inputVal : input) {
            List<?> inputData = (List<?>) inputVal;
            if (inputData.size() != 3) {
                throw new IllegalArgumentException("Expected table iterable to have entries of size 3");
            }
            var key = parseObject(inputData.get(0), iterable.keyType(), null);
            var col = parseObject(inputData.get(1), iterable.columnType(), null);
            var value = parseObject(inputData.get(2), iterable.valueType(), iterable);
            iterable.add(key, col, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableGrid(List<?> input, SerIterable iterable) {
        if (input.size() < 2) {
            throw new IllegalArgumentException("Expected grid iterable to be at least size 2");
        }
        var rows = (Integer) input.get(0);
        var columns = (Integer) input.get(1);
        iterable.dimensions(new int[] {rows, columns});
        for (var inputVal : input.subList(2, input.size())) {
            var inputData = (List<?>) inputVal;
            if (inputData.size() != 3) {
                throw new IllegalArgumentException("Expected grid iterable to have entries of size 3");
            }
            var row = (Integer) inputData.get(0);
            var col = (Integer) inputData.get(1);
            var value = parseObject(inputData.get(2), iterable.valueType(), iterable);
            iterable.add(row, col, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableCounted(List<?> input, SerIterable iterable) {
        for (var inputVal : input) {
            var inputData = (List<?>) inputVal;
            if (inputData.size() != 2) {
                throw new IllegalArgumentException("Expected counted iterable to have entries of size 2");
            }
            var value = parseObject(inputData.get(0), iterable.valueType(), iterable);
            var count = (Integer) inputData.get(1);
            iterable.add(null, null, value, count);
        }
        return iterable.build();
    }

    private Object parseIterableArray(List<?> input, SerIterable iterable) {
        for (var inputVal : input) {
            var value = parseObject(inputVal, iterable.valueType(), iterable);
            iterable.add(null, null, value, 1);
        }
        return iterable.build();
    }

    //-------------------------------------------------------------------------
    private Object parseSimple(Object input, Class<?> type) {
        return switch (input) {
            case null -> convertNull(type);
            case String s -> convertText(s, type);
            case Boolean b -> input;
            case Long l -> convertLong(l, type);
            case Integer i -> convertInteger(i, type);
            case Short s -> input;
            case Byte b -> input;
            case Double d -> convertDouble(d, type);
            case Float f -> convertFloat(f, type);
            default -> throw new IllegalArgumentException("Invalid data: Expected simple type but found " + input);
        };
    }

    private Object convertNull(Class<?> type) {
        if (type == double.class || type == Double.class) {
            return Double.NaN;  // leniently accept null for NaN
        } else if (type == float.class || type == Float.class) {
            return Float.NaN;  // leniently accept null for NaN
        } else {
            return null;
        }
    }

    private Object convertText(String input, Class<?> type) {
        if (type == Object.class || type.isAssignableFrom(String.class)) {
            return input;
        }
        return settings.getConverter().convertFromString(type, input);
    }

    private Object convertLong(Long inputLong, Class<?> type) {
        if (type == Long.class || type == long.class) {
            return inputLong;
        } else {
            return convertInteger(inputLong.longValue(), type);
        }
    }

    private Object convertInteger(Integer inputInteger, Class<?> type) {
        if (type == Integer.class || type == int.class) {
            return inputInteger;
        } else {
            return convertInteger(inputInteger.longValue(), type);
        }
    }

    private Object convertDouble(Double inputDouble, Class<?> type) {
        if (type == Double.class || type == double.class) {
            return inputDouble;
        } else if (type == Float.class || type == float.class) {
            return inputDouble.floatValue();
        } else {
            return convertInteger(inputDouble.longValue(), type);
        }
    }

    private Object convertFloat(Float inputFloat, Class<?> type) {
        if (type == Float.class || type == float.class) {
            return inputFloat;
        } else if (type == Double.class || type == double.class) {
            return inputFloat.doubleValue();
        } else {
            return convertInteger(inputFloat.longValue(), type);
        }
    }

    // converts the value we received to the type we desire
    private Object convertInteger(long value, Class<?> type) {
        if (type == Long.class || type == long.class) {
            return Long.valueOf(value);
            
        } else if (type == Short.class || type == short.class) {
            if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of short: " + value);
            }
            return Short.valueOf((short) value);
            
        } else if (type == Byte.class || type == byte.class) {
            if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of byte: " + value);
            }
            return Byte.valueOf((byte) value);
            
        } else if (type == Double.class || type == double.class) {
            double dblVal = value;
            if (value != (long) dblVal) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of double: " + value);
            }
            return Double.valueOf(dblVal);
            
        } else if (type == Float.class || type == float.class) {
            float fltVal = value;
            if (value != (long) fltVal) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of float: " + value);
            }
            return Float.valueOf(fltVal);

        } else if (type == Integer.class || type == int.class) {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid data: Expected int, but was " + value);
            }
            return Integer.valueOf((int) value);
        } else {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                return Long.valueOf(value);
            }
            return Integer.valueOf((int) value);
        }
    }

}
