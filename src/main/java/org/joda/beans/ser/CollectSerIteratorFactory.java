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
import java.util.List;
import java.util.Map;

import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableCell;
import org.joda.collect.grid.ImmutableGrid;

/**
 * Factory used to create wrappers around collection-like objects.
 *
 * @author Stephen Colebourne
 */
public class CollectSerIteratorFactory extends GuavaSerIteratorFactory {

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
        if (value instanceof Grid) {
            Class<?> valueType = defaultToObjectClass(JodaBeanUtils.collectionType(prop, beanClass));
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return grid((Grid<?>) value, declaredType, valueType, valueTypeTypes);
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
        if (value instanceof Grid) {
            if (childGenericTypes.size() == 1) {
                return grid((Grid<?>) value, declaredType, childGenericTypes.get(0), EMPTY_VALUE_TYPES);
            }
            return grid((Grid<?>) value, Object.class, Object.class, EMPTY_VALUE_TYPES);
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
        if (metaTypeDescription.equals("Grid")) {
            return grid(Object.class, EMPTY_VALUE_TYPES);
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
        if (Grid.class.isAssignableFrom(prop.propertyType())) {
            Class<?> valueType = JodaBeanUtils.collectionType(prop, beanClass);
            List<Class<?>> valueTypeTypes = JodaBeanUtils.collectionTypeTypes(prop, beanClass);
            return grid(valueType, valueTypeTypes);
        }
        return super.createIterable(prop, beanClass);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets an iterable wrapper for {@code Grid}.
     * 
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterable, not null
     */
    public static final SerIterable grid(final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterable() {
            private final List<Grid.Cell<?>> cells = new ArrayList<>();
            private int[] dimensions;
            @Override
            public SerIterator iterator() {
                return grid(build(), Object.class, valueType, valueTypeTypes);
            }
            @Override
            public void dimensions(int[] dimensions) {
                this.dimensions = dimensions;
            }
            @Override
            public void add(Object key, Object column, Object value, int count) {
                if (value != null) {
                    cells.add(ImmutableCell.of((Integer) key, (Integer) column, value));
                }
            }
            @Override
            @SuppressWarnings({ "unchecked", "rawtypes" })
            public Grid<?> build() {
                if (dimensions == null || dimensions.length != 2) {
                    throw new IllegalArgumentException("Expected 2 dimensions, rowCount and columnCount");
                }
                return ImmutableGrid.copyOf(dimensions[0], dimensions[1], (Iterable) cells);
            }
            @Override
            public SerCategory category() {
                return SerCategory.GRID;
            }
            @Override
            public Class<?> keyType() {
                return Integer.class;
            }
            @Override
            public Class<?> columnType() {
                return Integer.class;
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
     * Gets an iterator wrapper for {@code Grid}.
     * 
     * @param grid  the collection, not null
     * @param declaredType  the declared type, not null
     * @param valueType  the value type, not null
     * @param valueTypeTypes  the generic parameters of the value type
     * @return the iterator, not null
     */
    @SuppressWarnings("rawtypes")
    public static final SerIterator grid(
            final Grid<?> grid, final Class<?> declaredType,
            final Class<?> valueType, final List<Class<?>> valueTypeTypes) {
        return new SerIterator() {
            private final Iterator it = grid.cells().iterator();
            private Grid.Cell current;

            @Override
            public String metaTypeName() {
                return "Grid";
            }
            @Override
            public boolean metaTypeRequired() {
                return Grid.class.isAssignableFrom(declaredType) == false;
            }
            @Override
            public SerCategory category() {
                return SerCategory.GRID;
            }
            @Override
            public int dimensionSize(int dimension) {
                return (dimension == 0 ? grid.rowCount() : grid.columnCount());
            }
            @Override
            public int size() {
                return grid.size();
            }
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }
            @Override
            public void next() {
                current = (Grid.Cell) it.next();
            }
            @Override
            public Class<?> keyType() {
                return Integer.class;
            }
            @Override
            public Object key() {
                return current.getRow();
            }
            @Override
            public Class<?> columnType() {
                return Integer.class;
            }
            @Override
            public Object column() {
                return current.getColumn();
            }
            @Override
            public Class<?> valueType() {
                return valueType;
            }
            @Override
            public Object value() {
                return current.getValue();
            }
            @Override
            public Object value(int row, int column) {
                return grid.get(row, column);
            }
            @Override
            public List<Class<?>> valueTypeTypes() {
                return valueTypeTypes;
            }
        };
    }

}
