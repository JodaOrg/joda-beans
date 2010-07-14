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
 * 
 * @author Stephen Colebourne
 */
public abstract class BasicBean implements Bean {

    @Override
    public boolean propertyExists(String propertyName) {
        return metaBean().metaPropertyExists(propertyName);
    }

    @Override
    public Property<Object> property(String propertyName) {
        return metaBean().metaProperty(propertyName).createProperty(this);
    }

    @Override
    public PropertyMap propertyMap() {
        return metaBean().createPropertyMap(this);
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
        if (obj instanceof BasicBean) {
            BasicBean other = (BasicBean) obj;
            return propertyMap().flatten().equals(other.propertyMap().flatten());
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
        return propertyMap().flatten().hashCode();
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
        return getClass().getSimpleName() + propertyMap().flatten().toString();
    }

}
