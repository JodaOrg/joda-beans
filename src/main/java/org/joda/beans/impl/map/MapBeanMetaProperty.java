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
package org.joda.beans.impl.map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.StandardProperty;

/**
 * A meta-property using a {@code MapBean} for storage.
 * 
 * @author Stephen Colebourne
 */
public class MapBeanMetaProperty implements MetaProperty<MapBean, Object> {

    /** The map key, also known as the property name. */
    private final String name;

    /**
     * Factory to create a meta-property.
     * 
     * @param propertyName  the property name, not null
     */
    public static MapBeanMetaProperty of(String propertyName) {
        return new MapBeanMetaProperty(propertyName);
    }

    /**
     * Constructor.
     * 
     * @param propertyName  the property name, not null
     */
    private MapBeanMetaProperty(String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new IllegalArgumentException("Invalid property name: " + propertyName);
        }
        name = propertyName;
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<MapBean, Object> createProperty(Bean<MapBean> bean) {
        return StandardProperty.of(bean, this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<Object> propertyClass() {
        return Object.class;
    }

    @Override
    public Class<MapBean> beanClass() {
        return MapBean.class;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return PropertyReadWrite.READ_WRITE;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(Bean<MapBean> bean) {
        return bean.beanData().get(name);
    }

    @Override
    public void set(Bean<MapBean> bean, Object value) {
        bean.beanData().put(name, value);
    }

    @Override
    public Object put(Bean<MapBean> bean, Object value) {
        Object old = get(bean);
        set(bean, value);
        return old;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a debugging string.
     * 
     * @return a debugging string
     */
    @Override
    public String toString() {
        return "MetaProperty:" + name();
    }

}
