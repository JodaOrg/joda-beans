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

import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmOptionalMeta;
import org.junit.jupiter.api.Test;

import com.google.common.base.Optional;

/**
 * Test ImmOptional.
 */
public class TestOptionalBean {

    @Test
    public void test_optional_empty() {
        ImmOptional test = ImmOptional.builder()
            .optString(Optional.of("A"))
            .optStringGetter("A")
            .build();
        assertThat(test.getOptString()).isEqualTo(Optional.of("A"));
        assertThat(test.getOptStringGetter()).isEqualTo(Optional.of("A"));
        assertThat(test.getOptDoubleGetter()).isEqualTo(Optional.absent());
        assertThat(test.getOptIntGetter()).isEqualTo(Optional.absent());
        assertThat(test.getOptLongGetter()).isEqualTo(Optional.absent());

        // check that meta bean can be assigned to the metaImplements interface
        ImmOptionalMeta meta1 = ImmOptional.meta();
        ImmOptionalMeta meta2 = test.metaBean();
        assertThat(meta1.optString().get(test)).isEqualTo(Optional.of("A"));
        assertThat(meta1.optStringGetter().get(test)).isEqualTo("A");
        assertThat(meta2.optDoubleGetter().get(test)).isNull();
    }

    @Test
    public void test_optional_full() {
        ImmOptional test = ImmOptional.builder()
            .optString(Optional.of("A"))
            .optDoubleGetter(1.2d)
            .optIntGetter(3)
            .optLongGetter(4L)
            .build();
        assertThat(test.getOptString()).isEqualTo(Optional.of("A"));
        assertThat(test.getOptDoubleGetter()).isEqualTo(Optional.of(1.2d));
        assertThat(test.getOptIntGetter()).isEqualTo(Optional.of(3));
        assertThat(test.getOptLongGetter()).isEqualTo(Optional.of(4L));
    }

    @Test
    public void test_optional_property() {
        ImmOptional test = ImmOptional.builder()
            .optStringGetter("A")
            .build();
        assertThat(test.getOptStringGetter()).isEqualTo(Optional.of("A"));
        MetaProperty<Object> mp2 = test.metaBean().metaProperty("optStringGetter");
        assertThat(mp2.propertyType()).isEqualTo(String.class);
        assertThat(mp2.propertyGenericType()).isEqualTo(String.class);
        assertThat(mp2.declaringType()).isEqualTo(ImmOptional.class);
        assertThat(mp2.get(test)).isEqualTo("A");
        assertThat(mp2.style()).isEqualTo(PropertyStyle.IMMUTABLE);
    }

}
