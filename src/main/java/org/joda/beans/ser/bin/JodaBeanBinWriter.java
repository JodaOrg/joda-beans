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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to be written to a binary format.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 * <p>
 * The binary format is based on MessagePack v2.0.
 * Each bean is output as a map using the property name.
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
public class JodaBeanBinWriter {
    // this binary design is not the smallest possible
    // however, placing the 'ext' for the additional type info within
    // the bean data is much more friendly for dynamic languages using
    // a standalone MessagePack parser

    /**
     * The settings to use.
     */
    private final JodaBeanSer settings;
    /**
     * The output stream.
     */
    private MsgPackOutput output;
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private Map<Class<?>, String> knownTypes = new HashMap<>();
    /**
     * The known types.
     * <p>
     * The values are a backreference to the size of the map the first time an object with this class was serialized, and can be
     * reconstructed on read by storing the classes that aren't references as they're read.
     */
    private Map<Class<?>, SerializedType> knownTypesRef = new HashMap<>();
    /**
     * The known meta property types.
     * <p>
     * The values are a reference to the size of the map the first time the property was serialized, and can be
     * reconstructed on read by storing the meta properties that aren't references as they're read.
     */
    private Map<String, Integer> knownPropertyNameRef = new HashMap<>();
    /**
     * The known objects.
     * <p>
     * The values are a string reference to the size of the map the first time the object was serialized, and can be
     * reconstructed on read by storing the objects that aren't references as they're read.
     */
    private IdentityHashMap<Object, Integer> serializedObjects = new IdentityHashMap<>();
    /**
     * The known immutable objects.
     * <p>
     * The values are a string reference to the size of the map the first time the object was serialized, and can be
     * reconstructed on read by storing the objects that aren't references as they're read.
     */
    private Map<Object, Integer> serializedImmutableObjects = new HashMap<>();

    /**
     * Creates an instance.
     *
     * @param settings  the settings to use, not null
     */
    public JodaBeanBinWriter(final JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes the bean to an array of bytes.
     * <p>
     * The type of the bean will be set in the message.
     *
     * @param bean  the bean to output, not null
     * @return the binary data, not null
     */
    public byte[] write(final Bean bean) {
        return write(bean, true);
    }

    /**
     * Writes the bean to an array of bytes.
     *
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @return the binary data, not null
     */
    public byte[] write(final Bean bean, final boolean rootType) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        try {
            write(bean, rootType, baos);
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
        return baos.toByteArray();
    }

    /**
     * Writes the bean to the {@code OutputStream}.
     * <p>
     * The type of the bean will be set in the message.
     *
     * @param bean  the bean to output, not null
     * @param output  the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(final Bean bean, OutputStream output) throws IOException {
        write(bean, true, output);
    }

    /**
     * Writes the bean to the {@code OutputStream}.
     *
     * @param bean  the bean to output, not null
     * @param rootType  true to output the root type
     * @param output  the output stream, not null
     * @throws IOException if an error occurs
     */
    public void write(final Bean bean, final boolean rootType, OutputStream output) throws IOException {
        if (bean == null) {
            throw new NullPointerException("bean");
        }
        if (output == null) {
            throw new NullPointerException("output");
        }
        this.output = new MsgPackOutput(output);
        writeRoot(bean, rootType);
    }

    //-----------------------------------------------------------------------
    private void writeRoot(final Bean bean, final boolean rootType) throws IOException {
        output.writeArrayHeader(2);
        output.writeInt(1);  // version 1
        writeBean(bean, bean.getClass(), rootType ? RootType.ROOT_WITH_TYPE : RootType.ROOT_WITHOUT_TYPE);
    }

    private void writeBean(final Bean bean, final Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        if (settings.isReferences()) {
            Integer beanRef = serializedObjects.get(bean);
            if (beanRef != null) {
                output.writeExtensionInt(MsgPack.JODA_TYPE_REF, beanRef);
                return;
            } else if (bean instanceof ImmutableBean && settings.getImmutableClasses().contains(bean.getClass())) {
                beanRef = serializedImmutableObjects.get(bean);
                if (beanRef != null) {
                    output.writeExtensionInt(MsgPack.JODA_TYPE_REF, beanRef);
                    return;
                }
            }
        }

        int count = bean.metaBean().metaPropertyCount();
        MetaProperty<?>[] props = new MetaProperty<?>[count];
        Object[] values = new Object[count];
        int size = 0;
        for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
            if (prop.style().isSerializable() || (prop.style().isDerived() && settings.isIncludeDerived())) {
                Object value = SerOptional.extractValue(prop, bean);
                if (value != null) {
                    props[size] = prop;
                    values[size++] = value;
                }
            }
        }
        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            if (settings.isReferences()) {
                SerTypeMapper.EncodedType type = SerTypeMapper.encodeTypeWithReference(bean.getClass(), settings, basePackage, knownTypesRef);
                if (rootTypeFlag == RootType.ROOT_WITH_TYPE) {
                    basePackage = bean.getClass().getPackage().getName() + ".";
                }
                output.writeMapHeader(size + 1);
                output.writeExtension(MsgPack.JODA_TYPE_BEAN, type);
                output.writeNil();
            } else {
                String type = SerTypeMapper.encodeType(bean.getClass(), settings, basePackage, knownTypes);
                if (rootTypeFlag == RootType.ROOT_WITH_TYPE) {
                    basePackage = bean.getClass().getPackage().getName() + ".";
                }
                output.writeMapHeader(size + 1);
                output.writeExtensionString(MsgPack.JODA_TYPE_BEAN, type);
                output.writeNil();
            }
        } else {
            output.writeMapHeader(size);
        }
        for (int i = 0; i < size; i++) {
            MetaProperty<?> prop = props[i];
            Object value = values[i];
            if (settings.isReferences()) {
                Integer reference = serializedObjects.get(prop.name());
                if (reference != null) {
                    output.writeExtensionInt(MsgPack.JODA_TYPE_PROP_NAME, reference);
                } else {
                    output.writeString(prop.name());
                    serializedObjects.put(prop.name(), serializedObjects.size());
                }
            } else {
                output.writeString(prop.name());
            }
            Class<?> propType = SerOptional.extractType(prop, bean.getClass());
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

        if (settings.isReferences()) {
            if (bean instanceof ImmutableBean && settings.getImmutableClasses().contains(bean.getClass())) {
                serializedImmutableObjects.put(bean, serializedImmutableObjects.size());
            } else {
                serializedObjects.put(bean, serializedObjects.size());
            }
        }
    }

