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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to read from binary formats.
 * <p>
 * This class is immutable and may be used from multiple threads.
 * <p>
 * The binary formats are defined by {@link JodaBeanBinWriter}.
 */
public class JodaBeanBinReader extends MsgPack {

    /**
     * Settings.
     */
    private final JodaBeanSer settings;

    //-----------------------------------------------------------------------
    /**
     * Visualizes the binary data.
     * 
     * @param input  the input bytes, not null
     * @return the visualization
     */
    public static String visualize(byte[] input) {
        return new MsgPackVisualizer(input).visualizeData();
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanBinReader(JodaBeanSer settings) {
        if (settings == null) {
            throw new NullPointerException("settings");
        }
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input bytes, not null
     * @return the bean, not null
     */
    public Bean read(byte[] input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input bytes, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(byte[] input, Class<T> rootType) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        return read(new ByteArrayInputStream(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     */
    public Bean read(InputStream input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * <p>
     * Unusually for a method of this type, it closes the input stream.
     * 
     * @param <T>  the root type
     * @param input  the input stream, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(InputStream input, Class<T> rootType) {
        if (input == null) {
            throw new NullPointerException("input");
        }
        if (rootType == null) {
            throw new NullPointerException("rootType");
        }
        try {
            try (input) {
                var dataInput = input instanceof DataInputStream din ?
                        din :
                        new DataInputStream(input);
                return parseVersion(dataInput, rootType);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    //-----------------------------------------------------------------------
    // parses the version
    private <T> T parseVersion(DataInputStream input, Class<T> declaredType) throws Exception {
        // root array
        int arrayByte = input.readByte();
        int versionByte = input.readByte();
        switch (versionByte) {
            case 1:
                if (arrayByte != MIN_FIX_ARRAY + 2) {
                    throw new IllegalArgumentException(
                            "Invalid binary data: Expected array with 2 elements, but was: 0x" + toHex(arrayByte));
                }
                return new JodaBeanStandardBinReader(settings, input).read(declaredType);
            case 2:
                if (arrayByte != MIN_FIX_ARRAY + 4) {
                    throw new IllegalArgumentException(
                            "Invalid binary data: Expected array with 4 elements, but was: 0x" + toHex(arrayByte));
                }
                return new JodaBeanReferencingBinReader(settings, input).read(declaredType);
            default:
                throw new IllegalArgumentException(
                        "Invalid binary data: Expected version 1 or 2, but was: 0x" + toHex(versionByte));
        }
    }

}
