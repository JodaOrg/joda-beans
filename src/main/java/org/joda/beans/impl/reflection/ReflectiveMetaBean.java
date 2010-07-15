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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicPropertyMap;

/**
 * A standard meta-bean implementation.
 * <p>
 * This is the standard implementation of a meta-bean.
 * It requires that the bean implements {@code Bean} and has a no-arguments constructor.
 * 
 * @author Stephen Colebourne
 */
public final class ReflectiveMetaBean implements MetaBean {

    /** The bean type. */
    private final Class<? extends Bean> beanType;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<Object>> metaPropertyMap;

    /**
     * Factory to create a meta-bean avoiding duplicate generics.
     * 
     * @param <B>  the type of the bean
     * @param beanClass  the bean class, not null
     */
    public static <B extends Bean> ReflectiveMetaBean of(Class<B> beanClass) {
        return new ReflectiveMetaBean(beanClass);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     */
    @SuppressWarnings("unchecked")
    private ReflectiveMetaBean(Class<? extends Bean> beanType) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<Object>> map = new HashMap<String, MetaProperty<Object>>();
        Field[] fields = beanType.getDeclaredFields();
        for (Field field : fields) {
            if (MetaProperty.class.isAssignableFrom(field.getType()) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                MetaProperty<Object> mp;
                try {
                    mp = (MetaProperty<Object>) field.get(null);
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
    @Override
    public Bean createBean() {
        try {
            return beanType.newInstance();
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + name(), ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException("Bean cannot be created: " + name(), ex);
        }
    }

    @Override
    public PropertyMap createPropertyMap(Bean bean) {
        return BasicPropertyMap.of(bean);
    }

    //-----------------------------------------------------------------------
    @Override
    public String name() {
        return beanType.getName();
    }

    @Override
    public Class<? extends Bean> beanType() {
        return beanType;
    }

    //-----------------------------------------------------------------------
    @Override
    public int metaPropertyCount() {
        return metaPropertyMap.size();
    }

    @Override
    public boolean metaPropertyExists(String propertyName) {
        return metaPropertyMap.containsKey(propertyName);
    }

    @Override
    public MetaProperty<Object> metaProperty(String propertyName) {
        MetaProperty<Object> metaProperty = metaPropertyMap.get(propertyName);
        if (metaProperty == null) {
            throw new NoSuchElementException("Property not found: " + propertyName);
        }
        return metaProperty;
    }

    @Override
    public Iterable<MetaProperty<Object>> metaPropertyIterable() {
        return metaPropertyMap.values();
    }

    @Override
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
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
        return "MetaBean:" + name();
    }

}
