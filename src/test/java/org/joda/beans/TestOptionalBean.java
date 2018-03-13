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

import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.RWOnlyBean;
import org.junit.Test;

import com.google.common.base.Optional;

/**
 * Test ImmOptional.
 */
public class TestOptionalBean extends RWOnlyBean {

    @Test
    public void test_optional_empty() {
        ImmOptional test = ImmOptional.builder()
            .optString(Optional.of("A"))
            .build();
        assertEquals(test.getOptString(), Optional.of("A"));
        assertEquals(test.getOptDoubleGetter(), Optional.absent());
        assertEquals(test.getOptIntGetter(), Optional.absent());
        assertEquals(test.getOptLongGetter(), Optional.absent());
    }

    @Test
    public void test_optional_full() {
        ImmOptional test = ImmOptional.builder()
            .optString(Optional.of("A"))
            .optDoubleGetter(1.2d)
            .optIntGetter(3)
            .optLongGetter(4L)
            .build();
        assertEquals(test.getOptString(), Optional.of("A"));
        assertEquals(test.getOptDoubleGetter(), Optional.of(1.2d));
        assertEquals(test.getOptIntGetter(), Optional.of(3));
        assertEquals(test.getOptLongGetter(), Optional.of(4L));
    }

    @Test
    public void test_optional_property() {
        ImmOptional test = ImmOptional.builder()
            .optStringGetter("A")
            .build();
        assertEquals(test.getOptStringGetter(), Optional.of("A"));
        MetaProperty<Object> mp2 = test.metaBean().metaProperty("optStringGetter");
        assertEquals(mp2.propertyType(), String.class);
        assertEquals(mp2.propertyGenericType(), String.class);
        assertEquals(mp2.declaringType(), ImmOptional.class);
        assertEquals(mp2.get(test), "A");
        assertEquals(mp2.style(), PropertyStyle.IMMUTABLE);
    }

}
