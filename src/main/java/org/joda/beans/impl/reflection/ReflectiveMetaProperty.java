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
package org.joda.beans.impl.reflection;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.joda.beans.Bean;
import org.joda.beans.Property;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaProperty;
import org.joda.beans.impl.BasicProperty;

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
public final class ReflectiveMetaProperty<B, P> extends BasicMetaProperty<B, P> {

    /** The type of the property. */
    private final Class<P> propertyType;
    /** The read method. */
    private final Method readMethod;
    /** The write method. */
    private final Method writeMethod;

    /**
     * Factory to create a meta-property avoiding duplicate generics.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     */
    public static <B, P> ReflectiveMetaProperty<B, P> of(Class<B> beanType, String propertyName) {
        return new ReflectiveMetaProperty<B, P>(beanType, propertyName);
    }

    /**
     * Constructor using {@code PropertyDescriptor} to find the get and set methods.
     * 
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     */
    @SuppressWarnings("unchecked")
    private ReflectiveMetaProperty(Class<B> beanType, String propertyName) {
        super(beanType, propertyName);
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
        this.propertyType = (Class<P>) descriptor.getPropertyType();
        this.readMethod = readMethod;
        this.writeMethod = writeMethod;
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<B, P> createProperty(Bean<B> bean) {
        return BasicProperty.of(bean, this);
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return (readMethod == null ? PropertyReadWrite.WRITE_ONLY :
                (writeMethod == null ? PropertyReadWrite.READ_ONLY : PropertyReadWrite.READ_WRITE));
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public P get(Bean<B> bean) {
        if (readWrite().isReadable() == false) {
            throw new UnsupportedOperationException("Property cannot be read: " + name());
        }
        try {
            return (P) readMethod.invoke(bean.beanData(), (Object[]) null);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + name(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + name(), ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void set(Bean<B> bean, P value) {
        if (readWrite().isWritable() == false) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        try {
            writeMethod.invoke(bean.beanData(), value);
        } catch (IllegalArgumentException ex) {
            if (value == null && writeMethod.getParameterTypes()[0].isPrimitive()) {
                throw new NullPointerException("Property cannot be written: " + name() + ": Cannot store null in primitive");
            }
            if (propertyType.isInstance(value) == false) {
                throw new ClassCastException("Property cannot be written: " + name() + ": Invalid type: " + value.getClass().getName());
            }
            throw new UnsupportedOperationException("Property cannot be written: " + name(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be written: " + name(), ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
        }
    }

}
