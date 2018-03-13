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

import static org.junit.Assert.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.NoSuchElementException;

import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmAddress.Builder;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.ImmPersonNonFinal;
import org.joda.beans.sample.ImmSubPersonNonFinal;
import org.joda.beans.sample.ImmSubSubPersonFinal;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;

/**
 * Test property using Person.
 */
public class TestImmutable {

    @Test
    public void test_bean() {
        ImmAddress address = ImmAddress.builder()
                .number(12)
                .street("Park Lane")
                .city("Smallville")
                .owner(ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Lane");
    }

    @Test
    public void test_builder() {
        Builder builder = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane");
        assertEquals(builder.get("number"), 12);
        assertEquals(builder.get("street"), "Park Lane");
        assertEquals(builder.get("city"), null);
        builder.set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build());
        assertEquals(builder.get("number"), 12);
        assertEquals(builder.get("street"), "Park Lane");
        assertEquals(builder.get("city"), "Smallville");
        ImmAddress address = builder.build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Lane");
    }

    @Test
    public void test_with() {
        ImmAddress address = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        address = address.toBuilder().street("Park Road").build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Road");
    }

    //-----------------------------------------------------------------------
    @Test(expected = NoSuchElementException.class)
    public void test_builder_getInvalidPropertyName() {
        BeanBuilder<ImmAddress> builder = ImmAddress.meta().builder();
        try {
            builder.get("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void test_builder_setInvalidPropertyName() {
        BeanBuilder<ImmAddress> builder = ImmAddress.meta().builder();
        try {
            builder.set("Rubbish", "");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_builder_subclass() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        builder.set(ImmPersonNonFinal.meta().forename(), "Bobby");
        builder.set(ImmSubPersonNonFinal.meta().middleName(), "Joe");
        builder.set(ImmSubSubPersonFinal.meta().codeCounts(), ImmutableMultiset.of());
        assertEquals(builder.get("forename"), "Bobby");
        assertEquals(builder.get("middleName"), "Joe");
        assertEquals(builder.get("codeCounts"), ImmutableMultiset.of());
        assertEquals(builder.get(ImmPersonNonFinal.meta().forename()), "Bobby");
        assertEquals(builder.get(ImmSubPersonNonFinal.meta().middleName()), "Joe");
        assertEquals(builder.get(ImmSubSubPersonFinal.meta().codeCounts()), ImmutableMultiset.of());
        ImmSubSubPersonFinal result = builder.build();
        
        assertEquals(result.getForename(), "Bobby");
        assertEquals(result.getMiddleName(), "Joe");
        assertEquals(result.getCodeCounts(), ImmutableMultiset.of());
    }

    @Test(expected = NoSuchElementException.class)
    public void test_builder_subclass_getInvalidPropertyName() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        try {
            builder.get("Rubbish");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    @Test(expected = NoSuchElementException.class)
    public void test_builder_subclass_setInvalidPropertyName() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        try {
            builder.set("Rubbish", "");
        } catch (NoSuchElementException ex) {
            System.out.println(ex.getMessage());
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_builder_defaultValue() {
        ImmPerson person = ImmPerson.builder()
            .forename("A")
            .surname("B")
            .build();
        assertEquals(person.getForename(), "A");
        assertEquals(person.getSurname(), "B");
        assertEquals(person.getNumberOfCars(), 1);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_builder_methodTypes() {
        Calendar cal = Calendar.getInstance();
        GregorianCalendar gcal = new GregorianCalendar(2015, 5, 30);
        ImmutableList<Calendar> listCal = ImmutableList.of(cal);
        ImmutableList<GregorianCalendar> listGcal = ImmutableList.of(gcal);
        ImmutableList<Number> listNumbers = ImmutableList.<Number>of(2d, 5, 3f);
        ImmutableList<Integer> listInts = ImmutableList.<Integer>of(1, 2, 3);
        ImmGuava<Calendar> obj = ImmGuava.<Calendar>builder()
            .list(cal, gcal)
            .list(listCal)
            .listWildExtendsT(listCal)
            .listWildExtendsT(listGcal)
            .listWildExtendsNumber(2d, 5, 3f)
            .listWildExtendsNumber(listNumbers)
            .listWildExtendsNumber(listInts)
            .listWildExtendsComparable((Double) 2d, (Integer) 5, (Float) 3f)
            .listWildExtendsComparable(listInts)
            .build();
        assertEquals(obj.getList(), listCal);
    }

}
