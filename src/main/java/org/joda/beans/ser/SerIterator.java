/*
 *  Copyright 2001-present Stephen Colebourne
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
package org.joda.beans.ser;

import java.util.List;

/**
 * An abstraction of collections, lists, sets and maps.
 * <p>
 * This is a plugin point that can handle Guava collections.
 *
 * @author Stephen Colebourne
 */
public abstract class SerIterator {

    /**
     * Gets the meta type of the underlying.
     * 
     * @return the type, such as 'List' or 'Map'
     */
    public abstract String metaTypeName();

    /**
     * Checks if the meta type of the underlying is required.
     * 
     * @return true if generic inspection is insufficient to determine the collection
     */
    public abstract boolean metaTypeRequired();

    /**
     * Gets the size of one dimension of the wrapped collection.
     * 
     * @param dimension  the dimension, 0 for row, 1 for column
     * @return the size, -1 if unknown
     */
    public int dimensionSize(int dimension) {
        return -1;
    }

    /**
     * Gets the size of the wrapped collection.
     * 
     * @return the size, -1 if unknown
     */
    public abstract int size();

    /**
     * Gets the category of iterable.
     * 
     * @return the category, not null
     */
    public SerCategory category() {
        return SerCategory.COLLECTION;
    }

    /**
     * Checks if there is a next item.
     * 
     * @return true if there is another item
     */
    public abstract boolean hasNext();

    /**
     * Advances to the next item.
     */
    public abstract void next();

    /**
     * Gets the number of occurrences of this item.
     * 
     * @return the count
     */
    public int count() {
        return 1;
    }

    /**
     * Gets the type of the key.
     * 
     * @return the key type, null if no key
     */
    public Class<?> keyType() {
        return null;
    }

    /**
     * The key.
     * 
     * @return the key, may be null
     */
    public Object key() {
        return null;
    }

    /**
     * Gets the type of the column.
     * 
     * @return the column type, null if no column
     */
    public Class<?> columnType() {
        return null;
    }

    /**
     * The column.
     * 
     * @return the key, may be null
     */
    public Object column() {
        return null;
    }

    /**
     * Gets the type of the value.
     * 
     * @return the value type, not null
     */
    public abstract Class<?> valueType();

    /**
     * Gets the generic parameters of the value type.
     * 
     * @return the generic parameters of the value type, not null
     */
    public abstract List<Class<?>> valueTypeTypes();

    /**
     * The value.
     * 
     * @return the value, may be null
     */
    public abstract Object value();

    /**
     * The value at a row/column.
     * 
     * @param row  the row
     * @param column  the column
     * @return the value
     */
    public Object value(int row, int column) {
        return null;
    }

}
