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
package org.joda.beans.impl.direct;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.DerivedProperty;
import org.joda.beans.MetaProperty;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.impl.BasicMetaBean;

/**
 * A meta-bean implementation designed for use by the code generator.
 * 
 * @author Stephen Colebourne
 * @param <T>  the type of the bean
 */
public final class MinimalMetaBean<T extends Bean> extends BasicMetaBean implements TypedMetaBean<T> {

    /** The bean type. */
    private final Class<T> beanType;
    /** The constructor to use. */
    private final Supplier<BeanBuilder<T>> builderSupplier;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;

    /**
     * Obtains an instance of the meta-bean for immutable beans.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param getters  the getter functions, not null
     * @return the meta-bean, not null
     */
    @SafeVarargs
    public static <B extends Bean> MinimalMetaBean<B> of(
            Class<B> beanType,
            Supplier<BeanBuilder<B>> builderSupplier,
            Function<B, Object>... getters) {

        if (getters == null) {
            throw new NullPointerException("Getter functions must not be null");
        }
        return new MinimalMetaBean<>(
                beanType, builderSupplier, Arrays.asList(getters), Collections.nCopies(getters.length, null));
    }

    /**
     * Obtains an instance of the meta-bean for mutable beans.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param getters  the getter functions, not null
     * @param setters  the setter functions, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> MinimalMetaBean<B> of(
            Class<B> beanType,
            Supplier<BeanBuilder<B>> builderSupplier,
            List<Function<B, Object>> getters,
            List<BiConsumer<B, Object>> setters) {

        if (getters == null) {
            throw new NullPointerException("Getter functions must not be null");
        }
        if (setters == null) {
            throw new NullPointerException("Setter functions must not be null");
        }
        return new MinimalMetaBean<>(beanType, builderSupplier, getters, setters);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param getters  the getter functions, not null
     * @param setters  the setter functions, all nulls if immutable
     */
    private MinimalMetaBean(
            Class<T> beanType,
            Supplier<BeanBuilder<T>> builderSupplier,
            List<Function<T, Object>> getters,
            List<BiConsumer<T, Object>> setters) {

        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        if (builderSupplier == null) {
            throw new NullPointerException("Supplier of BeanBuilder must not be null");
        }
        this.beanType = beanType;
        this.builderSupplier = builderSupplier;
        // extract fields and match to getters
        Map<String, MetaProperty<?>> map = new HashMap<>();
        Field[] fields = Stream.of(beanType.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && f.getAnnotation(PropertyDefinition.class) != null)
                .toArray(Field[]::new);
        if (fields.length != getters.size()) {
            throw new NullPointerException("Getter functions must match fields");
        }
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            map.put(name, new MinimalMetaProperty<>(this, name, fields[i], getters.get(i), setters.get(i)));
        }
        // derived
        Method[] methods = beanType.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getAnnotation(DerivedProperty.class) != null &&
                    method.getName().startsWith("get") &&
                    method.getName().length() > 3 &&
                    Character.isUpperCase(method.getName().charAt(3)) &&
                    method.getParameterTypes().length == 0) {
                String methodName = method.getName();
                String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                MetaProperty<Object> mp = new MinimalMetaProperty<>(this, method, propertyName);
                map.put(propertyName, mp);
            }
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<T> builder() {
        return builderSupplier.get();
    }

    @Override
    public Class<T> beanType() {
        return beanType;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        return metaPropertyMap;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MinimalMetaBean) {
            MinimalMetaBean<?> other = (MinimalMetaBean<?>) obj;
            return this.beanType.equals(other.beanType);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return beanType.hashCode() + 3;
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaBean:" + beanName();
    }

}
