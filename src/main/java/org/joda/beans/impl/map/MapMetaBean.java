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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.BasicMetaBean;

/**
 * Implementation of a meta-bean for {@code MapBean}.
 * 
 * @author Stephen Colebourne
 */
class MapMetaBean extends BasicMetaBean {

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
    public BeanBuilder<MapBean> builder() {
        return new BasicBeanBuilder<MapBean>(new MapBean());
    }

    @Override
    public PropertyMap createPropertyMap(Bean bean) {
        return MapBeanPropertyMap.of(beanType().cast(bean));
    }

    @Override
    public Class<MapBean> beanType() {
        return MapBean.class;
    }

    @Override
    public String beanName() {
        return MapBean.class.getName();
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
        return bean.metaProperty(name);
    }

    @Override
    public Iterable<MetaProperty<Object>> metaPropertyIterable() {
        return new Iterable<MetaProperty<Object>>() {
            private final Iterator<String> it = bean.keySet().iterator();
            @Override
            public Iterator<MetaProperty<Object>> iterator() {
                return new Iterator<MetaProperty<Object>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public MetaProperty<Object> next() {
                        return MapBeanMetaProperty.of(bean, it.next());
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
    public Map<String, MetaProperty<Object>> metaPropertyMap() {
        Map<String, MetaProperty<Object>> map = new HashMap<String, MetaProperty<Object>>();
        for (String name : bean.keySet()) {
            map.put(name, MapBeanMetaProperty.of(bean, name));
        }
        return map;
    }

}
