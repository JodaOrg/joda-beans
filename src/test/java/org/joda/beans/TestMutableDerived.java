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
        assertEquals(test.getBaseBeanString(), "HopeNotHate");
        assertEquals(test.metaBean().metaPropertyCount(), 1);
        assertEquals(test.metaBean().metaPropertyMap().keySet().iterator().next(), "baseBeanString");
        assertEquals(test.metaBean().baseBeanString().get(test), "HopeNotHate");

        test.metaBean().baseBeanString().set(test, "Now");
        assertEquals(test.getBaseBeanString(), "Now");
        assertEquals(test.metaBean().baseBeanString().get(test), "Now");

        test.metaBean().baseBeanString().setString(test, "Please");
        assertEquals(test.getBaseBeanString(), "Please");
        assertEquals(test.metaBean().baseBeanString().get(test), "Please");
    }

    @Test
    public void test_mutableDerivedBean_builder() {
        MutableDerivedBean test = (MutableDerivedBean) MutableDerivedBean.builder()
                .set("baseBeanString", "HopeNotHate")
                .build();
        assertEquals(test.getBaseBeanString(), "HopeNotHate");
        assertEquals(test.metaBean().metaPropertyCount(), 1);
        assertEquals(test.metaBean().metaPropertyMap().keySet().iterator().next(), "baseBeanString");
        assertEquals(test.metaBean().baseBeanString().get(test), "HopeNotHate");
    }

}
