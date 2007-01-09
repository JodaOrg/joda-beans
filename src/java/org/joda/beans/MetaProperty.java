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
 * Represents those aspects of a property which are not specific to a
 * particular bean, such as the property type and name.
 * 
 * @author Stephen Colebourne
 */
public interface MetaProperty<B, T> {

    //-----------------------------------------------------------------------
    /**
     * Creates a property that binds this meta property to a specific bean.
     * 
     * @param bean  the bean to create the bound property for, not null
     * @return the bound property
     */
    Property<B, T> createProperty(B bean);

    /**
     * Gets the property name.
     * The JavaBean style methods getFoo() and setFoo() will lead 
     * to a property name of 'foo' and so on.
     * 
     * @return the name of the property
     */
    String getName();

    /**
     * Get the type of the property represented as a Class.
     * 
     * @return the type of the property
     */
    Class<T> getType();

    /**
     * Get the type of the property represented as a Class.
     * 
     * @return the type of the property
     */
    Class<B> getBeanType();

    /**
     * Gets whether the property is read-write, read-only or write-only.
     * 
     * @return the property read-write type
     */
    ReadWriteProperty getReadWrite();

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the bound property for the provided bean.
     * <p>
     * This is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * However some implementations of this interface may not require an actual get method.
     * 
     * @param bean  the bean to query, not null
     * @return the value of the property on the bound bean
     * @throws UnsupportedOperationException if the property is write-only
     */
    T get(B bean);

    /**
     * Sets the value of the bound property on the provided bean.
     * <p>
     * This is the equivalent to calling <code>setFoo()</code> on the bean itself.
     * However some implementations of this interface may not require an actual set method.
     * 
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the bound bean
     * @throws UnsupportedOperationException if the property is read-only
     */
    void set(B bean, T value);

}
