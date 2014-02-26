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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;

/**
 * Immutable implementation of the {@code Grid} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class SparseImmutableGrid<V> extends ImmutableGrid<V> implements Serializable {

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
     * The keys.
     */
    private final long[] keys;
    /**
     * The cells.
     */
    private final Cell<V>[] cells;
    /**
     * The cell set.
     */
    private transient ImmutableSet<Cell<V>> cellSet;
    /**
     * The values.
     */
    private transient ImmutableCollection<V> valueCollection;

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    @SuppressWarnings("unchecked")
    SparseImmutableGrid(Grid<V> gridToCopy) {
        if (gridToCopy == null) {
            throw new IllegalArgumentException("Grid must not be null");
        }
        this.rowCount = gridToCopy.rowCount();
        this.columnCount = gridToCopy.columnCount();
        validateCounts(rowCount, columnCount);
        
        int size = gridToCopy.cells().size();
        keys = new long[size];
        cells = new Cell[size];
        int i = 0;
        for (Cell<V> cell : gridToCopy.cells()) {
            keys[i] = key(cell.getRow(), cell.getColumn());
            cells[i] = ImmutableCell.copyOf(cell).validateCounts(rowCount, columnCount);
            i++;
        }
    }

    private long key(int row, int column) {
        return (((long) row) << 32) + column;
    }

    /**
     * Restricted constructor.
     */
    @SuppressWarnings("unchecked")
    SparseImmutableGrid(int rowCount, int columnCount, Iterable<? extends Cell<V>> cells) {
        validateCounts(rowCount, columnCount);
        if (cells == null) {
            throw new IllegalArgumentException("Cells must not be null");
        }
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        
        Collection<Cell<V>> list;
        if (cells instanceof Collection) {
            list = (Collection<Cell<V>>) cells;
        } else {
            list = new ArrayList<Cell<V>>();
            Iterables.addAll(list, cells);
        }
        int size = list.size();
        this.keys = new long[size];
        this.cells = new Cell[size];
        int i = 0;
        for (Cell<V> cell : list) {
            this.keys[i] = key(cell.getRow(), cell.getColumn());
            this.cells[i] = ImmutableCell.copyOf(cell).validateCounts(rowCount, columnCount);
            i++;
        }
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
        return cells.length;
    }

    @Override
    public boolean contains(int row, int column) {
        if (exists(row, column)) {
            return Arrays.binarySearch(keys, key(row, column)) >= 0;
        }
        return false;
    }

    @Override
    public V get(int row, int column) {
        if (exists(row, column)) {
            int index = Arrays.binarySearch(keys, key(row, column));
            if (index >= 0) {
                return cells[index].getValue();
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public ImmutableSet<Cell<V>> cells() {
        ImmutableSet<Cell<V>> c = cellSet;
        if (c == null) {
            c = ImmutableSet.copyOf(cells);
            cellSet = c;
        }
        return c;
    }

    @Override
    public ImmutableCollection<V> values() {
        ImmutableCollection<V> v = valueCollection;
        if (v == null) {
            v = super.values();
            valueCollection = v;
        }
        return v;
    }

}
