/*
 *  Copyright 2001-2012 Stephen Colebourne
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
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicBeanBuilder;

/**
 * Implementation of a meta-bean for {@code FlexiBean}.
 * 
 * @author Stephen Colebourne
 */
class FlexiMetaBean implements MetaBean {

    /**
     * The bean itself.
     */
    private final FlexiBean bean;

    /**
     * Creates the meta-bean.
     * 
     * @param flexiBean  the underlying bean, not null
     */
    FlexiMetaBean(FlexiBean flexiBean) {
        bean = flexiBean;
    }

    @Override
    public BeanBuilder<FlexiBean> builder() {
        return new BasicBeanBuilder<FlexiBean>(new FlexiBean());
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
        Object obj = bean.get(name);
        if (obj == null) {
            throw new NoSuchElementException("Unknown property: " + name);
        }
        return FlexiMetaProperty.of(bean.metaBean, name);
    }

    @Override
    public Iterable<MetaProperty<?>> metaPropertyIterable() {
        if (bean.data.isEmpty()) {
            return Collections.emptySet();
        }
        return new Iterable<MetaProperty<?>>() {
            private final Iterator<String> it = FlexiMetaBean.this.bean.data.keySet().iterator();
            @Override
            public Iterator<MetaProperty<?>> iterator() {
                return new Iterator<MetaProperty<?>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public MetaProperty<?> next() {
                        return FlexiMetaProperty.of(FlexiMetaBean.this.bean.metaBean, it.next());
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
            map.put(name, FlexiMetaProperty.of(bean.metaBean, name));
        }
        return Collections.unmodifiableMap(map);
    }

}
