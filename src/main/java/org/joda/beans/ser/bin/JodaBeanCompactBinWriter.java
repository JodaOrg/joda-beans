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

import static java.util.Collections.reverseOrder;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerDeserializer;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for an immutable Joda-Bean to be written to a compact binary format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * This class only supports serialization of instances of ImmutableBean and other basic types, if any mutable beans
 * are encountered during traversal an exception will be thrown.
 * <p>
 * An initial pass of the bean is used to build up a map of unique immutable beans and unique immutable instances of
 * other classes (based on an equality check).
 * Then the class and property names for each bean class is serialized up front as a map of class name to list of
 * property names, along with class information for any class where type information would be required when parsing
 * and is not available on the metabean for the enclosing bean object.
 * <p>
 * The binary format is based on MessagePack v2.0.
 * Each unique immutable bean is output as a list of each property value using the fixed property order previously
 * serialized.
 * Subsequent instances of unique objects (defined by an equality check) are replaced by references to the first
 * serialized instance.
 * <p>
 * Most simple types, defined by Joda-Convert, are output as MessagePack strings.
 * However, MessagePack nil, boolean, float, integral and bin types are also used
 * for null, byte[] and the Java numeric primitive types (excluding char).
 * <p>
 * Beans are output using MessagePack arrays where the first element is a reference to the initial class map.
 * Collections are output using MessagePack maps or arrays.
 * Multisets are output as a map of value to count.
 * Items which have other references are serialized with an integer key, which is then repeated at subsequent
 * appearances.
 * <p>
 * If a collection contains a collection then addition meta-type information is written to aid with deserialization.
 * At this level, the data read back may not be identical to that written.
 * <p>
 * The Java type names are sent using an 'ext' entity.
 * Five 'ext' types are used, one each for beans, meta-type and simple, reference keys and reference lookups.
 * The class name is passed as the 'ext' data.
 * The 'ext' value is sent as the first item in an array of property values for beans, an integer referring to the
 * location in the initial class mapping.
 * Where the additional type information is not about a bean, a tuple is written using a size 1 map where the key is
 * the 'ext' data and the value is the data being annotated.
 * <p>
 * For references, when an object will be referred back to it is written as a map of size one with 'ext' as the key
 * and the object that should be referred to as the value.
 * When that same object is referred back to it is written as 'ext' with the data from the initial 'ext'.
 * <p>
 * Certain basic types are also handled, such as String, Integer, File and URI.
 *
 * @author Will Nicholson
 */
public class JodaBeanCompactBinWriter extends AbstractBinWriter {

    /**
     * The classes that are being serialized.
     */
    private Map<Class<?>, ClassInfo> classes = new HashMap<>();
    /**
     * The amount of time each class needs to have its type serialized.
     */
    private Map<Class<?>, Integer> classSerializationCount = new HashMap<>();
    /**
     * The serialized objects that are repeated and require references.
     */
    private Map<Object, Ref> refs = new HashMap<>();

    /**
     * Creates an instance.
     *
     * @param settings the settings to use, not null
     */
    public JodaBeanCompactBinWriter(final JodaBeanSer settings) {
        super(settings);
    }

    //-----------------------------------------------------------------------

    /**
     * Writes the bean to an array of bytes.
     *
     * @param bean the bean to output, not null
     * @return the binary data, not null
     */
    public byte[] write(ImmutableBean bean) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try {
            write(bean, baos);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return baos.toByteArray();
    }

