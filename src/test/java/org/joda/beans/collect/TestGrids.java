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

import org.joda.beans.collect.Grid.Cell;
import org.testng.annotations.Test;

/**
 * Test Grids.
 */
@Test
public class TestGrids {

    public void test_immutableCell() {
        Cell<String> test = Grids.immutableCell(1, 2, "Hello");
        assertEquals(test.getRow(), 1);
        assertEquals(test.getColumn(), 2);
        assertEquals(test.getValue(), "Hello");
        assertEquals(test.toString(), "(1,2)=Hello");
    }

    public void test_immutableCell_equalsHashCode() {
        Cell<String> test = Grids.immutableCell(1, 2, "Hello");
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(Grids.immutableCell(1, 2, "Hello")), true);
        assertEquals(test.equals(Grids.immutableCell(0, 2, "Hello")), false);
        assertEquals(test.equals(Grids.immutableCell(1, 3, "Hello")), false);
        assertEquals(test.equals(Grids.immutableCell(1, 2, "Hell")), false);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), "Hello".hashCode() ^ 1 ^ Integer.rotateLeft(2, 16));
    }

    public void test_immutableCell_compareTo() {
        Cell<String> test = Grids.immutableCell(1, 2, "Hello");
        assertEquals(test.compareTo(Grids.immutableCell(1, 2, "Hello")) == 0, true);
        assertEquals(test.compareTo(Grids.immutableCell(1, 2, "Ignored")) == 0, true);
        
        assertEquals(test.compareTo(Grids.immutableCell(0, 2, "Hello")) > 0, true);
        assertEquals(test.compareTo(Grids.immutableCell(2, 2, "Hello")) < 0, true);
        
        assertEquals(test.compareTo(Grids.immutableCell(1, 1, "Hello")) > 0, true);
        assertEquals(test.compareTo(Grids.immutableCell(1, 3, "Hello")) < 0, true);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_immutableCell_negativeRow() {
        Grids.immutableCell(-1, 2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_immutableCell_negativeColumn() {
        Grids.immutableCell(1, -2, "Hello");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_immutableCell_nullValue() {
        Grids.immutableCell(1, 2, null);
    }

}
