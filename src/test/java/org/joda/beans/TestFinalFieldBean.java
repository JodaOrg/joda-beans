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
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.FinalFieldBean;
import org.joda.beans.sample.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * Test FinalFieldBean.
 */
public class TestFinalFieldBean {

    /** Bean. */
    private FinalFieldBean bean;

    @Before
    public void setUp() {
        bean = new FinalFieldBean("Hello");
        bean.setFieldNonFinal("Hello");
        bean.getListFinal().add("Hello");
        bean.getFlexiFinal().append("Hello", "World");
        bean.getPersonFinal().setSurname("Hello");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalString() {
        assertEquals(bean.getFieldFinal(), "Hello");
        assertEquals(bean.fieldFinal().get(), "Hello");
        assertEquals(bean.fieldFinal().metaProperty().declaringType(), FinalFieldBean.class);
        assertEquals(bean.fieldFinal().metaProperty().getString(bean), "Hello");
        assertEquals(bean.fieldFinal().metaProperty().get(bean), "Hello");
        assertEquals(bean.fieldFinal().metaProperty().name(), "fieldFinal");
        assertEquals(bean.fieldFinal().metaProperty().style(), PropertyStyle.READ_ONLY);
        assertEquals(bean.fieldFinal().metaProperty().propertyType(), String.class);
        try {
            bean.fieldFinal().set("foo");
            fail();
        } catch (UnsupportedOperationException ex) {
            // expected
        }
        try {
            bean.fieldFinal().metaProperty().set(bean, "foo");
            fail();
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_nonFinalString() {
        assertEquals(bean.getFieldNonFinal(), "Hello");
        assertEquals(bean.fieldNonFinal().get(), "Hello");
        assertEquals(bean.fieldNonFinal().metaProperty().declaringType(), FinalFieldBean.class);
        assertEquals(bean.fieldNonFinal().metaProperty().getString(bean), "Hello");
        assertEquals(bean.fieldNonFinal().metaProperty().get(bean), "Hello");
        assertEquals(bean.fieldNonFinal().metaProperty().name(), "fieldNonFinal");
        assertEquals(bean.fieldNonFinal().metaProperty().style(), PropertyStyle.READ_WRITE);
        assertEquals(bean.fieldNonFinal().metaProperty().propertyType(), String.class);
        
        bean.fieldNonFinal().set("foo");
        assertEquals(bean.getFieldNonFinal(), "foo");
        
        bean.fieldNonFinal().metaProperty().set(bean, "bar");
        assertEquals(bean.getFieldNonFinal(), "bar");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalList() {
        List<String> list = new ArrayList<>();
        list.add("Hello");
        assertEquals(bean.getListFinal(), list);
        assertEquals(bean.listFinal().get(), list);
        assertEquals(bean.listFinal().metaProperty().declaringType(), FinalFieldBean.class);
        assertEquals(bean.listFinal().metaProperty().get(bean), list);
        assertEquals(bean.listFinal().metaProperty().name(), "listFinal");
        assertEquals(bean.listFinal().metaProperty().style(), PropertyStyle.READ_WRITE);
        assertEquals(bean.listFinal().metaProperty().propertyType(), List.class);
        
        list.add("foo");
        List<String> expected1 = new ArrayList<>(list);
        bean.listFinal().set(list);
        assertEquals(bean.getListFinal(), expected1);
        
        list.add("bar");
        List<String> expected2 = new ArrayList<>(list);
        bean.listFinal().metaProperty().set(bean, list);
        assertEquals(bean.getListFinal(), expected2);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalBean() {
        FlexiBean flexi = new FlexiBean();
        flexi.append("Hello", "World");
        assertEquals(bean.getFlexiFinal(), flexi);
        assertEquals(bean.flexiFinal().get(), flexi);
        assertEquals(bean.flexiFinal().metaProperty().declaringType(), FinalFieldBean.class);
        assertEquals(bean.flexiFinal().metaProperty().get(bean), flexi);
        assertEquals(bean.flexiFinal().metaProperty().name(), "flexiFinal");
        assertEquals(bean.flexiFinal().metaProperty().style(), PropertyStyle.READ_WRITE);
        assertEquals(bean.flexiFinal().metaProperty().propertyType(), FlexiBean.class);
        
        flexi.append("foo", "foos");
        FlexiBean expected1 = new FlexiBean(flexi);
        bean.flexiFinal().set(flexi);
        assertEquals(bean.getFlexiFinal(), expected1);
        
        flexi.append("bar", "bars");
        FlexiBean expected2 = new FlexiBean(flexi);
        bean.flexiFinal().metaProperty().set(bean, flexi);
        assertEquals(bean.getFlexiFinal(), expected2);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalPerson() {
        Person person = new Person();
        person.setSurname("Hello");
        assertEquals(bean.getPersonFinal(), person);
        assertEquals(bean.personFinal().get(), person);
        assertEquals(bean.personFinal().metaProperty().declaringType(), FinalFieldBean.class);
        assertEquals(bean.personFinal().metaProperty().get(bean), person);
        assertEquals(bean.personFinal().metaProperty().name(), "personFinal");
        assertEquals(bean.personFinal().metaProperty().style(), PropertyStyle.READ_ONLY);
        assertEquals(bean.personFinal().metaProperty().propertyType(), Person.class);
        try {
            bean.personFinal().set(new Person());
            fail();
        } catch (UnsupportedOperationException ex) {
            // expected
        }
        try {
            bean.personFinal().metaProperty().set(bean, new Person());
            fail();
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

}
