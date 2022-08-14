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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.AbstractResult;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ClassAnnotation;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.SimpleAnnotation;
import org.junit.Test;

/**
 * Test property using Person.
 */
public class TestPerson {

    private static final int NUM_PROPERTIES = 8;
    private static final String FORENAME = "forename";
    private static final String SURNAME = "surname";
    private static final String NUMBER_OF_CARS = "numberOfCars";

    @Test
    public void test_bean() {
        Bean test = Person.meta().builder().build();
        
        assertThat(test instanceof Person).isTrue();
        
        assertThat(test.metaBean()).isEqualTo(Person.meta());
        
        assertThat(test.propertyNames().contains(FORENAME)).isTrue();
        assertThat(test.propertyNames().contains(SURNAME)).isTrue();
        assertThat(test.propertyNames().contains(NUMBER_OF_CARS)).isTrue();
        assertThat(test.propertyNames().contains("Rubbish")).isFalse();
        
        assertThat(test.property(FORENAME).name()).isEqualTo(FORENAME);
        assertThat(test.property(SURNAME).name()).isEqualTo(SURNAME);
        assertThat(test.property(NUMBER_OF_CARS).name()).isEqualTo(NUMBER_OF_CARS);
    }

    @Test
    public void test_bean_invalidPropertyName() {
        Bean test = Person.meta().builder().build();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.property("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaBean() {
        MetaBean test = Person.meta();
        
        assertThat(test.isBuildable()).isTrue();
        assertThat(test.beanType()).isEqualTo(Person.class);
        assertThat(test.beanName()).isEqualTo(Person.class.getName());
        
        assertThat(test.metaPropertyCount()).isEqualTo(NUM_PROPERTIES);
        
        assertThat(test.metaPropertyExists(FORENAME)).isTrue();
        assertThat(test.metaPropertyExists(SURNAME)).isTrue();
        assertThat(test.metaPropertyExists(NUMBER_OF_CARS)).isTrue();
        assertThat(test.metaPropertyExists("Rubbish")).isFalse();
        
        assertThat(test.metaProperty(FORENAME).name()).isEqualTo(FORENAME);
        assertThat(test.metaProperty(SURNAME).name()).isEqualTo(SURNAME);
        assertThat(test.metaProperty(NUMBER_OF_CARS).name()).isEqualTo(NUMBER_OF_CARS);
        
        Map<String, MetaProperty<?>> map = test.metaPropertyMap();
        assertThat(map.size()).isEqualTo(NUM_PROPERTIES);
        assertThat(map.containsKey(FORENAME)).isTrue();
        assertThat(map.containsKey(SURNAME)).isTrue();
        assertThat(map.containsKey(NUMBER_OF_CARS)).isTrue();
    }

    @Test
    public void test_metaBean_invalidPropertyName() {
        MetaBean test = Person.meta();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.metaProperty("Rubbish"));
    }

