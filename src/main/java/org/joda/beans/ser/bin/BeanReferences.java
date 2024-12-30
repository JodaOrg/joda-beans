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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerIterator;
import org.joda.beans.ser.SerOptional;

/**
 * Stores information on the references in a bean.
 */
final class BeanReferences {

    private static final Set<Class<?>> NON_REFERENCED = Set.of(
            Long.class,
            Integer.class,
            Short.class,
            Byte.class,
            Double.class,
            Float.class,
            Boolean.class,
            Character.class,
            byte[].class);

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
        var references = new BeanReferences(settings);
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
        var objects = new LinkedHashMap<Object, Integer>();
        findReferencesBean(root, root.getClass(), objects, null);

        // build up the list of references, but only for those instances that are repeated
        var refEntries = objects.entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
                .toList();
        for (var entry : refEntries) {
            var value = entry.getKey();
            var realType = value.getClass();

            // simple types do not need references
            if (!NON_REFERENCED.contains(realType)) {
                refs.put(value, new Ref(false, refs.size()));
            }
        }
        
        // reorder classes so the most repeated serialized names have the lowest number (hence the shortest extension)
        this.classInfoList.addAll(classSerializationCount.entrySet().stream()
                .sorted(reverseOrder(comparingInt(Map.Entry::getValue)))
                .map(entry -> classes.get(entry.getKey()))
                .toList());

        // adjust the position in the ClassInfo instance
        for (var position = 0; position < classInfoList.size(); position++) {
            var classInfo = classInfoList.get(position);
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

        // has this object been seen before, if so no need to check it again
        if (objects.compute(base, BeanReferences::incrementOrOne) > 1) {
            // shouldn't try and reuse references to collections
            if (!(base instanceof Bean) && parentIterator != null) {
                var childIterator = settings.getIteratorFactory().createChild(base, parentIterator);
                if (childIterator != null) {
                    findReferencesIterable(childIterator, objects);
                }
            }
            return;
        }
        if (base instanceof Bean bean) {
            addClassInfo(base, declaredClass);
            if (settings.getConverter().isConvertible(bean.getClass())) {
                return;
            }

            for (var prop : bean.metaBean().metaPropertyIterable()) {
                if (settings.isSerialized(prop)) {
                    var value = prop.get(bean);
                    var type = SerOptional.extractType(prop, base.getClass());

                    if (value != null) {
                        var itemIterator = settings.getIteratorFactory().create(value, prop, bean.getClass());
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
            var childIterator = settings.getIteratorFactory().createChild(base, parentIterator);
            if (childIterator != null) {
                // shouldn't try and reuse references to collections
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
        switch (itemIterator.category()) {
            case COLLECTION -> {
                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
                }
            }
            case COUNTED -> {
                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
                }
            }
            case MAP -> {
                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    findReferencesBean(itemIterator.key(), itemIterator.keyType(), objects, null);
                    findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
                }
            }
            case TABLE -> {
                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    findReferencesBean(itemIterator.key(), itemIterator.keyType(), objects, null);
                    findReferencesBean(itemIterator.column(), itemIterator.columnType(), objects, null);
                    findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
                }
            }
            case GRID -> {
                while (itemIterator.hasNext()) {
                    itemIterator.next();
                    findReferencesBean(itemIterator.value(), itemIterator.valueType(), objects, itemIterator);
                }
            }
        }
    }

    // add to list of known classes
    private void addClassInfo(Object value, Class<?> declaredClass) {
        if (value instanceof Bean bean) {
            if (!(value instanceof ImmutableBean)) {
                throw new IllegalArgumentException(
                        "Can only serialize immutable beans in referencing binary format: " + value.getClass().getName());
            }
            var isConvertible = settings.getConverter().isConvertible(value.getClass());
            if (declaredClass.equals(value.getClass()) &&
                    (classes.containsKey(value.getClass()) || isConvertible)) {
                return;
            }

            // don't need meta-property info if it's a convertible type
            if (isConvertible) {
                addClassInfoForEffectiveType(value);
            } else {
                var classInfo = classInfoFromMetaBean(bean.metaBean());
                addClassInfoAndIncrementCount(value.getClass(), classInfo);
            }
            
        } else if (declaredClass == Object.class && !value.getClass().equals(String.class)) {
            addClassInfoForEffectiveType(value);
            
        } else if (!settings.getConverter().isConvertible(declaredClass)) {
            addClassInfoForEffectiveType(value);
        }
    }

    private void addClassInfoForEffectiveType(Object value) {
        var effectiveType = settings.getConverter().findTypedConverter(value.getClass()).getEffectiveType();
        var classInfo = new ClassInfo(effectiveType, List.of());
        addClassInfoAndIncrementCount(effectiveType, classInfo);
    }

    // adds the class, incrementing the number of times it is used
    private void addClassInfoAndIncrementCount(Class<?> type, ClassInfo classInfo) {
        classes.putIfAbsent(type, classInfo);
        classSerializationCount.compute(type, BeanReferences::incrementOrOne);
    }

    // converts a meta-bean to a ClassInfo
    private ClassInfo classInfoFromMetaBean(MetaBean metaBean) {
        var metaProperties = new ArrayList<MetaProperty<?>>();
        for (var metaProp : metaBean.metaPropertyIterable()) {
            if (settings.isSerialized(metaProp)) {
                metaProperties.add(metaProp);
            }
        }
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
        var classInfo = classes.get(effectiveType);
        if (classInfo == null) {
            throw new IllegalStateException(
                    "Tried to serialise class that wasn't present in bean on first pass: " + effectiveType.getName());
        }
        return classInfo;
    }

    // CSOFF
    //-----------------------------------------------------------------------
    // The info needed to serialize instances of a class with a reference to the initially serialized class definition
    static final class ClassInfo {

        // The class itself - not necessary for serialization but here for easier inspection
        final Class<?> type;

        // The meta-properties (empty if not a bean) in the order in which they need to be serialized
        final List<MetaProperty<?>> metaProperties;

        // The position in the initial class definition list, lower means serialized more often
        int position;

        private ClassInfo(Class<?> type, List<MetaProperty<?>> metaProperties) {
            this.type = type;
            this.metaProperties = metaProperties;
            this.position = -1;
        }

        @Override
        public String toString() {
            return "ClassInfo{" +
                    "position=" + position +
                    ", type=" + type +
                    ", metaProperties=" + metaProperties +
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
