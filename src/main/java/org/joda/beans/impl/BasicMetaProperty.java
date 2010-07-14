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

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

/**
 * An abstract base meta-property.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public abstract class BasicMetaProperty<P> implements MetaProperty<P> {

    /** The type of the bean. */
    private final Class<?> beanType;
    /** The name of the property. */
    private final String name;

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type, not null
     * @param readWrite  the read-write type, not null
     */
    protected BasicMetaProperty(Class<?> beanType, String propertyName) {
        if (beanType == null) {
            throw new NullPointerException("Bean type must not be null");
        }
        if (propertyName == null || propertyName.length() == 0) {
            throw new NullPointerException("Property name must not be null or empty");
        }
        if (propertyName == null) {
        }
        this.beanType = beanType;
        this.name = propertyName;
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<P> createProperty(Bean bean) {
        return BasicProperty.of(bean, this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<?> beanType() {
        return beanType;
    }

    //-----------------------------------------------------------------------
    @Override
    public P put(Bean bean, P value) {
        P old = get(bean);
        set(bean, value);
        return old;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BasicMetaProperty<?>) {
            BasicMetaProperty<?> other = (BasicMetaProperty<?>) obj;
            return this.beanType.equals(other.beanType) && this.name.equals(other.name);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return beanType.hashCode() ^ name.hashCode();
    }

    /**
     * Returns a string that summarises the property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaProperty:" + name;
    }

}
