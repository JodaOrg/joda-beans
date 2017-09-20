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
package org.joda.beans.impl.light;

import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
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
    @SuppressWarnings("unchecked")
    public <P> P get(MetaProperty<P> metaProperty) {
        return (P) data[index(metaProperty)];
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

    @SuppressWarnings("deprecation")
    private int index(MetaProperty<?> metaProperty) {
        if (metaProperty instanceof LightMetaProperty) {
            int index = ((LightMetaProperty<?>) metaProperty).getConstructorIndex();
            if (index < 0) {
                throw new NoSuchElementException("Derived property cannot be set: " + metaProperty.name());
            }
            return index;
        }
        if (metaProperty instanceof AbstractLightMetaProperty) {
            try {
                int index = ((AbstractLightMetaProperty<?>) metaProperty).getConstructorIndex();
                if (index < 0) {
                    throw new NoSuchElementException("Derived property cannot be set: " + metaProperty.name());
                }
                return index;
            } catch (ClassCastException ex) {
                for (MetaProperty<?> mp : metaBean.metaPropertyIterable()) {
                    if (mp.equals(metaProperty)) {
                        return ((LightMetaProperty<?>) mp).getConstructorIndex();
                    }
                }
                throw new NoSuchElementException("Unknown property: " + metaProperty.name());
            }
        }
        return index(metaBean.metaProperty(metaProperty.name()));
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
