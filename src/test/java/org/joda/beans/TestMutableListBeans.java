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

import org.joda.beans.sample.MutableListFinalBean;
import org.joda.beans.sample.MutableListNonFinalBean;
import org.junit.jupiter.api.Test;

/**
 * Test list with builder.
 */
class TestMutableListBeans {

    @Test
    void test_finalBean_noList() {
        MutableListFinalBean test = MutableListFinalBean.builder().build();
        assertThat(test.getStrings()).isNull();
    }

    @Test
    void test_finalBean_list() {
        MutableListFinalBean test = MutableListFinalBean.builder().strings("A", "B").build();
        assertThat(test.getStrings()).containsExactly("A", "B");
    }

    @Test
    void test_nonFinalBean_noList() {
        MutableListNonFinalBean test = MutableListNonFinalBean.builder().build();
        assertThat(test.getStrings()).isNull();
    }

    @Test
    void test_nonFinalBean_list() {
        MutableListNonFinalBean test = MutableListNonFinalBean.builder().strings("A", "B").build();
        assertThat(test.getStrings()).containsExactly("A", "B");
    }

}
