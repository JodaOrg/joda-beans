/*
 *  Copyright 2001-2010 Stephen Colebourne
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
package org.joda.beans;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A standard map of properties.
 * <p>
 * This is the standard implementation of a map of properties derived from a meta-bean.
 * 
 * @param <B>  the type of the bean
 * @author Stephen Colebourne
 */
public final class StandardPropertyMap<B extends Bean<B>> extends AbstractMap<String, Property<B, ?>> {

    /** The bean. */
    private final B bean;

    /**
     * Factory to create a property map avoiding duplicate generics.
     * 
     * @param metaBean  the meta-bean
     */
    public static <B extends Bean<B>> StandardPropertyMap<B> of(B metaBean) {
        return new StandardPropertyMap<B>(metaBean);
    }

    /**
     * Creates a property binding the bean to the meta-property.
     * 
     * @param bean  the bean that the property is bound to, not null
     * @param metaProperty  the meta property, not null
     */
    private StandardPropertyMap(B metaBean) {
        Beans.checkNotNull(metaBean, "MetaBean must not be null");
        this.bean = metaBean;
    }

    //-----------------------------------------------------------------------
    @Override
    public Set<Entry<String, Property<B, ?>>> entrySet() {
        return new AbstractSet<Entry<String,Property<B, ?>>>() {
            @Override
            public int size() {
                return bean.metaBean().metaPropertyMap().size();
            }
            @Override
            public Iterator<Entry<String, Property<B, ?>>> iterator() {
                final Iterator<MetaProperty<B, ?>> it = bean.metaBean().metaPropertyMap().values().iterator();
                return new Iterator<Entry<String, Property<B,?>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    @SuppressWarnings("unchecked")
                    public Entry<String, Property<B, ?>> next() {
                        MetaProperty<B, ?> meta = it.next();
                        return (Entry) StandardProperty.of(bean, meta);
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Unmodifiable");
                    }
                };
            }
        };
    }

}
