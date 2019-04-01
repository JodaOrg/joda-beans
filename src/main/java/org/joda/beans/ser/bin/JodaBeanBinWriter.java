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
package org.joda.beans.ser.bin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to be written to a binary format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The binary format is based on MessagePack v2.0.
 * Each bean is output as a map using the property name.
 * <p>
 * Most simple types, defined by Joda-Convert, are output as MessagePack strings.
 * However, MessagePack nil, boolean, float, integral and bin types are also used
 * for null, byte[] and the Java numeric primitive types (excluding char).
 * <p>
 * Beans are output using MessagePack maps where the key is the property name.
 * Collections are output using MessagePack maps or arrays.
 * Multisets are output as a map of value to count.
 * <p>
 * If a collection contains a collection then addition meta-type information is
 * written to aid with deserialization.
 * At this level, the data read back may not be identical to that written.
 * <p>
 * Where necessary, the Java type is sent using an 'ext' entity.
 * Three 'ext' types are used, one each for beans, meta-type and simple.
 * The class name is passed as the 'ext' data.
 * The 'ext' value is sent as an additional key-value pair for beans, with the
 * 'ext' as the key and 'nil' as the value. Where the additional type information
 * is not about a bean, a tuple is written using a size 1 map where the key is the
 * 'ext' data and the value is the data being annotated.
 * <p>
 * Type names are shortened by the package of the root type if possible.
 * Certain basic types are also handled, such as String, Integer, File and URI.
 */
public class JodaBeanBinWriter extends AbstractBinWriter {
    // this binary design is not the smallest possible
    // however, placing the 'ext' for the additional type info within
    // the bean data is much more friendly for dynamic languages using
    // a standalone MessagePack parser

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanBinWriter(final JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to an array of bytes.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @return the binary data, not null
     */
    public byte[] write(final Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to an array of bytes.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the binary data, not null
     */
    public byte[] write(final Bean bean, final boolean rootType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try {
            write(bean, rootType, baos);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return baos.toByteArray();
    }

    /**
     * Writes the bean to the {@code OutputStream}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @param output  the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(final Bean bean, OutputStream output) throws IOException {
        write(bean, true, output);
    }

    /**
     * Writes the bean to the {@code OutputStream}.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @param output  the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(final Bean bean, final boolean rootType, OutputStream output) throws IOException {
        if (bean == null) {
            throw new NullPointerException("bean");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.output = new MsgPackOutput(output);
        writeRoot(bean, rootType);
    }

    //-----------------------------------------------------------------------
    private void writeRoot(final Bean bean, final boolean rootType) throws IOException {
        output.writeArrayHeader(2);
        output.writeInt(1);  // version 1
        writeRootBean(bean, rootType);
    }
}
