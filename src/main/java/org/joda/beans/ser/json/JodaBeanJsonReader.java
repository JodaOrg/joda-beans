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
package org.joda.beans.ser.json;

import java.io.Reader;
import java.io.StringReader;
import java.io.UncheckedIOException;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to read from JSON.
 * <p>
 * The JSON format is defined by {@link JodaBeanJsonWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
public class JodaBeanJsonReader extends AbstractJsonReader {

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanJsonReader(JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input string, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if unable to parse the JSON
     */
    public Bean read(String input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input string, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if unable to parse the JSON
     */
    public <T> T read(String input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        return read(new StringReader(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param input  the input reader, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if unable to parse the JSON
     */
    public Bean read(Reader input) {
        return read(input, Bean.class);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input reader, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     * @throws UncheckedIOException if unable to read the stream
     * @throws IllegalArgumentException if unable to parse the JSON
     */
    public <T> T read(Reader input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        JodaBeanUtils.notNull(rootType, "rootType");
        var jsonInput = new JsonInput(input);
        return parseRoot(jsonInput, rootType);
    }

}
