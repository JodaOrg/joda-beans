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

import java.util.ArrayList;
import java.util.Currency;
import java.util.NoSuchElementException;

import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.LightImmutable;
import org.joda.beans.sample.MinimalImmutable;
import org.joda.beans.sample.MinimalMutable;
import org.joda.beans.ser.JodaBeanSer;
import org.junit.jupiter.api.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

/**
 * Test style=minimal.
 */
public class TestMinimal {

    @Test
    public void test_immutable() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        MinimalImmutable bean = (MinimalImmutable) MinimalImmutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", MinimalImmutable.meta(), String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();
        
        assertThat(bean.getNumber()).isEqualTo(12);
        assertThat(bean.getTown()).isEqualTo(Optional.absent());
        assertThat(bean.getCity()).isEqualTo("Smallville");
        assertThat(bean.getStreetName()).isEqualTo("Park Lane");
        assertThat(bean.getOwner()).isEqualTo(person);
        assertThat(bean.getList()).isEqualTo(ImmutableList.of());
        
        assertThat(bean.metaBean().beanType()).isEqualTo(MinimalImmutable.class);
        assertThat(bean.metaBean().metaPropertyCount()).isEqualTo(9);
        assertThat(bean.metaBean().metaPropertyExists("number")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("town")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("address")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("foobar")).isFalse();
        
