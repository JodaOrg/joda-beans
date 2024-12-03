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
import org.joda.beans.sample.SimpleAnnotation;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;

/**
 * Test property using Person.
 */
class TestImmutable {

    @Test
    void test_bean() {
        ImmAddress address = ImmAddress.builder()
                .number(12)
                .street("Park Lane")
                .city("Smallville")
                .owner(ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        assertThat(address.getCity()).isEqualTo("Smallville");
        assertThat(address.getStreet()).isEqualTo("Park Lane");
    }

    @Test
    void test_builder() {
        Builder builder = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane");
        assertThat(builder.get("number")).isEqualTo(12);
        assertThat(builder.get("street")).isEqualTo("Park Lane");
        assertThat(builder.get("city")).isNull();
        builder.set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build());
        assertThat(builder.get("number")).isEqualTo(12);
        assertThat(builder.get("street")).isEqualTo("Park Lane");
        assertThat(builder.get("city")).isEqualTo("Smallville");
        ImmAddress address = builder.build();
        
        assertThat(address.getCity()).isEqualTo("Smallville");
        assertThat(address.getStreet()).isEqualTo("Park Lane");
    }

    @Test
    void test_with() {
        ImmAddress address = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        address = address.toBuilder().street("Park Road").build();
        
        assertThat(address.getCity()).isEqualTo("Smallville");
        assertThat(address.getStreet()).isEqualTo("Park Road");
    }

    @Test
    void test_annotations() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();

        assertThat(person.metaBean().numberOfCars().annotationOpt(SimpleAnnotation.class))
                .isPresent()
                .hasValueSatisfying(anno -> {
                    assertThat(anno.second()).isEqualTo("2");
                });
        assertThat(person.metaBean().age()
                .annotationOpt(SimpleAnnotation.class))
                .isPresent()
                .hasValueSatisfying(anno -> {
                    assertThat(anno.first()).isEqualTo("1");
                });
    }

    //-----------------------------------------------------------------------
    @Test
    void test_builder_getInvalidPropertyName() {
        BeanBuilder<ImmAddress> builder = ImmAddress.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.get("Rubbish"));
    }

    @Test
    void test_builder_setInvalidPropertyName() {
        BeanBuilder<ImmAddress> builder = ImmAddress.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.set("Rubbish", ""));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_builder_subclass() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        builder.set(ImmPersonNonFinal.meta().forename(), "Bobby");
        builder.set(ImmSubPersonNonFinal.meta().middleName(), "Joe");
        builder.set(ImmSubSubPersonFinal.meta().codeCounts(), ImmutableMultiset.of());
        assertThat(builder.get("forename")).isEqualTo("Bobby");
        assertThat(builder.get("middleName")).isEqualTo("Joe");
        assertThat(builder.get("codeCounts")).isEqualTo(ImmutableMultiset.of());
        assertThat(builder.get(ImmPersonNonFinal.meta().forename())).isEqualTo("Bobby");
        assertThat(builder.get(ImmSubPersonNonFinal.meta().middleName())).isEqualTo("Joe");
        assertThat(builder.get(ImmSubSubPersonFinal.meta().codeCounts())).isEqualTo(ImmutableMultiset.of());
        ImmSubSubPersonFinal result = builder.build();
        
        assertThat(result.getForename()).isEqualTo("Bobby");
        assertThat(result.getMiddleName()).isEqualTo("Joe");
        assertThat(result.getCodeCounts()).isEmpty();
    }

    @Test
    void test_builder_subclass_getInvalidPropertyName() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.get("Rubbish"));
    }

    @Test
    void test_builder_subclass_setInvalidPropertyName() {
        ImmSubSubPersonFinal.Builder builder = ImmSubSubPersonFinal.meta().builder();
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.set("Rubbish", ""));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_builder_defaultValue() {
        ImmPerson person = ImmPerson.builder()
            .forename("A")
            .surname("B")
            .build();
        assertThat(person.getForename()).isEqualTo("A");
        assertThat(person.getSurname()).isEqualTo("B");
        assertThat(person.getNumberOfCars()).isEqualTo(1);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_builder_methodTypes() {
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
            .listWildExtendsComparable(2d, 5, 3f)
            .listWildExtendsComparable(listInts)
            .build();
        assertThat(obj.getList()).isEqualTo(listCal);
    }

}