    /**
     * Writes the bean to the {@code OutputStream}.
     *
     * @param bean the bean to output, not null
     * @param output the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(ImmutableBean bean, OutputStream output) throws IOException {
        if (bean == null) {
            throw new NullPointerException("bean");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.output = new MsgPackOutput(output);
        writeRoot(bean);
    }
    //-----------------------------------------------------------------------

    private void writeRoot(ImmutableBean bean) throws IOException {
        output.writeArrayHeader(4); // 4 items: Version, Ref Count, Class info, Root Bean
        output.writeInt(2);  // version 2 - regular binary format is version 1

        writeClassDescriptions(bean);

        writeRootBean(bean, true);
    }

    private void writeClassDescriptions(ImmutableBean root) throws IOException {
        buildClassAndRefMap(root);

        // Write out ref count first
        int size = refs.size();
        if (size == Integer.MAX_VALUE) {
            throw new RuntimeException("Bean has at least Integer.MAX_VALUE repeated objects and cannot be serialized: " + size);
        }
        output.writeInt(size);

        int classCount = classes.size();
        if (classCount == Integer.MAX_VALUE) {
            throw new RuntimeException("Bean has at least Integer.MAX_VALUE class names and cannot be serialized: " + classCount);
        }

        // Reorder classes so the most repeated serialized names have the lowest number (hence the shortest extension)
        List<ClassInfo> classInfos = classSerializationCount.entrySet().stream()
                .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
                .map(entry -> classes.get(entry.getKey()))
                .collect(toList());

        classes = new HashMap<>();
        for (int position = 0; position < classInfos.size(); position++) {
            ClassInfo classInfo = classInfos.get(position);
            classes.put(classInfo.type, new ClassInfo(position, classInfo.type, classInfo.metaProperties));
        }

        // Write map of class name to a list of metatype names (which is empty if not a bean)
        output.writeMapHeader(classCount);

        for (ClassInfo classInfo : classInfos) {
            // Known types parameter is null as we never serialise the class names again
            String className = SerTypeMapper.encodeType(classInfo.type, settings, null, null);
            output.writeString(className);

            output.writeArrayHeader(classInfo.metaProperties.length);
            for (MetaProperty<?> property : classInfo.metaProperties) {
                output.writeString(property.name());
            }
        }
    }

    private void buildClassAndRefMap(ImmutableBean root) {
        Map<Object, Integer> objects = new HashMap<>();

        classes.put(root.getClass(), classInfoFromMetaBean(root.metaBean(), root.getClass()));
        classSerializationCount.put(root.getClass(), 1);

        addClasses(root, root.getClass(), objects);

        List<Map.Entry<Object, Integer>> refEntries = objects.entrySet().stream()
            .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
            .filter(entry -> entry.getValue() > 1)
            .collect(toList());

        for (Map.Entry<Object, Integer> entry : refEntries) {
            // Only add refs for objects that are repeated
            Object value = entry.getKey();
            Class<?> realType = value.getClass();

            // simple types do not need references
            if (realType != Integer.class && realType != Double.class && realType != Float.class &&
                realType != Boolean.class && realType != Long.class && realType != Short.class &&
                realType != Byte.class && realType != byte[].class) {

                refs.put(value, new Ref(false, refs.size()));
            }
        }
    }

    private void addClasses(Object base, Class<?> declaredClass, Map<Object, Integer> objects) {
        if (base == null) {
            return;
        }

        int result = objects.compute(base, JodaBeanCompactBinWriter::incrementOrOne);
        if (result > 1) {
            // Already checked class info for an equivalent object
            return;
        }

        addClassInfo(base, declaredClass);

        if (base instanceof Bean) {
            Bean bean = (Bean) base;
            if (settings.getConverter().isConvertible(bean.getClass())) {
                return;
            }

            for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
                if (shouldSerializeMetaProperty(prop)) {
                    Object value = prop.get(bean);
                    Class<?> type = SerOptional.extractType(prop, base.getClass());

                    if (value != null) {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                        if (itemIterator != null) {
                            if (itemIterator.metaTypeRequired()) {
                                objects.compute(itemIterator.metaTypeName(), JodaBeanCompactBinWriter::incrementOrOne);
                            }
                            addClassesIterable(itemIterator, objects);
                        } else {
                            addClasses(value, type, objects);
                        }
                    } else {
                        // In case it's a null value or optional field
                        addClassInfo(type, type);
                    }
                }
            }
        }
    }

    private static boolean shouldSerializeMetaProperty(MetaProperty<?> prop) {
        return prop.style().isSerializable() && !prop.style().isDerived();
    }

    // Used in Map#compute so we can initialise all the values to one and then increment
    private static int incrementOrOne(@SuppressWarnings("unused") Object k, Integer i) {
        return i == null ? 1 : i + 1;
    }

    private void addClassesIterable(SerIterator itemIterator, Map<Object, Integer> objects) {
        if (itemIterator.category() == SerCategory.MAP) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                addClasses(itemIterator.key(), itemIterator.keyType(), objects);
                addClasses(itemIterator.value(), itemIterator.valueType(), objects);
            }
        } else if (itemIterator.category() == SerCategory.COUNTED) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                addClasses(itemIterator.value(), itemIterator.valueType(), objects);
            }
        } else if (itemIterator.category() == SerCategory.TABLE) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                addClasses(itemIterator.key(), itemIterator.keyType(), objects);
                addClasses(itemIterator.column(), itemIterator.columnType(), objects);
                addClasses(itemIterator.value(), itemIterator.valueType(), objects);
            }
        } else if (itemIterator.category() == SerCategory.GRID) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                addClasses(itemIterator.value(), itemIterator.valueType(), objects);
            }
        } else {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                addClasses(itemIterator.value(), itemIterator.valueType(), objects);
            }
        }
    }

    private void addClassInfo(Object value, Class<?> declaredClass) {
        if (value instanceof Bean && !(value instanceof ImmutableBean)) {
            throw new IllegalArgumentException("Can only serialize immutable beans in compact binary format: " + value.getClass().getName());
        }

        if (value instanceof ImmutableBean) {
            boolean noNeedToSerializeTypeName = classes.containsKey(value.getClass()) && declaredClass.equals(value.getClass());
            if (noNeedToSerializeTypeName || settings.getConverter().isConvertible(value.getClass())) {
                return;
            }
            ImmutableBean bean = (ImmutableBean) value;
            ClassInfo classInfo = classInfoFromMetaBean(bean.metaBean(), bean.getClass());
            addClassInfoAndIncrementCount(bean.getClass(), classInfo);
        } else if (value instanceof Class<?>) {
            Class<?> type = (Class<?>) value;
            if (type.equals(declaredClass) || settings.getConverter().isConvertible(type)) {
                return;
            }
            if (Bean.class.isAssignableFrom(type) && !ImmutableBean.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Can only serialize immutable beans in compact binary format: " + type.getName());
            }
            if (ImmutableBean.class.isAssignableFrom(type)) {
                if (classes.containsKey(type)) {
                    classSerializationCount.compute(type, JodaBeanCompactBinWriter::incrementOrOne);
                    return;
                }
                SerDeserializer deser = settings.getDeserializers().findDeserializer(type);
                MetaBean metaBean = deser.findMetaBean(type);
                ClassInfo classInfo = classInfoFromMetaBean(metaBean, type);
                addClassInfoAndIncrementCount(type, classInfo);
            }
        } else if (declaredClass == Object.class && !value.getClass().equals(String.class)) {
            Class<?> effectiveType = settings.getConverter().findTypedConverter(value.getClass()).getEffectiveType();
            ClassInfo classInfo = new ClassInfo(0, value.getClass(), new MetaProperty<?>[0]);
            addClassInfoAndIncrementCount(effectiveType, classInfo);
        } else if (!settings.getConverter().isConvertible(declaredClass)) {
            ClassInfo classInfo = new ClassInfo(0, value.getClass(), new MetaProperty<?>[0]);
            addClassInfoAndIncrementCount(value.getClass(), classInfo);
        }
    }

    private void addClassInfoAndIncrementCount(Class<?> type, ClassInfo classInfo) {
        classes.putIfAbsent(type, classInfo);
        classSerializationCount.compute(type, JodaBeanCompactBinWriter::incrementOrOne);
    }

    private ClassInfo classInfoFromMetaBean(MetaBean metaBean, Class<?> aClass) {
        MetaProperty<?>[] metaProperties = StreamSupport.stream(metaBean.metaPropertyIterable().spliterator(), false)
            .filter(metaProp -> shouldSerializeMetaProperty(metaProp))
            .toArray(MetaProperty<?>[]::new);

        // Positions get recreated when all classes have been recorded
        return new ClassInfo(0, aClass, metaProperties);
    }

    //-----------------------------------------------------------------------

    @Override
    protected void writeBean(Bean bean, Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        Ref ref = refs.get(bean);
        if (ref != null) {
            if (ref.hasBeenSerialized) {
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
                return;
            }
            output.writeMapHeader(1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
        }

        ClassInfo classInfo = getClassInfo(bean.getClass());
        MetaProperty<?>[] props = classInfo.metaProperties;
        int count = props.length;
        Object[] values = new Object[count];
        int size = 0;
        for (MetaProperty<?> prop : props) {
            Object value = SerOptional.extractValue(prop, bean);
            values[size++] = value;
        }

        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            output.writeArrayHeader(size + 1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_BEAN, classInfo.position);
        } else {
            output.writeArrayHeader(size);
        }

        for (int i = 0; i < size; i++) {
            MetaProperty<?> prop = props[i];
            Object value = values[i];

            Class<?> propType = SerOptional.extractType(prop, bean.getClass());

            if (value == null) {
                output.writeNil();
                continue;
            }

            if (value instanceof Bean) {
                if (settings.getConverter().isConvertible(value.getClass())) {
                    writeSimple(propType, value);
                } else {
                    writeBean((Bean) value, propType, RootType.NOT_ROOT);
                }
            } else {
                SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                if (itemIterator != null) {
                    writeElements(itemIterator);
                } else {
                    writeSimple(propType, value);
                }
            }
        }
        if (ref != null) {
            refs.put(bean, new Ref(true, ref.position));
        }
    }

    @Override
    protected void writeMetaPropertyReference(String metaTypeName) throws IOException {
        Ref ref = refs.get(metaTypeName);
        if (ref != null) {
            if (ref.hasBeenSerialized) {
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_META, ref.position);
            } else {
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
                output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
                refs.put(metaTypeName, new Ref(true, ref.position));
            }
        } else {
            output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
        }
    }

    @Override
    protected Class<?> getAndSerializeEffectiveTypeIfRequired(Object value, Class<?> declaredType) throws IOException {
        Ref ref = refs.get(value);
        if (ref != null && ref.hasBeenSerialized) {
            // Don't need to change types if using a reference
            return declaredType;
        }
        Class<?> realType = value.getClass();
        Class<?> effectiveType = declaredType;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                ClassInfo classInfo = getClassInfo(effectiveType);
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
            } else {
                effectiveType = realType;
            }
        } else if (!settings.getConverter().isConvertible(declaredType)) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            ClassInfo classInfo = getClassInfo(effectiveType);
            output.writeMapHeader(1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
        }
        return effectiveType;
    }

    @Override
    protected void writeObjectAsString(Object value, Class<?> effectiveType) throws IOException {
        Ref ref = refs.get(value);
        if (ref != null && ref.hasBeenSerialized) {
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
        } else {
            String converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
            }
            if (ref != null) {
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
                output.writeString(converted);
                refs.put(value, new Ref(true, ref.position));
            } else {
                output.writeString(converted);
            }
        }
    }

    private ClassInfo getClassInfo(Class<?> effectiveType) {
        ClassInfo classInfo = classes.get(effectiveType);
        if (classInfo == null) {
            throw new IllegalStateException("Tried to serialise class that wasn't present in bean on first pass: " + effectiveType.getName());
        }
        return classInfo;
    }

    //-----------------------------------------------------------------------

    // The info needed serialize instances of a class with a reference to the initially serialized class definition
    private static final class ClassInfo {

        // The position in the initial class definition list, lower means serialized more often
        private final int position;

        // The class itself - not necessary for serialization but here for easier inspection
        private final Class<?> type;

        // The metaproperties (empty if not a bean) in the order in which they need to be serialized
        private final MetaProperty<?>[] metaProperties;

        private ClassInfo(int position, Class<?> type, MetaProperty<?>[] metaProperties) {
            this.position = position;
            this.type = type;
            this.metaProperties = metaProperties;
        }

        @Override
        public String toString() {
            return "ClassInfo{" +
                    "position=" + position +
                    ", type=" + type +
                    ", metaProperties=" + Arrays.toString(metaProperties) +
                    '}';
        }
    }

    // The reference itself (position) plus whether it has previously been serialized
    private static final class Ref {
        private final boolean hasBeenSerialized;
        private final int position;

        private Ref(boolean hasBeenSerialized, int position) {
            this.hasBeenSerialized = hasBeenSerialized;
            this.position = position;
        }

        @Override
        public String toString() {
            return "Ref{" +
                    "hasBeenSerialized=" + hasBeenSerialized +
                    ", position=" + position +
                    '}';
        }
    }
}
