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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A property is a field on a bean that can typically be called via get/set.
 * 
 * @author Stephen Colebourne
 */
public class DescriptorMetaProperty<B, T> implements MetaProperty<B, T> {

    /** The property descriptor. */
    private final PropertyDescriptor descriptor;
    /** The type of the bean. */
    private final Class<B> beanType;
    /** The read method. */
    private final Method readMethod;
    /** The write method. */
    private final Method writeMethod;

    /**
     * Constructor.
     * 
     * @param beanType  the bean type
     * @param propertyName  the property name
     */
    public DescriptorMetaProperty(Class<B> beanType, String propertyName) {
        super();
        try {
            descriptor = new PropertyDescriptor(propertyName, beanType);
        } catch (IntrospectionException ex) {
            throw new NoSuchFieldError("Invalid property: " + propertyName);
        }
        Method readMethod = descriptor.getReadMethod();
        Method writeMethod = descriptor.getWriteMethod();
        if (readMethod == null & writeMethod == null) {
            throw new NoSuchFieldError("Invalid property: " + propertyName);
        }
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.beanType = beanType;
    }

    //-----------------------------------------------------------------------
    /**
     * Creates a property that binds this meta property to a specific bean.
     * 
     * @param bean  the bean to create the bound property for
     * @return the bound property
     */
    public Property<B, T> createProperty(B bean) {
        return new SimpleProperty<B, T>(bean, this);
    }

    /**
     * Gets the property name.
     * The JavaBean style methods getFoo() and setFoo() will lead 
     * to a property name of 'foo' and so on.
     * 
     * @return the name of the property
     */
    public String getName() {
        return descriptor.getName();
    }

    /**
     * Get the type of the property represented as a Class.
     * 
     * @return the type of the property
     */
    public Class<T> getType() {
        // erasure is rubbish
        return (Class<T>) descriptor.getPropertyType();
    }

    /**
     * Get the type of the bean represented as a Class.
     * 
     * @return the type of the bean
     */
    public Class<B> getBeanType() {
        return beanType;
    }

    /**
     * Gets whether the property is read-write, read-only or write-only.
     * 
     * @return the property read-write type
     */
    public ReadWriteProperty getReadWrite() {
        return (readMethod == null ? ReadWriteProperty.WRITE_ONLY :
                (writeMethod == null ? ReadWriteProperty.READ_ONLY : ReadWriteProperty.READ_WRITE));
    }

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
    @SuppressWarnings("unchecked")
    public T get(B bean) {
        if (getReadWrite().isReadable() == false) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        }
        try {
            return (T) readMethod.invoke(bean, (Object[]) null);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        }
    }

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
    public void set(B bean, T value) {
        if (getReadWrite().isWritable() == false) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be written");
        }
        try {
            writeMethod.invoke(bean, value);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        } catch (InvocationTargetException ex) {
            throw new UnsupportedOperationException("Property '" + getName() + "' cannot be read");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a debugging string.
     * 
     * @return a debugging string
     */
    @Override
    public String toString() {
        return "MetaProperty:" + getName();
    }

}
