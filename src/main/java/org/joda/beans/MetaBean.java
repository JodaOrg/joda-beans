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
package org.joda.beans;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A meta-bean, defining those aspects of a bean which are not specific
 * to a particular instance, such as the type and set of meta-properties.
 * <p>
 * This interface can be thought of as the equivalent of {@link Class} but for beans.
 * In most cases the meta-bean will be code generated and the concrete class will have additional methods.
 * 
 * @author Stephen Colebourne
 */
public interface MetaBean {

    /**
     * Creates a bean builder that can be used to create an instance of this bean.
     * <p>
     * The builder is used in two main ways.
     * The first is to allow immutable beans to be constructed.
     * The second is to enable automated tools like serialization/deserialization.
     * <p>
     * The builder can be thought of as a {@code Map} of {@link MetaProperty} to value.
     * Note that the implementation is not necessarily an actual map.
     * 
     * @return the bean builder, not null
     * @throws UnsupportedOperationException if the bean cannot be created
     */
    BeanBuilder<? extends Bean> builder();

    /**
     * Creates a map of properties for the specified bean.
     * <p>
     * This allows the entire set of properties of the bean to be exposed as a {@code Map}.
     * The map is keyed by the property name and has {@link Property} instances as values.
     * Call {@link PropertyMap#flatten()} to convert the map to hold the actual values from the bean.
     * 
     * @param bean  the bean to create the map for, not null
     * @return the created property map, not null
     */
    PropertyMap createPropertyMap(Bean bean);

    //-----------------------------------------------------------------------
    /**
     * Gets the bean name, which is normally the fully qualified class name of the bean.
     * <p>
     * This is primarily used for human-readable output.
     * 
     * @return the name of the bean, not empty
     */
    String beanName();

    /**
     * Get the type of the bean, represented as a {@code Class}.
     * <p>
     * A {@code MetaBean} can be thought of as the equivalent of {@link Class} but for beans.
     * This method allows the actual {@code Class} instance of the bean to be obtained.
     * 
     * @return the type of the bean, not null
     */
    Class<? extends Bean> beanType();

    //-----------------------------------------------------------------------
    /**
     * Counts the number of properties.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method returns the count of properties.
     * 
     * @return the number of properties
     */
    int metaPropertyCount();

    /**
     * Checks if a property exists.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method checks whether there is a property with the specified name.
     * 
     * @param propertyName  the property name to check, null returns false
     * @return true if the property exists
     */
    boolean metaPropertyExists(String propertyName);

    /**
     * Gets a meta-property by name.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method returns the property with the specified name.
     * <p>
     * The base interface throws an exception if the name is not recognised.
     * By contrast, the {@code DynamicMetaBean} subinterface creates the property on demand.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, not null
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
    Iterable<MetaProperty<?>> metaPropertyIterable();

    /**
     * Gets the map of meta-properties, keyed by property name.
     * <p>
     * Where possible, use {@link #metaPropertyIterable()} instead as it typically has better performance.
     * 
     * @return the unmodifiable map of meta property objects, not null
     */
    Map<String, MetaProperty<?>> metaPropertyMap();

}
