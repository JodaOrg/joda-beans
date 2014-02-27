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

import org.testng.annotations.Test;

/**
 * Test SparseGrid.
 */
@Test
public class TestSparseGrid extends AbstractTestMutableGrid {

    @Override
    protected Grid<String> create3x3() {
        return SparseGrid.create(3, 3);
    }

    @Override
    protected Grid<String> create(int rowCount, int columnCount) {
        return SparseGrid.create(rowCount, columnCount);
    }

    @Override
    protected Grid<String> create(Grid<String> grid) {
        return SparseGrid.create(grid);
    }

}
