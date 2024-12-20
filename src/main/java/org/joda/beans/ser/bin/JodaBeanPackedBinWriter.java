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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import org.joda.beans.Bean;
import org.joda.beans.DynamicBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ResolvedType;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.impl.map.MapBean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
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

    static final int MIN_LENGTH_STR_VALUE = 3;

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
            if (type == Byte.class || type == byte.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeByte((Byte) value);
            }
            if (type == Short.class || type == short.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeShort((Short) value);
            }
            if (type == Character.class || type == char.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeChar((Character) value);
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
     * The bean definitions.
     */
    private final Map<Class<?>, List<MetaProperty<?>>> beanDefinitions = new IdentityHashMap<>();
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
     * @param includeRootType  whether to include the root type
     * @throws IOException if an error occurs
     */
    void write(Bean bean, boolean includeRootType) throws IOException {
        var beanClass = bean.getClass();
        // these first two bytes in BeanPack are compatible with MsgPack!
        output.writeArrayHeader(3);
        output.writeInt(3);  // version 3
        if (includeRootType && beanClass != FlexiBean.class && beanClass != MapBean.class && settings.isShortTypes()) {
            basePackage = beanClass.getPackage().getName() + '.';
            SerTypeMapper.encodeType(beanClass, settings, basePackage, knownTypes);
            writeString(basePackage);
        } else {
            basePackage = null;
            output.writeNull();
        }

        // root always outputs the bean, not Joda-Convert form
        writeBean(ResolvedType.of(beanClass), bean, includeRootType);
        if (typeDefinitionIndex > 0xFFFF) {
            throw new IllegalArgumentException("Invalid bindary data: Too many type references");
        }
        if (valueDefinitionIndex > 0xFFFFFF) {
            throw new IllegalArgumentException("Invalid bindary data: Too many value references");
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
        // cannot call valueDefinitions.get(value) here, as hashCode() of a random bean may be very expensive
        if (settings.getConverter().isConvertible(bean.getClass())) {
            writeSimple(propertyName, bean);
        } else if (settings.getBeanValueClasses().contains(bean.getClass())) {
            writeCachedBean(bean);
        } else {
            writeBean(declaredType, bean, true);
        }
    }

    // writes a bean, where the bean is a value that can be cached
    private void writeCachedBean(Bean bean) throws IOException {
        // note that the declared type is not used to refine the output, creating separation of types in the binary form
        var ref = valueDefinitions.get(bean);
        if (ref == null) {
            // always write the type, as it could be read while skipping where the type is not known
            output.writeValueDefinitionHeader();
            writeTypeNameOrReference(bean.getClass());

            var beanClass = bean.getClass();
            var metaProperties = beanDefinitions.get(beanClass);
            if (metaProperties == null) {
                writeBeanWithDefinition(bean);
            } else {
                writeBeanValues(bean, metaProperties);
            }
            valueDefinitions.put(bean, valueDefinitionIndex++);
        } else {
            output.writeValueReference(ref);
        }
    }

    // writes a bean, with meta type information if necessary
    private void writeBean(ResolvedType declaredType, Bean bean, boolean includeRootType) throws IOException {
        var beanClass = bean.getClass();
        var metaProperties = beanDefinitions.get(beanClass);
        if (metaProperties == null) {
            if (bean instanceof DynamicBean || !includeRootType) {
                if (beanClass != declaredType.getRawType()) {
                    writeTypeNameOrReference(beanClass);
                }
                writeDynamicBean(bean);
            } else {
                // always write the type, as it could be read while skipping where the type is not known
                writeTypeNameOrReference(beanClass);
                writeBeanWithDefinition(bean);
            }
        } else {
            if (beanClass != declaredType.getRawType()) {
                writeTypeNameOrReference(beanClass);
            }
            writeBeanValues(bean, metaProperties);
        }
    }

    // writes the dynamic bean as a map of property name to property value, without any type information
    private void writeDynamicBean(Bean bean) throws IOException {
        // note that the declared type is not used to refine the output, creating separation of types in the binary form
        var beanClass = bean.getClass();
        var metaBean = bean.metaBean();
        var beanMap = new LinkedHashMap<MetaProperty<?>, Object>(metaBean.metaPropertyCount());
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                var value = metaProperty.get(bean);
                if (value != null) {
                    beanMap.put(metaProperty, value);
                }
            }
        }
        output.writeMapHeader(beanMap.size());
        for (var entry : beanMap.entrySet()) {
            var metaProperty = entry.getKey();
            var value = entry.getValue();
            var resolvedType = metaProperty.propertyResolvedType(beanClass);
            var childPropertyName = metaProperty.name();
            if (value != null) {
                writeString(childPropertyName);
                writeObject(resolvedType, childPropertyName, value);
            }
        }
    }

    // writes the bean definition structure
    private void writeBeanWithDefinition(Bean bean) throws IOException {
        // note that the declared type is not used to refine the output, creating separation of types in the binary form
        var metaProperties = findSerializedMetaProperties(bean);
        if (metaProperties.size() > 255) {
            writeDynamicBean(bean);
        } else {
            var beanClass = bean.getClass();
            beanDefinitions.put(beanClass, metaProperties);
            output.writeBeanDefinitionHeader(metaProperties.size());
            for (var metaProperty : metaProperties) {
                writeString(metaProperty.name());
            }
            for (var metaProperty : metaProperties) {
                var resolvedType = metaProperty.propertyResolvedType(beanClass);
                var childPropertyName = metaProperty.name();
                var value = metaProperty.get(bean);
                writeObject(resolvedType, childPropertyName, value);
            }
        }
    }

    // find the list of meta properties that will be serialized
    private ArrayList<MetaProperty<?>> findSerializedMetaProperties(Bean bean) {
        var metaBean = bean.metaBean();
        var metaProperties = new ArrayList<MetaProperty<?>>(metaBean.metaPropertyCount());
        for (var metaProperty : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProperty)) {
                metaProperties.add(metaProperty);
            }
        }
        return metaProperties;
    }

    // writes the bean values without property names, effectively referring back to the original definition
    private void writeBeanValues(Bean bean, List<MetaProperty<?>> metaProperties) throws IOException {
        // note that the declared type is not used to refine the output, creating separation of types in the binary form
        var beanClass = bean.getClass();
        output.writeArrayHeader(metaProperties.size());
        for (var metaProperty : metaProperties) {
            if (settings.isSerialized(metaProperty)) {
                var resolvedType = metaProperty.propertyResolvedType(beanClass);
                var childPropertyName = metaProperty.name();
                var value = metaProperty.get(bean);
                writeObject(resolvedType, childPropertyName, value);
            }
        }
    }

    //-----------------------------------------------------------------------
    // writes a string
    private void writeString(String str) throws IOException {
        if (str.length() >= MIN_LENGTH_STR_VALUE) {
            var ref = valueDefinitions.get(str);
            if (ref == null) {
                valueDefinitions.put(str, valueDefinitionIndex++);
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
    private void writeSimple(String propertyName, Object value) throws IOException {
        // note that the declared type is not used to refine the output, creating separation of types in the binary form
        // write the reference, or call Joda-Convert if first time value is seen
        var ref = valueDefinitions.get(value);
        if (ref == null) {
            // always write the type (by passing OBJECT), as it could be read while skipping where the type is not known
            output.writeValueDefinitionHeader();
            writeJodaConvert(ResolvedType.OBJECT, propertyName, value);
            valueDefinitions.put(value, valueDefinitionIndex++);
        } else {
            output.writeValueReference(ref);
        }
    }

    // writes the object as a String using Joda-Convert
    private void writeJodaConvert(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        try {
            // handle situations where there is no declared type, or there is a subclass of the declared type
            var effectiveType = value.getClass();
            var converter = settings.getConverter().findTypedConverterNoGenerics(effectiveType);
            if (effectiveType != declaredType.toBoxed().getRawType()) {
                effectiveType = converter.getEffectiveType();
                writeTypeNameOrReference(effectiveType);
            }
            var converted = converter.convertToString(value);
            if (converted == null) {
                output.writeNull();
                return;
            }
            writeString(converted);
        } catch (RuntimeException ex) {
            throw invalidConversionMsg(propertyName, value, ex);
        }
    }

    private IllegalArgumentException invalidConversionMsg(String propertyName, Object value, RuntimeException ex) {
        var msg = "Unable to write property '" + propertyName + "', type " +
                value.getClass().getName() + " could not be converted to a String";
        return new IllegalArgumentException(msg, ex);
    }

    // writes the type header
    private final void writeTypeNameOrReference(Class<?> type) throws IOException {
        var ref = typeDefinitions.get(type);
        if (ref == null) {
            var encodedClassName = SerTypeMapper.encodeType(type, settings, basePackage, knownTypes);
            typeDefinitions.put(type, typeDefinitionIndex++);
            output.writeTypeName(encodedClassName);
        } else {
            output.writeTypeReference(ref);
        }
    }

    //-------------------------------------------------------------------------
    private static interface BinHandler<T> {
        public abstract void handle(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException;
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
                return (BinHandler<Optional<?>>) BaseBinHandlers::writeOptional;
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
                    return (BinHandler<Object>) BaseBinHandlers::writePrimitiveArray;
                } else {
                    return (BinHandler<Object[]>) BaseBinHandlers::writeArray;
                }
            }
            if (Map.class.isAssignableFrom(type)) {
                return (BinHandler<Map<?, ?>>) BaseBinHandlers::writeMap;
            }
            if (Collection.class.isAssignableFrom(type)) {
                return (BinHandler<Collection<?>>) BaseBinHandlers::writeCollection;
            }
            if (Iterable.class.isAssignableFrom(type)) {
                return (BinHandler<Iterable<?>>) BaseBinHandlers::writeIterable;
            }
            return (writer, declaredType, propertyName, value) -> writer.writeSimple(propertyName, value);
        }

        // writes an optional, with meta type information if necessary
        private static void writeOptional(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            // write actual type
            if (!Optional.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_OPTIONAL);
            }
            // write content, using an array of size 0 or 1
            var valueType = declaredType.getArgumentOrDefault(0);
            if (opt.isEmpty()) {
                writer.output.writeArrayHeader(0);
            } else {
                writer.output.writeArrayHeader(1);
                writer.writeObject(valueType, "", opt.get());
            }
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
                if (actualComponentType == String.class) {
                    writer.output.writeTypeReference(BeanPack.TYPE_CODE_STRING_ARRAY);
                } else if (actualComponentType == Object.class) {
                    writer.output.writeTypeReference(BeanPack.TYPE_CODE_OBJECT_ARRAY);
                } else {
                    writeArrayTypeDescription(writer, array.getClass());
                }
            }
            // write content
            var componentType = declaredType.toComponentTypeOrDefault();
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
            var handler = JodaBeanPackedBinWriter.LOOKUP.get(arrayType.getComponentType());
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

        // writes an iterable, with meta type information if necessary
        private static void writeIterable(
                JodaBeanPackedBinWriter writer,
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
            var itemType = declaredType.getArgumentOrDefault(0);
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

            var keyType = declaredType.getArgumentOrDefault(0);
            var valueType = declaredType.getArgumentOrDefault(1);
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
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return (BinHandler<com.google.common.base.Optional<?>>) GuavaBinHandlers::writeOptional;
            }
            if (Multimap.class.isAssignableFrom(type)) {
                return (BinHandler<Multimap<?, ?>>) GuavaBinHandlers::writeMultimap;
            }
            if (Multiset.class.isAssignableFrom(type)) {
                return (BinHandler<Multiset<?>>) GuavaBinHandlers::writeMultiset;
            }
            if (Table.class.isAssignableFrom(type)) {
                return (BinHandler<Table<?, ?, ?>>) GuavaBinHandlers::writeTable;
            }
            if (BiMap.class.isAssignableFrom(type)) {
                return (BinHandler<BiMap<?, ?>>) GuavaBinHandlers::writeBiMap;
            }
            return super.createHandler(type);
        }

        // writes an optional, with meta type information if necessary
        private static void writeOptional(
                JodaBeanPackedBinWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) throws IOException {

            // write actual type
            if (!com.google.common.base.Optional.class.isAssignableFrom(declaredType.getRawType())) {
                writer.output.writeTypeReference(BeanPack.TYPE_CODE_OPTIONAL);
            }
            // write content, using an array of size 0 or 1
            var valueType = declaredType.getArgumentOrDefault(0);
            if (opt.isPresent()) {
                writer.output.writeArrayHeader(1);
                writer.writeObject(valueType, "", opt.get());
            } else {
                writer.output.writeArrayHeader(0);
            }
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
            // write content, using a map of key to array of values
            var keyType = declaredType.getArgumentOrDefault(0);
            var valueType = declaredType.getArgumentOrDefault(1);
            var map = mmap.asMap();
            writer.output.writeMapHeader(map.size());
            for (var entry : map.entrySet()) {
                writer.writeObject(keyType, "", entry.getKey());
                var values = entry.getValue();
                writer.output.writeArrayHeader(values.size());
                for (var value : values) {
                    writer.writeObject(valueType, "", value);
                }
            }
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
            // write content, using a map of value to count
            var valueType = declaredType.getArgumentOrDefault(0);
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
            // write content, using a map of row to map of column to value
            var rowType = declaredType.getArgumentOrDefault(0);
            var columnType = declaredType.getArgumentOrDefault(1);
            var valueType = declaredType.getArgumentOrDefault(2);
            var rowMap = table.rowMap();
            writer.output.writeMapHeader(rowMap.size());
            for (var rowEntry : rowMap.entrySet()) {
                writer.writeObject(rowType, "", rowEntry.getKey());
                writer.output.writeMapHeader(rowEntry.getValue().size());
                for (var colEntry : rowEntry.getValue().entrySet()) {
                    writer.writeObject(columnType, "", colEntry.getKey());
                    writer.writeObject(valueType, "", colEntry.getValue());
                }
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
                // hack around Guava annoyance by assuming that size 0 and 1 ImmutableBiMap
                // was actually meant to be an ImmutableMap
                if ((declaredType.getRawType() != Map.class && declaredType.getRawType() != ImmutableMap.class) || biMap.size() >= 2) {
                    writer.output.writeTypeReference(BeanPack.TYPE_CODE_BIMAP);
                } else if (!Map.class.isAssignableFrom(declaredType.getRawType())) {
                    writer.output.writeTypeReference(BeanPack.TYPE_CODE_MAP);
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
                return (BinHandler<Grid<?>>) CollectBinHandlers::writeGrid;
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
            var valueType = declaredType.getArgumentOrDefault(0);
            var rowCount = grid.rowCount();
            var colCount = grid.columnCount();
            var totalSize = rowCount * colCount;
            var gridSize = grid.size();
            var sparse = gridSize <= (totalSize / 3d);
            writer.output.writeArrayHeader(3);
            writer.output.writeInt(sparse ? rowCount : -rowCount);
            writer.output.writeInt(colCount);
            if (sparse) {
                // sparse
                writer.output.writeArrayHeader(gridSize * 3);
                for (var cell : grid.cells()) {
                    writer.output.writeInt(cell.getRow());
                    writer.output.writeInt(cell.getColumn());
                    writer.writeObject(valueType, "", cell.getValue());
                }
            } else {
                // dense
                writer.output.writeArrayHeader(totalSize);
                for (var row = 0; row < rowCount; row++) {
                    for (var column = 0; column < colCount; column++) {
                        writer.writeObject(valueType, "", grid.get(row, column));
                    }
                }
            }
        }
    }
}
