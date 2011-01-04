/*
 *  Copyright 2001-2011 Stephen Colebourne
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
package org.joda.beans;

import java.util.NoSuchElementException;

/**
 * A bean consisting of a set of properties.
 * <p>
 * The implementation may be any class, but is typically a standard JavaBean
 * with get/set methods. Alternate implementations might store the properties
 * in another data structure such as a map.
 * 
 * @author Stephen Colebourne
 */
public interface Bean {

    /**
     * Gets the meta-bean representing the parts of the bean that are
     * common across all instances, such as the set of meta-properties.
     * 
     * @return the meta-bean, not null
     */
    MetaBean metaBean();

    /**
     * Checks if a property exists.
     * 
     * @param propertyName  the property name to check, null returns false
     * @return true if the property exists
     */
    boolean propertyExists(String propertyName);

    /**
     * Gets a property by name.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, null throws NoSuchElementException
     * @return the property, not empty
     * @throws NoSuchElementException if the property name is invalid
     */
    <R> Property<R> property(String propertyName);

    /**
     * Gets the map of properties, keyed by property name.
     * 
     * @return the unmodifiable map of property objects, not null
     */
    PropertyMap propertyMap();

}
