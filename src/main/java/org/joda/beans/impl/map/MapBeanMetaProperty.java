/*
 *  Copyright 2001-2011 Stephen Colebourne
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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaProperty;
import org.joda.beans.impl.BasicProperty;

/**
 * A meta-property using a {@code MapBean} for storage.
 * 
 * @author Stephen Colebourne
 */
public class MapBeanMetaProperty extends BasicMetaProperty<Object> {

    /** The bean. */
    private MapBean bean;

    /**
     * Factory to create a meta-property.
     * 
     * @param mapBean  the {@code MapBean}, not null
     * @param propertyName  the property name, not empty
     */
    public static MapBeanMetaProperty of(MapBean mapBean, String propertyName) {
        return new MapBeanMetaProperty(mapBean, propertyName);
    }

    /**
     * Constructor.
     * 
     * @param mapBean  the {@code MapBean}, not null
     * @param propertyName  the property name, not empty
     */
    private MapBeanMetaProperty(MapBean mapBean, String propertyName) {
        super(propertyName);
        this.bean = mapBean;
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<Object> createProperty(Bean bean) {
        return BasicProperty.of(bean, this);
    }

    @Override
    public MetaBean metaBean() {
        return bean;
    }

    @Override
    public Class<Object> propertyType() {
        return Object.class;
    }

    @Override
    public Class<Object> propertyGenericType() {
        return Object.class;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return PropertyReadWrite.READ_WRITE;
    }

    @Override
    public List<Annotation> annotations() {
        return Collections.emptyList();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(Bean bean) {
        if (bean != this.bean) {
            throw new ClassCastException("Bean is a MapBean, but not the correct MapBean");
        }
        return this.bean.get(name());
    }

    @Override
    public void set(Bean bean, Object value) {
        if (bean != this.bean) {
            throw new ClassCastException("Bean is a MapBean, but not the correct MapBean");
        }
        this.bean.put(name(), value);
    }

}
