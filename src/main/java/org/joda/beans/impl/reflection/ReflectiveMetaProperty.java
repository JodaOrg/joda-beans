/*
 *  Copyright 2001-present Stephen Colebourne
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

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * A meta-property implemented using a {@code PropertyDescriptor}.
 * <p>
 * The property descriptor class is part of the JDK JavaBean standard.
 * It provides access to get and set a property on a bean.
 * <p>
 * Instances of this class should be declared as a static constant on the bean,
 * one for each property, followed by a {@code ReflectiveMetaBean} declaration.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
final class ReflectiveMetaProperty<P> extends BasicMetaProperty<P> {

    /** The meta-bean. */
    private volatile MetaBean metaBean;
    /** The declaring type. */
    private final Class<?> declaringType;
    /** The type of the property. */
    private final Class<P> propertyType;
    /** The getter. */
    private final Method getMethod;
    /** The setter. */
    private final Method setMethod;

    /**
     * Constructor using {@code PropertyDescriptor} to find the get and set methods.
     * 
     * @param metaBean  the meta-bean
     * @param beanType  the bean type, not null
     * @param propertyName  the property name, not empty
     */
    @SuppressWarnings({"unchecked", "null"})
    ReflectiveMetaProperty(MetaBean metaBean, Class<? extends Bean> beanType, String propertyName) {
        super(propertyName);
        String getterName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        String isserName = "is" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Method getMethod = findGetMethod(beanType, getterName);
        Method isMethod = findGetMethod(beanType, isserName);
        if (getMethod == null && isMethod == null) {
            throw new IllegalArgumentException(
                "Unable to find property getter: " + beanType.getSimpleName() + "." + getterName + "()");
        }
        getMethod = isMethod != null ? isMethod : getMethod;
        Method setMethod = null;
        if (!ImmutableBean.class.isAssignableFrom(beanType)) {
            String setterName = "set" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
            setMethod = findSetMethod(beanType, setterName, getMethod.getReturnType());
            if (setMethod == null) {
                throw new IllegalArgumentException(
                    "Unable to find property setter: " + beanType.getSimpleName() + "." + setterName + "()");
            }
        }
        this.metaBean = metaBean;
        this.declaringType = (getMethod != null ? getMethod.getDeclaringClass() : setMethod.getDeclaringClass());
        this.propertyType = (Class<P>) getMethod.getReturnType();
        this.getMethod = getMethod;
        this.setMethod = setMethod;
    }

    // finds a method on class or public method on super-type
    private static Method findGetMethod(Class<? extends Bean> beanType, String getterName) {
        try {
            return beanType.getDeclaredMethod(getterName);
        } catch (NoSuchMethodException ex) {
            try {
                return beanType.getMethod(getterName);
            } catch (NoSuchMethodException ex2) {
                return null;
            }
        }
    }

    // finds a method on class or public method on super-type
    private static Method findSetMethod(Class<? extends Bean> beanType, String setterName, Class<?> fieldType) {
        try {
            return beanType.getDeclaredMethod(setterName, fieldType);
        } catch (NoSuchMethodException ex) {
            Method[] methods = beanType.getMethods();
            List<Method> potential = new ArrayList<>();
            for (Method method : methods) {
                if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                    potential.add(method);
                }
            }
            if (potential.size() == 1) {
                return potential.get(0);
            }
            for (Method method : potential) {
                if (method.getParameterTypes()[0].equals(fieldType)) {
                    return method;
                }
            }
            return null;
        }
    }

    /**
     * Sets the meta-bean, necessary due to ordering restrictions during loading.
     * @param metaBean  the meta-bean, not null
     */
    void setMetaBean(MetaBean metaBean) {
        this.metaBean = metaBean;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Class<?> declaringType() {
        return declaringType;
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public Type propertyGenericType() {
        if (getMethod != null) {
            return getMethod.getGenericReturnType();
        }
        return setMethod.getGenericParameterTypes()[0];
    }

    @Override
    public PropertyStyle style() {
        return (getMethod == null ? PropertyStyle.WRITE_ONLY :
                (setMethod == null ? PropertyStyle.READ_ONLY : PropertyStyle.READ_WRITE));
    }

    @Override
    public List<Annotation> annotations() {
        if (getMethod != null) {
            return Arrays.asList(getMethod.getDeclaredAnnotations());
        }
        return Arrays.asList(setMethod.getDeclaredAnnotations());
    }

    //-----------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public P get(Bean bean) {
        if (style().isReadable() == false) {
            throw new UnsupportedOperationException("Property cannot be read: " + name());
        }
        try {
            return (P) getMethod.invoke(bean, (Object[]) null);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + name(), ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
        }
    }

    @SuppressWarnings("null")
    @Override
    public void set(Bean bean, Object value) {
        if (style().isWritable() == false) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        try {
            setMethod.invoke(bean, value);
        } catch (IllegalArgumentException ex) {
            if (value == null && setMethod.getParameterTypes()[0].isPrimitive()) {
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
