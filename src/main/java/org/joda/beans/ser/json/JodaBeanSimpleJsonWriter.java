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
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;
import org.joda.convert.ToStringConverter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Table;

/**
 * Provides the ability for a Joda-Bean to be written to a simple JSON format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The JSON format used here is natural, with no meta-data.
 * As such, it may not be possible to write some objects or read the JSON data back in.
 * <p>
 * Beans are output using JSON objects where the key is the property name.
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * Null values are generally omitted, but where included are sent as 'null'.
 * Boolean values are sent as 'true' and 'false'.
 * Numeric values are sent as JSON numbers.
 * Maps must have a key that can be converted to a string by Joda-Convert.
 * The property type needs to be known when writing/reading - properties, or
 * list/map entries, that are defined as {@code Object} are unlikely to work well.
 * <p>
 * Collections are output using JSON arrays. Maps as JSON objects.
 */
public class JodaBeanSimpleJsonWriter {

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<JsonHandler<Object>> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected JsonHandler computeValue(Class<?> type) {
            if (Bean.class.isAssignableFrom(type)) {
                return (JsonHandler<Bean>) JodaBeanSimpleJsonWriter::writeBeanMaybeSimple;
            }
            if (type.isArray()) {
                var componentType = type.getComponentType();
                if (componentType.isPrimitive()) {
                    if (componentType != byte.class) {
                        return JodaBeanSimpleJsonWriter::writePrimitiveArray;
                    }
                } else {
                    return (JsonHandler<Object[]>) JodaBeanSimpleJsonWriter::writeArray;
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
    public JodaBeanSimpleJsonWriter(JodaBeanSer settings) {
        JodaBeanUtils.notNull(settings, "settings");
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    public String write(Bean bean) {
        var buf = new StringBuilder(1024);
        try {
            write(bean, buf);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return buf.toString();
    }

    /**
     * Writes the bean to the {@code Appendable}.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, Appendable output) throws IOException {
        JodaBeanUtils.notNull(bean, "bean");
        JodaBeanUtils.notNull(output, "output");
        this.output = new JsonOutput(output, settings.getIndent(), settings.getNewLine());
        writeBeanMaybeSimple(ResolvedType.OBJECT, "", bean);
        output.append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    // walk an object, by determining the runtime type
    private void writeObject(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        if (value == null) {
            output.writeNull();
        } else {
            var handler = LOOKUP.get(value.getClass());
            handler.handle(this, declaredType, propertyName, value);
        }
    }

    //-------------------------------------------------------------------------
    // writes a bean, favouring output as a Joda-Convert type
    private void writeBeanMaybeSimple(ResolvedType declaredType, String propertyName, Bean bean) throws IOException {
        // check for Joda-Convert cannot be in ClassValue as it relies on the settings
        if (settings.getConverter().isConvertible(bean.getClass())) {
            writeJodaConvert(declaredType, propertyName, bean);
        } else {
            writeBean(declaredType, bean, false);
        }
    }

    // writes a bean as an object with properties
    private void writeBean(ResolvedType declaredType, Bean bean, boolean isRoot) throws IOException {
        output.writeObjectStart();
        writeBeanProperties(declaredType, bean);
        output.writeObjectEnd();
    }

    // writes the bean properties
    private void writeBeanProperties(ResolvedType declaredType, Bean bean) throws IOException {
        for (var metaProperty : bean.metaBean().metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
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
    private void writeArray(ResolvedType declaredType, String propertyName, Object[] array) throws IOException {
        var componentType = declaredType.toComponentType();
        output.writeArrayStart();
        for (var item : array) {
            output.writeArrayItemStart();
            writeObject(componentType, "", item);
        }
        output.writeArrayEnd();
    }

    private void writePrimitiveArray(ResolvedType declaredType, String propertyName, Object array) throws IOException {
        var componentType = declaredType.toComponentType();
        var handler = LOOKUP.get(componentType.getRawType());
        var arrayLength = Array.getLength(array);
        output.writeArrayStart();
        for (int i = 0; i < arrayLength; i++) {
            output.writeArrayItemStart();
            handler.handle(this, componentType, propertyName, Array.get(array, i));
        }
        output.writeArrayEnd();
    }

    private void writeDouble(Double val) throws IOException {
        if (Double.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeDouble(val);
        }
    }

    private void writeFloat(Float val) throws IOException {
        if (Float.isNaN(val)) {
            output.writeNull();
        } else {
            output.writeFloat(val);
        }
    }

    private void writeJodaConvert(ResolvedType declaredType, String propertyName, Object value) throws IOException {
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

    // writes a map given map entries, used by Map/Multimap/BiMap
    private <K, V> void writeMapEntries(
            ResolvedType declaredType,
            String propertyName,
            Collection<Map.Entry<K, V>> mapEntries) throws IOException {

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
            writeObject(valueType, "", entry.getValue());
        }
        output.writeObjectEnd();
    }

    private static IllegalArgumentException invalidNullMapKey(String propertyName) {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "', map key must not be null");
    }

    //-------------------------------------------------------------------------
    private static interface JsonHandler<T> {
        public abstract void handle(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException;

        public default void handleProperty(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException {

            writer.output.writeObjectKey(propertyName);
            handle(writer, declaredType, propertyName, obj);
        }
    }

    //-------------------------------------------------------------------------
    private static sealed class BaseJsonHandlers {

        private static final BaseJsonHandlers INSTANCE = getInstance();

        private static final BaseJsonHandlers getInstance() {
            try {
                ImmutableGrid.of();  // check if class is available
                return new CollectJsonHandlers();
            } catch (RuntimeException | LinkageError ex) {
                try {
                    ImmutableMultiset.of();  // check if class is available
                    return new GuavaJsonHandlers();
                } catch (RuntimeException | LinkageError ex2) {
                    return new BaseJsonHandlers();
                }
            }
        }

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Map.class.isAssignableFrom(type)) {
                return (JsonHandler<Map<?, ?>>) BaseJsonHandlers::writeMap;
            }
            if (Iterable.class.isAssignableFrom(type)) {
                return (JsonHandler<Collection<?>>) BaseJsonHandlers::writeCollection;
            }
            return JodaBeanSimpleJsonWriter::writeJodaConvert;
        }

        // writes a collection
        private static void writeCollection(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Iterable<?> coll) throws IOException {

            var itemType = declaredType.getArgumentOrDefault(0);
            writer.output.writeArrayStart();
            for (var item : coll) {
                writer.output.writeArrayItemStart();
                writer.writeObject(itemType, "", item);
            }
            writer.output.writeArrayEnd();
        }

        // writes a map, with meta type information if necessary
        private static void writeMap(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Map<?, ?> map) throws IOException {

            // write content
            writer.writeMapEntries(declaredType, propertyName, map.entrySet());
        }
    }

    //-------------------------------------------------------------------------
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
            if (BiMap.class.isAssignableFrom(type)) {
                return (JsonHandler<BiMap<?, ?>>) GuavaJsonHandlers::writeBiMap;
            }
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return GuavaOptionalJsonHandler.INSTANCE;
            }
            return super.computeValue(type);
        }

        // writes a multimap
        private static void writeMultimap(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multimap<?, ?> mmap) throws IOException {

            writer.writeMapEntries(declaredType, propertyName, mmap.entries());
        }

        // writes a multiset
        private static void writeMultiset(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multiset<?> mset) throws IOException {

            // write content, using an array of value to count
            var valueType = declaredType.getArgumentOrDefault(0);
            writer.output.writeArrayStart();
            for (var entry : mset.entrySet()) {
                writer.output.writeArrayItemStart();
                writer.output.writeArrayStart();
                writer.output.writeArrayItemStart();
                writer.writeObject(valueType, "", entry.getElement());
                writer.output.writeArrayItemStart();
                writer.output.writeInt(entry.getCount());
                writer.output.writeArrayEnd();
            }
            writer.output.writeArrayEnd();
        }

        // writes a table
        private static void writeTable(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Table<?, ?, ?> table) throws IOException {

            // write content, using an array of cells
            var rowType = declaredType.getArgumentOrDefault(0);
            var columnType = declaredType.getArgumentOrDefault(1);
            var valueType = declaredType.getArgumentOrDefault(2);
            writer.output.writeArrayStart();
            for (var cell : table.cellSet()) {
                writer.output.writeArrayItemStart();
                writer.output.writeArrayStart();
                writer.output.writeArrayItemStart();
                writer.writeObject(rowType, "", cell.getRowKey());
                writer.output.writeArrayItemStart();
                writer.writeObject(columnType, "", cell.getColumnKey());
                writer.output.writeArrayItemStart();
                writer.writeObject(valueType, "", cell.getValue());
                writer.output.writeArrayEnd();
            }
            writer.output.writeArrayEnd();
        }

        // writes a BiMap, with meta type information if necessary
        private static void writeBiMap(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                BiMap<?, ?> biMap) throws IOException {

            writer.writeMapEntries(declaredType, propertyName, biMap.entrySet());
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
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Grid<?> grid) throws IOException {

            // write grid using sparse approach
            var valueType = declaredType.getArgumentOrDefault(0);
            writer.output.writeArrayStart();
            writer.output.writeArrayItemStart();
            writer.output.writeInt(grid.rowCount());
            writer.output.writeArrayItemStart();
            writer.output.writeInt(grid.columnCount());
            for (var cell : grid.cells()) {
                writer.output.writeArrayItemStart();
                writer.output.writeArrayStart();
                writer.output.writeArrayItemStart();
                writer.output.writeInt(cell.getRow());
                writer.output.writeArrayItemStart();
                writer.output.writeInt(cell.getColumn());
                writer.output.writeArrayItemStart();
                writer.writeObject(valueType, "", cell.getValue());
                writer.output.writeArrayEnd();
            }
            writer.output.writeArrayEnd();
        }
    }

    //-------------------------------------------------------------------------
    static final class OptionalJsonHandler implements JsonHandler<Optional<?>> {
        private static final OptionalJsonHandler INSTANCE = new OptionalJsonHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            var valueType = declaredType.getArgumentOrDefault(0);
            writer.writeObject(valueType, "", opt.orElse(null));
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            var value = opt.orElse(null);
            if (value != null) {
                var valueType = declaredType.getArgumentOrDefault(0);
                writer.output.writeObjectKey(propertyName);
                writer.writeObject(valueType, propertyName, value);
            }
        }
    }

    //-------------------------------------------------------------------------
    static final class GuavaOptionalJsonHandler implements JsonHandler<com.google.common.base.Optional<?>> {
        private static final GuavaOptionalJsonHandler INSTANCE = new GuavaOptionalJsonHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) throws IOException {

            var valueType = declaredType.getArgumentOrDefault(0);
            writer.writeObject(valueType, "", opt.orNull());
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanSimpleJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) throws IOException {

            var value = opt.orNull();
            if (value != null) {
                var valueType = declaredType.getArgumentOrDefault(0);
                writer.output.writeObjectKey(propertyName);
                writer.writeObject(valueType, propertyName, value);
            }
        }
    }
}
