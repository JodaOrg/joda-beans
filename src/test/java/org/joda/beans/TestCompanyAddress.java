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
import org.joda.beans.sample.CompanyAddress;
import org.junit.jupiter.api.Test;

/**
 * Test property using Person.
 */
class TestCompanyAddress {

    private static final int NUM_PROPERTIES = 5;
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String NUMBER = "number";
    private static final String COMPANY_NAME = "companyName";

    @Test
    void test_bean() {
        Bean test = CompanyAddress.meta().builder().build();
        
        assertThat(test instanceof CompanyAddress).isTrue();
        
        assertThat(test.metaBean()).isEqualTo(CompanyAddress.meta());
        
        assertThat(test.propertyNames()).contains(STREET);
        assertThat(test.propertyNames()).contains(CITY);
        assertThat(test.propertyNames()).contains(NUMBER);
        assertThat(test.propertyNames()).contains(COMPANY_NAME);
        assertThat(test.propertyNames()).doesNotContain("Rubbish");
        
        assertThat(test.property(STREET).name()).isEqualTo(STREET);
        assertThat(test.property(CITY).name()).isEqualTo(CITY);
        assertThat(test.property(NUMBER).name()).isEqualTo(NUMBER);
        assertThat(test.property(COMPANY_NAME).name()).isEqualTo(COMPANY_NAME);
    }

    @Test
    void test_bean_invalidPropertyName() {
        Bean test = CompanyAddress.meta().builder().build();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.property("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaBean() {
        MetaBean test = CompanyAddress.meta();
        
        assertThat(test.beanType()).isEqualTo(CompanyAddress.class);
        
        assertThat(test.beanName()).isEqualTo(CompanyAddress.class.getName());
        
        assertThat(test.metaPropertyCount()).isEqualTo(NUM_PROPERTIES);
        
        assertThat(test.metaPropertyExists(STREET)).isTrue();
        assertThat(test.metaPropertyExists(CITY)).isTrue();
        assertThat(test.metaPropertyExists(NUMBER)).isTrue();
        assertThat(test.metaPropertyExists("Rubbish")).isFalse();
        
        assertThat(test.metaProperty(STREET).name()).isEqualTo(STREET);
        assertThat(test.metaProperty(CITY).name()).isEqualTo(CITY);
        assertThat(test.metaProperty(NUMBER).name()).isEqualTo(NUMBER);
        
        Map<String, MetaProperty<?>> map = test.metaPropertyMap();
        assertThat(map.size()).isEqualTo(NUM_PROPERTIES);
        assertThat(map.containsKey(STREET)).isTrue();
        assertThat(map.containsKey(CITY)).isTrue();
        assertThat(map.containsKey(NUMBER)).isTrue();
    }

    @Test
    void test_metaBean_invalidPropertyName() {
        MetaBean test = CompanyAddress.meta();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.metaProperty("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_namedPropertyMethod_street() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.street();
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(CompanyAddress.meta().street());
        
        assertThat(test.get()).isNull();
        address.setStreet("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    @Test
    void test_namedPropertyMethod_companyName() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.companyName();
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(CompanyAddress.meta().companyName());
        
        assertThat(test.get()).isNull();
        address.setCompanyName("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_property_String_street() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.property(STREET);
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(CompanyAddress.meta().street());
        
        assertThat(test.get()).isNull();
        address.setStreet("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    @Test
    void test_property_String_companyName() {
        CompanyAddress address = new CompanyAddress();
        Property<String> test = address.property(COMPANY_NAME);
        
        assertThat((Object) test.bean()).isSameAs(address);
        assertThat(test.metaProperty()).isSameAs(CompanyAddress.meta().companyName());
        
        assertThat(test.get()).isNull();
        address.setCompanyName("A");
        assertThat(test.get()).isEqualTo("A");
        test.set("B");
        assertThat(test.get()).isEqualTo("B");
        assertThat(test.put("C")).isEqualTo("B");
        assertThat(test.get()).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_namedMetaPropertyMethod_street() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().street();
        
        assertThat(test.metaBean().beanType()).isEqualTo(CompanyAddress.class);
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

    @Test
    void test_namedMetaPropertyMethod_companyName() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().companyName();
        
        assertThat(test.metaBean().beanType()).isEqualTo(CompanyAddress.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isEqualTo(COMPANY_NAME);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(address)).isNull();
        address.setCompanyName("A");
        assertThat(test.get(address)).isEqualTo("A");
        test.set(address, "B");
        assertThat(test.get(address)).isEqualTo("B");
        assertThat(test.put(address, "C")).isEqualTo("B");
        assertThat(test.get(address)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaProperty_String_street() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().metaProperty(STREET);
        
        assertThat(test.metaBean().beanType()).isEqualTo(CompanyAddress.class);
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

    @Test
    void test_metaProperty_String_companyName() {
        CompanyAddress address = new CompanyAddress();
        MetaProperty<String> test = CompanyAddress.meta().metaProperty(COMPANY_NAME);
        
        assertThat(test.metaBean().beanType()).isEqualTo(CompanyAddress.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.name()).isEqualTo(COMPANY_NAME);
        assertThat(test.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(test.get(address)).isNull();
        address.setCompanyName("A");
        assertThat(test.get(address)).isEqualTo("A");
        test.set(address, "B");
        assertThat(test.get(address)).isEqualTo("B");
        assertThat(test.put(address, "C")).isEqualTo("B");
        assertThat(test.get(address)).isEqualTo("C");
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaProperty_types() {
        MetaProperty<String> test = CompanyAddress.meta().companyName();
        
        assertThat(test.metaBean().beanType()).isEqualTo(CompanyAddress.class);
        assertThat(test.propertyType()).isEqualTo(String.class);
        assertThat(test.propertyGenericType()).isEqualTo(String.class);
    }

    @Test
    void test_metaProperty_annotations() {
        MetaProperty<String> prop = CompanyAddress.meta().companyName();
        List<Annotation> test = prop.annotations();
        
        assertThat(test).hasSize(1);
        assertThat(test.get(0)).isInstanceOf(PropertyDefinition.class);
    }

}
