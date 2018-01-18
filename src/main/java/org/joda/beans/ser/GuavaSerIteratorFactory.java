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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.collect.TreeMultiset;

/**
 * Guava factory used to create wrappers around collection-like objects.
 *
 * @author Stephen Colebourne
 */
public class GuavaSerIteratorFactory extends SerIteratorFactory {

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
        Class<?> declaredType = prop.propertyType();
        if (value instanceof BiMap) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return biMap((BiMap<?, ?>) value, declaredType, keyType, valueType, valueTypeTypes);
        }
        if (value instanceof Multiset) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return multiset((Multiset<?>) value, declaredType, valueType, valueTypeTypes);
        }
        if (value instanceof Multimap) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return multimap((Multimap<?, ?>) value, declaredType, keyType, valueType, valueTypeTypes);
        }
        if (value instanceof Table) {
            Class<?> rowType = JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 0);
            Class<?> colType = JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 1);
            Class<?> valueType = JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 2);
            return table((Table<?, ?, ?>) value, declaredType, rowType, colType, valueType, EMPTY_VALUE_TYPES);
        }
        return super.create(value, prop, beanClass);
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
    @Override
    public SerIterator createChild(final Object value, final SerIterator parent) {
        Class<?> declaredType = parent.valueType();
        List<Class<?>> childGenericTypes = parent.valueTypeTypes();
        if (value instanceof BiMap) {
            if (childGenericTypes.size() == 2) {
                return biMap((BiMap<?, ?>) value, declaredType, childGenericTypes.get(0), childGenericTypes.get(1), EMPTY_VALUE_TYPES);
            }
            return biMap((BiMap<?, ?>) value, Object.class, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value instanceof Multimap) {
            if (childGenericTypes.size() == 2) {
                return multimap((Multimap<?, ?>) value, declaredType, childGenericTypes.get(0), childGenericTypes.get(1), EMPTY_VALUE_TYPES);
            }
            return multimap((Multimap<?, ?>) value, Object.class, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value instanceof Multiset) {
            if (childGenericTypes.size() == 1) {
                return multiset((Multiset<?>) value, declaredType, childGenericTypes.get(0), EMPTY_VALUE_TYPES);
            }
            return multiset((Multiset<?>) value, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (value instanceof Table) {
            if (childGenericTypes.size() == 3) {
                return table((Table<?, ?, ?>) value, declaredType,
                        childGenericTypes.get(0), childGenericTypes.get(1),
                        childGenericTypes.get(2), EMPTY_VALUE_TYPES);
            }
            return table((Table<?, ?, ?>) value, Object.class, Object.class, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        return super.createChild(value, parent);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an iterator wrapper for a meta-property value.
     * 
     * @param metaTypeDescription  the description of the collection type, not null
     * @param settings  the settings object, not null
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the iterator, null if not a collection-like type
     */
    @Override
    public SerIterable createIterable(final String metaTypeDescription, final JodaBeanSer settings, final Map<String, Class<?>> knownTypes) {
        if (metaTypeDescription.equals("BiMap")) {
            return biMap(Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("SetMultimap")) {
            return setMultimap(Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("ListMultimap")) {
            return listMultimap(Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Multimap")) {
            return listMultimap(Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Multiset")) {
            return multiset(Object.class, EMPTY_VALUE_TYPES);
        }
        if (metaTypeDescription.equals("Table")) {
            return table(Object.class, Object.class, Object.class, EMPTY_VALUE_TYPES);
        }
        return super.createIterable(metaTypeDescription, settings, knownTypes);
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
        if (BiMap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = defaultToObjectClass(JodaBeanUtils.mapKeyType(prop, beanClass));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.mapValueType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return biMap(keyType, valueType, valueTypeTypes);
        }
        if (SortedMultiset.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.collectionType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return sortedMultiset(valueType, valueTypeTypes);
        }
        if (Multiset.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.collectionType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return multiset(valueType, valueTypeTypes);
        }
        if (SetMultimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = defaultToObjectClass(JodaBeanUtils.mapKeyType(prop, beanClass));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.mapValueType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return setMultimap(keyType, valueType, valueTypeTypes);
        }
        if (ListMultimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = defaultToObjectClass(JodaBeanUtils.mapKeyType(prop, beanClass));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.mapValueType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return listMultimap(keyType, valueType, valueTypeTypes);
        }
        if (Multimap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = defaultToObjectClass(JodaBeanUtils.mapKeyType(prop, beanClass));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.mapValueType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return listMultimap(keyType, valueType, valueTypeTypes);
        }
        if (Table.class.isAssignableFrom(prop.propertyType())) {
            Class<?> rowType = defaultToObjectClass(JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 0));
            Class<?> colType = defaultToObjectClass(JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 1));
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.extractTypeClass(prop, beanClass, 3, 2));
            return table(rowType, colType, valueType, EMPTY_VALUE_TYPES);
        }
        if (ImmutableList.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return immutableList(valueType, valueTypeTypes);
        }
        if (ImmutableSortedSet.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return immutableSortedSet(valueType, valueTypeTypes);
        }
        if (ImmutableSet.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return immutableSet(valueType, valueTypeTypes);
        }
        if (ImmutableSortedMap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return immutableSortedMap(keyType, valueType, valueTypeTypes);
        }
        if (ImmutableMap.class.isAssignableFrom(prop.propertyType())) {
            Class<?> keyType = JodaBeanUtils.mapKeyType(prop, beanClass);
            Class<?> valueType = JodaBeanUtils.mapValueType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.mapValueTypeTypes(prop, beanClass);
            return immutableMap(keyType, valueType, valueTypeTypes);
        }
        return super.createIterable(prop, beanClass);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code BiMap}.
     * 
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable biMap(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final BiMap<Object, Object> map = HashBiMap.create();
        return map(keyType, valueType, valueTypeTypes, map);
    }

    /**
     * Gets an iterable wrapper for {@code Multiset}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable multiset(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Multiset<Object> coll = HashMultiset.create();
        return multiset(valueType, valueTypeTypes, coll);
    }

    /**
     * Gets an iterable wrapper for {@code SortedMultiset}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static final SerIterable sortedMultiset(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        Ordering natural = Ordering.natural();
        final SortedMultiset<Object> coll = TreeMultiset.create(natural);
        return multiset(valueType, valueTypeTypes, coll);
    }

    private static SerIterable multiset(final Class<?> valueType, final List<Class<?>> valueTypeTypes, final Multiset<Object> coll) {
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multiset(coll, Object.class, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
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
            public SerCategory category() {
                return SerCategory.COUNTED;
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
     * Gets an iterator wrapper for {@code Multiset}.
     * 
     * @param multiset  the collection, not null
     * @param declaredType  the declared type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator multiset(
            final Multiset<?> multiset, final Class<?> declaredType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = multiset.entrySet().iterator();
            private Multiset.Entry current;

            @Override
            public String metaTypeName() {
                return "Multiset";
            }
            @Override
            public boolean metaTypeRequired() {
                return Multiset.class.isAssignableFrom(declaredType) == false;
            }
            @Override
            public SerCategory category() {
                return SerCategory.COUNTED;
            }
            @Override
            public int size() {
                return multiset.entrySet().size();
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
            public int count() {
                return current.getCount();
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
                return current.getElement();
            }
        };
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code ListMultimap}.
     * 
     * @param keyType  the key type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable listMultimap(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final ListMultimap<Object, Object> map = ArrayListMultimap.create();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multimap(map, Object.class, keyType, valueType, valueTypeTypes);
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
     * Gets an iterable wrapper for {@code SetMultimap}.
     * 
     * @param keyType  the key type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable setMultimap(final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final SetMultimap<Object, Object> map = HashMultimap.create();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return multimap(map, Object.class, keyType, valueType, valueTypeTypes);
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
     * Gets an iterator wrapper for {@code Multimap}.
     * 
     * @param map  the collection, not null
     * @param declaredType  the declared type, not null
     * @param keyType  the key type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator multimap(
            final Multimap<?, ?> map, final Class<?> declaredType,
            final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = map.entries().iterator();
            private Map.Entry current;

            @Override
            public String metaTypeName() {
                if (map instanceof SetMultimap) {
                    return "SetMultimap";
                }
                if (map instanceof ListMultimap) {
                    return "ListMultimap";
                }
                return "Multimap";
            }
            @Override
            public boolean metaTypeRequired() {
                if (map instanceof SetMultimap) {
                    return SetMultimap.class.isAssignableFrom(declaredType) == false;
                }
                if (map instanceof ListMultimap) {
                    return ListMultimap.class.isAssignableFrom(declaredType) == false;
                }
                return Multimap.class.isAssignableFrom(declaredType) == false;
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
     * Gets an iterable wrapper for {@code Table}.
     * 
     * @param rowType  the row type, not null
     * @param colType  the column type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable table(final Class<?> rowType, final Class<?> colType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Table<Object, Object, Object> table = HashBasedTable.create();
        return new SerIterable() {
            @Override
            public SerIterator iterator() {
                return table(table, Object.class, rowType, colType, valueType, valueTypeTypes);
            }
            @Override
            public void add(Object row, Object column, Object value, int count) {
                if (row == null) {
                    throw new IllegalArgumentException("Missing row");
                }
                if (column == null) {
                    throw new IllegalArgumentException("Missing column");
                }
                if (count != 1) {
                    throw new IllegalArgumentException("Unexpected count");
                }
                table.put(row, column, value);
            }
            @Override
            public Object build() {
                return table;
            }
            @Override
            public SerCategory category() {
                return SerCategory.TABLE;
            }
            @Override
            public Class<?> keyType() {
                return rowType;
            }
            @Override
            public Class<?> columnType() {
                return colType;
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
     * Gets an iterator wrapper for {@code Table}.
     * 
     * @param table  the collection, not null
     * @param declaredType  the declared type, not null
     * @param rowType  the row type, not null
     * @param colType  the col type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator table(
            final Table<?, ?, ?> table, final Class<?> declaredType,
            final Class<?> rowType, final Class<?> colType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = table.cellSet().iterator();
            private Cell current;

            @Override
            public String metaTypeName() {
                return "Table";
            }
            @Override
            public boolean metaTypeRequired() {
                return Table.class.isAssignableFrom(declaredType) == false;
            }
            @Override
            public SerCategory category() {
                return SerCategory.TABLE;
            }
            @Override
            public int size() {
                return table.size();
            }
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public void next() {
                current = (Cell) it.next();
            }
            @Override
            public Class<?> keyType() {
                return rowType;
            }
            @Override
            public Object key() {
                return current.getRowKey();
            }
            @Override
            public Class<?> columnType() {
                return colType;
            }
            @Override
            public Object column() {
                return current.getColumnKey();
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

    /**
     * Gets an iterator wrapper for {@code BiMap}.
     * 
     * @param map  the collection, not null
     * @param declaredType  the declared type, not null
     * @param keyType  the value type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator biMap(
            final BiMap<?, ?> map, final Class<?> declaredType,
            final Class<?> keyType, final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = map.entrySet().iterator();
            private Entry current;

            @Override
            public String metaTypeName() {
                return "BiMap";
            }
            @Override
            public boolean metaTypeRequired() {
                // hack around Guava annoyance by assuming that size 0 and 1 ImmutableBiMap
                // was actually meant to be an ImmutableMap
                if ((declaredType == Map.class || declaredType == ImmutableMap.class) && map.size() < 2) {
                    return false;
                }
                return BiMap.class.isAssignableFrom(declaredType) == false;
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

    /**
     * Gets an iterable wrapper for {@code ImmutableList}.
     *
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable immutableList(
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
                return ImmutableList.copyOf(coll);
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
     * Gets an iterable wrapper for {@code ImmutableSortedSet}.
     *
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable immutableSortedSet(
            final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Set<Object> coll = new LinkedHashSet<>();
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
                return ImmutableSortedSet.copyOf(coll);
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
     * Gets an iterable wrapper for {@code ImmutableSet}.
     *
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable immutableSet(
            final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        final Set<Object> coll = new LinkedHashSet<>();
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
                return ImmutableSet.copyOf(coll);
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

    static SerIterable immutableMap(
            final Class<?> keyType, final Class<?> valueType,
            final List<Class<?>> valueTypeTypes) {
        final Map<Object, Object> map = new LinkedHashMap<>();
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
                return ImmutableMap.copyOf(map);
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

    static SerIterable immutableSortedMap(
            final Class<?> keyType, final Class<?> valueType,
            final List<Class<?>> valueTypeTypes) {
        final Map<Object, Object> map = new LinkedHashMap<>();
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
                return ImmutableSortedMap.copyOf(map);
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

}