    @Test
    public void test_metaBean_abstract() {
        MetaBean test = AbstractResult.meta();
        
        assertThat(test.isBuildable()).isFalse();
        assertThat(test.beanType()).isEqualTo(AbstractResult.class);
        assertThat(test.beanName()).isEqualTo(AbstractResult.class.getName());
        
        assertThat(test.metaPropertyCount()).isEqualTo(2);
        
        assertThat(test.metaPropertyExists("docs")).isTrue();
        assertThat(test.metaPropertyExists("Rubbish")).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_namedPropertyMethod() {
        Person person = new Person();
        Property<String> test = person.forename();
        
        assertThat((Object) test.bean()).isSameAs(person);
        assertThat(test.metaProperty()).isSameAs(Person.meta().forename());
        
        assertThat(test.get()).isNull();
        person.setForename("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_property_String() {
        Person person = new Person();
        Property<String> test = person.property(FORENAME);
        
        assertThat((Object) test.bean()).isSameAs(person);
        assertThat(test.metaProperty()).isSameAs(Person.meta().forename());
        
        assertThat(test.get()).isNull();
        person.setForename("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_namedMetaPropertyMethod() {
        Person person = new Person();
        MetaProperty<String> test = Person.meta().forename();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Person.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isSameAs(FORENAME);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(person)).isNull();
        person.setForename("A");
        assertThat(test.get(person)).isEqualTo("A");
        test.set(person, "B");
        assertThat(test.get(person)).isEqualTo("B");
        assertThat(test.put(person, "C")).isEqualTo("B");
        assertThat(test.get(person)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaProperty_String() {
        Person person = new Person();
        MetaProperty<String> test = Person.meta().metaProperty(FORENAME);
        
        assertThat(test.metaBean().beanType()).isEqualTo(Person.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isSameAs(FORENAME);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(person)).isNull();
        person.setForename("A");
        assertThat(test.get(person)).isEqualTo("A");
        test.set(person, "B");
        assertThat(test.get(person)).isEqualTo("B");
        assertThat(test.put(person, "C")).isEqualTo("B");
        assertThat(test.get(person)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaProperty_types_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Person.class);
        assertThat(test.propertyType()).isEqualTo(List.class);
        assertThat(test.propertyGenericType()).isInstanceOf(ParameterizedType.class);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertThat(pt.getRawType()).isEqualTo(List.class);
        assertThat(pt.getOwnerType()).isNull();
        Type[] actualTypes = pt.getActualTypeArguments();
        assertThat(actualTypes.length).isEqualTo(1);
        assertThat(actualTypes[0]).isEqualTo(Address.class);
    }

    @Test
    public void test_BeanUtils_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Person.class);
        assertThat(test.propertyType()).isEqualTo(List.class);
        assertThat(test.propertyGenericType()).isInstanceOf(ParameterizedType.class);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertThat(pt.getRawType()).isEqualTo(List.class);
        assertThat(pt.getOwnerType()).isNull();
        Type[] actualTypes = pt.getActualTypeArguments();
        assertThat(actualTypes.length).isEqualTo(1);
        assertThat(actualTypes[0]).isEqualTo(Address.class);
    }

    @Test
    public void test_metaProperty_types_otherAddressMap() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        assertThat(test.metaBean().beanType()).isEqualTo(Person.class);
        assertThat(test.propertyType()).isEqualTo(Map.class);
        assertThat(test.propertyGenericType()).isInstanceOf(ParameterizedType.class);
        ParameterizedType pt = (ParameterizedType) test.propertyGenericType();
        assertThat(pt.getRawType()).isEqualTo(Map.class);
        assertThat(pt.getOwnerType()).isNull();
        Type[] actualTypes = pt.getActualTypeArguments();
        assertThat(actualTypes.length).isEqualTo(2);
        assertThat(actualTypes[0]).isEqualTo(String.class);
        assertThat(actualTypes[1]).isEqualTo(Address.class);
    }

    @Test
    public void test_metaProperty_annotations_addressList() {
        MetaProperty<List<Address>> prop = Person.meta().addressList();
        List<Annotation> test = prop.annotations();
        
        assertThat(test.size()).isEqualTo(1);
        assertThat(test.get(0) instanceof PropertyDefinition).isTrue();
    }

    @Test
    public void test_metaProperty_annotations_extensions() {
        MetaProperty<FlexiBean> prop = Person.meta().extensions();
        List<Annotation> annos = prop.annotations();
        
        assertThat(annos.size()).isEqualTo(2);
        assertThat(annos.get(0) instanceof PropertyDefinition).isTrue();
        assertThat(annos.get(1) instanceof SimpleAnnotation).isTrue();
        assertThat(prop.annotation(PropertyDefinition.class).get()).isEqualTo("smart");
    }

    @Test
    public void test_metaBean_annotations() {
        Person.Meta meta = Person.meta();
        List<Annotation> annos = meta.annotations();
        
        assertThat(annos.size()).isEqualTo(2);
        assertThat(annos.get(0) instanceof BeanDefinition).isTrue();
        assertThat(annos.get(1) instanceof ClassAnnotation).isTrue();
        assertThat(meta.annotation(BeanDefinition.class).builderScope()).isEqualTo("smart");
    }

}
