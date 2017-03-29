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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

/**
 * A standard map of properties.
 * <p>
 * This is the standard implementation of a map of properties derived from a meta-bean.
 * 
 * @author Stephen Colebourne
 */
public final class BasicPropertyMap
        extends AbstractMap<String, Property<?>> {

    /** The bean. */
    private final Bean bean;

    /**
     * Factory to create a property map avoiding duplicate generics.
     * 
     * @param bean  the bean
     * @return the property map, not null
     */
    public static BasicPropertyMap of(Bean bean) {
        return new BasicPropertyMap(bean);
    }

    /**
     * Creates a property map.
     * 
     * @param bean  the bean that the property is bound to, not null
     */
    private BasicPropertyMap(Bean bean) {
        if (bean == null) {
            throw new NullPointerException("Bean must not be null");
        }
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return bean.metaBean().metaPropertyCount();
    }

    @Override
    public boolean containsKey(Object obj) {
        return obj instanceof String ? bean.metaBean().metaPropertyExists(obj.toString()) : false;
    }

    @Override
    public Property<?> get(Object obj) {
        return containsKey(obj) ? bean.metaBean().metaProperty(obj.toString()).createProperty(bean) : null;
    }

    @Override
    public Set<String> keySet() {
        return bean.metaBean().metaPropertyMap().keySet();
    }

    @Override
    public Set<Entry<String, Property<?>>> entrySet() {
        return new AbstractSet<Entry<String, Property<?>>>() {
            // TODO: possibly override contains()
            @Override
            public int size() {
                return bean.metaBean().metaPropertyCount();
            }
            @Override
            public Iterator<Entry<String, Property<?>>> iterator() {
                final Iterator<MetaProperty<?>> it = bean.metaBean().metaPropertyMap().values().iterator();
                return new Iterator<Entry<String, Property<?>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public Entry<String, Property<?>> next() {
                        MetaProperty<?> meta = it.next();
                        return new SimpleImmutableEntry<>(meta.name(), BasicProperty.of(bean, meta));
                    }
                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("Unmodifiable");
                    }
                };
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * Flattens the contents of this property map to a {@code Map}.
     * <p>
     * The returned map will contain all the properties from the bean with their actual values.
     * 
     * @return the unmodifiable map of property name to value, not null
     */
    public Map<String, Object> flatten() {
        return JodaBeanUtils.flatten(bean);
    }

}
