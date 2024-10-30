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
package org.joda.beans.ser.json;

import static org.joda.beans.ser.json.JodaBeanJsonWriter.BEAN;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.META;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.TYPE;
import static org.joda.beans.ser.json.JodaBeanJsonWriter.VALUE;

import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from JSON.
 * <p>
 * The JSON format is defined by {@link JodaBeanJsonWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
abstract class AbstractJsonReader {

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
    private final Map<String, Class<?>> knownTypes = new HashMap<>();

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    AbstractJsonReader(final JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Parses the root bean.
     * 
     * @param input  the JSON input
     * @param declaredType  the declared type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    <T> T parseRoot(JsonInput input, Class<T> declaredType) throws Exception {
        this.input = input;
        var parsed = parseObject(input.acceptEvent(JsonEvent.OBJECT), declaredType, null, null, null, true);
        return declaredType.cast(parsed);
    }

    // parse a bean, event after object start passed in
    private Object parseBean(JsonEvent event, Class<?> beanType) {
        var propName = "";
        try {
            var deser = settings.getDeserializers().findDeserializer(beanType);
            var metaBean = deser.findMetaBean(beanType);
            var builder = deser.createBuilder(beanType, metaBean);
            while (event != JsonEvent.OBJECT_END) {
                // property name
                propName = input.acceptObjectKey(event);
                var metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                // ignore unknown properties
                if (metaProp == null || metaProp.style().isDerived()) {
                    input.skipData();
                } else {
                    var value = parseObject(input.readEvent(),
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
            Class<?> inputDeclaredType,
            MetaProperty<?> metaProp,
            Class<?> beanType,
            SerIterable parentIterable,
            boolean rootType) throws Exception {

        // avoid nulls
        var declaredType = (inputDeclaredType == null ? Object.class : inputDeclaredType);
        // establish type
        if (event == JsonEvent.OBJECT) {
            event = input.readEvent();
            if (event == JsonEvent.STRING) {
                var key = input.parseObjectKey();
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
                    childIterable = settings.getIteratorFactory().createIterable(metaProp, beanType, true);
                } else if (parentIterable != null) {
                    childIterable = settings.getIteratorFactory().createIterable(parentIterable);
                }
                if (childIterable == null) {
                    if (event == JsonEvent.ARRAY) {
                        childIterable = parseUnknownArray(declaredType);
                    } else {
                        childIterable = parseUnknownObject(declaredType);
                    }
                }
                return parseIterable(event, childIterable);
            } else {
                return parseSimple(event, declaredType);
            }
        }
    }

    SerIterable parseUnknownArray(Class<?> declaredType) {
        throw new IllegalArgumentException("JSON contained an array without information about the Java type");
    }

    SerIterable parseUnknownObject(Class<?> declaredType) {
        throw new IllegalArgumentException("JSON contained an object without information about the Java type");
    }

    private Object parseTypedBean(Class<?> declaredType, boolean rootType) throws Exception {
        var typeStr = input.acceptString();
        Class<?> effectiveType = SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
        if (rootType) {
            if (!Bean.class.isAssignableFrom(effectiveType)) {
                throw new IllegalArgumentException("Root type is not a Joda-Bean: " + effectiveType.getName());
            }
            basePackage = effectiveType.getPackage().getName() + ".";
        }
        if (!declaredType.isAssignableFrom(effectiveType)) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " +
                declaredType.getName() + " and " + effectiveType.getName());
        }
        var event = input.readEvent();
        if (event == JsonEvent.COMMA) {
            event = input.readEvent();
        }
        return parseBean(event, effectiveType);
    }

    private Object parseTypedSimple(Class<?> declaredType) throws Exception {
        var typeStr = input.acceptString();
        var effectiveType = settings.getDeserializers().decodeType(typeStr, settings, basePackage, knownTypes, declaredType);
        if (!declaredType.isAssignableFrom(effectiveType)) {
            throw new IllegalArgumentException("Specified type is incompatible with declared type: " +
                declaredType.getName() + " and " + effectiveType.getName());
        }
        input.acceptEvent(JsonEvent.COMMA);
        var valueKey = input.acceptObjectKey(input.readEvent());
        if (!valueKey.equals(VALUE)) {
            throw new IllegalArgumentException("Invalid JSON data: Expected 'value' key but found " + valueKey);
        }
        var result = parseSimple(input.readEvent(), effectiveType);
        input.acceptEvent(JsonEvent.OBJECT_END);
        return result;
    }

    private Object parseTypedMeta() throws Exception {
        var metaType = input.acceptString();
        var childIterable = settings.getIteratorFactory().createIterable(metaType, settings, knownTypes);
        input.acceptEvent(JsonEvent.COMMA);
        var valueKey = input.acceptObjectKey(input.readEvent());
        if (!valueKey.equals(VALUE)) {
            throw new IllegalArgumentException("Invalid JSON data: Expected 'value' key but found " + valueKey);
        }
        var result = parseIterable(input.readEvent(), childIterable);
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
                var keyStr = input.acceptObjectKey(event);
                var key = parseText(keyStr, iterable.keyType());
                var value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
                iterable.add(key, null, value, 1);
                event = input.acceptObjectSeparator();
            }
        } else if (event == JsonEvent.ARRAY) {
            event = input.readEvent();
            while (event != JsonEvent.ARRAY_END) {
                input.ensureEvent(event, JsonEvent.ARRAY);
                var key = parseObject(input.readEvent(), iterable.keyType(), null, null, null, false);
                input.acceptEvent(JsonEvent.COMMA);
                var value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
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
            var key = parseObject(input.readEvent(), iterable.keyType(), null, null, null, false);
            input.acceptEvent(JsonEvent.COMMA);
            var column = parseObject(input.readEvent(), iterable.columnType(), null, null, null, false);
            input.acceptEvent(JsonEvent.COMMA);
            var value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
            iterable.add(key, column, value, 1);
            input.acceptEvent(JsonEvent.ARRAY_END);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseIterableGrid(JsonEvent event, SerIterable iterable) throws Exception {
        input.ensureEvent(event, JsonEvent.ARRAY);
        input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
        var rows = (int) input.parseNumberIntegral();
        input.acceptEvent(JsonEvent.COMMA);
        input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
        var columns = (int) input.parseNumberIntegral();
        iterable.dimensions(new int[] {rows, columns});
        event = input.acceptArraySeparator();
        while (event != JsonEvent.ARRAY_END) {
            input.ensureEvent(event, JsonEvent.ARRAY);
            input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
            var row = (int) input.parseNumberIntegral();
            input.acceptEvent(JsonEvent.COMMA);
            input.acceptEvent(JsonEvent.NUMBER_INTEGRAL);
            var column = (int) input.parseNumberIntegral();
            input.acceptEvent(JsonEvent.COMMA);
            var value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
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
            var value = parseObject(input.readEvent(), iterable.valueType(), null, null, iterable, false);
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
            var value = parseObject(event, iterable.valueType(), null, null, iterable, false);
            iterable.add(null, null, value, 1);
            event = input.acceptArraySeparator();
        }
        return iterable.build();
    }

    private Object parseSimple(JsonEvent event, Class<?> type) throws Exception {
        switch (event) {
            case STRING: {
                var text = input.parseString();
                return parseText(text, type);
            }
            case NUMBER_INTEGRAL: {
                var value = input.parseNumberIntegral();
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
                    
                } else if (type == Double.class || type == double.class) {
                    double dblVal = value;
                    if (value != (long) dblVal) {
                        throw new IllegalArgumentException("Invalid JSON data: Value exceeds capacity of double: " + value);
                    }
                    return Double.valueOf(dblVal);
                    
                } else if (type == Float.class || type == float.class) {
                    float fltVal = value;
                    if (value != (long) fltVal) {
                        throw new IllegalArgumentException("Invalid JSON data: Value exceeds capacity of float: " + value);
                    }
                    return Float.valueOf(fltVal);
                    
                } else if (type == Integer.class || type == int.class) {
                    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                        throw new IllegalArgumentException("Invalid JSON data: Expected int, but was " + value);
                    }
                    return Integer.valueOf((int) value);
                } else {
                    if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
                        return Long.valueOf(value);
                    }
                    return Integer.valueOf((int) value);
                }
            }
            case NUMBER_FLOATING: {
                var value = input.parseNumberFloating();
                if (type == Float.class || type == float.class) {
                    return Float.valueOf((float) value);
                } else {
                    return Double.valueOf(value);
                }
            }
            case NULL: {
                if (type == double.class || type == Double.class) {
                    return Double.NaN;  // leniently accept null for NaN
                } else if (type == float.class || type == Float.class) {
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

    private Object parseText(String text, Class<?> type) {
        if (type == Object.class || type.isAssignableFrom(String.class)) {
            return text;
        }
        return settings.getConverter().convertFromString(type, text);
    }

}
