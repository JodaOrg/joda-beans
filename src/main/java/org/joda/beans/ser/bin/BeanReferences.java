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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerCategory;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;

/**
 * Stores information on the references in a bean.
 */
final class BeanReferences {

    /**
     * The settings.
     */
    private final JodaBeanSer settings;
    /**
     * The classes that are being serialized.
     */
    private final Map<Class<?>, ClassInfo> classes = new HashMap<>();
    /**
     * The amount of time each class needs to have its type serialized, linked for stability.
     */
    private final Map<Class<?>, Integer> classSerializationCount = new LinkedHashMap<>();
    /**
     * The sorted class infos.
     */
    private final List<ClassInfo> classInfoList = new ArrayList<>();
    /**
     * The serialized objects that are repeated and require references.
     */
    private final Map<Object, Ref> refs = new HashMap<>();

    // finds 
    static BeanReferences find(ImmutableBean root, JodaBeanSer settings) {
        BeanReferences references = new BeanReferences(settings);
        references.findReferences(root);
        return references;
    }

    // creates an instance
    private BeanReferences(JodaBeanSer settings) {
        this.settings = settings;
    }

    //-----------------------------------------------------------------------
    // finds classes and references within the bean
    private void findReferences(ImmutableBean root) {
        // handle root bean
        classes.put(root.getClass(), classInfoFromMetaBean(root.metaBean()));
        classSerializationCount.put(root.getClass(), 1);

        // recursively check object graph
        Map<Object, Integer> objects = new LinkedHashMap<>();
        findReferencesBean(root, root.getClass(), objects, null);

        // build up the list of references, but only for those instances that are repeated
        List<Map.Entry<Object, Integer>> refEntries = objects.entrySet().stream()
                .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
                .filter(entry -> entry.getValue() > 1)
                .collect(toList());
        for (Map.Entry<Object, Integer> entry : refEntries) {
            Object value = entry.getKey();
            Class<?> realType = value.getClass();

            // simple types do not need references
            if (realType != Integer.class &&
                    realType != Double.class &&
                    realType != Float.class &&
                    realType != Boolean.class &&
                    realType != Long.class &&
                    realType != Short.class &&
                    realType != Byte.class &&
                    realType != byte[].class) {

                refs.put(value, new Ref(false, refs.size()));
            }
        }
        
        // reorder classes so the most repeated serialized names have the lowest number (hence the shortest extension)
        this.classInfoList.addAll(classSerializationCount.entrySet().stream()
                .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
                .map(entry -> classes.get(entry.getKey()))
                .collect(toList()));

        // adjust the position in the ClassInfo instance
        for (int position = 0; position < classInfoList.size(); position++) {
            ClassInfo classInfo = classInfoList.get(position);
            classInfo.position = position;
        }
    }

    // recursively find the references
    private void findReferencesBean(
            Object base,
            Class<?> declaredClass,
            Map<Object, Integer> objects,
            SerIterator parentIterator) {

        if (base == null) {
            return;
        }

        // has this object been seen before, if so no need to check references again
        int result = objects.compute(base, BeanReferences::incrementOrOne);
        if (result > 1) {
            if (base instanceof Bean || parentIterator == null || settings.getIteratorFactory().createChild(base, parentIterator) == null) {
                addClassInfo(base, declaredClass);
            }
            return;
        }

        if (base instanceof Bean) {
            addClassInfo(base, declaredClass);
            Bean bean = (Bean) base;
            if (settings.getConverter().isConvertible(bean.getClass())) {
                return;
            }

            for (MetaProperty<?> prop : bean.metaBean().metaPropertyIterable()) {
                if (settings.isSerialized(prop)) {
                    Object value = prop.get(bean);
                    Class<?> type = SerOptional.extractType(prop, base.getClass());

                    if (value != null) {
                        SerIterator itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
                        if (itemIterator != null) {
                            if (itemIterator.metaTypeRequired()) {
                                objects.compute(itemIterator.metaTypeName(), BeanReferences::incrementOrOne);
                            }
                            findReferencesIterable(itemIterator, objects);
                        } else {
                            findReferencesBean(value, type, objects, null);
                        }
                    }
                }
            }
        } else if (parentIterator != null) {
            SerIterator childIterator = settings.getIteratorFactory().createChild(base, parentIterator);
            if (childIterator != null) {
                findReferencesIterable(childIterator, objects);
            } else {
                addClassInfo(base, declaredClass);
            }
        } else {
            addClassInfo(base, declaredClass);
        }
    }

