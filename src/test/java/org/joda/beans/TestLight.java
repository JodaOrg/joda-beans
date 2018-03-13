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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Currency;
import java.util.NoSuchElementException;

import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.impl.light.LightMetaBean;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.LightImmutable;
import org.joda.beans.sample.LightMutable;
import org.joda.beans.ser.JodaBeanSer;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Test style=light.
 */
public class TestLight {

    @Test
    public void test_immutable() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        LightImmutable bean = LightImmutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", LightImmutable.meta(), String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .set("hiddenText", "wow")
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getTown(), Optional.absent());
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreetName(), "Park Lane");
        assertEquals(bean.getOwner(), person);
        assertEquals(bean.getList(), ImmutableList.of());
        
        assertEquals(bean.metaBean().beanType(), LightImmutable.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 10);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("town"), true);
        assertEquals(bean.metaBean().metaPropertyExists("address"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);
        
        assertEquals(bean.metaBean().metaPropertyExists("place"), false);
        assertEquals(bean.metaBean().metaProperty("place"), bean.metaBean().metaProperty("city"));
        LightImmutable builtWithAlias = LightImmutable.meta().builder()
                .set("place", "Place")
                .set("street", "Park Lane")
                .set("owner", person)
                .build();
        assertEquals(builtWithAlias.getCity(), "Place");
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), LightImmutable.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.IMMUTABLE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("town");
        assertEquals(mp2.propertyType(), String.class);
        assertEquals(mp2.propertyGenericType(), String.class);
        assertEquals(mp2.declaringType(), LightImmutable.class);
        assertEquals(mp2.get(bean), null);
        assertEquals(mp2.style(), PropertyStyle.IMMUTABLE);
        
        MetaProperty<Object> mp3 = bean.metaBean().metaProperty("address");
        assertEquals(mp3.propertyType(), String.class);
        assertEquals(mp3.propertyGenericType(), String.class);
        assertEquals(mp3.declaringType(), LightImmutable.class);
        assertEquals(mp3.get(bean), "12 Park Lane Smallville");
        assertEquals(mp3.style(), PropertyStyle.DERIVED);
        
        MetaProperty<Object> mp4 = bean.metaBean().metaProperty("hiddenText");
        assertEquals(mp4.propertyType(), String.class);
        assertEquals(mp4.propertyGenericType(), String.class);
        assertEquals(mp4.declaringType(), LightImmutable.class);
        assertEquals(mp4.get(bean), "wow");
        assertEquals(mp4.style(), PropertyStyle.IMMUTABLE);
        
        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<currency>USD<"));
        assertFalse(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<town>"));
        
        try {
            LightImmutable.meta().builder().set(mp3, "Nothing");
            fail();
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    @Test
    public void test_immutable_order() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        LightImmutable bean = (LightImmutable) LightImmutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", LightImmutable.meta(), String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();

        ImmutableList<MetaProperty<?>> mps = ImmutableList.copyOf(bean.metaBean().metaPropertyIterable());
        assertEquals(mps.get(0).name(), "number");
        assertEquals(mps.get(1).name(), "flag");
        assertEquals(mps.get(2).name(), "street");
        assertEquals(mps.get(3).name(), "town");
        assertEquals(mps.get(4).name(), "city");
        assertEquals(mps.get(5).name(), "owner");
        assertEquals(mps.get(6).name(), "list");
        assertEquals(mps.get(7).name(), "currency");
        assertEquals(mps.get(8).name(), "hiddenText");
        assertEquals(mps.get(9).name(), "address");
    }

    @Test
    public void test_mutable() {
        LightMutable bean = LightMutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", LightMutable.meta(), String.class), "Smallville")
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .set("hiddenText", "wow")
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getTown(), Optional.absent());
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreetName(), "Park Lane");
        assertEquals(bean.getList(), ImmutableList.of());
        assertEquals(bean.getCurrency(), Optional.of(Currency.getInstance("USD")));
        
        bean.setCity("Nodnol");
        assertEquals(bean.getCity(), "Nodnol");
        
        bean.property("city").set("Paris");
        assertEquals(bean.getCity(), "Paris");
        
        bean.metaBean().metaProperty("city").set(bean, "London");
        assertEquals(bean.getCity(), "London");
        
        assertEquals(bean.metaBean().beanType(), LightMutable.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 9);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("town"), true);
        assertEquals(bean.metaBean().metaPropertyExists("address"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);
        
        assertEquals(bean.metaBean().metaPropertyExists("place"), false);
        assertEquals(bean.metaBean().metaProperty("place"), bean.metaBean().metaProperty("city"));
        LightMutable builtWithAlias = LightMutable.meta().builder()
                .set("place", "Place")
                .set("street", "Park Lane")
                .build();
        assertEquals(builtWithAlias.getCity(), "Place");
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), LightMutable.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("currency");
        assertEquals(mp2.propertyType(), Currency.class);
        assertEquals(mp2.propertyGenericType(), Currency.class);
        assertEquals(mp2.declaringType(), LightMutable.class);
        assertEquals(mp2.get(bean), Currency.getInstance("USD"));
        assertEquals(mp2.style(), PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp3 = bean.metaBean().metaProperty("address");
        assertEquals(mp3.propertyType(), String.class);
        assertEquals(mp3.propertyGenericType(), String.class);
        assertEquals(mp3.declaringType(), LightMutable.class);
        assertEquals(mp3.get(bean), "12 Park Lane London");
        assertEquals(mp3.style(), PropertyStyle.DERIVED);
        
        MetaProperty<Object> mp4 = bean.metaBean().metaProperty("hiddenText");
        assertEquals(mp4.propertyType(), String.class);
        assertEquals(mp4.propertyGenericType(), String.class);
        assertEquals(mp4.declaringType(), LightMutable.class);
        assertEquals(mp4.get(bean), "wow");
        assertEquals(mp4.style(), PropertyStyle.READ_WRITE);
        
        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<currency>USD<"));
        assertFalse(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<town>"));
        
        try {
            LightImmutable.meta().builder().set(mp3, "Nothing");
            fail();
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_immutableOld() {
        @SuppressWarnings("deprecation")
        LightMetaBean<LightImmutable> oldMeta = LightMetaBean.of(LightImmutable.class);

        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        LightImmutable bean = oldMeta.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", oldMeta, String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .set("hiddenText", "wow")
                .build();

        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getTown(), Optional.absent());
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreetName(), "Park Lane");
        assertEquals(bean.getOwner(), person);
        assertEquals(bean.getList(), ImmutableList.of());

        assertEquals(bean.metaBean().beanType(), LightImmutable.class);
        assertEquals(bean.metaBean().metaPropertyCount(), 10);
        assertEquals(bean.metaBean().metaPropertyExists("number"), true);
        assertEquals(bean.metaBean().metaPropertyExists("town"), true);
        assertEquals(bean.metaBean().metaPropertyExists("address"), true);
        assertEquals(bean.metaBean().metaPropertyExists("foobar"), false);

        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertEquals(mp.propertyType(), int.class);
        assertEquals(mp.declaringType(), LightImmutable.class);
        assertEquals(mp.get(bean), 12);
        assertEquals(mp.style(), PropertyStyle.IMMUTABLE);

        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("town");
        assertEquals(mp2.propertyType(), String.class);
        assertEquals(mp2.propertyGenericType(), String.class);
        assertEquals(mp2.declaringType(), LightImmutable.class);
        assertEquals(mp2.get(bean), null);
        assertEquals(mp2.style(), PropertyStyle.IMMUTABLE);

        MetaProperty<Object> mp3 = bean.metaBean().metaProperty("address");
        assertEquals(mp3.propertyType(), String.class);
        assertEquals(mp3.propertyGenericType(), String.class);
        assertEquals(mp3.declaringType(), LightImmutable.class);
        assertEquals(mp3.get(bean), "12 Park Lane Smallville");
        assertEquals(mp3.style(), PropertyStyle.DERIVED);

        MetaProperty<Object> mp4 = bean.metaBean().metaProperty("hiddenText");
        assertEquals(mp4.propertyType(), String.class);
        assertEquals(mp4.propertyGenericType(), String.class);
        assertEquals(mp4.declaringType(), LightImmutable.class);
        assertEquals(mp4.get(bean), "wow");
        assertEquals(mp4.style(), PropertyStyle.IMMUTABLE);

        assertTrue(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<currency>USD<"));
        assertFalse(JodaBeanSer.PRETTY.xmlWriter().write(bean).contains("<town>"));

        try {
            oldMeta.builder().set(mp3, "Nothing");
            fail();
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

}
