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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Immutable implementation of the {@code Grid} data structure storing one cell.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class SingletonGrid<V> extends ImmutableGrid<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The row count.
     */
    private final int rowCount;
    /**
     * The column count.
     */
    private final int columnCount;
    /**
     * The cell.
     */
    private final ImmutableCell<V> cell;

    /**
     * Restricted constructor.
     */
    SingletonGrid(int rowCount, int columnCount, Cell<V> cell) {
        validateCounts(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cell = ImmutableCell.copyOf(cell).validateCounts(rowCount, columnCount);
    }

    /**
     * Restricted constructor.
     */
    SingletonGrid(int rowCount, int columnCount, int row, int column, V value) {
        validateCounts(rowCount, columnCount);
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.cell = ImmutableCell.of(row, column, value).validateCounts(rowCount, columnCount);
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

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(int row, int column) {
        return cell.equalRowColumn(row, column);
    }

    @Override
    public boolean containsValue(Object valueToFind) {
        return cell.equalValue(valueToFind);
    }

    @Override
    public V get(int row, int column) {
        return cell.equalRowColumn(row, column) ? cell.getValue() : null;
    }

    @Override
    public ImmutableSet<Cell<V>> cells() {
        return ImmutableSet.<Cell<V>>of(cell);
    }

    @Override
    public ImmutableList<V> values() {
        return ImmutableList.of(cell.getValue());
    }

}
