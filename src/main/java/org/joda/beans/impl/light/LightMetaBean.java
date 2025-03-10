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
package org.joda.beans.impl.light;

import static java.util.stream.Collectors.joining;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.DerivedProperty;
import org.joda.beans.gen.PropertyDefinition;

/**
 * A meta-bean implementation that operates using method handles.
 * <p>
 * The properties are found using the {@link PropertyDefinition} annotation.
 * There must be a constructor matching the property definitions (arguments of same order and types).
 * <p>
 * This uses method handles to avoid problems with reflection {@code setAccessible()} in Java SE 9.
 * 
 * @param <T>  the type of the bean
 */
public final class LightMetaBean<T extends Bean> implements TypedMetaBean<T> {

    /**
     * The empty object array.
     */
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /** The bean type. */
    private final Class<T> beanType;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;
    /** The aliases. */
    private final Map<String, String> aliasMap;
    /** The constructor to use. */
    private final Function<Object[], T> constructorFn;
    /** The construction data array. */
    private final Object[] constructionData;

    //-----------------------------------------------------------------------
    /**
     * Obtains an instance of the meta-bean using standard default values.
     * <p>
     * The properties will be determined using reflection to find the fields.
     * Each field must have a {@link PropertyDefinition} annotation.
     * The order of the properties is undefined as Java fields are not necessarily
     * returned in source code order.
     * <p>
     * The default values for primitives are determined automatically.
     * If the bean has non-primitive values like lists and maps that need defaulting
     * then {@link #of(Class, java.lang.invoke.MethodHandles.Lookup, String[], Object...)}
     * must be used.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param lookup  the method handle lookup, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> LightMetaBean<B> of(Class<B> beanType, MethodHandles.Lookup lookup) {
        // the field name order is undefined
        // but since they are not being matched against default values that is OK
        return new LightMetaBean<>(beanType, lookup, fieldNames(beanType), EMPTY_OBJECT_ARRAY);
    }

    // determine the field names by reflection
    private static String[] fieldNames(Class<?> beanType) {
        var fieldNames = new ArrayList<String>();
        for (var field : beanType.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(PropertyDefinition.class) != null) {
                fieldNames.add(field.getName());
            }
        }
        return fieldNames.toArray(new String[0]);
    }

    /**
     * Obtains an instance of the meta-bean specifying default values.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * <p>
     * The field names must be specified as reflection does not return fields in source code order.
     * The default values must be provided if they cannot be determined automatically.
     * Default values for primitives are determined automatically, but empty lists and maps are not.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param lookup  the method handle lookup, not null
     * @param fieldNames  the field names, one for each property, not null
     * @param defaultValues  the default values, one for each property, not null
     * @return the meta-bean, not null
     */
    public static <B extends Bean> LightMetaBean<B> of(
            Class<B> beanType,
            MethodHandles.Lookup lookup,
            String[] fieldNames,
            Object... defaultValues) {

        return new LightMetaBean<>(beanType, lookup, fieldNames, defaultValues);
    }

