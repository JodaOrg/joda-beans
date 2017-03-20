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
package org.joda.beans.impl.map;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;
import org.joda.beans.impl.BasicMetaProperty;

/**
 * A meta-property using a {@code MapBean} for storage.
 * 
 * @author Stephen Colebourne
 */
final class MapBeanMetaProperty extends BasicMetaProperty<Object> {

    /** The meta-bean. */
    private final MetaBean metaBean;

    /**
     * Factory to create a meta-property.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     */
    static MapBeanMetaProperty of(MetaBean metaBean, String propertyName) {
        return new MapBeanMetaProperty(metaBean, propertyName);
    }

    /**
     * Constructor.
     * 
     * @param metaBean  the meta-bean, not null
     * @param propertyName  the property name, not empty
     */
    private MapBeanMetaProperty(MetaBean metaBean, String propertyName) {
        super(propertyName);
        this.metaBean = metaBean;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Class<?> declaringType() {
        return MapBean.class;
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
    public PropertyStyle style() {
        return PropertyStyle.READ_WRITE;
    }

    @Override
    public List<Annotation> annotations() {
        return Collections.emptyList();
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(Bean bean) {
        return ((MapBean) bean).get(name());
    }

    @Override
    public void set(Bean bean, Object value) {
        ((MapBean) bean).put(name(), value);
    }

}
