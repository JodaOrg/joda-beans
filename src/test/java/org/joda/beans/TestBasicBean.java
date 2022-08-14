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

import org.joda.beans.sample.Address;
import org.joda.beans.sample.CompanyAddress;
import org.joda.beans.sample.Person;
import org.junit.Test;

/**
 * Test BasicBean.
 */
public class TestBasicBean {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_equals() {
        Person a1 = new Person();
        Person a2 = new Person();
        Person b = new Person();
        
        a1.setForename("Stephen");
        a2.setForename("Stephen");
        b.setForename("Etienne");
        
        assertThat(a1.equals(a1)).isTrue();
        assertThat(a1.equals(a2)).isTrue();
        assertThat(a2.equals(a1)).isTrue();
        assertThat(a2.equals(a2)).isTrue();
        
        assertThat(a1.equals(b)).isFalse();
        assertThat(b.equals(a1)).isFalse();
        
        assertThat(b.equals("Weird type")).isFalse();
        assertThat(b.equals(null)).isFalse();
    }

    @Test
    public void test_hashCode() {
        Person a1 = new Person();
        Person a2 = new Person();
        
        a1.setForename("Stephen");
        a2.setForename("Stephen");
        
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
    }

    @Test
    public void test_toString() {
        Person a = new Person();
        a.setForename("Stephen");
        a.setSurname("Colebourne");
        
        assertThat(a.toString())
                .startsWith("Person{")
                .endsWith("}")
                .contains("forename=Stephen")
                .contains("surname=Colebourne");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_property_equals() {
        Address obj1 = new Address();
        CompanyAddress obj2 = new CompanyAddress();
        Property<String> p1 = obj1.city();
        Property<String> p2 = obj2.city();
        
        obj1.setCity("London");
        obj2.setCity("London");
        
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void test_property_hashCode() {
        Person obj1 = new Person();
        Person obj2 = new Person();
        Property<String> p1 = obj1.forename();
        Property<String> p2 = obj2.forename();
        
        obj1.setForename("Stephen");
        obj2.setForename("Stephen");
        
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    public void test_property_toString() {
        Person obj1 = new Person();
        Property<String> p1 = obj1.forename();
        
        obj1.setForename("Stephen");
        
        assertThat(p1).hasToString("Person:forename=Stephen");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaProperty_equals() {
        MetaProperty<String> p1 = Address.meta().city();
        MetaProperty<String> p2 = CompanyAddress.meta().city();
        
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    public void test_metaProperty_hashCode() {
        MetaProperty<String> p1 = Person.meta().forename();
        MetaProperty<String> p2 = Person.meta().forename();
        
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    public void test_metaProperty_toString() {
        MetaProperty<String> mp1 = Person.meta().forename();
        
        assertThat(mp1).hasToString("Person:forename");
    }

}
