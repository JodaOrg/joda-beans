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
 * Immutable implementations of the {@code Grid.Cell} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class MutableCell<V> extends AbstractCell<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The row.
     */
    private int row;
    /**
     * The column.
     */
    private int column;
    /**
     * The value.
     */
    private V value;

    /**
     * Creates an instance.
     */
    MutableCell() {
    }


    /**
     * Creates an instance.
     * 
     * @param row  the row index
     * @param column  the column index
     * @param value  the value
     */
    MutableCell(int row, int column, V value) {
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
     * Sets the content of the cell.
     * 
     * @param row  the row index
     * @param column  the column index
     * @param value  the value
     */
    void set(int row, int column, V value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

}
