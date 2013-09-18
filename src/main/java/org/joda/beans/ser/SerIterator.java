/*
 *  Copyright 2001-2013 Stephen Colebourne
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

/**
 * An abstraction of collections, lists, sets and maps.
 * <p>
 * This is a plugin point that can handle Guava collections.
 *
 * @author Stephen Colebourne
 */
public interface SerIterator {

    /**
     * Gets the exposed type of the underlying.
     * 
     * @return the type, such as 'List' or 'Map'
     */
    String simpleTypeName();

    /**
     * Gets the size of the wrapped collection.
     * 
     * @return the size, -1 if unknown
     */
    int size();

    /**
     * Checks if there is a next item.
     * 
     * @return true if there is another item
     */
    boolean hasNext();

    /**
     * Advances to the next item.
     */
    void next();

    /**
     * Gets the number of occurrences of this item.
     * 
     * @return the count
     */
    int count();

    /**
     * Gets the type of the key.
     * 
     * @return the key type, null if no key
     */
    Class<?> keyType();

    /**
     * The key.
     * 
     * @return the key, may be null
     */
    Object key();

    /**
     * Gets the type of the value.
     * 
     * @return the value type, not null
     */
    Class<?> valueType();

    /**
     * The value.
     * 
     * @return the value, may be null
     */
    Object value();

}
