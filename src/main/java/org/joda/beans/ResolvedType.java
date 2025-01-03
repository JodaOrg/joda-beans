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

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.SequencedCollection;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.convert.FromString;
import org.joda.convert.ToString;

/**
 * A resolved generic type.
 * <p>
 * This stores a generic type, where any type variables or wildcards have been resolved.
 * The process of resolving uses a context class to resolve type variables like {@code <T>}.
 * <p>
 * Where type variables cannot be resolved, the type parameter upper bound will be used.
 * For example, {@code ResolvedType.of(List.class)} returns {@code List<Object>} unless a context
 * class is passed in that extends {@code List} and constrains the type, such as an imaginary class
 * {@code StringList implements List<String>}.
 * <p>
 * Note that special cases like anonymous classes, hidden classes and specialized enum subclasses
 * are not resolved.
 * 
 * @since 3.0.0
 */
public final class ResolvedType {

    /**
     * The resolved type for {@code Object.class}.
     */
    public static final ResolvedType OBJECT = new ResolvedType(Object.class);
    /**
     * The resolved type for {@code String.class}.
     */
    public static final ResolvedType STRING = new ResolvedType(String.class);

    /**
     * Pattern for class name.
     */
    private static final Pattern PARSE_CLASS_NAME = Pattern.compile("([^<>, \\[]+)");
    /**
     * Pattern for separator.
     */
    private static final Pattern PARSE_SEPARATOR = Pattern.compile("(([^<>, \\[]+)|<|>|, *|(?:\\[\\])+)");
    /**
     * The short class names.
     */
    private static final Map<String, Class<?>> NAMES = Set.of(
            Object.class,
            String.class,
            Number.class,
            Long.class,
            Integer.class,
            Short.class,
            Byte.class,
            Double.class,
            Float.class,
            Character.class,
            Boolean.class,
            Void.class,
            long.class,
            int.class,
            short.class,
            byte.class,
            double.class,
            float.class,
            char.class,
            boolean.class,
            void.class,
            Collection.class,
            SequencedCollection.class,
            Set.class,
            SortedSet.class,
            NavigableSet.class,
            SequencedSet.class,
            List.class,
            Map.class,
            SortedMap.class,
            NavigableMap.class,
            SequencedMap.class).stream()
            .collect(toMap(cls -> cls.getSimpleName(), cls -> cls));

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
    private ResolvedType(Class<?> rawType) {
        // validation occurs in the calling code
        this.rawType = rawType;
        this.arguments = List.of();
    }

    /**
     * Restricted constructor.
     */
    private ResolvedType(Class<?> rawType, List<ResolvedType> arguments) {
        // validation occurs in the calling code
        this.rawType = rawType;
        this.arguments = arguments;
    }

    //-------------------------------------------------------------------------
    /**
     * Parses a {@code ResolvedType} from a formal string.
     * 
     * @param str  the string to parse, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     * @throws IllegalArgumentException if the string cannot be parsed
     */
    @FromString
    public static ResolvedType parse(String str) {
        Objects.requireNonNull(str, "str must not be null");
        var matcher = PARSE_CLASS_NAME.matcher(str);
        return parse(matcher, str, true);
    }

