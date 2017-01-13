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
 * An abstraction over collections, lists, sets and maps.
 * <p>
 * This is a plugin point that can handle Guava collections.
 *
 * @author Stephen Colebourne
 */
public abstract class SerIterable {

    /**
     * Obtains an iterator over the data.
     * 
     * @return the iterator, not null
     */
    public abstract SerIterator iterator();

    /**
     * Sets the dimensions of the wrapped collection.
     * 
     * @param dimensions  the dimension, 0 for row, 1 for column
     */
    public void dimensions(int[] dimensions) {
        throw new IllegalArgumentException("Iterable does not support dimensinos");
    }

    /**
     * Adds an item to the builder.
     * 
     * @param key  the key, such as for a map, null if no key
     * @param column  the column, such as for a table, null if no column
     * @param value  the value, such as for a map or list value, may be null
     * @param count  the count, such as for a multiset, typically one or greater
     */
    public abstract void add(Object key, Object column, Object value, int count);

    /**
     * Builds the final collection.
     * 
     * @return the build collection, not null
     */
    public abstract Object build();

    /**
     * Gets the category of iterable.
     * 
     * @return the category, not null
     */
    public SerCategory category() {
        return SerCategory.COLLECTION;
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
     * Gets the type of the column.
     * 
     * @return the column type, null if no column
     */
    public Class<?> columnType() {
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

}
