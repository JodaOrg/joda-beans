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
package org.joda.beans;

import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A bean consisting of a set of properties.
 * <p>
 * The implementation may be any class, but is typically a standard JavaBean
 * with get/set methods. Alternate implementations might store the properties
 * in another data structure such as a map.
 * 
 * @author Stephen Colebourne
 */
public interface Bean {

    /**
     * Gets the meta-bean representing the parts of the bean that are
     * common across all instances, such as the set of meta-properties.
     * <p>
     * The meta-bean can be thought of as the equivalent of {@link Class} but for beans.
     * 
     * @return the meta-bean, not null
     */
    public abstract MetaBean metaBean();

    /**
     * Gets a property by name.
     * <p>
     * Each bean consists of a known set of properties.
     * This method checks whether there is a property with the specified name.
     * <p>
     * The base interface throws an exception if the name is not recognised.
     * By contrast, the {@code DynamicBean} subinterface creates the property on demand.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, not null
     * @return the property, not null
     * @throws NoSuchElementException if the property name is invalid
     */
    public default <R> Property<R> property(String propertyName) {
        return metaBean().<R>metaProperty(propertyName).createProperty(this);
    }

    /**
     * Gets the set of property names.
     * <p>
     * Each bean consists of a known set of properties.
     * This method returns the known property names.
     * 
     * @return the unmodifiable set of property names, not null
     */
    public default Set<String> propertyNames() {
        return metaBean().metaPropertyMap().keySet();
    }

}
