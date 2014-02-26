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

import org.joda.beans.collect.Grid.Cell;

/**
 * Immutable implementations of the {@code Grid.Cell} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public final class ImmutableCell<V> extends AbstractCell<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The row.
     */
    private final int row;
    /**
     * The column.
     */
    private final int column;
    /**
     * The value.
     */
    private final V value;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of {@code Cell}.
     *
     * @param <R> the type of the value
     * @param row  the row, zero or greater
     * @param column  the column, zero or greater
     * @param value  the value to put into the grid, not null
     * @return the immutable cell, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> ImmutableCell<R> of(int row, int column, R value) {
        if (row < 0) {
            throw new IndexOutOfBoundsException("Row must not be negative: " + row + " < 0");
        }
        if (column < 0) {
            throw new IndexOutOfBoundsException("Column must not be negative: " + column + " < 0");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        return new ImmutableCell<R>(row, column, value);
    }

    /**
     * Obtains an instance of {@code Cell}.
     * 
     * @param <R> the type of the value
     * @param cell  the cell to copy, not null
     * @return the immutable cell, not null
     */
    public static <R> ImmutableCell<R> copyOf(Cell<? extends R> cell) {
        if (cell == null) {
            throw new IllegalArgumentException("Cell must not be null");
        }
        if (cell instanceof ImmutableCell) {
            @SuppressWarnings("unchecked")
            ImmutableCell<R> result = (ImmutableCell<R>) cell;
            return result;
        }
        return ImmutableCell.<R>of(cell.getRow(), cell.getColumn(), cell.getValue());
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    ImmutableCell(int row, int column, V value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    //-----------------------------------------------------------------------
    @Override
    public int getRow() {
        return row;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public V getValue() {
        return value;
    }

    /**
     * Validates this cell against the specified counts.
     */
    ImmutableCell<V> validateCounts(int rowCount, int columnCount) {
        if (row >= rowCount || column >= columnCount) {
            throw new IndexOutOfBoundsException("Invalid row-column: " + row + "," + column + " for grid " + rowCount + "x" + columnCount);
        }
        return this;
    }

}
