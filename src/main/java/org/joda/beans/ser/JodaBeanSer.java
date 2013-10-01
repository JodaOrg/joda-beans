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
package org.joda.beans.ser;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ser.xml.JodaBeanXmlReader;
import org.joda.beans.ser.xml.JodaBeanXmlWriter;
import org.joda.convert.StringConvert;

/**
 * Provides the ability for a Joda-Bean to be serialized.
 * <p>
 * Serialization of Joda-Beans uses the information in the beans to optimize
 * the size of the data output.
 *
 * @author Stephen Colebourne
 */
public final class JodaBeanSer {

    /**
     * Obtains the singleton compact instance.
     */
    public static final JodaBeanSer COMPACT = new JodaBeanSer("", "", StringConvert.create(), SerIteratorFactory.INSTANCE);
    /**
     * Obtains the singleton pretty-printing instance.
     */
    public static final JodaBeanSer PRETTY = new JodaBeanSer(" ", "\n", StringConvert.create(), SerIteratorFactory.INSTANCE);
    /**
     * Known simple classes.
     */
    private static final Map<Class<?>, String> BASIC_TYPES;
    /**
     * Known simple classes.
     */
    private static final Map<String, Class<?>> BASIC_TYPES_REVERSED;
    static {
        Map<Class<?>, String> map = new HashMap<Class<?>, String>();
        map.put(String.class, "String");
        map.put(Boolean.class, "Boolean");
        map.put(Character.class, "Character");
        map.put(Byte.class, "Byte");
        map.put(Short.class, "Short");
        map.put(Integer.class, "Integer");
        map.put(Long.class, "Long");
        map.put(Float.class, "Float");
        map.put(Double.class, "Double");
        map.put(BigInteger.class, "BigInteger");
        map.put(BigDecimal.class, "BigDecimal");
        map.put(Class.class, "Class");
        map.put(Package.class, "Package");
        map.put(File.class, "File");
        map.put(Locale.class, "Locale");
        map.put(URL.class, "URL");
        map.put(URI.class, "URI");
        map.put(UUID.class, "UUID");
        Map<String, Class<?>> reversed = new HashMap<String, Class<?>>();
        for (Entry<Class<?>, String> entry : map.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
        BASIC_TYPES = Collections.unmodifiableMap(map);
        BASIC_TYPES_REVERSED = Collections.unmodifiableMap(reversed);
    }

    /**
     * The indent to use.
     */
    private final String indent;
    /**
     * The new line to use.
     */
    private final String newLine;
    /**
     * The string converter to use.
     */
    private final StringConvert converter;
    /**
     * The iterator factory to use.
     */
    private final SerIteratorFactory iteratorFactory;

    /**
     * Creates an instance.
     * 
     * @param indent  the indent, not null
     * @param newLine  the new line, not null
     */
    private JodaBeanSer(String indent, String newLine, StringConvert converter, SerIteratorFactory iteratorFactory) {
        this.indent = indent;
        this.newLine = newLine;
        this.converter = converter;
        this.iteratorFactory = iteratorFactory;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the pretty print indent.
     * 
     * @return the indent, not null
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Returns a copy of this serializer with the specified pretty print indent.
     * 
     * @param indent  the indent, not null
     * @return a copy of this object with the indent changed, not null
     */
    public JodaBeanSer withIndent(String indent) {
        JodaBeanUtils.notNull(indent, "indent");
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory);
    }

    /**
     * Gets the new line string.
     * 
     * @return the newLine, not null
     */
    public String getNewLine() {
        return newLine;
    }

    /**
     * Returns a copy of this serializer with the specified pretty print new line.
     * 
     * @param newLine  the new line, not null
     * @return a copy of this object with the new line changed, not null
     */
    public JodaBeanSer withNewLine(String newLine) {
        JodaBeanUtils.notNull(newLine, "newLine");
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory);
    }

    /**
     * Gets the string converter.
     * <p>
     * The default converter can be modified.
     * 
     * @return the converter, not null
     */
    public StringConvert getConverter() {
        return converter;
    }

