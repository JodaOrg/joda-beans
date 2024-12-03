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

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerOptional;
import org.joda.beans.ser.SerTypeMapper;

/**
 * Provides the ability for a Joda-Bean to written to the referencing binary format.
 */
class JodaBeanReferencingBinWriter extends AbstractBinWriter {

    private BeanReferences references;

    // creates an instance
    JodaBeanReferencingBinWriter(JodaBeanSer settings, OutputStream output) {
        super(settings, output);
    }

    //-----------------------------------------------------------------------
    // writes the bean
    void write(Bean bean) throws IOException {
        if (!(bean instanceof ImmutableBean immutable)) {
            throw new IllegalArgumentException(
                    "Referencing binary format can only write ImmutableBean instances: " + bean.getClass().getName());
        }
        // sets up the map of beans - classes & classSerializationCount
        references = BeanReferences.find(immutable, settings);

        // write array of 4 items - Version, Ref Count, Class Info, Root Bean
        output.writeArrayHeader(4);
        output.writeInt(2);
        writeClassDescriptions(references);
        writeRootBean(bean, true);
    }

    // determines what beans occur more than once and setup references
    private void writeClassDescriptions(BeanReferences references) throws IOException {
        // write out ref count first, which is the number of instances that are referenced
        output.writeInt(references.getReferences().size());

        // write map of class name to a list of metatype names (which is empty if not a bean)
        var classInfos = references.getClassInfoList();
        output.writeMapHeader(classInfos.size());
        for (var classInfo : classInfos) {
            // known types parameter is null as we never serialize the class names again
            var className = SerTypeMapper.encodeType(classInfo.type, settings, null, null);
            output.writeString(className);

            output.writeArrayHeader(classInfo.metaProperties.size());
            for (MetaProperty<?> property : classInfo.metaProperties) {
                output.writeString(property.name());
            }
        }
    }

    //-----------------------------------------------------------------------
    @Override
    void writeBean(Bean bean, Class<?> declaredType, RootType rootTypeFlag) throws IOException {
        var ref = references.getReferences().get(bean);
        if (ref != null) {
            if (ref.hasBeenSerialized) {
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
                return;
            }
            output.writeMapHeader(1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
        }

        var classInfo = references.getClassInfo(bean.getClass());
        var props = classInfo.metaProperties;
        var count = props.size();
        var values = new Object[count];
        var size = 0;
        for (var prop : props) {
            var value = SerOptional.extractValue(prop, bean);
            values[size++] = value;
        }

        if (rootTypeFlag == RootType.ROOT_WITH_TYPE || (rootTypeFlag == RootType.NOT_ROOT && bean.getClass() != declaredType)) {
            output.writeArrayHeader(size + 1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_BEAN, classInfo.position);
        } else {
            output.writeArrayHeader(size);
        }

        for (var i = 0; i < size; i++) {
            var prop = props.get(i);
            var value = values[i];
            var propType = SerOptional.extractType(prop, bean.getClass());

            if (value == null) {
                output.writeNil();
                continue;
            }

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
        if (ref != null) {
            ref.sent();
        }
    }

    @Override
    void writeMetaPropertyReference(String metaTypeName) throws IOException {
        var ref = references.getReferences().get(metaTypeName);
        if (ref != null) {
            if (ref.hasBeenSerialized) {
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_META, ref.position);
            } else {
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
                output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
                ref.sent();
            }
        } else {
            output.writeExtensionString(MsgPack.JODA_TYPE_META, metaTypeName);
        }
    }

    @Override
    Class<?> getAndSerializeEffectiveTypeIfRequired(Object value, Class<?> declaredType) throws IOException {
        var ref = references.getReferences().get(value);
        if (ref != null && ref.hasBeenSerialized) {
            // Don't need to change types if using a reference
            return declaredType;
        }
        var realType = value.getClass();
        var effectiveType = declaredType;
        if (declaredType == Object.class) {
            if (realType != String.class) {
                effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
                var classInfo = references.getClassInfo(effectiveType);
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
            } else {
                effectiveType = realType;
            }
        } else if (!settings.getConverter().isConvertible(declaredType)) {
            effectiveType = settings.getConverter().findTypedConverter(realType).getEffectiveType();
            var classInfo = references.getClassInfo(effectiveType);
            output.writeMapHeader(1);
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_DATA, classInfo.position);
        }
        return effectiveType;
    }

    @Override
    void writeObjectAsString(Object value, Class<?> effectiveType) throws IOException {
        var ref = references.getReferences().get(value);
        if (ref != null && ref.hasBeenSerialized) {
            output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF, ref.position);
        } else {
            var converted = settings.getConverter().convertToString(effectiveType, value);
            if (converted == null) {
                throw new IllegalArgumentException("Unable to write because converter returned a null string: " + value);
            }
            if (ref != null) {
                output.writeMapHeader(1);
                output.writePositiveExtensionInt(MsgPack.JODA_TYPE_REF_KEY, ref.position);
                output.writeString(converted);
                ref.sent();
            } else {
                output.writeString(converted);
            }
        }
    }

}
