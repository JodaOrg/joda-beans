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

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A meta-bean, defining those aspects of a bean which are not specific
 * to a particular instance, such as the type and set of meta-properties.
 * 
 * @author Stephen Colebourne
 */
public interface MetaBean {

    /**
     * Creates a bean builder that can be used to create an instance of this bean.
     * 
     * @return the bean builder, not null
     * @throws UnsupportedOperationException if the bean cannot be created
     */
    BeanBuilder<? extends Bean> builder();

    /**
     * Creates a map of properties for the specified bean.
     * 
     * @param bean  the bean to create the map for, not null
     * @return the created property map, not null
     */
    PropertyMap createPropertyMap(Bean bean);

    //-----------------------------------------------------------------------
    /**
     * Gets the bean name, which is normally the fully qualified class name of the bean.
     * 
     * @return the name of the bean, not empty
     */
    String beanName();

    /**
     * Get the type of the bean represented as a {@code Class}.
     * 
     * @return the type of the bean, not null
     */
    Class<? extends Bean> beanType();

    //-----------------------------------------------------------------------
    /**
     * Counts the number of properties.
     * 
     * @return the number of properties
     */
    int metaPropertyCount();

    /**
     * Checks if a property exists.
     * 
     * @param propertyName  the property name to check, null returns false
     * @return true if the property exists
     */
    boolean metaPropertyExists(String propertyName);

    /**
     * Gets a meta-property by name.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, null throws NoSuchElementException
     * @return the meta property, not null
     * @throws NoSuchElementException if the property name is invalid
     */
    <R> MetaProperty<R> metaProperty(String propertyName);

    /**
     * Gets an iterator of meta-properties.
     * <p>
     * This method returns an {@code Iterable}, which is simpler than a {@code Map}.
     * As a result, implementations may be able to optimise, and so this method should be
     * preferred to {@link #metaPropertyMap()} where a choice is possible.
     * 
     * @return the unmodifiable map of meta property objects, not null
     */
    Iterable<MetaProperty<Object>> metaPropertyIterable();

    /**
     * Gets the map of meta-properties, keyed by property name.
     * <p>
     * Where possible, use {@link #metaPropertyIterable()} instead.
     * 
     * @return the unmodifiable map of meta property objects, not null
     */
    Map<String, MetaProperty<Object>> metaPropertyMap();

}
