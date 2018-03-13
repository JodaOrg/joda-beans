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
import static org.junit.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.sample.Address;
import org.junit.Test;

/**
 * Test property using Person.
 */
public class TestAddress {

    private static final int NUM_PROPERTIES = 4;
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String NUMBER = "number";

    @Test
    public void test_bean() {
        Bean test = new Address();
        
        assertEquals(test instanceof Address, true);
        
        assertEquals(test.metaBean(), Address.meta());
        
        assertEquals(test.propertyNames().contains(STREET), true);
        assertEquals(test.propertyNames().contains(CITY), true);
        assertEquals(test.propertyNames().contains(NUMBER), true);
        assertEquals(test.propertyNames().contains("Rubbish"), false);
        
        assertEquals(test.property(STREET).name(), STREET);
        assertEquals(test.property(CITY).name(), CITY);
        assertEquals(test.property(NUMBER).name(), NUMBER);
    }

    @Test(expected = NoSuchElementException.class)
    public void test_bean_invalidPropertyName() {
        Bean test = Address.meta().builder().build();
        try {
            test.property("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @Test
    public void test_builder1() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        builder.set("street", "Main Street");
        assertEquals(builder.get("street"), "Main Street");
        builder.set("city", "London");
        assertEquals(builder.get("street"), "Main Street");
        assertEquals(builder.get("city"), "London");
        String street = builder.get(Address.meta().street());
        assertEquals(street, "Main Street");
        String city = builder.get(Address.meta().city());
        assertEquals(city, "London");
        
        Address test = builder.build();
        Address expected = new Address();
        expected.setStreet("Main Street");
        expected.setCity("London");
        
        assertEquals(test, expected);
    }

    @Test
    public void test_builder2() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        builder.set(Address.meta().street(), "Main Street");
        builder.set(Address.meta().number(), 12);
        
        Address test = builder.build();
        Address expected = new Address();
        expected.setStreet("Main Street");
        expected.setNumber(12);
        
        assertEquals(test, expected);
    }

    @Test(expected = NoSuchElementException.class)
    public void test_builder_getInvalidPropertyName() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        try {
            builder.get("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void test_builder_setInvalidPropertyName() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        try {
            builder.set("Rubbish", "");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaBean() {
        MetaBean test = Address.meta();
        
        assertEquals(test.beanType(), Address.class);
        
        assertEquals(test.beanName(), Address.class.getName());
        
        assertEquals(test.metaPropertyCount(), NUM_PROPERTIES);
        
        assertEquals(test.metaPropertyExists(STREET), true);
        assertEquals(test.metaPropertyExists(CITY), true);
        assertEquals(test.metaPropertyExists(NUMBER), true);
        assertEquals(test.metaPropertyExists("Rubbish"), false);
        
        assertEquals(test.metaProperty(STREET).name(), STREET);
        assertEquals(test.metaProperty(CITY).name(), CITY);
        assertEquals(test.metaProperty(NUMBER).name(), NUMBER);
        
        Map<String, MetaProperty<?>> map = test.metaPropertyMap();
        assertEquals(map.size(), NUM_PROPERTIES);
        assertEquals(map.containsKey(STREET), true);
        assertEquals(map.containsKey(CITY), true);
        assertEquals(map.containsKey(NUMBER), true);
        assertEquals(map.containsKey("NotHere"), false);
        assertEquals(map.get(STREET), Address.meta().street());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_metaBean_invalidPropertyName() {
        MetaBean test = Address.meta();
        try {
            test.metaProperty("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_namedPropertyMethod() {
        Address address = new Address();
        Property<String> test = address.street();
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), Address.meta().street());
        
        assertEquals(test.get(), null);
        address.setStreet("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_property_String() {
        Address address = new Address();
        Property<String> test = address.property(STREET);
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), Address.meta().street());
        
        assertEquals(test.get(), null);
        address.setStreet("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_namedMetaPropertyMethod() {
        Address address = new Address();
        MetaProperty<String> test = Address.meta().street();
        
        assertEquals(test.metaBean().beanType(), Address.class);
        assertEquals(test.propertyType(), String.class);
        assertEquals(test.name(), STREET);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setStreet("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaProperty_String() {
        Address address = new Address();
        MetaProperty<String> test = Address.meta().metaProperty(STREET);
        
        assertEquals(test.metaBean().beanType(), Address.class);
        assertEquals(test.propertyType(), String.class);
        assertEquals(test.name(), STREET);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setStreet("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaProperty_types() {
        MetaProperty<String> test = Address.meta().street();
        
        assertEquals(test.metaBean().beanType(), Address.class);
        assertEquals(test.propertyType(), String.class);
        assertEquals(test.propertyGenericType(), String.class);
    }

    @Test
    public void test_metaProperty_annotations() {
        MetaProperty<String> prop = Address.meta().street();
        List<Annotation> test = prop.annotations();
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0) instanceof PropertyDefinition, true);
    }

}
