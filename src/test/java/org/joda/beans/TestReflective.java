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
package org.joda.beans;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.sample.ReflectiveMutable;
import org.joda.beans.ser.JodaBeanSer;
import org.junit.Test;

/**
 * Test {@code ReflectiveMetaBean}.
 */
public class TestReflective {

    @Test
    public void test_mutable() {
        ReflectiveMutable bean = (ReflectiveMutable) ReflectiveMutable.META_BEAN.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", ReflectiveMutable.META_BEAN, String.class), "Smallville")
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreet(), "Park Lane");
        
        bean.setCity("Nodnol");
        assertEquals(bean.getCity(), "Nodnol");
        
        bean.property("city").set("Paris");
        assertEquals(bean.getCity(), "Paris");
        
        bean.metaBean().metaProperty("city").set(bean, "London");
        assertEquals(bean.getCity(), "London");
        
        assertEquals(bean.metaBean().beanType(), ReflectiveMutable.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 4);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), ReflectiveMutable.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("street");
        assertEquals(mp2.propertyType(), String.class);
        assertEquals(mp2.propertyGenericType(), String.class);
        assertEquals(mp2.declaringType(), ReflectiveMutable.class);
        assertEquals(mp2.get(bean), "Park Lane");
        assertEquals(mp2.style(), PropertyStyle.READ_WRITE);
        
        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<street>Park Lane<"));
    }

}
