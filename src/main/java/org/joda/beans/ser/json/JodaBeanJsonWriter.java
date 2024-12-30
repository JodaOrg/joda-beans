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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.ResolvedType;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTypeMapper;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;
import org.joda.convert.ToStringConverter;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Table;

/**
 * Provides the ability for a Joda-Bean to be written to JSON.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The JSON format is kept relatively natural, however some meta-data is added.
 * This has the unfortunate effect of adding an additional object structure to
 * hold the type in a few places.
 * <p>
 * Beans are output using JSON objects where the key is the property name.
 * The type of the bean will be sent using the '&#64;type' property name if necessary.
 * <p>
 * Most simple types, defined by Joda-Convert, are output as JSON strings.
 * If the simple type requires additional type information, the value is replaced by
 * a JSON object containing the keys '&#64;type' and 'value'.
 * <p>
 * Null values are generally omitted, but where included are sent as 'null'.
 * Boolean values are sent as 'true' and 'false'.
 * Integer and Double values are sent as JSON numbers.
 * Other numeric types are also sent as numbers but may have additional type information.
 * <p>
 * Collections are output using JSON objects or arrays.
 * Multisets are output as a map of value to count.
 * <p>
 * If a collection contains a collection then addition meta-type information is
 * written to aid with deserialization.
 * At this level, the data read back may not be identical to that written.
 * If the collection type requires additional type information, the value is replaced by
 * a JSON object containing the keys '&#64;meta' and 'value'.
 * <p>
 * Type names are shortened by the package of the root type if possible.
 * Certain basic types are also handled, such as String, Integer, File and URI.
 */
public class JodaBeanJsonWriter {

    /**
     * JSON bean type attribute.
     */
    static final String BEAN = "@bean";
    /**
     * JSON simple type attribute.
     */
    static final String TYPE = "@type";
    /**
     * JSON meta-type attribute.
     */
    static final String META = "@meta";
    /**
     * JSON value attribute.
     */
    static final String VALUE = "value";

    // why is there an ugly ClassValue setup here?
    // because this is O(1) whereas switch with pattern match which is O(n)
    private static final ClassValue<JsonHandler<Object>> LOOKUP = new ClassValue<>() {

        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        @Override
        protected JsonHandler computeValue(Class<?> type) {
            if (Bean.class.isAssignableFrom(type)) {
                return (JsonHandler<Bean>) JodaBeanJsonWriter::writeBeanMaybeSimple;
            }
            if (type == String.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeString((String) value);
            }
            if (type == Long.class || type == long.class) {
                return (writer, declaredType, propName, value) -> writer.writeLong(declaredType, (Long) value);
            }
            if (type == Integer.class || type == int.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeInt((Integer) value);
            }
            if (type == Short.class || type == short.class) {
                return (writer, declaredType, propName, value) -> writer.writeShort(declaredType, (Short) value);
            }
            if (type == Byte.class || type == byte.class) {
                return (writer, declaredType, propName, value) -> writer.writeByte(declaredType, (Byte) value);
            }
            if (type == Double.class || type == double.class) {
                return (writer, declaredType, propName, value) -> writer.writeDouble(declaredType, (Double) value);
            }
            if (type == Float.class || type == float.class) {
                return (writer, declaredType, propName, value) -> writer.writeFloat(declaredType, (Float) value);
            }
            if (type == Boolean.class || type == boolean.class) {
                return (writer, declaredType, propName, value) -> writer.output.writeBoolean((Boolean) value);
            }
            return BaseJsonHandlers.INSTANCE.computeValue(type);
        }
    };

