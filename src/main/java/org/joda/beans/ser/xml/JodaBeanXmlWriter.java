/*
 *  Copyright 2001-2013 Stephen Colebourne
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
import static org.joda.beans.ser.xml.JodaBeanXml.COUNT;
import static org.joda.beans.ser.xml.JodaBeanXml.KEY;
import static org.joda.beans.ser.xml.JodaBeanXml.METATYPE;
import static org.joda.beans.ser.xml.JodaBeanXml.NULL;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterator;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to XML.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
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
     * The string builder.
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
     * 
     * @param bean  the bean to output, not null
     * @return the XML, not null
     */
    public String write(final Bean bean) {
        return writeToBuilder(bean).toString();
    }

    /**
     * Writes the bean to the {@code StringBuilder}.
     * 
     * @param bean  the bean to output, not null
     * @return the builder, not null
     */
    public StringBuilder writeToBuilder(final Bean bean) {
        if (bean == null) {
            throw new NullPointerException("bean");
        }
        this.rootBean = bean;
        this.basePackage = bean.getClass().getPackage().getName() + ".";
        
        String type = rootBean.getClass().getName();
        writeHeader();
        builder.append('<').append(BEAN);
        appendAttribute(builder, TYPE, type);
        builder.append('>').append(settings.getNewLine());
        writeBean(rootBean, settings.getIndent());
        builder.append('<').append('/').append(BEAN).append('>').append(settings.getNewLine());
        return builder;
    }

    private void writeHeader() {
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    private boolean writeBean(final Bean bean, final String currentIndent) {
        boolean output = false;
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable()) {
                output = true;
                Object value = prop.get(bean);
                if (value != null) {
                    String propName = prop.name();
                    Class<?> propType = prop.propertyType();
                    if (value instanceof Bean) {
                        if (settings.getConverter().isConvertible(value.getClass())) {
                            writeSimple(currentIndent, propName, new StringBuilder(), propType, value);
                        } else {
                            writeBean(currentIndent, propName, new StringBuilder(), propType, (Bean) value);
                        }
                    } else {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                        if (itemIterator != null) {
                            writeElements(currentIndent, propName, new StringBuilder(), itemIterator);
                        } else {
                            writeSimple(currentIndent, propName, new StringBuilder(), propType, value);
                        }
                    }
                }
            }
        }
        return output;
    }

    //-----------------------------------------------------------------------
    private void writeBean(final String currentIndent, final String tagName, final StringBuilder attrs, final Class<?> propType, final Bean value) {
        builder.append(currentIndent).append('<').append(tagName).append(attrs);
        if (value.getClass() != propType) {
            String typeStr = settings.encodeClass(value.getClass(), basePackage);
            appendAttribute(builder, TYPE, typeStr);
        }
        builder.append('>').append(settings.getNewLine());
        if (writeBean(value, currentIndent + settings.getIndent())) {
            builder.append(currentIndent).append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        } else {
            builder.insert(builder.length() - 2, '/');
        }
    }

    //-----------------------------------------------------------------------
    private void writeElements(final String currentIndent, final String tagName, final StringBuilder attrs, final SerIterator itemIterator) {
        if (itemIterator.size() == 0) {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('/').append('>').append(settings.getNewLine());
        } else {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>').append(settings.getNewLine());
            writeElements(currentIndent + settings.getIndent(), itemIterator);
            builder.append(currentIndent).append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        }
    }

    private void writeElements(final String currentIndent, final SerIterator itemIterator) {
        StringConverter<Object> converter = null;
        if (itemIterator.keyType() != null) {
            converter = settings.getConverter().findConverterNoGenerics(itemIterator.keyType());
        }
        while (itemIterator.hasNext()) {
            itemIterator.next();
            StringBuilder attr = new StringBuilder(32);
            if (converter != null) {
                String keyStr = encodeAttribute(converter.convertToString(itemIterator.key()));
                if (keyStr == null) {
                    throw new IllegalArgumentException("Unable to embed map key as it cannot be represented as a string: " + itemIterator.key());
                }
                appendAttribute(attr, KEY, keyStr);
            }
            if (itemIterator.count() != 1) {
                appendAttribute(attr, COUNT, Integer.toString(itemIterator.count()));
            }
            Object value = itemIterator.value();
            writeElement(currentIndent, attr, itemIterator.valueType(), value);
        }
    }

    //-----------------------------------------------------------------------
    private void writeElement(final String currentIndent, final StringBuilder attrs, final Class<?> valueType, final Object value) {
        if (value == null) {
            appendAttribute(attrs, NULL, "true");
            builder.append(currentIndent).append("<item").append(attrs).append("/>").append(settings.getNewLine());
        } else if (value instanceof Bean) {
            if (settings.getConverter().isConvertible(value.getClass())) {
                writeSimple(currentIndent, "item", attrs, valueType, value);
            } else {
                writeBean(currentIndent, "item", attrs, valueType, (Bean) value);
            }
        } else {
            SerIterator itemIterator = settings.getIteratorFactory().create(value);
            if (itemIterator != null) {
                appendAttribute(attrs, METATYPE, itemIterator.metaTypeName());
                writeElements(currentIndent, "item", attrs, itemIterator);
            } else {
                writeSimple(currentIndent, "item", attrs, valueType, value);
            }
        }
    }

    //-----------------------------------------------------------------------
    private void writeSimple(final String currentIndent, final String tagName, final StringBuilder attrs, Class<?> type, final Object value) {
        if (type == Object.class) {
            type = value.getClass();
            if (type != String.class) {
                String typeStr = settings.encodeClass(type, basePackage);
                appendAttribute(attrs, TYPE, typeStr);
            }
        }
        String converted = settings.getConverter().convertToString(type, value);
        builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>');
        appendEncoded(converted);
        builder.append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
    }

    private StringBuilder appendEncoded(final String text) {
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
                    builder.append("&#9;");
                    break;
                case '\n':
                    builder.append("&#xA;");
                    break;
                case '\r':
                    builder.append("&#xD;");
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

    //-----------------------------------------------------------------------
    private StringBuilder appendAttribute(final StringBuilder buf, final String attrName, final String encodedValue) {
        return buf.append(' ').append(attrName).append('=').append('\"').append(encodedValue).append('\"');
    }

    private String encodeAttribute(final String text) {
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
