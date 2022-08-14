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

import java.util.List;
import java.util.NoSuchElementException;

import org.joda.beans.sample.AbstractResult;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.AddressResult;
import org.joda.beans.sample.CompanyAddress;
import org.joda.beans.sample.CompanyAddressMidResult;
import org.joda.beans.sample.CompanyAddressResult;
import org.junit.jupiter.api.Test;

/**
 * Test property using Person.
 */
public class TestResult {

    @Test
    public void test_bean() {
        Bean test = new AddressResult();
        
        assertThat(test.metaBean()).isEqualTo(AddressResult.meta());
        
        assertThat(test.propertyNames().contains("docs")).isTrue();
        assertThat(test.property("docs").name()).isEqualTo("docs");
        assertThat(test.toString()).isEqualTo("AddressResult{docs=null, resultType=Address}");
    }

    @Test
    public void test_bean_invalidPropertyName() {
        Bean test = AddressResult.meta().builder().build();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.property("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_metaBean() {
        MetaBean test = AddressResult.meta();
        assertThat(test.beanType()).isEqualTo(AddressResult.class);
        assertThat(test.beanName()).isEqualTo(AddressResult.class.getName());
        assertThat(test.metaPropertyCount()).isEqualTo(2);
        assertThat(test.metaPropertyExists("docs")).isTrue();
        assertThat(test.metaProperty("docs").name()).isEqualTo("docs");
        assertThat(test.metaPropertyExists("resultType")).isTrue();
        assertThat(test.metaProperty("resultType").name()).isEqualTo("resultType");
    }

    @Test
    public void test_metaBean_invalidPropertyName() {
        MetaBean test = AddressResult.meta();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> test.metaProperty("Rubbish"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_genericType_abstract() {
        @SuppressWarnings("unchecked")
        AbstractResult.Meta<Address> test = AbstractResult.meta();
        assertThat(test.docs().propertyType()).isEqualTo(List.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), AbstractResult.class)).isEqualTo(Address.class);
    }

    @Test
    public void test_genericType_Address() {
        AddressResult obj = new AddressResult();
        AddressResult.Meta test = AddressResult.meta();
        assertThat(test.docs().propertyType()).isEqualTo(List.class);
        assertThat(JodaBeanUtils.collectionType(obj.docs())).isEqualTo(Address.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), AddressResult.class)).isEqualTo(Address.class);
    }

    @Test
    public void test_genericType_CompanyAddress() {
        CompanyAddressResult obj = new CompanyAddressResult();
        CompanyAddressResult.Meta test = CompanyAddressResult.meta();
        assertThat(test.docs().propertyType()).isEqualTo(List.class);
        assertThat(JodaBeanUtils.collectionType(obj.docs())).isEqualTo(CompanyAddress.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), test.docs().declaringType())).isEqualTo(Address.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), CompanyAddressResult.class)).isEqualTo(CompanyAddress.class);
    }

    @Test
    public void test_genericType_CompanyAddressMid() {
        CompanyAddressMidResult obj = new CompanyAddressMidResult();
        CompanyAddressMidResult.Meta test = CompanyAddressMidResult.meta();
        assertThat(test.docs().propertyType()).isEqualTo(List.class);
        assertThat(JodaBeanUtils.collectionType(obj.docs())).isEqualTo(CompanyAddress.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), test.docs().declaringType())).isEqualTo(Address.class);
        assertThat(JodaBeanUtils.collectionType(test.docs(), CompanyAddressResult.class)).isEqualTo(CompanyAddress.class);
    }

}
