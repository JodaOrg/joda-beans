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

import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableSortedSet.Builder;

/**
 * Immutable implementation of the {@code Grid} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class SparseImmutableGrid<V> extends ImmutableGrid<V> {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The cells.
     */
    private final ImmutableSortedSet<Cell<V>> cells;
    /**
     * The values.
     */
    private transient ImmutableList<V> values;

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    SparseImmutableGrid(Iterable<Cell<V>> data) {
        Builder<Cell<V>> builder = ImmutableSortedSet.naturalOrder();
        for (Cell<V> cell : data) {
            builder.add(ImmutableCell.of(cell));
        }
        this.cells = builder.build();
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return cells.size();
    }

    @Override
    public boolean contains(int row, int column) {
        Cell<V> finder = finder(row, column);
        Set<Cell<V>> tail = cells.subSet(finder, true, finder, true);
        if (tail.size() > 0) {
            return true;
        }
        return false;
    }

    @Override
    public V get(int row, int column) {
        Cell<V> finder = finder(row, column);
        Set<Cell<V>> tail = cells.subSet(finder, true, finder, true);
        if (tail.size() > 0) {
            return tail.iterator().next().getValue();
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public ImmutableSortedSet<Cell<V>> cells() {
        return cells;
    }

    @Override
    public ImmutableList<V> values() {
        ImmutableList<V> v = values;
        if (v == null) {
            v = super.values();
            values = v;
        }
        return v;
    }

}
