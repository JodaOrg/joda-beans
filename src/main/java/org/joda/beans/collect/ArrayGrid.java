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

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;

/**
 * Mutable implementation of the {@code Grid} data structure based on an array.
 * <p>
 * This uses one item of memory for each possible combination of row and column.
 * The row and column counts are fixed at creation.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public final class ArrayGrid<V> extends AbstractGrid<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The number of rows.
     */
    private final int rowCount;
    /**
     * The number of columns.
     */
    private final int columnCount;
    /**
     * The size.
     */
    private int size;
    /**
     * The values.
     */
    private final V[] values;

    //-----------------------------------------------------------------------
    /**
     * Creates an empty {@code ArrayGrid} of the specified size.
     * 
     * @param <V> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of rows, zero or greater
     * @return the mutable grid, not null
     */
    public static <V> ArrayGrid<V> create(int rowCount, int columnCount) {
        return new ArrayGrid<V>(rowCount, columnCount);
    }

    /**
     * Creates a {@code ArrayGrid} copying from another grid.
     *
     * @param <V> the type of the value
     * @param grid  the grid to copy, not null
     * @return the mutable grid, not null
     */
    public static <V> ArrayGrid<V> create(Grid<? extends V> grid) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid must not be null");
        }
        ArrayGrid<V> created = ArrayGrid.create(grid.rowCount(), grid.columnCount());
        created.putAll(grid);
        return created;
    }

    /**
     * Creates a {@code ArrayGrid} copying from an array.
     * <p>
     * The row count and column count are derived from the maximum size of the array.
     * The grid is initialized from the non-null values.
     *
     * @param <V> the type of the value
     * @param array  the array, first by row, then by column
     * @return the mutable grid, not null
     */
    public static <V> ArrayGrid<V> create(V[][] array) {
        if (array == null) {
            throw new IllegalArgumentException("Array must not be null");
        }
        int rowCount = array.length;
        if (rowCount == 0) {
            return new ArrayGrid<V>(0, 0);
        }
        int columnCount = array[0].length;
        for (int i = 1; i < rowCount; i++) {
            columnCount = Math.max(columnCount, array[i].length);
        }
        ArrayGrid<V> created = ArrayGrid.create(rowCount, columnCount);
        for (int row = 0; row < array.length; row++) {
            V[] rowValues = array[row];
            for (int column = 0; column < rowValues.length; column++) {
                V cellValue = rowValues[column];
                if (cellValue != null) {
                    created.values[row * columnCount + column] = cellValue;
                    created.size++;
                }
            }
        }
        return created;
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    @SuppressWarnings("unchecked")
    ArrayGrid(int rowCount, int columnCount) {
        validateCounts(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.values = (V[]) new Object[rowCount * columnCount];
    }

    //-----------------------------------------------------------------------
    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int columnCount() {
        return columnCount;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(int row, int column) {
        if (exists(row, column)) {
            return values[row * columnCount + column] != null;
        }
        return false;
    }

    @Override
    public boolean containsValue(Object valueToFind) {
        if (valueToFind != null) {
            for (Object value : values) {
                if (valueToFind.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public V get(int row, int column) {
        if (exists(row, column)) {
            return values[row * columnCount + column];
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public Set<Cell<V>> cells() {
        return new Cells<V>(this);
    }

    /**
     * View onto the grid.
     */
    static class Cells<V> extends AbstractSet<Cell<V>> {
        private final ArrayGrid<V> grid;

        public Cells(ArrayGrid<V> grid) {
            this.grid = grid;
        }

        @Override
        public int size() {
            return grid.size;
        }

        @Override
        public boolean contains(Object obj) {
            Cell<?> cell = (Cell<?>) obj;
            return Objects.equal(cell.getValue(), grid.get(cell.getRow(), cell.getColumn()));
        }

        @Override
        public Iterator<Cell<V>> iterator() {
            return new Iterator<Cell<V>>() {
                private MutableCell<V> cell = new MutableCell<V>();
                private int count;
                private int current = -1;

                @Override
                public boolean hasNext() {
                    return (count < grid.size);
                }
                @Override
                public Cell<V> next() {
                    if (hasNext() == false) {
                        throw new NoSuchElementException("No more elements");
                    }
                    current++;
                    for ( ; current < grid.values.length; current++) {
                        if (grid.values[current] != null) {
                            break;
                        }
                    }
                    V value = grid.values[current];
                    count++;
                    cell.set(current / grid.columnCount, current % grid.columnCount, value);
                    return cell;
                }
                @Override
                public void remove() {
                    if (current < 0) {
                        throw new IllegalStateException("Unable to remove, next() not called yet");
                    }
                    if (grid.values[current] == null) {
                        throw new IllegalStateException("Unable to remove, element has been removed");
                    }
                    grid.values[current] = null;
                    grid.size--;
                    count--;
                }
            };
        }

        @Override
        public boolean add(Cell<V> cell) {
            int oldSize = grid.size;
            grid.put(cell.getRow(), cell.getColumn(), cell.getValue());
            return grid.size > oldSize;
        }

        @Override
        public boolean remove(Object obj) {
            Cell<?> cell = (Cell<?>) obj;
            return grid.remove(cell.getRow(), cell.getColumn());
        }

        @Override
        public void clear() {
            grid.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableCollection<V> values() {
        Object[] array = new Object[size];
        int index = 0;
        for (Object object : values) {
            if (object != null) {
                array[index++] = object;
            }
        }
        return ImmutableList.copyOf((V[]) array);
    }

    //-----------------------------------------------------------------------
    @Override
    public void clear() {
        Arrays.fill(values, null);
        size = 0;
    }

    @Override
    public void put(int row, int column, V value) {
        if (exists(row, column) == false) {
            throw new IndexOutOfBoundsException("Invalid row-column: " + row + "," + column);
        }
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        Object current = values[row * columnCount + column];
        values[row * columnCount + column] = value;
        if (current == null) {
            size++;
        }
    }

    @Override
    public void putAll(Grid<? extends V> grid) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid must nor be null");
        }
        for (Cell<? extends V> cell : grid.cells()) {
            put(cell.getRow(), cell.getColumn(), cell.getValue());
        }
    }

    @Override
    public boolean remove(int row, int column) {
        V current = get(row, column);
        if (current != null) {
            values[row * columnCount + column] = null;
            size--;
            return true;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ArrayGrid) {
            ArrayGrid<?> other = (ArrayGrid<?>) obj;
            return Arrays.equals(values, other.values);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = 0; i < values.length; i++) {
            Object value = values[i];
            if (value != null) {
                int row = i / columnCount;
                int column = i % columnCount;
                hash += (row ^ Integer.rotateLeft(column, 16) ^ value.hashCode());
            }
        }
        return rowCount ^ Integer.rotateLeft(columnCount, 16) ^ hash;
    }

}
