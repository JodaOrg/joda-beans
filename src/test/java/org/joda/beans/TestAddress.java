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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.sample.Address;
import org.junit.jupiter.api.Test;

/**
 * Test property using Person.
 */
class TestAddress {

    private static final int NUM_PROPERTIES = 4;
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String NUMBER = "number";

    @Test
    void test_bean() {
        Bean test = new Address();
        
        assertThat(test).isInstanceOf(Address.class);
        
        assertThat(test.metaBean()).isEqualTo(Address.meta());
        
        assertThat(test.propertyNames()).contains(STREET);
        assertThat(test.propertyNames()).contains(CITY);
        assertThat(test.propertyNames()).contains(NUMBER);
        assertThat(test.propertyNames()).doesNotContain("Rubbish");
        
        assertThat(test.property(STREET).name()).isEqualTo(STREET);
        assertThat(test.property(CITY).name()).isEqualTo(CITY);
        assertThat(test.property(NUMBER).name()).isEqualTo(NUMBER);
    }

    @Test
    void test_bean_invalidPropertyName() {
        Bean test = Address.meta().builder().build();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.property("Rubbish"));
    }

    @Test
    void test_builder1() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        builder.set("street", "Main Street");
        assertThat(builder.get("street")).isEqualTo("Main Street");
        builder.set("city", "London");
        assertThat(builder.get("street")).isEqualTo("Main Street");
        assertThat(builder.get("city")).isEqualTo("London");
        String street = builder.get(Address.meta().street());
        assertThat(street).isEqualTo("Main Street");
        String city = builder.get(Address.meta().city());
        assertThat(city).isEqualTo("London");
        
        Address test = builder.build();
        Address expected = new Address();
        expected.setStreet("Main Street");
        expected.setCity("London");
        
        assertThat(test).isEqualTo(expected);
    }

    @Test
    void test_builder2() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        builder.set(Address.meta().street(), "Main Street");
        builder.set(Address.meta().number(), 12);
        
        Address test = builder.build();
        Address expected = new Address();
        expected.setStreet("Main Street");
        expected.setNumber(12);
        
        assertThat(test).isEqualTo(expected);
    }

    @Test
    void test_builder_getInvalidPropertyName() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.get("Rubbish"));
    }

    @Test
    void test_builder_setInvalidPropertyName() {
        BeanBuilder<? extends Address> builder = Address.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.set("Rubbish", ""));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaBean() {
        MetaBean test = Address.meta();
        
        assertThat(test.beanType()).isEqualTo(Address.class);
        
        assertThat(test.beanName()).isEqualTo(Address.class.getName());
        
        assertThat(test.metaPropertyCount()).isEqualTo(NUM_PROPERTIES);
        
        assertThat(test.metaPropertyExists(STREET)).isTrue();
        assertThat(test.metaPropertyExists(CITY)).isTrue();
        assertThat(test.metaPropertyExists(NUMBER)).isTrue();
        assertThat(test.metaPropertyExists("Rubbish")).isFalse();
        
        assertThat(test.metaProperty(STREET).name()).isEqualTo(STREET);
        assertThat(test.metaProperty(CITY).name()).isEqualTo(CITY);
        assertThat(test.metaProperty(NUMBER).name()).isEqualTo(NUMBER);
        
        Map<String, MetaProperty<?>> map = test.metaPropertyMap();
        assertThat(map).hasSize(NUM_PROPERTIES);
        assertThat(map).containsKey(STREET);
        assertThat(map).containsKey(CITY);
        assertThat(map).containsKey(NUMBER);
        assertThat(map).doesNotContainKey("NotHere");
        assertThat(map.get(STREET)).isEqualTo(Address.meta().street());
    }

    @Test
    void test_metaBean_invalidPropertyName() {
        MetaBean test = Address.meta();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.metaProperty("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_namedPropertyMethod() {
        Address address = new Address();
        Property<String> test = address.street();
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(Address.meta().street());
        
        assertThat(test.get()).isNull();
        address.setStreet("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_property_String() {
        Address address = new Address();
        Property<String> test = address.property(STREET);
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(Address.meta().street());
        
        assertThat(test.get()).isNull();
        address.setStreet("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_namedMetaPropertyMethod() {
        Address address = new Address();
        MetaProperty<String> test = Address.meta().street();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Address.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isEqualTo(STREET);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(address)).isNull();
        address.setStreet("A");
        assertThat(test.get(address)).isEqualTo("A");
        test.set(address, "B");
        assertThat(test.get(address)).isEqualTo("B");
        assertThat(test.put(address, "C")).isEqualTo("B");
        assertThat(test.get(address)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaProperty_String() {
        Address address = new Address();
        MetaProperty<String> test = Address.meta().metaProperty(STREET);
        
        assertThat(test.metaBean().beanType()).isEqualTo(Address.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isEqualTo(STREET);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(address)).isNull();
        address.setStreet("A");
        assertThat(test.get(address)).isEqualTo("A");
        test.set(address, "B");
        assertThat(test.get(address)).isEqualTo("B");
        assertThat(test.put(address, "C")).isEqualTo("B");
        assertThat(test.get(address)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaProperty_types() {
        MetaProperty<String> test = Address.meta().street();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Address.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.propertyGenericType()).isEqualTo(String.class);
    }

    @Test
    void test_metaProperty_annotations() {
        MetaProperty<String> prop = Address.meta().street();
        List<Annotation> test = prop.annotations();
        
        assertThat(test.size()).isEqualTo(1);
        assertThat(test.get(0)).isInstanceOf(PropertyDefinition.class);
    }

}