    /**
     * Constructor.
     */
    private LightMetaBean(
            Class<T> beanType,
            MethodHandles.Lookup lookup,
            String[] fieldNames,
            Object[] defaultValues) {

        Objects.requireNonNull(beanType, "beanType must not be null");
        Objects.requireNonNull(lookup, "lookup must not be null");
        Objects.requireNonNull(fieldNames, "fieldNames must not be null");
        Objects.requireNonNull(defaultValues, "defaultValues must not be null");
        if (defaultValues.length > 0 && defaultValues.length != fieldNames.length) {
            throw new IllegalArgumentException("Number of default values must match number of fields");
        }
        this.beanType = beanType;
        // handle ordered or random
        var map = new LinkedHashMap<String, MetaProperty<?>>();
        var propertyTypes = new ArrayList<Class<?>>();
        for (var fieldName : fieldNames) {
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
            var pdef = field.getAnnotation(PropertyDefinition.class);
            var name = field.getName();
            if (pdef.get().isEmpty() || pdef.get().equals("field") || pdef.get().startsWith("optional")) {
                map.put(name, LightMetaProperty.of(this, field, lookup, name, propertyTypes.size()));
            } else {
                var getterName = "get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                Method getMethod = null;
                if (field.getType() == boolean.class) {
                    getMethod = findGetMethod(
                            beanType, "is" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1));
                }
                if (getMethod == null) {
                    getMethod = findGetMethod(beanType, getterName);
                    if (getMethod == null) {
                        throw new IllegalArgumentException(
                                "Unable to find property getter: " + beanType.getSimpleName() + "." + getterName + "()");
                    }
                }
                Method setMethod = null;
                if (!ImmutableBean.class.isAssignableFrom(beanType)) {
                    var setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                    setMethod = findSetMethod(beanType, setterName, field.getType());
                    if (setMethod == null) {
                        throw new IllegalArgumentException(
                                "Unable to find property setter: " + beanType.getSimpleName() + "." + setterName + "()");
                    }
                }
                map.put(name, LightMetaProperty.of(this, field, getMethod, setMethod, lookup, name, propertyTypes.size()));
            }
            propertyTypes.add(field.getType());
        }
        var constructor = findConstructor(beanType, propertyTypes);
        var constructionData = defaultValues;
        if (defaultValues.length == 0) {
            constructionData = buildConstructionData(constructor);
        }
        // derived
        var methods = beanType.getDeclaredMethods();
        for (var method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(DerivedProperty.class) != null &&
                    method.getName().startsWith("get") &&
                    method.getName().length() > 3 &&
                    Character.isUpperCase(method.getName().charAt(3)) &&
                    method.getParameterTypes().length == 0) {
                var methodName = method.getName();
                var propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                var mp = LightMetaProperty.of(this, method, lookup, propertyName, -1);
                map.put(propertyName, mp);
            }
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
        this.aliasMap = new HashMap<>();
        this.constructionData = constructionData;
        var handle = findConstructorHandle(beanType, lookup, constructor);
        this.constructorFn = args -> build(handle, args);
    }

    /**
     * Constructor used internally.
     */
    private LightMetaBean(
            Class<T> beanType,
            Map<String, MetaProperty<?>> metaPropertyMap,
            Map<String, String> aliasMap,
            Function<Object[], T> constructorFn,
            Object[] constructionData) {
        
        this.beanType = beanType;
        this.metaPropertyMap = metaPropertyMap;
        this.aliasMap = aliasMap;
        this.constructorFn = constructorFn;
        this.constructionData = constructionData;
    }

    // finds a method on class or public method on super-type
    private static Method findGetMethod(Class<? extends Bean> beanType, String getterName) {
        try {
            return beanType.getDeclaredMethod(getterName);
        } catch (NoSuchMethodException ex) {
            try {
                return beanType.getMethod(getterName);
            } catch (NoSuchMethodException ex2) {
                return null;
            }
        }
    }

    // finds a method on class or public method on super-type
    private static Method findSetMethod(Class<? extends Bean> beanType, String setterName, Class<?> fieldType) {
        try {
            return beanType.getDeclaredMethod(setterName, fieldType);
        } catch (NoSuchMethodException ex) {
            var methods = beanType.getMethods();
            var potential = new ArrayList<Method>();
            for (var method : methods) {
                if (method.getName().equals(setterName) && method.getParameterTypes().length == 1) {
                    potential.add(method);
                }
            }
            if (potential.size() == 1) {
                return potential.get(0);
            }
            for (Method method : potential) {
                if (method.getParameterTypes()[0].equals(fieldType)) {
                    return method;
                }
            }
            return null;
        }
    }

    // finds constructor which matches types exactly
    private static <T extends Bean> MethodHandle findConstructorHandle(
            Class<T> beanType,
            MethodHandles.Lookup lookup,
            Constructor<?> constructor) {

        try {
            // spreader allows an Object[] to invoke the positional arguments
            var constructorType = MethodType.methodType(Void.TYPE, constructor.getParameterTypes());
            var baseHandle = lookup.findConstructor(beanType, constructorType)
                    .asSpreader(Object[].class, constructor.getParameterTypes().length);
            // change the return type so caller can use invokeExact()
            return baseHandle.asType(baseHandle.type().changeReturnType(Bean.class));
        } catch (NoSuchMethodException ex) {
            throw new IllegalArgumentException("Unable to find constructor: " + beanType.getSimpleName());
        } catch (IllegalAccessException ex) {
            throw new IllegalArgumentException("Unable to access constructor: " + beanType.getSimpleName());
        }
    }

    // finds constructor which matches types exactly
    private static <T extends Bean> Constructor<T> findConstructor(Class<T> beanType, List<Class<?>> propertyTypes) {
        var types = propertyTypes.toArray(new Class<?>[0]);
        try {
            return beanType.getDeclaredConstructor(types);
        } catch (NoSuchMethodException ex) {
            return findConstructorFallback(beanType, types, ex);
        }
    }

    // try a more lenient search
    // this handles cases where field is a concrete class and constructor is an interface
    private static <T extends Bean> Constructor<T> findConstructorFallback(Class<T> beanType, Class<?>[] types, NoSuchMethodException ex) {
        @SuppressWarnings("unchecked")
        var cons = (Constructor<T>[]) beanType.getDeclaredConstructors();
        Constructor<T> match = null;
        outer:
        for (var con : cons) {
            var conTypes = con.getParameterTypes();
            if (conTypes.length == types.length) {
                for (var j = 0; j < types.length; j++) {
                    if (!conTypes[j].isAssignableFrom(types[j])) {
                        continue outer;
                    }
                }
                if (match != null) {
                    throw new UnsupportedOperationException("Unable to find constructor: More than one matches");
                }
                match = con;
            }
        }
        if (match == null) {
            var signature = Stream.of(types).map(Class::getName).collect(joining(", "));
            var msg = "Unable to find constructor: " + beanType.getSimpleName() + '(' + signature + ')';
            throw new UnsupportedOperationException(msg, ex);
        }
        return match;
    }

    // array used to collect data when building
    // needs to have default values for primitives
    // note that this does not handle empty collections/maps
    private static Object[] buildConstructionData(Constructor<?> constructor) {
        var parameterTypes = constructor.getParameterTypes();
        var args = new Object[parameterTypes.length];
        for (var i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == boolean.class) {
                args[i] = false;
            } else if (parameterTypes[i] == int.class) {
                args[i] = 0;
            } else if (parameterTypes[i] == long.class) {
                args[i] = (long) 0;
            } else if (parameterTypes[i] == short.class) {
                args[i] = (short) 0;
            } else if (parameterTypes[i] == byte.class) {
                args[i] = (byte) 0;
            } else if (parameterTypes[i] == float.class) {
                args[i] = (float) 0;
            } else if (parameterTypes[i] == double.class) {
                args[i] = (double) 0;
            } else if (parameterTypes[i] == char.class) {
                args[i] = (char) 0;
            }
        }
        return args;
    }

    @SuppressWarnings("unchecked")
    private T build(MethodHandle handle, Object[] args) {
        try {
            return (T) handle.invokeExact(args);
            
        } catch (Error ex) {
            throw ex;
        } catch (Throwable ex) {
            throw new IllegalArgumentException(
                    "Bean cannot be created: " + beanName() + " from " + Arrays.toString(args), ex);
        }
    }

    //-----------------------------------------------------------------------
    T build(Object[] args) {
        return constructorFn.apply(args);
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
    public LightMetaBean<T> withAlias(String alias, String realName) {
        if (!metaPropertyMap.containsKey(realName)) {
            throw new IllegalArgumentException("Invalid property name: " + realName);
        }
        var aliasMap = new HashMap<>(this.aliasMap);
        aliasMap.put(alias, realName);
        return new LightMetaBean<>(beanType, metaPropertyMap, aliasMap, constructorFn, constructionData);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isBuildable() {
        return true;
    }

    @Override
    public BeanBuilder<T> builder() {
        return new LightBeanBuilder<>(this, constructionData.clone());
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
        return obj instanceof LightMetaBean<?> other &&
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
