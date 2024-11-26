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
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.DynamicBean;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

/**
 * Writes the Joda-Bean BeanPack binary format with strings deduplicated by reference.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
final class JodaBeanPackedBinWriter {

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<BinHandler<Object>> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected BinHandler computeValue(Class<?> type) {
            if (Bean.class.isAssignableFrom(type)) {
                return (BinHandler<Bean>) JodaBeanPackedBinWriter::writeBeanMaybeSimple;
            }
            // these primitive types are always written and interpretted without a type
            if (type == String.class) {
                return (writer, declaredType, propName, value) -> writer.writeString((String) value);
            }
            if (type == Integer.class || type == int.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Integer) value);
            }
            if (type == Long.class || type == long.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeLong((Long) value);
            }
            if (type == Short.class || type == short.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeShort((Short) value);
            }
            if (type == Byte.class || type == byte.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeByte((Byte) value);
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
            if (type == LocalDate.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeDate((LocalDate) value);
            }
            if (type == LocalTime.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeTime((LocalTime) value);
            }
            if (type == Instant.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInstant((Instant) value);
            }
            if (type == Duration.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeDuration((Duration) value);
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
    private final BeanPackOutput output;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private final Map<Class<?>, String> knownTypes = new IdentityHashMap<>();
    /**
     * The type definitions that have been output.
     */
    private int typeDefinitionIndex;
    /**
     * The type definitions that have been output.
     */
    private final Map<Class<?>, Integer> typeDefinitions = new IdentityHashMap<>();
    /**
     * The bean definitions that have been output (effectively an IdentityHashSet).
     */
    private final Map<Class<?>, Void> beanDefinitions = new IdentityHashMap<>();
    /**
     * The type definitions that have been output.
     */
    private int valueDefinitionIndex;
    /**
     * The value definitions that have been output.
     */
    private final Map<Object, Integer> valueDefinitions = new HashMap<>();

    /**
     * Creates an instance.
     * 
     * @param settings  the settings to use, not null
     * @param out  the output stream, not null
     */
    JodaBeanPackedBinWriter(JodaBeanSer settings, OutputStream out) {
        this.settings = settings;
        this.output = new BeanPackOutput(out);
    }

    //-------------------------------------------------------------------------
    /**
     * Writes the bean to the {@code OutputStream}.
     * 
     * @param bean  the bean to output, not null
     * @throws IOException if an error occurs
     */
    void write(Bean bean) throws IOException {
        try {
            // these first two bytes in BeanPack are compatible with MsgPack!
            output.writeArrayHeader(2);
            output.writeInt(3);  // version 3
            // root always outputs the bean, not Joda-Convert form
            writeBean(ResolvedType.OBJECT, bean, true);
        } catch (UncheckedIOException ex) {
            throw ex.getCause();
        }
    }

    //-----------------------------------------------------------------------
    // writes an object, by determining the runtime type
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
            writeSimple(declaredType, propertyName, bean);
        } else {
            writeBean(declaredType, bean, false);
        }
    }

    // writes a bean, with meta type information if necessary
    private void writeBean(ResolvedType declaredType, Bean bean, boolean isRoot) throws IOException {
        var beanClass = bean.getClass();
        if (!beanDefinitions.containsKey(beanClass)) {
            if (bean instanceof DynamicBean) {
                if (beanClass != declaredType.getRawType()) {
                    writeTypeNameOrReference(beanClass, isRoot);
                }
                writeDynamicBean(bean);
            } else {
                beanDefinitions.put(beanClass, null);
                writeTypeNameOrReference(beanClass, isRoot);
                writeBeanWithDefinition(bean);
            }
        } else {
            if (beanClass != declaredType.getRawType()) {
                writeTypeNameOrReference(beanClass, isRoot);
            }
            writeBeanValues(bean);
        }
    }

    // writes the dynamic bean as a map of property name to property value
    private void writeDynamicBean(Bean bean) throws IOException {
        var beanClass = bean.getClass();
        var metaBean = bean.metaBean();
        output.writeMapHeader(metaBean.metaPropertyCount());
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                var resolvedType = metaProperty.propertyResolvedType(beanClass);
                var childPropertyName = metaProperty.name();
                var value = metaProperty.get(bean);
                output.writeString(childPropertyName);
                writeObject(resolvedType, childPropertyName, value);
            } else {
                output.writeNull();
                output.writeNull();
            }
        }
    }

    // writes the bean definition structure
    private void writeBeanWithDefinition(Bean bean) throws IOException {
        var beanClass = bean.getClass();
        var metaBean = bean.metaBean();
        output.writeBeanDefinitionHeader(metaBean.metaPropertyCount());
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                output.writeString(metaProperty.name());
            } else {
                output.writeNull();
            }
        }
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                var resolvedType = metaProperty.propertyResolvedType(beanClass);
                var childPropertyName = metaProperty.name();
                var value = metaProperty.get(bean);
                writeObject(resolvedType, childPropertyName, value);
            } else {
                output.writeNull();
            }
        }
    }

    // writes the bean values without property names, effectively referring back to the original definition
    private void writeBeanValues(Bean bean) throws IOException {
        // have kept the extraneous array header as it makes it easier to parse
        var beanClass = bean.getClass();
        var metaBean = bean.metaBean();
        output.writeArrayHeader(metaBean.metaPropertyCount());
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                var resolvedType = metaProperty.propertyResolvedType(beanClass);
                var childPropertyName = metaProperty.name();
                var value = metaProperty.get(bean);
                writeObject(resolvedType, childPropertyName, value);
            } else {
                output.writeNull();
            }
        }
    }

    //-----------------------------------------------------------------------
    // writes a simple type, with meta type information if necessary
    // this method is never called for primitive types like int/long/LocalDate
    private void writeString(String str) throws IOException {
        if (str.length() >= 6) {
            var ref = valueDefinitions.get(str);
            if (ref == null) {
                valueDefinitions.put(str, valueDefinitionIndex++);
                output.writeValueDefinitionMarker();
                output.writeString(str);
            } else {
                output.writeValueReference(ref);
            }
        } else {
            output.writeString(str);
        }
    }

    // writes a simple type, with meta type information if necessary
    // this method is never called for primitive types like int/long/LocalDate
    private void writeSimple(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        // handle situations where there is no declared type, or there is a subclass of the declared type
        var effectiveType = value.getClass();
        if (effectiveType != declaredType.getRawType()) {
            effectiveType = settings.getConverter().findTypedConverter(effectiveType).getEffectiveType();
            writeTypeNameOrReference(effectiveType, false);
        }
        // write the reference, or call Joda-Convert if first time value is seen
        var ref = valueDefinitions.get(value);
        if (ref == null) {
            writeJodaConvert(effectiveType, propertyName, value);
        } else {
            output.writeValueReference(ref);
        }
    }

    // writes the object as a String using Joda-Convert
    private void writeJodaConvert(Class<?> effectiveType, String propertyName, Object value) throws IOException {
        try {
            var converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw invalidNullString(propertyName, value);
            }
            var ref = valueDefinitions.get(converted);
            if (ref == null) {
                valueDefinitions.put(value, valueDefinitionIndex);
                valueDefinitions.put(converted, valueDefinitionIndex++);
                output.writeValueDefinitionMarker();
                output.writeString(converted);
            } else {
                valueDefinitions.put(value, ref);
                output.writeValueReference(ref);
            }
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

    // writes the type header
    private final void writeTypeNameOrReference(Class<?> type, boolean isRoot) throws IOException {
        var ref = typeDefinitions.get(type);
        if (ref == null) {
            var encodedClassName = SerTypeMapper.encodeType(type, settings, basePackage, knownTypes);
            if (isRoot && basePackage == null) {
                basePackage = type.getPackage().getName() + ".";
            }
            typeDefinitions.put(type, typeDefinitionIndex++);
            output.writeTypeName(encodedClassName);
        } else {
            output.writeTypeReference(ref);
        }
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException;

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
            if (Optional.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Optional<?>>) BaseBinHandlers::writeOptional;
            }
            if (type == byte[].class) {
                return (writer, declaredType, propName, value) -> writer.output.writeBytes((byte[]) value);
            }
            if (type == double[].class) {
                return (writer, declaredType, propName, value) -> writer.output.writeDoubles((double[]) value);
            }
            if (type.isArray()) {
                var componentType = type.getComponentType();
                if (componentType.isPrimitive()) {
                    return (CollectionBinHandler<Object>) BaseBinHandlers::writePrimitiveArray;
                } else {
                    return (CollectionBinHandler<Object[]>) BaseBinHandlers::writeArray;
                }
            }
            if (Collection.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Collection<?>>) BaseBinHandlers::writeCollection;
            }
            if (Map.class.isAssignableFrom(type)) {
                return (CollectionBinHandler<Map<?, ?>>) BaseBinHandlers::writeMap;
            }
            return JodaBeanPackedBinWriter::writeSimple;
        }

        private static void writeOptional(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            // write actual type
            if (!Optional.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_OPTIONAL);
            }
            // write content
            var valueType = declaredType.getArgumentOrDefault(0).toRawType();
            writer.writeObject(valueType, "", opt.orElse(null));
        }

        // writes an array, with meta type information if necessary
        private static void writeArray(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object[] array) throws IOException {

            // write actual type
            var actualComponentType = array.getClass().getComponentType();
            if (!declaredType.isArray() || declaredType.getRawType().getComponentType() != actualComponentType) {
                writeArrayTypeDescription(writer, actualComponentType);
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object array) throws IOException {

            // write actual type
            var arrayType = array.getClass();
            if (!declaredType.isArray()) {
                writeArrayTypeDescription(writer, arrayType);
            }
            // write content
            var componentType = toWeakenedType(declaredType.toComponentType());
            var handler = JodaBeanPackedBinWriter.LOOKUP.get(componentType.getRawType());
            var arrayLength = Array.getLength(array);
            writer.output.writeArrayHeader(arrayLength);
            for (int i = 0; i < arrayLength; i++) {
                handler.handle(writer, declaredType, propertyName, Array.get(array, i));
            }
        }

        // writes the meta type header
        private static final void writeArrayTypeDescription(JodaBeanPackedBinWriter writer, Class<?> arrayType) throws IOException {
            var ref = writer.typeDefinitions.get(arrayType);
            if (ref == null) {
                var encodedClassName = metaTypeArrayName(arrayType.getComponentType());
                writer.typeDefinitions.put(arrayType, writer.typeDefinitionIndex++);
                writer.output.writeTypeName(encodedClassName);
            } else {
                writer.output.writeTypeReference(ref);
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

        // writes a collection, with meta type information if necessary
        private static void writeCollection(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Collection<?> coll) throws IOException {

            // write actual type
            if (coll instanceof Set && !Set.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_SET);
            } else if (!Collection.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_LIST);
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Map<?, ?> map) throws IOException {

            // write actual type
            if (!Map.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_MAP);
            }
            // write content
            writeMapEntries(writer, declaredType, propertyName, map.entrySet());
        }

        // writes a map given map entries, code shared with Multimap
        static <K, V> void writeMapEntries(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Collection<Map.Entry<K, V>> mapEntries) throws IOException {

            var keyType = toWeakenedType(declaredType.getArgumentOrDefault(0));
            var valueType = toWeakenedType(declaredType.getArgumentOrDefault(1));
            writer.output.writeMapHeader(mapEntries.size());
            for (var entry : mapEntries) {
                var key = entry.getKey();
                if (key == null) {
                    throw invalidNullMapKey(propertyName);
                }
                writer.writeObject(keyType, "", entry.getKey());
                writer.writeObject(valueType, "", entry.getValue());
            }
        }

        private static IllegalArgumentException invalidNullMapKey(String propertyName) throws IOException {
            return new IllegalArgumentException(
                    "Unable to write property '" + propertyName + "', map key must not be null");
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
            return super.createHandler(type);
        }

        // writes a multimap, with meta type information if necessary
        private static void writeMultimap(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multimap<?, ?> mmap) throws IOException {

            // write actual type
            if (mmap instanceof SetMultimap && !SetMultimap.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_SET_MULTIMAP);
            } else if (!Multimap.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_LIST_MULTIMAP);
            }
            // write content
            writeMapEntries(writer, declaredType, propertyName, mmap.entries());
        }

        // writes a multiset, with meta type information if necessary
        private static void writeMultiset(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multiset<?> mset) throws IOException {

            // write actual type
            if (!Multiset.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_MULTISET);
            }
            // write content
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Table<?, ?, ?> table) throws IOException {

            // write actual type
            if (!Table.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_TABLE);
            }
            // write content
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                BiMap<?, ?> biMap) throws IOException {

            // write actual type
            if (!BiMap.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_BIMAP);
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
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Grid<?> grid) throws IOException {

            // write actual type
            if (!Grid.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_GRID);
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
}
