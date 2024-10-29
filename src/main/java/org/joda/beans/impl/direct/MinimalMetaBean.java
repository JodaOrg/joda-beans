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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaProperty;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.DerivedProperty;
import org.joda.beans.gen.PropertyDefinition;

/**
 * A meta-bean implementation designed for use by the code generator.
 * 
 * @param <T>  the type of the bean
 */
public final class MinimalMetaBean<T extends Bean> implements TypedMetaBean<T> {

    /** The bean type. */
    private final Class<T> beanType;
    /** The constructor to use. */
    private final Supplier<BeanBuilder<T>> builderSupplier;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;
    /** The aliases. */
    private final Map<String, String> aliasMap;

    /**
     * Obtains an instance of the meta-bean for immutable beans.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * The field names must be specified as reflection does not return fields in source code order.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param fieldNames  the field names, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param getters  the getter functions, not null
     * @return the meta-bean, not null
     */
    @SafeVarargs
    public static <B extends Bean> MinimalMetaBean<B> of(
            Class<B> beanType,
            String[] fieldNames,
            Supplier<BeanBuilder<B>> builderSupplier,
            Function<B, Object>... getters) {

        Objects.requireNonNull(getters, "getters must not be null");
        return new MinimalMetaBean<>(beanType, fieldNames, builderSupplier, Arrays.asList(getters), null);
    }

    /**
     * Obtains an instance of the meta-bean for mutable beans.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * The field names must be specified as reflection does not return fields in source code order.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param fieldNames  the field names, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param getters  the getter functions, not null
     * @param setters  the setter functions, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> MinimalMetaBean<B> of(
            Class<B> beanType,
            String[] fieldNames,
            Supplier<BeanBuilder<B>> builderSupplier,
            List<Function<B, Object>> getters,
            List<BiConsumer<B, Object>> setters) {

        Objects.requireNonNull(getters, "getters must not be null");
        Objects.requireNonNull(setters, "setters must not be null");
        return new MinimalMetaBean<>(beanType, fieldNames, builderSupplier, getters, setters);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     * @param builderSupplier  the supplier of bean builders, not null
     * @param fieldNames  the field names, not null
     * @param getters  the getter functions, not null
     * @param setters  the setter functions, may be null
     */
    private MinimalMetaBean(
            Class<T> beanType,
            String[] fieldNames,
            Supplier<BeanBuilder<T>> builderSupplier,
            List<Function<T, Object>> getters,
            List<BiConsumer<T, Object>> setters) {

        Objects.requireNonNull(beanType, "beanType must not be null");
        Objects.requireNonNull(builderSupplier, "builderSupplier must not be null");
        Objects.requireNonNull(fieldNames, "fieldNames must not be null");
        Objects.requireNonNull(getters, "getters must not be null");
        if (fieldNames.length != getters.size()) {
            throw new IllegalArgumentException("Number of getter functions must match number of fields");
        }
        if (setters != null && fieldNames.length != setters.size()) {
            throw new IllegalArgumentException("Number of setter functions must match number of fields");
        }
        this.beanType = beanType;
        this.builderSupplier = builderSupplier;
        // extract fields and match to getters/setters
        var map = new LinkedHashMap<String, MetaProperty<?>>();
        for (var i = 0; i < fieldNames.length; i++) {
            var fieldName = fieldNames[i];
            Field field;
            try {
                field = beanType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ex) {
                throw new IllegalArgumentException(ex);
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new IllegalArgumentException("Field must not be static");
            }
            if (field.getAnnotation(PropertyDefinition.class) == null) {
                throw new IllegalArgumentException("Field must have PropertyDefinition annotation");
            }
            map.put(fieldName, new MinimalMetaProperty<>(
                    this, fieldName, field, getters.get(i), setters != null ? setters.get(i) : null));
        }
        // derived
        var methods = beanType.getDeclaredMethods();
        for (var method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    Modifier.isPublic(method.getModifiers()) &&
                    method.getAnnotation(DerivedProperty.class) != null &&
                    method.getName().startsWith("get") &&
                    method.getName().length() > 3 &&
                    Character.isUpperCase(method.getName().charAt(3)) &&
                    method.getParameterTypes().length == 0) {
                var methodName = method.getName();
                var propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                var mp = new MinimalMetaProperty<Object>(this, method, propertyName);
                map.put(propertyName, mp);
            }
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
        this.aliasMap = new HashMap<>();
    }

    private MinimalMetaBean(
            Class<T> beanType,
            Supplier<BeanBuilder<T>> builderSupplier,
            Map<String, MetaProperty<?>> metaPropertyMap,
            Map<String, String> aliasMap) {

        this.beanType = beanType;
        this.builderSupplier = builderSupplier;
        this.metaPropertyMap = metaPropertyMap;
        this.aliasMap = aliasMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds an alias to the meta-bean.
     * <p>
     * When using {@link #metaProperty(String)}, the alias will return the
     * meta-property of the real name.
     * 
     * @param alias  the alias
     * @param realName  the real name
     * @return the new meta-bean instance
     * @throws IllegalArgumentException if the realName is invalid
     */
    public MinimalMetaBean<T> withAlias(String alias, String realName) {
        if (!metaPropertyMap.containsKey(realName)) {
            throw new IllegalArgumentException("Invalid property name: " + realName);
        }
        var aliasMap = new HashMap<String, String>(this.aliasMap);
        aliasMap.put(alias, realName);
        return new MinimalMetaBean<>(beanType, builderSupplier, metaPropertyMap, aliasMap);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isBuildable() {
        return true;
    }

    @Override
    public BeanBuilder<T> builder() {
        return builderSupplier.get();
    }

    @Override
    public Class<T> beanType() {
        return beanType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> MetaProperty<R> metaProperty(String propertyName) {
        var mp = metaPropertyMap().get(aliasMap.getOrDefault(propertyName, propertyName));
        if (mp == null) {
            throw new NoSuchElementException("Unknown property: " + propertyName);
        }
        return (MetaProperty<R>) mp;
    }

    @Override
    public Map<String, MetaProperty<?>> metaPropertyMap() {
        return metaPropertyMap;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        return obj instanceof MinimalMetaBean<?> other &&
                this.beanType.equals(other.beanType);
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
