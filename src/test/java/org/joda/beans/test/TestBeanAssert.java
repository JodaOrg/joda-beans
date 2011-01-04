/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import org.joda.beans.Address;
import org.joda.beans.Person;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test BeanAssert.
 */
@Test
public class TestBeanAssert {

    private Person person1;
    private Person person2;

    @BeforeMethod
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

    public void test_same() {
        BeanAssert.assertBeanEquals(person1, person1);
    }

    public void test_equal() {
        BeanAssert.assertBeanEquals(person1, person2);
    }

    public void test_bean_oneField() {
        person2.setForename("Bug1");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    public void test_bean_twoFields() {
        person2.setForename("Bug1");
        person2.setSurname("Bug2");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>\n.surname: Content differs, expected String <Cable> but was <Bug2>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    public void test_bean_bug_embedded() {
        person2.getAddressList().get(0).setNumber(234);
        person2.getMainAddress().setCity("Bug3");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.addressList[0].number: Content differs, expected Integer <12> but was <234>\n.mainAddress.city: Content differs, expected String <Gamesville> but was <Bug3>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    public void test_bean_sizes() {
        Address address = new Address();
        address.setCity("Nowhere");
        person2.getOtherAddressMap().put("Bug", address);
        person2.getAddressList().clear();
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.addressList: List size differs, expected 1 but was 0\n.otherAddressMap: Map size differs, expected 1 but was 2", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    public void test_bean_map() {
        person2.getOtherAddressMap().get("Home").setCity("Bug");
        person2.getOtherAddressMap().get("Home").setOwner(new Person());
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.otherAddressMap[Home].city: Content differs, expected String <Skyton> but was <Bug>\n.otherAddressMap[Home].owner: Expected null, but was Person <Person{addressList=[], forename=null, numberOfCars=0, sur...>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    public void test_beanFullDetail_twoFields() {
        person2.setForename("Bug1");
        person2.setSurname("Bug2");
        try {
            BeanAssert.assertBeanEqualsFullDetail(person1, person2);
        } catch (BeanComparisonError ex) {
            Assert.assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>\n.surname: Content differs, expected String <Cable> but was <Bug2>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

}
