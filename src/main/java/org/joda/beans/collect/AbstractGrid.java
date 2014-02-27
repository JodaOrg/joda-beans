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

import java.util.AbstractList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Abstract implementation of the {@code Grid} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
abstract class AbstractGrid<V> implements Grid<V> {

    /**
     * Validates the row and column counts.
     * 
     * @param rowCount  the row count
     * @param columnCount  the column count
     */
    static void validateCounts(int rowCount, int columnCount) {
        if (rowCount < 0) {
            throw new IllegalArgumentException("Row count must not be negative: " + rowCount + " < 0");
        }
        if (columnCount < 0) {
            throw new IllegalArgumentException("Column count must not be negative: " + columnCount + " < 0");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    AbstractGrid() {
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean exists(int row, int column) {
        return row >= 0 && row < rowCount() && column >= 0 && column < columnCount();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public int size() {
        return cells().size();
    }

    @Override
    public boolean contains(int row, int column) {
        return cell(row, column) != null;
    }

    @Override
    public boolean containsValue(Object valueToFind) {
        return (valueToFind != null ? values().contains(valueToFind) : false);
    }

    @Override
    public V get(int row, int column) {
        Cell<V> cell = cell(row, column);
        return (cell != null ? cell.getValue() : null);
    }

    @Override
    public Cell<V> cell(int row, int column) {
        if (exists(row, column)) {
            for (Cell<V> cell : cells()) {
                if (cell.getRow() == row && cell.getColumn() == column) {
                    return cell;
                }
            }
        }
        return null;
    }

    //-----------------------------------------------------------------------
    @Override
    public ImmutableCollection<V> values() {
        Builder<V> builder = ImmutableList.builder();
        for (Cell<V> cell : cells()) {
            builder.add(cell.getValue());
        }
        return builder.build();
    }

    //-----------------------------------------------------------------------
    @Override
    public List<V> row(int row) {
        Preconditions.checkElementIndex(row, rowCount(), "Row index");
        return new Inner<V>(this, columnCount(), row, true);
    }

    @Override
    public List<List<V>> rows() {
        return new Outer<V>(this, rowCount(), columnCount(), true);
    }

    @Override
    public List<V> column(int column) {
        Preconditions.checkElementIndex(column, columnCount(), "Column index");
        return new Inner<V>(this, rowCount(), column, false);
    }

    @Override
    public List<List<V>> columns() {
        return new Outer<V>(this, columnCount(), rowCount(), false);
    }

    static class Outer<V> extends AbstractList<List<V>> {
        private final Grid<V> grid;
        private final int size;
        private final int innerSize;
        private final boolean rows;

        Outer(Grid<V> grid, int size, int innerSize, boolean rows) {
            this.grid = grid;
            this.size = size;
            this.innerSize = innerSize;
            this.rows = rows;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public List<V> get(int index) {
            Preconditions.checkElementIndex(index, size);
            return new Inner<V>(grid, innerSize, index, rows);
        }
    }

    static class Inner<V> extends AbstractList<V> {
        private final Grid<V> grid;
        private final int size;
        private final int outerIndex;
        private final boolean rows;

        Inner(Grid<V> grid, int size, int outerIndex, boolean rows) {
            this.grid = grid;
            this.size = size;
            this.outerIndex = outerIndex;
            this.rows = rows;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public V get(int index) {
            Preconditions.checkElementIndex(index, size);
            if (rows) {
                return grid.get(outerIndex, index);
            } else {
                return grid.get(index, outerIndex);
            }
        }

        @Override
        public V set(int index, V newValue) {
            V old = get(index);
            if (rows) {
                if (newValue == null) {
                    grid.remove(outerIndex, index);
                } else {
                    grid.put(outerIndex, index, newValue);
                }
            } else {
                if (newValue == null) {
                    grid.remove(index, outerIndex);
                } else {
                    grid.put(index, outerIndex, newValue);
                }
            }
            return old;
        }
    }

    //-----------------------------------------------------------------------
    Cell<V> finder(int row, int column) {
        @SuppressWarnings({ "unchecked", "rawtypes" })
        Cell<V> finder = new ImmutableCell(row, column, "");
        return finder;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Grid) {
            Grid<?> other = (Grid<?>) obj;
            return rowCount() == other.rowCount() &&
                    columnCount() == other.columnCount() &&
                    cells().equals(other.cells());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return rowCount() ^ Integer.rotateLeft(columnCount(), 16) ^ cells().hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(size() * 16);
        buf.append('[').append(rowCount()).append('x').append(columnCount()).append(':');
        if (size() > 0) {
            for (Cell<V> cell : cells()) {
                buf.append(cell).append(',').append(' ');
            }
            buf.setLength(buf.length() - 2);
        }
        buf.append(']');
        return buf.toString();
    }

}
