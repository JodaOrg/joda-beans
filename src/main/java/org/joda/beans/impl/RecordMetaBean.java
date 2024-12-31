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
package org.joda.beans.impl;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.TypedMetaBean;

/**
 * A meta-bean for beans implemented using the record language feature.
 * 
 * @param <T>  the record bean type
 */
final class RecordMetaBean<T extends ImmutableBean> extends BasicMetaBean implements TypedMetaBean<T> {

    private final Class<T> beanType;
    private final Map<String, RecordMetaProperty<?>> metaPropertyMap;
    private final MethodHandle constructorHandle;

    RecordMetaBean(Class<T> beanType, MethodHandles.Lookup lookup) {
        JodaBeanUtils.notNull(beanType, "beanType");
        JodaBeanUtils.notNull(lookup, "lookup");
        this.beanType = beanType;
        var recordComponents = beanType.getRecordComponents();
        var paramTypes = new Class<?>[recordComponents.length];
        @SuppressWarnings("unchecked")
        var properties = LinkedHashMap.<String, RecordMetaProperty<?>>newLinkedHashMap(recordComponents.length);
        for (int i = 0; i < recordComponents.length; i++) {
            var name = recordComponents[i].getName();
            paramTypes[i] = recordComponents[i].getType();
            var getterHandle = findGetterHandle(recordComponents[i], lookup);
            properties.put(name, new RecordMetaProperty<Object>(this, recordComponents[i], getterHandle, i));
        }
        try {
            var constructor = beanType.getDeclaredConstructor(paramTypes);
            this.constructorHandle = findConstructorHandle(beanType, lookup, constructor);
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Invalid record", ex);
        }
        this.metaPropertyMap = Collections.unmodifiableMap(properties);
    }

    // finds the getter handle
    private MethodHandle findGetterHandle(RecordComponent recordComponent, Lookup lookup) {
        try {
            var handle = lookup.unreflect(recordComponent.getAccessor());
            return handle.asType(MethodType.methodType(Object.class, Bean.class));
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Invalid record, method cannot be accessed: " + recordComponent.getName(), ex);
        }
    }

    // finds constructor which matches types exactly
    private static MethodHandle findConstructorHandle(
            Class<?> beanType,
            MethodHandles.Lookup lookup,
            Constructor<?> constructor) {

        try {
            // spreader allows an Object[] to invoke the positional arguments
            var constructorType = MethodType.methodType(Void.TYPE, constructor.getParameterTypes());
            var baseHandle = lookup.findConstructor(beanType, constructorType)
                    .asSpreader(Object[].class, constructor.getParameterTypes().length);
            // change the return type so caller can use invokeExact() - this is the erased type of T
            return baseHandle.asType(baseHandle.type().changeReturnType(ImmutableBean.class));
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Invalid record, constructor cannot be found: " + beanType.getSimpleName());
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Invalid record, constructor cannot be accessed: " + beanType.getSimpleName());
        }
    }

    //-------------------------------------------------------------------------
    // finds the index of a property
    int index(String propertyName) {
        var metaProperty = metaPropertyMap.get(propertyName);
        if (metaProperty == null) {
            throw new NoSuchElementException("Unknown property: " + propertyName);
        }
        return metaProperty.getConstructorIndex();
    }

    // builds a new instance
    T build(Object[] data) {
        try {
            return (T) constructorHandle.invokeExact(data);
        } catch (Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new IllegalArgumentException(
                    "Bean cannot be created: " + beanName() + " from " + Arrays.toString(data), ex);
        }
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean isBuildable() {
        return true;
    }

    @Override
    public BeanBuilder<T> builder() {
        var data = new Object[metaPropertyMap.size()];
        return new RecordBeanBuilder<>(this, data);
    }

    @Override
    public Class<T> beanType() {
        return beanType;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        return (Map) metaPropertyMap;
    }
}
