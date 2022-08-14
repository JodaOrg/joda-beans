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

import org.joda.beans.sample.MutableDerivedBean;
import org.junit.Test;

/**
 * Test mutable derived beans.
 */
public class TestMutableDerived {

    @Test
    public void test_mutableDerivedBean() {
        MutableDerivedBean test = (MutableDerivedBean) MutableDerivedBean.builder()
                .baseBeanString("HopeNotHate")
                .build();
        assertThat(test.getBaseBeanString()).isEqualTo("HopeNotHate");
        assertThat(test.metaBean().metaPropertyCount()).isEqualTo(1);
        assertThat(test.metaBean().metaPropertyMap().keySet().iterator().next()).isEqualTo("baseBeanString");
        assertThat(test.metaBean().baseBeanString().get(test)).isEqualTo("HopeNotHate");

        test.metaBean().baseBeanString().set(test, "Now");
        assertThat(test.getBaseBeanString()).isEqualTo("Now");
        assertThat(test.metaBean().baseBeanString().get(test)).isEqualTo("Now");

        test.metaBean().baseBeanString().setString(test, "Please");
        assertThat(test.getBaseBeanString()).isEqualTo("Please");
        assertThat(test.metaBean().baseBeanString().get(test)).isEqualTo("Please");
    }

    @Test
    public void test_mutableDerivedBean_builder() {
        MutableDerivedBean test = (MutableDerivedBean) MutableDerivedBean.builder()
                .set("baseBeanString", "HopeNotHate")
                .build();
        assertThat(test.getBaseBeanString()).isEqualTo("HopeNotHate");
        assertThat(test.metaBean().metaPropertyCount()).isEqualTo(1);
        assertThat(test.metaBean().metaPropertyMap().keySet().iterator().next()).isEqualTo("baseBeanString");
        assertThat(test.metaBean().baseBeanString().get(test)).isEqualTo("HopeNotHate");
    }

}
