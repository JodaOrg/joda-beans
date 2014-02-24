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

import org.joda.beans.collect.Grid.Cell;

/**
 * Implementations of the {@code Grid} data structure.
 * 
 * @author Stephen Colebourne
 */
public final class Grids {

    /**
     * Restricted constructor.
     */
    private Grids() {
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an immutable cell.
     * <p>
     * The result is serializable.
     *
     * @param <R> the type of the value
     * @param row  the row, zero or greater
     * @param column  the column, zero or greater
     * @param value  the value to put into the grid, not null
     * @return the immutable cell, not null
     * @throws IndexOutOfBoundsException if either index is less than zero
     */
    public static <R> Cell<R> immutableCell(int row, int column, R value) {
        if (row < 0) {
            throw new IndexOutOfBoundsException("Row must not be negative");
        }
        if (column < 0) {
            throw new IndexOutOfBoundsException("Column must not be negative");
        }
        if (value == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        return new ImmutableCell<R>(row, column, value);
    }

}
