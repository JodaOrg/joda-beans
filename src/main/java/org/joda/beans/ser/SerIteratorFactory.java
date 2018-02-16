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
            Class.forName("org.joda.collect.grid.Grid");
            return new CollectSerIteratorFactory();
        } catch (Exception | LinkageError ex) {
            try {
                Class.forName("com.google.common.collect.Multimap");
                return new GuavaSerIteratorFactory();
            } catch (Exception | LinkageError ex2) {
                return new SerIteratorFactory();
            }
        }
    }
    /**
     * An empty list of classes.
     */
    public static final List<Class<?>> EMPTY_VALUE_TYPES = Collections.emptyList();
    /**
     * Map of array types.
     */
    private static final Map<String, Class<?>> META_TYPE_MAP = new HashMap<>();
    static {
        META_TYPE_MAP.put("Object[]", Object.class);
        META_TYPE_MAP.put("String[]", String.class);
        META_TYPE_MAP.put("boolean[]", boolean.class);
        META_TYPE_MAP.put("char[]", char.class);
        META_TYPE_MAP.put("byte[]", byte.class);
        META_TYPE_MAP.put("short[]", short.class);
        META_TYPE_MAP.put("int[]", int.class);
        META_TYPE_MAP.put("long[]", long.class);
        META_TYPE_MAP.put("float[]", float.class);
        META_TYPE_MAP.put("double[]", double.class);
        META_TYPE_MAP.put("Object[][]", Object[].class);
        META_TYPE_MAP.put("String[][]", String[].class);
        META_TYPE_MAP.put("boolean[][]", boolean[].class);
        META_TYPE_MAP.put("char[][]", char[].class);
        META_TYPE_MAP.put("byte[][]", byte[].class);
        META_TYPE_MAP.put("short[][]", short[].class);
        META_TYPE_MAP.put("int[][]", int[].class);
        META_TYPE_MAP.put("long[][]", long[].class);
        META_TYPE_MAP.put("float[][]", float[].class);
        META_TYPE_MAP.put("double[][]", double[].class);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param value  the possible collection-like object, not null
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @param allowPrimitiveArrays  whether to allow primitive arrays
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(Object value, MetaProperty<?> prop, Class<?> beanClass, boolean allowPrimitiveArrays) {
        if (allowPrimitiveArrays &&
                value.getClass().isArray() &&
                value.getClass().getComponentType().isPrimitive() &&
                value.getClass().getComponentType() != byte.class) {
            return arrayPrimitive(value, prop.propertyType(), value.getClass().getComponentType());
        }
        return create(value, prop, beanClass);
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param value  the possible collection-like object, not null
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator create(Object value, MetaProperty<?> prop, Class<?> beanClass) {
        Class<?> declaredType = prop.propertyType();
        if (value instanceof Collection) {
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.collectionType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return collection((Collection<?>) value, declaredType, valueType, valueTypeTypes);
        }
        if (value instanceof Map) {
            Class<?> keyType = defaultToObjectClass(JodaBeanUtils.mapKeyType(prop, beanClass));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.mapValueType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return map((Map<?, ?>) value, declaredType, keyType, valueType, valueTypeTypes);
        }
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive() == false) {
            Object[] array = (Object[]) value;
            return array(array, declaredType, array.getClass().getComponentType());
        }
        return null;
    }

    /**
     * Creates an iterator wrapper for a value retrieved from a parent iterator.
     * <p>
     * Allows the parent iterator to define the child iterator using generic type information.
     * This handles cases such as a {@code List} as the value in a {@code Map}.
     * 
     * @param value  the possible collection-like object, not null
     * @param parent  the parent iterator, not null
     * @return the iterator, null if not a collection-like type
     */
    public SerIterator createChild(Object value, SerIterator parent) {
        Class<?> declaredType = parent.valueType();
        List<Class<?>> childGenericTypes = parent.valueTypeTypes();
        if (value instanceof Collection) {
            if (childGenericTypes.size() == 1) {
                return collection((Collection<?>) value, declaredType, childGenericTypes.get(0), EMPTY_VALUE_TYPES);
            }
            return collection((Collection<?>) value, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value instanceof Map) {
            if (childGenericTypes.size() == 2) {
                return map((Map<?, ?>) value, declaredType, childGenericTypes.get(0), childGenericTypes.get(1), EMPTY_VALUE_TYPES);
            }
            return map((Map<?, ?>) value, Object.class, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value.getClass().isArray() && value.getClass().getComponentType().isPrimitive() == false) {
            Object[] array = (Object[]) value;
            return array(array, Object.class, value.getClass().getComponentType());
        }
        return null;
    }

    /**
     * Defaults input class to Object class.
     * 
     * @param type  the type, may be null
     * @return the type, not null
     */
    protected Class<?> defaultToObjectClass(Class<?> type) {
        return (type != null ? type : Object.class);
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
    public SerIterable createIterable(String metaTypeDescription, JodaBeanSer settings, Map<String, Class<?>> knownTypes) {
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
        if (metaTypeDescription.endsWith("[][][]")) {
            throw new IllegalArgumentException("Three-dimensional arrays cannot be parsed");
        }
        if (metaTypeDescription.endsWith("[][]")) {
            Class<?> type = META_TYPE_MAP.get(metaTypeDescription);
            if (type != null) {
                return array(type);
            }
            String clsStr = metaTypeDescription.substring(0, metaTypeDescription.length() - 4);
            try {
                Class<?> cls = SerTypeMapper.decodeType(clsStr, settings, null, knownTypes);
                String compound = "[L" + cls.getName() + ";";
                return array(Class.forName(compound));  // needs to be Class.forName
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        }
        if (metaTypeDescription.endsWith("[]")) {
            Class<?> type = META_TYPE_MAP.get(metaTypeDescription);
            if (type == null) {
                String clsStr = metaTypeDescription.substring(0, metaTypeDescription.length() - 2);
                try {
                    type = SerTypeMapper.decodeType(clsStr, settings, null, knownTypes);
                } catch (ClassNotFoundException ex) {
                    throw new RuntimeException(ex);
                }
            }
            return type.isPrimitive() ? arrayPrimitive(type) : array(type);
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
            if (valueType.isArray()) {
                if (valueType.getComponentType().isPrimitive()) {
                    return arrayPrimitive(valueType.getComponentType());
                } else {
                    return array(valueType.getComponentType());
                }
            }
        }
        return null;
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @param allowPrimitiveArrays  whether to allow primitive arrays
     * @return the iterable, null if not a collection-like type
     */
    public SerIterable createIterable(MetaProperty<?> prop, Class<?> beanClass, boolean allowPrimitiveArrays) {
        if (allowPrimitiveArrays &&
                prop.propertyType().isArray() &&
                prop.propertyType().getComponentType().isPrimitive() &&
                prop.propertyType().getComponentType() != byte.class) {
            return arrayPrimitive(prop.propertyType().getComponentType());
        }
        return createIterable(prop, beanClass);
    }

    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param prop  the meta-property defining the value, not null
     * @param beanClass  the class of the bean, not the meta-property, for better generics, not null
     * @return the iterable, null if not a collection-like type
     */
    public SerIterable createIterable(MetaProperty<?> prop, Class<?> beanClass) {
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
    public static final SerIterable list(
            final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final List<Object> coll = new ArrayList<>();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return collection(coll, Object.class, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
        final Set<Object> coll = new HashSet<>();
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
        final SortedSet<Object> coll = new TreeSet<>();
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
        final NavigableSet<Object> coll = new TreeSet<>();
        return set(valueType, valueTypeTypes, coll);
    }

    private static SerIterable set(
            final Class<?> valueType, final List<Class<?>> valueTypeTypes, final Set<Object> coll) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return collection(coll, Object.class, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
     * @param declaredType  the declared type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator collection(
            final Collection<?> coll, final Class<?> declaredType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
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
            public boolean metaTypeRequired() {
                if (coll instanceof Set) {
                    return Set.class.isAssignableFrom(declaredType) == false;
                }
                if (coll instanceof List) {
                    return List.class.isAssignableFrom(declaredType) == false;
                }
                return Collection.class.isAssignableFrom(declaredType) == false;
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
        final Map<Object, Object> map = new HashMap<>();
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
        final SortedMap<Object, Object> map = new TreeMap<>();
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
        final NavigableMap<Object, Object> map = new TreeMap<>();
        return map(keyType, valueType, valueTypeTypes, map);
    }

    static SerIterable map(
            final Class<?> keyType, final Class<?> valueType,
            final List<Class<?>> valueTypeTypes, final Map<Object, Object> map) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return map(map, Object.class, keyType, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
            public SerCategory category() {
                return SerCategory.MAP;
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
     * @param declaredType  the declared type, not null
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator map(
            final Map<?, ?> map, final Class<?> declaredType,
            final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = map.entrySet().iterator();
            private Entry current;

            @Override
            public String metaTypeName() {
                return "Map";
            }
            @Override
            public boolean metaTypeRequired() {
                return Map.class.isAssignableFrom(declaredType) == false;
            }
            @Override
            public SerCategory category() {
                return SerCategory.MAP;
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
     * Gets an iterable wrapper for an object array.
     * 
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    public static final SerIterable array(final Class<?> valueType) {
        final List<Object> list = new ArrayList<>();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return array(build(), Object.class, valueType);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
     * Gets an iterable wrapper for a primitive array.
     * 
     * @param valueType  the value type, not null
     * @return the iterable, not null
     */
    static final SerIterable arrayPrimitive(final Class<?> valueType) {
        final List<Object> list = new ArrayList<>();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return arrayPrimitive(build(), Object.class, valueType);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
            public Object build() {
                Object array = Array.newInstance(valueType, list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }
                return array;
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
     * Gets an iterator wrapper for an object array.
     * 
     * @param array  the array, not null
     * @param declaredType  the declared type, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    public static final SerIterator array(
            final Object[] array, final Class<?> declaredType, final Class<?> valueType) {
        return new SerIterator() {
            private int index = -1;

            @Override
            public String metaTypeName() {
                return metaTypeNameBase(valueType);
            }
            private String metaTypeNameBase(Class<?> arrayType) {
                if (arrayType.isArray()) {
                    return metaTypeNameBase(arrayType.getComponentType()) + "[]";
                }
                if (arrayType == Object.class) {
                    return "Object[]";
                }
                if (arrayType == String.class) {
                    return "String[]";
                }
                return arrayType.getName() + "[]";
            }
            @Override
            public boolean metaTypeRequired() {
                if (valueType == Object.class) {
                    return Object[].class.isAssignableFrom(declaredType) == false;
                }
                if (valueType == String.class) {
                    return String[].class.isAssignableFrom(declaredType) == false;
                }
                return true;
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

    /**
     * Gets an iterator wrapper for a primitive array.
     * 
     * @param array  the array, not null
     * @param declaredType  the declared type, not null
     * @param valueType  the value type, not null
     * @return the iterator, not null
     */
    static final SerIterator arrayPrimitive(
            final Object array, final Class<?> declaredType, final Class<?> valueType) {
        final int arrayLength = Array.getLength(array);
        return new SerIterator() {
            private int index = -1;

            @Override
            public String metaTypeName() {
                return metaTypeNameBase(valueType);
            }
            private String metaTypeNameBase(Class<?> arrayType) {
                if (arrayType.isArray()) {
                    return metaTypeNameBase(arrayType.getComponentType()) + "[]";
                }
                if (arrayType == Object.class) {
                    return "Object[]";
                }
                if (arrayType == String.class) {
                    return "String[]";
                }
                return arrayType.getName() + "[]";
            }
            @Override
            public boolean metaTypeRequired() {
                if (valueType == Object.class) {
                    return Object[].class.isAssignableFrom(declaredType) == false;
                }
                if (valueType == String.class) {
                    return String[].class.isAssignableFrom(declaredType) == false;
                }
                return true;
            }
            @Override
            public int size() {
                return arrayLength;
            }
            @Override
            public boolean hasNext() {
                return (index + 1) < arrayLength;
            }
            @Override
            public void next() {
                index++;
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
                return Array.get(array, index);
            }
        };
    }

}
