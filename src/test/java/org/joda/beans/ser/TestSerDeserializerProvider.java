/*
 *  Copyright 2001-2016 Stephen Colebourne
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

import static org.testng.Assert.assertSame;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.Person;
import org.testng.annotations.Test;

/**
 * Test ser.
 */
@Test
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

    public void test_provider() {
        SerDeserializers deser = new SerDeserializers(PROVIDER);
        assertSame(deser.findDeserializer(Person.class), DESER);
    }

}
