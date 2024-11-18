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

import java.io.IOException;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ser.JodaBeanSer;

/**
 * Provides the ability for a Joda-Bean to be written to a simple JSON format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The JSON format used here is natural, with no meta-data.
 * As such, it may not be possible to write some objects or read the JSON data back in.
 * <p>
 * Beans are output using JSON objects where the key is the property name.
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * Null values are generally omitted, but where included are sent as 'null'.
 * Boolean values are sent as 'true' and 'false'.
 * Numeric values are sent as JSON numbers.
 * Maps must have a key that can be converted to a string by Joda-Convert.
 * The property type needs to be known when writing/reading - properties, or
 * list/map entries, that are defined as {@code Object} are unlikely to work well.
 * <p>
 * Collections are output using JSON arrays. Maps as JSON objects.
 */
public class JodaBeanSimpleJsonWriter {

    /**
     * The settings to use.
     */
    private final JodaBeanSimpleJsonWalker walker;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanSimpleJsonWriter(JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.walker = new JodaBeanSimpleJsonWalker(settings);
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    public String write(Bean bean) {
        var buf = new StringBuilder(1024);
        try {
            write(bean, buf);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return buf.toString();
    }

    /**
     * Writes the bean to the {@code Appendable}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, Appendable output) throws IOException {
        JodaBeanUtils.notNull(bean, "bean");
        JodaBeanUtils.notNull(output, "output");
        walker.walk(bean, output);
    }

}