    // recursively parse using a matcher that retains the current state
    private static ResolvedType parse(Matcher matcher, String str, boolean root) {
        // first parse the class name
        matcher.usePattern(PARSE_CLASS_NAME);
        if (!matcher.find()) {
            throw invalidFormat(str);
        }
        var name = matcher.group(1);
        var parsedRawType = NAMES.get(name);
        if (parsedRawType == null) {
            try {
                parsedRawType = Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new IllegalArgumentException("Unable to parse ResolvedType, class not found: " + name);
            }
        }
        var typeArgs = new ArrayList<ResolvedType>();
        // then parse any trailing separators
        matcher.usePattern(PARSE_SEPARATOR);
        while (matcher.find()) {
            var separator = matcher.group(1);
            // parse type arguments, which may be comma separated
            if (separator.equals("<")) {
                if (!typeArgs.isEmpty()) {
                    throw invalidFormat(str);
                }
                typeArgs.add(parse(matcher, str, false));
                if (!matcher.hasMatch()) {
                    throw invalidFormat(str);
                }
                separator = matcher.group(1);
                while (separator.startsWith(",")) {
                    typeArgs.add(parse(matcher, str, false));
                    separator = matcher.group(1);
                }
                continue;
            }
            // ends the current type argument, at the root level it is an error
            if (separator.startsWith(",") | separator.equals(">")) {
                if (root) {
                    throw invalidFormat(str);
                }
                return of(parsedRawType, typeArgs);
            }
            // handle an array
            if (separator.startsWith("[")) {
                for (var i = 0; i < separator.length() / 2; i++) {
                    parsedRawType = parsedRawType.arrayType();
                }
                continue;
            }
            // found some unexpected text
            throw invalidFormat(str);
        }
        return of(parsedRawType, typeArgs);
    }

