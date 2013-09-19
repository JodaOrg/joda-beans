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

import java.util.regex.Pattern;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterator;
import org.joda.convert.StringConverter;

/**
 * Provides the ability for a Joda-Bean to be written to XML.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanXmlWriter {

    /**
     * Pattern matching java lang simple classes.
     */
    private static final Pattern JAVA_LANG = Pattern.compile("java[.]lang[.][a-zA-Z]+");

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The root bean.
     */
    private final Bean rootBean;
    /**
     * The base package.
     */
    private final String basePackage;
    /**
     * The string builder.
     */
    private final StringBuilder builder;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param bean  the bean to output, not null
     */
    public JodaBeanXmlWriter(JodaBeanSer settings, Bean bean) {
        this(settings, bean, new StringBuilder(1024));
    }

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param bean  the bean to output, not null
     * @param builder  the builder to output to, not null
     */
    public JodaBeanXmlWriter(JodaBeanSer settings, Bean bean, StringBuilder builder) {
        this.settings = settings;
        this.rootBean = bean;
        this.basePackage = bean.getClass().getPackage().getName() + ".";
        this.builder = builder;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * 
     * @return the XML, not null
     */
    public String write() {
        return writeToBuilder().toString();
    }

    /**
     * Writes the bean to the {@code StringBuilder}.
     * 
     * @return the builder, not null
     */
    public StringBuilder writeToBuilder() {
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
    private boolean writeBean(Bean bean, String currentIndent) {
        boolean output = false;
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable()) {
                output = true;
                Object value = prop.get(bean);
                if (value != null) {
                    String propName = prop.name();
                    Class<?> propType = prop.propertyType();
                    if (value instanceof Bean) {
                        writeBean(currentIndent, propName, new StringBuilder(), propType, (Bean) value);
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
    private void writeBean(String currentIndent, String tagName, StringBuilder attrs, Class<?> propType, Bean value) {
        builder.append(currentIndent).append('<').append(tagName).append(attrs);
        if (value.getClass() != propType) {
            String typeStr = value.getClass().getName();
            if (typeStr.startsWith(basePackage) && Character.isUpperCase(typeStr.charAt(basePackage.length()))) {
                typeStr = typeStr.substring(basePackage.length());
            }
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
    private void writeElements(String currentIndent, String tagName, StringBuilder attrs, SerIterator itemIterator) {
        if (itemIterator.size() == 0) {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('/').append('>').append(settings.getNewLine());
        } else {
            builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>').append(settings.getNewLine());
            writeElements(currentIndent + settings.getIndent(), itemIterator);
            builder.append(currentIndent).append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
        }
    }

    private void writeElements(String currentIndent, SerIterator itemIterator) {
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
    private void writeElement(String currentIndent, StringBuilder attrs, Class<?> valueType, Object value) {
        if (value == null) {
            appendAttribute(attrs, NULL, "true");
            builder.append(currentIndent).append("<item").append(attrs).append("/>").append(settings.getNewLine());
        } else if (value instanceof Bean) {
            writeBean(currentIndent, "item", attrs, valueType, (Bean) value);
        } else {
            SerIterator itemIterator = settings.getIteratorFactory().create(value);
            if (itemIterator != null) {
                appendAttribute(attrs, METATYPE, itemIterator.simpleTypeName());
                writeElements(currentIndent, "item", attrs, itemIterator);
            } else {
                writeSimple(currentIndent, "item", attrs, valueType, value);
            }
        }
    }

    //-----------------------------------------------------------------------
    private void writeSimple(String currentIndent, String tagName, StringBuilder attrs, Class<?> propType, Object value) {
        if (propType == Object.class) {
            propType = value.getClass();
            if (propType != String.class) {
                String typeName = value.getClass().getName();
                if (JAVA_LANG.matcher(propType.getName()).matches()) {
                    typeName = propType.getSimpleName();
                }
                appendAttribute(attrs, TYPE, typeName);
            }
        }
        String converted = settings.getConverter().convertToString(propType, value);
        builder.append(currentIndent).append('<').append(tagName).append(attrs).append('>');
        appendEncoded(converted);
        builder.append('<').append('/').append(tagName).append('>').append(settings.getNewLine());
    }

    private StringBuilder appendEncoded(String text) {
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
    private StringBuilder appendAttribute(StringBuilder buf, String attrName, String encodedValue) {
        return buf.append(' ').append(attrName).append('=').append('\"').append(encodedValue).append('\"');
    }

    private String encodeAttribute(String text) {
        return appendEncodedAttribute(new StringBuilder(text.length() + 16), text).toString();
    }

    private StringBuilder appendEncodedAttribute(StringBuilder builder, String text) {
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
