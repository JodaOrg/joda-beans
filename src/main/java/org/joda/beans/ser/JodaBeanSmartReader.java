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
package org.joda.beans.ser;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;

/**
 * Determines the correct file format and parses it appropriately.
 */
public class JodaBeanSmartReader {

    /**
     * The settings.
     */
    private final JodaBeanSer settings;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    JodaBeanSmartReader(JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the input is a serialized Joda-Bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * <p>
     * Callers may pass in part of the file, rather than the whole file.
     * Up to 128 bytes are needed to determine the format (XML requires the most, others far less).
     * 
     * @param input  the input bytes to check, which need only consist of the first 128 bytes of the file, not null
     * @return true if it is a known format
     */
    public boolean isKnownFormat(byte[] input) {
        return determineFormat(input) != JodaBeanSerFormat.UNKNOWN;
    }

    /**
     * Checks if the input is a serialized Joda-Bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * <p>
     * The input stream will be marked and reset, thus these operations must be supported.
     * As such, the same stream can then be for parsing.
     * 
     * @param input  the input stream to check, where only the first few bytes are read, not null
     * @return true if it is a known format
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if the input stream does not support mark/reset
     */
    public boolean isKnownFormat(InputStream input) {
        return determineFormat(input) != JodaBeanSerFormat.UNKNOWN;
    }

    //-----------------------------------------------------------------------
    // determines the format of a serialized Joda-Bean
    private JodaBeanSerFormat determineFormat(byte[] input) {
        if (input.length < 2) {
            return JodaBeanSerFormat.UNKNOWN;
        }
        // parse each known format. including possible UTF BOM prefix
        if (input.length >= 4 && input[0] == (byte) 0xef && input[1] == (byte) 0xbb && input[2] == (byte) 0xbf) {
            if (input[3] == '<' && isXml(input, 3)) {
                return JodaBeanSerFormat.XML_UTF8;
            } else if (input[3] == '{' && isJson(input, 3)) {
                return JodaBeanSerFormat.JSON_UTF8;
            } else {
                return JodaBeanSerFormat.UNKNOWN;
            }
        } else if (input[0] == '<' && isXml(input, 0)) {
            return JodaBeanSerFormat.XML;
        } else if (input[0] == '{' && isJson(input, 0)) {
            return JodaBeanSerFormat.JSON;
        } else if (input[0] == (byte) 0x94 && input[1] == (byte) 0x02) {
            return JodaBeanSerFormat.BIN;
        } else if (input[0] == (byte) 0x92 && input[1] == (byte) 0x01) {
            return JodaBeanSerFormat.BIN;
        } else {
            return JodaBeanSerFormat.UNKNOWN;
        }
    }

    private boolean isXml(byte[] bytes, int pos) {
        String str = new String(bytes, pos, bytes.length - pos, StandardCharsets.UTF_8);
        return str.contains("<bean ") || str.contains("<bean>");
    }

    private boolean isJson(byte[] bytes, int pos) {
        for (int i = pos + 1; i < bytes.length; i++) {
            byte b = bytes[i];
            if (b == '}' || b == '"') {
                return true;
            } else if (!(b == ' ' || b == '\t' || b == '\r' || b == '\n')) {
                return false;
            }
        }
        return false;
    }

    // determines the format of a serialized Joda-Bean
    private JodaBeanSerFormat determineFormat(InputStream input) {
        if (!input.markSupported()) {
            throw new IllegalArgumentException("Input stream does not support mark/reset");
        }
        byte[] buf = new byte[128];
        try {
            input.mark(128);
            int readCount = read(input, buf, 128);
            buf = Arrays.copyOf(buf, readCount);
            input.reset();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return determineFormat(buf);
    }

    // fully reads the stream
    private static int read(InputStream in, byte[] buf, int len) throws IOException {
        int pos = 0;
        while (pos < len) {
            int result = in.read(buf, pos, len - pos);
            if (result == -1) {
                break;
            }
            pos += result;
        }
        return pos;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * 
     * @param input  the input bytes to parse, not null
     * @return the bean, not null
     * @throws IllegalArgumentException if the file format is not recognized
     * @throws RuntimeException if unable to parse
     */
    public Bean read(byte[] input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * 
     * @param <T>  the root type
     * @param input  the input bytes to parse, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws IllegalArgumentException if the file format is not recognized
     * @throws RuntimeException if unable to parse
     */
    public <T> T read(byte[] input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        return read(new ByteArrayInputStream(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * 
     * @param input  the input stream, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if the file format is not recognized
     * @throws RuntimeException if unable to parse
     */
    public Bean read(InputStream input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * <p>
     * XML and JSON files may be prefixed by the UTF-8 Unicode BOM.
     * 
     * @param <T>  the root type
     * @param input  the input stream, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if the file format is not recognized
     * @throws RuntimeException if unable to parse
     */
    public <T> T read(InputStream input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        JodaBeanUtils.notNull(rootType, "rootType");
        BufferedInputStream buffered = buffer(input);
        JodaBeanSerFormat format = determineFormat(buffered);
        return format.read(buffered, rootType, settings);
    }

    // buffer the input stream
    private BufferedInputStream buffer(InputStream input) {
        if (input.getClass() == BufferedInputStream.class) {
            return (BufferedInputStream) input;
        }
        return new BufferedInputStream(input);
    }

}
