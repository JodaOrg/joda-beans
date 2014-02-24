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
 * Mock cell.
 */
public class MockBadGrid extends AbstractGrid<String> {
    
    private final int row;
    private final int column;
    private final String value;

    public MockBadGrid(int row, int column, String value) {
        this.row = row;
        this.column = column;
        this.value = value;
    }

    @Override
    public SortedSet<Cell<String>> cells() {
        TreeSet<Cell<String>> set = new TreeSet<Cell<String>>();
        set.add(new AbstractCell<String>() {
            @Override
            public int getRow() {
                return row;
            }

            @Override
            public int getColumn() {
                return column;
            }

            @Override
            public String getValue() {
                return value;
            }
        });
        return set;
    }

    @Override
    public void clear() {
    }

    @Override
    public void put(int row, int column, String value) {
    }

    @Override
    public void putAll(Grid<? extends String> grid) {
    }

    @Override
    public boolean remove(int row, int column) {
        return false;
    }

}
