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
import static org.testng.Assert.assertSame;

import org.testng.annotations.Test;

/**
 * Test ImmutableGrid factories.
 */
@Test
public class TestImmutableGrid {

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_of_intIntObject_negativeRow() {
        ImmutableGrid.of(-1, 2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_of_intIntObject_negativeColumn() {
        ImmutableGrid.of(1, -2, "Hello");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_of_intIntObject_nullValue() {
        ImmutableGrid.of(1, 2, null);
    }

    //-----------------------------------------------------------------------
    public void test_copyOf_Grid_alreadyImmutable() {
        HashGrid<String> hash = HashGrid.create();
        hash.put(0, 0, "Hello");
        hash.put(0, 1, "World");
        ImmutableGrid<String> base = ImmutableGrid.copyOf(hash);
        ImmutableGrid<String> test = ImmutableGrid.copyOf(base);
        assertSame(test, base);
    }

    public void test_copyOf_Grid_size0() {
        HashGrid<String> hash = HashGrid.create();
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash);
        assertEquals(test instanceof EmptyGrid, true);
    }

    public void test_copyOf_Grid_size1() {
        HashGrid<String> hash = HashGrid.create();
        hash.put(0, 1, "Hello");
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash);
        assertEquals(test instanceof SingletonGrid, true);
        assertEquals(test.cells().first().getRow(), 0);
        assertEquals(test.cells().first().getColumn(), 1);
        assertEquals(test.cells().first().getValue(), "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_copyOf_Grid_badCell_negativeRow() {
        ImmutableGrid.copyOf(new MockBadGrid(-1, 0, "Hello"));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_copyOf_Grid_badCell_negativeColumn() {
        ImmutableGrid.copyOf(new MockBadGrid(0, -1, "Hello"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_copyOf_Grid_badCell_nullValue() {
        ImmutableGrid.copyOf(new MockBadGrid(0, 0, null));
    }

    //-----------------------------------------------------------------------
    public void test_copyOf_Cells_size0() {
        HashGrid<String> hash = HashGrid.create();
        ImmutableGrid<String> test = ImmutableGrid.copyOf(hash.cells());
        assertEquals(test instanceof EmptyGrid, true);
    }

}
