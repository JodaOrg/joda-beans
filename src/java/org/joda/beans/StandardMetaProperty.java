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

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A meta-property implemented using a {@code PropertyDescriptor}.
 * <p>
 * The property descriptor class is part of the JDK JavaBean standard.
 * It provides access to get and set a property on a bean.
 * 
 * @param <B>  the type of the bean
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public final class StandardMetaProperty<B, P> implements MetaProperty<B, P> {

    /** The name of the property. */
    private final String name;
    /** The type of the property. */
    private final Class<P> propertyClass;
    /** The type of the bean. */
    private final Class<B> beanClass;
    /** The read method. */
    private final Method readMethod;
    /** The write method. */
    private final Method writeMethod;

    /**
     * Factory to create a meta-property avoiding duplicate generics.
     * 
     * @param beanType  the bean type
     * @param propertyName  the property name
     */
    public static <B, P> StandardMetaProperty<B, P> of(Class<B> beanType, String propertyName) {
        return new StandardMetaProperty<B, P>(beanType, propertyName);
    }

    /**
     * Constructor using {@code PropertyDescriptor} to find the get and set methods.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not null
     */
    @SuppressWarnings("unchecked")
    private StandardMetaProperty(Class<B> beanType, String propertyName) {
        PropertyDescriptor descriptor;
        try {
            descriptor = new PropertyDescriptor(propertyName, beanType);
        } catch (IntrospectionException ex) {
            throw new NoSuchFieldError("Invalid property: " + propertyName + ": " + ex.getMessage());
        }
        Method readMethod = descriptor.getReadMethod();
        Method writeMethod = descriptor.getWriteMethod();
        if (readMethod == null && writeMethod == null) {
            throw new NoSuchFieldError("Invalid property: " + propertyName + ": Both read and write methods are missing");
        }
        this.name = descriptor.getName();
        this.propertyClass = (Class<P>) descriptor.getPropertyType();
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
        this.beanClass = beanType;
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<B, P> createProperty(B bean) {
        return StandardProperty.of(bean, this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<P> propertyClass() {
        return propertyClass;
    }

    @Override
    public Class<B> beanClass() {
        return beanClass;
    }

    @Override
    public ReadWriteProperty readWrite() {
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
    public P get(B bean) {
        if (readWrite().isReadable() == false) {
            throw new UnsupportedOperationException("Property cannot be read: " + name);
        }
        try {
            return (P) readMethod.invoke(bean, (Object[]) null);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + name, ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + name, ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
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
    public void set(B bean, P value) {
        if (readWrite().isWritable() == false) {
            throw new UnsupportedOperationException("Property cannot be written: " + name);
        }
        try {
            writeMethod.invoke(bean, value);
        } catch (IllegalArgumentException ex) {
            if (value == null && writeMethod.getParameterTypes()[0].isPrimitive()) {
                throw new NullPointerException("Property cannot be written: " + name + ": Cannot store null in primitive");
            }
            throw new UnsupportedOperationException("Property cannot be written: " + name, ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be written: " + name, ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarizes the property.
     * 
     * @return a summary string, never null
     */
    @Override
    public String toString() {
        return "MetaProperty:" + name;
    }

}
