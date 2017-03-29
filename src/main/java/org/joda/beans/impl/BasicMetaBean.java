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

import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyMap;

/**
 * Basic implementation of {@code MetaBean}.
 * 
 * @author Stephen Colebourne
 */
@SuppressWarnings("deprecation")
public abstract class BasicMetaBean implements MetaBean {

    @Override
    public PropertyMap createPropertyMap(Bean bean) {
        return BasicPropertyMap.of(bean);
    }

    @Override
    public String beanName() {
        return beanType().getName();
    }

    @Override
    public int metaPropertyCount() {
        return metaPropertyMap().size();
    }

    @Override
    public boolean metaPropertyExists(String propertyName) {
        return metaPropertyMap().containsKey(propertyName);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> MetaProperty<R> metaProperty(String propertyName) {
        MetaProperty<?> mp = metaPropertyMap().get(propertyName);
        if (mp == null) {
            throw new NoSuchElementException("Unknown property: " + propertyName);
        }
        return (MetaProperty<R>) mp;
    }

    @Override
    public Iterable<MetaProperty<?>> metaPropertyIterable() {
        return metaPropertyMap().values();
    }

    //-----------------------------------------------------------------------
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
