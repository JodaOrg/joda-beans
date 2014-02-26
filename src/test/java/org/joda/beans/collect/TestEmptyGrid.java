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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Test EmptyGrid.
 */
@Test
public class TestEmptyGrid {

    public void test_factory() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        assertEquals(test.rowCount(), 0);
        assertEquals(test.columnCount(), 0);
        assertEquals(test.exists(0, 0), false);
        assertEquals(test.exists(-1, 0), false);
        assertEquals(test.exists(0, -1), false);
        assertEquals(test.exists(1, 0), false);
        assertEquals(test.exists(0, 1), false);
        assertEquals(test.exists(1, 1), false);
        assertEquals(test.isEmpty(), true);
        assertEquals(test.size(), 0);
        assertEquals(test.contains(0, 0), false);
        assertEquals(test.get(0, 0), null);
        assertEquals(test.cells() instanceof ImmutableSortedSet, true);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values() instanceof ImmutableList, true);
        assertEquals(test.values().size(), 0);
        assertEquals(test.toString(), "[0x0:]");
    }

    //-----------------------------------------------------------------------
    public void test_containsValue_Object() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        assertEquals(test.containsValue("Hello"), false);
        assertEquals(test.containsValue(""), false);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(Integer.valueOf(6)), false);
    }

    public void test_equalsHashCode_0x0() {
        ImmutableGrid<String> test = ImmutableGrid.of();
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(ImmutableGrid.of()), true);
        assertEquals(test.equals(HashGrid.create(0, 0)), true);
        assertEquals(test.equals(HashGrid.create(1, 1)), false);
        assertEquals(test.equals(ArrayGrid.create(0, 0)), true);
        assertEquals(test.equals(ArrayGrid.create(1, 1)), false);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), test.cells().hashCode());
    }

    public void test_equalsHashCode_1x2() {
        ImmutableGrid<String> test = ImmutableGrid.of(1, 2);
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(ImmutableGrid.of(1, 2)), true);
        assertEquals(test.equals(HashGrid.create(1, 2)), true);
        assertEquals(test.equals(HashGrid.create(1, 1)), false);
        assertEquals(test.equals(ArrayGrid.create(1, 2)), true);
        assertEquals(test.equals(ArrayGrid.create(1, 1)), false);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), 1 ^ Integer.rotateLeft(2, 16) ^ test.cells().hashCode());
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
