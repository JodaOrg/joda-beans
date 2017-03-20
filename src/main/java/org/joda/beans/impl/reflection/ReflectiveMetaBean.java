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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.BasicBeanBuilder;

/**
 * A meta-bean implementation that uses reflection.
 * <p>
 * This is implementation of a meta-bean can be used directly by applications without code generation.
 * It requires that the bean implements {@code Bean} and has a no-arguments constructor.
 * Therefore, it is only suitable for mutable beans.
 * <p>
 * Typically, the meta-bean will be created as a public static final constant.
 * Only one method from {@link Bean} needs to be implemented, which simply returns the meta-bean.
 * 
 * @author Stephen Colebourne
 */
public final class ReflectiveMetaBean implements MetaBean {

    /** The bean type. */
    private final Class<? extends Bean> beanType;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;

    /**
     * Create a meta-bean, reflecting to find meta properties.
     * 
     * @param <B>  the type of the bean
     * @param beanClass  the bean class, not null
     * @return the meta-bean, not null
     * @deprecated Use factory that accepts the property names
     */
    @Deprecated
    public static <B extends Bean> ReflectiveMetaBean of(Class<B> beanClass) {
        return new ReflectiveMetaBean(beanClass);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @deprecated Use constructor that accepts the property names
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    private ReflectiveMetaBean(Class<? extends Bean> beanType) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<?>> map = new HashMap<>();
        Field[] fields = beanType.getDeclaredFields();
        for (Field field : fields) {
            if (MetaProperty.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                MetaProperty<Object> mp;
                try {
                    mp = (MetaProperty<Object>) field.get(null);
                    if (mp instanceof ReflectiveMetaProperty) {
                        ((ReflectiveMetaProperty<Object>) mp).setMetaBean(this);
                    }
                } catch (IllegalArgumentException ex) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName(), ex);
                } catch (IllegalAccessException ex) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName(), ex);
                }
                if (mp == null) {
                    throw new UnsupportedOperationException("MetaProperty cannot be created: " + field.getName() + ": Value must not be null");
                }
                map.put(mp.name(), mp);
            }
        }
        
        this.metaPropertyMap = Collections.unmodifiableMap(map);
    }

    //-----------------------------------------------------------------------
    /**
     * Create a meta-bean and meta properties.
     * <p>
     * The meta-properties will be created from the property names by searching for a getter and setter.
     * 
     * @param <B>  the type of the bean
     * @param beanClass  the bean class, not null
     * @param propertyNames  the property names, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> ReflectiveMetaBean of(Class<B> beanClass, String... propertyNames) {
        return new ReflectiveMetaBean(beanClass, propertyNames);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param propertyNames  the property names, not null
     */
    @SuppressWarnings("deprecation")
    private ReflectiveMetaBean(Class<? extends Bean> beanType, String[] propertyNames) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        if (propertyNames == null) {
            throw new NullPointerException("Property names must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<?>> map = new HashMap<>();
        for (String name : propertyNames) {
            map.put(name, new ReflectiveMetaProperty<>(this, beanType, name));
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<Bean> builder() {
        try {
            Bean bean = beanType.newInstance();
            return new BasicBeanBuilder<>(bean);
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + beanName(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + beanName(), ex);
        }
    }

    @Override
    public Class<? extends Bean> beanType() {
        return beanType;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        return metaPropertyMap;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ReflectiveMetaBean) {
            ReflectiveMetaBean other = (ReflectiveMetaBean) obj;
            return this.beanType.equals(other.beanType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return beanType.hashCode() + 3;
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaBean:" + beanName();
    }

}
