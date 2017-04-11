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
package org.joda.beans.impl;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;

/**
 * Implementation of {@code BeanBuilder} that buffers data in a local map.
 * <p>
 * This is useful for cases where the builder data might be manipulated before
 * the final build. The buffer can be directly mutated.
 * 
 * @author Stephen Colebourne
 * @param <T>  the bean type
 */
public class BufferingBeanBuilder<T extends Bean>
        implements BeanBuilder<T> {

    /**
     * The target meta-bean.
     */
    private final MetaBean metaBean;
    /**
     * The buffered data.
     */
    private final ConcurrentMap<MetaProperty<?>, Object> buffer = new ConcurrentHashMap<>();

    //-----------------------------------------------------------------------
    /**
     * Constructs the builder wrapping the target bean.
     * 
     * @param metaBean  the target meta-bean, not null
     * @return a new untyped builder, not null
     */
    public static BufferingBeanBuilder<?> of(MetaBean metaBean) {
        return new BufferingBeanBuilder<>(metaBean);
    }

    //-----------------------------------------------------------------------
    /**
     * Constructs the builder wrapping the target bean.
     * 
     * @param metaBean  the target meta-bean, not null
     */
    public BufferingBeanBuilder(MetaBean metaBean) {
        if (metaBean == null) {
            throw new NullPointerException("MetaBean must not be null");
        }
        this.metaBean = metaBean;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the meta-beans.
     * 
     * @return the meta-bean, not null
     */
    public MetaBean getMetaBean() {
        return metaBean;
    }

    /**
     * Gets the buffer holding the state of the builder.
     * <p>
     * The buffer may be mutated.
     * 
     * @return the mutable buffer, not null
     */
    public ConcurrentMap<MetaProperty<?>, Object> getBuffer() {
        return buffer;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the buffered value associated with the specified property name.
     * 
     * @param propertyName  the property name, not null
     * @return the current value in the builder, null if not found or value is null
     */
    @Override
    public Object get(String propertyName) {
        return get(getMetaBean().metaProperty(propertyName));
    }

    /**
     * Gets the buffered value associated with the specified property name.
     * 
     * @param metaProperty  the meta-property, not null
     * @return the current value in the builder, null if not found or value is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <P> P get(MetaProperty<P> metaProperty) {
        return (P) getBuffer().get(metaProperty);
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<T> set(String propertyName, Object value) {
        return set(getMetaBean().metaProperty(propertyName), value);
    }

    @Override
    public BeanBuilder<T> set(MetaProperty<?> metaProperty, Object value) {
        getBuffer().put(metaProperty, value);
        return this;
    }

    @Override
    public T build() {
        @SuppressWarnings("unchecked")
        BeanBuilder<T> builder = (BeanBuilder<T>) getMetaBean().builder();
        for (Entry<MetaProperty<?>, Object> entry : getBuffer().entrySet()) {
            builder.set(entry.getKey(), entry.getValue());
        }
        return builder.build();
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the builder.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "BeanBuilder for " + metaBean.beanName();
    }

}
