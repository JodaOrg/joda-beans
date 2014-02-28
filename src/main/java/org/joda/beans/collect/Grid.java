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

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableCollection;

/**
 * A data structure representing a grid keyed by {@code int} row and {@code int} column.
 * <p>
 * A grid has a fixed number of rows and columns, but not all cells must be occupied.
 * Dense and sparse implementations are provided that handle high and low numbers of cells
 * relative to the potential capacity.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public interface Grid<V> {

    /**
     * Gets the number of rows in the grid.
     * <p>
     * A grid has a fixed number of rows and columns, but not all cells must be occupied.
     * This returns the row capacity, not the number of occupied rows.
     * It is guaranteed that {@link #contains(int, int)} will return {@code false}
     * for indices larger than the row count.
     * 
     * @return the number of rows, zero or greater
     */
    int rowCount();

    /**
     * Gets the number of columns in the grid.
     * <p>
     * A grid has a fixed number of rows and columns, but not all cells must be occupied.
     * This returns the column capacity, not the number of occupied columns.
     * It is guaranteed that {@link #contains(int, int)} will return {@code false}
     * for indices larger than the column count.
     * 
     * @return the number of columns, zero or greater
     */
    int columnCount();

    /**
     * Checks if the specified row-column exists.
     * <p>
     * This simply checks that the row and column indices are between
     * zero and the row and column counts.
     * 
     * @param row  the row
     * @param column  the column
     * @return true if the row-column exists
     */
    boolean exists(int row, int column);

    //-----------------------------------------------------------------------
    /**
     * Checks if the grid is full.
     * <p>
     * A full grid has a cell at every combination of row and column.
     * 
     * @return true if full
     */
    boolean isFull();

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
     * Checks if a value is present at the specified row-column.
     * <p>
     * If either index does not exist, false is returned.
     * 
     * @param row  the row
     * @param column  the column
     * @return true if there is a value at the row-column
     */
    boolean contains(int row, int column);

    /**
     * Checks if the specified value is contained in the grid.
     * 
     * @param valueToFind  the value to find, null returns false
     * @return true if the grid contains the value
     */
    boolean containsValue(Object valueToFind);

    /**
     * Gets the value at the specified row-column.
     * <p>
     * If either index does not exist, null is returned.
     * 
     * @param row  the row
     * @param column  the column
     * @return the value at the row-column, null if not found
     */
    V get(int row, int column);

    /**
     * Gets the cell at the specified row-column.
     * <p>
     * If either index does not exist, null is returned.
     * 
     * @param row  the row
     * @param column  the column
     * @return the cell at the row-column, null if not found
     */
    Cell<V> cell(int row, int column);

    //-----------------------------------------------------------------------
    /**
     * Checks if this grid equals another grid.
     * <p>
     * Two grids are equal if they are the same size and contain the same set of cells.
     * 
     * @param obj  the object to compare to, null returns false
     * @return true if equal
     */
    @Override
    boolean equals(Object obj);

    /**
     * Gets a suitable hash code.
     * <p>
     * The hash code is {@code rowCount ^ Integer.rotateLeft(columnCount, 16) ^ cells.hashCode()}.
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
     * <p>
     * If either index does not exist, {@code IndexOutOfBoundsException} is thrown.
     * 
     * @param row  the row, zero or greater
     * @param column  the column, zero or greater
     * @param value  the value to put into the grid, not null
     * @throws IndexOutOfBoundsException if either index does not exist
     * @throws UnsupportedOperationException if read-only
     */
    void put(int row, int column, V value);

    /**
     * Puts all cells from a grid into this grid.
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
     * <p>
     * If either index does not exist, no action occurs and false is returned.
     * 
     * @param row  the row
     * @param column  the column
     * @return true if the grid is altered
     * @throws UnsupportedOperationException if read-only
     */
    boolean remove(int row, int column);

    //-----------------------------------------------------------------------
    /**
     * Gets the complete set of cells.
     * <p>
     * If the grid is mutable then cells may be added or removed from the set.
     * The cells are returned in order, looping around rows, then columns.
     * <p>
     * The cell returned from the set iterator may be a mutable {@code Cell}
     * implementation that cannot be stored beyond the lifetime of an iteration.
     * 
     * @return the set of all cells, not null
     */
    Set<Cell<V>> cells();

    /**
     * Gets the values in order through rows, then columns.
     * <p>
     * The returned data structure is an ordered collection.
     * The values are returned in order, looping around rows, then columns.
     * 
     * @return the collection of all values, not null
     */
    ImmutableCollection<V> values();

    /**
     * Gets the columns of a single row.
     * <p>
     * The list will contain all columns from zero to {@code columnCount}.
     * Where there is no value for a cell, the list will contain null.
     * <p>
     * The returned list is immutable, except for {@link List#set(int, Object)},
     * which adds, updates or deletes from the underlying grid.
     * 
     * @param row  the row, zero or greater
     * @return the columns of the specified row, not null
     * @throws IndexOutOfBoundsException if the row is invalid
     */
    List<V> row(int row);

    /**
     * Gets the entire grid of values, by row then column.
     * <p>
     * The outer list contains all rows from zero to {@code rowCount}.
     * Each inner list contains all columns from zero to {@code columnCount}.
     * Where there is no value for a cell, the value is null.
     * <p>
     * The returned list is immutable, except for the {@link List#set(int, Object)}
     * method on the inner list, which adds, updates or deletes from the underlying grid.
     * 
     * @return the entire grid, by row then column, not null
     */
    List<List<V>> rows();

    /**
     * Gets the rows of a single column.
     * <p>
     * The list will contain all rows from zero to {@code rowCount}.
     * Where data is not present, the list will contain null.
     * <p>
     * The returned list is immutable, except for {@link List#set(int, Object)},
     * which adds, updates or deletes from the underlying grid.
     * 
     * @param column  the column, zero or greater
     * @return the rows of the specified column, not null
     * @throws IndexOutOfBoundsException if the column is invalid
     */
    List<V> column(int column);

    /**
     * Gets the entire grid of values, by column then row.
     * <p>
     * The outer list contains all columns from zero to {@code columnCount}.
     * Each inner list contains all rows from zero to {@code rowCount}.
     * Where there is no value for a cell, the value is null.
     * <p>
     * The returned list is immutable, except for the {@link List#set(int, Object)}
     * method on the inner list, which adds, updates or deletes from the underlying grid.
     * 
     * @return the entire grid, by row then column, not null
     */
    List<List<V>> columns();

    //-----------------------------------------------------------------------
    /**
     * A cell within the grid compared only using row and column.
     * 
     * @param <V> the type of the value
     */
    public interface Cell<V> {

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
         * Checks if the row-column of this cell matches the specified row and column.
         * 
         * @param row  the row to check
         * @param column  the column to check
         * @return true if equal
         */
        boolean equalRowColumn(int row, int column);

        /**
         * Checks if the value of this cell matches the specified value.
         * 
         * @param value  the row to check, null returns false
         * @return true if equal
         */
        boolean equalValue(Object value);

        /**
         * Checks if this cell equals another cell.
         * <p>
         * Two cells are equal if they have equal row, column and value.
         * 
         * @param obj  the object to compare to, null returns false
         * @return true if equal
         */
        @Override
        boolean equals(Object obj);

        /**
         * Gets a suitable hash code.
         * <p>
         * The hash code is {@code row ^ Integer.rotateLeft(column, 16) ^ value.hashCode()}.
         * 
         * @return the hash code
         */
        @Override
        int hashCode();
    }

}
