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
package org.joda.beans;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Objects;

/**
 * A resolved generic type.
 * <p>
 * This stores a generic type, where any type variables or wildcards have been resolved.
 * 
 * @since 3.0.0
 */
public final class ResolvedType {

    private static final ResolvedType OBJECT = new ResolvedType(Object.class, List.of());

    /**
     * The raw type.
     */
    private final Class<?> rawType;
    /**
     * The type arguments.
     */
    private final List<ResolvedType> arguments;

    /**
     * Restricted constructor.
     */
    private ResolvedType(Class<?> rawType, List<ResolvedType> arguments) {
        this.rawType = rawType;
        this.arguments = arguments;
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance from a raw type.
     * 
     * @param rawType  the raw type, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType of(Class<?> rawType) {
        Objects.requireNonNull(rawType, "rawType");
        return new ResolvedType(rawType, List.of());
    }

    /**
     * Obtains an instance from a raw type and type arguments.
     * <p>
     * The 
     * 
     * @param rawType  the raw type, not null
     * @param arguments  the type arguments, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType of(Class<?> rawType, ResolvedType... arguments) {
        Objects.requireNonNull(rawType, "rawType");
        Objects.requireNonNull(arguments, "arguments");
        return new ResolvedType(rawType, List.of(arguments));
    }

    /**
     * Obtains an instance from a raw type and type arguments.
     * 
     * @param rawType  the raw type, not null
     * @param arguments  the type arguments, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType of(Class<?> rawType, Class<?>... arguments) {
        Objects.requireNonNull(rawType, "rawType");
        Objects.requireNonNull(arguments, "arguments");
        var resolved = new ResolvedType[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            resolved[i] = ResolvedType.of(arguments[i]);
        }
        return new ResolvedType(rawType, List.of(resolved));
    }

    /**
     * Obtains an instance from a raw type and type arguments.
     * 
     * @param type  the type to resolve, not null
     * @param contextClass  the context class to evaluate against, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType of(Type type, Class<?> contextClass) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(contextClass, "contextClass");
        return switch (type) {
            case Class<?> cls -> of(cls);
            case ParameterizedType parameterizedType -> {
                var typeArguments = new ResolvedType[parameterizedType.getActualTypeArguments().length];
                for (int i = 0; i < typeArguments.length; i++) {
                    typeArguments[i] = of(parameterizedType.getActualTypeArguments()[i], contextClass);
                }
                if (parameterizedType.getRawType() instanceof Class<?> rawType) {
                    yield new ResolvedType(rawType, List.of(typeArguments));
                }
                throw new IllegalArgumentException("Unknown generic type class: " + type);
            }
            case GenericArrayType arrType -> {
                var componentType = of(arrType.getGenericComponentType(), contextClass);
                // TODO need to create raw array type, and handle nested arrays
                yield createGenericArrayType(componentType);
            }
            case TypeVariable<?> tvar -> {
                /// TODO: need to actually resolve using contextClass
                var bounds = tvar.getBounds();
                yield bounds.length == 0 ? OBJECT : of(bounds[0], contextClass);
            }
            case WildcardType wild -> {
                var bounds = wild.getUpperBounds();
                yield bounds.length == 0 ? OBJECT : of(bounds[0], contextClass);
            }
            default -> throw new IllegalArgumentException("Unknown generic type class: " + type);
        };
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the raw type.
     * 
     * @return the raw type, may be a primitive or an array type, not null
     */
    public Class<?> getRawType() {
        return rawType;
    }

    /**
     * Gets the type arguments, empty if the type is not a parameterized type or a generic array type.
     * 
     * @return the type arguments, not null
     */
    public List<ResolvedType> getArguments() {
        return arguments;
    }

    /**
     * Gets the matching type argument.
     * 
     * @param size  the number of generic parameters expected
     * @param index  the index of the generic parameter
     * @return the type
     * @throws IllegalArgumentException if the size is invalid
     * @throws IndexOutOfBoundsException if the index is invalid
     */
    public ResolvedType getArgument(int size, int index) {
        if (arguments.size() != size) {
            throw new IllegalArgumentException(
                    "Unexpected generic type, expected " + size + " arguments, but was " + arguments.size());
        }
        return arguments.get(index);
    }

    //-------------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResolvedType other &&
                this.rawType == other.rawType &&
                arguments.equals(other.arguments);
    }

    @Override
    public int hashCode() {
        return rawType.hashCode() ^ arguments.hashCode();
    }

    @Override
    public String toString() {
        if (arguments.isEmpty()) {
            return rawType.getName();
        } else {
            // TODO: nested arrays
            var baseType = rawType.isArray() ? rawType.getComponentType() : rawType;
            var builder = new StringBuilder().append(baseType.getName()).append('<');
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(arguments.get(i));
            }
            builder.append('>');
            if (rawType.isArray()) {
                builder.append("[]");
            }
            return builder.toString();
        }
    }

}
