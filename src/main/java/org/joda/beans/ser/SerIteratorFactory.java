/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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
    /**
     * An empty list of classes.
     */
    public static final List<Class<?>> EMPTY_VALUE_TYPES = Collections.emptyList();

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for an arbitrary value.
     * 
     * @param value  the possible collection-like object, not null
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(final Object value) {
        return create(value, EMPTY_VALUE_TYPES);
    }

    /**
     * Creates an iterator wrapper for an arbitrary value.
     * 
     * @param value  the possible collection-like object, not null
     * @param types  the generic type parameters to use, empty if unknown
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(final Object value, final List<Class<?>> types) {
        if (value instanceof Collection) {
            if (types.size() == 1) {
                return collection((Collection<?>) value, types.get(0), EMPTY_VALUE_TYPES);
            }
            return collection((Collection<?>) value, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value instanceof Map) {
            if (types.size() == 2) {
                return map((Map<?, ?>) value, types.get(0), types.get(1), EMPTY_VALUE_TYPES);
            }
            return map((Map<?, ?>) value, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive() == false) {
            Object[] array = (Object[]) value;
            return array(array, array.getClass().getComponentType());
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
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return collection((Collection<?>) value, valueType, valueTypeTypes);
        }
        if (value instanceof Map) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return map((Map<?, ?>) value, keyType, valueType, valueTypeTypes);
        }
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive() == false) {
            Object[] array = (Object[]) value;
            return array(array, array.getClass().getComponentType());
        }
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for a meta-type description.
     * 
     * @param metaTypeDescription  the description of the collection type, not null
     * @param settings  the settings object, not null
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the iterable, null if not a collection-like type
     */
    public SerIterable createIterable(final String metaTypeDescription, final JodaBeanSer settings, final Map<String, Class<?>> knownTypes) {
        if (metaTypeDescription.equals("Set")) {
            return set(Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("List")) {
            return list(Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Collection")) {
            return list(Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Map")) {
            return map(Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Object[]")) {
            return array(Object.class);
        }
        if (metaTypeDescription.endsWith("[]")) {
            String clsStr = metaTypeDescription.substring(0, metaTypeDescription.length() - 2);
            try {
                Class<?> cls = SerTypeMapper.decodeType(clsStr, settings, null, knownTypes);
                return array(cls);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        return null;
    }

    /**
     * Creates an iterator wrapper for a child where there are second level generic parameters.
     * 
     * @param iterable  the parent iterable, not null
     * @return the iterable, null if not a collection-like type
     */
    public SerIterable createIterable(SerIterable iterable) {
        List<Class<?>> valueTypeTypes = iterable.valueTypeTypes();
        if (valueTypeTypes.size() > 0) {
            Class<?> valueType = iterable.valueType();
            if (NavigableSet.class.isAssignableFrom(valueType)) {
                return navigableSet(valueTypeTypes.get(0), EMPTY_VALUE_TYPES);
            }
            if (SortedSet.class.isAssignableFrom(valueType)) {
                return sortedSet(valueTypeTypes.get(0), EMPTY_VALUE_TYPES);
            }
            if (Set.class.isAssignableFrom(valueType)) {
                return set(valueTypeTypes.get(0), EMPTY_VALUE_TYPES);
            }
            if (Collection.class.isAssignableFrom(valueType)) {  // includes List
                return list(valueTypeTypes.get(0), EMPTY_VALUE_TYPES);
            }
            if (NavigableMap.class.isAssignableFrom(valueType)) {
                if (valueTypeTypes.size() == 2) {
                    return navigableMap(valueTypeTypes.get(0), valueTypeTypes.get(1), EMPTY_VALUE_TYPES);
                }
                return navigableMap(Object.class, Object.class, EMPTY_VALUE_TYPES);
            }
            if (SortedMap.class.isAssignableFrom(valueType)) {
                if (valueTypeTypes.size() == 2) {
                    return sortedMap(valueTypeTypes.get(0), valueTypeTypes.get(1), EMPTY_VALUE_TYPES);
                }
                return sortedMap(Object.class, Object.class, EMPTY_VALUE_TYPES);
            }
            if (Map.class.isAssignableFrom(valueType)) {
                if (valueTypeTypes.size() == 2) {
                    return map(valueTypeTypes.get(0), valueTypeTypes.get(1), EMPTY_VALUE_TYPES);
                }
                return map(Object.class, Object.class, EMPTY_VALUE_TYPES);
            }
            if (valueType.isArray() && valueType.getComponentType().isPrimitive() == false) {
                return array(valueType.getComponentType());
            }
        }
        return null;
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterable, null if not a collection-like type
     */
    public SerIterable createIterable(final MetaProperty<?> prop, final Class<?> beanClass) {
        if (NavigableSet.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return navigableSet(valueType, valueTypeTypes);
        }
        if (SortedSet.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return sortedSet(valueType, valueTypeTypes);
        }
        if (Set.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return set(valueType, valueTypeTypes);
        }
        if (Collection.class.isAssignableFrom(prop.propertyType())) {  // includes List
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return list(valueType, valueTypeTypes);
        }
        if (NavigableMap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return navigableMap(keyType, valueType, valueTypeTypes);
        }
        if (SortedMap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return sortedMap(keyType, valueType, valueTypeTypes);
        }
        if (Map.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return map(keyType, valueType, valueTypeTypes);
        }
        if (prop.propertyType().isArray() && prop.propertyType().getComponentType().isPrimitive() == false) {
            return array(prop.propertyType().getComponentType());
        }
        return null;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code List}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable list(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final List<Object> coll = new ArrayList<Object>();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return collection(coll, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key != null) {
                    throw new IllegalArgumentException("Unexpected key");
                }
                for (int i = 0; i < count; i++) {
                    coll.add(value);
                }
            }
            @Override
            public Object build() {
                return coll;
            }
            @Override
            public boolean isMapLike() {
                return false;
            }
            @Override
            public boolean isCounted() {
                return false;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
        };
    }

    /**
     * Gets an iterable wrapper for {@code Set}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable set(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Set<Object> coll = new HashSet<Object>();
        return set(valueType, valueTypeTypes, coll);
    }

    /**
     * Gets an iterable wrapper for {@code SortedSet}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable sortedSet(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final SortedSet<Object> coll = new TreeSet<Object>();
        return set(valueType, valueTypeTypes, coll);
    }

    /**
     * Gets an iterable wrapper for {@code NavigableSet}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable navigableSet(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final NavigableSet<Object> coll = new TreeSet<Object>();
        return set(valueType, valueTypeTypes, coll);
    }

    private static SerIterable set(final Class<?> valueType, final List<Class<?>> valueTypeTypes, final Set<Object> coll) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return collection(coll, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key != null) {
                    throw new IllegalArgumentException("Unexpected key");
                }
                for (int i = 0; i < count; i++) {
                    coll.add(value);
                }
            }
            @Override
            public Object build() {
                return coll;
            }
            @Override
            public boolean isMapLike() {
                return false;
            }
            @Override
            public boolean isCounted() {
                return false;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
        };
    }

    /**
     * Gets an iterator wrapper for {@code Collection}.
     * 
     * @param coll  the collection, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator collection(final Collection<?> coll, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = coll.iterator();
            private Object current;

            @Override
            public String metaTypeName() {
                if (coll instanceof Set) {
                    return "Set";
                }
                if (coll instanceof List) {
                    return "List";
                }
                return "Collection";
            }
            @Override
            public boolean isMapLike() {
                return false;
            }
            @Override
            public boolean isCounted() {
                return false;
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
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
            @Override
            public Object value() {
                return current;
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code Map}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable map(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Map<Object, Object> map = new HashMap<Object, Object>();
        return map(keyType, valueType, valueTypeTypes, map);
    }

    /**
     * Gets an iterable wrapper for {@code SortedMap}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable sortedMap(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final SortedMap<Object, Object> map = new TreeMap<Object, Object>();
        return map(keyType, valueType, valueTypeTypes, map);
    }

    /**
     * Gets an iterable wrapper for {@code NavigableMap}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable navigableMap(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final NavigableMap<Object, Object> map = new TreeMap<Object, Object>();
        return map(keyType, valueType, valueTypeTypes, map);
    }

    private static SerIterable map(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes, final Map<Object, Object> map) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return map(map, keyType, valueType, valueTypeTypes);
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
            public boolean isMapLike() {
                return true;
            }
            @Override
            public boolean isCounted() {
                return false;
            }
            @Override
            public Class<?> keyType() {
                return keyType;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
        };
    }

    /**
     * Gets an iterator wrapper for {@code Map}.
     * 
     * @param map  the collection, not null
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator map(final Map<?, ?> map, final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = map.entrySet().iterator();
            private Entry current;

            @Override
            public String metaTypeName() {
                return "Map";
            }
            @Override
            public boolean isMapLike() {
                return true;
            }
            @Override
            public boolean isCounted() {
                return false;
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
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
            @Override
            public Object value() {
                return current.getValue();
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for an array.
     * 
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    public static final SerIterable array(final Class<?> valueType) {
        final List<Object> list = new ArrayList<Object>();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return array(build(), valueType);
            }
            @Override
            public void add(Object key, Object value, int count) {
                if (key != null) {
                    throw new IllegalArgumentException("Unexpected key");
                }
                if (count != 1) {
                    throw new IllegalArgumentException("Unexpected count");
                }
                for (int i = 0; i < count; i++) {
                    list.add(value);
                }
            }
            @Override
            public Object[] build() {
                Object[] array = (Object[]) Array.newInstance(valueType, list.size());
                return list.toArray(array);
            }
            @Override
            public boolean isMapLike() {
                return false;
            }
            @Override
            public boolean isCounted() {
                return false;
            }
            @Override
            public Class<?> keyType() {
                return null;
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public List<Class<?>> valueTypeTypes() {
                return EMPTY_VALUE_TYPES;
            }
        };
    }

    /**
     * Gets an iterator wrapper for an array.
     * 
     * @param array  the array, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    public static final SerIterator array(final Object[] array, final Class<?> valueType) {
        return new SerIterator() {
            private int index = -1;

            @Override
            public String metaTypeName() {
                if (valueType == Object.class) {
                    return "Object[]";
                }
                if (valueType == String.class) {
                    return "String[]";
                }
                return valueType.getName() + "[]";
            }
            @Override
            public boolean isMapLike() {
                return false;
            }
            @Override
            public boolean isCounted() {
                return false;
            }
            @Override
            public int size() {
                return array.length;
            }
            @Override
            public boolean hasNext() {
                return (index + 1) < array.length;
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
            public List<Class<?>> valueTypeTypes() {
                return Collections.emptyList();
            }
            @Override
            public Object value() {
                return array[index];
            }
        };
    }

}
