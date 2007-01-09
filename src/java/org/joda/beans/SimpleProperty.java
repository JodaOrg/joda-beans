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
 * Implementation of a {@link Property} that uses reflection to access
 * get and set methods.
 * 
 * @author Stephen Colebourne
 */
public class SimpleProperty<B, T> implements Property<B, T> {

    /** The bean that the property is bound to. */
    private final B bean;
    /** The property. */
    private final MetaProperty<B, T> metaProperty;

    /**
     * Constructor.
     * 
     * @param bean  the bean that the property is bound to, not null
     * @param metaProperty  the meta property, not null
     */
    public SimpleProperty(B bean, MetaProperty<B, T> metaProperty) {
        if (bean == null) {
            throw new IllegalArgumentException("The bean must not be null");
        }
        if (metaProperty == null) {
            throw new IllegalArgumentException("The meta property must not be null");
        }
        this.bean = bean;
        this.metaProperty = metaProperty;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean which owns this bound property.
     * 
     * @return the bean
     */
    public B bean() {
        return bean;
    }

    /**
     * Gets the property itself.
     * 
     * @return the name of the property
     */
    public MetaProperty<B, T> metaProperty() {
        return metaProperty;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the bound property.
     * <p>
     * This is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * 
     * @return the value of the property on the bound bean
     * @throws UnsupportedOperationException if the property is write-only
     */
    public T get() {
        return metaProperty().get(bean());
    }

    /**
     * Sets the value of the bound property.
     * <p>
     * This is the equivalent to calling <code>setFoo()</code> on the bean itself.
     * 
     * @param value  the value to set into the property on the bound bean
     * @throws UnsupportedOperationException if the property is read-only
     */
    public void set(T value) {
        metaProperty().set(bean(), value);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a debugging string.
     * 
     * @return a debugging string
     */
    @Override
    public String toString() {
        return metaProperty().getName() + "=" + get();
    }

}
