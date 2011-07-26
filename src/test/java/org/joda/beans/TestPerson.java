/*
 *  Copyright 2001-2011 Stephen Colebourne
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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.bind.annotation.XmlID;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.Person;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

/**
 * Test property using Person.
 */
@Test
public class TestPerson {

    private static final int NUM_PROPERTIES = 8;
    private static final String FORENAME = "forename";
    private static final String SURNAME = "surname";
    private static final String NUMBER_OF_CARS = "numberOfCars";

    public void test_bean() {
        Bean test = Person.meta().builder().build();
        
        assertEquals(test instanceof Person, true);
        
        assertEquals(test.metaBean(), Person.meta());
        
        assertEquals(test.propertyNames().contains(FORENAME), true);
        assertEquals(test.propertyNames().contains(SURNAME), true);
        assertEquals(test.propertyNames().contains(NUMBER_OF_CARS), true);
        assertEquals(test.propertyNames().contains("Rubbish"), false);
        
        assertEquals(test.property(FORENAME).name(), FORENAME);
        assertEquals(test.property(SURNAME).name(), SURNAME);
        assertEquals(test.property(NUMBER_OF_CARS).name(), NUMBER_OF_CARS);
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_bean_invalidPropertyName() {
        Bean test = Person.meta().builder().build();
        try {
            test.property("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_metaBean() {
        MetaBean test = Person.meta();
        
        assertEquals(test.beanType(), Person.class);
        
        assertEquals(test.beanName(), Person.class.getName());
        
        assertEquals(test.metaPropertyCount(), NUM_PROPERTIES);
        
        assertEquals(test.metaPropertyExists(FORENAME), true);
        assertEquals(test.metaPropertyExists(SURNAME), true);
        assertEquals(test.metaPropertyExists(NUMBER_OF_CARS), true);
        assertEquals(test.metaPropertyExists("Rubbish"), false);
        
        assertEquals(test.metaProperty(FORENAME).name(), FORENAME);
        assertEquals(test.metaProperty(SURNAME).name(), SURNAME);
        assertEquals(test.metaProperty(NUMBER_OF_CARS).name(), NUMBER_OF_CARS);
        
        Map<String, MetaProperty<Object>> map = test.metaPropertyMap();
        assertEquals(map.size(), NUM_PROPERTIES);
        assertEquals(map.containsKey(FORENAME), true);
        assertEquals(map.containsKey(SURNAME), true);
        assertEquals(map.containsKey(NUMBER_OF_CARS), true);
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_metaBean_invalidPropertyName() {
        MetaBean test = Person.meta();
        try {
            test.metaProperty("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_namedPropertyMethod() {
        Person person = new Person();
        Property<String> test = person.forename();
        
        assertSame(test.bean(), person);
        assertSame(test.metaProperty(), Person.meta().forename());
        
        assertEquals(test.get(), null);
        person.setForename("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    public void test_property_String() {
        Person person = new Person();
        Property<String> test = person.property(FORENAME);
        
        assertSame(test.bean(), person);
        assertSame(test.metaProperty(), Person.meta().forename());
        
        assertEquals(test.get(), null);
        person.setForename("A");
        assertEquals(test.get(), "A");
        test.set("B");
        assertEquals(test.get(), "B");
        assertEquals(test.put("C"), "B");
        assertEquals(test.get(), "C");
    }

    //-----------------------------------------------------------------------
    public void test_propertyMap() {
        Person person = new Person();
        PropertyMap test = person.metaBean().createPropertyMap(person);
        
        assertSame(test.size(), NUM_PROPERTIES);
        assertEquals(test.containsKey(FORENAME), true);
        assertEquals(test.containsKey(SURNAME), true);
        assertEquals(test.containsKey(NUMBER_OF_CARS), true);
    }

    public void test_propertyMap_flatten() {
        Person person = new Person();
        person.setForename("A");
        person.setSurname("B");
        person.setNumberOfCars(3);
        Map<String, Object> test = person.metaBean().createPropertyMap(person).flatten();
        
        assertSame(test.size(), NUM_PROPERTIES);
        assertEquals(test.get(FORENAME), "A");
        assertEquals(test.get(SURNAME), "B");
        assertEquals(test.get(NUMBER_OF_CARS), 3);
    }

    //-----------------------------------------------------------------------
    public void test_namedMetaPropertyMethod() {
        Person person = new Person();
        MetaProperty<String> test = Person.meta().forename();
        
        assertSame(test.metaBean().beanType(), Person.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), FORENAME);
        assertEquals(test.readWrite(), PropertyReadWrite.READ_WRITE);
        
        assertEquals(test.get(person), null);
        person.setForename("A");
        assertEquals(test.get(person), "A");
        test.set(person, "B");
        assertEquals(test.get(person), "B");
        assertEquals(test.put(person, "C"), "B");
        assertEquals(test.get(person), "C");
    }

    //-----------------------------------------------------------------------
    public void test_metaProperty_String() {
        Person person = new Person();
        MetaProperty<String> test = Person.meta().metaProperty(FORENAME);
        
        assertSame(test.metaBean().beanType(), Person.class);
        assertSame(test.propertyType(), String.class);
        assertSame(test.name(), FORENAME);
        assertEquals(test.readWrite(), PropertyReadWrite.READ_WRITE);
        
        assertEquals(test.get(person), null);
        person.setForename("A");
        assertEquals(test.get(person), "A");
        test.set(person, "B");
        assertEquals(test.get(person), "B");
        assertEquals(test.put(person, "C"), "B");
        assertEquals(test.get(person), "C");
    }

    //-----------------------------------------------------------------------
    public void test_metaProperty_types_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertSame(test.metaBean().beanType(), Person.class);
        assertSame(test.propertyType(), List.class);
        assertSame(test.propertyGenericType() instanceof ParameterizedType, true);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertEquals(pt.getRawType(), List.class);
        assertEquals(pt.getOwnerType(), null);
        Type[] actualTypes = pt.getActualTypeArguments();
        assertEquals(actualTypes.length, 1);
        assertEquals(actualTypes[0], Address.class);
    }

    public void test_BeanUtils_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertSame(test.metaBean().beanType(), Person.class);
        assertSame(test.propertyType(), List.class);
        assertSame(test.propertyGenericType() instanceof ParameterizedType, true);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertEquals(pt.getRawType(), List.class);
        assertEquals(pt.getOwnerType(), null);
        Type[] actualTypes = pt.getActualTypeArguments();
        assertEquals(actualTypes.length, 1);
        assertEquals(actualTypes[0], Address.class);
    }

    public void test_metaProperty_types_otherAddressMap() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        assertSame(test.metaBean().beanType(), Person.class);
        assertSame(test.propertyType(), Map.class);
        assertSame(test.propertyGenericType() instanceof ParameterizedType, true);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertEquals(pt.getRawType(), Map.class);
        assertEquals(pt.getOwnerType(), null);
        Type[] actualTypes = pt.getActualTypeArguments();
        assertEquals(actualTypes.length, 2);
        assertEquals(actualTypes[0], String.class);
        assertEquals(actualTypes[1], Address.class);
    }

    public void test_metaProperty_annotations_addressList() {
        MetaProperty<List<Address>> prop = Person.meta().addressList();
        List<Annotation> test = prop.annotations();
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0) instanceof PropertyDefinition, true);
    }

    public void test_metaProperty_annotations_extensions() {
        MetaProperty<FlexiBean> prop = Person.meta().extensions();
        List<Annotation> test = prop.annotations();
        
        assertEquals(test.size(), 2);
        assertEquals(test.get(0) instanceof PropertyDefinition, true);
        assertEquals(test.get(1) instanceof XmlID, true);
    }

}
