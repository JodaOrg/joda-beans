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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.sample.ImmKey;
import org.joda.beans.sample.Pair;
import org.joda.beans.sample.Person;
import org.junit.Test;

/**
 * Test ser.
 */
public class TestSerDeserializerProvider {

    private static final SerDeserializer DESER = new SerDeserializer() {
        
        @Override
        public void setValue(BeanBuilder<?> builder, MetaProperty<?> metaProp, Object value) {
        }
        
        @Override
        public MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName) {
            return null;
        }
        
        @Override
        public MetaBean findMetaBean(Class<?> beanType) {
            return null;
        }
        
        @Override
        public BeanBuilder<?> createBuilder(Class<?> beanType, MetaBean metaBean) {
            return null;
        }
        
        @Override
        public Object build(Class<?> beanType, BeanBuilder<?> builder) {
            return null;
        }
    };
    
    private static final SerDeserializerProvider PROVIDER = new SerDeserializerProvider() {
        
        @Override
        public SerDeserializer findDeserializer(Class<?> beanType) {
            return DESER;
        }
    };

    @Test
    public void test_provider() {
        SerDeserializers deser = new SerDeserializers(PROVIDER);
        assertSame(deser.findDeserializer(Person.class), DESER);
    }

    @Test
    public void test_classpathImmKey() {
        assertTrue(SerDeserializers.INSTANCE.getDeserializers().containsKey(ImmKey.class));
        assertTrue(SerDeserializers.LENIENT.getDeserializers().containsKey(ImmKey.class));
        assertTrue(new SerDeserializers(PROVIDER).getDeserializers().containsKey(ImmKey.class));

        assertEquals(ImmKey.meta().name(), SerDeserializers.INSTANCE.findDeserializer(
                ImmKey.class).findMetaProperty(ImmKey.class, ImmKey.meta(), "key"));
        assertNull(SerDeserializers.LENIENT.findDeserializer(
                ImmKey.class).findMetaProperty(ImmKey.class, ImmKey.meta(), "wibble"));
    }

    @Test
    public void test_classpathPair() {
        assertTrue(SerDeserializers.INSTANCE.getDeserializers().containsKey(Pair.class));
        assertTrue(SerDeserializers.LENIENT.getDeserializers().containsKey(Pair.class));
        assertTrue(new SerDeserializers(PROVIDER).getDeserializers().containsKey(Pair.class));

        assertEquals(Pair.meta().first(), SerDeserializers.INSTANCE.findDeserializer(
                Pair.class).findMetaProperty(Pair.class, Pair.meta(), "left"));
        assertNull(SerDeserializers.LENIENT.findDeserializer(
                Pair.class).findMetaProperty(Pair.class, ImmKey.meta(), "wibble"));
    }

}
