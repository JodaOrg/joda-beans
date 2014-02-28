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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.joda.beans.collect.Grid.Cell;

/**
 * Test abstract Grid.
 */
public abstract class AbstractTestGrid {

    private static final Object DUMMY = new Object() {};

    protected <R> void checkGrid(Grid<R> test) {
        assertEquals(test.size(), 0);
        assertEquals(test.isEmpty(), true);
        assertEquals(test.isFull(), test.rowCount() == 0 && test.columnCount() == 0);
        for (int i = -1; i < test.rowCount(); i++) {
            for (int j = -1; j < test.columnCount(); j++) {
                if (i < 0 || j < 0 || i >= test.rowCount() || j >= test.columnCount()) {
                    assertEquals(test.exists(i, j), false);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                } else {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                    assertEquals(test.row(i).get(j), null);
                    assertEquals(test.rows().get(i).get(j), null);
                    assertEquals(test.column(j).get(i), null);
                    assertEquals(test.columns().get(j).get(i), null);
                    assertEquals(test.row(i).size(), test.columnCount());
                    assertEquals(test.rows().size(), test.rowCount());
                    assertEquals(test.rows().get(i).size(), test.columnCount());
                    assertEquals(test.column(j).size(), test.rowCount());
                    assertEquals(test.columns().size(), test.columnCount());
                    assertEquals(test.columns().get(j).size(), test.rowCount());
                }
            }
        }
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(DUMMY), false);
        
        assertEquals(test.cells().size(), 0);
        Iterator<Cell<R>> cellIt = test.cells().iterator();
        assertIteratorEnd(cellIt);
        
