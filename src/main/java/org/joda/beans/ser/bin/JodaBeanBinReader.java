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

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Provides the ability for a Joda-Bean to read from a binary format.
 * <p>
 * The binary format is defined by {@link JodaBeanBinWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanBinReader extends AbstractBinReader {

    /**
     * Visualizes the binary data, writing to system out.
     *
     * @param input the input bytes, not null
     * @return the visualization
     */
    public static String visualize(byte[] input) {
        return new MsgPackVisualizer(input).visualizeData();
    }

    //-----------------------------------------------------------------------

    /**
     * Creates an instance.
     *
     * @param settings the settings, not null
     */
    public JodaBeanBinReader(final JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------

    /**
     * Reads and parses to a bean.
     *
     * @param input the input bytes, not null
     * @return the bean, not null
     */
    public Bean read(final byte[] input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     *
     * @param <T> the root type
     * @param input the input bytes, not null
     * @param rootType the root type, not null
     * @return the bean, not null
     */
    public <T> T read(final byte[] input, Class<T> rootType) {
        return read(new ByteArrayInputStream(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     *
     * @param input the input reader, not null
     * @return the bean, not null
     */
    public Bean read(final InputStream input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     *
     * @param <T> the root type
     * @param input the input stream, not null
     * @param rootType the root type, not null
     * @return the bean, not null
     */
    public <T> T read(final InputStream input, Class<T> rootType) {
        if (input instanceof DataInputStream) {
            this.input = (DataInputStream) input;
        } else {
            this.input = new DataInputStream(input);
        }
        try {
            try {
                return parseRoot(rootType);
            } finally {
                input.close();
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}