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

import java.util.Map;

/**
 * A meta-property using a {@code Map} for storage.
 * <p>
 * This meta-property uses a {@code Map} instead of a JavaBean to store the property.
 * 
 * @author Stephen Colebourne
 */
public class MapMetaProperty<T> implements MetaProperty<Map<String, T>, T> {

    /** The map key, also known as the property name. */
    private final String key;

    /**
     * Constructor.
     * 
     * @param propertyName  the property name
     */
    public MapMetaProperty(String propertyName) {
        super();
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException("Invalid property name: " + propertyName);
        }
        key = propertyName;
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a property that binds this meta property to a specific bean.
     * 
     * @param bean  the bean to create the bound property for
     * @return the bound property
     */
    public Property<Map<String, T>, T> createProperty(Map<String, T> bean) {
        return StandardProperty.of(bean, this);
    }

    /**
     * Gets the property name.
     * 
     * @return the name of the property
     */
    public String name() {
        return key;
    }

    /**
     * Get the type of the property represented as a Class.
     * 
     * @return the type of the property
     */
    public Class<T> propertyClass() {
        // isn't erasure horrible
        return null;
    }

    /**
     * Get the type of the bean represented as a Class.
     * 
     * @return the type of the bean
     */
    @SuppressWarnings("unchecked")
    public Class<Map<String, T>> beanClass() {
        // if you think erasure is great, try fixing this
        return (Class<Map<String, T>>) (Class) Map.class;
    }

    /**
     * Gets whether the property is read-write, read-only or write-only.
     * 
     * @return the property read-write type
     */
    public ReadWriteProperty readWrite() {
        return ReadWriteProperty.READ_WRITE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the bound property for the provided bean.
     * <p>
     * This is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * However some implementations of this interface may not require an actual get method.
     * 
     * @param bean  the bean to query, not null
     * @return the value of the property on the bound bean
     * @throws UnsupportedOperationException if the property is write-only
     */
    public T get(Map<String, T> bean) {
        return bean.get(key);
    }

    /**
     * Sets the value of the bound property on the provided bean.
     * <p>
     * This is the equivalent to calling <code>setFoo()</code> on the bean itself.
     * However some implementations of this interface may not require an actual set method.
     * 
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the bound bean
     * @throws UnsupportedOperationException if the property is read-only
     */
    public void set(Map<String, T> bean, T value) {
        bean.put(key, value);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a debugging string.
     * 
     * @return a debugging string
     */
    @Override
    public String toString() {
        return "MetaProperty:" + name();
    }

}
