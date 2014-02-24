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

import com.google.common.base.Objects;

/**
 * Abstract implementation of the {@code Grid.Cell} data structure.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
abstract class AbstractCell<V> implements Cell<V> {

    /**
     * Restricted constructor.
     */
    AbstractCell() {
    }

    //-----------------------------------------------------------------------
    @Override
    public int compareTo(Cell<V> other) {
        int thisRow = getRow();
        int otherRow = other.getRow();
        int cmp = (thisRow < otherRow ? -1 : (thisRow > otherRow ? 1 : 0));
        if (cmp == 0) {
            int thisCol = getColumn();
            int otherCol = other.getColumn();
            cmp = (thisCol < otherCol ? -1 : (thisCol > otherCol ? 1 : 0));
        }
        return cmp;
    }

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
        return getValue().hashCode() ^ getRow() ^ Integer.rotateLeft(getColumn(), 16);
    }

    @Override
    public String toString() {
        return "(" + getRow() + "," + getColumn() + ")=" + getValue();
    }

}