    /**
     * Returns a copy of this serializer with the specified string converter.
     * <p>
     * The default converter can be modified.
     * 
     * @param converter  the converter, not null
     * @return a copy of this object with the converter changed, not null
     */
    public JodaBeanSer withConverter(StringConvert converter) {
        JodaBeanUtils.notNull(converter, "converter");
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory);
    }

    /**
     * Gets the iterator factory.
     * 
     * @return the iterator factory, not null
     */
    public SerIteratorFactory getIteratorFactory() {
        return iteratorFactory;
    }

    /**
     * Returns a copy of this serializer with the specified iterator factory.
     * 
     * @param iteratorFactory  the iterator factory, not null
     * @return a copy of this object with the iterator factory changed, not null
     */
    public JodaBeanSer withIteratorFactory(SerIteratorFactory iteratorFactory) {
        JodaBeanUtils.notNull(converter, "converter");
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory);
    }

    //-----------------------------------------------------------------------
    /**
     * Encodes a basic class.
     * <p>
     * This handles known simple types, like String, Integer or File, and prefixing.
     * It also allows a map of message specific shorter forms.
     * 
     * @param cls  the class to encode, not null
     * @param basePackage  the base package to use with trailing dot, null if none
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the class object, null if not a basic type
     */
    public String encodeClass(final Class<?> cls, final String basePackage, final Map<Class<?>, String> knownTypes) {
        String result = BASIC_TYPES.get(cls);
        if (result != null) {
            return result;
        }
        if (knownTypes != null) {
            result = knownTypes.get(cls);
            if (result != null) {
                return result;
            }
        }
        result = cls.getName();
        if (basePackage != null &&
                result.startsWith(basePackage) &&
                Character.isUpperCase(result.charAt(basePackage.length())) &&
                BASIC_TYPES.containsKey(result.substring(basePackage.length())) == false) {
            // use short format
            result = result.substring(basePackage.length());
            if (knownTypes != null) {
                knownTypes.put(cls, result);
            }
        } else {
            // use long format, short next time if possible
            if (knownTypes != null) {
                String simpleName = cls.getSimpleName();
                if (Character.isUpperCase(simpleName.charAt(0)) &&
                        BASIC_TYPES.containsKey(simpleName) == false &&
                        knownTypes.containsKey(simpleName) == false) {
                    knownTypes.put(cls, simpleName);
                } else {
                    knownTypes.put(cls, result);
                }
            }
        }
        return result;
    }

    /**
     * Decodes a class.
     * <p>
     * This uses the context class loader.
     * This handles known simple types, like String, Integer or File, and prefixing.
     * It also allows a map of message specific shorter forms.
     * 
     * @param className  the class name, not null
     * @param basePackage  the base package to use with trailing dot, null if none
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the class object, not null
     * @throws ClassNotFoundException if not found
     */
    public Class<?> decodeClass(final String className, final String basePackage, final Map<String, Class<?>> knownTypes) throws ClassNotFoundException {
        Class<?> result = BASIC_TYPES_REVERSED.get(className);
        if (result != null) {
            return result;
        }
        if (knownTypes != null) {
            result = knownTypes.get(className);
            if (result != null) {
                return result;
            }
        }
        String fullName = className;
        boolean expanded = false;
        if (basePackage != null && className.length() > 0 && Character.isUpperCase(className.charAt(0))) {
            fullName = basePackage + className;
            expanded = true;
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        try {
            result = loader != null ? loader.loadClass(fullName) : Class.forName(fullName);
            if (knownTypes != null) {
                knownTypes.put(fullName, result);
                if (expanded) {
                    knownTypes.put(className, result);
                } else {
                    String simpleName = result.getSimpleName();
                    if (Character.isUpperCase(simpleName.charAt(0)) &&
                            BASIC_TYPES.containsKey(simpleName) == false &&
                            knownTypes.containsKey(simpleName) == false) {
                        knownTypes.put(simpleName, result);
                    }
                }
            }
            return result;
        } catch (ClassNotFoundException ex) {
            // handle pathological case of package name starting with upper case
            if (fullName.equals(className) == false) {
                try {
                    result = loader != null ? loader.loadClass(className) : Class.forName(className);
                    if (knownTypes != null) {
                        knownTypes.put(className, result);
                    }
                    return result;
                } catch (ClassNotFoundException ignored) {
                }
            }
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an XML writer.
     * 
     * @return the XML writer, not null
     */
    public JodaBeanXmlWriter xmlWriter() {
        return new JodaBeanXmlWriter(this);
    }

    /**
     * Creates an XML reader.
     * 
     * @return the XML reader, not null
     */
    public JodaBeanXmlReader xmlReader() {
        return new JodaBeanXmlReader(this);
    }

}
