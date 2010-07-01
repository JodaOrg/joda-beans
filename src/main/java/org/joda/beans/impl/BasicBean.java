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
import org.joda.beans.Property;
import org.joda.beans.PropertyMap;

/**
 * Basic implementation of {@code Bean} intended for applications to subclass.
 * <p>
 * The subclass must to provide an implementation for {@link Bean#metaBean()}.
 * This returns the complete definition of the bean at the meta level.
 * <p>
 * A standard meta-bean implementation is {@link StandardMetaBean}. This is normally
 * declared as a static constant, so {@code metaBean()} simply returns the constant.
 * This approach also requires a public static {@link MetaProperty} instance for each property.
 * 
 * @author Stephen Colebourne
 */
public abstract class BasicBean<B extends BasicBean<B>> implements Bean<B> {

    @SuppressWarnings("unchecked")
    public B bean() {
        return (B) this;
    }

    public boolean propertyExists(String propertyName) {
        return metaBean().metaPropertyExists(propertyName);
    }

    public Property<B, Object> property(String propertyName) {
        return metaBean().metaProperty(propertyName).createProperty(bean());
    }

    public PropertyMap<B> propertyMap() {
        return metaBean().createPropertyMap(bean());
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof BasicBean<?>) {
            BasicBean<?> other = (BasicBean<?>) obj;
            return propertyMap().flatten().equals(other.propertyMap().flatten());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return propertyMap().flatten().hashCode();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + propertyMap().flatten().toString();
    }

}
