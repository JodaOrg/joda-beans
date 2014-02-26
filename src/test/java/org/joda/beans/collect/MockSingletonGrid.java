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

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Mock grid.
 */
public class MockSingletonGrid extends AbstractGrid<String> {
    
    private final int rowCount;
    private final int columnCount;
    private final int row;
    private final int column;
    private final String value;

    public MockSingletonGrid(int rowCount, int columnCount, int row, int column, String value) {
        this.rowCount = rowCount;
        this.columnCount = columnCount;
        this.row = row;
        this.column = column;
        this.value = value;
    }

    @Override
    public int rowCount() {
        return rowCount;
    }

    @Override
    public int columnCount() {
        return columnCount;
    }

    @Override
    public SortedSet<Cell<String>> cells() {
        TreeSet<Cell<String>> set = new TreeSet<Cell<String>>();
        set.add(new MockCell(row, column, value));
        return set;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void put(int row, int column, String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Grid<? extends String> grid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(int row, int column) {
        throw new UnsupportedOperationException();
    }

}
