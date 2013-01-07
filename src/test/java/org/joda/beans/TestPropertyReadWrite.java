/*
 *  Copyright 2001-2013 Stephen Colebourne
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
 * Test PropertyReadWrite.
 */
@Test
public class TestPropertyReadWrite {

    public void test_READ_ONLY() {
        assertEquals(PropertyReadWrite.READ_ONLY.isReadable(), true);
        assertEquals(PropertyReadWrite.READ_ONLY.isWritable(), false);
    }

    public void test_READ_WRITE() {
        assertEquals(PropertyReadWrite.READ_WRITE.isReadable(), true);
        assertEquals(PropertyReadWrite.READ_WRITE.isWritable(), true);
    }

    public void test_WRITE_ONLY() {
        assertEquals(PropertyReadWrite.WRITE_ONLY.isReadable(), false);
        assertEquals(PropertyReadWrite.WRITE_ONLY.isWritable(), true);
    }

}
