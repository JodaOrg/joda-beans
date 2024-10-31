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
package org.joda.beans.ser;

import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

/**
 * Assists with serialization and deserialization of optional properties.
 */
public class SerOptional {

    /** Guava Optional type. */
    private static final Class<?> GUAVA_OPTIONAL_CLASS;
    /** Extractor for Guava Optional. */
    private static final Function<Object, Object> GUAVA_EXTRACT;
    /** Wrapper for Guava Optional. */
    private static final BiFunction<Class<?>, Object, Object> GUAVA_WRAPPER;
    static {
        Class<?> optionalType;
        Function<Object, Object> extract;
        BiFunction<Class<?>, Object, Object> wrapper;
        try {
            optionalType = GuavaSerOptional.OPTIONAL_TYPE;
            extract = GuavaSerOptional::extractValue;
            wrapper = GuavaSerOptional::wrapValue;
        } catch (RuntimeException | LinkageError ex) {
            optionalType = Optional.class;
            extract = Function.identity();
            wrapper = (cls, value) -> value;
        }
        GUAVA_OPTIONAL_CLASS = optionalType;
        GUAVA_EXTRACT = extract;
        GUAVA_WRAPPER = wrapper;
    }

    /**
     * Extracts the value of the property from a bean, unwrapping any optional.
     * 
     * @param metaProp  the property to query, not null
     * @param bean  the bean to query, not null
     * @return the value of the property, with any optional wrapper removed
     */
    public static Object extractValue(MetaProperty<?> metaProp, Bean bean) {
        var value = metaProp.get(bean);
        return switch (value) {
            case null -> null;
            case Optional<?> opt -> opt.orElse(null);
            case OptionalLong opt -> opt.isPresent() ? opt.getAsLong() : null;
            case OptionalInt opt -> opt.isPresent() ? opt.getAsInt() : null;
            case OptionalDouble opt -> opt.isPresent() ? opt.getAsDouble() : null;
            default -> GUAVA_EXTRACT.apply(value);
        };
    }

    /**
     * Extracts the type the optional is wrapping.
     * 
     * @param metaProp  the property to query, not null
     * @param beanType  the type of the bean, not null
     * @return the type of the property with any optional wrapper removed
     */
    public static Class<?> extractType(MetaProperty<?> metaProp, Class<?> beanType) {
        var propType = metaProp.propertyType();
        if (propType == Optional.class ||
                propType == OptionalLong.class ||
                propType == OptionalInt.class ||
                propType == OptionalDouble.class ||
                propType == GUAVA_OPTIONAL_CLASS) {
            return extractType(metaProp, beanType, propType);
        }
        return propType;
    }

    // broken out for hotspot
    private static Class<?> extractType(MetaProperty<?> metaProp, Class<?> beanType, Class<?> type) {
        try {
            var genericType = JodaBeanUtils.extractTypeClass(metaProp, beanType, 1, 0);
            return (genericType != null ? genericType : type);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Wraps the value of a property if it is an optional.
     * 
     * @param metaProp  the property to query, not null
     * @param beanType  the type of the bean, not null
     * @param value  the value to wrap, may be null
     * @return the value of the property, with any optional wrapper added
     */
    public static Object wrapValue(MetaProperty<?> metaProp, Class<?> beanType, Object value) {
        var propType = metaProp.propertyType();
        if (propType == Optional.class) {
            return Optional.ofNullable(value);
        } else if (propType == OptionalLong.class) {
            return value == null ? OptionalLong.empty() : OptionalLong.of((Long) value);
        } else if (propType == OptionalInt.class) {
            return value == null ? OptionalInt.empty() : OptionalInt.of((Integer) value);
        } else if (propType == OptionalDouble.class) {
            return value == null ? OptionalDouble.empty() : OptionalDouble.of((Double) value);
        } else {
            return GUAVA_WRAPPER.apply(propType, value);
        }
    }

    //-------------------------------------------------------------------------
    // a separate class so that it can fail to load if Guava is missing
    private static final class GuavaSerOptional {
        public static final Class<?> OPTIONAL_TYPE = com.google.common.base.Optional.class;

        private static Object extractValue(Object value) {
            return value instanceof com.google.common.base.Optional<?> opt ? opt.orNull() : value;
        }

        public static Object wrapValue(Class<?> propType, Object value) {
            if (propType == OPTIONAL_TYPE) {
                return com.google.common.base.Optional.fromNullable(value);
            }
            return value;
        }
    }

}
