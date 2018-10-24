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
package org.joda.beans.ser.xml;

import static org.joda.beans.ser.xml.JodaBeanXml.BEAN;
import static org.joda.beans.ser.xml.JodaBeanXml.COL;
import static org.joda.beans.ser.xml.JodaBeanXml.COLS;
import static org.joda.beans.ser.xml.JodaBeanXml.COUNT;
import static org.joda.beans.ser.xml.JodaBeanXml.ENTRY;
import static org.joda.beans.ser.xml.JodaBeanXml.ITEM;
import static org.joda.beans.ser.xml.JodaBeanXml.KEY;
import static org.joda.beans.ser.xml.JodaBeanXml.METATYPE;
import static org.joda.beans.ser.xml.JodaBeanXml.NULL;
import static org.joda.beans.ser.xml.JodaBeanXml.ROW;
import static org.joda.beans.ser.xml.JodaBeanXml.ROWS;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to XML.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The XML consists of a root level 'bean' element with a 'type' attribute.
 * At each subsequent level, a bean is output using the property name.
 * Where necessary, the 'type' attribute is used to clarify a type.
 * <p>
 * Simple types, defined by Joda-Convert, are output as strings.
 * Beans are output recursively within the parent property element.
 * Collections are output using 'item' elements within the property element.
 * The 'item' elements will use 'key' for map keys, 'count' for multiset counts
 * and 'null=true' for null entries. Note that map keys must be simple types.
 * <p>
 * If a collection contains a collection then more information is written.
 * A 'metatype' attribute is added to define the high level type, such as List.
 * At this level, the data read back may not be identical to that written.
 * <p>
 * Type names are shortened by the package of the root type if possible.
 * Certain basic types are also handled, such as String, Integer, File and URI.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanXmlWriter {

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The string builder, unused if writing to an {@code Appendable}.
     */
    private final StringBuilder builder;
    /**
     * The root bean.
     */
    private Bean rootBean;
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
    public JodaBeanXmlWriter(final JodaBeanSer settings) {
        this(settings, new StringBuilder(1024));
    }

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param builder  the builder to output to, not null
     */
    public JodaBeanXmlWriter(final JodaBeanSer settings, final StringBuilder builder) {
        this.settings = settings;
        this.builder = builder;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @return the XML, not null
     */
    public String write(final Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to a string.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the XML, not null
     */
    public String write(final Bean bean, final boolean rootType) {
        return writeToBuilder(bean, rootType).toString();
    }

    /**
     * Writes the bean to the {@code StringBuilder}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @return the builder, not null
     */
    public StringBuilder writeToBuilder(final Bean bean) {
        return writeToBuilder(bean, true);
    }

    /**
     * Writes the bean to the {@code StringBuilder}.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the builder, not null
     */
    public StringBuilder writeToBuilder(final Bean bean, final boolean rootType) {
        try {
            write(bean, rootType, this.builder);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return builder;
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
    public void write(final Bean bean, final Appendable output) throws IOException {
        write(bean, true, output);
    }

    /**
     * Writes the bean to the {@code Appendable}.
     *
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @param builder  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(final Bean bean, final boolean rootType, final Appendable builder) throws IOException {
        if (bean == null) {
            throw new NullPointerException("bean");
        }
        this.rootBean = bean;
        this.basePackage = (rootType ? bean.getClass().getPackage().getName() + "." : null);
        
        String type = rootBean.getClass().getName();
        writeHeader(builder);
        builder.append('<').append(BEAN);
        if (rootType) {
            appendAttribute(builder, TYPE, type);
        }
        builder.append('>').append(settings.getNewLine());
        writeBean(rootBean, settings.getIndent(), builder);
        builder.append('<').append('/').append(BEAN).append('>').append(settings.getNewLine());
    }

    private void writeHeader(Appendable builder) throws IOException {
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    private boolean willWriteBean(final Bean bean) {
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                return true;
            }
        }
        return false;
    }

    private void writeBean(final Bean bean, final String currentIndent, final Appendable builder) throws IOException {
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                Object value = SerOptional.extractValue(prop, bean);
                if (value != null) {
                    String propName = prop.name();
                    Class<?> propType = SerOptional.extractType(prop, bean.getClass());
                    if (value instanceof Bean) {
                        if (settings.getConverter().isConvertible(value.getClass())) {
                            writeSimple(currentIndent, propName, new StringBuilder(), propType, value, builder);
                        } else {
                            writeBean(currentIndent, propName, new StringBuilder(), propType, (Bean) value, builder);
                        }
                    } else {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                        if (itemIterator != null) {
                            writeElements(currentIndent, propName, new StringBuilder(), itemIterator, builder);
                        } else {
                            writeSimple(currentIndent, propName, new StringBuilder(), propType, value, builder);
                        }
                    }
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    private void writeBean(final String currentIndent, final String tagName, final StringBuilder attrs, final Class<?> propType, final Bean value, final Appendable builder) throws IOException {
        if (value == null) {
            throw new IllegalArgumentException("Bean cannot be null");
        }
        builder.append(currentIndent).append('<').append(tagName).append(attrs);
        if (value.getClass() != propType) {
            String typeStr = SerTypeMapper.encodeType(value.getClass(), settings, basePackage, knownTypes);
            appendAttribute(builder, TYPE, typeStr);
        }
        if (willWriteBean(value)) {
            builder.append('>').append(settings.getNewLine());
            writeBean(value, currentIndent + settings.getIndent(), builder);
            builder.append(currentIndent).append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        } else {
            builder.append('/').append('>').append(settings.getNewLine());
        }
    }

    //-----------------------------------------------------------------------
    private void writeElements(final String currentIndent, final String tagName, final StringBuilder attrs, final SerIterator itemIterator, final Appendable builder) throws IOException {
        if (itemIterator.metaTypeRequired()) {
            appendAttribute(attrs, METATYPE, itemIterator.metaTypeName());
        }
        if (itemIterator.category() == SerCategory.GRID) {
            appendAttribute(attrs, ROWS, Integer.toString(itemIterator.dimensionSize(0)));
            appendAttribute(attrs, COLS, Integer.toString(itemIterator.dimensionSize(1)));
        }
        if (itemIterator.size() == 0) {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('/').append('>').append(settings.getNewLine());
        } else {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>').append(settings.getNewLine());
            writeElements(currentIndent + settings.getIndent(), itemIterator, builder);
            builder.append(currentIndent).append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        }
    }

    private void writeElements(final String currentIndent, final SerIterator itemIterator, final Appendable builder) throws IOException {
        // find converter once for performance, and before checking if key is bean
        StringConverter<Object> keyConverter = null;
        StringConverter<Object> rowConverter = null;
        StringConverter<Object> columnConverter = null;
        boolean keyBean = false;
        if (itemIterator.category() == SerCategory.TABLE || itemIterator.category() == SerCategory.GRID) {
            try {
                rowConverter = settings.getConverter().findConverterNoGenerics(itemIterator.keyType());
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Unable to write table/grid as declared key type is not a simple type: " + itemIterator.keyType().getName(), ex);
            }
            try {
                columnConverter = settings.getConverter().findConverterNoGenerics(itemIterator.columnType());
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Unable to write table/grid as declared column type is not a simple type: " + itemIterator.columnType().getName(), ex);
            }
        } else if (itemIterator.category() == SerCategory.MAP) {
            // if key type is known and convertible use short key format, else use full bean format
            if (settings.getConverter().isConvertible(itemIterator.keyType())) {
                keyConverter = settings.getConverter().findConverterNoGenerics(itemIterator.keyType());
            } else {
                keyBean = true;
            }
        }
        // output each item
        while (itemIterator.hasNext()) {
            itemIterator.next();
            StringBuilder attr = new StringBuilder(32);
            if (keyConverter != null) {
                String keyStr = convertToString(keyConverter, itemIterator.key(), "map key");
                appendAttribute(attr, KEY, keyStr);
            }
            if (rowConverter != null) {
                String rowStr = convertToString(rowConverter, itemIterator.key(), "table row");
                appendAttribute(attr, ROW, rowStr);
                String colStr = convertToString(columnConverter, itemIterator.column(), "table column");
                appendAttribute(attr, COL, colStr);
            }
            if (itemIterator.count() != 1) {
                appendAttribute(attr, COUNT, Integer.toString(itemIterator.count()));
            }
            if (keyBean) {
                Object key = itemIterator.key();
                builder.append(currentIndent).append('<').append(ENTRY).append(attr).append('>').append(settings.getNewLine());
                writeKeyElement(currentIndent + settings.getIndent(), key, itemIterator, builder);
                writeValueElement(currentIndent + settings.getIndent(), ITEM, new StringBuilder(), itemIterator, builder);
                builder.append(currentIndent).append('<').append('/').append(ENTRY).append('>').append(settings.getNewLine());
            } else {
                String tagName = itemIterator.category() == SerCategory.MAP ? ENTRY : ITEM;
                writeValueElement(currentIndent, tagName, attr, itemIterator, builder);
            }
        }
    }

    private String convertToString(StringConverter<Object> converter, Object obj, String description) {
        if (obj == null) {
            throw new IllegalArgumentException("Unable to write " + description + " as it cannot be null: " + obj);
        }
        String str = encodeAttribute(converter.convertToString(obj));
        if (str == null) {
            throw new IllegalArgumentException("Unable to write " + description + " as it cannot be a null string: " + obj);
        }
        return str;
    }

    private void writeKeyElement(final String currentIndent, Object key, final SerIterator itemIterator, final Appendable builder) throws IOException {
        if (key == null) {
            throw new IllegalArgumentException("Unable to write map key as it cannot be null: " + key);
        }
        // if key type is known and convertible use short key format
        if (settings.getConverter().isConvertible(itemIterator.keyType())) {
            writeSimple(currentIndent, ITEM, new StringBuilder(), Object.class, key, builder);
        } else if (key instanceof Bean) {
            writeBean(currentIndent, ITEM, new StringBuilder(), itemIterator.keyType(), (Bean) key, builder);
        } else {
            // this case covers where the key type is not known, such as an Object meta-property
            try {
                writeSimple(currentIndent, ITEM, new StringBuilder(), Object.class, key, builder);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException("Unable to write map as declared key type is neither a bean nor a simple type: " + itemIterator.keyType().getName(), ex);
            }
        }
    }

    private void writeValueElement(final String currentIndent, final String tagName, final StringBuilder attrs, final SerIterator itemIterator, final Appendable builder) throws IOException {
        Object value = itemIterator.value();
        Class<?> valueType = itemIterator.valueType();
        if (value == null) {
            appendAttribute(attrs, NULL, "true");
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append("/>").append(settings.getNewLine());
        } else if (value instanceof Bean) {
            if (settings.getConverter().isConvertible(value.getClass())) {
                writeSimple(currentIndent, tagName, attrs, valueType, value, builder);
            } else {
                writeBean(currentIndent, tagName, attrs, valueType, (Bean) value, builder);
            }
        } else {
            SerIterator childIterator = settings.getIteratorFactory().createChild(value, itemIterator);
            if (childIterator != null) {
                writeElements(currentIndent, tagName, attrs, childIterator, builder);
            } else {
                writeSimple(currentIndent, tagName, attrs, valueType, value, builder);
            }
        }
    }

    //-----------------------------------------------------------------------
    private void writeSimple(final String currentIndent, final String tagName, final StringBuilder attrs, final Class<?> declaredType, final Object value, final Appendable builder) throws IOException {
        Class<?> effectiveType;
        if (declaredType == Object.class) {
            Class<?> realType = value.getClass();
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                String typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                appendAttribute(attrs, TYPE, typeStr);
            } else {
                effectiveType = realType;
            }
        } else if (settings.getConverter().isConvertible(declaredType) == false) {
            effectiveType = settings.getConverter().findTypedConverter(value.getClass()).getEffectiveType();
            String typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
            appendAttribute(attrs, TYPE, typeStr);
        } else {
            effectiveType = declaredType;
        }
        try {
            String converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
            }
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>');
            appendEncoded(converted, builder);
            builder.append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to convert type " + effectiveType.getName() + " declared as " + declaredType.getName(), ex);
        }
    }

    private void appendEncoded(final String text, final Appendable builder) throws IOException {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '\t':
                case '\n':
                case '\r':
                    builder.append(ch);
                    break;
                default:
                    if ((int) ch < 32) {
                        throw new IllegalArgumentException("Invalid character for XML: " + ((int) ch));
                    }
                    builder.append(ch);
                    break;
            }
        }
    }

    //-----------------------------------------------------------------------
    private void appendAttribute(final Appendable buf, final String attrName, final String encodedValue) throws IOException {
        buf.append(' ').append(attrName).append('=').append('\"').append(encodedValue).append('\"');
    }

    private String encodeAttribute(final String text) {
        if (text == null) {
            return null;
        }
        return appendEncodedAttribute(new StringBuilder(text.length() + 16), text).toString();
    }

    private StringBuilder appendEncodedAttribute(final StringBuilder builder, final String text) {
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            switch (ch) {
                case '&':
                    builder.append("&amp;");
                    break;
                case '<':
                    builder.append("&lt;");
                    break;
                case '>':
                    builder.append("&gt;");
                    break;
                case '"':
                    builder.append("&quot;");
                    break;
                case '\'':
                    builder.append("&apos;");
                    break;
                case '\t':
                    builder.append("&#09;");
                    break;
                case '\n':
                    builder.append("&#0A;");
                    break;
                case '\r':
                    builder.append("&#0D;");
                    break;
                default:
                    if ((int) ch < 32) {
                        throw new IllegalArgumentException("Invalid character for XML: " + ((int) ch));
                    }
                    builder.append(ch);
                    break;
            }
        }
        return builder;
    }

}
