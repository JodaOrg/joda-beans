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
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to be written to both the standard and referencing binary formats.
 * <p>
 * This class contains mutable state and cannot be used from multiple threads.
 * A new instance must be created for each message.
 */
abstract class AbstractBinWriter {

    /**
     * The settings to use.
     */
    final JodaBeanSer settings;  // CSIGNORE
    /**
     * The output stream.
     */
    final MsgPackOutput output;  // CSIGNORE
    /**
     * The base package including the trailing dot.
     */
    private String basePackage;
    /**
     * The known types.
     */
    private final Map<Class<?>, String> knownTypes = new HashMap<>();

    // creates an instance
    AbstractBinWriter(JodaBeanSer settings, OutputStream output) {
        this.settings = settings;
        this.output = new MsgPackOutput(output);
    }

    //-----------------------------------------------------------------------
    void writeRootBean(Bean bean, boolean rootTypeFlag) throws IOException {
        writeBean(bean, bean.getClass(), rootTypeFlag ? RootType.ROOT_WITH_TYPE : RootType.ROOT_WITHOUT_TYPE);
    }

    void writeBean(Bean bean, Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        var count = bean.metaBean().metaPropertyCount();
        var props = new MetaProperty<?>[count];
        var values = new Object[count];
        var size = 0;
        for (var prop : bean.metaBean().metaPropertyIterable()) {
            if (settings.isSerialized(prop)) {
                var value = SerOptional.extractValue(prop, bean);
                if (value != null) {
                    props[size] = prop;
                    values[size++] = value;
                }
            }
        }
        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            var type = SerTypeMapper.encodeType(bean.getClass(), settings, basePackage, knownTypes);
            if (rootTypeFlag == RootType.ROOT_WITH_TYPE) {
                basePackage = bean.getClass().getPackage().getName() + ".";
            }
            output.writeMapHeader(size + 1);
            output.writeExtensionString(MsgPack.JODA_TYPE_BEAN, type);
            output.writeNil();
        } else {
            output.writeMapHeader(size);
        }
        for (var i = 0; i < size; i++) {
            MetaProperty<?> prop = props[i];
            var value = values[i];
            output.writeString(prop.name());
            var propType = SerOptional.extractType(prop, bean.getClass());
            if (value instanceof Bean beanValue) {
                if (settings.getConverter().isConvertible(value.getClass())) {
                    writeSimple(propType, value);
                } else {
                    writeBean(beanValue, propType, RootType.NOT_ROOT);
                }
            } else {
                var itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                if (itemIterator != null) {
                    writeElements(itemIterator);
                } else {
                    writeSimple(propType, value);
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    void writeMetaPropertyReference(String metaTypeName) throws IOException {
        output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
    }

    void writeElements(SerIterator itemIterator) throws IOException {
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

    void writeArray(SerIterator itemIterator) throws IOException {
        output.writeArrayHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    void writeMap(SerIterator itemIterator) throws IOException {
        output.writeMapHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            var key = itemIterator.key();
            if (key == null) {
                throw new IllegalArgumentException("Unable to write map key as it cannot be null");
            }
            writeObject(itemIterator.keyType(), key, null);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    void writeTable(SerIterator itemIterator) throws IOException {
        output.writeArrayHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            output.writeArrayHeader(3);
            writeObject(itemIterator.keyType(), itemIterator.key(), null);
            writeObject(itemIterator.columnType(), itemIterator.column(), null);
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
        }
    }

    void writeGrid(SerIterator itemIterator) throws IOException {
        var rows = itemIterator.dimensionSize(0);
        var columns = itemIterator.dimensionSize(1);
        var totalSize = rows * columns;
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
            for (var row = 0; row < rows; row++) {
                for (var column = 0; column < columns; column++) {
                    writeObject(itemIterator.valueType(), itemIterator.value(row, column), itemIterator);
                }
            }
        }
    }

    void writeCounted(SerIterator itemIterator) throws IOException {
        output.writeMapHeader(itemIterator.size());
        while (itemIterator.hasNext()) {
            itemIterator.next();
            writeObject(itemIterator.valueType(), itemIterator.value(), itemIterator);
            output.writeInt(itemIterator.count());
        }
    }

    void writeObject(Class<?> declaredType, Object obj, SerIterator parentIterator) throws IOException {
        if (obj == null) {
            output.writeNil();
        } else if (settings.getConverter().isConvertible(obj.getClass())) {
            writeSimple(declaredType, obj);
        } else if (obj instanceof Bean bean) {
            writeBean(bean, declaredType, RootType.NOT_ROOT);
        } else if (parentIterator != null) {
            var childIterator = settings.getIteratorFactory().createChild(obj, parentIterator);
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
    void writeSimple(Class<?> declaredType, Object value) throws IOException {
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
        Class<?> effectiveType = getAndSerializeEffectiveTypeIfRequired(value, declaredType);

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
            writeObjectAsString(value, effectiveType);
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unable to convert type " + effectiveType.getName() + " declared as " + declaredType.getName(), ex);
        }
    }

    // called when serializing an object in {@link #writeSimple(Class, Object)}, to get the effective type of the
    // object and if necessary to serialize the class information
    // needs to handle no declared type and subclass instances
    Class<?> getAndSerializeEffectiveTypeIfRequired(Object value, Class<?> declaredType) throws IOException {
        var realType = value.getClass();
        var effectiveType = declaredType;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                output.writeMapHeader(1);
                var type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
                output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
            } else {
                effectiveType = realType;
            }
        } else if (!settings.getConverter().isConvertible(declaredType)) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            output.writeMapHeader(1);
            var type = SerTypeMapper.encodeType(effectiveType, settings, basePackage, knownTypes);
            output.writeExtensionString(MsgPack.JODA_TYPE_DATA, type);
        }
        return effectiveType;
    }

    // writes a value as a string
    // called after discerning that the value is not a simple type
    void writeObjectAsString(Object value, Class<?> effectiveType) throws IOException {
        var converted = settings.getConverter().convertToString(effectiveType, value);
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
