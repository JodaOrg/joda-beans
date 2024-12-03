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

import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.joda.beans.sample.SubValidateBean;
import org.joda.beans.sample.ValidateBean;
import org.junit.jupiter.api.Test;

/**
 * Test ValidateBean.
 */
class TestValidateBean {

    @Test
    void test_notNull_set() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setFirst(null));
    }

    @Test
    void test_notNull_propertySet() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.first().set(null));
    }

    @Test
    void test_notNull_create() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder()
                        .set("first", null)
                        .set("second", "B")
                        .set("third", "C")
                        .set("fourth", "D")
                        .build());
    }

    @Test
    void test_notNull_create_notIncluded() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder().set("second", "B").set("third", "C").set("fourth", "D").build());
    }

    //-----------------------------------------------------------------------
    @Test
    void test_notBlank_set_null() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setFifth(null));
    }

    @Test
    void test_notBlank_set_empty() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setFifth(" "));
    }

    @Test
    void test_notBlank_propertySet_null() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.fifth().set(null));
    }

    @Test
    void test_notBlank_propertySet_empty() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.fifth().set(" "));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_notEmpty_set_null() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setSecond(null));
    }

    @Test
    void test_notEmpty_set_empty() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setSecond(""));
    }

    @Test
    void test_notEmpty_propertySet_null() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.second().set(null));
    }

    @Test
    void test_notEmpty_propertySet_empty() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.second().set(""));
    }

    @Test
    void test_notEmpty_create_null() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder()
                        .set("first", "A")
                        .set("second", null)
                        .set("third", "C")
                        .set("fourth", "D").build());
    }

    @Test
    void test_notEmpty_create_empty() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder()
                        .set("first", "A")
                        .set("second", "")
                        .set("third", "C")
                        .set("fourth", "D")
                        .build());
    }

    @Test
    void test_notEmpty_create_notIncluded() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder()
                        .set("first", "A")
                        .set("third", "C")
                        .set("fourth", "D")
                        .build());
    }

    //-----------------------------------------------------------------------
    @Test
    void test_static_set() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setThird(null));
    }

    @Test
    void test_static_propertySet() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.third().set(null));
    }

    @Test
    void test_static_create() {
        assertThatIllegalArgumentException()
                .isThrownBy(
                        () -> ValidateBean.meta().builder()
                                .set("first", "A")
                                .set("second", "B")
                                .set("third", "NotC")
                                .set("fourth", "D")
                                .build())
                .withMessage("third");
    }

    @Test
    void test_static_create_notIncluded() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder().set("first", "A").set("second", "B").set("fourth", "D").build());
    }

    //-----------------------------------------------------------------------
    @Test
    void test_bean_set() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.setFourth(null));
    }

    @Test
    void test_bean_propertySet() {
        ValidateBean test = new ValidateBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.fourth().set(null));
    }

    @Test
    void test_bean_create() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ValidateBean.meta().builder()
                        .set("first", "A")
                        .set("second", "B")
                        .set("third", "C")
                        .set("fourth", "NotD")
                        .build())
                .withMessage("fourth");
    }

    @Test
    void test_bean_create_notIncluded() {
        assertThatIllegalArgumentException().isThrownBy(
                () -> ValidateBean.meta().builder().set("first", "A").set("second", "B").set("third", "C").build());
    }

    //-----------------------------------------------------------------------
    @Test
    void test_subbean_create_notIncluded() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> SubValidateBean.meta().builder()
                        .set("first", "A")
                        .set("second", "B")
                        .set("third", "C")
                        .set("fourth", "D")
                        .build())
                .withMessageContaining("sub");
    }

}
