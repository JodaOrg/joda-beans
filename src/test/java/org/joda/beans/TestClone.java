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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;

import org.joda.beans.sample.Address;
import org.joda.beans.sample.ClonePerson;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.NoClone;
import org.junit.jupiter.api.Test;

/**
 * Test property using ClonePerson.
 */
class TestClone {

    @Test
    void test_bean() {
        ClonePerson base = new ClonePerson();
        base.setSurname("Cable");
        base.setMiddleNames(new String[] {"A", "B", "C"});
        base.setFirstNames(Arrays.asList("Vince", "Matey"));
        base.setDateOfBirth(new Date());
        Address address = new Address();
        address.setCity("London");
        base.setAddresses(Arrays.asList(address));
        Company company = new Company();
        company.setCompanyName("Government");
        base.setCompanies(new Company[] {company});
        base.setAmounts(new int[] {1, 2});
        
        ClonePerson cloned = base.clone();
        assertThat(cloned).isEqualTo(base);
        assertThat(cloned.getMiddleNames()).isNotSameAs(base.getMiddleNames());
        assertThat(cloned.getFirstNames()).isNotSameAs(base.getFirstNames());
        assertThat(cloned.getDateOfBirth()).isNotSameAs(base.getDateOfBirth());
        assertThat(cloned.getAddresses()).isNotSameAs(base.getAddresses());
        assertThat(cloned.getAddresses().get(0)).isNotSameAs(base.getAddresses().get(0));
        assertThat(cloned.getCompanies()).isNotSameAs(base.getCompanies());
        assertThat(cloned.getCompanies()[0]).isNotSameAs(base.getCompanies()[0]);
        assertThat(cloned.getAmounts()).isNotSameAs(base.getAmounts());
    }

    @Test
    void test_noclone_on_mutable_bean_option() {
        Class<?> c = NoClone.class;
        Method[] noCloneMethods = c.getDeclaredMethods();

        for (Method method : noCloneMethods) {
            assertThat("clone").isNotEqualTo(method.getName());
        }
    }

}
