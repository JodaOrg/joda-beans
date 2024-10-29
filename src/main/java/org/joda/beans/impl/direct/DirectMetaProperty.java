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
package org.joda.beans.impl.direct;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * A meta-property implementation designed for use by the code generator.
 * <p>
 * This meta-property uses reflection to find the {@code Field} to obtain the annotations.
 * 
 * @param <P>  the type of the property content
 */
public final class DirectMetaProperty<P> extends BasicMetaProperty<P> {

    /** The meta-bean. */
    private final MetaBean metaBean;
    /** The property type. */
    private final Class<P> propertyType;
    /** The declaring type. */
    private final Class<?> declaringType;
    /** The field or method implementing the property. */
    private final AccessibleObject fieldOrMethod;
    /** The style. */
    private final PropertyStyle style;

    /**
     * Factory to create a read-write meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofReadWrite(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var field = findField(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.READ_WRITE, field);
    }

    /**
     * Factory to create a read-only meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofReadOnly(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var field = findField(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.READ_ONLY, field);
    }

    /**
     * Factory to create a write-only meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofWriteOnly(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var field = findField(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.WRITE_ONLY, field);
    }

    /**
     * Factory to create a buildable read-only meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofReadOnlyBuildable(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var field = findField(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.READ_ONLY_BUILDABLE, field);
    }

    /**
     * Factory to create a derived read-only meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofDerived(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var method = findMethod(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.DERIVED, method);
    }

    /**
     * Factory to create an immutable meta-property avoiding duplicate generics.
     * 
     * @param <P>  the property type
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the type declaring the property, not null
     * @param propertyType  the property type, not null
     * @return the property, not null
     */
    public static <P> DirectMetaProperty<P> ofImmutable(
            MetaBean metaBean, String propertyName, Class<?> declaringType, Class<P> propertyType) {
        var field = findField(metaBean, propertyName);
        return new DirectMetaProperty<>(metaBean, propertyName, declaringType, propertyType, PropertyStyle.IMMUTABLE, field);
    }

    private static Field findField(MetaBean metaBean, String propertyName) {
        Class<?> cls = metaBean.beanType();
        while (cls != DirectBean.class && cls != Object.class && cls != null) {
            try {
                return cls.getDeclaredField(propertyName);
            } catch (NoSuchFieldException ex) {
                try {
                    return cls.getDeclaredField("_" + propertyName);
                } catch (NoSuchFieldException ex2) {
                    cls = cls.getSuperclass();
                }
            }
        }
        return null;
    }

    private static AccessibleObject findMethod(MetaBean metaBean, String propertyName) {
        var methodName = "get" + propertyName.substring(0, 1).toUpperCase(Locale.ENGLISH) + propertyName.substring(1);
        Class<?> cls = metaBean.beanType();
        while (cls != DirectBean.class && cls != Object.class && cls != null) {
            try {
                return cls.getDeclaredMethod(methodName);
            } catch (NoSuchMethodException ex) {
                cls = cls.getSuperclass();
            }
        }
        return findField(metaBean, propertyName);  // backwards compatibility
    }

    /**
     * Constructor.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     * @param declaringType  the declaring type, not null
     * @param propertyType  the property type, not null
     * @param style  the style, not null
     * @param fieldOrMethod  the reflected field or method, not null
     */
    private DirectMetaProperty(MetaBean metaBean, String propertyName, Class<?> declaringType,
            Class<P> propertyType, PropertyStyle style, AccessibleObject fieldOrMethod) {
        super(propertyName);
        if (metaBean == null) {
            throw new NullPointerException("MetaBean must not be null");
        }
        if (declaringType == null) {
            throw new NullPointerException("Declaring type must not be null");
        }
        if (propertyType == null) {
            throw new NullPointerException("Property type must not be null");
        }
        if (style == null) {
            throw new NullPointerException("PropertyStyle must not be null");
        }
        this.metaBean = metaBean;
        this.propertyType = propertyType;
        this.declaringType = declaringType;
        this.style = style;
        this.fieldOrMethod = fieldOrMethod;  // may be null
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
        if (fieldOrMethod == null) {
            return propertyType;
        }
        return fieldOrMethod instanceof Field ?
                ((Field) fieldOrMethod).getGenericType() :
                ((Method) fieldOrMethod).getGenericReturnType();
    }

    @Override
    public PropertyStyle style() {
        return style;
    }

    @Override
    public <A extends Annotation> A annotation(Class<A> annotationClass) {
        if (fieldOrMethod == null) {
            throw new UnsupportedOperationException("Field not found for property: " + name());
        }
        A annotation = fieldOrMethod.getAnnotation(annotationClass);
        if (annotation == null) {
            throw new NoSuchElementException("Unknown annotation: " + annotationClass.getName());
        }
        return annotation;
    }

    @Override
    public <A extends Annotation> Optional<A> annotationOpt(Class<A> annotationClass) {
        return fieldOrMethod == null ?
                Optional.empty() :
                Optional.ofNullable(fieldOrMethod.getAnnotation(annotationClass));
    }

    @Override
    public List<Annotation> annotations() {
        if (fieldOrMethod == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(fieldOrMethod.getDeclaredAnnotations());
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public P get(Bean bean) {
        var meta = (DirectMetaBean) bean.metaBean();
        return (P) meta.propertyGet(bean, name(), false);
    }

    @Override
    public void set(Bean bean, Object value) {
        var meta = (DirectMetaBean) bean.metaBean();
        meta.propertySet(bean, name(), value, false);
    }

}
