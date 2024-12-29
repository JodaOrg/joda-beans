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
import java.util.Collection;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.convert.ToStringConverter;

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
public class JodaBeanSimpleJsonWriter extends JodaBeanJsonWriter {

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    public JodaBeanSimpleJsonWriter(JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    @Override
    public String write(Bean bean) {
        return super.write(bean);
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
    @Override
    public void write(Bean bean, Appendable output) throws IOException {
        super.write(bean, false, output);
    }

    //-------------------------------------------------------------------------
    @Override
    void writeBeanType(ResolvedType declaredType, Bean bean, boolean includeRootType) throws IOException {
        // do not write type
    }

    //-------------------------------------------------------------------------
    @Override
    void writeLong(ResolvedType declaredType, Long val) throws IOException {
        // do not write type
        output.writeLong(val);
    }

    @Override
    void writeShort(ResolvedType declaredType, Short val) throws IOException {
        // do not write type
        output.writeInt(val);
    }

    @Override
    void writeByte(ResolvedType declaredType, Byte val) throws IOException {
        // do not write type
        output.writeInt(val);
    }

    @Override
    void writeDouble(ResolvedType declaredType, Double val) throws IOException {
        // do not write type
        if (Double.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeDouble(val);
        }
    }

    @Override
    void writeFloat(ResolvedType declaredType, Float val) throws IOException {
        // do not write type
        if (Float.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeFloat(val);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    void writeSimple(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        // do not write type
        super.writeJodaConvert(declaredType, propertyName, value);
    }

    // writes a map given map entries, used by Map/Multimap/BiMap
    @Override
    <K, V> void writeMapEntries(
            ResolvedType declaredType,
            String propertyName,
            Collection<Map.Entry<K, V>> mapEntries) throws IOException {

        // simple JSON requires the key to be a Joda-Convert type
        var keyType = toWeakenedType(declaredType.getArgumentOrDefault(0));
        var valueType = toWeakenedType(declaredType.getArgumentOrDefault(1));
        // converter based on the declared type if possible, else based on the runtime type
        var keyConverterOpt = settings.getConverter().converterFor(keyType.getRawType());
        ToStringConverter<Object> keyConverter = keyConverterOpt.isPresent() ?
                keyConverterOpt.get().withoutGenerics() :
                key -> settings.getConverter().convertToString(key);
        output.writeObjectStart();
        for (var entry : mapEntries) {
            var key = entry.getKey();
            if (key == null) {
                throw invalidNullMapKey(propertyName);
            }
            var str = keyConverter.convertToString(key);
            if (str == null) {
                throw invalidConvertedNullMapKey(propertyName);
            }
            output.writeObjectKey(str);
            writeObject(valueType, "", entry.getValue());
        }
        output.writeObjectEnd();
    }

    //-------------------------------------------------------------------------
    // writes content with a meta type
    @Override
    void writeWithMetaType(ContentHandler contentHandler, MetaTypeHandler metaTypeHandler) throws IOException {
        // do not write type
        contentHandler.handle();
    }

    @Override
    void writeWithMetaType(ContentHandler contentHandler, Class<?> cls, ResolvedType declaredType, String metaTypeName)
            throws IOException {

        // do not write type
        contentHandler.handle();
    }

}
