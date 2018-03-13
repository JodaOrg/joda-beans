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

import static org.junit.Assert.assertEquals;

import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmTolerance;
import org.joda.beans.sample.Person;
import org.junit.Before;
import org.junit.Test;

/**
 * Test BeanAssert.
 */
public class TestBeanAssert {

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
    public void test_same() {
        BeanAssert.assertBeanEquals(person1, person1);
    }

    @Test
    public void test_equal() {
        BeanAssert.assertBeanEquals(person1, person2);
    }

    @Test
    public void test_bean_oneField() {
        person2.setForename("Bug1");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_twoFields() {
        person2.setForename("Bug1");
        person2.setSurname("Bug2");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>\n.surname: Content differs, expected String <Cable> but was <Bug2>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_bug_embedded() {
        person2.getAddressList().get(0).setNumber(234);
        person2.getMainAddress().setCity("Bug3");
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.addressList[0].number: Content differs, expected Integer <12> but was <234>\n.mainAddress.city: Content differs, expected String <Gamesville> but was <Bug3>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_sizes() {
        Address address = new Address();
        address.setCity("Nowhere");
        person2.getOtherAddressMap().put("Bug", address);
        person2.getAddressList().clear();
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.addressList: List size differs, expected 1 but was 0\n.otherAddressMap: Map size differs, expected 1 but was 2", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_map() {
        person2.getOtherAddressMap().get("Home").setCity("Bug");
        person2.getOtherAddressMap().get("Home").setOwner(new Person());
        try {
            BeanAssert.assertBeanEquals(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.otherAddressMap[Home].city: Content differs, expected String <Skyton> but was <Bug>\n.otherAddressMap[Home].owner: Expected null, but was Person <Person{forename=null, surname=null, numberOfCars=0, addre...>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_beanFullDetail_twoFields() {
        person2.setForename("Bug1");
        person2.setSurname("Bug2");
        try {
            BeanAssert.assertBeanEqualsFullDetail(person1, person2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.forename: Content differs, expected String <Vince> but was <Bug1>\n.surname: Content differs, expected String <Cable> but was <Bug2>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_bean_oneField_double() {
        ImmTolerance t1 = ImmTolerance.builder().value(0.015d).build();
        ImmTolerance t2 = ImmTolerance.builder().value(0.016d).build();
        try {
            BeanAssert.assertBeanEquals(t1, t2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.value: Content differs, expected Double <0.015> but was <0.016>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_oneField_double_withinToleranceUp() {
        ImmTolerance t1 = ImmTolerance.builder().value(0.015d).build();
        ImmTolerance t2 = ImmTolerance.builder().value(0.0151d).build();
        BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
    }

    @Test
    public void test_bean_oneField_double_withinToleranceDown() {
        ImmTolerance t1 = ImmTolerance.builder().value(0.015d).build();
        ImmTolerance t2 = ImmTolerance.builder().value(0.0149d).build();
        BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
    }

    @Test
    public void test_bean_oneField_double_notInTolerance() {
        ImmTolerance t1 = ImmTolerance.builder().value(0.015d).build();
        ImmTolerance t2 = ImmTolerance.builder().value(0.0153d).build();
        try {
            BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.value: Double values differ by more than allowed tolerance, expected Double <0.015> but was <0.0153>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_bean_oneField_doubleArray() {
        ImmTolerance t1 = ImmTolerance.builder().array(new double[] {0.015d, 0.015d}).build();
        ImmTolerance t2 = ImmTolerance.builder().array(new double[] {0.015d, 0.016d}).build();
        try {
            BeanAssert.assertBeanEquals(t1, t2);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.array: Content differs, expected " +
            		"double[] <[0.015, 0.015]> but was <[0.015, 0.016]>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    @Test
    public void test_bean_oneField_doubleArray_withinToleranceUp() {
        ImmTolerance t1 = ImmTolerance.builder().array(new double[] {0.015d, 0.015d}).build();
        ImmTolerance t2 = ImmTolerance.builder().array(new double[] {0.015d, 0.0151d}).build();
        BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
    }

    @Test
    public void test_bean_oneField_doubleArray_withinToleranceDown() {
        ImmTolerance t1 = ImmTolerance.builder().array(new double[] {0.015d, 0.015d}).build();
        ImmTolerance t2 = ImmTolerance.builder().array(new double[] {0.015d, 0.0149d}).build();
        BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
    }

    @Test
    public void test_bean_oneField_doubleArray_notInTolerance() {
        ImmTolerance t1 = ImmTolerance.builder().array(new double[] {0.015d, 0.015d}).build();
        ImmTolerance t2 = ImmTolerance.builder().array(new double[] {0.015d, 0.0153d}).build();
        try {
            BeanAssert.assertBeanEquals(t1, t2, 0.0002d);
        } catch (BeanComparisonError ex) {
            assertEquals("Bean did not equal expected. Differences:\n.array: Double arrays differ by " +
        		"more than allowed tolerance, expected double[] <[0.015, 0.015]> but was <[0.015, 0.0153]>", ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

}
