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
import java.util.Collections;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterable;
import org.joda.beans.ser.SerIteratorFactory;

/**
 * Provides the ability for a Joda-Bean to read from JSON.
 * <p>
 * The JSON format is defined by {@link JodaBeanJsonWriter}.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
public class JodaBeanSimpleJsonReader extends AbstractJsonReader {

    /**
     * Creates an instance.
     * 
     * @param settings  the settings, not null
     */
    public JodaBeanSimpleJsonReader(JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------
    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input string, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(String input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        return read(new StringReader(input), rootType);
    }

    /**
     * Reads and parses to a bean.
     * 
     * @param <T>  the root type
     * @param input  the input reader, not null
     * @param rootType  the root type, not null
     * @return the bean, not null
     */
    public <T> T read(Reader input, Class<T> rootType) {
        JodaBeanUtils.notNull(input, "input");
        JodaBeanUtils.notNull(rootType, "rootType");
        try {
            JsonInput jsonInput = new JsonInput(input);
            return parseRoot(jsonInput, rootType);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    //-----------------------------------------------------------------------
    @Override
    SerIterable parseUnknownArray(Class<?> declaredType) {
        if (declaredType.isArray()) {
            return SerIteratorFactory.array(declaredType.getComponentType());
        } else {
            return SerIteratorFactory.list(Object.class, Collections.<Class<?>>emptyList());
        }
    }

    @Override
    SerIterable parseUnknownObject(Class<?> declaredType) {
        return SerIteratorFactory.map(String.class, Object.class, Collections.<Class<?>>emptyList());
    }

}
