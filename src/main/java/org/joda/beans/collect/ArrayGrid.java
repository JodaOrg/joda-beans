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
import java.util.Arrays;
import java.util.SortedSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Mutable implementation of the {@code Grid} data structure based on an array.
 * <p>
 * This uses one item of memory for each possible combination of row and column.
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
    private final Object[] values;

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
        if (rowCount < 0) {
            throw new IllegalArgumentException("Row count must be zero or greater");
        }
        if (columnCount < 0) {
            throw new IllegalArgumentException("Column count must be zero or greater");
        }
        return new ArrayGrid<V>(rowCount, columnCount);
    }

    /**
     * Creates a {@code ArrayGrid} of the specified size copying from another grid.
     *
     * @param <V> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of rows, zero or greater
     * @param grid  the grid to copy, not null
     * @return the mutable grid, not null
     */
    public static <V> ArrayGrid<V> create(int rowCount, int columnCount, Grid<? extends V> grid) {
        ArrayGrid<V> created = ArrayGrid.create(rowCount, columnCount);
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
    ArrayGrid(int rowCount, int columnCount) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.values = new Object[rowCount * columnCount];
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean contains(int row, int column) {
        if (row >= 0 && row < rowCount && column >= 0 && column < columnCount) {
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
    @SuppressWarnings("unchecked")
    public V get(int row, int column) {
        if (row >= 0 && row < rowCount && column >= 0 && column < columnCount) {
            return (V) values[row * columnCount + column];
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public SortedSet<Cell<V>> cells() {
        ImmutableSortedSet.Builder<Cell<V>> builder = ImmutableSortedSet.naturalOrder();
        for (int i = 0; i < values.length; i++) {
            if (values[i] != null) {
                builder.add(new ImmutableCell<V>(i / columnCount, i % columnCount, (V) values[i]));
            }
        }
        return builder.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public ImmutableList<V> values() {
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
        if (row >= 0 && row < rowCount && column >= 0 && column < columnCount) {
            if (value == null) {
                throw new IllegalArgumentException("Value must not be null");
            }
            Object current = values[row * columnCount + column];
            values[row * columnCount + column] = value;
            if (current == null) {
                size++;
            }
            return;
        }
        throw new IndexOutOfBoundsException("Invalid row-column: " + row + "," + column);
    }

    @Override
    public void putAll(Grid<? extends V> grid) {
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

}
