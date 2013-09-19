/*
 *  Copyright 2001-2013 Stephen Colebourne
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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.impl.map.MapBean;
import org.joda.convert.StringConvert;

/**
 * A set of utilities to assist when working with beans and properties.
 * 
 * @author Stephen Colebourne
 */
public final class JodaBeanUtils {

    /**
     * The cache of meta-beans.
     */
    private static final ConcurrentHashMap<Class<?>, MetaBean> metaBeans = new ConcurrentHashMap<Class<?>, MetaBean>();
    /**
     * The cache of meta-beans.
     */
    private static final StringConvert converter = new StringConvert();

    /**
     * Restricted constructor.
     */
    private JodaBeanUtils() {
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the meta-bean given a class.
     * <p>
     * This only works for those beans that have registered their meta-beans.
     * See {@link #registerMetaBean(MetaBean)}.
     * 
     * @param cls  the class to get the meta-bean for, not null
     * @return the meta-bean, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static MetaBean metaBean(Class<?> cls) {
        if (cls == FlexiBean.class) {
            return new FlexiBean().metaBean();
        } else if (cls == MapBean.class) {
            return new MapBean().metaBean();
        }
        MetaBean meta = metaBeans.get(cls);
        if (meta == null) {
            if (DynamicBean.class.isAssignableFrom(cls)) {
                try {
                    return cls.asSubclass(DynamicBean.class).newInstance().metaBean();
                } catch (InstantiationException ex) {
                    throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName(), ex);
                } catch (IllegalAccessException ex) {
                    throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName(), ex);
                }
            }
            throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName());
        }
        return meta;
    }

    /**
     * Registers a meta-bean.
     * <p>
     * This should be done for all beans in a static factory where possible.
     * If the meta-bean is dynamic, this method should not be called.
     * 
     * @param metaBean  the meta-bean, not null
     * @throws IllegalArgumentException if unable to register
     */
    public static void registerMetaBean(MetaBean metaBean) {
        Class<? extends Bean> type = metaBean.beanType();
        if (metaBeans.putIfAbsent(type, metaBean) != null) {
            throw new IllegalArgumentException("Cannot register class twice: " + type.getName());
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the standard string format converter.
     * <p>
     * This returns a singleton that may be mutated (holds a concurrent map).
     * New conversions should be registered at program startup.
     * 
     * @return the standard string converter, not null
     */
    public static StringConvert stringConverter() {
        return converter;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if two objects are equal handling null.
     * 
     * @param obj1  the first object, may be null
     * @param obj2  the second object, may be null
     * @return true if equal
     */
    public static boolean equal(Object obj1, Object obj2) {
        if (obj1 == obj2) {
            return true;
        }
        if (obj1 == null || obj2 == null) {
            return false;
        }
        if (obj1.getClass().isArray() && obj1.getClass() == obj2.getClass()) {
            if (obj1 instanceof Object[] && obj2 instanceof Object[]) {
                return Arrays.deepEquals((Object[]) obj1, (Object[]) obj2);
            } else if (obj1 instanceof int[] && obj2 instanceof int[]) {
                return Arrays.equals((int[]) obj1, (int[]) obj2);
            } else if (obj1 instanceof long[] && obj2 instanceof long[]) {
                return Arrays.equals((long[]) obj1, (long[]) obj2);
            } else if (obj1 instanceof byte[] && obj2 instanceof byte[]) {
                return Arrays.equals((byte[]) obj1, (byte[]) obj2);
            } else if (obj1 instanceof double[] && obj2 instanceof double[]) {
                return Arrays.equals((double[]) obj1, (double[]) obj2);
            } else if (obj1 instanceof float[] && obj2 instanceof float[]) {
                return Arrays.equals((float[]) obj1, (float[]) obj2);
            } else if (obj1 instanceof char[] && obj2 instanceof char[]) {
                return Arrays.equals((char[]) obj1, (char[]) obj2);
            } else if (obj1 instanceof short[] && obj2 instanceof short[]) {
                return Arrays.equals((short[]) obj1, (short[]) obj2);
            } else if (obj1 instanceof boolean[] && obj2 instanceof boolean[]) {
                return Arrays.equals((boolean[]) obj1, (boolean[]) obj2);
            }
        }
        return obj1.equals(obj2);
    }

    /**
     * Checks if two floats are equal based on identity.
     * <p>
     * This performs the same check as {@link Float#equals(Object)}.
     * 
     * @param val1  the first value, may be null
     * @param val2  the second value, may be null
     * @return true if equal
     */
    public static boolean equal(float val1, float val2) {
        return Float.floatToIntBits(val1) == Float.floatToIntBits(val2);
    }

    /**
     * Checks if two doubles are equal based on identity.
     * <p>
     * This performs the same check as {@link Double#equals(Object)}.
     * 
     * @param val1  the first value, may be null
     * @param val2  the second value, may be null
     * @return true if equal
     */
    public static boolean equal(double val1, double val2) {
        return Double.doubleToLongBits(val1) == Double.doubleToLongBits(val2);
    }

    /**
     * Returns a hash code for an object handling null.
     * 
     * @param obj  the object, may be null
     * @return the hash code
     */
    public static int hashCode(Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[]) {
                return Arrays.deepHashCode((Object[]) obj);
            } else if (obj instanceof int[]) {
                return Arrays.hashCode((int[]) obj);
            } else if (obj instanceof long[]) {
                return Arrays.hashCode((long[]) obj);
            } else if (obj instanceof byte[]) {
                return Arrays.hashCode((byte[]) obj);
            } else if (obj instanceof double[]) {
                return Arrays.hashCode((double[]) obj);
            } else if (obj instanceof float[]) {
                return Arrays.hashCode((float[]) obj);
            } else if (obj instanceof char[]) {
                return Arrays.hashCode((char[]) obj);
            } else if (obj instanceof short[]) {
                return Arrays.hashCode((short[]) obj);
            } else if (obj instanceof boolean[]) {
                return Arrays.hashCode((boolean[]) obj);
            }
        }
        return obj.hashCode();
    }

    /**
     * Returns a hash code for a {@code boolean}.
     * 
     * @param value  the value to convert to a hash code
     * @return the hash code
     */
    public static int hashCode(boolean value) {
        return value ? 1231 : 1237;
    }

    /**
     * Returns a hash code for an {@code int}.
     * 
     * @param value  the value to convert to a hash code
     * @return the hash code
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * Returns a hash code for a {@code long}.
     * 
     * @param value  the value to convert to a hash code
     * @return the hash code
     */
    public static int hashCode(long value) {
        return (int) (value ^ value >>> 32);
    }

    /**
     * Returns a hash code for a {@code float}.
     * 
     * @param value  the value to convert to a hash code
     * @return the hash code
     */
    public static int hashCode(float value) {
        return Float.floatToIntBits(value);
    }

    /**
     * Returns a hash code for a {@code double}.
     * 
     * @param value  the value to convert to a hash code
     * @return the hash code
     */
    public static int hashCode(double value) {
        return hashCode(Double.doubleToLongBits(value));
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the two beans have the same set of properties.
     * <p>
     * This comparison checks that both beans have the same set of property names
     * and that the value of each property name is also equal.
     * It does not check the bean type, thus a {@link FlexiBean} may be equal
     * to a {@link DirectBean}.
     * <p>
     * This comparison is usable with the {@link #propertiesHashCode} method.
     * The result is the same as that if each bean was converted to a {@code Map}
     * from name to value.
     * 
     * @param bean1  the first bean to compare, not null
     * @param bean2  the second bean to compare, not null
     * @return true if equal
     */
    public static boolean propertiesEqual(Bean bean1, Bean bean2) {
        Set<String> names = bean1.propertyNames();
        if (names.equals(bean2.propertyNames()) == false) {
            return false;
        }
        for (String name : names) {
            Object value1 = bean1.property(name).get();
            Object value2 = bean2.property(name).get();
            if (equal(value1, value2) == false) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a hash code based on the set of properties on a bean.
     * <p>
     * This hash code is usable with the {@link #propertiesEqual} method.
     * The result is the same as that if each bean was converted to a {@code Map}
     * from name to value.
     * 
     * @param bean  the bean to generate a hash code for, not null
     * @return the hash code
     */
    public static int propertiesHashCode(Bean bean) {
        int hash = 7;
        Set<String> names = bean.propertyNames();
        for (String name : names) {
            Object value = bean.property(name).get();
            hash += hashCode(value);
        }
        return hash;
    }

    /**
     * Returns a string describing the set of properties on a bean.
     * <p>
     * The result is the same as that if the bean was converted to a {@code Map}
     * from name to value.
     * 
     * @param bean  the bean to generate a string for, not null
     * @param prefix  the prefix to use, null ignored
     * @return the string form of the bean, not null
     */
    public static String propertiesToString(Bean bean, String prefix) {
        Set<String> names = bean.propertyNames();
        StringBuilder buf = new StringBuilder((names.size()) * 32 + prefix.length());
        if (prefix != null) {
            buf.append(prefix);
        }
        buf.append('{');
        if (names.size() > 0) {
            for (String name : names) {
                Object value = bean.property(name).get();
                buf.append(name).append('=').append(value).append(',').append(' ');
            }
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Clones a bean.
     * <p>
     * This performs a deep clone. There is no protection against cycles in
     * the object graph beyond {@code StackOverflowError}.
     * 
     * @param <T>  the type of the bean
     * @param original  the original bean to clone, null returns null
     * @return the cloned bean, null if null input
     */
    @SuppressWarnings("unchecked")
    public static <T extends Bean> T clone(T original) {
        if (original == null || original instanceof ImmutableBean) {
            return original;
        }
        BeanBuilder<? extends Bean> builder = original.metaBean().builder();
        for (MetaProperty<?> mp : original.metaBean().metaPropertyIterable()) {
            if (mp.style().isBuildable()) {
                Object value = mp.get(original);
                if (value instanceof Bean) {
                    value = clone((Bean) value);
                }
                builder.set(mp.name(), value);
            }
        }
        return (T) builder.build();
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the value is not null, throwing an exception if it is.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null
     */
    public static void notNull(Object value, String propertyName) {
        if (value == null) {
            throw new IllegalArgumentException("Argument '" + propertyName + "' must not be null");
        }
    }

    /**
     * Checks if the value is not empty, throwing an exception if it is.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null or empty
     */
    public static void notEmpty(String value, String propertyName) {
        if (value == null || value.length() == 0) {
            throw new IllegalArgumentException("Argument '" + propertyName + "' must not be empty");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts the collection content type as a {@code Class} from a property.
     * <p>
     * This method allows the resolution of generics in certain cases.
     * 
     * @param prop  the property to examine, not null
     * @return the collection content type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a collection
     */
    public static Class<?> collectionType(Property<?> prop) {
        return collectionType(prop.metaProperty(), prop.bean().getClass());
    }

    /**
     * Extracts the collection content type as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the collection content type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a collection
     */
    public static Class<?> collectionType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractType(targetClass, prop, 1, 0);
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a map
     */
    public static Class<?> mapKeyType(Property<?> prop) {
        return mapKeyType(prop.metaProperty(), prop.bean().getClass());
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a map
     */
    public static Class<?> mapKeyType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractType(targetClass, prop, 2, 0);
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a map
     */
    public static Class<?> mapValueType(Property<?> prop) {
        return mapValueType(prop.metaProperty(), prop.bean().getClass());
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a map
     */
    public static Class<?> mapValueType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractType(targetClass, prop, 2, 1);
    }

    private static Class<?> extractType(Class<?> targetClass, MetaProperty<?> prop, int size, int index) {
        Type genType = prop.propertyGenericType();
        if (genType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length == size) {
                Type type = types[index];
                if (type instanceof TypeVariable) {
                    type = resolveGenerics(targetClass, (TypeVariable<?>) type);
                }
                return eraseToClass(type);
            }
        }
        return null;
    }

    private static Type resolveGenerics(Class<?> targetClass, TypeVariable<?> typevar) {
        // looks up meaning of type variables like T
        Map<Type, Type> resolved = new HashMap<Type, Type>();
        Type type = targetClass;
        while (type != null) {
            if (type instanceof Class) {
                type = ((Class<?>) type).getGenericSuperclass();
            } else if (type instanceof ParameterizedType) {
                // find actual types captured by subclass
                ParameterizedType pt = (ParameterizedType) type;
                Type[] actualTypeArguments = pt.getActualTypeArguments();
                // find type variables declared in source code
                Class<?> rawType = eraseToClass(pt.getRawType());
                TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
                for (int i = 0; i < actualTypeArguments.length; i++) {
                    resolved.put(typeParameters[i], actualTypeArguments[i]);
                }
                type = rawType.getGenericSuperclass();
            }
        }
        // resolve type variable to a meaningful type
        Type result = typevar;
        while (resolved.containsKey(result)) {
            result = resolved.get(result);
        }
        return result;
    }

    private static Class<?> eraseToClass(Type type) {
        if (type instanceof Class) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            return eraseToClass(((ParameterizedType) type).getRawType());
        } else if (type instanceof GenericArrayType) {
            Type componentType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> componentClass = eraseToClass(componentType);
            if (componentClass != null) {
                return Array.newInstance(componentClass, 0).getClass();
            }
        } else if (type instanceof TypeVariable) {
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            if (bounds.length == 0) {
                return Object.class;
            } else {
                return eraseToClass(bounds[0]);
            }
        }
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains a comparator for the specified bean query.
     * <p>
     * The result of the query must be {@link Comparable}.
     * 
     * @param query  the query to use, not null
     * @param ascending  true for ascending, false for descending
     * @return the comparator, not null
     */
    public static Comparator<Bean> comparator(BeanQuery<?> query, boolean ascending) {
        return (ascending ? comparatorAscending(query) : comparatorDescending(query));
    }

    /**
     * Obtains an ascending comparator for the specified bean query.
     * <p>
     * The result of the query must be {@link Comparable}.
     * 
     * @param query  the query to use, not null
     * @return the comparator, not null
     */
    public static Comparator<Bean> comparatorAscending(BeanQuery<?> query) {
        if (query == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        return new Comp(query);
    }

    /**
     * Obtains an descending comparator for the specified bean query.
     * <p>
     * The result of the query must be {@link Comparable}.
     * 
     * @param query  the query to use, not null
     * @return the comparator, not null
     */
    public static Comparator<Bean> comparatorDescending(BeanQuery<?> query) {
        if (query == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        return Collections.reverseOrder(new Comp(query));
    }

    //-------------------------------------------------------------------------
    /**
     * Compare for BeanQuery.
     */
    private static final class Comp implements Comparator<Bean> {
        private final BeanQuery<?> query;

        private Comp(BeanQuery<?> query) {
            this.query = query;
        }

        @Override
        public int compare(Bean bean1, Bean bean2) {
            @SuppressWarnings("unchecked")
            Comparable<Object> value1 = (Comparable<Object>) query.get(bean1);
            Object value2 = query.get(bean2);
            return value1.compareTo(value2);
        }
    }

}
