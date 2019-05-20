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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * The Joda-Beans serialization format.
 */
enum JodaBeanSerFormat {

    /**
     * The binary format.
     */
    BIN ,
    /**
     * The JSON format.
     */
    JSON,
    /**
     * The JSON format.
     */
    JSON_UTF8,
    /**
     * The XML format.
     */
    XML,
    /**
     * The XML format.
     */
    XML_UTF8,
    /**
     * The format is unknown.
     */
    UNKNOWN;

    // creates the reader, handling any UTF BOM
    <T> T read(InputStream stream, Class<T> rootType, JodaBeanSer settings) {
        // javac generics fails when this code is moved to enum subclasses
        switch (this) {
            case BIN: {
                return settings.binReader().read(stream, rootType);
            }
            case JSON: {
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                return rootType.cast(settings.simpleJsonReader().read(reader, rootType));
            }
            case JSON_UTF8: {
                read(stream, 3);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                return rootType.cast(settings.simpleJsonReader().read(reader, rootType));
            }
            case XML: {
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                return rootType.cast(settings.xmlReader().read(reader, rootType));
            }
            case XML_UTF8: {
                read(stream, 3);
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                return rootType.cast(settings.xmlReader().read(reader, rootType));
            }
            case UNKNOWN:
            default:
                throw new IllegalArgumentException("File is not a recognised Joda-Beans format");
        }
    }

    // read a fixed number of bytes from the input stream
    private static void read(InputStream buffered, int count) {
        try {
            for (int i = 0; i < count; i++) {
                buffered.read();
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

}