    /**
     * The settings to use.
     */
    final JodaBeanSer settings;  // CSIGNORE
    /**
     * The outputter.
     */
    JsonOutput output;  // CSIGNORE
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
     */
    public JodaBeanJsonWriter(JodaBeanSer settings) {
        // COMPATIBLE_V2 value is eliminated here and in the subclass
        JodaBeanUtils.notNull(settings, "settings");
        if (settings.getJsonNumberFormat() == JodaBeanJsonNumberFormat.COMPATIBLE_V2) {
            this.settings = settings.withJsonNumberFormat(JodaBeanJsonNumberFormat.STRING);
        } else {
            this.settings = settings;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to a string.
     * <p>
     * The type of the bean will be set in the message.
     * 
     * @param bean  the bean to output, not null
     * @return the JSON, not null
     */
    public String write(Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to a string specifying whether to include the type at the root.
     * 
     * @param bean  the bean to output, not null
     * @param includeRootType  true to output the root type
     * @return the JSON, not null
     */
    public String write(Bean bean, boolean includeRootType) {
        var buf = new StringBuilder(1024);
        try {
            write(bean, includeRootType, buf);
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
        write(bean, true, output);
    }

    /**
     * Writes the bean to the {@code Appendable} specifying whether to include the type at the root.
     * 
     * @param bean  the bean to output, not null
     * @param includeRootType  true to output the root type
     * @param output  the output appendable, not null
     * @throws IOException if an error occurs
     */
    public void write(Bean bean, boolean includeRootType, Appendable output) throws IOException {
        JodaBeanUtils.notNull(bean, "bean");
        JodaBeanUtils.notNull(output, "output");
        this.output = new JsonOutput(output, settings.getJsonNumberFormat(), settings.getIndent(), settings.getNewLine());
        var rootType = includeRootType ? ResolvedType.OBJECT : ResolvedType.of(bean.getClass());
        // root always outputs the bean, not Joda-Convert form
        writeBean(rootType, bean, includeRootType);
        output.append(settings.getNewLine());
    }

    //-----------------------------------------------------------------------
    // walk an object, by determining the runtime type
    void writeObject(ResolvedType declaredType, String propertyName, Object value) throws IOException {
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
        output.writeObjectStart();
        writeBeanType(declaredType, bean, isRoot);
        writeBeanProperties(declaredType, bean);
        output.writeObjectEnd();
    }

    // optionally writes the type of the bean
    void writeBeanType(ResolvedType declaredType, Bean bean, boolean includeRootType) throws IOException {
        if (bean.getClass() != declaredType.getRawType()) {
            var typeStr = SerTypeMapper.encodeType(bean.getClass(), settings, basePackage, knownTypes);
            if (includeRootType) {
                basePackage = bean.getClass().getPackage().getName() + ".";
            }
            output.writeObjectKeyValue(BEAN, typeStr);
        }
    }

    // optionally writes the type of the bean
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
    void writeLong(ResolvedType declaredType, Long val) throws IOException {
        if (declaredType.getRawType() == long.class) {
            output.writeLong(val);
        } else {
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, "Long");
            output.writeObjectKey(VALUE);
            output.writeLong(val);
            output.writeObjectEnd();
        }
    }

    void writeShort(ResolvedType declaredType, Short val) throws IOException {
        if (declaredType.getRawType() == short.class) {
            output.writeInt(val);
        } else {
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, "Short");
            output.writeObjectKey(VALUE);
            output.writeInt(val);
            output.writeObjectEnd();
        }
    }

    void writeByte(ResolvedType declaredType, Byte val) throws IOException {
        if (declaredType.getRawType() == byte.class) {
            output.writeInt(val);
        } else {
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, "Byte");
            output.writeObjectKey(VALUE);
            output.writeInt(val);
            output.writeObjectEnd();
        }
    }

    void writeDouble(ResolvedType declaredType, Double val) throws IOException {
        if (declaredType.getRawType() == double.class || (!Double.isNaN(val) && !Double.isInfinite(val))) {
            output.writeDouble(val);
        } else {
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, "Double");
            output.writeObjectKey(VALUE);
            output.writeDouble(val);
            output.writeObjectEnd();
        }
    }

