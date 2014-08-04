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
package org.joda.beans.impl;

import java.util.Map;
import java.util.Map.Entry;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;

/**
 * Basic implementation of {@code BeanBuilder} that wraps a {@code MetaBean}.
 * <p>
 * The subclass implementation generally has concrete fields for each property.
 * This class has effectively been replaced by {@link DirectFieldsBeanBuilder}.
 * It is retained for situations where the builder is being implemented manually.
 * 
 * @author Stephen Colebourne
 * @param <T>  the bean type
 */
public abstract class BasicImmutableBeanBuilder<T extends Bean>
        implements BeanBuilder<T> {

    /**
     * The meta bean.
     */
    private final MetaBean meta;

    /**
     * Constructs the builder.
     * 
     * @param meta  the meta-bean, not null
     */
    public BasicImmutableBeanBuilder(MetaBean meta) {
        this.meta = meta;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
        return get(meta.metaProperty(propertyName));
    }

    @Override
    public Object get(MetaProperty<?> metaProperty) {
        return get(metaProperty.name());
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<T> set(MetaProperty<?> metaProperty, Object value) {
        set(metaProperty.name(), value);
        return this;
    }

    @Override
    public BeanBuilder<T> setString(String propertyName, String value) {
        setString(meta.metaProperty(propertyName), value);
        return this;
    }

    @Override
    public BeanBuilder<T> setString(MetaProperty<?> metaProperty, String value) {
        set(metaProperty.name(), JodaBeanUtils.stringConverter().convertFromString(metaProperty.propertyType(), value));
        return this;
    }

    @Override
    public BeanBuilder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
        for (Entry<String, ? extends Object> entry : propertyValueMap.entrySet()) {
            set(entry.getKey(), entry.getValue());
        }
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the builder.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "BeanBuilder";
    }

}
