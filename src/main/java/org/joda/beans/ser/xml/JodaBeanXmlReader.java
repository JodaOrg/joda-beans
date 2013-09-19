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
import static org.joda.beans.ser.xml.JodaBeanXml.BEAN_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.COUNT_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ITEM_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.KEY_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.METATYPE_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.NULL_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE_QNAME;

import java.io.Reader;
import java.io.StringReader;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerIteratorFactory;

/**
 * Provides the ability for a Joda-Bean to read from XML.
 * <p>
 * The XML format is defined by {@link JodaBeanXmlWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanXmlReader {

    /**
     * Factory for parsing.
     */
    private static final XMLInputFactory FACTORY = XMLInputFactory.newFactory();
    static {
        FACTORY.setProperty("javax.xml.stream.isCoalescing", Boolean.TRUE);
    }

    /**
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * The reader.
     */
    private XMLEventReader reader;
    /**
     * The base package.
     */
    private String basePackage;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanXmlReader(JodaBeanSer settings) {
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
        return read(new StringReader(input));
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(Reader input) {
        try {
            reader = FACTORY.createXMLEventReader(input);
            StartElement start = advanceToStartElement();
            if (start.getName().equals(BEAN_QNAME) == false) {
                throw new IllegalArgumentException("Root element must be '" + BEAN + "'");
            }
            Attribute attr = start.getAttributeByName(TYPE_QNAME);
            if (attr == null) {
                throw new IllegalArgumentException("Root element attribute must specify '" + TYPE + "'");
            }
            String typeStr = attr.getValue();
            Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(typeStr);
            MetaBean metaBean = JodaBeanUtils.metaBean(type);
            basePackage = type.getPackage().getName();
            Bean bean = parseBean(metaBean, type);
            reader.close();
            return bean;
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Parses to a bean.
     * 
     * @param metaBean  the meta bean, not null
     * @param beanType  the bean type, not null
     * @return the bean, not null
     */
    private Bean parseBean(MetaBean metaBean, Class<?> beanType) throws Exception {
        try {
            BeanBuilder<? extends Bean> builder = metaBean.builder();
            XMLEvent event = reader.nextEvent();
            while (event.isEndElement() == false) {
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    String name = start.getName().getLocalPart();
                    MetaProperty<?> metaProp = metaBean.metaProperty(name);
                    Class<?> childType = parseTypeAttribute(start, metaProp.propertyType());
                    Object value;
                    if (Bean.class.isAssignableFrom(childType)) {
                        MetaBean childMetaBean = JodaBeanUtils.metaBean(childType);
                        value = parseBean(childMetaBean, childType);
                    } else {
                        SerIterable iterable = SerIteratorFactory.INSTANCE.createIterable(metaProp, beanType);
                        if (iterable != null) {
                            value = parseIterable(iterable);
                        } else {
                            String text = advanceAndParseText();
                            value = settings.getConverter().convertFromString(childType, text);
                        }
                    }
                    builder.set(metaProp, value);
                }
                event = reader.nextEvent();
            }
            return builder.build();
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + metaBean.beanName(), ex);
        }
    }

    /**
     * Parses to a collection wrapper.
     * 
     * @param iterable  the iterable builder, not null
     * @return the iterable, not null
     */
    private Object parseIterable(SerIterable iterable) throws Exception {
        XMLEvent event = reader.nextEvent();
        while (event.isEndElement() == false) {
            if (event.isStartElement()) {
                StartElement start = event.asStartElement();
                if (start.getName().equals(ITEM_QNAME) == false) {
                    throw new IllegalArgumentException("Expected item");
                }
                // key
                Object key = null;
                Attribute keyAttr = start.getAttributeByName(KEY_QNAME);
                if (keyAttr != null) {
                    String keyStr = keyAttr.getValue();
                    if (iterable.keyType() != null) {
                        key = settings.getConverter().convertFromString(iterable.keyType(), keyStr);
                    } else {
                        key = keyStr;
                    }
                }
                // count
                int count = 1;
                Attribute countAttr = start.getAttributeByName(COUNT_QNAME);
                if (countAttr != null) {
                    count = Integer.parseInt(countAttr.getValue());
                }
                // null
                Object value;
                Attribute nullAttr = start.getAttributeByName(NULL_QNAME);
                if (nullAttr != null) {
                    if (nullAttr.getValue().equals("true") == false) {
                        throw new IllegalArgumentException("Unexpected value for null attribute");
                    }
                    advanceAndParseText();  // move to end tag and ignore any text
                    value = null;
                } else {
                    // type
                    Class<?> childType = parseTypeAttribute(start, iterable.valueType());
                    if (Bean.class.isAssignableFrom(childType)) {
                        MetaBean childMetaBean = JodaBeanUtils.metaBean(childType);
                        value = parseBean(childMetaBean, childType);
                    } else {
                        // metatype
                        Attribute metaTypeAttr = start.getAttributeByName(METATYPE_QNAME);
                        if (metaTypeAttr != null) {
                            SerIterable childIterable = SerIteratorFactory.INSTANCE.createIterable(metaTypeAttr.getValue());
                            if (childIterable == null) {
                                throw new IllegalArgumentException("Invalid metaType");
                            }
                            value = parseIterable(childIterable);
                        } else {
                            String text = advanceAndParseText();
                            value = settings.getConverter().convertFromString(childType, text);
                        }
                    }
                }
                iterable.add(key, value, count);
            }
            event = reader.nextEvent();
        }
        return iterable.build();
    }

    private Class<?> parseTypeAttribute(StartElement start, Class<?> defaultType) throws ClassNotFoundException {
        Attribute typeAttr = start.getAttributeByName(TYPE_QNAME);
        if (typeAttr == null) {
            return (defaultType == Object.class ? String.class : defaultType);
        }
        String childTypeStr = typeAttr.getValue();
        if (childTypeStr.length() > 0) {
            if (Character.isUpperCase(childTypeStr.charAt(0))) {
                childTypeStr = basePackage + "." + childTypeStr;
            }
            if (childTypeStr.startsWith(".")) {
                childTypeStr = basePackage + childTypeStr;
            }
        }
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(childTypeStr);
        } catch (ClassNotFoundException ex) {
            if (typeAttr.getValue().contains(".") == false) {
                childTypeStr = "java.lang." + typeAttr.getValue();
                try {
                    return Thread.currentThread().getContextClassLoader().loadClass(childTypeStr);
                } catch (ClassNotFoundException ex2) {
                    // ignore
                }
            }
            throw ex;
        }
    }

    // reader can be anywhere, but normally at StartDocument
    private StartElement advanceToStartElement() throws Exception {
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isStartElement()) {
                return event.asStartElement();
            }
        }
        throw new IllegalArgumentException("Unexpected end of document");
    }

    // reader must be at StartElement
    private String advanceAndParseText() throws Exception {
        StringBuilder buf = new StringBuilder();
        while (reader.hasNext()) {
            XMLEvent event = reader.nextEvent();
            if (event.isEndElement()) {
                return buf.toString();
            }
            if (event.isCharacters()) {
                buf.append(event.asCharacters().getData());
            }
        }
        throw new IllegalArgumentException("Unexpected end of document");
    }

}
