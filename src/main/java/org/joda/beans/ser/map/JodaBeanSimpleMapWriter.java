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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to a JSON-like in memory {@code Map}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The format used here is natural, with no meta-data.
 * As such, it may not be possible to write some objects or read the JSON data back in.
 * <p>
 * Beans are output as maps where the key is the property name.
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * Null values are generally omitted, booleans and numbers are left as is.
 * Maps must have a key that can be converted to a string by Joda-Convert.
 * The property type needs to be known when writing/reading - properties, or
 * list/map entries, that are defined as {@code Object} are unlikely to work well.
 * <p>
 * Collections are output using lists, Maps as maps, with other collection types
 * having a complex list-based format.
 */
public class JodaBeanSimpleMapWriter {

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanSimpleMapWriter(JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    public Map<String, Object> write(Bean bean) {
        JodaBeanUtils.notNull(bean, "bean");
        return writeBean(bean, bean.getClass());
    }

    //-----------------------------------------------------------------------
    // write a bean as a JSON object
    private Map<String, Object> writeBean(Bean bean, Class<?> declaredType) {
        Map<String, Object> result = new LinkedHashMap<>();
        // property information
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                Object value = SerOptional.extractValue(prop, bean);
                if (value != null) {
                    Object outputValue = null;
                    Class<?> propType = SerOptional.extractType(prop, bean.getClass());
                    if (value instanceof Bean) {
                        if (settings.getConverter().isConvertible(value.getClass())) {
                            outputValue = writeSimple(propType, value);
                        } else {
                            outputValue = writeBean((Bean) value, propType);
                        }
                    } else {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass(), true);
                        if (itemIterator != null) {
                            outputValue = writeElements(itemIterator);
                        } else {
                            outputValue = writeSimple(propType, value);
                        }
                    }
                    result.put(prop.name(), outputValue);
                }
            }
        }
        return result;
    }

    //-----------------------------------------------------------------------
    // write a collection
    private Object writeElements(SerIterator itemIterator) {
        if (itemIterator.category() == SerCategory.MAP) {
            return writeMap(itemIterator);
        } else if (itemIterator.category() == SerCategory.COUNTED) {
            return writeCounted(itemIterator);
        } else if (itemIterator.category() == SerCategory.TABLE) {
            return writeTable(itemIterator);
        } else if (itemIterator.category() == SerCategory.GRID) {
            return writeGrid(itemIterator);
        } else {
            return writeArray(itemIterator);
        }
    }

    // write list/set/array
    private Object writeArray(SerIterator itemIterator) {
        List<Object> result = new ArrayList<>();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            result.add(writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator));
        }
        return result;
    }

    // write map
    private Object writeMap(SerIterator itemIterator) {
        if (itemIterator.size() == 0) {
            return new LinkedHashMap<>();
        }
        // if key type is known and convertible use short key format, else use full bean format
        if (settings.getConverter().isConvertible(itemIterator.keyType())) {
            return writeMapSimple(itemIterator);
        } else {
            return writeMapComplex(itemIterator);
        }
    }

    // write map with simple keys
    private Object writeMapSimple(SerIterator itemIterator) {
        Map<String, Object> result = new LinkedHashMap<>();
        StringConverter<Object> keyConverter = settings.getConverter().findConverterNoGenerics(itemIterator.keyType());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object key = itemIterator.key();
            if (key == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be null");
            }
            String str = keyConverter.convertToString(itemIterator.key());
            if (str == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be a null string");
            }
            result.put(str, writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator));
        }
        return result;
    }

    // write map with complex keys
    private Object writeMapComplex(SerIterator itemIterator) {
        Map<String, Object> result = new LinkedHashMap<>();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object key = itemIterator.key();
            if (key == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be null");
            }
            String str = settings.getConverter().convertToString(itemIterator.key());
            if (str == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be a null string");
            }
            result.put(str, writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator));
        }
        return result;
    }

    // write table
    private Object writeTable(SerIterator itemIterator) {
        List<Object> result = new ArrayList<>();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object outputKey = writeObject(itemIterator.keyType(), itemIterator.key(), null);
            Object outputCol = writeObject(itemIterator.columnType(), itemIterator.column(), null);
            Object outputValue = writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            result.add(Arrays.asList(outputKey, outputCol, outputValue));
        }
        return result;
    }

    // write grid using sparse approach
    private Object writeGrid(SerIterator itemIterator) {
        List<Object> result = new ArrayList<>();
        result.add(itemIterator.dimensionSize(0));
        result.add(itemIterator.dimensionSize(1));
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Integer outputKey = (Integer) itemIterator.key();
            Integer outputCol = (Integer) itemIterator.column();
            Object outputValue = writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            result.add(Arrays.asList(outputKey, outputCol, outputValue));
        }
        return result;
    }

    // write counted set
    private Object writeCounted(final SerIterator itemIterator) {
        List<Object> result = new ArrayList<>();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object outputValue = writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            Integer outputCount = itemIterator.count();
            result.add(Arrays.asList(outputValue, outputCount));
        }
        return result;
    }

    // write collection object
    private Object writeObject(Class<?> declaredType, Object obj, SerIterator parentIterator) {
        if (obj == null) {
            return null;
        } else if (settings.getConverter().isConvertible(obj.getClass())) {
            return writeSimple(declaredType, obj);
        } else if (obj instanceof Bean) {
            return writeBean((Bean) obj, declaredType);
        } else if (parentIterator != null) {
            SerIterator childIterator = settings.getIteratorFactory().createChild(obj, parentIterator);
            if (childIterator != null) {
                return writeElements(childIterator);
            } else {
                return writeSimple(declaredType, obj);
            }
        } else {
            return writeSimple(declaredType, obj);
        }
    }

    //-----------------------------------------------------------------------
    // write simple type
    private Object writeSimple(Class<?> declaredType, Object value) {
        Class<?> realType = value.getClass();
        if (realType == Integer.class || realType == Long.class || realType == Short.class ||
                realType == Byte.class || realType == Float.class || realType == Double.class ||
                realType == Boolean.class) {
            return value;
        } else {
            // write as a string
            try {
                String converted = settings.getConverter().convertToString(realType, value);
                if (converted == null) {
                    throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
                }
                return converted;
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Unable to convert type " + declaredType.getName() + " for real type: " + realType.getName(), ex);
            }
        }
    }

}
