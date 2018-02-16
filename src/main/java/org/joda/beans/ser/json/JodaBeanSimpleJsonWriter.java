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

import java.io.IOException;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to a simple JSON format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The JSON format used here is natural, with no meta-data.
 * As such, it may not be possible to write some objects or read the JSON data back in.
 * <p>
 * Beans are output using JSON objects where the key is the property name.
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * Null values are generally omitted, but where included are sent as 'null'.
 * Boolean values are sent as 'true' and 'false'.
 * Numeric values are sent as JSON numbers.
 * Maps must have a key that can be converted to a string by Joda-Convert.
 * The property type needs to be known when writing/reading - properties, or
 * list/map entries, that are defined as {@code Object} are unlikely to work well.
 * <p>
 * Collections are output using JSON arrays. Maps as JSON objects.
 * Multisets are output as a map of value to count.
 */
public class JodaBeanSimpleJsonWriter {

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The outputter.
     */
    private JsonOutput output;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanSimpleJsonWriter(final JodaBeanSer settings) {
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
    public String write(Bean bean) {
        StringBuilder buf = new StringBuilder(1024);
        try {
            write(bean, buf);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return buf.toString();
    }

    /**
     * Writes the bean to the {@code Appendable}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, Appendable output) throws IOException {
        JodaBeanUtils.notNull(bean, "bean");
        JodaBeanUtils.notNull(output, "output");
        this.output = new JsonOutput(output, settings.getIndent(), settings.getNewLine());
        writeBean(bean, bean.getClass());
        output.append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    // write a bean as a JSON object
    private void writeBean(Bean bean, Class<?> declaredType) throws IOException {
        output.writeObjectStart();
        // property information
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                Object value = SerOptional.extractValue(prop, bean);
                if (value != null) {
                    output.writeObjectKey(prop.name());
                    Class<?> propType = SerOptional.extractType(prop, bean.getClass());
                    if (value instanceof Bean) {
                        if (settings.getConverter().isConvertible(value.getClass())) {
                            writeSimple(propType, value);
                        } else {
                            writeBean((Bean) value, propType);
                        }
                    } else {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass(), true);
                        if (itemIterator != null) {
                            writeElements(itemIterator);
                        } else {
                            writeSimple(propType, value);
                        }
                    }
                }
            }
        }
        output.writeObjectEnd();
    }

    //-----------------------------------------------------------------------
    // write a collection
    private void writeElements(SerIterator itemIterator) throws IOException {
        if (itemIterator.category() == SerCategory.MAP) {
            writeMap(itemIterator);
        } else if (itemIterator.category() == SerCategory.COUNTED) {
            writeCounted(itemIterator);
        } else if (itemIterator.category() == SerCategory.TABLE) {
            writeTable(itemIterator);
        } else if (itemIterator.category() == SerCategory.GRID) {
            writeGrid(itemIterator);
        } else {
            writeArray(itemIterator);
        }
    }

    // write list/set/array
    private void writeArray(SerIterator itemIterator) throws IOException {
        output.writeArrayStart();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayItemStart();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
        output.writeArrayEnd();
    }

    // write map
    private void writeMap(SerIterator itemIterator) throws IOException {
        // if key type is known and convertible use short key format, else use full bean format
        if (settings.getConverter().isConvertible(itemIterator.keyType())) {
            writeMapSimple(itemIterator);
        } else {
            writeMapComplex(itemIterator);
        }
    }

    // write map with simple keys
    private void writeMapSimple(SerIterator itemIterator) throws IOException {
        StringConverter<Object> keyConverter = settings.getConverter().findConverterNoGenerics(itemIterator.keyType());
        output.writeObjectStart();
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
            output.writeObjectKey(str);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
        output.writeObjectEnd();
    }

    // write map with complex keys
    private void writeMapComplex(SerIterator itemIterator) throws IOException {
        output.writeObjectStart();
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
            output.writeObjectKey(str);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
        output.writeObjectEnd();
    }

    // write table
    private void writeTable(SerIterator itemIterator) throws IOException {
        output.writeArrayStart();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayItemStart();
            output.writeArrayStart();
            output.writeArrayItemStart();
            writeObject(itemIterator.keyType(), itemIterator.key(), null);
            output.writeArrayItemStart();
            writeObject(itemIterator.columnType(), itemIterator.column(), null);
            output.writeArrayItemStart();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeArrayEnd();
        }
        output.writeArrayEnd();
    }

    // write grid using sparse approach
    private void writeGrid(SerIterator itemIterator) throws IOException {
        output.writeArrayStart();
        output.writeArrayItemStart();
        output.writeInt(itemIterator.dimensionSize(0));
        output.writeArrayItemStart();
        output.writeInt(itemIterator.dimensionSize(1));
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayItemStart();
            output.writeArrayStart();
            output.writeArrayItemStart();
            output.writeInt((Integer) itemIterator.key());
            output.writeArrayItemStart();
            output.writeInt((Integer) itemIterator.column());
            output.writeArrayItemStart();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeArrayEnd();
        }
        output.writeArrayEnd();
    }

    // write counted set
    private void writeCounted(final SerIterator itemIterator) throws IOException {
        output.writeArrayStart();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayItemStart();
            output.writeArrayStart();
            output.writeArrayItemStart();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeArrayItemStart();
            output.writeInt(itemIterator.count());
            output.writeArrayEnd();
        }
        output.writeArrayEnd();
    }

    // write collection object
    private void writeObject(Class<?> declaredType, Object obj, SerIterator parentIterator) throws IOException {
        if (obj == null) {
            output.writeNull();
        } else if (settings.getConverter().isConvertible(obj.getClass())) {
            writeSimple(declaredType, obj);
        } else if (obj instanceof Bean) {
            writeBean((Bean) obj, declaredType);
        } else if (parentIterator != null) {
            SerIterator childIterator = settings.getIteratorFactory().createChild(obj, parentIterator);
            if (childIterator != null) {
                writeElements(childIterator);
            } else {
                writeSimple(declaredType, obj);
            }
        } else {
            writeSimple(declaredType, obj);
        }
    }

    //-----------------------------------------------------------------------
    // write simple type
    private void writeSimple(Class<?> declaredType, Object value) throws IOException {
        Class<?> realType = value.getClass();
        if (realType == Integer.class) {
            output.writeInt(((Integer) value).intValue());
        } else if (realType == Long.class) {
            output.writeLong(((Long) value).longValue());
        } else if (realType == Short.class) {
            output.writeInt(((Short) value).shortValue());
        } else if (realType == Byte.class) {
            output.writeInt(((Byte) value).byteValue());
        } else if (realType == Float.class) {
            float flt = ((Float) value).floatValue();
            if (Float.isNaN(flt)) {
                // write as string
                output.writeNull();
            } else if (Float.isInfinite(flt)) {
                // write as string
                output.writeString(Float.toString(flt));
            } else {
                output.writeFloat(flt);
            }
        } else if (realType == Double.class) {
            double dbl = ((Double) value).doubleValue();
            if (Double.isNaN(dbl)) {
                // write as string
                output.writeNull();
            } else if (Double.isInfinite(dbl)) {
                // write as string
                output.writeString(Double.toString(dbl));
            } else {
                output.writeDouble(dbl);
            }
        } else if (realType == Boolean.class) {
            output.writeBoolean(((Boolean) value).booleanValue());
        } else {
            // write as a string
            try {
                String converted = settings.getConverter().convertToString(realType, value);
                if (converted == null) {
                    throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
                }
                output.writeString(converted);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Unable to convert type " + declaredType.getName() + " for real type: " + realType.getName(), ex);
            }
        }
    }

}
