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

/**
 * Represents a property that is linked to a specific bean.
 * <p>
 * Normally, this will be implemented by wrapping a get/set method pair.
 * However, it can also be implemented in other ways, such as accessing
 * a map.
 * 
 * @author Stephen Colebourne
 */
public interface Property<B, T> {

    //-----------------------------------------------------------------------
    /**
     * Gets the bean which owns this bound property.
     * 
     * @return the bean
     */
    B bean();

    /**
     * Gets the property itself.
     * 
     * @return the name of the property
     */
    MetaProperty<B, T> metaProperty();

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the bound property.
     * <p>
     * This is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * 
     * @return the value of the property on the bound bean
     * @throws UnsupportedOperationException if the property is write-only
     */
    T get();

    /**
     * Sets the value of the bound property.
     * <p>
     * This is the equivalent to calling <code>setFoo()</code> on the bean itself.
     * 
     * @param value  the value to set into the property on the bound bean
     * @throws UnsupportedOperationException if the property is read-only
     */
    void set(T value);

}
