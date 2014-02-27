/*
 *  Copyright 2001-2014 Stephen Colebourne
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
package org.joda.beans.collect;

import static org.testng.Assert.assertEquals;

import org.testng.annotations.Test;

/**
 * Test SparseImmutableGrid.
 */
@Test
public class TestSparseImmutableGrid extends AbstractTestGrid {

    public void test_factory_copyOf_Grid() {
        HashGrid<String> hash = HashGrid.create(2, 3);
        hash.put(0, 0, "Hello");
        hash.put(0, 1, "World");
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash);
        assertEquals(test.rowCount(), 2);
        assertEquals(test.columnCount(), 3);
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
        assertEquals(test.toString(), "[2x3:(0,0)=Hello, (0,1)=World]");
    }

    public void test_factory_copyOfDeriveCounts() {
        HashGrid<String> hash = HashGrid.create(2, 2);
        hash.put(0, 0, "Hello");
        hash.put(0, 1, "World");
        ImmutableGrid<String> test = ImmutableGrid.copyOfDeriveCounts(hash.cells());
        assertEquals(test.rowCount(), 1);
        assertEquals(test.columnCount(), 2);
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
        assertEquals(test.toString(), "[1x2:(0,0)=Hello, (0,1)=World]");
    }

    //-----------------------------------------------------------------------
    public void test_containsValue_Object() {
        HashGrid<String> hash = HashGrid.create(2, 2);
        hash.put(0, 0, "Hello");
        hash.put(0, 1, "World");
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash);
        assertEquals(test.containsValue("Hello"), true);
        assertEquals(test.containsValue("World"), true);
        assertEquals(test.containsValue("Spicy"), false);
        assertEquals(test.containsValue(""), false);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(Integer.valueOf(6)), false);
    }

    public void test_equalsHashCode() {
        HashGrid<String> hash = HashGrid.create(2, 3);
        hash.put(0, 0, "Hello");
        hash.put(0, 1, "World");
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash);
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(ImmutableGrid.copyOf(hash)), true);
        assertEquals(test.equals(hash), true);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), 2 ^ Integer.rotateLeft(3, 16) ^ test.cells().hashCode());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_clear() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        test.clear();
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_put() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        test.put(0, 0, "Hello");
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_putAll() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        test.putAll(ImmutableGrid.<String>of());
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_remove() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        test.remove(0, 0);
    }

}
