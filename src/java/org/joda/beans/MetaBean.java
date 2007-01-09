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
 * Represents those aspects of a bean which are not specific to a
 * specific instance. In other words, it performs the same role that Class
 * does for Object.
 * 
 * @author Stephen Colebourne
 */
public interface MetaBean<B> {

    //-----------------------------------------------------------------------
    /**
     * Creates a new instance of the bean represented by this meta bean.
     * 
     * @return the created bean
     */
    Bean<B> createBean();

    /**
     * Get the type of the bean that will be created represented as a Class.
     * 
     * @return the type of the bean
     */
    Class<B> getType();

    /**
     * Gets the bean name, which is normally the fully qualified class name of the bean.
     * 
     * @return the name of the bean
     */
    String getName();

    //-----------------------------------------------------------------------
    /**
     * Gets the map of meta properties, keyed by property name.
     * 
     * @return the map of meta property objects, never null
     */
    Map<String, MetaProperty<B, ?>> metaPropertyMap();

    /**
     * Gets a meta property by name.
     * 
     * @param propertyName  the property name to retrieve
     * @return the meta property, null if not found
     */
    MetaProperty<B, ?> metaProperty(String propertyName);

}
