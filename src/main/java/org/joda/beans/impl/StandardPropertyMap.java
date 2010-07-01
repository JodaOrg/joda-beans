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
package org.joda.beans.impl;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyMap;

/**
 * A standard map of properties.
 * <p>
 * This is the standard implementation of a map of properties derived from a meta-bean.
 * 
 * @param <B>  the type of the bean
 * @author Stephen Colebourne
 */
public final class StandardPropertyMap<B>
        extends AbstractMap<String, Property<B, Object>> implements PropertyMap<B> {

    /** The bean. */
    private final Bean<B> bean;

    /**
     * Factory to create a property map avoiding duplicate generics.
     * 
     * @param bean  the bean
     */
    public static <B, T extends Bean<B>> StandardPropertyMap<B> of(T bean) {
        return new StandardPropertyMap<B>(bean);
    }

    /**
     * Creates a property map.
     * 
     * @param bean  the bean that the property is bound to, not null
     */
    private StandardPropertyMap(Bean<B> bean) {
        if (bean == null) {
            throw new NullPointerException("Bean must not be null");
        }
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return bean.metaBean().metaPropertyMap().size();
    }

    @Override
    public boolean containsKey(Object obj) {
        return obj instanceof String ? bean.metaBean().metaPropertyExists(obj.toString()) : false;
    }

    @Override
    public Property<B, Object> get(Object obj) {
        return containsKey(obj) ? bean.metaBean().metaProperty(obj.toString()).createProperty(bean) : null;
    }

    @Override
    public Set<String> keySet() {
        return bean.metaBean().metaPropertyMap().keySet();
    }

    @Override
    public Set<Entry<String, Property<B, Object>>> entrySet() {
        return new AbstractSet<Entry<String,Property<B, Object>>>() {
            @Override
            public int size() {
                return bean.metaBean().metaPropertyMap().size();
            }
            @Override
            public boolean contains(Object obj) {
                if (obj instanceof Entry<?, ?>) {
                    Entry<?, ?> entry = (Entry<?, ?>) obj;
                    return entry.getKey() instanceof String && bean.metaBean().metaPropertyExists(obj.toString());
                }
                return false;
            }
            @Override
            public Iterator<Entry<String, Property<B, Object>>> iterator() {
                final Iterator<MetaProperty<B, Object>> it = bean.metaBean().metaPropertyMap().values().iterator();
                return new Iterator<Entry<String, Property<B, Object>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    @SuppressWarnings("unchecked")
                    public Entry<String, Property<B, Object>> next() {
                        MetaProperty<B, Object> meta = it.next();
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

    //-----------------------------------------------------------------------
    @Override
    public Map<String, Object> flatten() {
        // TODO: dedicated map implementation
        Map<String, MetaProperty<B, Object>> propertyMap = bean.metaBean().metaPropertyMap();
        Map<String, Object> map = new HashMap<String, Object>(propertyMap.size());
        for (Entry<String, MetaProperty<B, Object>> entry : propertyMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(bean));
        }
        return Collections.unmodifiableMap(map);
    }

}
