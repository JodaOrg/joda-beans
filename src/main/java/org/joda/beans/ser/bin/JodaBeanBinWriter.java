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
import java.util.Objects;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to be written to a binary format.
 * <p>
 * This class is immutable and may be used from multiple threads.
 * 
 * <h3>Standard format</h3>
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
 * 
 * <h3>Referencing format</h3>
 * The referencing format is based on the standard format.
 * As a more complex format, it is intended to be consumed only by Joda-Beans
 * (whereas the standard format could be consumed by any consumer using MsgPack).
 * Thus this format is not fully documented and may change over time.
 * <p>
 * The referencing format only supports serialization of instances of {@code ImmutableBean}
 * and other basic types. If any mutable beans are encountered during traversal an exception will be thrown.
 * <p>
 * An initial pass of the bean is used to build up a map of unique immutable beans
 * and unique immutable instances of other classes (based on an equality check).
 * Then the class and property names for each bean class is serialized up front as a map of class name to list of
 * property names, along with class information for any class where type information would be required when parsing
 * and is not available on the metabean for the enclosing bean object.
 * <p>
 * Each unique immutable bean is output as a list of each property value using the fixed
 * property order previously serialized. Subsequent instances of unique objects (defined by an
 * equality check) are replaced by references to the first serialized instance.
 * <p>
 * The Java type names are sent using an 'ext' entity.
 * Five 'ext' types are used, one each for beans, meta-type and simple, reference keys and reference lookups.
 * The class name is passed as the 'ext' data.
 * The 'ext' value is sent as the first item in an array of property values for beans, an integer referring to the
 * location in the initial class mapping.
 * Where the additional type information is not about a bean, a tuple is written using a size 1 map where the key is
 * the 'ext' data and the value is the data being annotated.
 * <p>
 * For references, when an object will be referred back to it is written as a map of size one with 'ext' as the key
 * and the object that should be referred to as the value.
 * When that same object is referred back to it is written as 'ext' with the data from the initial 'ext'.
 */
public class JodaBeanBinWriter {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * Whether to use referencing.
     */
    private final boolean referencing;

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanBinWriter(JodaBeanSer settings) {
        this(settings, false);
    }

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param referencing  whether to use referencing
     */
    public JodaBeanBinWriter(JodaBeanSer settings, boolean referencing) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.referencing = referencing;
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
    public byte[] write(Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to an array of bytes.
     * 
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the binary data, not null
     */
    public byte[] write(Bean bean, boolean rootType) {
        var baos = new ByteArrayOutputStream(1024);
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
    public void write(Bean bean, OutputStream output) throws IOException {
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
    public void write(Bean bean, boolean rootType, OutputStream output) throws IOException {
        Objects.requireNonNull(bean, "bean must not be null");
        Objects.requireNonNull(output, "output must not be null");
        if (referencing) {
            if (!(bean instanceof ImmutableBean)) {
                throw new IllegalArgumentException(
                    "Referencing binary format can only write ImmutableBean instances: " + bean.getClass().getName());
            }
            new JodaBeanReferencingBinWriter(settings, output).write((ImmutableBean) bean);
        } else {
            new JodaBeanStandardBinWriter(settings, output).write(bean, rootType);
        }
    }

}
