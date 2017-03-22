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
package org.joda.beans.integrate.kryo;

import org.joda.beans.Bean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.bin.JodaBeanBinWriter;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.factories.ReflectionSerializerFactory;
import com.esotericsoftware.kryo.factories.SerializerFactory;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;

/**
 * Integration between Joda-Beans and Kryo serialization.
 * <p>
 * Once setup, this causes Kryo to serialize any Joda-Bean using {@link JodaBeanBinWriter}.
 * (Kryo cannot handle immutable Joda-Beans by default as they do not have a no-args constructor)
 * <p>
 * The following will register this class:
 * <pre>
 *  // create kryo
 *  Kryo kryo = ...
 *  // register Joda-Beans serializer as the default
 *  kryo.setDefaultSerializer(new KryoJodaBeanSerializer());
 *  // use kryo
 * </pre>
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class KryoJodaBeanSerializer implements SerializerFactory {

    /**
     * The serializer factory to use if the object is not a bean.
     */
    private final SerializerFactory defaultSerializerFactory;

    //-------------------------------------------------------------------------
    /**
     * Creates an instance using the default {@code FieldSerializer}.
     */
    public KryoJodaBeanSerializer() {
        this(FieldSerializer.class);
    }

    /**
     * Creates an instance using {@code ReflectionSerializerFactory}.
     * 
     * @param defaultSerializerType  the default serializer type
     */
    public KryoJodaBeanSerializer(Class<? extends Serializer> defaultSerializerType) {
        this.defaultSerializerFactory = new ReflectionSerializerFactory(
                defaultSerializerType);
    }

    /**
     * Creates an instance.
     * 
     * @param defaultSerializerFactory  the default serializer factory
     */
    public KryoJodaBeanSerializer(SerializerFactory defaultSerializerFactory) {
        this.defaultSerializerFactory = defaultSerializerFactory;
    }

    //-------------------------------------------------------------------------
    @Override
    public Serializer makeSerializer(Kryo kryo, Class<?> type) {
        return new JodaBeanSerializer<>(defaultSerializerFactory.makeSerializer(kryo, type));
    }

    //-------------------------------------------------------------------------
    final class JodaBeanSerializer<T> extends Serializer<T> {

        // the default serializer
        private final Serializer<T> defaultSerializer;

        /**
         * Creates an instance.
         * 
         * @param defaultSerializer  the default serializer
         */
        JodaBeanSerializer(Serializer<T> defaultSerializer) {
            this.defaultSerializer = defaultSerializer;
        }

        //-------------------------------------------------------------------------
        @Override
        public void write(Kryo kryo, Output output, T object) {
            if (object instanceof Bean) {
                byte[] bytes = JodaBeanSer.COMPACT.binWriter()
                        .write((Bean) object, false);
                output.writeVarInt(bytes.length, true);
                output.write(bytes);
            } else {
                defaultSerializer.write(kryo, output, object);
            }
        }

        @Override
        public T read(Kryo kryo, Input input, Class<T> type) {
            if (Bean.class.isAssignableFrom(type)) {
                int len = input.readVarInt(true);
                byte[] bytes = input.readBytes(len);
                return JodaBeanSer.COMPACT.binReader().read(bytes, type);
            } else {
                return defaultSerializer.read(kryo, input, type);
            }
        }
    }

}
