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
package org.joda.beans.impl.reflection;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.impl.BasicBeanBuilder;

/**
 * A meta-bean implementation that uses reflection.
 * <p>
 * This is implementation of a meta-bean can be used directly by applications without code generation.
 * It requires that the bean implements {@code Bean} and has a no-arguments constructor.
 * Therefore, it is only suitable for mutable beans.
 * <p>
 * Typically, the meta-bean will be created as a public static final constant.
 * Only one method from {@link Bean} needs to be implemented, which simply returns the meta-bean.
 * 
 * @author Stephen Colebourne
 * @param <T>  the type of the bean
 */
public final class ReflectiveMetaBean<T extends Bean> implements TypedMetaBean<T> {

    /** The bean type. */
    private final Class<T> beanType;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;

    /**
     * Create a meta-bean and meta properties.
     * <p>
     * The meta-properties will be created from the property names by searching for a getter and setter.
     * 
     * @param <B>  the type of the bean
     * @param beanClass  the bean class, not null
     * @param propertyNames  the property names, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> ReflectiveMetaBean<B> of(Class<B> beanClass, String... propertyNames) {
        return new ReflectiveMetaBean<>(beanClass, propertyNames);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param propertyNames  the property names, not null
     */
    private ReflectiveMetaBean(Class<T> beanType, String[] propertyNames) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        if (propertyNames == null) {
            throw new NullPointerException("Property names must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<?>> map = new LinkedHashMap<>();
        for (String name : propertyNames) {
            map.put(name, new ReflectiveMetaProperty<>(this, beanType, name));
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isBuildable() {
        try {
            beanType.getDeclaredConstructor().newInstance();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public BeanBuilder<T> builder() {
        try {
            T bean = beanType.getDeclaredConstructor().newInstance();
            return new BasicBeanBuilder<>(bean);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + beanName(), ex);
        }
    }

    @Override
    public Class<T> beanType() {
        return beanType;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        return metaPropertyMap;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReflectiveMetaBean) {
            ReflectiveMetaBean<?> other = (ReflectiveMetaBean<?>) obj;
            return this.beanType.equals(other.beanType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return beanType.hashCode() + 3;
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaBean:" + beanName();
    }

}
