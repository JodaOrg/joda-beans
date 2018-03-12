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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
 * The old reflection approach is still present, but deprecated.
 * 
 * @author Stephen Colebourne
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

    /**
     * Obtains an instance of the meta-bean.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * 
     * @param <B>  the type of the bean
     * @param beanClass  the bean class, not null
     * @return the meta-bean, not null
     * @deprecated Use method handles version of this method
     */
    @Deprecated
    public static <B extends Bean> LightMetaBean<B> of(Class<B> beanClass) {
        return new LightMetaBean<>(beanClass);
    }

    /**
     * Constructor.
     * @param beanType  the type
     * @deprecated Use method handles version of this method
     */
    @Deprecated
    private LightMetaBean(Class<T> beanType) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<?>> map = new LinkedHashMap<>();
        Field[] fields = beanType.getDeclaredFields();
        List<Class<?>> propertyTypes = new ArrayList<>();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(PropertyDefinition.class) != null) {
                // handle code that uses new annotation location but old meta-bean approach
                PropertyDefinition pdef = field.getAnnotation(PropertyDefinition.class);
                String name = field.getName();
                if (pdef.get().equals("field") || pdef.get().startsWith("optional") || pdef.get().equals("")) {
                    field.setAccessible(true);
                    if (!ImmutableBean.class.isAssignableFrom(beanType)) {
                        map.put(name, MutableLightMetaProperty.of(this, field, name, propertyTypes.size()));
                    } else {
                        map.put(name, ImmutableLightMetaProperty.of(this, field, name, propertyTypes.size()));
                    }
                } else {
                    String getterName = "get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                    Method getMethod = null;
                    if (field.getType() == boolean.class) {
                        getMethod = findGetMethod(beanType,
                                "is" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1));
                    }
                    if (getMethod == null) {
                        getMethod = findGetMethod(beanType, getterName);
                        if (getMethod == null) {
                            throw new IllegalArgumentException(
                                    "Unable to find property getter: " + beanType.getSimpleName() + "." + getterName + "()");
                        }
                    }
                    getMethod.setAccessible(true);
                    if (ImmutableBean.class.isAssignableFrom(beanType)) {
                        map.put(name, ImmutableLightMetaProperty.<Object>of(this, field, getMethod, name, propertyTypes.size()));

                    } else {
                        String setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                        Method setMethod = findSetMethod(beanType, setterName, field.getType());
                        if (setMethod == null) {
                            throw new IllegalArgumentException(
                                    "Unable to find property setter: " + beanType.getSimpleName() + "." + setterName + "()");
                        }
                        map.put(name, MutableLightMetaProperty.of(
                                this, field, getMethod, setMethod, name, propertyTypes.size()));
                    }
                }
                propertyTypes.add(field.getType());

            } else if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers())) {
                // handle annotation moving package
                // this is a best efforts approach
                String name = field.getName();
                field.setAccessible(true);
                if (!ImmutableBean.class.isAssignableFrom(beanType) && !Modifier.isFinal(field.getModifiers())) {
                    map.put(name, MutableLightMetaProperty.of(this, field, name, propertyTypes.size()));
                } else {
                    map.put(name, ImmutableLightMetaProperty.of(this, field, name, propertyTypes.size()));
                }
                propertyTypes.add(field.getType());
            }
        }
        // derived
        Method[] methods = beanType.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(DerivedProperty.class) != null &&
                    method.getName().startsWith("get") &&
                    method.getName().length() > 3 &&
                    Character.isUpperCase(method.getName().charAt(3)) &&
                    method.getParameterTypes().length == 0) {
                String methodName = method.getName();
                String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                if (!Modifier.isPublic(method.getModifiers())) {
                    method.setAccessible(true);
                }
                MetaProperty<Object> mp = ImmutableLightMetaProperty.of(this, method, propertyName, -1);
                map.put(propertyName, mp);
            }
        }

        this.metaPropertyMap = Collections.unmodifiableMap(map);
        this.aliasMap = new HashMap<>();
        Constructor<T> construct = findConstructor(beanType, propertyTypes);
        construct.setAccessible(true);
        this.constructionData = buildConstructionData(construct);
        this.constructorFn = args -> build(construct, args);
    }

    /**
     * Creates an instance of the bean.
     * 
     * @param constructor  the constructor
     * @param args  the arguments
     * @return the created instance
     * @deprecated Use method handles version of this method
     */
    @Deprecated
    private T build(Constructor<T> constructor, Object[] args) {
        try {
            return constructor.newInstance(args);

        } catch (IllegalArgumentException | IllegalAccessException | InstantiationException ex) {
            throw new IllegalArgumentException(
                    "Bean cannot be created: " + beanName() + " from " + args, ex);
        } catch (InvocationTargetException ex) {
            if (ex.getCause() instanceof RuntimeException) {
                throw (RuntimeException) ex.getCause();
            }
            throw new RuntimeException(ex);
        }
    }

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

    /**
     * Obtains an instance of the meta-bean specifying default values.
     * <p>
     * The properties will be determined using reflection to find the
     * {@link PropertyDefinition} annotation.
     * <p>
     * The default values must be provided if they cannot be determined automatically.
     * Default values for primitives are determined automatically, but empty lists and maps are not.
     * 
     * @param <B>  the type of the bean
     * @param beanType  the bean type, not null
     * @param lookup  the method handle lookup, not null
     * @param defaultValues  the default values, one for each property, not null
     * @return the meta-bean, not null
     * @deprecated Use version with field names, because no way to determine order of fields by reflection
     */
    @Deprecated
    public static <B extends Bean> LightMetaBean<B> of(
            Class<B> beanType,
            MethodHandles.Lookup lookup,
            Object... defaultValues) {

        // the field name order is undefined (not source code order)
        // this is fundamentally broken as they are being matched against default values (in source code order)
        return new LightMetaBean<>(beanType, lookup, fieldNames(beanType), defaultValues);
    }

    // determine the field names by reflection
    private static String[] fieldNames(Class<?> beanType) {
        Field[] fields = Stream.of(beanType.getDeclaredFields())
                .filter(f -> !Modifier.isStatic(f.getModifiers()) && f.getAnnotation(PropertyDefinition.class) != null)
                .toArray(Field[]::new);
        List<String> fieldNames = new ArrayList<>();
        for (int i = 0; i < fields.length; i++) {
            fieldNames.add(fields[i].getName());
        }
        return fieldNames.toArray(new String[fieldNames.size()]);
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

        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        if (lookup == null) {
            throw new NullPointerException("Lookup must not be null");
        }
        if (fieldNames == null) {
            throw new NullPointerException("Field names array must not be null");
        }
        if (defaultValues == null) {
            throw new NullPointerException("Default values array must not be null");
        }
        if (defaultValues.length > 0 && defaultValues.length != fieldNames.length) {
            throw new IllegalArgumentException("Number of default values must match number of fields");
        }
        this.beanType = beanType;
        // handle ordered or random
        Map<String, MetaProperty<?>> map = new LinkedHashMap<>();
        List<Class<?>> propertyTypes = new ArrayList<>();
        for (int i = 0; i < fieldNames.length; i++) {
            String fieldName = fieldNames[i];
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
            PropertyDefinition pdef = field.getAnnotation(PropertyDefinition.class);
            String name = field.getName();
            if (pdef.get().equals("field") || pdef.get().startsWith("optional") || pdef.get().equals("")) {
                map.put(name, LightMetaProperty.of(this, field, lookup, name, propertyTypes.size()));
            } else {
                String getterName = "get" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
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
                    String setterName = "set" + name.substring(0, 1).toUpperCase(Locale.ENGLISH) + name.substring(1);
                    setMethod = findSetMethod(beanType, setterName, field.getType());
                    if (setMethod == null) {
                        throw new IllegalArgumentException(
                                "Unable to find property setter: " + beanType.getSimpleName() + "." + setterName + "()");
                    }
                }
                map.put(name,
                        LightMetaProperty.of(this, field, getMethod, setMethod, lookup, name, propertyTypes.size()));
            }
            propertyTypes.add(field.getType());
        }
        Constructor<?> constructor = findConstructor(beanType, propertyTypes);
        if (defaultValues.length == 0) {
            defaultValues = buildConstructionData(constructor);
        }
        // derived
        Method[] methods = beanType.getDeclaredMethods();
        for (Method method : methods) {
            if (!Modifier.isStatic(method.getModifiers()) &&
                    method.getAnnotation(DerivedProperty.class) != null &&
                    method.getName().startsWith("get") &&
                    method.getName().length() > 3 &&
                    Character.isUpperCase(method.getName().charAt(3)) &&
                    method.getParameterTypes().length == 0) {
                String methodName = method.getName();
                String propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
                MetaProperty<Object> mp = LightMetaProperty.of(this, method, lookup, propertyName, -1);
                map.put(propertyName, mp);
            }
        }
        this.metaPropertyMap = Collections.unmodifiableMap(map);
        this.aliasMap = new HashMap<>();
        this.constructionData = defaultValues;
        MethodHandle handle = findConstructorHandle(beanType, lookup, constructor);
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
            Method[] methods = beanType.getMethods();
            List<Method> potential = new ArrayList<>();
            for (Method method : methods) {
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
            MethodType constructorType = MethodType.methodType(Void.TYPE, constructor.getParameterTypes());
            MethodHandle baseHandle = lookup.findConstructor(beanType, constructorType)
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
        Class<?>[] types = propertyTypes.toArray(new Class<?>[propertyTypes.size()]);
        try {
            Constructor<T> con = beanType.getDeclaredConstructor(types);
            return con;
            
        } catch (NoSuchMethodException ex) {
            // try a more lenient search
            // this handles cases where field is a concrete class and constructor is an interface
            @SuppressWarnings("unchecked")
            Constructor<T>[] cons = (Constructor<T>[]) beanType.getDeclaredConstructors();
            Constructor<T> match = null;
            for (int i = 0; i < cons.length; i++) {
                Constructor<T> con = cons[i];
                Class<?>[] conTypes = con.getParameterTypes();
                if (conTypes.length == types.length) {
                    for (int j = 0; j < types.length; j++) {
                        if (!conTypes[j].isAssignableFrom(types[j])) {
                            break;
                        }
                    }
                    if (match != null) {
                        throw new UnsupportedOperationException("Unable to find constructor: More than one matches");
                    }
                    match = con;
                }
            }
            if (match == null) {
                String msg = "Unable to find constructor: " + beanType.getSimpleName() + "(";
                for (Class<?> type : types) {
                    msg += Objects.toString(type.getName(), "<null>");
                }
                msg += ")";
                throw new UnsupportedOperationException(msg, ex);
            }
            return match;
        }
    }

    // array used to collect data when building
    // needs to have default values for primitives
    // note that this does not handle empty collections/maps
    private static Object[] buildConstructionData(Constructor<?> constructor) {
        Class<?>[] parameterTypes = constructor.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] == boolean.class) {
                args[i] = false;
            } else if (parameterTypes[i] == int.class) {
                args[i] = (int) 0;
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
        Map<String, String> aliasMap = new HashMap<>(this.aliasMap);
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
        MetaProperty<?> mp = metaPropertyMap().get(aliasMap.getOrDefault(propertyName, propertyName));
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
        if (obj instanceof LightMetaBean) {
            LightMetaBean<?> other = (LightMetaBean<?>) obj;
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
