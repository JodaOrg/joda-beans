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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multiset;
import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

/**
 * Provides the ability for an immutable Joda-Bean to be written to a compact binary format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * This class only supports serialization of instances of ImmutableBean and other basic types, if any mutable beans
 * are encountered during traversal an exception will be thrown.
 * <p>
 * Multiple passes of the bean are used to build up a map of unique immutable beans and unique immutable instances of
 * other classes (as defined by JodaBeanSer settings), and then the property names and class name for each class is serialized up front.
 * Then subsequent bean instances are serialized with references to previously serialized items if required.
 * <p>
 * The binary format is based on MessagePack v2.0.
 * Each unique immutable bean is output as a list of each property value using a fixed property order previously serialized.
 * Subsequent instances of the immutable bean (defined by an equality check) are replaced by references to the first instance.
 * <p>
 * Most simple types, defined by Joda-Convert, are output as MessagePack strings.
 * However, MessagePack nil, boolean, float, integral and bin types are also used
 * for null, byte[] and the Java numeric primitive types (excluding char).
 * <p>
 * Beans are output using MessagePack maps where the key is the property name.
 * Collections are output using MessagePack maps or arrays.
 * Multisets are output as a map of value to count.
 * <p>
 * If a collection contains a collection then addition meta-type information is
 * written to aid with deserialization.
 * At this level, the data read back may not be identical to that written.
 * <p>
 * Where necessary, the Java type is sent using an 'ext' entity.
 * Three 'ext' types are used, one each for beans, meta-type and simple.
 * The class name is passed as the 'ext' data.
 * The 'ext' value is sent as an additional key-value pair for beans, with the
 * 'ext' as the key and 'nil' as the value. Where the additional type information
 * is not about a bean, a tuple is written using a size 1 map where the key is the
 * 'ext' data and the value is the data being annotated.
 * <p>
 * Type names are shortened by the package of the root type if possible.
 * Certain basic types are also handled, such as String, Integer, File and URI.
 *
 * @author Stephen Colebourne
 */
public class JodaBeanCompactBinWriter extends AbstractBinWriter {

    /**
     * The classes that are being serialized.
     */
    private Map<Class<?>, ClassInfo> classes = new HashMap<>();
    /**
     * The serialized objects that are repeated and require references.
     */
    private Map<Object, Ref> refs = new HashMap<>();
    private List<ClassInfo> classInfoByReference;

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
        basePackage = root.getClass().getPackage().getName() + ".";
        buildClassAndRefMap(root);

        // Write out ref count first
        output.writeInt(refs.size());

        classInfoByReference = classes.values().stream()
                .sorted(comparingInt(info -> info.position))
                .collect(toList());

        // Write map of class name to a list of metatype names (which is empty if not a bean)
        output.writeMapHeader(classInfoByReference.size());

        for (ClassInfo classInfo : classInfoByReference) {
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
        Multiset<Object> objects = HashMultiset.create();
        addClasses(root, objects);

        for (Multiset.Entry<Object> entry : objects.entrySet()) {
            // Only add refs for objects that are repeated
            if (entry.getCount() > 1) {
                refs.put(entry.getElement(), new Ref(false, refs.size()));
            }
        }
    }

