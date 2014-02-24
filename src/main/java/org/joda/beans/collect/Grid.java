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

import java.util.SortedSet;

import com.google.common.collect.ImmutableList;

/**
 * A data structure representing a grid keyed by {@code int} row and {@code int} column.
 * <p>
 * Implementations are provided for dense (fully populated) and sparse (only some cells present).
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public interface Grid<V> {

    /**
     * Checks if the grid is empty.
     * 
     * @return true if empty
     */
    boolean isEmpty();

    /**
     * Gets the number of cells that are present.
     * 
     * @return the size of the set of cells
     */
    int size();

    /**
     * Checks if the specified row-column exists.
     * 
     * @param row  the row, negative returns false
     * @param column  the column, negative returns false
     * @return true if the row-column exists
     */
    boolean contains(int row, int column);

    /**
     * Checks if the specified row-column exists.
     * 
     * @param valueToFind  the value to find, null returns false
     * @return true if the grid contains the value
     */
    boolean containsValue(Object valueToFind);

    /**
     * Gets the value at the specified row-column.
     * 
     * @param row  the row, negative returns null
     * @param column  the column, negative returns null
     * @return the value at the row-column, null if not found
     */
    V get(int row, int column);

    //-----------------------------------------------------------------------
    /**
     * Checks if this grid equals another grid.
     * <p>
     * Two grids are equal if they contains the same data.
     * 
     * @param obj  the object to compare to, null returns false
     * @return true if equal
     */
    @Override
    boolean equals(Object obj);

    /**
     * Gets a suitable hash code.
     * <p>
     * The hash code is that returned by {@link #cells()}.
     * 
     * @return the hash code
     */
    @Override
    int hashCode();

    //-----------------------------------------------------------------------
    /**
     * Clears the grid.
     * <p>
     * The grid will be empty after calling this method.
     * 
     * @throws UnsupportedOperationException if read-only
     */
    void clear();

    /**
     * Puts a value into this grid.
     * <p>
     * The value at the specified row-column is set.
     * Any previous value at the row-column is replaced.
     * 
     * @param row  the row, zero or greater
     * @param column  the column, zero or greater
     * @param value  the value to put into the grid, not null
     * @throws IndexOutOfBoundsException if either index is less than zero or too large
     * @throws UnsupportedOperationException if read-only
     */
    void put(int row, int column, V value);

    /**
     * Puts a value into this grid.
     * <p>
     * The value at the specified row-column is set.
     * Any previous value at the row-column is replaced.
     * 
     * @param grid  the grid to put into this grid, not null
     * @throws IndexOutOfBoundsException if a cell has an invalid index
     * @throws UnsupportedOperationException if read-only
     */
    void putAll(Grid<? extends V> grid);

    /**
     * Removes the value at the specified row-column.
     * 
     * @param row  the row, negative returns null
     * @param column  the column, negative returns null
     * @return true if the grid is altered
     * @throws UnsupportedOperationException if read-only
     */
    boolean remove(int row, int column);

    //-----------------------------------------------------------------------
    /**
     * Gets the complete set of cells.
     * <p>
     * Mutable implementations may use a {@code Cell} implementation that
     * is updated during iteration.
     * 
     * @return the set of all cells, not null
     */
    SortedSet<Cell<V>> cells();

    /**
     * Gets the list of values in order through rows, then columns.
     * <p>
     * Values may occur more than once.
     * 
     * @return the list of all values, not null
     */
    ImmutableList<V> values();

//    /**
//     * Gets the data of a single row.
//     * <p>
//     * The list will contain all the columns of the specified row.
//     * Where data is not present, the list will contain null.
//     * 
//     * @param row  the row, zero or greater
//     * @return the columns of the specified row, not null
//     * @throws IndexOutOfBoundsException if the row is invalid
//     */
//    List<V> rowColumns(int row);
//
//    /**
//     * Gets the data of a single column.
//     * <p>
//     * The list will contain all the columns of the specified column.
//     * Where data is not present, the list will contain null.
//     * 
//     * @param column  the column, zero or greater
//     * @return the rows of the specified column, not null
//     * @throws IndexOutOfBoundsException if the column is invalid
//     */
//    List<V> columnRows(int column);

    //-----------------------------------------------------------------------
    /**
     * A cell within the grid.
     * <p>
     * Cells are sorted by row, then by column, with value ignored.
     * 
     * @param <V> the type of the value
     */
    public interface Cell<V> extends Comparable<Cell<V>> {

        /**
         * Gets the row index.
         * 
         * @return the row, zero or greater
         */
        int getRow();

        /**
         * Gets the column index.
         * 
         * @return the column, zero or greater
         */
        int getColumn();

        /**
         * Gets the value of the cell.
         * 
         * @return the cell value, not null
         */
        V getValue();

        /**
         * Checks if this cell equals another cell.
         * <p>
         * Two cells are equal if they contains the same value at the same index.
         * 
         * @param obj  the object to compare to, null returns false
         * @return true if equal
         */
        @Override
        boolean equals(Object obj);

        /**
         * Gets a suitable hash code.
         * <p>
         * The hash code is {@code value.hashCode() ^ row ^ Integer.rotateLeft(column, 16)}.
         * 
         * @return the hash code
         */
        @Override
        int hashCode();
    }

}
