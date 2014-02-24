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

/**
 * Immutable implementation of the {@code Grid} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public abstract class ImmutableGrid<V> extends AbstractGrid<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    //-----------------------------------------------------------------------
    /**
     * Obtains an empty immutable grid.
     * 
     * @param <R> the type of the value
     * @return the empty immutable grid, not null
     */
    public static <R> ImmutableGrid<R> of() {
        return new EmptyGrid<R>();
    }

    /**
     * Obtains an immutable grid with one cell.
     * 
     * @param <R> the type of the value
     * @param row  the row, zero or greater
     * @param column  the column, zero or greater
     * @param value  the value to put into the grid, not null
     * @return the singleton immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> of(int row, int column, R value) {
        if (row < 0) {
            throw new IndexOutOfBoundsException("Row must not be negative");
        }
        if (column < 0) {
            throw new IndexOutOfBoundsException("Column must not be negative");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        return new SingletonGrid<R>(row, column, value);
    }

    /**
     * Obtains an immutable grid by copying another grid.
     * 
     * @param <R> the type of the value
     * @param gridToCopy  the grid to copy, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOf(Grid<R> gridToCopy) {
        if (gridToCopy instanceof ImmutableGrid) {
            return (ImmutableGrid<R>) gridToCopy;
        }
        if (gridToCopy.size() == 0) {
            return ImmutableGrid.of();
        }
        if (gridToCopy.size() == 1) {
            Cell<R> cell = gridToCopy.cells().first();
            return ImmutableGrid.of(cell.getRow(), cell.getColumn(), cell.getValue());
        }
        return new SparseImmutableGrid<R>(gridToCopy.cells());
    }

    /**
     * Obtains an immutable grid by copying a set of cells.
     * 
     * @param <R> the type of the value
     * @param cellsToCopy  the cells to copy, not null
     * @return the immutable grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableGrid<R> copyOf(Iterable<Cell<R>> cellsToCopy) {
        if (cellsToCopy.iterator().hasNext() == false) {
            return ImmutableGrid.of();
        }
        return new SparseImmutableGrid<R>(cellsToCopy);
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
