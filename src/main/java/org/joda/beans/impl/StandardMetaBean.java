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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;

/**
 * A standard meta-bean implementation.
 * <p>
 * This is the standard implementation of a meta-bean.
 * It requires that the bean implements {@code Bean} and has a no-arguments constructor.
 * 
 * @param <B>  the type of the bean
 * @author Stephen Colebourne
 */
public final class StandardMetaBean<B> implements MetaBean<B> {

    /** The bean class. */
    private final Class<B> beanClass;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<B, Object>> metaPropertyMap;

    /**
     * Factory to create a meta-bean avoiding duplicate generics.
     * 
     * @param beanClass  the bean class, not null
     */
    public static <B extends Bean<B>> StandardMetaBean<B> of(Class<B> beanClass) {
        return new StandardMetaBean<B>(beanClass);
    }

    /**
     * Creates a property binding the bean to the meta-property.
     * 
     * @param beanClass  the bean class, not null
     */
    @SuppressWarnings("unchecked")
    private StandardMetaBean(Class<B> beanClass) {
        if (beanClass == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        this.beanClass = beanClass;
        Map<String, MetaProperty<B, Object>> map = new HashMap<String, MetaProperty<B, Object>>();
        Field[] fields = beanClass.getFields();
        for (Field field : fields) {
            if (MetaProperty.class.isAssignableFrom(field.getType()) &&
                    Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers())) {
                MetaProperty<B, Object> mp;
                try {
                    mp = (MetaProperty<B, Object>) field.get(null);
                } catch (IllegalArgumentException ex) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName(), ex);
                } catch (IllegalAccessException ex) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName(), ex);
                }
                if (mp == null) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName() + ": Value must not be null");
                }
                map.put(mp.name(), mp);
            }
        }
        
        this.metaPropertyMap = map;
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public Bean<B> createBean() {
        try {
            Object obj = beanClass.newInstance();
            if (obj instanceof Bean<?>) {
                return (Bean<B>) obj;
            }
            throw new UnsupportedOperationException("TODO: Write WrappingBean impl");
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + name(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + name(), ex);
        }
    }

    @Override
    public PropertyMap<B> createPropertyMap(Bean<B> bean) {
        return StandardPropertyMap.of(bean);
    }

    //-----------------------------------------------------------------------
    @Override
    public String name() {
        return beanClass.getName();
    }

    @Override
    public Class<B> beanClass() {
        return beanClass;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean metaPropertyExists(String propertyName) {
        return metaPropertyMap.containsKey(propertyName);
    }

    @Override
    public MetaProperty<B, Object> metaProperty(String propertyName) {
        MetaProperty<B, Object> metaProperty = metaPropertyMap.get(propertyName);
        if (metaProperty == null) {
            throw new NoSuchElementException("Property not found: " + propertyName);
        }
        return metaProperty;
    }

    @Override
    public Map<String, MetaProperty<B, Object>> metaPropertyMap() {
        return metaPropertyMap;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof StandardMetaBean<?>) {
            StandardMetaBean<?> other = (StandardMetaBean<?>) obj;
            return this.beanClass.equals(other.beanClass);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return beanClass.hashCode() + 3;
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, never null
     */
    @Override
    public String toString() {
        return "MetaBean:" + name();
    }

}