        assertEquals(test.values().size(), 0);
        Iterator<R> valueIt = test.values().iterator();
        assertIteratorEnd(valueIt);
    }

    protected <R> void checkGrid(Grid<R> test, int row1, int column1, R value1) {
        assertEquals(test.size(), 1);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isFull(), test.rowCount() * test.columnCount() == 1);
        for (int i = -1; i < test.rowCount(); i++) {
            for (int j = -1; j < test.columnCount(); j++) {
                if (i < 0 || j < 0 || i >= test.rowCount() || j >= test.columnCount()) {
                    assertEquals(test.exists(i, j), false);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                } else if (i == row1 && j == column1) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value1);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value1));
                    assertEquals(test.row(i).get(j), value1);
                    assertEquals(test.rows().get(i).get(j), value1);
                    assertEquals(test.column(j).get(i), value1);
                    assertEquals(test.columns().get(j).get(i), value1);
                } else {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                    assertEquals(test.row(i).get(j), null);
                    assertEquals(test.rows().get(i).get(j), null);
                    assertEquals(test.column(j).get(i), null);
                    assertEquals(test.columns().get(j).get(i), null);
                    assertEquals(test.row(i).size(), test.columnCount());
                    assertEquals(test.rows().size(), test.rowCount());
                    assertEquals(test.rows().get(i).size(), test.columnCount());
                    assertEquals(test.column(j).size(), test.rowCount());
                    assertEquals(test.columns().size(), test.columnCount());
                    assertEquals(test.columns().get(j).size(), test.rowCount());
                }
            }
        }
        assertEquals(test.containsValue(value1), true);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(DUMMY), false);
        
        assertEquals(test.cells().size(), 1);
        Iterator<Cell<R>> cellIt = test.cells().iterator();
        assertEquals(cellIt.hasNext(), true);
        Cell<R> cell = cellIt.next();
        assertEquals(cell.getRow(), row1);
        assertEquals(cell.getColumn(), column1);
        assertEquals(cell.getValue(), value1);
        assertIteratorEnd(cellIt);
        
        assertEquals(test.values().size(), 1);
        Iterator<R> valueIt = test.values().iterator();
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value1);
        assertIteratorEnd(valueIt);
    }

    protected <R> void checkGrid(Grid<R> test, int row1, int column1, R value1, int row2, int column2, R value2) {
        assertEquals(test.size(), 2);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isFull(), test.rowCount() * test.columnCount() == 2);
        for (int i = -1; i < test.rowCount(); i++) {
            for (int j = -1; j < test.columnCount(); j++) {
                if (i < 0 || j < 0 || i >= test.rowCount() || j >= test.columnCount()) {
                    assertEquals(test.exists(i, j), false);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                } else if (i == row1 && j == column1) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value1);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value1));
                    assertEquals(test.row(i).get(j), value1);
                    assertEquals(test.rows().get(i).get(j), value1);
                    assertEquals(test.column(j).get(i), value1);
                    assertEquals(test.columns().get(j).get(i), value1);
                } else if (i == row2 && j == column2) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value2);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value2));
                    assertEquals(test.row(i).get(j), value2);
                    assertEquals(test.rows().get(i).get(j), value2);
                    assertEquals(test.column(j).get(i), value2);
                    assertEquals(test.columns().get(j).get(i), value2);
                } else {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                    assertEquals(test.row(i).get(j), null);
                    assertEquals(test.rows().get(i).get(j), null);
                    assertEquals(test.column(j).get(i), null);
                    assertEquals(test.columns().get(j).get(i), null);
                    assertEquals(test.row(i).size(), test.columnCount());
                    assertEquals(test.rows().size(), test.rowCount());
                    assertEquals(test.rows().get(i).size(), test.columnCount());
                    assertEquals(test.column(j).size(), test.rowCount());
                    assertEquals(test.columns().size(), test.columnCount());
                    assertEquals(test.columns().get(j).size(), test.rowCount());
                }
            }
        }
        assertEquals(test.containsValue(value1), true);
        assertEquals(test.containsValue(value2), true);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(DUMMY), false);
        
        assertEquals(test.cells().size(), 2);
        Iterator<Cell<R>> cellIt = test.cells().iterator();
        assertEquals(cellIt.hasNext(), true);
        Cell<R> cell = cellIt.next();
        assertEquals(cell.getRow(), row1);
        assertEquals(cell.getColumn(), column1);
        assertEquals(cell.getValue(), value1);
        assertEquals(cellIt.hasNext(), true);
        cell = cellIt.next();
        assertEquals(cell.getRow(), row2);
        assertEquals(cell.getColumn(), column2);
        assertEquals(cell.getValue(), value2);
        assertIteratorEnd(cellIt);
        
        assertEquals(test.values().size(), 2);
        Iterator<R> valueIt = test.values().iterator();
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value1);
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value2);
        assertIteratorEnd(valueIt);
    }

    protected <R> void checkGrid(Grid<R> test, int row1, int column1, R value1, int row2, int column2, R value2, int row3, int column3, R value3) {
        assertEquals(test.size(), 3);
        assertEquals(test.isEmpty(), false);
        assertEquals(test.isFull(), test.rowCount() * test.columnCount() == 3);
        for (int i = -1; i < test.rowCount(); i++) {
            for (int j = -1; j < test.columnCount(); j++) {
                if (i < 0 || j < 0 || i >= test.rowCount() || j >= test.columnCount()) {
                    assertEquals(test.exists(i, j), false);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                } else if (i == row1 && j == column1) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value1);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value1));
                    assertEquals(test.row(i).get(j), value1);
                    assertEquals(test.rows().get(i).get(j), value1);
                    assertEquals(test.column(j).get(i), value1);
                    assertEquals(test.columns().get(j).get(i), value1);
                } else if (i == row2 && j == column2) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value2);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value2));
                    assertEquals(test.row(i).get(j), value2);
                    assertEquals(test.rows().get(i).get(j), value2);
                    assertEquals(test.column(j).get(i), value2);
                    assertEquals(test.columns().get(j).get(i), value2);
                } else if (i == row3 && j == column3) {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), true);
                    assertEquals(test.get(i, j), value3);
                    assertEquals(test.cell(i, j), ImmutableCell.of(i, j, value3));
                    assertEquals(test.row(i).get(j), value3);
                    assertEquals(test.rows().get(i).get(j), value3);
                    assertEquals(test.column(j).get(i), value3);
                    assertEquals(test.columns().get(j).get(i), value3);
                } else {
                    assertEquals(test.exists(i, j), true);
                    assertEquals(test.contains(i, j), false);
                    assertEquals(test.get(i, j), null);
                    assertEquals(test.cell(i, j), null);
                    assertEquals(test.row(i).get(j), null);
                    assertEquals(test.rows().get(i).get(j), null);
                    assertEquals(test.column(j).get(i), null);
                    assertEquals(test.columns().get(j).get(i), null);
                    assertEquals(test.row(i).size(), test.columnCount());
                    assertEquals(test.rows().size(), test.rowCount());
                    assertEquals(test.rows().get(i).size(), test.columnCount());
                    assertEquals(test.column(j).size(), test.rowCount());
                    assertEquals(test.columns().size(), test.columnCount());
                    assertEquals(test.columns().get(j).size(), test.rowCount());
                }
            }
        }
        assertEquals(test.containsValue(value1), true);
        assertEquals(test.containsValue(value2), true);
        assertEquals(test.containsValue(value3), true);
        assertEquals(test.containsValue(null), false);
        assertEquals(test.containsValue(DUMMY), false);
        
        assertEquals(test.cells().size(), 3);
        Iterator<Cell<R>> cellIt = test.cells().iterator();
        assertEquals(cellIt.hasNext(), true);
        Cell<R> cell = cellIt.next();
        assertEquals(cell.getRow(), row1);
        assertEquals(cell.getColumn(), column1);
        assertEquals(cell.getValue(), value1);
        assertEquals(cellIt.hasNext(), true);
        cell = cellIt.next();
        assertEquals(cell.getRow(), row2);
        assertEquals(cell.getColumn(), column2);
        assertEquals(cell.getValue(), value2);
        assertEquals(cellIt.hasNext(), true);
        cell = cellIt.next();
        assertEquals(cell.getRow(), row3);
        assertEquals(cell.getColumn(), 1);
        assertEquals(cell.getValue(), value3);
        assertIteratorEnd(cellIt);
        
        assertEquals(test.values().size(), 3);
        Iterator<R> valueIt = test.values().iterator();
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value1);
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value2);
        assertEquals(valueIt.hasNext(), true);
        assertEquals(valueIt.next(), value3);
        assertIteratorEnd(valueIt);
    }

    private void assertIteratorEnd(Iterator<?> it) {
        assertEquals(it.hasNext(), false);
        try {
            it.next();
            fail();
        } catch (NoSuchElementException ex) {
            // expected
        }
    }

}
