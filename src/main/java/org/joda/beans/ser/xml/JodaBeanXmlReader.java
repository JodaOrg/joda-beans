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

import static org.joda.beans.ser.xml.JodaBeanXml.BEAN_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.COLS_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.COL_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.COUNT_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ENTRY_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ITEM_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.KEY_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.METATYPE_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.NULL_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ROWS_QNAME;
import static org.joda.beans.ser.xml.JodaBeanXml.ROW_QNAME;
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
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerDeserializer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to read from XML.
 * <p>
 * The XML format is defined by {@link JodaBeanXmlWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
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
    private Map<String, Class<?>> knownTypes = new HashMap<>();

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
            try {
                reader = factory().createXMLEventReader(input);
                return read(rootType);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
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
            try {
                reader = factory().createXMLEventReader(input);
                return read(rootType);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
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
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory;
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
        Class<?> effectiveType = rootType;
        if (attr != null) {
            String typeStr = attr.getValue();
            effectiveType = SerTypeMapper.decodeType(typeStr, settings, null, knownTypes);
            if (rootType.isAssignableFrom(effectiveType) == false) {
                throw new IllegalArgumentException("Specified root type is incompatible with XML root type: " + rootType.getName() + " and " + effectiveType.getName());
            }
        }
        if (Bean.class.isAssignableFrom(effectiveType) == false) {
            throw new IllegalArgumentException("Root type is not a Joda-Bean: " + effectiveType.getName());
        }
        basePackage = effectiveType.getPackage().getName() + ".";
        Object parsed = parseBean(effectiveType);
        return rootType.cast(parsed);
    }

    /**
     * Parses a logical bean in the input XML.
     * <p>
     * Return type allows for a non-bean to be returned.
     * 
     * @param beanType  the bean type, not null
     * @return the bean, not null
     */
    @SuppressWarnings("null")
    private Object parseBean(final Class<?> beanType) throws Exception {
        String propName = "";
        try {
            XMLEvent event = null;
            // handle case where whole bean is Joda-Convert string
            if (settings.getConverter().isConvertible(beanType)) {
                StringBuilder buf = new StringBuilder();
                while (reader.hasNext()) {
                    event = nextEvent(">btxt ");
                    if (event.isCharacters()) {
                        buf.append(event.asCharacters().getData());
                    } else if (event.isEndElement()) {
                        return settings.getConverter().convertFromString(beanType, buf.toString());
                    } else if (event.isStartElement()) {
                        break;  // not serialized via Joda-Convert
                    } else if (event.isEndDocument()) {
                        throw new IllegalArgumentException("Unexpected end of document");
                    }
                }
            } else {
                event = nextEvent(">bean ");
            }
            // handle structured bean
            SerDeserializer deser = settings.getDeserializers().findDeserializer(beanType);
            MetaBean metaBean = deser.findMetaBean(beanType);
            BeanBuilder<?> builder = deser.createBuilder(beanType, metaBean);
            // handle beans with structure
            while (event.isEndElement() == false) {
                if (event.isStartElement()) {
                    StartElement start = event.asStartElement();
                    propName = start.getName().getLocalPart();
                    MetaProperty<?> metaProp = deser.findMetaProperty(beanType, metaBean, propName);
                    if (metaProp == null || metaProp.style().isDerived()) {
                        int depth = 0;
                        event = nextEvent(" skip ");
                        while (event.isEndElement() == false || depth > 0) {
                            if (event.isStartElement()) {
                                depth++;
                            } else if (event.isEndElement()) {
                                depth--;
                            }
                            event = nextEvent(" skip ");
                        }
                        // skip elements
                    } else {
                        Class<?> childType = parseTypeAttribute(start, SerOptional.extractType(metaProp, beanType));
                        Object value;
                        if (Bean.class.isAssignableFrom(childType)) {
                            value = parseBean(childType);
                        } else {
                            SerIterable iterable = settings.getIteratorFactory().createIterable(metaProp, beanType);
                            if (iterable != null) {
                                value = parseIterable(start, iterable);
                            } else {
                                // metatype
                                Attribute metaTypeAttr = start.getAttributeByName(METATYPE_QNAME);
                                if (metaTypeAttr != null) {
                                    iterable = settings.getIteratorFactory().createIterable(metaTypeAttr.getValue(), settings, knownTypes);
                                    if (iterable == null) {
                                        throw new IllegalArgumentException("Invalid metaType");
                                    }
                                    value = parseIterable(start, iterable);
                                } else {
                                    String text = advanceAndParseText();
                                    value = settings.getConverter().convertFromString(childType, text);
                                }
                            }
                        }
                        deser.setValue(builder, metaProp, SerOptional.wrapValue(metaProp, beanType, value));
                    }
                    propName = "";
                }
                event = nextEvent(".bean ");
            }
            return deser.build(beanType, builder);
        } catch (Exception ex) {
            throw new RuntimeException("Error parsing bean: " + beanType.getName() + "::" + propName + ", " + ex.getMessage(), ex);
        }
    }

    /**
     * Parses to a collection wrapper.
     * 
     * @param iterable  the iterable builder, not null
     * @return the iterable, not null
     */
    private Object parseIterable(final StartElement iterableEvent, final SerIterable iterable) throws Exception {
        Attribute rowsAttr = iterableEvent.getAttributeByName(ROWS_QNAME);
        Attribute columnsAttr = iterableEvent.getAttributeByName(COLS_QNAME);
        if (rowsAttr != null && columnsAttr != null) {
            iterable.dimensions(new int[] {Integer.parseInt(rowsAttr.getValue()), Integer.parseInt(columnsAttr.getValue())});
        }
        XMLEvent event = nextEvent(">iter ");
        while (event.isEndElement() == false) {
            if (event.isStartElement()) {
                StartElement start = event.asStartElement();
                QName expectedType = iterable.category() == SerCategory.MAP ? ENTRY_QNAME : ITEM_QNAME;
                if (start.getName().equals(expectedType) == false) {
                    throw new IllegalArgumentException("Expected '" + expectedType.getLocalPart() + "' but found '" + start.getName() + "'");
                }
                int count = 1;
                Object key = null;
                Object column = null;
                Object value = null;
                if (iterable.category() == SerCategory.COUNTED) {
                    Attribute countAttr = start.getAttributeByName(COUNT_QNAME);
                    if (countAttr != null) {
                        count = Integer.parseInt(countAttr.getValue());
                    }
                    value = parseValue(iterable, start);
                    
                } else if (iterable.category() == SerCategory.TABLE || iterable.category() == SerCategory.GRID) {
                    Attribute rowAttr = start.getAttributeByName(ROW_QNAME);
                    Attribute colAttr = start.getAttributeByName(COL_QNAME);
                    if (rowAttr == null || colAttr == null) {
                        throw new IllegalArgumentException("Unable to read table as row/col attribute missing");
                    }
                    String rowStr = rowAttr.getValue();
                    if (iterable.keyType() != null) {
                        key = settings.getConverter().convertFromString(iterable.keyType(), rowStr);
                    } else {
                        key = rowStr;
                    }
                    String colStr = colAttr.getValue();
                    if (iterable.columnType() != null) {
                        column = settings.getConverter().convertFromString(iterable.columnType(), colStr);
                    } else {
                        column = colStr;
                    }
                    value = parseValue(iterable, start);
                    
                } else if (iterable.category() == SerCategory.MAP) {
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
                        
                    } else {
                        // two items nested in this entry
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
                    }                    
                    
                } else {  // COLLECTION
                    value = parseValue(iterable, start);
                }
                iterable.add(key, column, value, count);
            }
            event = nextEvent(".iter ");
        }
        return iterable.build();
    }

    private Object parseKey(final SerIterable iterable, StartElement start) throws Exception {
        // type
        Class<?> childType = parseTypeAttribute(start, iterable.keyType());
        if (Bean.class.isAssignableFrom(childType) || settings.getConverter().isConvertible(childType)) {
            return parseBean(childType);
        } else if (childType.isAssignableFrom(String.class)) {
            return parseBean(String.class);
        } else {
            throw new IllegalArgumentException("Unable to read map as parsed key type is neither a bean nor a simple type: " + childType.getName());
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
                value = parseBean(childType);
            } else {
                // try deep generic parameters
                SerIterable childIterable = settings.getIteratorFactory().createIterable(iterable);
                if (childIterable != null) {
                    value = parseIterable(start, childIterable);
                } else {
                    // metatype
                    Attribute metaTypeAttr = start.getAttributeByName(METATYPE_QNAME);
                    if (metaTypeAttr != null) {
                        childIterable = settings.getIteratorFactory().createIterable(metaTypeAttr.getValue(), settings, knownTypes);
                        if (childIterable == null) {
                            throw new IllegalArgumentException("Invalid metaType");
                        }
                        value = parseIterable(start, childIterable);
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
    private Class<?> parseTypeAttribute(StartElement start, Class<?> defaultType) throws ClassNotFoundException {
        Attribute typeAttr = start.getAttributeByName(TYPE_QNAME);
        if (typeAttr == null) {
            return (defaultType == Object.class ? String.class : defaultType);
        }
        String typeStr = typeAttr.getValue();
        return settings.getDeserializers().decodeType(typeStr, settings, basePackage, knownTypes, defaultType);
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
            if (event.isCharacters()) {
                buf.append(event.asCharacters().getData());
            } else if (event.isEndElement()) {
                return buf.toString();
            } else if (event.isStartElement()) {
                throw new IllegalArgumentException("Unexpected start tag");
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
