/*
 *  Copyright 2001-2010 Stephen Colebourne
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
 * For a JavaBean, this will ultimately wrap a get/set methods of the bean.
 * Alternate implementations might store the properties in another data structure
 * such as a map.
 * 
 * @author Stephen Colebourne
 */
public interface Bean {

    /**
     * Gets the data store for this bean.
     * <p>
     * There are multiple ways that this bean can be implemented.
     * The simplest is to make a regular JavaBean implement this interface, in which
     * case this method would return {@code this}.
     * More complicated approaches use different data stores.
     * 
     * @param <B>  the bean type
     * @return the bean itself, not null
     */
    <B> B beanData();

    /**
     * Gets the meta-bean representing the parts of the bean that are
     * common across all instances, such as the set of meta-properties.
     * 
     * @return the meta-bean, not null
     */
    MetaBean metaBean();

    //-----------------------------------------------------------------------
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
     * @param propertyName  the property name to retrieve, null throws NoSuchElementException
     * @return the property, not empty
     * @throws NoSuchElementException if the property name is invalid
     */
    Property<Object> property(String propertyName);

    /**
     * Gets the map of properties, keyed by property name.
     * 
     * @return the unmodifiable map of property objects, not null
     */
    PropertyMap propertyMap();

}