    // recursively find the references in an iterable
    private void findReferencesIterable(SerIterator itemIterator, Map<Object, Integer> objects) {
        if (itemIterator.category() == SerCategory.MAP) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                findReferencesBean(itemIterator.key(), itemIterator.keyType(), objects, null);
                findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
            }
        } else if (itemIterator.category() == SerCategory.COUNTED) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
            }
        } else if (itemIterator.category() == SerCategory.TABLE) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                findReferencesBean(itemIterator.key(), itemIterator.keyType(), objects, null);
                findReferencesBean(itemIterator.column(), itemIterator.columnType(), objects, null);
                findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
            }
        } else if (itemIterator.category() == SerCategory.GRID) {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
            }
        } else {
            while (itemIterator.hasNext()) {
                itemIterator.next();
                findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
            }
        }
    }

    // add to list of known classes
    private void addClassInfo(Object value, Class<?> declaredClass) {
        if (value instanceof Bean && !(value instanceof ImmutableBean)) {
            throw new IllegalArgumentException(
                    "Can only serialize immutable beans in referencing binary format: " + value.getClass().getName());
        }

        if (value instanceof ImmutableBean) {
            boolean isConvertible = settings.getConverter().isConvertible(value.getClass());
            boolean noNeedToSerializeTypeName = declaredClass.equals(value.getClass()) &&
                    (classes.containsKey(value.getClass()) || isConvertible);

            if (noNeedToSerializeTypeName) {
                return;
            }

            ImmutableBean bean = (ImmutableBean) value;
            // Don't need metaproperty info if it's a convertible type
            ClassInfo classInfo = isConvertible ?
                    new ClassInfo(value.getClass(), new MetaProperty<?>[0]) :
                    classInfoFromMetaBean(bean.metaBean());

            addClassInfoAndIncrementCount(bean.getClass(), classInfo);
            
        } else if (declaredClass == Object.class) {
            if (!value.getClass().equals(String.class)) {
                Class<?> effectiveType = settings.getConverter().findTypedConverter(value.getClass()).getEffectiveType();
                ClassInfo classInfo = new ClassInfo(effectiveType, new MetaProperty<?>[0]);
                addClassInfoAndIncrementCount(effectiveType, classInfo);
            }
        } else if (!settings.getConverter().isConvertible(declaredClass)) {
            ClassInfo classInfo = new ClassInfo(value.getClass(), new MetaProperty<?>[0]);
            addClassInfoAndIncrementCount(value.getClass(), classInfo);
        }
    }

    // adds the class, incrementing the number of times it is used
    private void addClassInfoAndIncrementCount(Class<?> type, ClassInfo classInfo) {
        classes.putIfAbsent(type, classInfo);
        classSerializationCount.compute(type, BeanReferences::incrementOrOne);
    }

    // converts a meta-bean to a ClassInfo
    private ClassInfo classInfoFromMetaBean(MetaBean metaBean) {
        MetaProperty<?>[] metaProperties = StreamSupport.stream(metaBean.metaPropertyIterable().spliterator(), false)
            .filter(metaProp -> settings.isSerialized(metaProp))
            .toArray(MetaProperty<?>[]::new);

        // Positions get recreated when all classes have been recorded
        return new ClassInfo(metaBean.beanType(), metaProperties);
    }

    // Used in Map#compute so we can initialise all the values to one and then increment
    private static int incrementOrOne(@SuppressWarnings("unused") Object k, Integer i) {
        return i == null ? 1 : Math.addExact(i, 1);
    }

    //-----------------------------------------------------------------------
    // gets the map of references
    Map<Object, Ref> getReferences() {
        return refs;
    }
    
    List<ClassInfo> getClassInfoList() {
        return classInfoList;
    }

    // lookup the class info by type
    ClassInfo getClassInfo(Class<?> effectiveType) {
        ClassInfo classInfo = classes.get(effectiveType);
        if (classInfo == null) {
            throw new IllegalStateException(
                    "Tried to serialise class that wasn't present in bean on first pass: " + effectiveType.getName());
        }
        return classInfo;
    }

    // CSOFF
    //-----------------------------------------------------------------------
    // The info needed serialize instances of a class with a reference to the initially serialized class definition
    static final class ClassInfo {

        // The class itself - not necessary for serialization but here for easier inspection
        final Class<?> type;

        // The metaproperties (empty if not a bean) in the order in which they need to be serialized
        final MetaProperty<?>[] metaProperties;

        // The position in the initial class definition list, lower means serialized more often
        int position;

        private ClassInfo(Class<?> type, MetaProperty<?>[] metaProperties) {
            this.type = type;
            this.metaProperties = metaProperties;
            this.position = -1;
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

    //-----------------------------------------------------------------------
    // The reference itself (position) plus whether it has previously been serialized
    static final class Ref {
        boolean hasBeenSerialized;
        final int position;

        private Ref(boolean hasBeenSerialized, int position) {
            this.hasBeenSerialized = hasBeenSerialized;
            this.position = position;
        }

        void sent() {
            hasBeenSerialized = true;
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
