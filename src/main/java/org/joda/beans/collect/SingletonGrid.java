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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

/**
 * Immutable implementation of the {@code Grid} data structure storing one cell.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class SingletonGrid<V> extends ImmutableGrid<V> {

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
    /**
     * The cell.
     */
    private transient ImmutableSortedSet<Cell<V>> cell;

    /**
     * Restricted constructor.
     */
    SingletonGrid(int row, int column, V value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return 1;
    }

    @Override
    public boolean contains(int row, int column) {
        return this.row == row && this.column == column;
    }

    @Override
    public boolean containsValue(Object valueToFind) {
        return value.equals(valueToFind);
    }

    @Override
    public V get(int row, int column) {
        return contains(row, column) ? value : null;
    }

    @Override
    public ImmutableSortedSet<Cell<V>> cells() {
        ImmutableSortedSet<Cell<V>> c = cell;
        if (c == null) {
            c = ImmutableSortedSet.<Cell<V>>of(new ImmutableCell<V>(row, column, value));
            cell = c;
        }
        return c;
    }

    @Override
    public ImmutableList<V> values() {
        return ImmutableList.of(value);
    }

}
