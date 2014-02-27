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
 * Test SingletonGrid.
 */
@Test
public class TestSingletonGrid extends AbstractTestGrid {

    public void test_factory_simple() {
        ImmutableGrid<String> test = ImmutableGrid.of("Hello");
        assertEquals(test.rowCount(), 1);
        assertEquals(test.columnCount(), 1);
        checkGrid(test, 0, 0, "Hello");
        assertEquals(test.toString(), "[1x1:(0,0)=Hello]");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_factory_simple_nullValue() {
        ImmutableGrid.of(null);
    }

    //-----------------------------------------------------------------------
    public void test_factory_full() {
        ImmutableGrid<String> test = ImmutableGrid.of(2, 2, 0, 1, "Hello");
        assertEquals(test.rowCount(), 2);
        assertEquals(test.columnCount(), 2);
        checkGrid(test, 0, 1, "Hello");
        assertEquals(test.toString(), "[2x2:(0,1)=Hello]");
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_factory_full_negativeRowCount() {
        ImmutableGrid.of(-2, 3, 0, 1, "World");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_factory_full_negativeColumnCount() {
        ImmutableGrid.of(2, -3, 0, 1, "World");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_full_negativeRow() {
        ImmutableGrid.of(2, 2, -1, 2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_full_negativeColumn() {
        ImmutableGrid.of(2, 2, 1, -2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_full_rowEqualRowCount() {
        ImmutableGrid.of(1, 1, 1, 0, "World");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_full_columnEqualColumnCount() {
        ImmutableGrid.of(1, 1, 0, 1, "World");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_factory_full_nullValue() {
        ImmutableGrid.of(2, 2, 1, 2, null);
    }

    //-----------------------------------------------------------------------
    public void test_containsValue_Object() {
        ImmutableGrid<String> test = create();
        assertEquals(test.containsValue("Hello"), true);
        assertEquals(test.containsValue("Spicy"), false);
        assertEquals(test.containsValue(""), false);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(Integer.valueOf(6)), false);
    }

    public void test_equalsHashCode() {
        ImmutableGrid<String> test = create();
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(create()), true);
        
        HashGrid<String> hash = HashGrid.create(2, 3);
        hash.put(0, 1, "Hello");
        assertEquals(test.equals(hash), true);
        
        HashGrid<String> colCountDiff = HashGrid.create(2, 4);
        colCountDiff.put(0, 1, "Hello");
        assertEquals(test.equals(colCountDiff), false);
        
        HashGrid<String> valueDiff = HashGrid.create(2, 3);
        valueDiff.put(0, 1, "World");
        assertEquals(test.equals(valueDiff), false);
        
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), 2 ^ Integer.rotateLeft(3, 16) ^ test.cells().hashCode());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_clear() {
        ImmutableGrid<String> test = create();
        test.clear();
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_put() {
        ImmutableGrid<String> test = create();
        test.put(0, 0, "Hello");
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_putAll() {
        ImmutableGrid<String> test = create();
        test.putAll(ImmutableGrid.<String>of());
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_remove() {
        ImmutableGrid<String> test = create();
        test.remove(0, 0);
    }

    private ImmutableGrid<String> create() {
        return ImmutableGrid.of(2, 3, 0, 1, "Hello");
    }

}
