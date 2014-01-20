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
package org.joda.beans.ser.xml;

import static org.joda.beans.ser.xml.JodaBeanXml.BEAN_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.COUNT_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ENTRY_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ITEM_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.KEY_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.METATYPE_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.NULL_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE;
import static org.joda.beans.ser.xml.JodaBeanXml.TYPE_QNAME;

import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
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
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * The reader.
     */
    private XMLEventReader reader;
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
    public JodaBeanXmlReader(final JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input string, not null
     * @return the bean, not null
     */
    public Bean read(final String input) {
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
    public <T> T read(final String input, Class<T> rootType) {
        return read(new StringReader(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(final InputStream input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input stream, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(final InputStream input, Class<T> rootType) {
        try {
            reader = factory().createXMLEventReader(input);
            return read(rootType);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(final Reader input) {
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
    public <T> T read(final Reader input, Class<T> rootType) {
        try {
            reader = factory().createXMLEventReader(input);
            return read(rootType);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Creates the factory.
     * <p>
     * Recreated each time to avoid JDK-8028111.
     * 
     * @return the factory, not null
     */
    private XMLInputFactory factory() {
        return XMLInputFactory.newFactory();
    }

    //-----------------------------------------------------------------------
    /**
     * Parses the root bean.
     * 
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws Exception if an error occurs
     */
    private <T> T read(final Class<T> rootType) throws Exception {
        StartElement start = advanceToStartElement();
        if (start.getName().equals(BEAN_QNAME) == false) {
            throw new IllegalArgumentException("Expected root element 'bean' but found '" + start.getName() + "'");
        }
        Attribute attr = start.getAttributeByName(TYPE_QNAME);
        if (attr == null && rootType == Bean.class) {
            throw new IllegalArgumentException("Root element attribute must specify '" + TYPE + "'");
        }
        Class<?> type;
        if (attr != null) {
            String typeStr = attr.getValue();
            type = settings.decodeClass(typeStr, null, knownTypes);
            if (rootType.isAssignableFrom(type) == false) {
                throw new IllegalArgumentException("Specified root type is incompatible with XML root type: " + rootType.getName() + " and " + type.getName());
            }
        } else {
            type = rootType;
        }
        if (Bean.class.isAssignableFrom(type) == false) {
            throw new IllegalArgumentException("Root type is not a Joda-Bean: " + type.getName());
        }
        MetaBean metaBean = JodaBeanUtils.metaBean(type);
        basePackage = type.getPackage().getName() + ".";
        Bean bean = parseBean(metaBean, type);
        reader.close();
        @SuppressWarnings("unchecked")
        T result = (T) bean;
        return result;
    }

    /**
     * Parses to a bean.
     * 
     * @param metaBean  the meta bean, not null
     * @param beanType  the bean type, not null
     * @return the bean, not null
     */
    private Bean parseBean(final MetaBean metaBean, final Class<?> beanType) throws Exception {
        try {
            BeanBuilder<? extends Bean> builder = metaBean.builder();
            XMLEvent event = nextEvent(">bean ");
            while (event.isEndElement() == false) {
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    String name = start.getName().getLocalPart();
                    MetaProperty<?> metaProp = metaBean.metaProperty(name);
                    Class<?> childType = parseTypeAttribute(start, metaProp.propertyType());
                    Object value;
                    if (Bean.class.isAssignableFrom(childType)) {
                        if (settings.getConverter().isConvertible(childType)) {
                            String text = advanceAndParseText();
                            value = settings.getConverter().convertFromString(childType, text);
                        } else {
                            MetaBean childMetaBean = JodaBeanUtils.metaBean(childType);
                            value = parseBean(childMetaBean, childType);
                        }
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
                event = nextEvent(".bean ");
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
    private Object parseIterable(final SerIterable iterable) throws Exception {
        XMLEvent event = nextEvent(">iter ");
        while (event.isEndElement() == false) {
            if (event.isStartElement()) {
                StartElement start = event.asStartElement();
                QName expectedType = iterable.isMapLike() ? ENTRY_QNAME : ITEM_QNAME;
                if (start.getName().equals(expectedType) == false) {
                    throw new IllegalArgumentException("Expected '" + expectedType.getLocalPart() + "' but found '" + start.getName() + "'");
                }
                // count
                int count = 1;
                Attribute countAttr = start.getAttributeByName(COUNT_QNAME);
                if (countAttr != null) {
                    count = Integer.parseInt(countAttr.getValue());
                }
                // key and value
                Object key = null;
                Object value = null;
                Attribute keyAttr = start.getAttributeByName(KEY_QNAME);
                if (keyAttr != null) {
                    // item is value with a key attribute
                    String keyStr = keyAttr.getValue();
                    if (iterable.keyType() != null) {
                        key = settings.getConverter().convertFromString(iterable.keyType(), keyStr);
                    } else {
                        key = keyStr;
                    }
                    value = parseValue(iterable, start);
                    
                } else if (iterable.keyType() != null) {
                    // two items nested in this entry
                    if (Bean.class.isAssignableFrom(iterable.keyType()) == false) {
                        throw new IllegalArgumentException("Unable to read map as declared key type is neither a bean nor a simple type: " + iterable.keyType().getName());
                    }
                    event = nextEvent(">>map ");
                    int loop = 0;
                    while (event.isEndElement() == false) {
                        if (event.isStartElement()) {
                            start = event.asStartElement();
                            if (start.getName().equals(ITEM_QNAME) == false) {
                                throw new IllegalArgumentException("Expected 'item' but found '" + start.getName() + "'");
                            }
                            if (key == null) {
                                key = parseKey(iterable, start);
                            } else {
                                value = parseValue(iterable, start);
                            }
                            loop++;
                        }
                        event = nextEvent("..map ");
                    }
                    if (loop != 2) {
                        throw new IllegalArgumentException("Expected 2 'item's but found " + loop);
                    }
                    
                } else {
                    // item is value
                    value = parseValue(iterable, start);
                }
                iterable.add(key, value, count);
            }
            event = nextEvent(".iter ");
        }
        return iterable.build();
    }

    private Object parseKey(final SerIterable iterable, StartElement start) throws Exception {
        // type
        Class<?> childType = parseTypeAttribute(start, iterable.keyType());
        if (Bean.class.isAssignableFrom(childType)) {
            MetaBean childMetaBean = JodaBeanUtils.metaBean(childType);
            return parseBean(childMetaBean, childType);
        } else {
            throw new IllegalArgumentException("Unable to read map as parsed key type is not a bean: " + childType.getName());
        }
    }

    private Object parseValue(final SerIterable iterable, StartElement start) throws Exception {
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
                if (settings.getConverter().isConvertible(childType)) {
                    String text = advanceAndParseText();
                    value = settings.getConverter().convertFromString(childType, text);
                } else {
                    MetaBean childMetaBean = JodaBeanUtils.metaBean(childType);
                    value = parseBean(childMetaBean, childType);
                }
            } else {
                // try deep generic parameters
                SerIterable childIterable = SerIteratorFactory.INSTANCE.createIterable(iterable);
                if (childIterable != null) {
                    value = parseIterable(childIterable);
                } else {
                    // metatype
                    Attribute metaTypeAttr = start.getAttributeByName(METATYPE_QNAME);
                    if (metaTypeAttr != null) {
                        childIterable = SerIteratorFactory.INSTANCE.createIterable(metaTypeAttr.getValue(), settings, knownTypes);
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
        }
        return value;
    }

    //-----------------------------------------------------------------------
    private Class<?> parseTypeAttribute(final StartElement start, final Class<?> defaultType) throws ClassNotFoundException {
        Attribute typeAttr = start.getAttributeByName(TYPE_QNAME);
        if (typeAttr == null) {
            return (defaultType == Object.class ? String.class : defaultType);
        }
        String childTypeStr = typeAttr.getValue();
        return settings.decodeClass(childTypeStr, basePackage, knownTypes);
    }

    // reader can be anywhere, but normally at StartDocument
    private StartElement advanceToStartElement() throws Exception {
        while (reader.hasNext()) {
            XMLEvent event = nextEvent("advnc ");
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
            XMLEvent event = nextEvent("text  ");
            if (event.isEndElement()) {
                return buf.toString();
            }
            if (event.isCharacters()) {
                buf.append(event.asCharacters().getData());
            }
        }
        throw new IllegalArgumentException("Unexpected end of document");
    }

    // provide for debugging
    private XMLEvent nextEvent(String location) throws Exception {
        XMLEvent event = reader.nextEvent();
//        System.out.println(location + event.toString().replace('\n', ' ') + " " + event.getClass().getSimpleName());
        return event;
    }

}
