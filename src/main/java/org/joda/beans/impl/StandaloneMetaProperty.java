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
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.PropertyStyle;

/**
 * A meta-property that exists separate from a bean.
 * <p>
 * One use case for this is to handle renamed properties in {@code SerDeserializer}.
 * 
 * @param <P>  the type of the property content
 */
public final class StandaloneMetaProperty<P> extends BasicMetaProperty<P> {

    /**
     * The meta-bean, which does not have to refer to this property.
     */
    private final MetaBean metaBean;
    /**
     * The type of the property.
     */
    private final Class<P> propertyType;
    /**
     * The type of the property.
     */
    private final Type type;

    //-----------------------------------------------------------------------
    /**
     * Creates a non-generified property.
     * 
     * @param <R>  the property type
     * @param propertyName  the property name, not empty
     * @param metaBean  the meta-bean, which does not have to refer to this property, not null
     * @param propertyType  the type of the property, not null
     * @return the meta-property, not null
     */
    public static <R> StandaloneMetaProperty<R> of(String propertyName, MetaBean metaBean, Class<R> propertyType) {
        return new StandaloneMetaProperty<>(propertyName, metaBean, propertyType, propertyType);
    }

    /**
     * Creates a property.
     * 
     * @param <R>  the property type
     * @param propertyName  the property name, not empty
     * @param metaBean  the meta-bean, which does not have to refer to this property, not null
     * @param propertyType  the type of the property, not null
     * @param type  the type of the property, not null
     * @return the meta-property, not null
     */
    public static <R> StandaloneMetaProperty<R> of(String propertyName, MetaBean metaBean, Class<R> propertyType, Type type) {
        return new StandaloneMetaProperty<>(propertyName, metaBean, propertyType, type);
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param propertyName  the property name, not empty
     * @param metaBean  the meta-bean, which does not have to refer to this property, not null
     * @param propertyType  the type of the property, not null
     * @param type  the type of the property, not null
     */
    private StandaloneMetaProperty(String propertyName, MetaBean metaBean, Class<P> propertyType, Type type) {
        super(propertyName);
        Objects.requireNonNull(metaBean, "metaBean must not be null");
        Objects.requireNonNull(propertyType, "propertyType must not be null");
        Objects.requireNonNull(type, "type must not be null");
        this.metaBean = metaBean;
        this.propertyType = propertyType;
        this.type = type;
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Class<?> declaringType() {
        return metaBean().beanType();
    }

    @Override
    public Class<P> propertyType() {
        return propertyType;
    }

    @Override
    public Type propertyGenericType() {
        return type;
    }

    @Override
    public PropertyStyle style() {
        return PropertyStyle.READ_WRITE;
    }

    //-----------------------------------------------------------------------
    @Override
    public List<Annotation> annotations() {
        return Collections.emptyList();
    }

    //-----------------------------------------------------------------------
    @Override
    public P get(Bean bean) {
        return propertyType.cast(metaBean().metaProperty(name()).get(bean));
    }

    @Override
    public void set(Bean bean, Object value) {
        metaBean().metaProperty(name()).set(bean, value);
    }

}
