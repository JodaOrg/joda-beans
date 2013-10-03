/*
 *  Copyright 2001-2013 Stephen Colebourne
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
package org.joda.beans.ser.xml;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.Company;
import org.joda.beans.gen.CompanyAddress;
import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmPerson;
import org.joda.beans.gen.Person;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;

/**
 * Test property roundtrip using XML.
 */
@Test
public class TestAddressXml {

    public void test_writeAddress() {
        Person person = new Person();
        person.setForename("Etienne");
        person.setSurname("Colebourne");
        person.getExtensions().set("interests", "joda");
        person.getExtensions().set("conferenceCount", 21);
        person.getExtensions().set("quality", 'B');
        person.getExtensions().set("company", new Company("OpenGamma"));
        Address address = new Address();
        address.setOwner(person);
        address.setNumber(251);
        address.setStreet("Big Road");
        address.setCity("London & Capital of the World <!>");
        CompanyAddress workAddress = new CompanyAddress();
        workAddress.setCompanyName("OpenGamma");
        workAddress.setNumber(185);
        workAddress.setStreet("Park Street");
        workAddress.setCity("London");
        Address homeAddress = new Address();
        homeAddress.setNumber(251);
        homeAddress.setStreet("Big Road");
        homeAddress.setCity("Bigton");
        person.setMainAddress(workAddress);
        person.getOtherAddressMap().put("home", homeAddress);
        person.getOtherAddressMap().put("work", workAddress);
        person.getOtherAddressMap().put("other", null);
        person.getAddressList().add(homeAddress);
        person.getAddressList().add(null);
        person.getAddressList().add(workAddress);
        person.getAddressesList().add(ImmutableList.of(homeAddress, workAddress));
        
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
//        System.out.println(xml);
        
        Address bean = (Address) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmAddress() {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("A", Arrays.asList("B", "b"));
        Map<String, List<Integer>> map2 = new HashMap<String, List<Integer>>();
        map2.put("A", Arrays.asList(3, 2, 1));
        ImmPerson person = ImmPerson.builder()
            .forename("Etienne")
            .surname("Colebourne")
            .addressList(Arrays.asList(new Address()))
            .codeCounts(ImmutableMultiset.of("A", "A", "B"))
            . build();
        ImmPerson child = ImmPerson.builder()
                .forename("Etiennette")
                .surname("Colebourne")
                . build();
        ImmAddress childAddress = ImmAddress.builder()
                .owner(child)
                .number(185)
                .street("Park Street")
                .city("London")
                .data(new byte[] {64, 65, 66})
                .build();
        ImmAddress address = ImmAddress.builder()
            .owner(person)
            .number(185)
            .street("Park Street")
            .city("London & Capital of the World <!>\n")
            .listInMap(map)
            .listNumericInMap(map2)
            .beanBeanMap(ImmutableMap.of(child, childAddress))
            .build();
        
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
        
        xml = xml.replace("185", "18<!-- comment -->5");
//        System.out.println(xml);
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.xmlReader().read(xml);
///        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

}
