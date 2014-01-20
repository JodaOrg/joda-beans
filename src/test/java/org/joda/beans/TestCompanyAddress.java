/*
 *  Copyright 2001-2014 Stephen Colebourne
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
import static org.testng.Assert.assertSame;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.gen.CompanyAddress;
import org.testng.annotations.Test;

/**
 * Test property using Person.
 */
@Test
public class TestCompanyAddress {

    private static final int NUM_PROPERTIES = 5;
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String NUMBER = "number";
    private static final String COMPANY_NAME = "companyName";

    public void test_bean() {
        Bean test = CompanyAddress.meta().builder().build();
        
        assertEquals(test instanceof CompanyAddress, true);
        
        assertEquals(test.metaBean(), CompanyAddress.meta());
        
        assertEquals(test.propertyNames().contains(STREET), true);
        assertEquals(test.propertyNames().contains(CITY), true);
        assertEquals(test.propertyNames().contains(NUMBER), true);
        assertEquals(test.propertyNames().contains(COMPANY_NAME), true);
        assertEquals(test.propertyNames().contains("Rubbish"), false);
        
        assertEquals(test.property(STREET).name(), STREET);
        assertEquals(test.property(CITY).name(), CITY);
        assertEquals(test.property(NUMBER).name(), NUMBER);
        assertEquals(test.property(COMPANY_NAME).name(), COMPANY_NAME);
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_bean_invalidPropertyName() {
        Bean test = CompanyAddress.meta().builder().build();
        try {
            test.property("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_metaBean() {
        MetaBean test = CompanyAddress.meta();
        
        assertEquals(test.beanType(), CompanyAddress.class);
        
        assertEquals(test.beanName(), CompanyAddress.class.getName());
        
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
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_metaBean_invalidPropertyName() {
        MetaBean test = CompanyAddress.meta();
        try {
            test.metaProperty("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_namedPropertyMethod_street() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.street();
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), CompanyAddress.meta().street());
        
        assertEquals(test.get(), null);
        address.setStreet("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    public void test_namedPropertyMethod_companyName() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.companyName();
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), CompanyAddress.meta().companyName());
        
        assertEquals(test.get(), null);
        address.setCompanyName("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    public void test_property_String_street() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.property(STREET);
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), CompanyAddress.meta().street());
        
        assertEquals(test.get(), null);
        address.setStreet("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    public void test_property_String_companyName() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.property(COMPANY_NAME);
        
        assertSame(test.bean(), address);
        assertSame(test.metaProperty(), CompanyAddress.meta().companyName());
        
        assertEquals(test.get(), null);
        address.setCompanyName("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    public void test_propertyMap() {
        CompanyAddress address = new CompanyAddress();
        PropertyMap test = address.metaBean().createPropertyMap(address);
        
        assertSame(test.size(), NUM_PROPERTIES);
        assertEquals(test.containsKey(STREET), true);
        assertEquals(test.containsKey(CITY), true);
        assertEquals(test.containsKey(NUMBER), true);
        assertEquals(test.containsKey(COMPANY_NAME), true);
    }

    public void test_propertyMap_flatten() {
        CompanyAddress address = new CompanyAddress();
        address.setStreet("A");
        address.setCity("B");
        address.setNumber(3);
        address.setCompanyName("D");
        Map<String, Object> test = address.metaBean().createPropertyMap(address).flatten();
        
        assertSame(test.size(), NUM_PROPERTIES);
        assertEquals(test.get(STREET), "A");
        assertEquals(test.get(CITY), "B");
        assertEquals(test.get(NUMBER), 3);
        assertEquals(test.get(COMPANY_NAME), "D");
    }

    //-----------------------------------------------------------------------
    public void test_namedMetaPropertyMethod_street() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().street();
        
        assertSame(test.metaBean().beanType(), CompanyAddress.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), STREET);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setStreet("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    public void test_namedMetaPropertyMethod_companyName() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().companyName();
        
        assertSame(test.metaBean().beanType(), CompanyAddress.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), COMPANY_NAME);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setCompanyName("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    //-----------------------------------------------------------------------
    public void test_metaProperty_String_street() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().metaProperty(STREET);
        
        assertSame(test.metaBean().beanType(), CompanyAddress.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), STREET);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setStreet("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    public void test_metaProperty_String_companyName() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().metaProperty(COMPANY_NAME);
        
        assertSame(test.metaBean().beanType(), CompanyAddress.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), COMPANY_NAME);
        assertEquals(test.style(), PropertyStyle.READ_WRITE);
        
        assertEquals(test.get(address), null);
        address.setCompanyName("A");
        assertEquals(test.get(address), "A");
        test.set(address, "B");
        assertEquals(test.get(address), "B");
        assertEquals(test.put(address, "C"), "B");
        assertEquals(test.get(address), "C");
    }

    //-----------------------------------------------------------------------
    public void test_metaProperty_types() {
        MetaProperty<String> test = CompanyAddress.meta().companyName();
        
        assertSame(test.metaBean().beanType(), CompanyAddress.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.propertyGenericType(), String.class);
    }

    public void test_metaProperty_annotations() {
        MetaProperty<String> prop = CompanyAddress.meta().companyName();
        List<Annotation> test = prop.annotations();
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0) instanceof PropertyDefinition, true);
    }

}
