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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerDeserializer;
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
     * @param input  the input string, not null
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
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    private <T> T parseRoot(Map<String, Object> input, Class<T> declaredType) throws Exception {
        Object parsed = parseBean(input, declaredType);
        return declaredType.cast(parsed);
    }

    // parse a bean, event after object start passed in
    private Object parseBean(Map<String, Object> input, Class<?> beanType) throws Exception {
        String propName = "";
        try {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(beanType);
            MetaBean metaBean = deser.findMetaBean(beanType);
            BeanBuilder<?> builder = deser.createBuilder(beanType, metaBean);
            for (Entry<String, Object> entry : input.entrySet()) {
                // property name
                propName = entry.getKey();
                MetaProperty<?> metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                // ignore unknown properties
                if (metaProp != null && !metaProp.style().isDerived()) {
                    Object value = parseObject(
                            entry.getValue(), SerOptional.extractType(metaProp, beanType), metaProp, beanType, null);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                }
                propName = "";
            }
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse object, event passed in
    @SuppressWarnings("unchecked")
    private Object parseObject(
            Object input,
            Class<?> declaredType,
            MetaProperty<?> metaProp,
            Class<?> beanType,
            SerIterable parentIterable) throws Exception {

        // parse based on type
        if (Bean.class.isAssignableFrom(declaredType)) {
            if (input instanceof Map) {
                return parseBean((Map<String, Object>) input, declaredType);
            } else {
                return parseSimple(input, declaredType);
            }
        } else {
            if (input instanceof List || input instanceof Map) {
                SerIterable childIterable = null;
                if (metaProp != null) {
                    childIterable = settings.getIteratorFactory().createIterable(metaProp, beanType, true);
                } else if (parentIterable != null) {
                    childIterable = settings.getIteratorFactory().createIterable(parentIterable);
                }
                if (childIterable == null) {
                    if (input instanceof List) {
                        if (declaredType.isArray()) {
                            childIterable = SerIteratorFactory.array(declaredType.getComponentType());
                        } else {
                            childIterable = SerIteratorFactory.list(Object.class, Collections.<Class<?>>emptyList());
                        }
                    } else {
                        childIterable = SerIteratorFactory.map(
                                String.class, Object.class, Collections.<Class<?>>emptyList());
                    }
                }
                return parseIterable(input, childIterable);
            } else {
                return parseSimple(input, declaredType);
            }
        }
    }

    //-----------------------------------------------------------------------
    private Object parseIterable(Object input, SerIterable iterable) throws Exception {
        if (iterable.category() == SerCategory.MAP) {
            return parseIterableMap(input, iterable);
        } else if (iterable.category() == SerCategory.COUNTED) {
            return parseIterableCounted(input, iterable);
        } else if (iterable.category() == SerCategory.TABLE) {
            return parseIterableTable(input, iterable);
        } else if (iterable.category() == SerCategory.GRID) {
            return parseIterableGrid(input, iterable);
        } else {
            return parseIterableArray(input, iterable);
        }
    }

    @SuppressWarnings("unchecked")
    private Object parseIterableMap(Object input, SerIterable iterable) throws Exception {
        if (input instanceof Map) {
            for (Entry<String, Object> entry : ((Map<String, Object>) input).entrySet()) {
                String keyStr = entry.getKey();
                Object key = convertText(keyStr, iterable.keyType());
                Object value = parseObject(entry.getValue(), iterable.valueType(), null, null, iterable);
                iterable.add(key, null, value, 1);
            }
            return iterable.build();
        } else if (input instanceof List) {
            List<?> inputList = (List<?>) input;
            for (Object inputVal : inputList) {
                List<?> inputData = (List<?>) inputVal;
                if (inputData.size() != 3) {
                    throw new IllegalArgumentException("Expected table iterable to have entries of size 3");
                }
                Object key = parseObject(inputData.get(0), iterable.keyType(), null, null, null);
                Object value = parseObject(inputData.get(2), iterable.valueType(), null, null, iterable);
                iterable.add(key, null, value, 1);
            }
            return iterable.build();
        } else {
            throw new IllegalArgumentException("Invalid data: Expected List or Map but found " + input);
        }
    }

    private Object parseIterableTable(Object input, SerIterable iterable) throws Exception {
        List<?> inputList = (List<?>) input;
        for (Object inputVal : inputList) {
            List<?> inputData = (List<?>) inputVal;
            if (inputData.size() != 3) {
                throw new IllegalArgumentException("Expected table iterable to have entries of size 3");
            }
            Object key = parseObject(inputData.get(0), iterable.keyType(), null, null, null);
            Object col = parseObject(inputData.get(1), iterable.columnType(), null, null, null);
            Object value = parseObject(inputData.get(2), iterable.valueType(), null, null, iterable);
            iterable.add(key, col, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableGrid(Object input, SerIterable iterable) throws Exception {
        List<?> inputList = (List<?>) input;
        if (inputList.size() < 2) {
            throw new IllegalArgumentException("Expected counted iterable to be size 3");
        }
        Integer rows = (Integer) inputList.get(0);
        Integer columns = (Integer) inputList.get(1);
        iterable.dimensions(new int[] {rows, columns});
        for (Object inputVal : inputList.subList(2, inputList.size())) {
            List<?> inputData = (List<?>) inputVal;
            if (inputData.size() != 2) {
                throw new IllegalArgumentException("Expected grid iterable to have entries of size 3");
            }
            Integer row = (Integer) inputData.get(0);
            Integer col = (Integer) inputData.get(1);
            Object value = parseObject(inputData.get(2), iterable.valueType(), null, null, iterable);
            iterable.add(row, col, value, 1);
        }
        return iterable.build();
    }

    private Object parseIterableCounted(Object input, SerIterable iterable) throws Exception {
        List<?> inputList = (List<?>) input;
        for (Object inputVal : inputList) {
            List<?> inputData = (List<?>) inputVal;
            if (inputData.size() != 2) {
                throw new IllegalArgumentException("Expected counted iterable to have entries of size 2");
            }
            Object value = parseObject(inputData.get(0), iterable.valueType(), null, null, iterable);
            Integer count = (Integer) inputData.get(1);
            iterable.add(null, null, value, count);
        }
        return iterable.build();
    }

    private Object parseIterableArray(Object input, SerIterable iterable) throws Exception {
        List<?> inputList = (List<?>) input;
        for (Object inputVal : inputList) {
            Object value = parseObject(inputVal, iterable.valueType(), null, null, iterable);
            iterable.add(null, null, value, 1);
        }
        return iterable.build();
    }

    private Object parseSimple(Object input, Class<?> type) throws Exception {
        if (input == null) {
            if (type == double.class || type == Double.class) {
                return Double.NaN;  // leniently accept null for NaN
            } else if (type == float.class || type == Float.class) {
                return Float.NaN;  // leniently accept null for NaN
            } else {
                return null;
            }
        }
        if (input instanceof String) {
            return convertText(input, type);
        }
        if (input instanceof Boolean) {
            return input;
        }
        if (input instanceof Long) {
            Long inputLong = (Long) input;
            if (type == Long.class || type == long.class) {
                return inputLong;
            } else {
                return convertInteger(inputLong.longValue(), type);
            }
        }
        if (input instanceof Integer) {
            Integer inputInteger = (Integer) input;
            if (type == Integer.class || type == int.class) {
                return inputInteger;
            } else {
                return convertInteger(inputInteger.longValue(), type);
            }
        }
        if (input instanceof Double) {
            Double inputDouble = (Double) input;
            if (type == Double.class || type == double.class) {
                return inputDouble;
            } else if (type == Float.class || type == float.class) {
                return inputDouble.floatValue();
            } else {
                return convertInteger(inputDouble.longValue(), type);
            }
        }
        if (input instanceof Float) {
            Float inputFloat = (Float) input;
            if (type == Float.class || type == float.class) {
                return inputFloat;
            } else if (type == Double.class || type == double.class) {
                return inputFloat.doubleValue();
            } else {
                return convertInteger(inputFloat.longValue(), type);
            }
        }
        if (input instanceof Short || input instanceof Byte) {
            return input;
        }
        throw new IllegalArgumentException("Invalid data: Expected simple type but found " + input);
    }

    private Object convertText(Object input, Class<?> type) {
        if (type == Object.class || type.isAssignableFrom(String.class)) {
            return input;
        }
        return settings.getConverter().convertFromString(type, (String) input);
    }

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
            double dblVal = (double) value;
            if (value != (long) dblVal) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of double: " + value);
            }
            return Double.valueOf(dblVal);
            
        } else if (type == Float.class || type == float.class) {
            float fltVal = (float) value;
            if (value != (long) fltVal) {
                throw new IllegalArgumentException("Invalid data: Value exceeds capacity of float: " + value);
            }
            return Float.valueOf(fltVal);
            
        } else {
            if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Invalid data: Expected int, but was " + value);
            }
            return Integer.valueOf((int) value);
        }
    }

}
