/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import java.util.Map;
import java.util.NoSuchElementException;

import org.testng.annotations.Test;

/**
 * Test property using Person.
 */
@Test
public class TestPerson {

    private static final String FORENAME = "forename";
    private static final String SURNAME = "surname";
    private static final String NUMBER_OF_CARS = "numberOfCars";

    public void test_bean() {
        Bean<Person> test = Person.META.createBean();
        
        assertEquals(test.beanData() instanceof Person, true);
        
        assertEquals(test.metaBean(), Person.META);
        
        assertEquals(test.propertyExists(FORENAME), true);
        assertEquals(test.propertyExists(SURNAME), true);
        assertEquals(test.propertyExists(NUMBER_OF_CARS), true);
        assertEquals(test.propertyExists("Rubbish"), false);
        
        assertEquals(test.property(FORENAME).name(), FORENAME);
        assertEquals(test.property(SURNAME).name(), SURNAME);
        assertEquals(test.property(NUMBER_OF_CARS).name(), NUMBER_OF_CARS);
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_bean_invalidPropertyName() {
        Bean<Person> test = Person.META.createBean();
        try {
            test.property("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_metaBean() {
        MetaBean<Person> test = Person.META;
        
        assertEquals(test.beanClass(), Person.class);
        
        assertEquals(test.name(), Person.class.getName());
        
        assertEquals(test.metaPropertyCount(), 3);
        
        assertEquals(test.metaPropertyExists(FORENAME), true);
        assertEquals(test.metaPropertyExists(SURNAME), true);
        assertEquals(test.metaPropertyExists(NUMBER_OF_CARS), true);
        assertEquals(test.metaPropertyExists("Rubbish"), false);
        
        assertEquals(test.metaProperty(FORENAME).name(), FORENAME);
        assertEquals(test.metaProperty(SURNAME).name(), SURNAME);
        assertEquals(test.metaProperty(NUMBER_OF_CARS).name(), NUMBER_OF_CARS);
        
        Map<String, MetaProperty<Person, Object>> map = test.metaPropertyMap();
        assertEquals(map.size(), 3);
        assertEquals(map.containsKey(FORENAME), true);
        assertEquals(map.containsKey(SURNAME), true);
        assertEquals(map.containsKey(NUMBER_OF_CARS), true);
    }

    @Test(expectedExceptions=NoSuchElementException.class)
    public void test_metaBean_invalidPropertyName() {
        MetaBean<Person> test = Person.META;
        try {
            test.metaProperty("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public void test_property() {
        Person person = new Person();
        Property<Person, String> test = person.forename();
        
        assertSame(test.bean(), person);
        assertSame(test.metaProperty(), Person.FORENAME);
        
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
        PropertyMap<Person> test = person.propertyMap();
        
        assertSame(test.size(), 3);
        assertEquals(test.containsKey(FORENAME), true);
        assertEquals(test.containsKey(SURNAME), true);
        assertEquals(test.containsKey(NUMBER_OF_CARS), true);
    }

    public void test_propertyMap_flatten() {
        Person person = new Person();
        person.setForename("A");
        person.setSurname("B");
        person.setNumberOfCars(3);
        Map<String, Object> test = person.propertyMap().flatten();
        
        assertSame(test.size(), 3);
        assertEquals(test.get(FORENAME), "A");
        assertEquals(test.get(SURNAME), "B");
        assertEquals(test.get(NUMBER_OF_CARS), 3);
    }

    //-----------------------------------------------------------------------
    public void test_metaProperty() {
        Person person = new Person();
        MetaProperty<Person, String> test = Person.FORENAME;
        
        assertSame(test.beanClass(), Person.class);
        assertSame(test.propertyClass(), String.class);
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

}
