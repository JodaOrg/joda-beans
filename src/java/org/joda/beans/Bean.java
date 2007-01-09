/*
 *  Copyright 2001-2007 Stephen Colebourne
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

/**
 * Represents a property that is linked to a specific bean.
 * <p>
 * Normally, this will be implemented by wrapping a get/set method pair.
 * However, it can also be implemented in other ways, such as accessing
 * a map.
 * 
 * @author Stephen Colebourne
 */
public interface Bean<B> {

    //-----------------------------------------------------------------------
    /**
     * Gets the bean which this Bean interface defines.
     * Where possible, it is advisable to make the actual bean implement
     * this interface, and make this method return <code>this</code>.
     * 
     * @return the bean itself
     */
    B bean();

    /**
     * Gets the meta bean which defines the static non-instance data for this bean.
     * 
     * @return the meta bean
     */
    MetaBean<B> metaBean();

    //-----------------------------------------------------------------------
    /**
     * Gets the map of properties, keyed by property name.
     * 
     * @return the list of meta property objects, never null
     */
    Map<String, Property<B, ?>> propertyMap();

    /**
     * Gets a property by name.
     * 
     * @param propertyName  the property name to retrieve
     * @return the meta property, null if not found
     */
    Property<B, ?> property(String propertyName);

}
