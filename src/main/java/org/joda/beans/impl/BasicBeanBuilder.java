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
package org.joda.beans.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;

/**
 * Basic implementation of {@code BeanBuilder} that wraps a real bean.
 * <p>
 * This approach saves creating a temporary map, but is only suitable if the
 * bean has a no-arg constructor and allows properties to be set.
 * 
 * @author Stephen Colebourne
 */
public class BasicBeanBuilder<T extends Bean> implements BeanBuilder<T> {

    /**
     * The actual target bean.
     */
    private final T bean;

    /**
     * Constructs the builder wrapping the target bean.
     * 
     * @param bean  the target bean, not null
     */
    public BasicBeanBuilder(T bean) {
        if (bean == null) {
            throw new NullPointerException("Bean must not be null");
        }
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the target bean.
     * 
     * @return the target bean, not null
     */
    protected T getTargetBean() {
        return bean;
    }

    /**
     * Gets the current value of the property.
     * 
     * @param propertyName  the property name, not null
     * @return the current value in the builder, null if not found or value is null
     */
    protected BeanBuilder<T> get(String propertyName) {
        bean.property(propertyName).get();
        return this;
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<T> set(String propertyName, Object value) {
        bean.property(propertyName).set(value);
        return this;
    }

    @Override
    public BeanBuilder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
        for (Entry<String, ? extends Object> entry : propertyValueMap.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    @Override
    public T build() {
        validate(bean);
        return bean;
    }

    /**
     * Hook to allow a subclass to validate the bean.
     * 
     * @param bean  the bean to validate, not null
     */
    protected void validate(T bean) {
        // override to validate the bean
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the builder.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "BeanBuilder for " + bean.metaBean().beanName();
    }

}