    void writeFloat(ResolvedType declaredType, Float val) throws IOException {
        if (declaredType.getRawType() == float.class) {
            output.writeFloat(val);
        } else {
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, "Float");
            output.writeObjectKey(VALUE);
            output.writeFloat(val);
            output.writeObjectEnd();
        }
    }

    // writes a simple type
    void writeSimple(ResolvedType declaredType, String propertyName, Object value) throws IOException {
        // handle no declared type and subclasses
        Class<?> realType = value.getClass();
        Class<?> effectiveType = declaredType.getRawType();
        var requiresClose = false;
        if (effectiveType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                var typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                output.writeObjectStart();
                output.writeObjectKeyValue(TYPE, typeStr);
                output.writeObjectKey(VALUE);
                requiresClose = true;
            } else {
                effectiveType = realType;
            }
        } else if (!settings.getConverter().isConvertible(effectiveType)) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            var typeStr = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
            output.writeObjectStart();
            output.writeObjectKeyValue(TYPE, typeStr);
            output.writeObjectKey(VALUE);
            requiresClose = true;
        }

        writeJodaConvert(declaredType, propertyName, value);

        // close open map
        if (requiresClose) {
            output.writeObjectEnd();
        }
    }

    // writes using Joda-Convert
    void writeJodaConvert(ResolvedType declaredType, String propertyName, Object value) throws IOException {
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

    // writes a map given map entries, code shared with Multimap
    <K, V> void writeMapEntries(
            ResolvedType declaredType,
            String propertyName,
            Collection<Map.Entry<K, V>> mapEntries) throws IOException {

        // if key type is known and convertible use short key format, else use full bean format
        var keyType = toWeakenedType(declaredType.getArgumentOrDefault(0));
        var keyConverterOpt = settings.getConverter().converterFor(keyType.getRawType());
        if (keyConverterOpt.isPresent()) {
            writeMapEntriesSimple(declaredType, propertyName, mapEntries, keyType, keyConverterOpt.get().withoutGenerics());
        } else {
            writeMapComplex(declaredType, propertyName, mapEntries, keyType);
        }
    }

    private <K, V> void writeMapEntriesSimple(
            ResolvedType declaredType,
            String propertyName,
            Collection<Map.Entry<K, V>> mapEntries,
            ResolvedType keyType,
            ToStringConverter<Object> keyConverter) throws IOException {

        var valueType = toWeakenedType(declaredType.getArgumentOrDefault(1));
        output.writeObjectStart();
        for (var entry : mapEntries) {
            var key = entry.getKey();
            if (key == null) {
                throw invalidNullMapKey(propertyName);
            }
            var str = keyConverter.convertToString(key);
            if (str == null) {
                throw invalidConvertedNullMapKey(propertyName);
            }
            output.writeObjectKey(str);
            writeObject(valueType, "", entry.getValue());
        }
        output.writeObjectEnd();
    }

    private <K, V> void writeMapComplex(
            ResolvedType declaredType,
            String propertyName,
            Collection<Map.Entry<K, V>> mapEntries,
            ResolvedType keyType) throws IOException {

        var valueType = toWeakenedType(declaredType.getArgumentOrDefault(1));
        output.writeArrayStart();
        for (var entry : mapEntries) {
            var key = entry.getKey();
            if (key == null) {
                throw invalidNullMapKey(propertyName);
            }
            output.writeArrayItemStart();
            output.writeArrayStart();
            output.writeArrayItemStart();
            writeObject(keyType, "", key);
            output.writeArrayItemStart();
            writeObject(valueType, "", entry.getValue());
            output.writeArrayEnd();
        }
        output.writeArrayEnd();
    }

    static IllegalArgumentException invalidNullMapKey(String propertyName) {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "', map key must not be null");
    }

    static IllegalArgumentException invalidConvertedNullMapKey(String propertyName) {
        return new IllegalArgumentException(
                "Unable to write property '" + propertyName + "', converted map key must not be null");
    }

    //-------------------------------------------------------------------------
    // gets the weakened type, which exists for backwards compatibility
    // once the parser can handle ResolvedType this method can, in theory, be removed
    static ResolvedType toWeakenedType(ResolvedType base) {
        for (var arg : base.getArguments()) {
            var rawType = arg.getRawType();
            if (LOOKUP.get(rawType).isCollection()) {
                return base.toRawType();
            }
        }
        return base;
    }

    //-------------------------------------------------------------------------
    // handler for meta types, but with IOException
    static interface MetaTypeHandler {
        public abstract String handle() throws IOException;
    }

    // handler for meta type content, but with IOException
    static interface ContentHandler {
        public abstract void handle() throws IOException;
    }

    // writes content with a meta type
    void writeWithMetaType(ContentHandler contentHandler, MetaTypeHandler metaTypeHandler) throws IOException {
        var metaTypeName = metaTypeHandler.handle();
        if (metaTypeName != null) {
            output.writeObjectStart();
            output.writeObjectKeyValue(META, metaTypeName);
            output.writeObjectKey(VALUE);
            contentHandler.handle();
            output.writeObjectEnd();
        } else {
            contentHandler.handle();
        }
    }

    // writes content with a meta type
    void writeWithMetaType(ContentHandler contentHandler, Class<?> cls, ResolvedType declaredType, String metaTypeName)
            throws IOException {

        if (!cls.isAssignableFrom(declaredType.getRawType())) {
            output.writeObjectStart();
            output.writeObjectKeyValue(META, metaTypeName);
            output.writeObjectKey(VALUE);
            contentHandler.handle();
            output.writeObjectEnd();
        } else {
            contentHandler.handle();
        }
    }

    //-------------------------------------------------------------------------
    private static interface JsonHandler<T> {
        public abstract void handle(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException;

        public default void handleProperty(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                T obj) throws IOException {

            writer.output.writeObjectKey(propertyName);
            handle(writer, declaredType, propertyName, obj);
        }

        public default boolean isCollection() {
            return false;
        }
    }

    private static interface CollectionJsonHandler<T> extends JsonHandler<T> {
        @Override
        public default boolean isCollection() {
            return true;
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
            if (type == Optional.class) {
                return OptionalJsonHandler.INSTANCE;
            }
            if (type.isArray()) {
                var componentType = type.getComponentType();
                if (componentType.isPrimitive()) {
                    if (componentType == byte.class) {
                        return JodaBeanJsonWriter::writeSimple;
                    } else {
                        return (CollectionJsonHandler<Object>) BaseJsonHandlers::writePrimitiveArray;
                    }
                } else {
                    return (CollectionJsonHandler<Object[]>) BaseJsonHandlers::writeArray;
                }
            }
            if (Map.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Map<?, ?>>) BaseJsonHandlers::writeMap;
            }
            if (Iterable.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Collection<?>>) BaseJsonHandlers::writeCollection;
            }
            return JodaBeanJsonWriter::writeSimple;
        }

        // writes an array, with meta type information if necessary
        private static void writeArray(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object[] array) throws IOException {

            var arrayComponentType = array.getClass().getComponentType();
            // check actual type
            MetaTypeHandler metaTypeHandler = () -> {
                if (declaredType.getRawType() != array.getClass()) {
                    return metaTypeArrayName(arrayComponentType);
                }
                return null;
            };
            // write content
            ContentHandler contentHandler = () -> {
                var componentType = toWeakenedType(ResolvedType.of(arrayComponentType));
                writer.output.writeArrayStart();
                for (var item : array) {
                    writer.output.writeArrayItemStart();
                    writer.writeObject(componentType, "", item);
                }
                writer.output.writeArrayEnd();
            };
            writer.writeWithMetaType(contentHandler, metaTypeHandler);
        }

        // writes a primitive array, with meta type information if necessary
        private static void writePrimitiveArray(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Object array) throws IOException {

            var arrayComponentType = array.getClass().getComponentType();
            // check actual type
            MetaTypeHandler metaTypeHandler = () -> {
                if (declaredType.getRawType() != array.getClass()) {
                    return metaTypeArrayName(arrayComponentType);
                }
                return null;
            };
            // write content
            ContentHandler contentHandler = () -> {
                var componentType = ResolvedType.of(arrayComponentType);
                var handler = JodaBeanJsonWriter.LOOKUP.get(arrayComponentType);
                var arrayLength = Array.getLength(array);
                writer.output.writeArrayStart();
                for (int i = 0; i < arrayLength; i++) {
                    writer.output.writeArrayItemStart();
                    handler.handle(writer, componentType, propertyName, Array.get(array, i));
                }
                writer.output.writeArrayEnd();
            };
            writer.writeWithMetaType(contentHandler, metaTypeHandler);
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

        // writes a collection
        private static void writeCollection(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Iterable<?> coll) throws IOException {

            // check actual type
            MetaTypeHandler metaTypeHandler = () -> {
                if (coll instanceof Set && !Set.class.isAssignableFrom(declaredType.getRawType())) {
                    return "Set";
                } else if (coll instanceof List && !List.class.isAssignableFrom(declaredType.getRawType())) {
                    return "List";
                } else if (!Collection.class.isAssignableFrom(declaredType.getRawType())) {
                    return "Collection";
                }
                return null;
            };
            // write content, using an array
            ContentHandler contentHandler = () -> {
                var itemType = toWeakenedType(declaredType.getArgumentOrDefault(0));
                writer.output.writeArrayStart();
                for (var item : coll) {
                    writer.output.writeArrayItemStart();
                    writer.writeObject(itemType, "", item);
                }
                writer.output.writeArrayEnd();
            };
            writer.writeWithMetaType(contentHandler, metaTypeHandler);
        }

        // writes a map, with meta type information if necessary
        private static void writeMap(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Map<?, ?> map) throws IOException {

            // write content, using a map
            ContentHandler contentHandler = () -> writer.writeMapEntries(declaredType, propertyName, map.entrySet());
            writer.writeWithMetaType(contentHandler, Map.class, declaredType, "Map");
        }
    }

    //-------------------------------------------------------------------------
    private static sealed class GuavaJsonHandlers extends BaseJsonHandlers {

        @Override
        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Multimap.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Multimap<?, ?>>) GuavaJsonHandlers::writeMultimap;
            }
            if (Multiset.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Multiset<?>>) GuavaJsonHandlers::writeMultiset;
            }
            if (Table.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Table<?, ?, ?>>) GuavaJsonHandlers::writeTable;
            }
            if (BiMap.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<BiMap<?, ?>>) GuavaJsonHandlers::writeBiMap;
            }
            if (com.google.common.base.Optional.class.isAssignableFrom(type)) {
                return GuavaOptionalJsonHandler.INSTANCE;
            }
            return super.computeValue(type);
        }

        // writes a multimap
        private static void writeMultimap(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multimap<?, ?> mmap) throws IOException {

            // check actual type
            MetaTypeHandler metaTypeHandler = () -> {
                if (mmap instanceof SetMultimap && !SetMultimap.class.isAssignableFrom(declaredType.getRawType())) {
                    return "SetMultimap";
                } else if (mmap instanceof ListMultimap && !ListMultimap.class.isAssignableFrom(declaredType.getRawType())) {
                    return "ListMultimap";
                } else if (!Multimap.class.isAssignableFrom(declaredType.getRawType())) {
                    return "Multimap";
                }
                return null;
            };
            // write content, using a map
            ContentHandler contentHandler = () -> writer.writeMapEntries(declaredType, propertyName, mmap.entries());
            writer.writeWithMetaType(contentHandler, metaTypeHandler);
        }

        // writes a multiset
        private static void writeMultiset(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Multiset<?> mset) throws IOException {

            // write content, using an array of value to count
            ContentHandler contentHandler = () -> {
                var valueType = toWeakenedType(declaredType.getArgumentOrDefault(0));
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
            };
            writer.writeWithMetaType(contentHandler, Multiset.class, declaredType, "Multiset");
        }

        // writes a table
        private static void writeTable(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Table<?, ?, ?> table) throws IOException {

            // write content, using an array of cells
            ContentHandler contentHandler = () -> {
                var rowType = toWeakenedType(declaredType.getArgumentOrDefault(0));
                var columnType = toWeakenedType(declaredType.getArgumentOrDefault(1));
                var valueType = toWeakenedType(declaredType.getArgumentOrDefault(2));
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
            };
            writer.writeWithMetaType(contentHandler, Table.class, declaredType, "Table");
        }

        // writes a BiMap, with meta type information if necessary
        private static void writeBiMap(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                BiMap<?, ?> biMap) throws IOException {

            MetaTypeHandler metaTypeHandler = () -> {
                if (!BiMap.class.isAssignableFrom(declaredType.getRawType())) {
                    // hack around Guava annoyance by assuming that size 0 and 1 ImmutableBiMap
                    // was actually meant to be an ImmutableMap
                    if ((declaredType.getRawType() != Map.class && declaredType.getRawType() != ImmutableMap.class) || biMap.size() >= 2) {
                        return "BiMap";
                    } else if (!Map.class.isAssignableFrom(declaredType.getRawType())) {
                        return "Map";
                    }
                }
                return null;
            };
            ContentHandler contentHandler = () -> writer.writeMapEntries(declaredType, propertyName, biMap.entrySet());
            writer.writeWithMetaType(contentHandler, metaTypeHandler);
        }
    }

    //-------------------------------------------------------------------------
    private static final class CollectJsonHandlers extends GuavaJsonHandlers {

        @Override
        @SuppressWarnings("rawtypes")  // sneaky use of raw type to allow typed value in each method below
        JsonHandler computeValue(Class<?> type) {
            if (Grid.class.isAssignableFrom(type)) {
                return (CollectionJsonHandler<Grid<?>>) CollectJsonHandlers::writeGrid;
            }
            return super.computeValue(type);
        }

        private static void writeGrid(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Grid<?> grid) throws IOException {

            // write grid using sparse approach
            ContentHandler contentHandler = () -> {
                var valueType = toWeakenedType(declaredType.getArgumentOrDefault(0));
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
            };
            writer.writeWithMetaType(contentHandler, Grid.class, declaredType, "Grid");
        }
    }

    //-------------------------------------------------------------------------
    static final class OptionalJsonHandler implements JsonHandler<Optional<?>> {
        private static final OptionalJsonHandler INSTANCE = new OptionalJsonHandler();

        // when Optional is not a property, it is processed as a kind of collection
        @Override
        public void handle(
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                Optional<?> opt) throws IOException {

            var valueType = declaredType.getArgumentOrDefault(0);
            writer.writeObject(valueType, "", opt.orElse(null));
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanJsonWriter writer,
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
                JodaBeanJsonWriter writer,
                ResolvedType declaredType,
                String propertyName,
                com.google.common.base.Optional<?> opt) throws IOException {

            var valueType = declaredType.getArgumentOrDefault(0);
            writer.writeObject(valueType, "", opt.orNull());
        }

        // when Optional is a property, it is ignored if empty
        @Override
        public void handleProperty(
                JodaBeanJsonWriter writer,
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
