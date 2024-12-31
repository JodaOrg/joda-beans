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

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyStyle;

import com.google.common.collect.ImmutableList;

/**
 * The RecordBean meta-property.
 * 
 * @param <P>  the property type
 */
final class RecordMetaProperty<P> implements MetaProperty<P> {

    private final MetaBean metaBean;
    private final RecordComponent recordComponent;
    private final MethodHandle getterHandle;
    private final int constructorIndex;

    RecordMetaProperty(
            MetaBean metaBean,
            RecordComponent recordComponent,
            MethodHandle getterHandle,
            int constructorIndex) {

        this.metaBean = metaBean;
        this.recordComponent = recordComponent;
        this.getterHandle = getterHandle;
        this.constructorIndex = constructorIndex;
    }

    //-------------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public String name() {
        return recordComponent.getName();
    }

    @Override
    public Class<?> declaringType() {
        return recordComponent.getDeclaringRecord();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<P> propertyType() {
        return (Class<P>) recordComponent.getType();
    }

    @Override
    public Type propertyGenericType() {
        return recordComponent.getGenericType();
    }

    @Override
    public PropertyStyle style() {
        return PropertyStyle.IMMUTABLE;
    }

    @Override
    public List<Annotation> annotations() {
        return ImmutableList.copyOf(recordComponent.getAnnotations());
    }

    @Override
    @SuppressWarnings("unchecked")
    public P get(Bean bean) {
        try {
            return (P) getterHandle.invokeExact(bean);
        } catch (Throwable ex) {
            throw new RuntimeException("Property cannot be read: " + name(), ex);
        }
    }

    @Override
    public void set(Bean bean, Object value) {
        throw new UnsupportedOperationException("Property cannot be written: " + name());
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MetaProperty<?> other &&
                name().equals(other.name()) &&
                declaringType().equals(other.declaringType());
    }

    @Override
    public int hashCode() {
        return name().hashCode() ^ declaringType().hashCode();
    }

    /**
     * Returns a string that summarises the meta-property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return declaringType().getSimpleName() + ":" + name();
    }

    int getConstructorIndex() {
        return constructorIndex;
    }
}
