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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.ArrayList;
import java.util.List;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.FinalFieldBean;
import org.joda.beans.sample.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test FinalFieldBean.
 */
public class TestFinalFieldBean {

    /** Bean. */
    private FinalFieldBean bean;

    @BeforeEach
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
        assertThat(bean.getFieldFinal()).isEqualTo("Hello");
        assertThat(bean.fieldFinal().get()).isEqualTo("Hello");
        assertThat(bean.fieldFinal().metaProperty().declaringType()).isEqualTo(FinalFieldBean.class);
        assertThat(bean.fieldFinal().metaProperty().getString(bean)).isEqualTo("Hello");
        assertThat(bean.fieldFinal().metaProperty().get(bean)).isEqualTo("Hello");
        assertThat(bean.fieldFinal().metaProperty().name()).isEqualTo("fieldFinal");
        assertThat(bean.fieldFinal().metaProperty().style()).isEqualTo(PropertyStyle.READ_ONLY);
        assertThat(bean.fieldFinal().metaProperty().propertyType()).isEqualTo(String.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> bean.fieldFinal().set("foo"));
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> bean.fieldFinal().metaProperty().set(bean, "foo"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_nonFinalString() {
        assertThat(bean.getFieldNonFinal()).isEqualTo("Hello");
        assertThat(bean.fieldNonFinal().get()).isEqualTo("Hello");
        assertThat(bean.fieldNonFinal().metaProperty().declaringType()).isEqualTo(FinalFieldBean.class);
        assertThat(bean.fieldNonFinal().metaProperty().getString(bean)).isEqualTo("Hello");
        assertThat(bean.fieldNonFinal().metaProperty().get(bean)).isEqualTo("Hello");
        assertThat(bean.fieldNonFinal().metaProperty().name()).isEqualTo("fieldNonFinal");
        assertThat(bean.fieldNonFinal().metaProperty().style()).isEqualTo(PropertyStyle.READ_WRITE);
        assertThat(bean.fieldNonFinal().metaProperty().propertyType()).isEqualTo(String.class);
        
        bean.fieldNonFinal().set("foo");
        assertThat(bean.getFieldNonFinal()).isEqualTo("foo");
        
        bean.fieldNonFinal().metaProperty().set(bean, "bar");
        assertThat(bean.getFieldNonFinal()).isEqualTo("bar");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalList() {
        List<String> list = new ArrayList<>();
        list.add("Hello");
        assertThat(bean.getListFinal()).isEqualTo(list);
        assertThat(bean.listFinal().get()).isEqualTo(list);
        assertThat(bean.listFinal().metaProperty().declaringType()).isEqualTo(FinalFieldBean.class);
        assertThat(bean.listFinal().metaProperty().get(bean)).isEqualTo(list);
        assertThat(bean.listFinal().metaProperty().name()).isEqualTo("listFinal");
        assertThat(bean.listFinal().metaProperty().style()).isEqualTo(PropertyStyle.READ_WRITE);
        assertThat(bean.listFinal().metaProperty().propertyType()).isEqualTo(List.class);
        
        list.add("foo");
        List<String> expected1 = new ArrayList<>(list);
        bean.listFinal().set(list);
        assertThat(bean.getListFinal()).isEqualTo(expected1);
        
        list.add("bar");
        List<String> expected2 = new ArrayList<>(list);
        bean.listFinal().metaProperty().set(bean, list);
        assertThat(bean.getListFinal()).isEqualTo(expected2);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalBean() {
        FlexiBean flexi = new FlexiBean();
        flexi.append("Hello", "World");
        assertThat(bean.getFlexiFinal()).isEqualTo(flexi);
        assertThat(bean.flexiFinal().get()).isEqualTo(flexi);
        assertThat(bean.flexiFinal().metaProperty().declaringType()).isEqualTo(FinalFieldBean.class);
        assertThat(bean.flexiFinal().metaProperty().get(bean)).isEqualTo(flexi);
        assertThat(bean.flexiFinal().metaProperty().name()).isEqualTo("flexiFinal");
        assertThat(bean.flexiFinal().metaProperty().style()).isEqualTo(PropertyStyle.READ_WRITE);
        assertThat(bean.flexiFinal().metaProperty().propertyType()).isEqualTo(FlexiBean.class);
        
        flexi.append("foo", "foos");
        FlexiBean expected1 = new FlexiBean(flexi);
        bean.flexiFinal().set(flexi);
        assertThat(bean.getFlexiFinal()).isEqualTo(expected1);
        
        flexi.append("bar", "bars");
        FlexiBean expected2 = new FlexiBean(flexi);
        bean.flexiFinal().metaProperty().set(bean, flexi);
        assertThat(bean.getFlexiFinal()).isEqualTo(expected2);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_finalPerson() {
        Person person = new Person();
        person.setSurname("Hello");
        assertThat(bean.getPersonFinal()).isEqualTo(person);
        assertThat(bean.personFinal().get()).isEqualTo(person);
        assertThat(bean.personFinal().metaProperty().declaringType()).isEqualTo(FinalFieldBean.class);
        assertThat(bean.personFinal().metaProperty().get(bean)).isEqualTo(person);
        assertThat(bean.personFinal().metaProperty().name()).isEqualTo("personFinal");
        assertThat(bean.personFinal().metaProperty().style()).isEqualTo(PropertyStyle.READ_ONLY);
        assertThat(bean.personFinal().metaProperty().propertyType()).isEqualTo(Person.class);

        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> bean.personFinal().set(new Person()));
        assertThatExceptionOfType(UnsupportedOperationException.class)
                .isThrownBy(() -> bean.personFinal().metaProperty().set(bean, new Person()));
    }

}
