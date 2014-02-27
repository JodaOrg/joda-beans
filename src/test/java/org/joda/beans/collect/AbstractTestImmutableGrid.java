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

import java.util.Iterator;

import org.joda.beans.collect.Grid.Cell;
import org.testng.annotations.Test;

/**
 * Test abstract Grid.
 */
public abstract class AbstractTestImmutableGrid extends AbstractTestGrid {

    protected abstract ImmutableGrid<String> createNonEmpty();

    //-----------------------------------------------------------------------
    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_clear() {
        createNonEmpty().clear();
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_put() {
        createNonEmpty().put(0, 0, "Hello");
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_putAll() {
        createNonEmpty().putAll(ImmutableGrid.<String>of());
    }

    @SuppressWarnings("deprecation")
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_immutable_remove() {
        createNonEmpty().remove(0, 0);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_cells_add() {
        ImmutableGrid<String> test = createNonEmpty();
        Iterator<Cell<String>> it = test.cells().iterator();
        it.next();
        it.remove();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_cells_remove() {
        createNonEmpty().cells().add(ImmutableCell.of(0, 0, "Rejected"));
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_cells_clear() {
        createNonEmpty().cells().clear();
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_rows_clear() {
        createNonEmpty().rows().clear();
    }

    @Test(expectedExceptions = UnsupportedOperationException.class)
    public void test_columns_clear() {
        createNonEmpty().rows().clear();
    }

}
