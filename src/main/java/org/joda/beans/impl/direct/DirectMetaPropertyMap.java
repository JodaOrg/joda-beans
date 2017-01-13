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
package org.joda.beans.impl.direct;

import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.joda.beans.MetaProperty;

/**
 * A map of name to meta-property designed for use by the code generator.
 * <p>
 * This meta-property map implementation is designed primarily for code-generation.
 * It stores a reference to the meta-bean and the meta-properties.
 * The meta-properties are accessed using {@link DirectMetaBean#metaPropertyGet(String)}.
 * <p>
 * This class is immutable and thread-safe.
 * 
 * @author Stephen Colebourne
 */
@SuppressWarnings("rawtypes")
public final class DirectMetaPropertyMap implements Map<String, MetaProperty<?>> {

    /** The meta-bean. */
    private final DirectMetaBean metaBean;
    /** The property names. */
    private final Set<String> keys;
    /** The meta-properties. */
    private final Collection<MetaProperty<?>> values;
    /** The map entries. */
    private final Set<Entry<String, MetaProperty<?>>> entries;

    /**
     * Constructor.
     * 
     * @param metaBean  the meta-bean, not null
     * @param parent  the superclass parent, may be null
     * @param propertyNames  the property names, not null
     */
    @SuppressWarnings("unchecked")
    public DirectMetaPropertyMap(final DirectMetaBean metaBean, DirectMetaPropertyMap parent, String... propertyNames) {
        if (metaBean == null) {
            throw new NullPointerException("MetaBean must not be null");
        }
        this.metaBean = metaBean;
        int parentSize = 0;
        final Entry<String, MetaProperty<?>>[] metaProperties;
        if (parent != null) {
            parentSize = parent.size();
            metaProperties = Arrays.copyOf(((Entries) parent.entries).metaProperties, parentSize + propertyNames.length);
        } else {
            metaProperties = new Entry[propertyNames.length];
        }
        for (int i = 0; i < propertyNames.length; i++) {
            metaProperties[i + parentSize] = new AbstractMap.SimpleImmutableEntry(propertyNames[i], metaBean.metaPropertyGet(propertyNames[i]));
        }
        keys = new Keys(metaProperties);
        values = new Values(metaProperties);
        entries = new Entries(metaProperties);
    }

    //-----------------------------------------------------------------------
    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @SuppressWarnings("unchecked")
    @Override
    public MetaProperty<Object> get(Object propertyName) {
        if (propertyName  instanceof String) {
            return (MetaProperty<Object>) metaBean.metaPropertyGet((String) propertyName);
        }
        return null;
    }

    @Override
    public boolean containsKey(Object propertyName) {
        return propertyName instanceof String &&
                metaBean.metaPropertyGet(propertyName.toString()) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        return value instanceof MetaProperty &&
                metaBean.metaPropertyGet(((MetaProperty<?>) value).name()) != null;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaProperty<?> put(String key, MetaProperty<?> value) {
        throw new UnsupportedOperationException("DirectBean meta-property map cannot be modified");
    }

    @Override
    public MetaProperty<?> remove(Object key) {
        throw new UnsupportedOperationException("DirectBean meta-property map cannot be modified");
    }

    @Override
    public void putAll(Map<? extends String, ? extends MetaProperty<?>> m) {
        throw new UnsupportedOperationException("DirectBean meta-property map cannot be modified");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("DirectBean meta-property map cannot be modified");
    }

    //-----------------------------------------------------------------------
    @Override
    public Set<String> keySet() {
        return keys;
    }

    @Override
    public Collection<MetaProperty<?>> values() {
        return values;
    }

    @Override
    public Set<Entry<String, MetaProperty<?>>> entrySet() {
        return entries;
    }

    //-----------------------------------------------------------------------
    /**
     * Collection implementation for the keys.
     */
    private static final class Keys extends AbstractSet<String> {
        private final Entry<String, MetaProperty<?>>[] metaProperties;

        private Keys(Entry<String, MetaProperty<?>>[] metaProperties) {
            this.metaProperties = metaProperties;
        }

        @Override
        public Iterator<String> iterator() {
            return new Iterator<String>() {
                private int index;
                @Override
                public boolean hasNext() {
                    return index < metaProperties.length;
                }
                @Override
                public String next() {
                    return metaProperties[index++].getKey();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return metaProperties.length;
        }
    }

    /**
     * Collection implementation for the values.
     */
    private static final class Values extends AbstractCollection<MetaProperty<?>> {
        private final Entry<String, MetaProperty<?>>[] metaProperties;

        private Values(Entry<String, MetaProperty<?>>[] metaProperties) {
            this.metaProperties = metaProperties;
        }

        @Override
        public Iterator<MetaProperty<?>> iterator() {
            return new Iterator<MetaProperty<?>>() {
                private int index;
                @Override
                public boolean hasNext() {
                    return index < metaProperties.length;
                }
                @Override
                public MetaProperty<?> next() {
                    return metaProperties[index++].getValue();
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return metaProperties.length;
        }
    }

    /**
     * Collection implementation for the entries.
     */
    private static final class Entries extends AbstractSet<Entry<String, MetaProperty<?>>> {
        private final Entry<String, MetaProperty<?>>[] metaProperties;

        private Entries(Entry<String, MetaProperty<?>>[] metaProperties) {
            this.metaProperties = metaProperties;
        }

        @Override
        public Iterator<Entry<String, MetaProperty<?>>> iterator() {
            return new Iterator<Entry<String, MetaProperty<?>>>() {
                private int index;
                @Override
                public boolean hasNext() {
                    return index < metaProperties.length;
                }
                @Override
                public Entry<String, MetaProperty<?>> next() {
                    return metaProperties[index++];
                }
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public int size() {
            return metaProperties.length;
        }
    }

}
