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

import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

/**
 * A property that binds a {@code Bean} to a {@code MetaProperty}.
 * <p>
 * This is the standard implementation of a property.
 * It defers the strategy of getting and setting the value to the meta-property.
 * <p>
 * This implementation is also a map entry to aid performance in maps.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public final class BasicProperty<P> implements Property<P>, Entry<String, Property<P>> {

    /** The bean that the property is bound to. */
    private final Bean bean;
    /** The meta-property that the property is bound to. */
    private final MetaProperty<P> metaProperty;

    /**
     * Factory to create a property avoiding duplicate generics.
     * 
     * @param bean  the bean that the property is bound to, not null
     * @param metaProperty  the meta property, not null
     */
    public static <P> BasicProperty<P> of(Bean bean, MetaProperty<P> metaProperty) {
        return new BasicProperty<P>(bean, metaProperty);
    }

    /**
     * Creates a property binding the bean to the meta-property.
     * 
     * @param bean  the bean that the property is bound to, not null
     * @param metaProperty  the meta property, not null
     */
    private BasicProperty(Bean bean, MetaProperty<P> metaProperty) {
        if (bean == null) {
            throw new NullPointerException("Bean must not be null");
        }
        if (metaProperty == null) {
            throw new NullPointerException("MetaProperty must not be null");
        }
        this.bean = bean;
        this.metaProperty = metaProperty;
    }

    //-----------------------------------------------------------------------
    @Override
    public Bean bean() {
        return bean;
    }

    @Override
    public MetaProperty<P> metaProperty() {
        return metaProperty;
    }

    @Override
    public String name() {
        return metaProperty.name();
    }

    //-----------------------------------------------------------------------
    @Override
    public P get() {
        return metaProperty.get(bean);
    }

    @Override
    public void set(P value) {
        metaProperty.set(bean, value);
    }

    @Override
    public P put(P value) {
        return metaProperty.put(bean, value);
    }

    //-----------------------------------------------------------------------
    @Override
    public String getKey() {
        return name();
    }

    @Override
    public Property<P> getValue() {
        return this;
    }

    @Override
    public Property<P> setValue(Property<P> value) {
        throw new UnsupportedOperationException("Unmodifiable");
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        // specified by Map.Entry
        if (obj instanceof Entry<?, ?>) {
            Entry<?, ?> other = (Entry<?, ?>) obj;
            return (this.getKey() == null ? other.getKey() == null : this.getKey().equals(other.getKey())) &&
                (this.getValue() == null ? other.getValue() == null : this.getValue().equals(other.getValue()));
        }
        return false;
    }

    @Override
    public int hashCode() {
        // specified by Map.Entry
        return (getKey()==null ? 0 : getKey().hashCode()) ^ (getValue()==null ? 0 : getValue().hashCode());
    }

    /**
     * Returns a string that summarises the property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return name() + ":" + get();
    }

}
