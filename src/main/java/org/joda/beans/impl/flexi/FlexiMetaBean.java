/*
 *  Copyright 2001-2014 Stephen Colebourne
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
package org.joda.beans.impl.flexi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.DynamicMetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;

/**
 * Implementation of a meta-bean for {@code FlexiBean}.
 * 
 * @author Stephen Colebourne
 */
class FlexiMetaBean implements DynamicMetaBean {

    /**
     * The bean itself.
     */
    private final FlexiBean bean;

    /**
     * Creates the meta-bean.
     * 
     * @param bean  the underlying bean, not null
     */
    FlexiMetaBean(FlexiBean bean) {
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<FlexiBean> builder() {
        return new FlexiBeanBuilder(bean);
    }

    @Override
    public PropertyMap createPropertyMap(Bean bean) {
        return FlexiPropertyMap.of(beanType().cast(bean));
    }

    @Override
    public Class<FlexiBean> beanType() {
        return FlexiBean.class;
    }

    @Override
    public String beanName() {
        return FlexiBean.class.getName();
    }

    @Override
    public int metaPropertyCount() {
        return bean.size();
    }

    @Override
    public boolean metaPropertyExists(String name) {
        return bean.propertyExists(name);
    }

    @Override
    public MetaProperty<Object> metaProperty(String name) {
        // do not check if exists
        return FlexiMetaProperty.of(this, name);
    }

    @Override
    public Iterable<MetaProperty<?>> metaPropertyIterable() {
        if (bean.data.isEmpty()) {
            return Collections.emptySet();
        }
        return new Iterable<MetaProperty<?>>() {
            private final Iterator<String> it = bean.data.keySet().iterator();
            @Override
            public Iterator<MetaProperty<?>> iterator() {
                return new Iterator<MetaProperty<?>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public MetaProperty<?> next() {
                        return FlexiMetaProperty.of(FlexiMetaBean.this, it.next());
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
        if (bean.data.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, MetaProperty<?>> map = new HashMap<String, MetaProperty<?>>();
        for (String name : bean.data.keySet()) {
            map.put(name, FlexiMetaProperty.of(this, name));
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

}
