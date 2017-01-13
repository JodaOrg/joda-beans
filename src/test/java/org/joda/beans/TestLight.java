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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Currency;

import org.joda.beans.gen.ImmPerson;
import org.joda.beans.gen.Light;
import org.joda.beans.gen.MutableLight;
import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.testng.annotations.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Test style=light.
 */
@Test
public class TestLight {

    public void test_immutable() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        Light bean = (Light) Light.meta().builder()
                .setString("number", "12")
                .setString("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", Light.meta(), String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getTown(), Optional.absent());
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreetName(), "Park Lane");
        assertEquals(bean.getOwner(), person);
        assertEquals(bean.getList(), ImmutableList.of());
        
        assertEquals(bean.metaBean().beanType(), Light.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 8);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("town"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), Light.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.IMMUTABLE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("town");
        assertEquals(mp2.propertyType(), Optional.class);
        assertEquals(mp2.declaringType(), Light.class);
        assertEquals(mp2.get(bean), Optional.absent());
        assertEquals(mp2.style(), PropertyStyle.IMMUTABLE);
        
        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<currency>USD<"));
        assertFalse(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<town>"));
    }

    public void test_mutable() {
        MutableLight bean = (MutableLight) MutableLight.meta().builder()
                .setString("number", "12")
                .setString("text", "Park Lane")
                .set(StandaloneMetaProperty.of("city", MutableLight.meta(), String.class), "London")
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getText(), "Park Lane");
        assertEquals(bean.getList(), ImmutableList.of());
        assertEquals(bean.getCurrency(), Optional.of(Currency.getInstance("USD")));
        
        bean.setCity("Nodnol");
        assertEquals(bean.getCity(), "Nodnol");
        
        bean.property("city").set("Paris");;
        assertEquals(bean.getCity(), "Paris");
        
        bean.metaBean().metaProperty("city").set(bean, "London");;
        assertEquals(bean.getCity(), "London");
        
        assertEquals(bean.metaBean().beanType(), MutableLight.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 6);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("text"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), MutableLight.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("currency");
        assertEquals(mp2.propertyType(), Optional.class);
        assertEquals(mp2.declaringType(), MutableLight.class);
        assertEquals(mp2.get(bean), Optional.of(Currency.getInstance("USD")));
        assertEquals(mp2.style(), PropertyStyle.READ_WRITE);
        
        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<currency>USD<"));
        assertFalse(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<town>"));
    }

}
