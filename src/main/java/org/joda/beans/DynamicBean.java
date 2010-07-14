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

/**
 * A dynamic bean that allows properties to be added and removed.
 * <p>
 * A JavaBean is defined at compile-time and cannot have additional properties added.
 * Instances of this interface allow additional properties to be added and removed
 * probably by wrapping a map
 * 
 * @author Stephen Colebourne
 */
public interface DynamicBean extends Bean {

    /**
     * Adds a property to those allowed to be stored in the bean.
     * <p>
     * Some implementations will automatically add properties, in which case this
     * method will have no effect.
     * 
     * @param propertyName  the property name to check, not empty, not null
     * @param propertyType  the property type, not null
     */
    void propertyDefine(String propertyName, Class<?> propertyType);

    /**
     * Removes a property by name.
     * 
     * @param propertyName  the property name to remove, null ignored
     */
    void propertyRemove(String propertyName);

}
