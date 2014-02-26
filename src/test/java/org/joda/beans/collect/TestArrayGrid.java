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
 * Test ArrayGrid.
 */
@Test
public class TestArrayGrid extends AbstractTestMutableGrid {

    @Override
    protected Grid<String> create3x3() {
        return ArrayGrid.create(3, 3);
    }

    @Override
    protected Grid<String> create(int rowCount, int columnCount) {
        return ArrayGrid.create(rowCount, columnCount);
    }

    @Override
    protected Grid<String> create(Grid<String> grid) {
        return ArrayGrid.create(grid);
    }

    //-----------------------------------------------------------------------
    public void test_create_array2D() {
        String[][] array = new String[][] {
                {"Hello", "World"},
                {null},
        };
        ArrayGrid<String> test = ArrayGrid.create(array);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.size(), 2);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), "World");
        assertEquals(test.get(1, 0), null);
        assertEquals(test.get(1, 1), null);
    }

    public void test_create_array2D_empty() {
        String[][] array = new String[][] {};
        ArrayGrid<String> test = ArrayGrid.create(array);
        assertEquals(test.isEmpty(), true);
        assertEquals(test.size(), 0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_array2D_null() {
        ArrayGrid.create((String[][]) null);
    }

    //-----------------------------------------------------------------------
    public void test_rowCount_columnCount() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.rowCount(), 3);
        assertEquals(test.columnCount(), 3);
    }

    public void test_rowCount_columnCount_empty() {
        Grid<String> test = create3x3();
        assertEquals(test.rowCount(), 3);
        assertEquals(test.columnCount(), 3);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_put_rowTooBig() {
        ArrayGrid<String> test = ArrayGrid.create(2, 2);
        test.put(3, 1, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_put_columnTooBig() {
        ArrayGrid<String> test = ArrayGrid.create(2, 2);
        test.put(1, 3, "Hello");
    }

}
