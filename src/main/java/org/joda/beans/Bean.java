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
package org.joda.beans;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A bean consisting of a set of properties.
 * <p>
 * For a JavaBean, this will ultimately wrap a get/set methods of the bean.
 * Alternate implementations might store the properties in another data structure
 * such as a map.
 * 
 * @param <B>  the type of the bean
 * @author Stephen Colebourne
 */
public interface Bean<B> {

    /**
     * Gets the bean which this interface defines.
     * <p>
     * The bean returned is the actual bean instance.
     * Where possible, the actual bean should implement this interface, thus
     * this method would return {@code this}.
     * 
     * @return the bean itself, never null
     */
    B bean();

    /**
     * Gets the meta-bean representing the parts of the bean that are
     * common across all instances, such as the set of meta-properties.
     * 
     * @return the meta-bean, never null
     */
    MetaBean<B> metaBean();

    //-----------------------------------------------------------------------
    /**
     * Gets the map of properties, keyed by property name.
     * 
     * @return the unmodifiable map of property objects, never null
     */
    Map<String, Property<B, ?>> propertyMap();

    /**
     * Gets a property by name.
     * 
     * @param propertyName  the property name to retrieve, null throws NoSuchElementException
     * @return the property, never null
     * @throws NoSuchElementException if the property name is invalid
     */
    Property<B, ?> property(String propertyName);

}
