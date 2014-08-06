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
package org.joda.beans.ser;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ser.bin.JodaBeanBinReader;
import org.joda.beans.ser.bin.JodaBeanBinWriter;
import org.joda.beans.ser.json.JodaBeanJsonReader;
import org.joda.beans.ser.json.JodaBeanJsonWriter;
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
    public static final JodaBeanSer COMPACT = new JodaBeanSer("", "", StringConvert.create(),
            SerIteratorFactory.INSTANCE, true, SerDeserializers.INSTANCE);
    /**
     * Obtains the singleton pretty-printing instance.
     */
    public static final JodaBeanSer PRETTY = new JodaBeanSer(" ", "\n", StringConvert.create(),
            SerIteratorFactory.INSTANCE, true, SerDeserializers.INSTANCE);

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
     * Whether to use short types.
     */
    private final boolean shortTypes;
    /**
     * The deserializers.
     */
    private final SerDeserializers deserializers;

    /**
     * Creates an instance.
     * 
     * @param indent  the indent, not null
     * @param newLine  the new line, not null
     * @param converter  the converter, not null
     * @param iteratorFactory  the iterator factory, not null
     * @param shortTypes  whether to use short types
     * @param deserializers  the deserializers to use, not null
     */
    private JodaBeanSer(String indent, String newLine, StringConvert converter,
                SerIteratorFactory iteratorFactory, boolean shortTypes, SerDeserializers deserializers) {
        this.indent = indent;
        this.newLine = newLine;
        this.converter = converter;
        this.iteratorFactory = iteratorFactory;
        this.shortTypes = shortTypes;
        this.deserializers = deserializers;
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
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
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
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
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
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
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
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
    }

    /**
     * Gets whether to use short types.
     * 
     * @return the short types flag, not null
     */
    public boolean isShortTypes() {
        return shortTypes;
    }

    /**
     * Returns a copy of this serializer with the short types flag set.
     * 
     * @param shortTypes  whether to use short types, not null
     * @return a copy of this object with the short types flag changed, not null
     */
    public JodaBeanSer withShortTypes(boolean shortTypes) {
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
    }

    /**
     * Gets the deserializers.
     * <p>
     * The default deserializers can be modified.
     * 
     * @return the converter, not null
     */
    public SerDeserializers getDeserializers() {
        return deserializers;
    }

    /**
     * Returns a copy of this serializer with the specified deserializers.
     * <p>
     * The default deserializers can be modified.
     * 
     * @param deserializers  the deserializers, not null
     * @return a copy of this object with the converter changed, not null
     */
    public JodaBeanSer withDeserializers(SerDeserializers deserializers) {
        JodaBeanUtils.notNull(deserializers, "deserializers");
        return new JodaBeanSer(indent, newLine, converter, iteratorFactory, shortTypes, deserializers);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a binary writer.
     * 
     * @return the binary writer, not null
     */
    public JodaBeanBinWriter binWriter() {
        return new JodaBeanBinWriter(this);
    }

    /**
     * Creates a binary reader.
     * 
     * @return the binary reader, not null
     */
    public JodaBeanBinReader binReader() {
        return new JodaBeanBinReader(this);
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

    //-----------------------------------------------------------------------
    /**
     * Creates a JSON writer.
     * 
     * @return the JSON writer, not null
     */
    public JodaBeanJsonWriter jsonWriter() {
        return new JodaBeanJsonWriter(this);
    }

    /**
     * Creates a JSON reader.
     * 
     * @return the JSON reader, not null
     */
    public JodaBeanJsonReader jsonReader() {
        return new JodaBeanJsonReader(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