    private void addClasses(Object base, Multiset<Object> objects) {
        if (base == null) {
            return;
        }

        addClassAndRef(base, objects);

        if (base instanceof Bean) {
            Bean bean = (Bean) base;
            for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
                Object value = prop.get(bean);
                addClasses(value, objects);
                Class<?> type = SerOptional.extractType(prop, base.getClass());
                addClassAndRef(type, objects); // In case it's a null value or optional field

                if (value != null) {
                    SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                    if (itemIterator != null && itemIterator.metaTypeRequired()) {
                        addClassAndRef(itemIterator.metaTypeName(), objects);
                    }
                }
            }
        } else if (base instanceof Collection) {
            for (Object value : ((Collection) base)) {
                addClasses(value, objects);
            }
        } else if (base instanceof Map) {
            Map map = (Map) base;
            for (Object key : map.keySet()) {
                addClasses(key, objects);
                addClasses(map.get(key), objects);
            }
            //} else if (base instanceof Table) {
            //    Table table = (Table) base;
            //    addClasses(table.columnMap(), objects);
            //} else if (base instanceof Grid) {
            //    Grid grid = (Grid) base;
            //    addClasses(grid.cells(), objects);
        }
    }

    private void addClassAndRef(Object value, Multiset<Object> objects) {
        if (value == null) {
            return;
        }

        objects.add(value);

        if (value instanceof Bean && !(value instanceof ImmutableBean)) {
            throw new IllegalArgumentException("Can only serialize immutable beans in compact binary format: " + value.getClass().getName());
        }

        if (classes.containsKey(value.getClass())) {
            return;
        }

        if (value instanceof ImmutableBean) {
            ImmutableBean bean = (ImmutableBean) value;
            MetaProperty<?>[] metaProperties = Iterables.toArray(
                    bean.metaBean().metaPropertyIterable(), MetaProperty.class);
            ClassInfo classInfo = new ClassInfo(classes.size(), value.getClass(), metaProperties);
            classes.putIfAbsent(value.getClass(), classInfo);
        } else if (value instanceof Class<?>) {
            Class<?> type = (Class<?>) value;
            if (ImmutableBean.class.isAssignableFrom(type)) {
                SerDeserializer deser = settings.getDeserializers().findDeserializer(type);
                MetaBean metaBean = deser.findMetaBean(type);

                if (metaBean != null) {
                    MetaProperty<?>[] metaProperties = Iterables.toArray(
                            metaBean.metaPropertyIterable(),
                            MetaProperty.class);

                    ClassInfo classInfo = new ClassInfo(classes.size(), type, metaProperties);
                    classes.putIfAbsent(value.getClass(), classInfo);
                }
            } else {
                ClassInfo classInfo = new ClassInfo(classes.size(), type, new MetaProperty<?>[0]);
                classes.putIfAbsent(type, classInfo);
            }
        } else {
            ClassInfo classInfo = new ClassInfo(classes.size(), value.getClass(), new MetaProperty<?>[0]);
            classes.putIfAbsent(value.getClass(), classInfo);
        }

    }

    @Override
    protected void writeBean(Bean bean, Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        Ref ref = refs.get(bean);
        if (ref != null) {
            if (ref.hasBeenSerialized) {
                output.writeExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
                return;
            }
            output.writeMapHeader(1);
            output.writeExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
        }

        ClassInfo classInfo = getClassInfo(bean.getClass());
        MetaProperty<?>[] props = classInfo.metaProperties;
        int count = props.length;
        Object[] values = new Object[count];
        int size = 0;
        for (MetaProperty<?> prop : props) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                Object value = SerOptional.extractValue(prop, bean);
                values[size++] = value;
            }
        }

        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            output.writeArrayHeader(size + 1);
            output.writeExtensionInt(MsgPack.JODA_TYPE_BEAN, classInfo.position);
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
                output.writeExtensionInt(MsgPack.JODA_TYPE_META, ref.position);
            } else {
                output.writeMapHeader(1);
                output.writeExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
                output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
                refs.put(metaTypeName, new Ref(true, ref.position));
            }
        } else {
            output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
        }
    }

    @Override
    protected Class<?> getEffectiveType(Class<?> declaredType, Class<?> realType) throws IOException {
        Class<?> effectiveType = declaredType;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                ClassInfo classInfo = getClassInfo(effectiveType);
                output.writeMapHeader(1);
                output.writeExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
            } else {
                effectiveType = realType;
            }
        } else if (settings.getConverter().isConvertible(declaredType) == false) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            ClassInfo classInfo = getClassInfo(effectiveType);
            output.writeMapHeader(1);
            output.writeExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
        }
        return effectiveType;
    }

    @Override
    protected void writeObjectAsString(Object value, Class<?> effectiveType) throws IOException {
        Ref ref = refs.get(value);
        if (ref != null && ref.hasBeenSerialized) {
            output.writeExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
        } else {
            String converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
            }
            if (ref != null) {
                output.writeMapHeader(1);
                output.writeExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
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

        // The position in the initial class definition list
        private final int position;

        // The class itself - not necessary for serialization but here for easier inspection
        private final Class<?> type;

        // The metaproperties (empty if not a bean) in the order in which they need to be serialized
        private final MetaProperty<?>[] metaProperties;

        private ClassInfo(int position, Class<?> type, MetaProperty<?>[] metaProperties) {
            this.position = position;
            this.type = type;
            this.metaProperties = Stream.of(metaProperties)
                    .filter(it -> !it.style().isDerived())
                    .toArray(MetaProperty<?>[]::new);
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
