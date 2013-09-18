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

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
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
     * Creates an XML writer.
     * 
     * @param bean  the bean to write, not null
     * @return the XML writer, not null
     */
    public JodaBeanXmlWriter xmlWriter(Bean bean) {
        return new JodaBeanXmlWriter(this, bean);
    }

}
