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

/**
 * Immutable implementation of the {@code Grid} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public abstract class ImmutableGrid<V> extends AbstractGrid<V> {

    /**
     * Obtains an empty immutable grid with zero row-column count.
     * 
     * @param <R> the type of the value
     * @return the empty immutable grid, not null
     */
    public static <R> ImmutableGrid<R> of() {
        return new EmptyGrid<R>();
    }

    /**
     * Obtains an empty immutable grid of the specified row-column count.
     * 
     * @param <R> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of columns, zero or greater
     * @return the empty immutable grid, not null
     */
    public static <R> ImmutableGrid<R> of(int rowCount, int columnCount) {
        return new EmptyGrid<R>(rowCount, columnCount);
    }

    /**
     * Obtains an immutable grid with row-column count 1x1 and a single cell.
     * <p>
     * The single cell is at row zero column zero.
     * 
     * @param <R> the type of the value
     * @param value  the value of the single cell, not null
     * @return the empty immutable grid, not null
     */
    public static <R> ImmutableGrid<R> of(R value) {
        return new SingletonGrid<R>(1, 1, 0, 0, value);
    }

    /**
     * Obtains an immutable grid of the specified row-column count with a single cell.
     * 
     * @param <R> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of columns, zero or greater
     * @param row  the row of the single cell, zero or greater
     * @param column  the column of the single cell, zero or greater
     * @param value  the value of the single cell, not null
     * @return the empty immutable grid, not null
     */
    public static <R> ImmutableGrid<R> of(int rowCount, int columnCount, int row, int column, R value) {
        return new SingletonGrid<R>(rowCount, columnCount, row, column, value);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an immutable grid with one cell.
     * 
     * @param <R> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of columns, zero or greater
     * @param cell  the cell that the grid should contain, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOf(int rowCount, int columnCount, Cell<R> cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell must not be null");
        }
        return new SingletonGrid<R>(rowCount, columnCount, cell);
    }

    /**
     * Obtains an immutable grid by copying a set of cells.
     * 
     * @param <R> the type of the value
     * @param rowCount  the number of rows, zero or greater
     * @param columnCount  the number of columns, zero or greater
     * @param cells  the cells to copy, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOf(int rowCount, int columnCount, Iterable<? extends Cell<R>> cells) {
        if (cells == null) {
            throw new IllegalArgumentException("Cells must not be null");
        }
        if (cells.iterator().hasNext() == false) {
            return new EmptyGrid<R>(rowCount, columnCount);
        }
        return new SparseImmutableGrid<R>(rowCount, columnCount, cells);
    }

    /**
     * Obtains an immutable grid by copying a set of cells, deriving the row and column count.
     * <p>
     * The row and column counts are calculated as the maximum row and column specified.
     * 
     * @param <R> the type of the value
     * @param cells  the cells to copy, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOfDeriveCounts(Iterable<? extends Cell<R>> cells) {
        if (cells == null) {
            throw new IllegalArgumentException("Cells must not be null");
        }
        if (cells.iterator().hasNext() == false) {
            return new EmptyGrid<R>();
        }
        int rowCount = 0;
        int columnCount = 0;
        for (Cell<R> cell : cells) {
            rowCount = Math.max(rowCount, cell.getRow());
            columnCount = Math.max(columnCount, cell.getColumn());
        }
        return new SparseImmutableGrid<R>(rowCount + 1, columnCount + 1, cells);
    }

    //-----------------------------------------------------------------------
    /**
     * Obtains an immutable grid by copying another grid.
     * <p>
     * If you need to change the row-column count, use {@link #of(int, int, Iterable)}
     * passing in the set of cells from the grid.
     * 
     * @param <R> the type of the value
     * @param grid  the grid to copy, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOf(Grid<R> grid) {
        if (grid == null) {
            throw new IllegalArgumentException("Grid must not be null");
        }
        if (grid instanceof ImmutableGrid) {
            return (ImmutableGrid<R>) grid;
        }
        validateCounts(grid.rowCount(), grid.columnCount());
        if (grid.size() == 0) {
            return new EmptyGrid<R>(grid.rowCount(), grid.columnCount());
        }
        if (grid.size() == 1) {
            Cell<R> cell = grid.cells().iterator().next();
            return new SingletonGrid<R>(grid.rowCount(), grid.columnCount(), cell);
        }
        if (grid.size() >= (grid.rowCount() * grid.columnCount() / 2)) {
            return DenseImmutableGrid.create(grid);
        }
        return new SparseImmutableGrid<R>(grid);
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    ImmutableGrid() {
    }

    //-----------------------------------------------------------------------
    /**
     * {@inheritDoc}
     * @deprecated Grid is read-only
     */
    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Grid is read-only");
    }

    /**
     * {@inheritDoc}
     * @deprecated Grid is read-only
     */
    @Deprecated
    @Override
    public void put(int row, int column, V value) {
        throw new UnsupportedOperationException("Grid is read-only");
    }

    /**
     * {@inheritDoc}
     * @deprecated Grid is read-only
     */
    @Deprecated
    @Override
    public void putAll(Grid<? extends V> grid) {
        throw new UnsupportedOperationException("Grid is read-only");
    }

    /**
     * {@inheritDoc}
     * @deprecated Grid is read-only
     */
    @Deprecated
    @Override
    public boolean remove(int row, int column) {
        throw new UnsupportedOperationException("Grid is read-only");
    }

}
