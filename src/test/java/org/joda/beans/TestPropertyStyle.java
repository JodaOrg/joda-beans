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

import org.junit.Test;

/**
 * Test PropertyStyle.
 */
public class TestPropertyStyle {

    @Test
    public void test_READ_ONLY() {
        assertEquals(PropertyStyle.READ_ONLY.isReadable(), true);
        assertEquals(PropertyStyle.READ_ONLY.isWritable(), false);
        assertEquals(PropertyStyle.READ_ONLY.isBuildable(), false);
        assertEquals(PropertyStyle.READ_ONLY.isReadOnly(), true);
        assertEquals(PropertyStyle.READ_ONLY.isDerived(), false);
        assertEquals(PropertyStyle.READ_ONLY.isSerializable(), false);
    }

    @Test
    public void test_READ_WRITE() {
        assertEquals(PropertyStyle.READ_WRITE.isReadable(), true);
        assertEquals(PropertyStyle.READ_WRITE.isWritable(), true);
        assertEquals(PropertyStyle.READ_WRITE.isBuildable(), true);
        assertEquals(PropertyStyle.READ_WRITE.isReadOnly(), false);
        assertEquals(PropertyStyle.READ_WRITE.isDerived(), false);
        assertEquals(PropertyStyle.READ_WRITE.isSerializable(), true);
    }

    @Test
    public void test_WRITE_ONLY() {
        assertEquals(PropertyStyle.WRITE_ONLY.isReadable(), false);
        assertEquals(PropertyStyle.WRITE_ONLY.isWritable(), true);
        assertEquals(PropertyStyle.WRITE_ONLY.isBuildable(), true);
        assertEquals(PropertyStyle.WRITE_ONLY.isReadOnly(), false);
        assertEquals(PropertyStyle.WRITE_ONLY.isDerived(), false);
        assertEquals(PropertyStyle.WRITE_ONLY.isSerializable(), false);
    }

    @Test
    public void test_DERIVED() {
        assertEquals(PropertyStyle.DERIVED.isReadable(), true);
        assertEquals(PropertyStyle.DERIVED.isWritable(), false);
        assertEquals(PropertyStyle.DERIVED.isBuildable(), false);
        assertEquals(PropertyStyle.DERIVED.isReadOnly(), true);
        assertEquals(PropertyStyle.DERIVED.isDerived(), true);
        assertEquals(PropertyStyle.DERIVED.isSerializable(), false);
    }

    @Test
    public void test_IMMUTABLE() {
        assertEquals(PropertyStyle.IMMUTABLE.isReadable(), true);
        assertEquals(PropertyStyle.IMMUTABLE.isWritable(), false);
        assertEquals(PropertyStyle.IMMUTABLE.isBuildable(), true);
        assertEquals(PropertyStyle.IMMUTABLE.isReadOnly(), true);
        assertEquals(PropertyStyle.IMMUTABLE.isDerived(), false);
        assertEquals(PropertyStyle.IMMUTABLE.isSerializable(), true);
    }

}
