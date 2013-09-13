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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

/**
 * A factory used to create wrappers around collection-like objects.
 *
 * @author Stephen Colebourne
 */
public class SerIteratorFactory {

    /**
     * Singleton instance.
     */
    public static final SerIteratorFactory INSTANCE = getInstance();
    private static SerIteratorFactory getInstance() {
        try {
            Class.forName("com.google.common.collect.Multimap");
            return new GuavaSerIteratorFactory();
        } catch (Throwable ex) {
            return new SerIteratorFactory();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for an arbitrary value.
     * 
     * @param value  the possible collection-like object, not null
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(final Object value) {
        if (value instanceof Collection) {
            return collection((Collection<?>) value, Object.class);
        }
        if (value instanceof Map) {
            return map((Map<?, ?>) value, Object.class, Object.class);
        }
        if (value instanceof Object[] && value.getClass().getComponentType().isPrimitive() == false) {
            return array((Object[]) value);
        }
        return null;
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param value  the possible collection-like object, not null
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(final Object value, final MetaProperty<?> prop, Class<?> beanClass) {
        if (value instanceof Collection) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            return collection((Collection<?>) value, valueType);
        }
        if (value instanceof Map) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            return map((Map<?, ?>) value, keyType, valueType);
        }
        if (value instanceof Object[] && value.getClass().getComponentType().isPrimitive() == false) {
            return array((Object[]) value);
        }
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterator wrapper for {@code Collection}.
     * 
     * @param coll  the collection, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator collection(final Collection<?> coll, final Class<?> valueType) {
        return new SerIterator() {
            private final Iterator it = coll.iterator();
            private Object current;

            @Override
            public String simpleType() {
                if (coll instanceof List) {
                    return "List";
                }
                if (coll instanceof Set) {
                    return "Set";
                }
                return "Collection";
            }
            @Override
            public int size() {
                return coll.size();
            }
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public void next() {
                current = it.next();
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
                return 1;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public Object value() {
                return current;
            }
        };
    }

    /**
     * Gets an iterator wrapper for {@code Map}.
     * 
     * @param map  the collection, not null
     * @param valueType  the value type, not null
     * @param keyType  the value type, not null
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator map(final Map<?, ?> map, final Class<?> keyType, final Class<?> valueType) {
        return new SerIterator() {
            private final Iterator it = map.entrySet().iterator();
            private Entry current;

            @Override
            public String simpleType() {
                return "Map";
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
                current = (Entry) it.next();
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

    /**
     * Gets an iterator wrapper for an array.
     * 
     * @param array  the array, not null
     * @return the iterator, not null
     */
    public static final SerIterator array(final Object[] array) {
        return new SerIterator() {
            private final Class<?> valueType = array.getClass().getComponentType();
            private int index = -1;

            @Override
            public String simpleType() {
                return "Array";
            }
            @Override
            public int size() {
                return array.length;
            }
            @Override
            public boolean hasNext() {
                return index < array.length;
            }
            @Override
            public void next() {
                index++;
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
                return 1;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public Object value() {
                return array[index];
            }
        };
    }

}
