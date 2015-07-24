/*
 *  Copyright 2001-2015 Stephen Colebourne
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
package org.joda.beans.impl.light;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

/**
 * Implementation of {@code BeanBuilder} that builds light beans.
 * 
 * @author Stephen Colebourne
 * @param <B>  the bean type
 */
class LightBeanBuilder<B extends Bean>
        implements BeanBuilder<B> {

    /** The meta-bean. */
    private final LightMetaBean<B> metaBean;
    /** The data. */
    private final Object[] data;

    //-----------------------------------------------------------------------
    /**
     * Constructs the builder wrapping the target bean.
     * 
     * @param metaBean  the target meta-bean, not null
     */
    LightBeanBuilder(LightMetaBean<B> metaBean, Object[] data) {
        this.metaBean = metaBean;
        this.data = data;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
        return get(metaBean.metaProperty(propertyName));
    }

    @Override
    public Object get(MetaProperty<?> metaProperty) {
        return data[index(metaProperty)];
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<B> set(String propertyName, Object value) {
        return set(metaBean.metaProperty(propertyName), value);
    }

    @Override
    public BeanBuilder<B> set(MetaProperty<?> metaProperty, Object value) {
        data[index(metaProperty)] = value;
        return this;
    }

    @Override
    public BeanBuilder<B> setString(String propertyName, String value) {
        return setString(metaBean.metaProperty(propertyName), value);
    }

    @Override
    public BeanBuilder<B> setString(MetaProperty<?> metaProperty, String value) {
        Object object = JodaBeanUtils.stringConverter().convertFromString(metaProperty.propertyType(), value);
        return set(metaProperty, object);
    }

    @Override
    public BeanBuilder<B> setAll(Map<String, ? extends Object> propertyValueMap) {
        for (Entry<String, ? extends Object> entry : propertyValueMap.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    private int index(MetaProperty<?> metaProperty) {
        return ((LightMetaProperty<?>) metaProperty).getConstructorIndex();
    }

    //-----------------------------------------------------------------------
    @Override
    public B build() {
        return metaBean.build(data);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the builder.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "BeanBuilder for " + metaBean.beanName();
    }

}
