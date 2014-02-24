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
 * Immutable implementation of the {@code Grid} data structure storing no cells.
 * 
 * @param <V> the type of the value
 * @author Stephen Colebourne
 */
final class EmptyGrid<V> extends ImmutableGrid<V> {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * Restricted constructor.
     */
    EmptyGrid() {
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean contains(int row, int column) {
        return false;
    }

    @Override
    public boolean containsValue(Object valueToFind) {
        return false;
    }

    @Override
    public V get(int row, int column) {
        return null;
    }

    @Override
    public ImmutableSortedSet<Cell<V>> cells() {
        return ImmutableSortedSet.of();
    }

    @Override
    public ImmutableList<V> values() {
        return ImmutableList.of();
    }

}