        assertThat(bean.metaBean().metaPropertyExists("place")).isFalse();
        assertThat(bean.metaBean().metaProperty("place")).isEqualTo(bean.metaBean().metaProperty("city"));
        MinimalImmutable builtWithAlias = MinimalImmutable.meta().builder()
                .set("place", "Place")
                .set("street", "Park Lane")
                .set("owner", person)
                .build();
        assertThat(builtWithAlias.getCity()).isEqualTo("Place");
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertThat(mp.propertyType()).isEqualTo(int.class);
        assertThat(mp.declaringType()).isEqualTo(MinimalImmutable.class);
        assertThat(mp.get(bean)).isEqualTo(12);
        assertThat(mp.style()).isEqualTo(PropertyStyle.IMMUTABLE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("town");
        assertThat(mp2.propertyType()).isEqualTo(String.class);
        assertThat(mp2.propertyGenericType()).isEqualTo(String.class);
        assertThat(mp2.declaringType()).isEqualTo(MinimalImmutable.class);
        assertThat(mp2.get(bean)).isNull();
        assertThat(mp2.style()).isEqualTo(PropertyStyle.IMMUTABLE);
        
        MetaProperty<Object> mp3 = bean.metaBean().metaProperty("address");
        assertThat(mp3.propertyType()).isEqualTo(String.class);
        assertThat(mp3.propertyGenericType()).isEqualTo(String.class);
        assertThat(mp3.declaringType()).isEqualTo(MinimalImmutable.class);
        assertThat(mp3.get(bean)).isEqualTo("12 Park Lane Smallville");
        assertThat(mp3.style()).isEqualTo(PropertyStyle.DERIVED);
        
        assertThat(JodaBeanSer.PRETTY.xmlWriter().write(bean)).contains("<currency>USD<");
        assertThat(JodaBeanSer.PRETTY.xmlWriter().write(bean)).doesNotContain("<town>");
        
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> LightImmutable.meta().builder().set(mp3, "Nothing"));
    }

    @Test
    public void test_immutable_order() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        MinimalImmutable bean = (MinimalImmutable) MinimalImmutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", MinimalImmutable.meta(), String.class), "Smallville")
                .set("owner", person)
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();

        ImmutableList<MetaProperty<?>> mps = ImmutableList.copyOf(bean.metaBean().metaPropertyIterable());
        assertThat(mps.get(0).name()).isEqualTo("number");
        assertThat(mps.get(1).name()).isEqualTo("flag");
        assertThat(mps.get(2).name()).isEqualTo("street");
        assertThat(mps.get(3).name()).isEqualTo("town");
        assertThat(mps.get(4).name()).isEqualTo("city");
        assertThat(mps.get(5).name()).isEqualTo("owner");
        assertThat(mps.get(6).name()).isEqualTo("list");
        assertThat(mps.get(7).name()).isEqualTo("currency");
        assertThat(mps.get(8).name()).isEqualTo("address");
    }

    @Test
    public void test_mutable() {
        MinimalMutable bean = (MinimalMutable) MinimalMutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", MinimalMutable.meta(), String.class), "Smallville")
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();
        
        assertThat(bean.getNumber()).isEqualTo(12);
        assertThat(bean.getTown()).isEqualTo(Optional.absent());
        assertThat(bean.getCity()).isEqualTo("Smallville");
        assertThat(bean.getStreetName()).isEqualTo("Park Lane");
        assertThat(bean.getList()).isEqualTo(ImmutableList.of());
        assertThat(bean.getCurrency()).isEqualTo(Optional.of(Currency.getInstance("USD")));
        
        bean.setCity("Nodnol");
        assertThat(bean.getCity()).isEqualTo("Nodnol");
        
        bean.property("city").set("Paris");
        assertThat(bean.getCity()).isEqualTo("Paris");
        
        bean.metaBean().metaProperty("city").set(bean, "London");
        assertThat(bean.getCity()).isEqualTo("London");
        
        assertThat(bean.metaBean().beanType()).isEqualTo(MinimalMutable.class);
        assertThat(bean.metaBean().metaPropertyCount()).isEqualTo(8);
        assertThat(bean.metaBean().metaPropertyExists("number")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("town")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("address")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("foobar")).isFalse();
        
        assertThat(bean.metaBean().metaPropertyExists("place")).isFalse();
        assertThat(bean.metaBean().metaProperty("place")).isEqualTo(bean.metaBean().metaProperty("city"));
        MinimalMutable builtWithAlias = MinimalMutable.meta().builder()
                .set("place", "Place")
                .set("street", "Park Lane")
                .build();
        assertThat(builtWithAlias.getCity()).isEqualTo("Place");
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertThat(mp.propertyType()).isEqualTo(int.class);
        assertThat(mp.declaringType()).isEqualTo(MinimalMutable.class);
        assertThat(mp.get(bean)).isEqualTo(12);
        assertThat(mp.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("currency");
        assertThat(mp2.propertyType()).isEqualTo(Currency.class);
        assertThat(mp2.propertyGenericType()).isEqualTo(Currency.class);
        assertThat(mp2.declaringType()).isEqualTo(MinimalMutable.class);
        assertThat(mp2.get(bean)).isEqualTo(Currency.getInstance("USD"));
        assertThat(mp2.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp3 = bean.metaBean().metaProperty("address");
        assertThat(mp3.propertyType()).isEqualTo(String.class);
        assertThat(mp3.propertyGenericType()).isEqualTo(String.class);
        assertThat(mp3.declaringType()).isEqualTo(MinimalMutable.class);
        assertThat(mp3.get(bean)).isEqualTo("12 Park Lane London");
        assertThat(mp3.style()).isEqualTo(PropertyStyle.DERIVED);
        
        assertThat(JodaBeanSer.PRETTY.xmlWriter().write(bean)).contains("<currency>USD<");
        assertThat(JodaBeanSer.PRETTY.xmlWriter().write(bean)).doesNotContain("<town>");
        
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> LightImmutable.meta().builder().set(mp3, "Nothing"));
    }

    @Test
    public void test_mutable_order() {
        MinimalMutable bean = (MinimalMutable) MinimalMutable.meta().builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", MinimalMutable.meta(), String.class), "Smallville")
                .set("list", new ArrayList<String>())
                .set("currency", Currency.getInstance("USD"))
                .build();

        ImmutableList<MetaProperty<?>> mps = ImmutableList.copyOf(bean.metaBean().metaPropertyIterable());
        assertThat(mps.get(0).name()).isEqualTo("number");
        assertThat(mps.get(1).name()).isEqualTo("flag");
        assertThat(mps.get(2).name()).isEqualTo("street");
        assertThat(mps.get(3).name()).isEqualTo("town");
        assertThat(mps.get(4).name()).isEqualTo("city");
        assertThat(mps.get(5).name()).isEqualTo("list");
        assertThat(mps.get(6).name()).isEqualTo("currency");
        assertThat(mps.get(7).name()).isEqualTo("address");
    }

}