    //-----------------------------------------------------------------------
    private void writeMetaPropertyReference(String metaTypeName) throws IOException {
        if (settings.isReferences()) {
            Integer reference = knownPropertyNameRef.get(metaTypeName);
            if (reference != null) {
                output.writeExtensionInt(MsgPack.JODA_TYPE_META, reference);
            } else {
                output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
                knownPropertyNameRef.put(metaTypeName, knownPropertyNameRef.size());
            }
        } else {
            output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
        }
    }

    private void writeElements(final SerIterator itemIterator) throws IOException {
        if (itemIterator.metaTypeRequired()) {
            output.writeMapHeader(1);
            writeMetaPropertyReference(itemIterator.metaTypeName());
        }
        if (itemIterator.category() == SerCategory.MAP) {
            writeMap(itemIterator);
        } else if (itemIterator.category() == SerCategory.COUNTED) {
            writeCounted(itemIterator);
        } else if (itemIterator.category() == SerCategory.TABLE) {
            writeTable(itemIterator);
        } else if (itemIterator.category() == SerCategory.GRID) {
            writeGrid(itemIterator);
        } else {
            writeArray(itemIterator);
        }
    }

    private void writeArray(final SerIterator itemIterator) throws IOException {
        output.writeArrayHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    private void writeMap(final SerIterator itemIterator) throws IOException {
        output.writeMapHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            Object key = itemIterator.key();
            if (key == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be null: " + key);
            }
            writeObject(itemIterator.keyType(), key, null);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    private void writeTable(final SerIterator itemIterator) throws IOException {
        output.writeArrayHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayHeader(3);
            writeObject(itemIterator.keyType(), itemIterator.key(), null);
            writeObject(itemIterator.columnType(), itemIterator.column(), null);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    private void writeGrid(final SerIterator itemIterator) throws IOException {
        int rows = itemIterator.dimensionSize(0);
        int columns = itemIterator.dimensionSize(1);
        int totalSize = rows * columns;
        if (itemIterator.size() < (totalSize / 4)) {
            // sparse
            output.writeArrayHeader(itemIterator.size() + 2);
            output.writeInt(rows);
            output.writeInt(columns);
            while (itemIterator.hasNext()) {
                itemIterator.next();
                output.writeArrayHeader(3);
                output.writeInt((Integer) itemIterator.key());
                output.writeInt((Integer) itemIterator.column());
                writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            }
        } else {
            // dense
            output.writeArrayHeader(totalSize + 2);
            output.writeInt(rows);
            output.writeInt(columns);
            for (int row = 0; row < rows; row++) {
                for (int column = 0; column < columns; column++) {
                    writeObject(itemIterator.valueType(), itemIterator.value(row, column), itemIterator);
                }
            }
        }
    }

    private void writeCounted(final SerIterator itemIterator) throws IOException {
        output.writeMapHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeInt(itemIterator.count());
        }
    }

    private void writeObject(final Class<?> declaredType, final Object obj, SerIterator parentIterator) throws IOException {
        if (obj == null) {
            output.writeNil();
        } else if (settings.getConverter().isConvertible(obj.getClass())) {
            writeSimple(declaredType, obj);
        } else if (obj instanceof Bean) {
            writeBean((Bean) obj, declaredType, RootType.NOT_ROOT);
        } else if (parentIterator != null) {
            SerIterator childIterator = settings.getIteratorFactory().createChild(obj, parentIterator);
            if (childIterator != null) {
                writeElements(childIterator);
            } else {
                writeSimple(declaredType, obj);
            }
        } else {
            writeSimple(declaredType, obj);
        }
    }

    //-----------------------------------------------------------------------
    private void writeSimple(final Class<?> declaredType, final Object value) throws IOException {
        // simple types have no need to write a type object
        Class<?> realType = value.getClass();
        if (realType == Integer.class) {
            output.writeInt(((Integer) value).intValue());
            return;
        } else if (realType == Double.class) {
            output.writeDouble(((Double) value).doubleValue());
            return;
        } else if (realType == Float.class) {
            output.writeFloat(((Float) value).floatValue());
            return;
        } else if (realType == Boolean.class) {
            output.writeBoolean(((Boolean) value).booleanValue());
            return;
        }

        // handle no declared type and subclasses
        Class<?> effectiveType = declaredType;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                output.writeMapHeader(1);
                if (settings.isReferences()) {
                    SerTypeMapper.EncodedType type = SerTypeMapper.encodeTypeWithReference(effectiveType, settings, basePackage, knownTypesRef);
                    output.writeExtension(MsgPack.JODA_TYPE_DATA, type);
                } else {
                    String type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                    output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
                }
            } else {
                effectiveType = realType;
            }
        } else if (settings.getConverter().isConvertible(declaredType) == false) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            output.writeMapHeader(1);
            if (settings.isReferences()) {
                SerTypeMapper.EncodedType type = SerTypeMapper.encodeTypeWithReference(effectiveType, settings, basePackage, knownTypesRef);
                output.writeExtension(MsgPack.JODA_TYPE_DATA, type);
            } else {
                String type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
            }
        }

