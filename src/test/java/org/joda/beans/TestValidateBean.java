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

import org.joda.beans.sample.SubValidateBean;
import org.joda.beans.sample.ValidateBean;
import org.junit.Test;

/**
 * Test ValidateBean.
 */
public class TestValidateBean {

    @Test(expected = IllegalArgumentException.class)
    public void test_notNull_set() {
        ValidateBean test = new ValidateBean();
        test.setFirst(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notNull_propertySet() {
        ValidateBean test = new ValidateBean();
        test.first().set(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notNull_create() {
        ValidateBean.meta().builder().set("first", null).set("second", "B").set("third", "C").set("fourth", "D").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notNull_create_notIncluded() {
        ValidateBean.meta().builder().set("second", "B").set("third", "C").set("fourth", "D").build();
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_notBlank_set_null() {
        ValidateBean test = new ValidateBean();
        test.setFifth(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notBlank_set_empty() {
        ValidateBean test = new ValidateBean();
        test.setFifth(" ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notBlank_propertySet_null() {
        ValidateBean test = new ValidateBean();
        test.fifth().set(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notBlank_propertySet_empty() {
        ValidateBean test = new ValidateBean();
        test.fifth().set(" ");
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_set_null() {
        ValidateBean test = new ValidateBean();
        test.setSecond(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_set_empty() {
        ValidateBean test = new ValidateBean();
        test.setSecond("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_propertySet_null() {
        ValidateBean test = new ValidateBean();
        test.second().set(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_propertySet_empty() {
        ValidateBean test = new ValidateBean();
        test.second().set("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_create_null() {
        ValidateBean.meta().builder().set("first", "A").set("second", null).set("third", "C").set("fourth", "D").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_create_empty() {
        ValidateBean.meta().builder().set("first", "A").set("second", "").set("third", "C").set("fourth", "D").build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_notEmpty_create_notIncluded() {
        ValidateBean.meta().builder().set("first", "A").set("third", "C").set("fourth", "D").build();
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_static_set() {
        ValidateBean test = new ValidateBean();
        test.setThird(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_static_propertySet() {
        ValidateBean test = new ValidateBean();
        test.third().set(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_static_create() {
        try {
            ValidateBean.meta().builder().set("first", "A").set("second", "B").set("third", "NotC").set("fourth", "D").build();
        } catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "third");
            throw ex;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_static_create_notIncluded() {
        ValidateBean.meta().builder().set("first", "A").set("second", "B").set("fourth", "D").build();
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_bean_set() {
        ValidateBean test = new ValidateBean();
        test.setFourth(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_bean_propertySet() {
        ValidateBean test = new ValidateBean();
        test.fourth().set(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_bean_create() {
        try {
            ValidateBean.meta().builder().set("first", "A").set("second", "B").set("third", "C").set("fourth", "NotD").build();
        } catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "fourth");
            throw ex;
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_bean_create_notIncluded() {
        ValidateBean.meta().builder().set("first", "A").set("second", "B").set("third", "C").build();
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_subbean_create_notIncluded() {
        try {
            SubValidateBean.meta().builder().set("first", "A").set("second", "B").set("third", "C").set("fourth", "D").build();
        } catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage().contains("sub"), true);
            throw ex;
        }
    }

    //-----------------------------------------------------------------------
    public static void checkInTest(Object value, String name) {
        if ("C".equals(value)) {
            return;
        }
        throw new IllegalArgumentException(name);
    }

}
