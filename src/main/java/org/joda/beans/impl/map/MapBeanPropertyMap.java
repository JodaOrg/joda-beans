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

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaBean;
import org.joda.beans.Property;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicProperty;

/**
 * A map of properties for a {@code MapBean}.
 * 
 * @author Stephen Colebourne
 */
@SuppressWarnings("deprecation")
final class MapBeanPropertyMap
        extends AbstractMap<String, Property<?>> implements PropertyMap {

    /** The bean. */
    private final MapBean bean;

    /**
     * Factory to create a property map avoiding duplicate generics.
     * 
     * @param bean  the bean, not null
     * @return the property map, not null
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
    public Property<?> get(Object obj) {
        return containsKey(obj) ? bean.property(obj.toString()) : null;
    }

    @Override
    public Set<String> keySet() {
        return bean.keySet();
    }

    @Override
    public Set<Entry<String, Property<?>>> entrySet() {
        final MetaBean metaBean = bean.metaBean();
        return new AbstractSet<Entry<String, Property<?>>>() {
            // TODO: possibly override contains()
            @Override
            public int size() {
                return bean.size();
            }
            @Override
            public Iterator<Entry<String, Property<?>>> iterator() {
                final Iterator<String> it = bean.keySet().iterator();
                return new Iterator<Entry<String, Property<?>>>() {
                    @Override
                    public boolean hasNext() {
                        return it.hasNext();
                    }
                    @Override
                    public Entry<String, Property<?>> next() {
                        String name = it.next();
                        Property<?> prop = BasicProperty.of(bean, MapBeanMetaProperty.of(metaBean, name));
                        return new SimpleImmutableEntry<String, Property<?>>(name, prop);
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