        // long/short/byte only processed now to ensure that a distinction can be made between Integer and Long
        if (realType == Long.class) {
            output.writeLong(((Long) value).longValue());
            return;
        } else if (realType == Short.class) {
            output.writeInt(((Short) value).shortValue());
            return;
        } else if (realType == Byte.class) {
            output.writeInt(((Byte) value).byteValue());
            return;
        } else if (realType == byte[].class) {
            output.writeBytes((byte[]) value);
            return;
        }

        // write as a string
        try {
            if (settings.isReferences()) {
                Integer reference = serializedObjects.get(value);
                if (reference != null) {
                    output.writeExtensionInt(MsgPack.JODA_TYPE_REF, reference);
                } else if (settings.getImmutableClasses().contains(value.getClass())) {
                    reference = serializedImmutableObjects.get(value);
                    if (reference != null) {
                        output.writeExtensionInt(MsgPack.JODA_TYPE_IMM_REF, reference);
                    } else {
                        writeAsString(value, effectiveType);
                        serializedImmutableObjects.put(value, serializedImmutableObjects.size());
                    }
                } else {
                    writeAsString(value, effectiveType);
                    serializedObjects.put(value, serializedObjects.size());
                }
            } else {
                writeAsString(value, effectiveType);
            }
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to convert type " + effectiveType.getName() + " declared as " + declaredType.getName(), ex);
        }
    }

    private void writeAsString(Object value, Class<?> effectiveType) throws IOException {
        String converted = settings.getConverter().convertToString(effectiveType, value);
        if (converted == null) {
            throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
        }
        output.writeString(converted);
    }

    //-----------------------------------------------------------------------
    enum RootType {
        ROOT_WITH_TYPE,
        ROOT_WITHOUT_TYPE,
        NOT_ROOT,
    }

}
