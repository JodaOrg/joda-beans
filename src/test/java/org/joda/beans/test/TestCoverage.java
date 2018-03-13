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

import org.joda.beans.sample.Address;
import org.joda.beans.sample.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * Test code coverage helper.
 */
public class TestCoverage {

    private Person person1;
    private Person person2;

    @Before
    public void setUp() {
        person1 = new Person();
        person1.setForename("Vince");
        person1.setSurname("Cable");
        person1.setNumberOfCars(1);
        person1.getAddressList().add(new Address());
        person1.getAddressList().get(0).setNumber(12);
        person1.getAddressList().get(0).setStreet("Play Street");
        person1.getAddressList().get(0).setCity("Toytown");
        person1.getAddressList().get(0).setOwner(new Person());
        person1.getAddressList().get(0).getOwner().setForename("Nick");
        person1.getAddressList().get(0).getOwner().setSurname("Clegg");
        person1.setMainAddress(new Address());
        person1.getMainAddress().setStreet("Party Road");
        person1.getMainAddress().setCity("Gamesville");
        person1.getMainAddress().setOwner(new Person());
        person1.getMainAddress().getOwner().setForename("Simon");
        person1.getMainAddress().getOwner().setForename("Hughes");
        person1.getOtherAddressMap().put("Home", new Address());
        person1.getOtherAddressMap().get("Home").setNumber(999);
        person1.getOtherAddressMap().get("Home").setStreet("Upper Lane");
        person1.getOtherAddressMap().get("Home").setCity("Skyton");
        
        person2 = new Person();
        person2.setForename("Vince");
        person2.setSurname("Cable");
        person2.setNumberOfCars(1);
        person2.getAddressList().add(new Address());
        person2.getAddressList().get(0).setNumber(12);
        person2.getAddressList().get(0).setStreet("Play Street");
        person2.getAddressList().get(0).setCity("Toytown");
        person2.getAddressList().get(0).setOwner(new Person());
        person2.getAddressList().get(0).getOwner().setForename("Nick");
        person2.getAddressList().get(0).getOwner().setSurname("Clegg");
        person2.setMainAddress(new Address());
        person2.getMainAddress().setStreet("Party Road");
        person2.getMainAddress().setCity("Gamesville");
        person2.getMainAddress().setOwner(new Person());
        person2.getMainAddress().getOwner().setForename("Simon");
        person2.getMainAddress().getOwner().setForename("Hughes");
        person2.getOtherAddressMap().put("Home", new Address());
        person2.getOtherAddressMap().get("Home").setNumber(999);
        person2.getOtherAddressMap().get("Home").setStreet("Upper Lane");
        person2.getOtherAddressMap().get("Home").setCity("Skyton");
    }

    @Test
    public void test_coverage() {
        JodaBeanTests.coverMutableBean(person1);
    }

}
