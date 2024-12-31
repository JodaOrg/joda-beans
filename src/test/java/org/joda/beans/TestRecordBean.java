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

import java.lang.invoke.MethodHandles;
import java.util.NoSuchElementException;

import org.joda.beans.impl.RecordBean;
import org.joda.beans.sample.Pair;
import org.joda.beans.sample.RecordStrIntPair;
import org.joda.beans.ser.JodaBeanSer;
import org.junit.jupiter.api.Test;

/**
 * Test RecordBean.
 */
class TestRecordBean {

    private static record StringLongPair(String first, long second) implements RecordBean<StringLongPair> {
        static {
            RecordBean.register(StringLongPair.class, MethodHandles.lookup());
        }
    }

    @Test
    void test_metaBean_public() {
        var test = new RecordStrIntPair("A", 1);
        assertThat(test.first()).isEqualTo("A");
        assertThat(test.second()).isEqualTo(1);

        var meta = test.metaBean();
        assertThat(meta.isBuildable()).isTrue();
        assertThat(meta.beanType()).isEqualTo(RecordStrIntPair.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(2);

        var mp1 = meta.metaProperty("first");
        assertThat(mp1.name()).isEqualTo("first");
        assertThat(mp1.declaringType()).isEqualTo(RecordStrIntPair.class);
        assertThat(mp1.metaBean()).isSameAs(meta);
        assertThat(mp1.get(test)).isEqualTo("A");
        assertThat(mp1.propertyType()).isEqualTo(String.class);
        assertThat(mp1.style()).isEqualTo(PropertyStyle.IMMUTABLE);

        var mp2 = meta.metaProperty("second");
        assertThat(mp2.name()).isEqualTo("second");
        assertThat(mp2.declaringType()).isEqualTo(RecordStrIntPair.class);
        assertThat(mp2.metaBean()).isSameAs(meta);
        assertThat(mp2.get(test)).isEqualTo(1);
        assertThat(mp2.propertyType()).isEqualTo(int.class);
        assertThat(mp2.style()).isEqualTo(PropertyStyle.IMMUTABLE);

        assertThat(mp1).isEqualTo(mp1)
                .isNotEqualTo(mp2)
                .isNotEqualTo("")
                .isNotEqualTo(Pair.meta().first())
                .hasSameHashCodeAs(mp1)
                .doesNotHaveSameHashCodeAs(mp2);

        var builder = meta.builder();
        builder.set("first", "B");
        builder.set("second", 2);
        assertThat(builder.get("first")).isEqualTo("B");
        assertThat(builder.get(mp1)).isEqualTo("B");
        assertThat(builder.build()).isEqualTo(new RecordStrIntPair("B", 2));

        builder.set(mp1, "A");
        assertThat(builder.build()).isEqualTo(new RecordStrIntPair("A", 2));

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.get("foo"));
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.set("foo", ""));

        var json = JodaBeanSer.PRETTY.jsonWriter().write(test);
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        assertThat(parsed).isEqualTo(test);
    }

    @Test
    void test_metaBean_private() {
        var test = new StringLongPair("A", 1L);
        assertThat(test.first()).isEqualTo("A");
        assertThat(test.second()).isEqualTo(1L);

        var meta = test.metaBean();
        assertThat(meta.isBuildable()).isTrue();
        assertThat(meta.beanType()).isEqualTo(StringLongPair.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(2);
        
        var mp1 = meta.metaProperty("first");
        assertThat(mp1.name()).isEqualTo("first");
        assertThat(mp1.declaringType()).isEqualTo(StringLongPair.class);
        assertThat(mp1.metaBean()).isSameAs(meta);
        assertThat(mp1.get(test)).isEqualTo("A");
        assertThat(mp1.propertyType()).isEqualTo(String.class);
        assertThat(mp1.style()).isEqualTo(PropertyStyle.IMMUTABLE);

        var mp2 = meta.metaProperty("second");
        assertThat(mp2.name()).isEqualTo("second");
        assertThat(mp2.declaringType()).isEqualTo(StringLongPair.class);
        assertThat(mp2.metaBean()).isSameAs(meta);
        assertThat(mp2.get(test)).isEqualTo(1L);
        assertThat(mp2.propertyType()).isEqualTo(long.class);
        assertThat(mp2.style()).isEqualTo(PropertyStyle.IMMUTABLE);

        assertThat(mp1).isEqualTo(mp1)
                .isNotEqualTo(mp2)
                .isNotEqualTo("")
                .isNotEqualTo(Pair.meta().first())
                .hasSameHashCodeAs(mp1)
                .doesNotHaveSameHashCodeAs(mp2);

        var builder = meta.builder();
        builder.set("first", "B");
        builder.set("second", 2L);
        assertThat(builder.get("first")).isEqualTo("B");
        assertThat(builder.get(mp1)).isEqualTo("B");
        assertThat(builder.build()).isEqualTo(new StringLongPair("B", 2L));

        builder.set(mp1, "A");
        assertThat(builder.build()).isEqualTo(new StringLongPair("A", 2L));

        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.get("foo"));
        assertThatExceptionOfType(NoSuchElementException.class)
                .isThrownBy(() -> builder.set("foo", ""));

        var json = JodaBeanSer.PRETTY.jsonWriter().write(test);
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        assertThat(parsed).isEqualTo(test);
    }

}
