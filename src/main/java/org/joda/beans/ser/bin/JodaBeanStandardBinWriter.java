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
package org.joda.beans.ser.bin;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import org.joda.beans.Bean;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

/**
 * Writes a Joda-Bean in standard binary format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
final class JodaBeanStandardBinWriter {

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<BinHandler<Object>> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected BinHandler computeValue(Class<?> type) {
            if (Bean.class.isAssignableFrom(type)) {
                return (BinHandler<Bean>) JodaBeanStandardBinWriter::writeBeanMaybeSimple;
            }
            // these primitive types are always written and interpretted without a type
            if (type == String.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeString((String) value);
            }
            if (type == Integer.class || type == int.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Integer) value);
            }
            if (type == Double.class || type == double.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeDouble((Double) value);
            }
            if (type == Float.class || type == float.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeFloat((Float) value);
            }
            if (type == Boolean.class || type == boolean.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeBoolean((Boolean) value);
            }
            return BaseBinHandlers.INSTANCE.createHandler(type);
        }
    };

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The outputter.
     */
    private final MsgPackOutput output;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private final Map<Class<?>, String> knownTypes = new HashMap<>();

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param out  the output stream, not null
     */
    JodaBeanStandardBinWriter(JodaBeanSer settings, OutputStream out) {
        this.settings = settings;
        this.output = new MsgPackOutput(out);
    }

    //-------------------------------------------------------------------------
    /**
     * Writes the bean to the {@code OutputStream}.
     * 
     * @param bean  the bean to output, not null
     * @param includeRootType  whether to include the root type
     * @throws IOException if an error occurs
     */
    void write(Bean bean, boolean includeRootType) throws IOException {
        var rootType = includeRootType ? ResolvedType.OBJECT : ResolvedType.of(bean.getClass());
        output.writeArrayHeader(2);
        output.writeInt(1);  // version 1
        // root always outputs the bean, not Joda-Convert form
        writeBean(rootType, "", bean, includeRootType);
    }

    //-----------------------------------------------------------------------
    // writes an object, by determining the runtime type
    private void writeObject(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        if (value == null) {
            output.writeNil();
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
            writeSimple(declaredType, propertyName, bean);
        } else {
            writeBean(declaredType, propertyName, bean, false);
        }
    }

    // writes a bean, with meta type information if necessary
    private void writeBean(ResolvedType declaredType, String propertyName, Bean bean, boolean isRootAndInclRootType) throws IOException {
        // have to determine the number of properties being output before starting to write
        var count = bean.metaBean().metaPropertyCount();
        var propHandlers = new PropertyHandler[count];
        var size = 0;
        for (var metaProperty : bean.metaBean().metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                var value = metaProperty.get(bean);
                if (value != null) {
                    var resolvedType = metaProperty.propertyResolvedType(bean.getClass());
                    var handler = LOOKUP.get(value.getClass());
                    // package up each write in a lambda, avoiding a second lookup
                    var propHandler = handler.handleProperty(this, resolvedType, metaProperty.name(), value);
                    if (propHandler != null) {
                        propHandlers[size++] = propHandler;
                    }
                }
            }
        }
        // write out the header, potentially including the type
        if (bean.getClass() != declaredType.getRawType()) {
            var typeStr = SerTypeMapper.encodeType(bean.getClass(), settings, basePackage, knownTypes);
            if (isRootAndInclRootType) {
                basePackage = bean.getClass().getPackage().getName() + ".";
            }
            output.writeMapHeader(size + 1);
            output.writeExtensionString(MsgPack.JODA_TYPE_BEAN, typeStr);
            output.writeNil();
        } else {
            output.writeMapHeader(size);
        }
        // write each property
        for (int i = 0; i < size; i++) {
            propHandlers[i].handle();
        }
    }

    //-----------------------------------------------------------------------
    // writes a simple type, with meta type information if necessary
    private void writeSimple(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        // when int/double/float/boolean are parsed they are treated as being of that exact type
        // long/short/byte require type metadata
        // also handle no declared type and subclasses
        var effectiveType = getAndSerializeEffectiveTypeIfRequired(value, declaredType.getRawType());
        // currently favouring == checks over pattern match switch
        var type = value.getClass();
        if (type == Long.class) {
            output.writeLong((Long) value);
        } else if (type == Short.class) {
            output.writeInt((Short) value);
        } else if (type == Byte.class) {
            output.writeInt((Byte) value);
        } else if (type == byte[].class) {
            output.writeBytes((byte[]) value);
        } else {
            writeJodaConvert(effectiveType, propertyName, value);
        }
    }

    // called when serializing an object in {@link #writeSimple(Class, Object)}, to get the effective type of the
    // object and if necessary to serialize the class information
    // needs to handle no declared type and subclass instances
    private Class<?> getAndSerializeEffectiveTypeIfRequired(Object value, Class<?> declaredType) throws IOException {
        var realType = value.getClass();
        if (declaredType == Object.class) {
            if (realType != String.class) {
                var effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                output.writeMapHeader(1);
                var type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
                return effectiveType;
            } else {
                return realType;
            }
        }
        if (!settings.getConverter().isConvertible(declaredType)) {
            var effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            output.writeMapHeader(1);
            var type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
            output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
            return effectiveType;
        }
        return declaredType;
    }

    // writes the object as a String using Joda-Convert
    private void writeJodaConvert(Class<?> effectiveType, String propertyName, Object value) throws IOException {
        try {
            var converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw invalidNullString(propertyName, value);
            }
            output.writeString(converted);
        } catch (RuntimeException ex) {
            throw invalidConversionMsg(propertyName, value, ex);
        }
    }

    private static IllegalArgumentException invalidNullString(String propertyName, Object value) throws IOException {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "' because converter returned a null string: " + value);
    }

    private IllegalArgumentException invalidConversionMsg(String propertyName, Object value, RuntimeException ex) {
        var msg = "Unable to write property '" + propertyName + "', type " +
                value.getClass().getName() + " could not be converted to a String";
        return new IllegalArgumentException(msg, ex);
    }

    // writes the meta type header
    private static final void writeMetaType(JodaBeanStandardBinWriter writer, String metaTypeName) throws IOException {
        writer.output.writeMapHeader(1);
        writer.output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
    }

    // gets the weakened type, which exists for backwards compatibility
    // once the parser can handle ResolvedType this method can, in theory, be removed
    private static ResolvedType toWeakenedType(ResolvedType base) {
        for (var arg : base.getArguments()) {
            var rawType = arg.getRawType();
            if (LOOKUP.get(rawType).isCollection(rawType)) {
                return base.toRawType();
            }
        }
        return base;
    }

    //-------------------------------------------------------------------------
    private static interface BinHandler<T> {
        public abstract void handle(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException;

        public default PropertyHandler handleProperty(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) {

            return () -> {
                writer.output.writeString(propertyName);
                handle(writer, declaredType, propertyName, obj);
            };
        }

        public default boolean isCollection(Class<?> type) {
            return false;
        }
    }

    private static interface CollectionBinHandler<T> extends BinHandler<T> {
        @Override
        public default boolean isCollection(Class<?> type) {
            return true;
        }
    }

    //-------------------------------------------------------------------------
    // like Runnable, but with IOException
    private static interface PropertyHandler {
        public abstract void handle() throws IOException;
    }

    //-------------------------------------------------------------------------
    // handles base JDK collections
    private static sealed class BaseBinHandlers {

        // an instance loaded dependent on the classpath
        private static final BaseBinHandlers INSTANCE = getInstance();

        private static final BaseBinHandlers getInstance() {
            try {
                ImmutableGrid.of();  // check if class is available
                return new CollectBinHandlers();
            } catch (Exception | LinkageError ex) {
                try {
                    ImmutableMultiset.of();  // check if class is available
                    return new GuavaBinHandlers();
                } catch (Exception | LinkageError ex2) {
                    return new BaseBinHandlers();
                }
            }
        }

        // creates the handler, called from ClassValue.computeValue()
        BinHandler<?> createHandler(Class<?> type) {
            if (type == Optional.class) {
                return OptionalBinHandler.INSTANCE;
            }
            if (type.isArray()) {
                var componentType = type.getComponentType();
                if (componentType.isPrimitive()) {
                    if (componentType == byte.class) {
                        return JodaBeanStandardBinWriter::writeSimple;
                    } else {
                        return (CollectionBinHandler<Object>) BaseBinHandlers::writePrimitiveArray;
                    }
                } else {
                    return (CollectionBinHandler<Object[]>) BaseBinHandlers::writeArray;
                }
            }
            if (Map.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Map<?, ?>>) BaseBinHandlers::writeMap;
            }
            if (Collection.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Collection<?>>) BaseBinHandlers::writeCollection;
            }
            if (Iterable.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Iterable<?>>) BaseBinHandlers::writeIterable;
            }
            return JodaBeanStandardBinWriter::writeSimple;
        }

        // writes an array, with meta type information if necessary
        private static void writeArray(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object[] array) throws IOException {

            // write actual type
            var valueType = array.getClass().getComponentType();
            if (valueType == Object.class && !Object[].class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, metaTypeArrayName(valueType));
            } else if (valueType == String.class && !String[].class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, metaTypeArrayName(valueType));
            }
            // write content
            var componentType = toWeakenedType(declaredType.toComponentType());
            writer.output.writeArrayHeader(array.length);
            for (var item : array) {
                writer.writeObject(componentType, "", item);
            }
        }

        // writes a primitive array, with meta type information if necessary
        private static void writePrimitiveArray(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object array) throws IOException {

            // write actual type
            var valueType = array.getClass().getComponentType();
            if (!declaredType.isArray()) {
                writeMetaType(writer, metaTypeArrayName(valueType));
            }
            // write content
            var componentType = declaredType.toComponentType();
            var handler = JodaBeanStandardBinWriter.LOOKUP.get(componentType.getRawType());
            var arrayLength = Array.getLength(array);
            writer.output.writeArrayHeader(arrayLength);
            for (int i = 0; i < arrayLength; i++) {
                handler.handle(writer, componentType, propertyName, Array.get(array, i));
            }
        }

        // determines the meta type name to use
        private static String metaTypeArrayName(Class<?> valueType) {
            if (valueType.isArray()) {
                return metaTypeArrayName(valueType.getComponentType()) + "[]";
            }
            if (valueType == Object.class) {
                return "Object[]";
            }
            if (valueType == String.class) {
                return "String[]";
            }
            return valueType.getName() + "[]";
        }

        // writes an iterable, with meta type information if necessary
        private static void writeIterable(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Iterable<?> iterable) throws IOException {

            // convert to a list, which is necessary as there is no size() on Iterable
            // this ensures that the generics of the iterable are retained
            var list = StreamSupport.stream(iterable::spliterator, Spliterator.ORDERED, false).toList();
            var adjustedType = ResolvedType.of(List.class, declaredType.getArguments());
            writeCollection(writer, adjustedType, propertyName, list);
        }

        // writes a collection, with meta type information if necessary
        private static void writeCollection(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Collection<?> coll) throws IOException {

            // write actual type
            if (coll instanceof Set && !Set.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Set");
            } else if (coll instanceof List && !List.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "List");
            } else if (!Collection.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Collection");
            }
            // write content
            var itemType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            writer.output.writeArrayHeader(coll.size());
            for (var item : coll) {
                writer.writeObject(itemType, "", item);
            }
        }

        // writes a map, with meta type information if necessary
        private static void writeMap(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Map<?, ?> map) throws IOException {

            // write actual type
            if (!Map.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Map");
            }
            // write content
            writeMapEntries(writer, declaredType, propertyName, map.entrySet());
        }

        // writes a map given map entries, code shared with Multimap
        static <K, V> void writeMapEntries(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Collection<Map.Entry<K, V>> mapEntries) throws IOException {

            var keyType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            var valueType = toWeakenedType(declaredType.getArgumentOrDefault(1));
            writer.output.writeMapHeader(mapEntries.size());
            for (var entry : mapEntries) {
                writer.writeObject(keyType, "", entry.getKey());
                writer.writeObject(valueType, "", entry.getValue());
            }
        }
    }

    //-------------------------------------------------------------------------
    private static sealed class GuavaBinHandlers extends BaseBinHandlers {

        @Override
        BinHandler<?> createHandler(Class<?> type) {
            if (Multimap.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Multimap<?, ?>>) GuavaBinHandlers::writeMultimap;
            }
            if (Multiset.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Multiset<?>>) GuavaBinHandlers::writeMultiset;
            }
            if (Table.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Table<?, ?, ?>>) GuavaBinHandlers::writeTable;
            }
            if (BiMap.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<BiMap<?, ?>>) GuavaBinHandlers::writeBiMap;
            }
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return GuavaOptionalBinHandler.INSTANCE;
            }
            return super.createHandler(type);
        }

        // writes a multimap, with meta type information if necessary
        private static void writeMultimap(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multimap<?, ?> mmap) throws IOException {

            // write actual type
            if (mmap instanceof SetMultimap && !SetMultimap.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "SetMultimap");
            } else if (mmap instanceof ListMultimap && !ListMultimap.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "ListMultimap");
            } else if (!Multimap.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Multimap");
            }
            // write content, using a map with repeated keys
            writeMapEntries(writer, declaredType, propertyName, mmap.entries());
        }

        // writes a multiset, with meta type information if necessary
        private static void writeMultiset(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multiset<?> mset) throws IOException {

            // write actual type
            if (!Multiset.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Multiset");
            }
            // write content, using a map of value to count
            var valueType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            var entrySet = mset.entrySet();
            writer.output.writeMapHeader(entrySet.size());
            for (var entry : entrySet) {
                writer.writeObject(valueType, "", entry.getElement());
                writer.output.writeInt(entry.getCount());
            }
        }

        // writes a table, with meta type information if necessary
        private static void writeTable(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Table<?, ?, ?> table) throws IOException {

            // write actual type
            if (!Table.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Table");
            }
            // write content, using an array of cells
            var rowType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            var columnType = toWeakenedType(declaredType.getArgumentOrDefault(1));
            var valueType = toWeakenedType(declaredType.getArgumentOrDefault(2));
            writer.output.writeArrayHeader(table.size());
            for (var cell : table.cellSet()) {
                writer.output.writeArrayHeader(3);
                writer.writeObject(rowType, "", cell.getRowKey());
                writer.writeObject(columnType, "", cell.getColumnKey());
                writer.writeObject(valueType, "", cell.getValue());
            }
        }

        // writes a BiMap, with meta type information if necessary
        private static void writeBiMap(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                BiMap<?, ?> biMap) throws IOException {

            // write actual type
            if (!BiMap.class.isAssignableFrom(declaredType.getRawType())) {
                // hack around Guava annoyance by assuming that size 0 and 1 ImmutableBiMap
                // was actually meant to be an ImmutableMap
                if ((declaredType.getRawType() != Map.class && declaredType.getRawType() != ImmutableMap.class) || biMap.size() >= 2) {
                    writeMetaType(writer, "BiMap");
                } else if (!Map.class.isAssignableFrom(declaredType.getRawType())) {
                    writeMetaType(writer, "Map");
                }
            }
            // write content
            writeMapEntries(writer, declaredType, propertyName, biMap.entrySet());
        }
    }

    //-------------------------------------------------------------------------
    private static final class CollectBinHandlers extends GuavaBinHandlers {

        @Override
        BinHandler<?> createHandler(Class<?> type) {
            if (Grid.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Grid<?>>) CollectBinHandlers::writeGrid;
            }
            return super.createHandler(type);
        }

        // writes a grid, with meta type information if necessary
        private static void writeGrid(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Grid<?> grid) throws IOException {

            // write actual type
            if (!Grid.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Grid");
            }
            // write content using sparse or dense approach
            var valueType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            var rows = grid.rowCount();
            var columns = grid.columnCount();
            var totalSize = rows * columns;
            var gridSize = grid.size();
            if (gridSize < (totalSize / 4)) {
                // sparse
                writer.output.writeArrayHeader(gridSize + 2);
                writer.output.writeInt(rows);
                writer.output.writeInt(columns);
                for (var cell : grid.cells()) {
                    writer.output.writeArrayHeader(3);
                    writer.output.writeInt(cell.getRow());
                    writer.output.writeInt(cell.getColumn());
                    writer.writeObject(valueType, "", cell.getValue());
                }
            } else {
                // dense
                writer.output.writeArrayHeader(totalSize + 2);
                writer.output.writeInt(rows);
                writer.output.writeInt(columns);
                for (var row = 0; row < rows; row++) {
                    for (var column = 0; column < columns; column++) {
                        writer.writeObject(valueType, "", grid.get(row, column));
                    }
                }
            }
        }
    }

    //-------------------------------------------------------------------------
    static final class OptionalBinHandler implements BinHandler<Optional<?>> {
        private static final OptionalBinHandler INSTANCE = new OptionalBinHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            if (!Optional.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "Optional");
            }
            var valueType = declaredType.getArgumentOrDefault(0).toRawType();
            writer.writeObject(valueType, "", opt.orElse(null));
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public PropertyHandler handleProperty(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) {

            return opt
                    .map(value -> (PropertyHandler) () -> {
                        var valueType = declaredType.getArgumentOrDefault(0).toRawType();
                        writer.output.writeString(propertyName);
                        writer.writeObject(valueType, propertyName, value);
                    })
                    .orElse(null);
        }
    }

    //-------------------------------------------------------------------------
    static final class GuavaOptionalBinHandler implements BinHandler<com.google.common.base.Optional<?>> {
        private static final GuavaOptionalBinHandler INSTANCE = new GuavaOptionalBinHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) throws IOException {

            if (!com.google.common.base.Optional.class.isAssignableFrom(declaredType.getRawType())) {
                writeMetaType(writer, "GuavaOptional");
            }
            // write content
            var valueType = declaredType.getArgumentOrDefault(0).toRawType();
            writer.writeObject(valueType, "", opt.orNull());
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public PropertyHandler handleProperty(
                JodaBeanStandardBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) {

            return opt
                    .transform(value -> (PropertyHandler) () -> {
                        var valueType = declaredType.getArgumentOrDefault(0).toRawType();
                        writer.output.writeString(propertyName);
                        writer.writeObject(valueType, propertyName, value);
                    })
                    .orNull();
        }
    }
}
