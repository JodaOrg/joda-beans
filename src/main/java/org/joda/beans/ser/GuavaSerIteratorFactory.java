/*
 *  Copyright 2001-2013 Stephen Colebourne
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
package org.joda.beans.ser;

import java.util.Iterator;
import java.util.Map;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.TreeMultiset;

/**
 * Guava factory used to create wrappers around collection-like objects.
 *
 * @author Stephen Colebourne
 */
public class GuavaSerIteratorFactory extends SerIteratorFactory {

    /**
     * Creates an iterator wrapper for an arbitrary value.
     * 
     * @param value  the possible collection-like object, not null
     * @return the iterator, null if not a collection-like type
     */
    @Override
    public SerIterator create(final Object value) {
        if (value instanceof Multimap) {
            return multimap((Multimap<?, ?>) value, Object.class, Object.class);
        }
        if (value instanceof Multiset) {
            return multiset((Multiset<?>) value, Object.class);
        }
        return super.create(value);
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param value  the possible collection-like object, not null
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterator, null if not a collection-like type
     */
    @Override
    public SerIterator create(final Object value, final MetaProperty<?> prop, Class<?> beanClass) {
        if (value instanceof Multiset) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            return multiset((Multiset<?>) value, valueType);
        }
        if (value instanceof Multimap) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            return multimap((Multimap<?, ?>) value, keyType, valueType);
        }
        return super.create(value, prop, beanClass);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param metaTypeDescription  the description of the collection type, not null
     * @return the iterator, null if not a collection-like type
     */
    @Override
    public SerIterable createIterable(final String metaTypeDescription) {
        if (metaTypeDescription.equals("SetMultimap")) {
            return setMultimap(Object.class, Object.class);
        }
        if (metaTypeDescription.equals("ListMultimap")) {
            return listMultimap(Object.class, Object.class);
        }
        if (metaTypeDescription.equals("Multimap")) {
            return listMultimap(Object.class, Object.class);
        }
        if (metaTypeDescription.equals("Multiset")) {
            return multiset(Object.class);
        }
        return super.createIterable(metaTypeDescription);
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterator, null if not a collection-like type
     */
    @Override
    public SerIterable createIterable(final MetaProperty<?> prop, Class<?> beanClass) {
        if (SortedMultiset.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            return sortedMultiset(valueType);
        }
        if (Multiset.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            return multiset(valueType);
        }
        if (SetMultimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            return setMultimap(keyType, valueType);
        }
        if (ListMultimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            return listMultimap(keyType, valueType);
        }
        if (Multimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            return listMultimap(keyType, valueType);
        }
        return super.createIterable(prop, beanClass);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code Multiset}.
     * 
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    public static final SerIterable multiset(final Class<?> valueType) {
        final Multiset<Object> coll = HashMultiset.create();
        return multiset(valueType, coll);
    }

    /**
     * Gets an iterable wrapper for {@code SortedMultiset}.
     * 
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final SerIterable sortedMultiset(final Class<?> valueType) {
        Ordering natural = Ordering.natural();
        final SortedMultiset<Object> coll = TreeMultiset.create(natural);
        return multiset(valueType, coll);
    }

    private static SerIterable multiset(final Class<?> valueType, final Multiset<Object> coll) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multiset(coll, valueType);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key != null) {
                    throw new IllegalArgumentException("Unexpected key");
                }
                coll.add(value, count);
            }
            @Override
            public Object build() {
                return coll;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
        };
    }

    /**
     * Gets an iterator wrapper for {@code Multiset}.
     * 
     * @param multiset  the collection, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator multiset(final Multiset<?> multiset, final Class<?> valueType) {
        return new SerIterator() {
            private final Iterator it = multiset.entrySet().iterator();
            private Multiset.Entry current;

            @Override
            public String simpleTypeName() {
                return "Multiset";
            }
            @Override
            public int size() {
                return multiset.size();
            }
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public void next() {
                current = (Multiset.Entry) it.next();
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Object key() {
                return null;
            }
            @Override
            public int count() {
                return current.getCount();
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public Object value() {
                return current.getElement();
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code ListMultimap}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    public static final SerIterable listMultimap(final Class<?> keyType, final Class<?> valueType) {
        final ListMultimap<Object, Object> map = ArrayListMultimap.create();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multimap(map, keyType, valueType);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key == null) {
                    throw new IllegalArgumentException("Missing key");
                }
                if (count != 1) {
                    throw new IllegalArgumentException("Unexpected count");
                }
                map.put(key, value);
            }
            @Override
            public Object build() {
                return map;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
        };
    }

    /**
     * Gets an iterable wrapper for {@code SetMultimap}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    public static final SerIterable setMultimap(final Class<?> keyType, final Class<?> valueType) {
        final SetMultimap<Object, Object> map = HashMultimap.create();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multimap(map, keyType, valueType);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key == null) {
                    throw new IllegalArgumentException("Missing key");
                }
                if (count != 1) {
                    throw new IllegalArgumentException("Unexpected count");
                }
                map.put(key, value);
            }
            @Override
            public Object build() {
                return map;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
        };
    }

    /**
     * Gets an iterator wrapper for {@code Multimap}.
     * 
     * @param map  the collection, not null
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator multimap(final Multimap<?, ?> map, final Class<?> keyType, final Class<?> valueType) {
        return new SerIterator() {
            private final Iterator it = map.entries().iterator();
            private Map.Entry current;

            @Override
            public String simpleTypeName() {
                if (map instanceof SetMultimap) {
                    return "SetMultimap";
                }
                if (map instanceof ListMultimap) {
                    return "ListMultimap";
                }
                return "Multimap";
            }
            @Override
            public int size() {
                return map.size();
            }
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public void next() {
                current = (Map.Entry) it.next();
            }
            @Override
            public Class<?> keyType() {
                return keyType;
            }
            @Override
            public Object key() {
                return current.getKey();
            }
            @Override
            public int count() {
                return 1;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public Object value() {
                return current.getValue();
            }
        };
    }

}
