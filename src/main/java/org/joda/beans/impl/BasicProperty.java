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
public final class BasicProperty<P> implements Property<P> {

    /** The bean that the property is bound to. */
    private final Bean bean;
    /** The meta-property that the property is bound to. */
    private final MetaProperty<P> metaProperty;

    /**
     * Factory to create a property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param bean  the bean that the property is bound to, not null
     * @param metaProperty  the meta property, not null
     * @return the property, not null
     */
    public static <P> BasicProperty<P> of(Bean bean, MetaProperty<P> metaProperty) {
        return new BasicProperty<>(bean, metaProperty);
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
    @SuppressWarnings("unchecked")
    @Override
    public <B extends Bean> B bean() {
        return (B) bean;
    }

    @Override
    public MetaProperty<P> metaProperty() {
        return metaProperty;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Property) {
            Property<?> other = (Property<?>) obj;
            if (metaProperty.equals(other.metaProperty())) {
                Object a = get();
                Object b = other.get();
                return a == null ? b == null : a.equals(b);
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        P value = get();
        return metaProperty.hashCode() ^ (value == null ? 0 : value.hashCode());
    }

    /**
     * Returns a string that summarises the property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return metaProperty + "=" + get();
    }

}
