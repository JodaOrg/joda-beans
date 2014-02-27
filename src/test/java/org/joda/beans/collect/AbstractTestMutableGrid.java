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
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.joda.beans.collect.Grid.Cell;
import org.testng.annotations.Test;

/**
 * Test abstract Grid.
 */
@Test
public abstract class AbstractTestMutableGrid extends AbstractTestGrid {

    public void test_create_intInt() {
        Grid<String> test = create(2, 3);
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.rowCount(), 2);
        assertEquals(test.columnCount(), 3);
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
        assertEquals(test.toString(), "[2x3:(0,0)=Hello, (0,1)=World]");
    }

    public void test_create_intInt_empty() {
        Grid<String> test = create(2, 3);
        assertEquals(test.rowCount(), 2);
        assertEquals(test.columnCount(), 3);
        checkGrid(test);
        assertEquals(test.toString(), "[2x3:]");
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intInt_negativeRowCount() {
        create(-1, 2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intInt_negativeColumnCount() {
        create(1, -2);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intInt_negativeRowColumnCount() {
        create(-1, -2);
    }

    //-----------------------------------------------------------------------
    public void test_create_Grid() {
        Grid<String> test = create(new MockSingletonGrid(2, 3, 0, 1, "World"));
        assertEquals(test.rowCount(), 2);
        assertEquals(test.columnCount(), 3);
        checkGrid(test, 0, 1, "World");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_create_intIntGrid_negativeRow() {
        create(new MockSingletonGrid(2, 3, -1, 1, "World"));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_create_intIntGrid_negativeColumn() {
        create(new MockSingletonGrid(2, 3, 0, -1, "World"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intIntGrid_nullValue() {
        create(new MockSingletonGrid(2, 3, 0, 1, null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intIntGrid_negativeRowCount() {
        create(new MockSingletonGrid(-2, 3, 0, 1, "World"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intIntGrid_negativeColumnCount() {
        create(new MockSingletonGrid(2, -3, 0, 1, "World"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_create_intIntGrid_null() {
        create((Grid<String>) null);
    }

    //-----------------------------------------------------------------------
    public void test_containsValue_Object() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.containsValue("Hello"), true);
        assertEquals(test.containsValue("World"), true);
        assertEquals(test.containsValue("Spicy"), false);
        assertEquals(test.containsValue(""), false);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(Integer.valueOf(6)), false);
    }

    public void test_equalsHashCode() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.equals(test), true);
        assertEquals(test.equals(SparseGrid.create(test)), true);
        assertEquals(test.equals(DenseGrid.create(test)), true);
        assertEquals(test.equals(ImmutableGrid.copyOf(test)), true);
        assertEquals(test.equals(null), false);
        assertEquals(test.equals(""), false);
        
        assertEquals(test.hashCode(), 3 ^ Integer.rotateLeft(3, 16) ^ test.cells().hashCode());
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_clear() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.size(), 2);
        assertEquals(test.cells().size(), 2);
        assertEquals(test.values().size(), 2);
        
        test.clear();
        
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
    }

    @Test
    public void test_clear_empty() {
        Grid<String> test = create3x3();
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
        
        test.clear();
        
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_put_first() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        checkGrid(test, 0, 0, "Hello");
    }

    @Test
    public void test_put_second() {
        Grid<String> test = create3x3();
        test.put(0, 1, "World");
        checkGrid(test, 0, 1, "World");
        test.put(0, 0, "Hello");
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
    }

    @Test
    public void test_put_replace() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        checkGrid(test, 0, 0, "Hello");
        test.put(0, 0, "Update");
        checkGrid(test, 0, 0, "Update");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_put_negativeRow() {
        Grid<String> test = create3x3();
        test.put(-1, 2, "Hello");
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_put_negativeColumn() {
        Grid<String> test = create3x3();
        test.put(1, -2, "Hello");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_put_nullValue() {
        Grid<String> test = create3x3();
        test.put(1, 2, null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_putAll_emptyPlusNonEmpty() {
        Grid<String> test = create3x3();
        test.putAll(ImmutableGrid.of(2, 2, 0, 1, "Hello"));
        checkGrid(test, 0, 1, "Hello");
    }

    @Test
    public void test_putAll_nonEmptyPlusEmpty() {
        Grid<String> test = create3x3();
        test.put(0, 1, "Hello");
        checkGrid(test, 0, 1, "Hello");
        test.putAll(ImmutableGrid.<String>of());
        checkGrid(test, 0, 1, "Hello");
    }

    @Test
    public void test_putAll_emptyPlusEmpty() {
        Grid<String> test = create3x3();
        test.putAll(ImmutableGrid.<String>of());
        checkGrid(test);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_putAll_null() {
        Grid<String> test = create3x3();
        test.putAll((Grid<String>) null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_remove_last() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
        assertEquals(test.remove(0, 1), true);
        checkGrid(test, 0, 0, "Hello");
    }

    @Test
    public void test_remove_first() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        checkGrid(test, 0, 0, "Hello", 0, 1, "World");
        assertEquals(test.remove(0, 0), true);
        checkGrid(test, 0, 1, "World");
    }

    @Test
    public void test_remove_notPresent_empty() {
        Grid<String> test = create3x3();
        checkGrid(test);
        assertEquals(test.remove(1, 2), false);
        checkGrid(test);
    }

    @Test
    public void test_remove_notPresent_nonEmpty() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(2, 2, "World");
        checkGrid(test, 0, 0, "Hello", 2, 2, "World");
        assertEquals(test.remove(1, 1), false);
        assertEquals(test.remove(1, 2), false);
        assertEquals(test.remove(2, 1), false);
        checkGrid(test, 0, 0, "Hello", 2, 2, "World");
    }

    @Test
    public void test_remove_largeIndex() {
        DenseGrid<String> test = DenseGrid.create(2, 2);
        assertEquals(test.remove(999, 1000), false);
        checkGrid(test);
    }

    @Test
    public void test_remove_invalidIndex() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        checkGrid(test, 0, 0, "Hello");
        assertEquals(test.remove(-1, -1), false);
        assertEquals(test.remove(1, -1), false);
        assertEquals(test.remove(-1, 1), false);
        checkGrid(test, 0, 0, "Hello");
    }

    @Test
    public void test_remove_empty() {
        Grid<String> test = create3x3();
        assertEquals(test.remove(0, 0), false);
        checkGrid(test);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_cells() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.put(1, 1, "Space");
        assertEquals(test.cells().size(), 3);
        Iterator<Cell<String>> it = test.cells().iterator();
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(0, 0, "Hello"));
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(0, 1, "World"));
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(1, 1, "Space"));
        assertEquals(it.hasNext(), false);
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

    @Test
    public void test_cells_contains() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().contains(ImmutableCell.of(0, 0, "Hello")), true);
        assertEquals(test.cells().contains(ImmutableCell.of(0, 1, "World")), true);
        assertEquals(test.cells().contains(ImmutableCell.of(1, 1, "")), false);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void test_cells_contains_nonCell() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().contains("NonCell");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_cells_contains_null() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().contains(null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_cells_iterator_remove_first() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().size(), 2);
        Iterator<Cell<String>> it = test.cells().iterator();
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(0, 0, "Hello"));
        assertEquals(it.hasNext(), true);
        it.remove();
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(0, 1, "World"));
        assertEquals(it.hasNext(), false);
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0, 0), null);
        assertEquals(test.get(0, 1), "World");
        assertEquals(test.get(1, 0), null);
        assertEquals(test.get(1, 1), null);
    }

    @Test
    public void test_cells_iterator_remove_second() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(1, 1, "World");
        assertEquals(test.cells().size(), 2);
        Iterator<Cell<String>> it = test.cells().iterator();
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(0, 0, "Hello"));
        assertEquals(it.hasNext(), true);
        assertEquals(it.next(), ImmutableCell.of(1, 1, "World"));
        assertEquals(it.hasNext(), false);
        it.remove();
        assertEquals(it.hasNext(), false);
        assertEquals(test.size(), 1);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), null);
        assertEquals(test.get(1, 0), null);
        assertEquals(test.get(1, 1), null);
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void test_cells_iterator_removeBeforeNext() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(1, 1, "World");
        test.cells().iterator().remove();
    }

    @Test(expectedExceptions = IllegalStateException.class)
    public void test_cells_iterator_removeTwice() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(1, 1, "World");
        Iterator<Cell<String>> it = test.cells().iterator();
        it.next();
        it.remove();
        it.remove();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_cells_add() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().add(ImmutableCell.of(1, 2, "Extra"));
        
        assertEquals(test.size(), 3);
        assertEquals(test.contains(0, 0), true);
        assertEquals(test.contains(0, 1), true);
        assertEquals(test.contains(1, 2), true);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), "World");
        assertEquals(test.get(1, 2), "Extra");
        assertEquals(test.cells().size(), 3);
        assertEquals(test.values().size(), 3);
    }

    @Test
    public void test_cells_add_replace() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().add(ImmutableCell.of(0, 1, "World"));
        
        assertEquals(test.size(), 2);
        assertEquals(test.contains(0, 0), true);
        assertEquals(test.contains(0, 1), true);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), "World");
        assertEquals(test.cells().size(), 2);
        assertEquals(test.values().size(), 2);
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_cells_add_negativeRow() {
        Grid<String> test = create3x3();
        test.cells().add(new MockCell(-1, 2, "Hello"));
    }

    @Test(expectedExceptions = IndexOutOfBoundsException.class)
    public void test_cells_add_negativeColumn() {
        Grid<String> test = create3x3();
        test.cells().add(new MockCell(1, -2, "Hello"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_cells_add_nullValue() {
        Grid<String> test = create3x3();
        test.cells().add(new MockCell(1, 2, null));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_cells_add_null() {
        Grid<String> test = create3x3();
        test.cells().add(null);
    }

    //-----------------------------------------------------------------------
    @Test
    @SuppressWarnings("unchecked")
    public void test_cells_addAll() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().addAll(Arrays.asList(ImmutableCell.of(1, 2, "Extra"), ImmutableCell.of(2, 2, "Lots")));
        
        assertEquals(test.size(), 4);
        assertEquals(test.contains(0, 0), true);
        assertEquals(test.contains(0, 1), true);
        assertEquals(test.contains(1, 2), true);
        assertEquals(test.contains(2, 2), true);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), "World");
        assertEquals(test.get(1, 2), "Extra");
        assertEquals(test.get(2, 2), "Lots");
        assertEquals(test.cells().size(), 4);
        assertEquals(test.values().size(), 4);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_cells_remove_first() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().size(), 2);
        assertEquals(test.cells().remove(ImmutableCell.of(0, 0, "Hello")), true);
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0, 0), null);
        assertEquals(test.get(0, 1), "World");
    }

    @Test
    public void test_cells_remove_second() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().size(), 2);
        assertEquals(test.cells().remove(ImmutableCell.of(0, 1, "World")), true);
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), null);
    }

    @Test
    public void test_cells_remove_goodIndicesBadValue() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().size(), 2);
        assertEquals(test.cells().remove(ImmutableCell.of(0, 1, "Rubbish")), true);
        
        assertEquals(test.size(), 1);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), null);
    }

    @Test
    public void test_cells_remove_badCell_negativeRow() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        assertEquals(test.cells().size(), 2);
        assertEquals(test.cells().remove(new MockCell(-1, 1, "Hello")), false);
        
        assertEquals(test.size(), 2);
        assertEquals(test.get(0, 0), "Hello");
        assertEquals(test.get(0, 1), "World");
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void test_cells_remove_nonCell() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().remove("NonCell");
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_cells_remove_null() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().remove(null);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_cells_clear() {
        Grid<String> test = create3x3();
        test.put(0, 0, "Hello");
        test.put(0, 1, "World");
        test.cells().clear();
        
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
    }

    @Test
    public void test_cells_clear_empty() {
        Grid<String> test = create3x3();
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
        
        test.cells().clear();
        
        assertEquals(test.size(), 0);
        assertEquals(test.cells().size(), 0);
        assertEquals(test.values().size(), 0);
    }

    //-----------------------------------------------------------------------
    protected abstract Grid<String> create3x3();

    protected abstract Grid<String> create(int rowCount, int columnCount);

    protected abstract Grid<String> create(Grid<String> grid);

}
