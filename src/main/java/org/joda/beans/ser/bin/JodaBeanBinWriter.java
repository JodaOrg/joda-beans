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

import static org.joda.beans.ser.bin.JodaBeanBinFormat.REFERENCING;
import static org.joda.beans.ser.bin.JodaBeanBinFormat.STANDARD;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to be written to a binary format.
 * <p>
 * This class is immutable and may be used from multiple threads.
 * <p>
 * See {@link JodaBeanBinFormat} for details on each file format.
 */
public class JodaBeanBinWriter {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;
    /**
     * The format.
     */
    private final JodaBeanBinFormat format;

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanBinWriter(JodaBeanSer settings) {
        this(settings, STANDARD);
    }

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param referencing  whether to use referencing
     * @deprecated Use the format instead of the boolean flag
     */
    @Deprecated
    public JodaBeanBinWriter(JodaBeanSer settings, boolean referencing) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.format = referencing ? REFERENCING : STANDARD;
    }

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param format  the format, not null
     * @since 3.0.0
     */
    public JodaBeanBinWriter(JodaBeanSer settings, JodaBeanBinFormat format) {
        this.settings = Objects.requireNonNull(settings, "settings must not be null");
        this.format = Objects.requireNonNull(format, "format must not be null");
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
     * @param includeRootType  true to output the root type
     * @param output  the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, boolean includeRootType, OutputStream output) throws IOException {
        Objects.requireNonNull(bean, "bean must not be null");
        Objects.requireNonNull(output, "output must not be null");
        switch (format) {
            case STANDARD -> new JodaBeanStandardBinWriter(settings, output).write(bean, includeRootType);
            case REFERENCING -> new JodaBeanReferencingBinWriter(settings, output).write(bean);
            case PACKED -> new JodaBeanPackedBinWriter(settings, output).write(bean, includeRootType);
            default -> throw new IllegalArgumentException("Invalid bin format, must be Standard, Referencing or Packed");
        }
    }

}
