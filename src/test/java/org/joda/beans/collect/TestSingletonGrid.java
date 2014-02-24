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
 * Test SingletonGrid.
 */
@Test
public class TestSingletonGrid {

    public void test_grid() {
        ImmutableGrid<String> test = ImmutableGrid.of(0, 1, "Hello");
        assertEquals(test.isEmpty(), false);
        assertEquals(test.size(), 1);
        assertEquals(test.contains(0, 0), false);
        assertEquals(test.contains(0, 1), true);
        assertEquals(test.contains(1, 0), false);
        assertEquals(test.containsValue(""), false);
        assertEquals(test.containsValue("Hello"), true);
        assertEquals(test.get(0, 0), null);
        assertEquals(test.get(0, 1), "Hello");
        assertEquals(test.get(1, 0), null);
        assertEquals(test.cells() instanceof ImmutableSortedSet, true);
        assertEquals(test.cells().size(), 1);
        assertEquals(test.cells().first().getRow(), 0);
        assertEquals(test.cells().first().getColumn(), 1);
        assertEquals(test.cells().first().getValue(), "Hello");
        assertEquals(test.cells().first() instanceof ImmutableCell, true);
        assertEquals(test.values() instanceof ImmutableList, true);
        assertEquals(test.values().size(), 1);
        assertEquals(test.values().get(0), "Hello");
        assertEquals(test.toString(), "[(0,1)=Hello]");
    }

    public void test_immutableCell_equalsHashCode() {
        ImmutableGrid<String> test = ImmutableGrid.of(0, 1, "Hello");
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(ImmutableGrid.of(0, 1, "Hello")), true);
        HashGrid<String> hash = HashGrid.create();
        hash.put(0, 1, "Hello");
        assertEquals(test.equals(hash), true);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), test.cells().hashCode());
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_negativeRow() {
        ImmutableGrid.of(-1, 2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_factory_negativeColumn() {
        ImmutableGrid.of(1, -2, "Hello");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_factory_nullValue() {
        ImmutableGrid.of(1, 2, null);
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
