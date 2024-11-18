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
package org.joda.beans.ser.json;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.joda.beans.Bean;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.collect.grid.Grid;
import org.joda.convert.ToStringConverter;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

/**
 * Walks a Joda-Bean to write in simple JSON format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
class JodaBeanSimpleJsonWalker {

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<JsonHandler<Object>> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected JsonHandler computeValue(Class<?> type) {
            if (Bean.class.isAssignableFrom(type)) {
                return (JsonHandler<Bean>) JodaBeanSimpleJsonWalker::writeBean;
            }
            if (type.isArray()) {
                var componentType = type.getComponentType();
                if (componentType.isPrimitive()) {
                    if (componentType != byte.class) {
                        return JodaBeanSimpleJsonWalker::writePrimitiveArray;
                    }
                } else {
                    return (JsonHandler<Object[]>) JodaBeanSimpleJsonWalker::writeArray;
                }
            }
            if (type == String.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeString((String) value);
            }
            if (type == Long.class || type == long.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeLong((Long) value);
            }
            if (type == Integer.class || type == int.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Integer) value);
            }
            if (type == Short.class || type == short.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Short) value);
            }
            if (type == Byte.class || type == byte.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Byte) value);
            }
            if (type == Double.class || type == double.class) {
                return (writer, declaredType, propName, value) -> writer.writeDouble((Double) value);
            }
            if (type == Float.class || type == float.class) {
                return (writer, declaredType, propName, value) -> writer.writeFloat((Float) value);
            }
            if (type == Boolean.class || type == boolean.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeBoolean((Boolean) value);
            }
            if (type == Optional.class) {
                return OptionalJsonHandler.INSTANCE;
            }
            return BaseJsonHandlers.INSTANCE.computeValue(type);
        }
    };

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The outputter.
     */
    private JsonOutput output;

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     */
    JodaBeanSimpleJsonWalker(JodaBeanSer settings) {
        this.settings = settings;
    }

    //-------------------------------------------------------------------------
    /**
     * Writes the bean to the {@code Appendable}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    void walk(Bean bean, Appendable output) throws IOException {
        try {
            this.output = new JsonOutput(output, settings.getIndent(), settings.getNewLine());
            walkObject(ResolvedType.OBJECT, "", bean);
            output.append(settings.getNewLine());
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    //-----------------------------------------------------------------------
    // walk an object, by determining the runtime type
    private void walkObject(ResolvedType declaredType, String propertyName, Object value) {
        if (value == null) {
            output.writeNull();
        } else {
            var handler = LOOKUP.get(value.getClass());
            handler.handle(this, declaredType, propertyName, value);
        }
    }

    //-------------------------------------------------------------------------
    private void writeBean(ResolvedType declaredType, String propertyName, Bean bean) {
        if (settings.getConverter().isConvertible(bean.getClass())) {
            writeJodaConvert(declaredType, propertyName, bean);
        } else {
            output.writeObjectStart();
            writeBeanProperties(declaredType, propertyName, bean);
            output.writeObjectEnd();
        }
    }

    private void writeBeanProperties(ResolvedType declaredType, String propertyName, Bean bean) {
        for (var metaProperty : bean.metaBean().metaPropertyIterable()) {
            // checking for style.isReadable() is redundant, as isSerializable()/isDerived() cover the same check
            if (metaProperty.style().isSerializable() ||
                    (metaProperty.style().isDerived() && settings.isIncludeDerived())) {
                var value = metaProperty.get(bean);
                if (value != null) {
                    var resolvedType = ResolvedType.from(metaProperty.propertyGenericType(), bean.getClass());
                    var handler = LOOKUP.get(value.getClass());
                    handler.handleProperty(this, resolvedType, metaProperty.name(), value);
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    private void writeArray(ResolvedType declaredType, String propertyName, Object[] array) {
        var componentType = declaredType.toComponentType();
        output.writeArrayStart();
        for (var item : array) {
            output.writeArrayItemStart();
            walkObject(componentType, "", item);
        }
        output.writeArrayEnd();
    }

    private void writePrimitiveArray(ResolvedType declaredType, String propertyName, Object array) {
        var componentType = declaredType.toComponentType();
        var handler = LOOKUP.get(componentType.getRawType());
        var arrayLength = Array.getLength(array);
        output.writeArrayStart();
        for (int i = 0; i < arrayLength; i++) {
            output.writeArrayItemStart();
            handler.handle(this, declaredType, propertyName, Array.get(array, i));
        }
        output.writeArrayEnd();
    }

    private <T> void writeCollection(ResolvedType declaredType, String propertyName, Collection<T> coll) {
        var itemType = declaredType.getArgumentOrDefault(0);
        output.writeArrayStart();
        for (var item : coll) {
            output.writeArrayItemStart();
            walkObject(itemType, "", item);
        }
        output.writeArrayEnd();
    }

    private <K, V> void writeMap(ResolvedType declaredType, String propertyName, Collection<Map.Entry<K, V>> mapEntries) {
        var keyType = declaredType.getArgumentOrDefault(0);
        var valueType = declaredType.getArgumentOrDefault(1);
        // converter based on the declared type if possible, else based on the runtime type
        var keyConverterOpt = settings.getConverter().converterFor(keyType.getRawType());
        ToStringConverter<Object> keyConverter = keyConverterOpt.isPresent() ?
                keyConverterOpt.get().withoutGenerics() :
                key -> settings.getConverter().convertToString(key);
        output.writeObjectStart();
        for (var entry : mapEntries) {
            var key = entry.getKey();
            if (key == null) {
                throw invalidNullMapKey(propertyName);
            }
            var str = keyConverter.convertToString(key);
            output.writeObjectKey(str);
            walkObject(valueType, "", entry.getValue());
        }
        output.writeObjectEnd();
    }

    private static IllegalArgumentException invalidNullMapKey(String propertyName) {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "', map key must not be null");
    }

    private void writeDouble(Double val) {
        if (Double.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeDouble(val);
        }
    }

    private void writeFloat(Float val) {
        if (Float.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeFloat(val);
        }
    }

    private void writeJodaConvert(ResolvedType declaredType, String propertyName, Object value) {
        var realType = value.getClass();
        try {
            var converted = settings.getConverter().convertToString(value);
            if (converted == null) {
                throw invalidNullString(propertyName, value);
            }
            output.writeString(converted);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException(
                    "Unable to write property '" + propertyName + "', type " + realType.getName() + " could not be converted to a String",
                    ex);
        }
    }

    private static IllegalArgumentException invalidNullString(String propertyName, Object value) {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "' because converter returned a null string: " + value);
    }

    //-------------------------------------------------------------------------
    private static interface JsonHandler<T> {
        public abstract void handle(JodaBeanSimpleJsonWalker walker, ResolvedType declaredType, String propertyName, T obj);

        public default void handleProperty(JodaBeanSimpleJsonWalker walker, ResolvedType declaredType, String propertyName, T obj) {
            walker.output.writeObjectKey(propertyName);
            handle(walker, declaredType, propertyName, obj);
        }
    }

    //-------------------------------------------------------------------------
    static final class OptionalJsonHandler implements JsonHandler<Optional<?>> {
        private static final OptionalJsonHandler INSTANCE = new OptionalJsonHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) {

            var valueType = declaredType.getArgumentOrDefault(0);
            walker.walkObject(valueType, "", opt.orElse(null));
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) {

            var value = opt.orElse(null);
            if (value != null) {
                var valueType = declaredType.getArgumentOrDefault(0);
                walker.output.writeObjectKey(propertyName);
                walker.walkObject(valueType, propertyName, value);
            }
        }
    }

    private static sealed class BaseJsonHandlers {

        private static final BaseJsonHandlers INSTANCE = getInstance();

        private static final BaseJsonHandlers getInstance() {
            try {
                return new CollectJsonHandlers();
            } catch (RuntimeException | LinkageError ex) {
                try {
                    return new GuavaJsonHandlers();
                } catch (RuntimeException | LinkageError ex2) {
                    return new BaseJsonHandlers();
                }
            }
        }

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Collection.class.isAssignableFrom(type)) {
                return (JsonHandler<Collection<?>>) JodaBeanSimpleJsonWalker::writeCollection;
            }
            if (Map.class.isAssignableFrom(type)) {
                return (writer, declaredType, propName, value) -> writer.writeMap(declaredType, propName, ((Map<?, ?>) value).entrySet());
            }
            return JodaBeanSimpleJsonWalker::writeJodaConvert;
        }
    }

    private static sealed class GuavaJsonHandlers extends BaseJsonHandlers {

        @Override
        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Multimap.class.isAssignableFrom(type)) {
                return (JsonHandler<Multimap<?, ?>>) GuavaJsonHandlers::writeMultimap;
            }
            if (Multiset.class.isAssignableFrom(type)) {
                return (JsonHandler<Multiset<?>>) GuavaJsonHandlers::writeMultiset;
            }
            if (Table.class.isAssignableFrom(type)) {
                return (JsonHandler<Table<?, ?, ?>>) GuavaJsonHandlers::writeTable;
            }
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return GuavaOptionalJsonHandler.INSTANCE;
            }
            return super.computeValue(type);
        }

        private static void writeMultimap(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Multimap<?, ?> mmap) {

            walker.writeMap(declaredType, propertyName, mmap.entries());
        }

        private static void writeMultiset(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Multiset<?> mset) {

            var valueType = declaredType.getArgumentOrDefault(0);
            walker.output.writeArrayStart();
            for (var entry : mset.entrySet()) {
                walker.output.writeArrayItemStart();
                walker.output.writeArrayStart();
                walker.output.writeArrayItemStart();
                walker.walkObject(valueType, "", entry.getElement());
                walker.output.writeArrayItemStart();
                walker.output.writeInt(entry.getCount());
                walker.output.writeArrayEnd();
            }
            walker.output.writeArrayEnd();
        }

        private static void writeTable(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Table<?, ?, ?> table) {

            var rowType = declaredType.getArgumentOrDefault(0);
            var columnType = declaredType.getArgumentOrDefault(1);
            var valueType = declaredType.getArgumentOrDefault(2);
            walker.output.writeArrayStart();
            for (var cell : table.cellSet()) {
                walker.output.writeArrayItemStart();
                walker.output.writeArrayStart();
                walker.output.writeArrayItemStart();
                walker.walkObject(rowType, "", cell.getRowKey());
                walker.output.writeArrayItemStart();
                walker.walkObject(columnType, "", cell.getColumnKey());
                walker.output.writeArrayItemStart();
                walker.walkObject(valueType, "", cell.getValue());
                walker.output.writeArrayEnd();
            }
            walker.output.writeArrayEnd();
        }
    }

    static final class GuavaOptionalJsonHandler implements JsonHandler<com.google.common.base.Optional<?>> {
        private static final GuavaOptionalJsonHandler INSTANCE = new GuavaOptionalJsonHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) {

            var valueType = declaredType.getArgumentOrDefault(0);
            walker.walkObject(valueType, "", opt.orNull());
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) {

            var value = opt.orNull();
            if (value != null) {
                var valueType = declaredType.getArgumentOrDefault(0);
                walker.output.writeObjectKey(propertyName);
                walker.walkObject(valueType, propertyName, value);
            }
        }
    }

    //-------------------------------------------------------------------------
    private static final class CollectJsonHandlers extends GuavaJsonHandlers {

        @Override
        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Grid.class.isAssignableFrom(type)) {
                return (JsonHandler<Grid<?>>) CollectJsonHandlers::writeGrid;
            }
            return super.computeValue(type);
        }

        private static void writeGrid(
                JodaBeanSimpleJsonWalker walker,
                ResolvedType declaredType,
                String propertyName,
                Grid<?> grid) {

            // write grid using sparse approach
            var valueType = declaredType.getArgumentOrDefault(0);
            walker.output.writeArrayStart();
            walker.output.writeArrayItemStart();
            walker.output.writeInt(grid.rowCount());
            walker.output.writeArrayItemStart();
            walker.output.writeInt(grid.columnCount());
            for (var cell : grid.cells()) {
                walker.output.writeArrayItemStart();
                walker.output.writeArrayStart();
                walker.output.writeArrayItemStart();
                walker.output.writeInt(cell.getRow());
                walker.output.writeArrayItemStart();
                walker.output.writeInt(cell.getColumn());
                walker.output.writeArrayItemStart();
                walker.walkObject(valueType, "", cell.getValue());
                walker.output.writeArrayEnd();
            }
            walker.output.writeArrayEnd();
        }
    }
}
