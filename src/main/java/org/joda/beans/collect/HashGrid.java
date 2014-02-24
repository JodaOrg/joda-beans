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
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.google.common.collect.ForwardingSortedSet;

/**
 * Mutable implementation of the {@code Grid} data structure based on hashing.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
public final class HashGrid<V> extends AbstractGrid<V> implements Serializable {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The cells.
     */
    private final SortedSet<Cell<V>> cells;

    //-----------------------------------------------------------------------
    /**
     * Creates an empty {@code HashGrid}.
     * 
     * @param <R> the type of the value
     * @return the mutable grid, not null
     */
    public static <R> HashGrid<R> create() {
        return new HashGrid<R>(new TreeSet<Cell<R>>());
    }

    /**
     * Creates a {@code HashGrid} copying from another grid.
     *
     * @param <R> the type of the value
     * @param grid  the grid to copy, not null
     * @return the mutable grid, not null
     */
    public static <R> HashGrid<R> create(Grid<? extends R> grid) {
        HashGrid<R> created = HashGrid.create();
        created.putAll(grid);
        return created;
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    HashGrid(SortedSet<Cell<V>> data) {
        this.cells = data;
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return cells.size();
    }

    @Override
    public boolean contains(int row, int column) {
        Set<Cell<V>> tail = cells.tailSet(finder(row, column));
        if (tail.size() > 0) {
            Iterator<Cell<V>> it = tail.iterator();
            Cell<V> cell = it.next();
            if (cell.getRow() == row && cell.getColumn() == column) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(int row, int column) {
        Set<Cell<V>> tail = cells.tailSet(finder(row, column));
        if (tail.size() > 0) {
            Iterator<Cell<V>> it = tail.iterator();
            Cell<V> cell = it.next();
            if (cell.getRow() == row && cell.getColumn() == column) {
                return cell.getValue();
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public SortedSet<Cell<V>> cells() {
        return new ForwardingSortedSet<Cell<V>>() {
            @Override
            protected SortedSet<Cell<V>> delegate() {
                return cells;
            }
            @Override
            public boolean add(Cell<V> element) {
                return super.add(ImmutableCell.of(element));
            }
            @Override
            public boolean addAll(Collection<? extends Cell<V>> collection) {
                return super.standardAddAll(collection);
            }
        };
    }

    //-----------------------------------------------------------------------
    @Override
    public void clear() {
        cells.clear();
    }

    @Override
    public void put(int row, int column, V value) {
        Cell<V> cell = Grids.immutableCell(row, column, value);
        cells.add(cell);
    }

    @Override
    public void putAll(Grid<? extends V> grid) {
        for (Cell<? extends V> cell : grid.cells()) {
            cells.add(ImmutableCell.of(cell));
        }
    }

    @Override
    public boolean remove(int row, int column) {
        Set<Cell<V>> tail = cells.tailSet(finder(row, column));
        if (tail.size() > 0) {
            Iterator<Cell<V>> it = tail.iterator();
            Cell<V> cell = it.next();
            if (cell.getRow() == row && cell.getColumn() == column) {
                it.remove();
                return true;
            }
        }
        return false;
    }

}
