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

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;

/**
 * Implementation of a meta-bean for {@code MapBean}.
 * 
 * @author Stephen Colebourne
 */
class MapBeanBuilder implements BeanBuilder<MapBean> {

    /**
     * The bean itself.
     */
    private final MapBean bean;

    /**
     * Creates the meta-bean.
     * 
     * @param bean  the underlying bean, not null
     */
    MapBeanBuilder(MapBean bean) {
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(String propertyName) {
        // lenient getter
        return bean.get(propertyName);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <P> P get(MetaProperty<P> metaProperty) {
        // this approach allows meta-property from one bean to be used with another
        return (P) bean.get(metaProperty.name());
    }

    //-----------------------------------------------------------------------
    @Override
    public MapBeanBuilder set(String propertyName, Object value) {
        bean.put(propertyName, value);
        return this;
    }

    @Override
    public MapBeanBuilder set(MetaProperty<?> metaProperty, Object value) {
        // this approach allows meta-property from one bean to be used with another
        bean.put(metaProperty.name(), value);
        return this;
    }

    @Override
    public MapBean build() {
        return bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return "MapBeanBuilder";
    }

}
