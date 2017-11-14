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
package org.joda.beans.impl.light;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * An immutable meta-property based on a getter interface.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
final class LightMetaProperty<P> extends BasicMetaProperty<P> {

    /** The meta-bean. */
    private final MetaBean metaBean;
    /** The type of the property. */
    private final Class<P> propertyType;
    /** The type of the property. */
    private final Type propertyGenericType;
    /** The annotations. */
    private final List<Annotation> annotations;
    /** The read method. */
    private final MethodHandle getter;
    /** The optional write method. */
    private final MethodHandle setter;
    /** The index of the property in the constructor. */
    private final int constructorIndex;
    /** The property style. */
    private final PropertyStyle style;

    //-----------------------------------------------------------------------
    /**
     * Creates an instance from a {@code Field}.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta bean, not null
     * @param field  the field, not null
     * @param constructorIndex  the index of the property in the constructor
     * @return the property, not null
     */
    @SuppressWarnings("unchecked")
    static <P> LightMetaProperty<P> of(
            MetaBean metaBean,
            Field field,
            MethodHandles.Lookup lookup,
            String propertyName,
            int constructorIndex) {
        
        MethodHandle getter;
        try {
            getter = lookup.findGetter(field.getDeclaringClass(), field.getName(), field.getType());
        } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + propertyName, ex);
        }
        MethodHandle setter = null;
        if (!Modifier.isFinal(field.getModifiers())) {
            try {
                setter = lookup.findSetter(field.getDeclaringClass(), field.getName(), field.getType());
            } catch (IllegalArgumentException | NoSuchFieldException | IllegalAccessException ex) {
                throw new UnsupportedOperationException("Property cannot be read: " + propertyName, ex);
            }
        }
        return new LightMetaProperty<>(
                metaBean, 
                propertyName, 
                (Class<P>) field.getType(), 
                field.getGenericType(), 
                Arrays.asList(field.getAnnotations()), 
                getter,
                setter,
                constructorIndex,
                calculateStyle(metaBean, setter));
    }

    /**
     * Creates an instance from a {@code Method}.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta bean, not null
     * @param getMethod  the method, not null
     * @param setMethod  the method, not null
     * @param constructorIndex  the index of the property in the constructor
     * @return the property, not null
     */
    @SuppressWarnings("unchecked")
    static <P> LightMetaProperty<P> of(
            MetaBean metaBean,
            Field field,
            Method getMethod,
            Method setMethod,
            MethodHandles.Lookup lookup,
            String propertyName,
            int constructorIndex) {
        
        MethodHandle getter;
        try {
            MethodType type = MethodType.methodType(getMethod.getReturnType(), getMethod.getParameterTypes());
            getter = lookup.findVirtual(field.getDeclaringClass(), getMethod.getName(), type);
        } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + propertyName, ex);
        }
        MethodHandle setter = null;
        if (setMethod != null) {
            try {
                MethodType type = MethodType.methodType(void.class, setMethod.getParameterTypes());
                setter = lookup.findVirtual(field.getDeclaringClass(), setMethod.getName(), type);
            } catch (IllegalArgumentException | NoSuchMethodException | IllegalAccessException ex) {
                throw new UnsupportedOperationException("Property cannot be written: " + propertyName, ex);
            }
        }
        return new LightMetaProperty<>(
                metaBean, 
                propertyName, 
                (Class<P>) field.getType(), 
                field.getGenericType(), 
                Arrays.asList(field.getAnnotations()), 
                getter,
                setter,
                constructorIndex,
                calculateStyle(metaBean, setter));
    }

    private static PropertyStyle calculateStyle(MetaBean metaBean, MethodHandle setter) {
        if (ImmutableBean.class.isAssignableFrom(metaBean.beanType())) {
            return PropertyStyle.IMMUTABLE;
        }
        return setter != null ? PropertyStyle.READ_WRITE : PropertyStyle.READ_ONLY;
    }

    /**
     * Creates an instance from a derived {@code Method}.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta bean, not null
     * @param getMethod  the get method, not null
     * @param constructorIndex  the index of the property
     * @return the property, not null
     */
    @SuppressWarnings("unchecked")
    static <P> LightMetaProperty<P> of(
            MetaBean metaBean,
            final Method getMethod,
            MethodHandles.Lookup lookup,
            final String propertyName,
            int constructorIndex) {
        
        MethodHandle getter;
        try {
            getter = lookup.unreflect(getMethod);
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            throw new UnsupportedOperationException("Property cannot be read: " + propertyName, ex);
        }
        return new LightMetaProperty<>(
                metaBean, 
                propertyName, 
                (Class<P>) getMethod.getReturnType(), 
                getMethod.getGenericReturnType(), 
                Arrays.asList(getMethod.getAnnotations()), 
                getter,
                null,
                constructorIndex,
                PropertyStyle.DERIVED);
    }

    /**
     * Creates an instance.
     * 
     * @param metaBean  the meta bean, not null
     * @param propertyName  the property name, not empty
     * @param propertyType  the property type
     * @param propertyGenericType  the property generic type
     * @param annotations  the annotations
     * @param getter  the property getter
     * @param setter  the property setter
     * @param constructorIndex  the index of the property in the constructor
     */
    LightMetaProperty(
            MetaBean metaBean, 
            String propertyName,
            Class<P> propertyType,
            Type propertyGenericType,
            List<Annotation> annotations,
            MethodHandle getter,
            MethodHandle setter,
            int constructorIndex,
            PropertyStyle style) {
        
        super(propertyName);
        this.metaBean = metaBean;
        this.propertyType = propertyType;
        this.propertyGenericType = propertyGenericType;
        this.annotations = annotations;
        this.getter = getter.asType(MethodType.methodType(Object.class, Bean.class));
        this.setter = setter != null ? setter.asType(MethodType.methodType(void.class, Bean.class, Object.class)) : null;
        this.constructorIndex = constructorIndex;
        this.style = style;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Class<?> declaringType() {
        return metaBean.beanType();
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public Type propertyGenericType() {
        return propertyGenericType;
    }

    @Override
    public PropertyStyle style() {
        return style;
    }

    @Override
    public List<Annotation> annotations() {
        return annotations;
    }

    //-----------------------------------------------------------------------
    @Override
    public P get(Bean bean) {
        try {
            return (P) getter.invokeExact(bean);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void set(Bean bean, Object value) {
        if (setter == null) {
            throw new UnsupportedOperationException("Property cannot be written: " + name());
        }
        try {
            setter.invokeExact(bean, value);
        } catch (RuntimeException | Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }

    int getConstructorIndex() {
        return constructorIndex;
    }

}
