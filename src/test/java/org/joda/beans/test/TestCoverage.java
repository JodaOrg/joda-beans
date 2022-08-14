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
package org.joda.beans.test;

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.joda.beans.sample.Address;
import org.joda.beans.sample.MutableBaseBean;
import org.joda.beans.sample.MutableDerivedBean;
import org.joda.beans.sample.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * Test code coverage helper.
 */
public class TestCoverage {

    private Person person;
    private MutableBaseBean mutableBase;
    private MutableDerivedBean mutableDerived;

    @Before
    public void setUp() {
        person = new Person();
        person.setForename("Vince");
        person.setSurname("Cable");
        person.setNumberOfCars(1);
        person.getAddressList().add(new Address());
        person.getAddressList().get(0).setNumber(12);
        person.getAddressList().get(0).setStreet("Play Street");
        person.getAddressList().get(0).setCity("Toytown");
        person.getAddressList().get(0).setOwner(new Person());
        person.getAddressList().get(0).getOwner().setForename("Nick");
        person.getAddressList().get(0).getOwner().setSurname("Clegg");
        person.setMainAddress(new Address());
        person.getMainAddress().setStreet("Party Road");
        person.getMainAddress().setCity("Gamesville");
        person.getMainAddress().setOwner(new Person());
        person.getMainAddress().getOwner().setForename("Simon");
        person.getMainAddress().getOwner().setForename("Hughes");
        person.getOtherAddressMap().put("Home", new Address());
        person.getOtherAddressMap().get("Home").setNumber(999);
        person.getOtherAddressMap().get("Home").setStreet("Upper Lane");
        person.getOtherAddressMap().get("Home").setCity("Skyton");

        mutableBase = MutableBaseBean.builder()
                .baseBeanString("HopeNotHate")
                .build();

        mutableDerived = (MutableDerivedBean) MutableDerivedBean.builder()
                .baseBeanString("HopeNotHate")
                .build();
    }

    @Test
    public void test_coveragePerson() {
        assertThatNoException().isThrownBy(() -> JodaBeanTests.coverMutableBean(person));
    }

    @Test
    public void test_coverageMutableBase() {
        assertThatNoException().isThrownBy(() -> JodaBeanTests.coverMutableBean(mutableBase));
    }

    @Test
    public void test_coverageMutableDerived() {
        assertThatNoException().isThrownBy(() -> JodaBeanTests.coverMutableBean(mutableDerived));
    }

}
