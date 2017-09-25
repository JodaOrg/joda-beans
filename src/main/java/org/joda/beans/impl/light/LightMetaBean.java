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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

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

    /** The bean type. */
    private final Class<T> beanType;
    /** The meta-property instances of the bean. */
    private final Map<String, MetaProperty<?>> metaPropertyMap;
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

        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Bean cannot be created: " + beanName() + " from " + args, ex);
        } catch (IllegalAccessException ex) {
            throw new UnsupportedOperationException(
                    "Bean cannot be created: " + beanName() + " from " + args, ex);
        } catch (InstantiationException ex) {
            throw new UnsupportedOperationException(
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
     * Obtains an instance of the meta-bean.
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
     */
    public static <B extends Bean> LightMetaBean<B> of(
            Class<B> beanType,
            MethodHandles.Lookup lookup,
            Object... defaultValues) {

        return new LightMetaBean<>(beanType, lookup, defaultValues);
    }

    /**
     * Constructor.
     * 
     * @param beanType  the bean type, not null
     */
    private LightMetaBean(Class<T> beanType, MethodHandles.Lookup lookup, Object[] defaultValues) {
        if (beanType == null) {
            throw new NullPointerException("Bean class must not be null");
        }
        if (lookup == null) {
            throw new NullPointerException("Lookup must not be null");
        }
        if (defaultValues == null) {
            throw new NullPointerException("Default values must not be null");
        }
        this.beanType = beanType;
        Map<String, MetaProperty<?>> map = new LinkedHashMap<>();
        Field[] fields = beanType.getDeclaredFields();
        List<Class<?>> propertyTypes = new ArrayList<>();
        for (Field field : fields) {
            if (!Modifier.isStatic(field.getModifiers()) && field.getAnnotation(PropertyDefinition.class) != null) {
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
                    map.put(name, LightMetaProperty.of(this, field, getMethod, setMethod, lookup, name, propertyTypes.size()));
                }
                propertyTypes.add(field.getType());
            }
        }
        Constructor<?> constructor = findConstructor(beanType, propertyTypes);
        if (defaultValues.length == 0) {
            defaultValues = buildConstructionData(constructor);
        } else if (defaultValues.length != map.size()) {
            throw new IllegalArgumentException("Default values must match number of properties");
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
        this.constructionData = defaultValues;
        MethodHandle handle = findConstructorHandle(beanType, lookup, constructor);
        this.constructorFn = args -> build(handle, args);
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
