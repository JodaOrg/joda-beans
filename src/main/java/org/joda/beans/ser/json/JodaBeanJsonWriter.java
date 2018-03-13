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
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to JSON.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The JSON format is kept relatively natural, however some meta-data is added.
 * This has the unfortunate effect of adding an additional object structure to
 * hold the type in a few places.
 * <p>
 * Beans are output using JSON objects where the key is the property name.
 * The type of the bean will be sent using the '&#64;type' property name if necessary.
 * <p>
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * If the simple type requires additional type information, the value is replaced by
 * a JSON object containing the keys '&#64;type' and 'value'.
 * <p>
 * Null values are generally omitted, but where included are sent as 'null'.
 * Boolean values are sent as 'true' and 'false'.
 * Integer and Double values are sent as JSON numbers.
 * Other numeric types are also sent as numbers but may have additional type information.
 * <p>
 * Collections are output using JSON objects or arrays.
 * Multisets are output as a map of value to count.
 * <p>
 * If a collection contains a collection then addition meta-type information is
 * written to aid with deserialization.
 * At this level, the data read back may not be identical to that written.
 * If the collection type requires additional type information, the value is replaced by
 * a JSON object containing the keys '&#64;meta' and 'value'.
 * <p>
 * Type names are shortened by the package of the root type if possible.
 * Certain basic types are also handled, such as String, Integer, File and URI.
 */
public class JodaBeanJsonWriter {

    /**
     * JSON bean type attribute.
     */
    static final String BEAN = "@bean";
    /**
     * JSON simple type attribute.
     */
    static final String TYPE = "@type";
    /**
     * JSON meta-type attribute.
     */
    static final String META = "@meta";
    /**
     * JSON value attribute.
     */
    static final String VALUE = "value";

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The outputter.
     */
    private JsonOutput output;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private Map<Class<?>, String> knownTypes = new HashMap<>();

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanJsonWriter(final JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    public String write(Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to a string specifying whether to include the type at the root.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the JSON, not null
     */
    public String write(Bean bean, boolean rootType) {
        StringBuilder buf = new StringBuilder(1024);
        try {
            write(bean, rootType, buf);
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
        write(bean, true, output);
    }

    /**
     * Writes the bean to the {@code Appendable} specifying whether to include the type at the root.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, boolean rootType, Appendable output) throws IOException {
        JodaBeanUtils.notNull(bean, "bean");
        JodaBeanUtils.notNull(output, "output");
        this.output = new JsonOutput(output, settings.getIndent(), settings.getNewLine());
        writeBean(bean, bean.getClass(), rootType ? RootType.ROOT_WITH_TYPE : RootType.ROOT_WITHOUT_TYPE);
        output.append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    // write a bean as a JSON object
    private void writeBean(Bean bean, Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        output.writeObjectStart();
        // type information
        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            String typeStr = SerTypeMapper.encodeType(bean.getClass(), settings, basePackage, knownTypes);
            if (rootTypeFlag == RootType.ROOT_WITH_TYPE) {
                basePackage = bean.getClass().getPackage().getName() + ".";
            }
            output.writeObjectKeyValue(BEAN, typeStr);
        }
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
                            writeBean((Bean) value, propType, RootType.NOT_ROOT);
                        }
                    } else {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
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
        if (itemIterator.metaTypeRequired()) {
            output.writeObjectStart();
            output.writeObjectKeyValue(META, itemIterator.metaTypeName());
            output.writeObjectKey(VALUE);
        }
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
        if (itemIterator.metaTypeRequired()) {
            output.writeObjectEnd();
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
        output.writeArrayStart();
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object key = itemIterator.key();
            if (key == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be null: " + key);
            }
            output.writeArrayItemStart();
            output.writeArrayStart();
            output.writeArrayItemStart();
            writeObject(itemIterator.keyType(), key, null);
            output.writeArrayItemStart();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeArrayEnd();
        }
        output.writeArrayEnd();
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
            writeBean((Bean) obj, declaredType, RootType.NOT_ROOT);
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
        // simple types have no need to write a type object
        Class<?> realType = value.getClass();
        if (realType == Integer.class) {
            output.writeInt(((Integer) value).intValue());
            return;
        } else if (realType == Double.class) {
            double dbl = ((Double) value).doubleValue();
            if (Double.isNaN(dbl) == false && Double.isInfinite(dbl) == false) {
                output.writeDouble(dbl);
                return;
            }
        } else if (realType == Boolean.class) {
            output.writeBoolean(((Boolean) value).booleanValue());
            return;
        }
        
        // handle no declared type and subclasses
        Class<?> effectiveType = declaredType;
        boolean requiresClose = false;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                String typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                output.writeObjectStart();
                output.writeObjectKeyValue(TYPE, typeStr);
                output.writeObjectKey(VALUE);
                requiresClose = true;
            } else {
                effectiveType = realType;
            }
        } else if (settings.getConverter().isConvertible(declaredType) == false) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            String typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, typeStr);
            output.writeObjectKey(VALUE);
            requiresClose = true;
        }
        
        // long/short/byte/float only processed now to ensure that exact numeric type can be identified
        if (realType == Long.class) {
            output.writeLong(((Long) value).longValue());
            
        } else if (realType == Short.class) {
            output.writeInt(((Short) value).shortValue());
            
        } else if (realType == Byte.class) {
            output.writeInt(((Byte) value).byteValue());
            
        } else if (realType == Float.class) {
            output.writeFloat(((Float) value).floatValue());
            
        } else {
            // write as a string
            try {
                String converted = settings.getConverter().convertToString(effectiveType, value);
                if (converted == null) {
                    throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
                }
                output.writeString(converted);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "Unable to convert type " + effectiveType.getName() + " declared as " + declaredType.getName(), ex);
            }
        }
        
        // close open map
        if (requiresClose) {
            output.writeObjectEnd();
        }
    }

    //-----------------------------------------------------------------------
    enum RootType {
        ROOT_WITH_TYPE,
        ROOT_WITHOUT_TYPE,
        NOT_ROOT,
    }

}
