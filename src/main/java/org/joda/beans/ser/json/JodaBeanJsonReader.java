/*
 *  Copyright 2001-2014 Stephen Colebourne
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
package org.joda.beans.ser.json;

import static org.joda.beans.ser.json.JodaBeanJsonWriter.BEAN;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.META;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.TYPE;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.VALUE;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

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
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from JSON.
 * <p>
 * The JSON format is defined by {@link JodaBeanJsonWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 */
public class JodaBeanJsonReader {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * The reader.
     */
    private JsonInput input;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private Map<String, Class<?>> knownTypes = new HashMap<String, Class<?>>();

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanJsonReader(final JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input string, not null
     * @return the bean, not null
     */
    public Bean read(String input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input string, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(String input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        return read(new StringReader(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(Reader input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input reader, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(Reader input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        JodaBeanUtils.notNull(rootType, "rootType");
        try {
            this.input = new JsonInput(input);
            return parseRoot(rootType);
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
    private <T> T parseRoot(Class<T> declaredType) throws Exception {
        Object parsed = parseObject(input.acceptEvent(JsonEvent.OBJECT), declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    // parse a bean, event after object start passed in
    private Object parseBean(JsonEvent event, Class<?> beanType) throws Exception {
        String propName = "";
        try {
            SerDeserializer deser = settings.getDeserializers().findDeserializer(beanType);
            MetaBean metaBean = deser.findMetaBean(beanType);
            BeanBuilder<?> builder = deser.createBuilder(beanType, metaBean);
            while (event != JsonEvent.OBJECT_END) {
                // property name
                propName = input.acceptObjectKey(event);
                MetaProperty<?> metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                if (metaProp == null) {
                    input.skipData();
                } else {
                    Object value = parseObject(input.readEvent(),
                            SerOptional.extractType(metaProp, beanType), metaProp, beanType, null, false);
                    deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                }
                propName = "";
                event = input.acceptObjectSeparator();
            }
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    "Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    // parse object, event passed in
    private Object parseObject(
            JsonEvent event,
            Class<?> declaredType, MetaProperty<?> metaProp, Class<?> beanType,
            SerIterable parentIterable, boolean rootType) throws Exception {
        // establish type
        if (event == JsonEvent.OBJECT) {
            event = input.readEvent();
            if (event == JsonEvent.STRING) {
                String key = input.parseObjectKey();
                if (key.equals(BEAN)) {
                    return parseTypedBean(declaredType, rootType);
                } else if (key.equals(TYPE)) {
                    return parseTypedSimple(declaredType);
                } else if (key.equals(META)) {
                    return parseTypedMeta();
                } else {
                    input.pushBack('"');
                    input.pushBackObjectKey(key);
                    event = JsonEvent.OBJECT;
                }
            } else if (event == JsonEvent.OBJECT_END) {
                input.pushBack('}');
                event = JsonEvent.OBJECT;
            } else {
                throw new IllegalArgumentException("Invalid JSON data: Expected JSON object end but found " + event);
            }
        }
        // parse based on type
        if (Bean.class.isAssignableFrom(declaredType)) {
            if (event == JsonEvent.OBJECT) {
                return parseBean(input.readEvent(), declaredType);
            } else {
                return parseSimple(event, declaredType);
            }
        } else {
            if (event == JsonEvent.OBJECT || event == JsonEvent.ARRAY) {
                SerIterable childIterable = null;
                if (metaProp != null) {
                    childIterable = SerIteratorFactory.INSTANCE.createIterable(metaProp, beanType);
                } else if (parentIterable != null) {
                    childIterable = SerIteratorFactory.INSTANCE.createIterable(parentIterable);
                }
                if (childIterable == null) {
                    throw new IllegalArgumentException("Invalid JSON data: Invalid object or array");
                }
                return parseIterable(event, childIterable);
            } else {
                return parseSimple(event, declaredType);
            }
        }
    }

    private Object parseTypedBean(Class<?> declaredType, boolean rootType) throws Exception {
        String typeStr = input.acceptString();
        Class<?> effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
        if (rootType) {
            if (Bean.class.isAssignableFrom(effectiveType) == false) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + effectiveType.getName());
            }
            basePackage = effectiveType.getPackage().getName() + ".";
        }
        if (declaredType.isAssignableFrom(effectiveType) == false) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " +
                declaredType.getName() + " and " + effectiveType.getName());
        }
        JsonEvent event = input.readEvent();
        if (event == JsonEvent.COMMA) {
            event = input.readEvent();
        }
        return parseBean(event, effectiveType);
    }

    private Object parseTypedSimple(Class<?> declaredType) throws Exception {
        String typeStr = input.acceptString();
        Class<?> effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
        if (declaredType.isAssignableFrom(effectiveType) == false) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " +
                declaredType.getName() + " and " + effectiveType.getName());
        }
        input.acceptEvent(JsonEvent.COMMA);
        String valueKey = input.acceptObjectKey(input.readEvent());
        if (valueKey.equals(VALUE) == false) {
            throw new IllegalArgumentException("Invalid JSON data: Expected 'value' key but found " + valueKey);
        }
        Object result = parseSimple(input.readEvent(), effectiveType);
        input.acceptEvent(JsonEvent.OBJECT_END);
        return result;
    }

    private Object parseTypedMeta() throws Exception {
        String metaType = input.acceptString();
        SerIterable childIterable = SerIteratorFactory.INSTANCE.createIterable(metaType, settings, knownTypes);
        input.acceptEvent(JsonEvent.COMMA);
        String valueKey = input.acceptObjectKey(input.readEvent());
        if (valueKey.equals(VALUE) == false) {
            throw new IllegalArgumentException("Invalid JSON data: Expected 'value' key but found " + valueKey);
        }
        Object result = parseIterable(input.readEvent(), childIterable);
        input.acceptEvent(JsonEvent.OBJECT_END);
        return result;
    }

    private Object parseIterable(JsonEvent event, SerIterable iterable) throws Exception {
        if (iterable.category() == SerCategory.MAP) {
            return parseIterableMap(event, iterable);
        } else if (iterable.category() == SerCategory.COUNTED) {
            return parseIterableCounted(event, iterable);
        } else if (iterable.category() == SerCategory.TABLE) {
            return parseIterableTable(event, iterable);
        } else if (iterable.category() == SerCategory.GRID) {
            return parseIterableGrid(event, iterable);
        } else {
            return parseIterableArray(event, iterable);
        }
    }

    private Object parseIterableMap(JsonEvent event, SerIterable iterable) throws Exception {
        if (event == JsonEvent.OBJECT) {
            event = input.readEvent();
            while (event != JsonEvent.OBJECT_END) {
                String keyStr = input.acceptObjectKey(event);
                Object key = settings.getConverter().convertFromString(iterable.keyType(), keyStr);
                Object value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
                iterable.add(key, null, value, 1);
                event = input.acceptObjectSeparator();
            }
        } else if (event == JsonEvent.ARRAY) {
            event = input.readEvent();
            while (event != JsonEvent.ARRAY_END) {
                input.ensureEvent(event, JsonEvent.ARRAY);
                Object key = parseObject(input.readEvent(), iterable.keyType(), null, null, null, false);
                input.acceptEvent(JsonEvent.COMMA);
                Object value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
                input.acceptEvent(JsonEvent.ARRAY_END);
                iterable.add(key, null, value, 1);
                event = input.acceptArraySeparator();
            }
            return iterable.build();
        } else {
            throw new IllegalArgumentException("Invalid JSON data: Expected array or object but found " + event);
        }
        return iterable.build();
    }

    private Object parseIterableTable(JsonEvent event, SerIterable iterable) throws Exception {
        input.ensureEvent(event, JsonEvent.ARRAY);
        event = input.readEvent();
        while (event != JsonEvent.ARRAY_END) {
            input.ensureEvent(event, JsonEvent.ARRAY);
            Object key = parseObject(input.readEvent(), iterable.keyType(), null, null, null, false);
            input.acceptEvent(JsonEvent.COMMA);
            Object column = parseObject(input.readEvent(), iterable.columnType(), null, null, null, false);
            input.acceptEvent(JsonEvent.COMMA);
            Object value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
            iterable.add(key, column, value, 1);
            input.acceptEvent(JsonEvent.ARRAY_END);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseIterableGrid(JsonEvent event, SerIterable iterable) throws Exception {
        input.ensureEvent(event, JsonEvent.ARRAY);
        input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
        int rows = (int) input.parseNumberIntegral();
        input.acceptEvent(JsonEvent.COMMA);
        input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
        int columns = (int) input.parseNumberIntegral();
        iterable.dimensions(new int[] {rows, columns});
        event = input.acceptArraySeparator();
        while (event != JsonEvent.ARRAY_END) {
            input.ensureEvent(event, JsonEvent.ARRAY);
            input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
            int row = (int) input.parseNumberIntegral();
            input.acceptEvent(JsonEvent.COMMA);
            input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
            int column = (int) input.parseNumberIntegral();
            input.acceptEvent(JsonEvent.COMMA);
            Object value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
            input.acceptEvent(JsonEvent.ARRAY_END);
            iterable.add(row, column, value, 1);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseIterableCounted(JsonEvent event, SerIterable iterable) throws Exception {
        input.ensureEvent(event, JsonEvent.ARRAY);
        event = input.readEvent();
        while (event != JsonEvent.ARRAY_END) {
            input.ensureEvent(event, JsonEvent.ARRAY);
            Object value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
            input.acceptEvent(JsonEvent.COMMA);
            input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
            iterable.add(null, null, value, (int) input.parseNumberIntegral());
            input.acceptEvent(JsonEvent.ARRAY_END);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseIterableArray(JsonEvent event, SerIterable iterable) throws Exception {
        input.ensureEvent(event, JsonEvent.ARRAY);
        event = input.readEvent();
        while (event != JsonEvent.ARRAY_END) {
            Object value = parseObject(event, iterable.valueType(), null, null, iterable, false);
            iterable.add(null, null, value, 1);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseSimple(JsonEvent event, Class<?> type) throws Exception {
        switch (event) {
            case STRING: {
                String text = input.parseString();
                if (type == String.class || type == Object.class) {
                    return text;
                }
                return settings.getConverter().convertFromString(type, text);
            }
            case NUMBER_INTEGRAL: {
                long value = input.parseNumberIntegral();
                if (type == Long.class || type == long.class) {
                    return Long.valueOf(value);
                    
                } else if (type == Short.class || type == short.class) {
                    if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) {
                        throw new IllegalArgumentException("Invalid JSON data: Expected short, but was " + value);
                    }
                    return Short.valueOf((short) value);
                    
                } else if (type == Byte.class || type == byte.class) {
                    if (value < Byte.MIN_VALUE || value > Byte.MAX_VALUE) {
                        throw new IllegalArgumentException("Invalid JSON data: Expected byte, but was " + value);
                    }
                    return Byte.valueOf((byte) value);
                    
                } else {
                    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("Invalid JSON data: Expected int, but was " + value);
                    }
                    return Integer.valueOf((int) value);
                }
            }
            case NUMBER_FLOATING: {
                double value = input.parseNumberFloating();
                if (type == Float.class || type == float.class) {
                    return Float.valueOf((float) value);
                } else {
                    return Double.valueOf(value);
                }
            }
            case NULL: {
                if (type == double.class) {
                    return Double.NaN;  // leniently accept null for NaN
                } else if (type == float.class) {
                    return Float.NaN;  // leniently accept null for NaN
                } else {
                    return null;
                }
            }
            case TRUE:
                return Boolean.TRUE;
            case FALSE:
                return Boolean.FALSE;
            default:
                throw new IllegalArgumentException("Invalid JSON data: Expected simple type but found " + event);
        }
    }

}
