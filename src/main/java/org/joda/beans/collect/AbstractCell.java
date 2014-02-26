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

import java.util.Comparator;

import org.joda.beans.collect.Grid.Cell;

import com.google.common.base.Objects;

/**
 * Abstract implementation of the {@code Grid.Cell} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
abstract class AbstractCell<V> implements Cell<V> {

    /**
     * Compare by row then column.
     * 
     * @param <R> the type of the value
     * @return the comparator, not null
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    static final <R> Comparator<Cell<R>> comparator() {
        return (Comparator<Cell<R>>) (Comparator) COMPARATOR;
    }
    /**
     * Compare by row then column.
     */
    private static final Comparator<Cell<?>> COMPARATOR = new Comparator<Cell<?>>() {
        @Override
        public int compare(Cell<?> cell1, Cell<?> cell2) {
            int thisRow = cell1.getRow();
            int otherRow = cell2.getRow();
            int cmp = (thisRow < otherRow ? -1 : (thisRow > otherRow ? 1 : 0));
            if (cmp == 0) {
                int thisCol = cell1.getColumn();
                int otherCol = cell2.getColumn();
                cmp = (thisCol < otherCol ? -1 : (thisCol > otherCol ? 1 : 0));
            }
            return cmp;
        }
    };

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    AbstractCell() {
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equalRowColumn(int row, int column) {
        return row == getRow() && column == getColumn();
    }

    @Override
    public boolean equalValue(Object value) {
        return Objects.equal(value, getValue());
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Cell) {
            Cell<?> other = (Cell<?>) obj;
            return getRow() == other.getRow() && getColumn() == other.getColumn()
                    && Objects.equal(getValue(), other.getValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getRow() ^ Integer.rotateLeft(getColumn(), 16) ^ getValue().hashCode();
    }

    @Override
    public String toString() {
        return "(" + getRow() + "," + getColumn() + ")=" + getValue();
    }

}
