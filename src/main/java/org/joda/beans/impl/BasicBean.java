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

import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanUtils;
import org.joda.beans.Property;

/**
 * Basic implementation of {@code Bean} intended for applications to subclass.
 * <p>
 * The subclass must to provide an implementation for {@link Bean#metaBean()}.
 * This returns the complete definition of the bean at the meta level.
 * 
 * @author Stephen Colebourne
 */
public abstract class BasicBean implements Bean {

    @Override
    public <R> Property<R> property(String propertyName) {
        return metaBean().<R>metaProperty(propertyName).createProperty(this);
    }

    @Override
    public Set<String> propertyNames() {
        return metaBean().metaPropertyMap().keySet();
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this bean to another.
     * <p>
     * This compares all the properties of the bean.
     * 
     * @param obj  the object to compare to, null returns false
     * @return true if the beans are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Bean) {
            Bean other = (Bean) obj;
            return BeanUtils.propertiesEqual(this, other);
        }
        return false;
    }

    /**
     * Returns a suitable hash code.
     * <p>
     * The hash code is derived from all the properties of the bean.
     * 
     * @return a suitable hash code
     */
    @Override
    public int hashCode() {
        return BeanUtils.propertiesHashCode(this);
    }

    /**
     * Returns a string that summarises the property.
     * <p>
     * The string contains the class name and properties.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return BeanUtils.propertiesToString(this, metaBean().beanType().getSimpleName());
    }

}