    private static IllegalArgumentException invalidFormat(String str) {
        return new IllegalArgumentException("Unable to parse ResolvedType from '" + str + "', invalid format");
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance from a raw type.
     * <p>
     * This factory method is most useful for wrapping types that are not generic, such as {@code String}.
     * If the input class has generic type parameters, the result will represent the raw type, and the arguments will be empty.
     * Use {@link #from(Class)} if you want a method that defaults the type parameters to their upper bounds.
     * <p>
     * For example, passing {@code Map.class} to this method will simply wrap the input returning "Map",
     * and not "Map&lt;Object, Object&gt;".
     * 
     * @param rawType  the raw type, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType of(Class<?> rawType) {
        Objects.requireNonNull(rawType, "rawType must not be null");
        return new ResolvedType(rawType);
    }

    /**
     * Obtains an instance from a raw type and type arguments.
     * <p>
     * The number of type arguments must match the number of type parameters on the raw type or be empty.
     * For example, calling this method with {@code Map.class} requires the type arguments to either be
     * size 2 (generified) or size 0 (raw).
     * 
     * @param rawType  the raw type, not null
     * @param arguments  the type arguments, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     * @throws IllegalArgumentException if the number of arguments do not match the number of generics type parameters or zero
     */
    public static ResolvedType of(Class<?> rawType, ResolvedType... arguments) {
        Objects.requireNonNull(rawType, "rawType must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        return of(rawType, List.of(arguments));
    }

    /**
     * Obtains an instance from a raw type and type arguments.
     * <p>
     * The number of type arguments must match the number of type parameters on the raw type or be empty.
     * For example, calling this method with {@code Map.class} requires the type arguments to either be
     * size 2 (generified) or size 0 (raw).
     * 
     * @param rawType  the raw type, not null
     * @param arguments  the type arguments, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     * @throws IllegalArgumentException if the number of arguments do not match the number of generics type parameters or zero
     */
    public static ResolvedType of(Class<?> rawType, List<ResolvedType> arguments) {
        Objects.requireNonNull(rawType, "rawType must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        if (arguments.isEmpty()) {
            return new ResolvedType(rawType);
        }
        var baseType = extractBaseComponentType(rawType);
        var actualTypeParamCount = baseType.getTypeParameters().length;
        if (actualTypeParamCount != arguments.size()) {
            throw invalidTypeParamCount(rawType, actualTypeParamCount, arguments.size());
        }
        return new ResolvedType(rawType, List.copyOf(arguments));
    }

    private static IllegalArgumentException invalidTypeParamCount(Class<?> rawType, int actualCount, int inputCount) {
        return new IllegalArgumentException(
                "Class " + rawType.getName() + " has " + actualCount + " type parameters, but " + inputCount + " were supplied");
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance from a single-level flat list of raw types.
     * <p>
     * This factory method is most useful for wrapping types with single-level generics like {@code List<String>},
     * simply call {@code ofFlat(List.class, String.class)}.
     * <p>
     * Multi-level generic classes can be created, however the nested classes will be raw.
     * For example, {@code ofFlat(List.class, Optional.class} will resolve to "List&lt;Optional&gt;",
     * and not "List&lt;Optional&lt;Object&gt;&gt;".
     * To provide nested generics for {@code Optional} use {@link #of(Class, ResolvedType...)}.
     * 
     * @param rawType  the raw type, not null
     * @param arguments  the type arguments, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     * @throws IllegalArgumentException if the number of arguments do not match the number of generics type parameters or zero
     */
    public static ResolvedType ofFlat(Class<?> rawType, Class<?>... arguments) {
        Objects.requireNonNull(rawType, "rawType must not be null");
        Objects.requireNonNull(arguments, "arguments must not be null");
        if (arguments.length == 0) {
            return new ResolvedType(rawType);
        }
        var resolvedArgs = new ResolvedType[arguments.length];
        for (var i = 0; i < arguments.length; i++) {
            resolvedArgs[i] = new ResolvedType(arguments[i]);
        }
        return of(rawType, List.of(resolvedArgs));
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance from a raw type, defaulting any type parameters.
     * <p>
     * If the input class has generic type parameters, they will be resolved to their upper bound,
     * typically {@code Object.class}. Use {@link #of(Class)} if you simply want to obtain a wrapper around the raw type.
     * <p>
     * For example, passing {@code Map.class} to this method will determine the upper bounds, returning "Map&lt;Object, Object&gt;".
     * 
     * @param rawType  the raw type, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType from(Class<?> rawType) {
        Objects.requireNonNull(rawType, "rawType must not be null");
        return resolveClass(rawType, Object.class);
    }

    /**
     * Obtains an instance from a type and context class.
     * <p>
     * The type is typically obtained from reflection, such as from {@link MetaProperty#propertyGenericType()}.
     * The context class represents the {@code Class} associated with the object being queried,
     * which is used to resolve type variables like {@code <T>}.
     * 
     * @param type  the type to resolve, not null
     * @param contextClass  the context class to evaluate against, not null
     * @return the resolved type
     * @throws NullPointerException if null is passed in
     */
    public static ResolvedType from(Type type, Class<?> contextClass) {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(contextClass, "contextClass must not be null");
        return switch (type) {
            case Class<?> cls -> resolveClass(cls, contextClass);
            case ParameterizedType parameterizedType -> resolveParameterizedType(parameterizedType, contextClass);
            case GenericArrayType arrType -> resolveGenericArrayType(arrType, contextClass);
            case TypeVariable<?> tvar -> resolveTypeVariable(tvar, contextClass);
            case WildcardType wild -> resolveWildcard(wild, contextClass);
            default -> throw unknownGenericTypeClass(type);
        };
    }

    // should never happen, but might do if someone implements Type manually, or the JDK adds a new kind of Type
    private static IllegalArgumentException unknownGenericTypeClass(Type type) {
        return new IllegalArgumentException("Unknown generic type class: " + type);
    }

    // resolve a Class
    private static ResolvedType resolveClass(Class<?> cls, Class<?> contextClass) {
        var baseType = extractBaseComponentType(cls);
        var typeVariables = baseType.getTypeParameters();
        var typeArguments = new ResolvedType[typeVariables.length];
        for (var i = 0; i < typeArguments.length; i++) {
            typeArguments[i] = resolveTypeVariable(typeVariables[i], contextClass);
        }
        return new ResolvedType(cls, List.of(typeArguments));
    }

    // resolve things like List<String>
    private static ResolvedType resolveParameterizedType(ParameterizedType parameterizedType, Class<?> contextClass) {
        var actualTypeArguments = parameterizedType.getActualTypeArguments();
        var typeArguments = new ResolvedType[actualTypeArguments.length];
        for (var i = 0; i < typeArguments.length; i++) {
            typeArguments[i] = from(actualTypeArguments[i], contextClass);
        }
        // all known instances of ParameterizedType return Class
        return new ResolvedType((Class<?>) parameterizedType.getRawType(), List.of(typeArguments));
    }

    // resolve things like Optional<String>[]
    private static ResolvedType resolveGenericArrayType(GenericArrayType arrType, Class<?> contextClass) {
        var componentType = arrType.getGenericComponentType();  // Optional<String>
        var componentResolvedType = from(componentType, contextClass);  // Optional<String>
        var rawType = componentResolvedType.getRawType();  // Optional
        return new ResolvedType(rawType.arrayType(), componentResolvedType.getArguments());
    }

    // resolve things like <T extends Comparable<T>>
    private static ResolvedType resolveTypeVariable(TypeVariable<?> tvar, Class<?> contextClass) {
        var resolved = JodaBeanUtils.resolveGenerics(tvar, contextClass);
        if (resolved instanceof TypeVariable<?> unresolved) {
            var bounds = unresolved.getBounds();
            return bounds.length > 0 ? resolveGenericBound(bounds[0]) : OBJECT;
        }
        return from(resolved, contextClass);
    }

    // resolve things like <T extends Comparable<T>>
    private static ResolvedType resolveGenericBound(Type bound) {
        return switch (bound) {
            case Class<?> cls -> resolveClass(cls, Object.class);
            case ParameterizedType pt -> {
                var rawType = JodaBeanUtils.eraseToClass(pt.getRawType());
                var typeArgs = pt.getActualTypeArguments();
                var resolvedTypeArgs = new ResolvedType[typeArgs.length];
                if (typeArgs.length == 0) {
                    yield resolveClass(rawType, Object.class);  // ignore weird situations
                }
                for (var i = 0; i < typeArgs.length; i++) {
                    resolvedTypeArgs[i] = typeArgs[i] instanceof TypeVariable<?> ?
                            OBJECT :  // resolve <T extends Comparable<T>> into Comparable<Object>
                            resolveGenericBound(typeArgs[i]);
                }
                yield of(rawType, resolvedTypeArgs);
            }
            default -> resolveClass(JodaBeanUtils.eraseToClass(bound), Object.class);  // ignore weird situations
        };
    }

    // resolves a wildcard
    private static ResolvedType resolveWildcard(WildcardType wild, Class<?> contextClass) {
        var bounds = wild.getUpperBounds();
        return bounds.length == 0 ? OBJECT : from(bounds[0], contextClass);
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the raw type.
     * 
     * @return the raw type, may be a primitive type or an array type, not null
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
     * Gets the matching type argument or the default value of {@code Object}.
     * <p>
     * No check is performed to see if the index is valid for the raw type.
     * For example, you could request index 3 from a {@code Map}.
     * 
     * @param index  the index of the generic parameter
     * @return the type, defaulted to Object
     */
    public ResolvedType getArgumentOrDefault(int index) {
        if (index < 0 || index >= arguments.size()) {
            return OBJECT;
        }
        return arguments.get(index);
    }

    //-------------------------------------------------------------------------
    /**
     * Checks whether this is a parameterized generic type, irrespective of whether the type arguments are known.
     * <p>
     * For example, "List" and "List&lt;String&gt;" return true, while "String" returns false.
     * 
     * @return true if this is a type with generic type parameters
     */
    public boolean isParameterized() {
        return rawType.getTypeParameters().length != 0;
    }

    /**
     * Checks whether this is a raw type.
     * 
     * @return true if this is a parameterized generic type but the type arguments are empty
     */
    public boolean isRaw() {
        return arguments.isEmpty() && isParameterized();
    }

    /**
     * Checks whether this is a primitive type.
     * 
     * @return true if this is one of the 8 primitive types or void
     */
    public boolean isPrimitive() {
        return rawType.isPrimitive();
    }

    /**
     * Checks whether this is an array type.
     * 
     * @return true if this is an array type
     */
    public boolean isArray() {
        return rawType.isArray();
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the raw type, effectively dropping the generics.
     * 
     * @return the underlying raw type, as a {@code ResolvedType}
     */
    public ResolvedType toRawType() {
        return arguments.isEmpty() ? this : new ResolvedType(rawType);
    }

    /**
     * Returns the boxed type equivalent to this type.
     * <p>
     * If this type is one of the nine primitive types, the equivalent box is returned.
     * Otherwise, {@code this} is returned.
     * 
     * @return the equivalent boxed type
     */
    public ResolvedType toBoxed() {
        return rawType.isPrimitive() ? boxed() : this;
    }

    private ResolvedType boxed() {
        if (rawType == int.class) {
            return ResolvedType.of(Integer.class);
        } else if (rawType == long.class) {
            return ResolvedType.of(Long.class);
        } else if (rawType == double.class) {
            return ResolvedType.of(Double.class);
        } else if (rawType == boolean.class) {
            return ResolvedType.of(Boolean.class);
        } else if (rawType == byte.class) {
            return ResolvedType.of(Byte.class);
        } else if (rawType == char.class) {
            return ResolvedType.of(Character.class);
        } else if (rawType == short.class) {
            return ResolvedType.of(Short.class);
        } else if (rawType == float.class) {
            return ResolvedType.of(Float.class);
        } else {
            return ResolvedType.of(Void.class);
        }
    }

    /**
     * Gets the component type if the raw type is an array.
     * <p>
     * Note that the component type may be an array type if this type is a higher-dimension array.
     * 
     * @return the component type
     * @throws IllegalStateException if the type is not an array
     */
    public ResolvedType toComponentType() {
        var componentType = rawType.getComponentType();
        if (componentType == null) {
            throw invalidArrayType();
        }
        return new ResolvedType(componentType, arguments);
    }

    private IllegalStateException invalidArrayType() {
        return new IllegalStateException("Unable to get component type for " + this + ", type is not an array");
    }

    /**
     * Gets the component type if the raw type is an array, defaulting to {@code Object} if the type is not an array.
     * <p>
     * Note that the component type may be an array type if this type is a higher-dimension array.
     * 
     * @return the component type
     */
    public ResolvedType toComponentTypeOrDefault() {
        var componentType = rawType.getComponentType();
        if (componentType == null) {
            return ResolvedType.OBJECT;
        }
        return new ResolvedType(componentType, arguments);
    }

    /**
     * Returns an instance representing the array type of the raw type.
     * 
     * @return the array type
     * @throws UnsupportedOperationException if this type represents {@code void}
     */
    public ResolvedType toArrayType() {
        return new ResolvedType(rawType.arrayType(), arguments);
    }

    //-------------------------------------------------------------------------
    private static Class<?> extractBaseComponentType(Class<?> cls) {
        var nonArrayType = cls;
        while (nonArrayType.isArray()) {
            nonArrayType = nonArrayType.getComponentType();
        }
        return nonArrayType;
    }

    //-------------------------------------------------------------------------
    /**
     * Checks if this object equals another.
     * 
     * @param obj  the other object
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof ResolvedType other &&
                this.rawType == other.rawType &&
                arguments.equals(other.arguments);
    }

    /**
     * Returns a hash code compatible with equals.
     * 
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return rawType.hashCode() ^ arguments.hashCode();
    }

    /**
     * Returns a formal string representation of the type.
     * 
     * @return the formal string
     */
    @Override
    @ToString
    public String toString() {
        var baseType = rawType;
        var suffix = "";
        while (baseType.isArray()) {
            baseType = baseType.getComponentType();
            suffix += "[]";
        }
        var shortenedClassName = shortenedClassName(baseType);
        if (arguments.isEmpty()) {
            return shortenedClassName + suffix;
        } else {
            var builder = new StringBuilder(shortenedClassName).append('<');
            for (var i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    builder.append(", ");
                }
                builder.append(arguments.get(i));
            }
            builder.append('>');
            if (rawType.isArray()) {
                builder.append(suffix);
            }
            return builder.toString();
        }
    }

    private String shortenedClassName(Class<?> cls) {
        var name = cls.getName();
        if (cls.isPrimitive()) {
            return name;
        }
        if (NAMES.containsValue(cls)) {
            return cls.getSimpleName();
        }
        return name;
    }

}
