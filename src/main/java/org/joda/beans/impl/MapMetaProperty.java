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
package org.joda.beans.impl;

import java.util.Map;

import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyReadWrite;

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
    @Override
    public Property<Map<String, T>, T> createProperty(Map<String, T> bean) {
        return StandardProperty.of(bean, this);
    }

    @Override
    public String name() {
        return key;
    }

    @Override
    public Class<T> propertyClass() {
        // isn't erasure horrible
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<Map<String, T>> beanClass() {
        // if you think erasure is great, try fixing this
        return (Class<Map<String, T>>) (Class) Map.class;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return PropertyReadWrite.READ_WRITE;
    }

    //-----------------------------------------------------------------------
    @Override
    public T get(Map<String, T> bean) {
        return bean.get(key);
    }

    @Override
    public void set(Map<String, T> bean, T value) {
        bean.put(key, value);
    }

    @Override
    public T put(Map<String,T> bean, T value) {
        T old = get(bean);
        set(bean, value);
        return old;
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
