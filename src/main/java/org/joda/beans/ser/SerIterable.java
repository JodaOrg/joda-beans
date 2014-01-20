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
package org.joda.beans.ser;

import java.util.List;

/**
 * An abstraction over collections, lists, sets and maps.
 * <p>
 * This is a plugin point that can handle Guava collections.
 *
 * @author Stephen Colebourne
 */
public interface SerIterable {

    /**
     * Obtains an iterator over the data.
     * 
     * @return the iterator, not null
     */
    SerIterator iterator();

    /**
     * Adds an item to the builder.
     * 
     * @param key  the key, such as for a map, null if no key
     * @param value  the value, such as for a map or list value, may be null
     * @param count  the count, such as for a multiset, typically one or greater
     */
    void add(Object key, Object value, int count);

    /**
     * Builds the final collection.
     * 
     * @return the build collection, not null
     */
    Object build();

    /**
     * Is the iterable map-like, as opposed to collection-like.
     * 
     * @return true if map-like, false if collection-like
     */
    boolean isMapLike();

    /**
     * Gets the type of the key.
     * 
     * @return the key type, null if no key
     */
    Class<?> keyType();

    /**
     * Gets the type of the value.
     * 
     * @return the value type, not null
     */
    Class<?> valueType();

    /**
     * Gets the generic parameters of the value type.
     * 
     * @return the generic parameters of the value type, not null
     */
    List<Class<?>> valueTypeTypes();

}
