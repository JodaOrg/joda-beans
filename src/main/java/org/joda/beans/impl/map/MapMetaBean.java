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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.joda.beans.BeanBuilder;
import org.joda.beans.DynamicMetaBean;
import org.joda.beans.MetaProperty;

/**
 * Implementation of a meta-bean for {@code MapBean}.
 * 
 * @author Stephen Colebourne
 */
class MapMetaBean implements DynamicMetaBean {

    /**
     * The bean itself.
     */
    private final MapBean bean;

    /**
     * Creates the meta-bean.
     * 
     * @param bean  the underlying bean, not null
     */
    MapMetaBean(MapBean bean) {
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isBuildable() {
        return true;
    }

    @Override
    public BeanBuilder<MapBean> builder() {
        return new MapBeanBuilder(bean);
    }

    @Override
    public Class<MapBean> beanType() {
        return MapBean.class;
    }

    @Override
    public int metaPropertyCount() {
        return bean.size();
    }

    @Override
    public boolean metaPropertyExists(String name) {
        return bean.containsKey(name);
    }

    @Override
    public MetaProperty<Object> metaProperty(String name) {
        // do not check if exists
        return MapBeanMetaProperty.of(this, name);
    }

    @Override
    public Iterable<MetaProperty<?>> metaPropertyIterable() {
        return new Iterable<MetaProperty<?>>() {
            private final Iterator<String> it = bean.keySet().iterator();
            @Override
            public Iterator<MetaProperty<?>> iterator() {
                return new Iterator<MetaProperty<?>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public MetaProperty<?> next() {
                        return MapBeanMetaProperty.of(MapMetaBean.this, it.next());
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Unmodifiable");
                    }
                    
                };
            }
        };
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        Map<String, MetaProperty<?>> map = new LinkedHashMap<>();
        for (String name : bean.keySet()) {
            map.put(name, MapBeanMetaProperty.of(this, name));
        }
        return Collections.unmodifiableMap(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public void metaPropertyDefine(String propertyName, Class<?> propertyType) {
        bean.propertyDefine(propertyName, propertyType);
    }

    @Override
    public void metaPropertyRemove(String propertyName) {
        bean.propertyRemove(propertyName);
    }

    @Override
    public List<Annotation> annotations() {
        return Collections.emptyList();
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaBean:" + beanType().getSimpleName();
    }

}
