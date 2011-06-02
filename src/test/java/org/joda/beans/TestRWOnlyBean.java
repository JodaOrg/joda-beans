/*
 *  Copyright 2001-2011 Stephen Colebourne
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

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Test RWOnlyBean.
 */
@Test
public class TestRWOnlyBean {

    public void test_ro() {
        RWOnlyBean test = new RWOnlyBean();
        assertEquals(test.getRo(), null);
        assertEquals(test.propertyGet("ro"), null);
        assertEquals(test.ro().get(), null);
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_wo() {
        RWOnlyBean test = new RWOnlyBean();
        test.setWo("woo");
        test.propertyGet("wo");
    }

    public void test_manualGet() {
        RWOnlyBean test = new RWOnlyBean();
        assertEquals(test.getManualGet(), "goo");
        assertEquals(test.propertyGet("manualGet"), "goo");
        assertEquals(test.manualGet().get(), "goo");
    }

    public void test_derived() {
        RWOnlyBean test = new RWOnlyBean();
        assertEquals(test.getDerived(), "drv");
        assertEquals(test.propertyGet("derived"), "drv");
        assertEquals(test.derived().get(), "drv");
    }

}
