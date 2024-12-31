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
package org.joda.beans.impl;

import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaProperty;

/**
 * The RecordBean bean builder.
 * 
 * @param <T>  the record bean type
 */
final class RecordBeanBuilder<T extends ImmutableBean> implements BeanBuilder<T> {

    private final RecordMetaBean<T> metaBean;
    private final Object[] data;

    RecordBeanBuilder(RecordMetaBean<T> metaBean, Object[] data) {
        this.metaBean = metaBean;
        this.data = data;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
        return data[metaBean.index(propertyName)];
    }

    @Override
    public <P> P get(MetaProperty<P> metaProperty) {
        return metaProperty.propertyType().cast(get(metaProperty.name()));
    }

    @Override
    public BeanBuilder<T> set(String propertyName, Object value) {
        data[metaBean.index(propertyName)] = value;
        return this;
    }

    @Override
    public BeanBuilder<T> set(MetaProperty<?> metaProperty, Object value) {
        return set(metaProperty.name(), value);
    }

    @Override
    public T build() {
        return metaBean.build(data);
    }

    /**
     * Returns a string that summarises the builder.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "BeanBuilder: " + metaBean.beanType().getSimpleName();
    }
}
