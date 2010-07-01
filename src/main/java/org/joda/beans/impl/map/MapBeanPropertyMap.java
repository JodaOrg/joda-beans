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
package org.joda.beans.impl.map;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Property;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicProperty;

/**
 * A map of properties for a {@code MapBean}.
 * 
 * @author Stephen Colebourne
 */
public final class MapBeanPropertyMap
        extends AbstractMap<String, Property<MapBean, Object>> implements PropertyMap<MapBean> {

    /** The bean. */
    private final MapBean bean;

    /**
     * Factory to create a property map avoiding duplicate generics.
     * 
     * @param bean  the bean
     */
    public static MapBeanPropertyMap of(MapBean bean) {
        return new MapBeanPropertyMap(bean);
    }

    /**
     * Creates a property map.
     * 
     * @param bean  the bean that the property is bound to, not null
     */
    private MapBeanPropertyMap(MapBean bean) {
        if (bean == null) {
            throw new NullPointerException("Bean must not be null");
        }
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return bean.size();
    }

    @Override
    public boolean containsKey(Object obj) {
        return bean.containsKey(obj);
    }

    @Override
    public Property<MapBean, Object> get(Object obj) {
        return containsKey(obj) ? bean.property(obj.toString()) : null;
    }

    @Override
    public Set<String> keySet() {
        return bean.keySet();
    }

    @Override
    public Set<Entry<String, Property<MapBean, Object>>> entrySet() {
        return new AbstractSet<Entry<String,Property<MapBean, Object>>>() {
            // TODO: possibly override contains()
            @Override
            public int size() {
                return bean.size();
            }
            @Override
            public Iterator<Entry<String, Property<MapBean, Object>>> iterator() {
                final Iterator<String> it = bean.keySet().iterator();
                return new Iterator<Entry<String, Property<MapBean, Object>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public Entry<String, Property<MapBean, Object>> next() {
                        String name = it.next();
                        return BasicProperty.of(bean, MapBeanMetaProperty.of(name));
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
        Map<String, Object> copy = new HashMap<String, Object>(bean);
        return Collections.unmodifiableMap(copy);
    }

}
