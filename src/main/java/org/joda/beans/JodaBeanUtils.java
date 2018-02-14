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

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.collect.grid.DenseGrid;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;
import org.joda.collect.grid.SparseGrid;
import org.joda.convert.StringConvert;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.LinkedHashMultiset;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.SortedMultiset;
import com.google.common.collect.Table;
import com.google.common.collect.TreeMultiset;

/**
 * A set of utilities to assist when working with beans and properties.
 * 
 * @author Stephen Colebourne
 */
public final class JodaBeanUtils {

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
     * Obtains a meta-bean from a {@code Class}.
     * <p>
     * This will return a meta-bean if it has been registered, or if the class
     * implements {@link DynamicBean} and has a no-args constructor.
     * Note that the common case where the meta-bean is registered by a static initializer is handled.
     * 
     * @param cls  the class to get the meta-bean for, not null
     * @return the meta-bean, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     * @deprecated Use {@link MetaBean#of(Class)}
     */
    @Deprecated
    public static MetaBean metaBean(Class<?> cls) {
        return MetaBean.of(cls);
    }

    /**
     * Registers a meta-bean.
     * <p>
     * This should be done for all beans in a static factory where possible.
     * If the meta-bean is dynamic, this method should not be called.
     * 
     * @param metaBean  the meta-bean, not null
     * @throws IllegalArgumentException if unable to register
     * @deprecated Use {@link MetaBean#register(MetaBean)}
     */
    @Deprecated
    public static void registerMetaBean(MetaBean metaBean) {
        MetaBean.register(metaBean);
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
        if (obj1.getClass().isArray()) {
            return equalsArray(obj1, obj2);
        }
        // this does not handle arrays embedded in objects, such as in lists/maps
        // but you shouldn't use arrays like that, should you?
        return obj1.equals(obj2);
    }

    // extracted from equal(Object,Object) to aid hotspot inlining
    private static boolean equalsArray(Object obj1, Object obj2) {
        if (obj1 instanceof Object[] && obj2 instanceof Object[] && obj1.getClass() == obj2.getClass()) {
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
        // reachable if obj1 is an array and obj2 is not
        return false;
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
     * Checks if two floats are equal within the specified tolerance.
     * <p>
     * Two NaN values are equal. Positive and negative infinity are only equal with themselves.
     * Otherwise, the difference between the values is compared to the tolerance.
     * 
     * @param val1  the first value, may be null
     * @param val2  the second value, may be null
     * @param tolerance  the tolerance used to compare equal
     * @return true if equal
     */
    public static boolean equalWithTolerance(float val1, float val2, double tolerance) {
        return (Float.floatToIntBits(val1) == Float.floatToIntBits(val2)) || Math.abs(val1 - val2) <= tolerance;
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
     * Checks if two doubles are equal within the specified tolerance.
     * <p>
     * Two NaN values are equal. Positive and negative infinity are only equal with themselves.
     * Otherwise, the difference between the values is compared to the tolerance.
     * The tolerance is expected to be a finite value, not NaN or infinity.
     * 
     * @param val1  the first value, may be null
     * @param val2  the second value, may be null
     * @param tolerance  the tolerance used to compare equal
     * @return true if equal
     */
    public static boolean equalWithTolerance(double val1, double val2, double tolerance) {
        return (Double.doubleToLongBits(val1) == Double.doubleToLongBits(val2)) || Math.abs(val1 - val2) <= tolerance;
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
            return hashCodeArray(obj);
        }
        return obj.hashCode();
    }

    // extracted from hashCode(Object) to aid hotspot inlining
    private static int hashCodeArray(Object obj) {
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
        // unreachable?
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
     * Returns the {@code toString} value handling arrays.
     * 
     * @param obj  the object, may be null
     * @return the string, not null
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj.getClass().isArray()) {
            return toStringArray(obj);
        }
        return obj.toString();
    }

    // extracted from toString(Object) to aid hotspot inlining
    private static String toStringArray(Object obj) {
        if (obj instanceof Object[]) {
            return Arrays.deepToString((Object[]) obj);
        } else if (obj instanceof int[]) {
            return Arrays.toString((int[]) obj);
        } else if (obj instanceof long[]) {
            return Arrays.toString((long[]) obj);
        } else if (obj instanceof byte[]) {
            return Arrays.toString((byte[]) obj);
        } else if (obj instanceof double[]) {
            return Arrays.toString((double[]) obj);
        } else if (obj instanceof float[]) {
            return Arrays.toString((float[]) obj);
        } else if (obj instanceof char[]) {
            return Arrays.toString((char[]) obj);
        } else if (obj instanceof short[]) {
            return Arrays.toString((short[]) obj);
        } else if (obj instanceof boolean[]) {
            return Arrays.toString((boolean[]) obj);
        }
        // unreachable?
        return obj.toString();
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
        StringBuilder buf;
        if (prefix != null) {
            buf = new StringBuilder((names.size()) * 32 + prefix.length()).append(prefix);
        } else {
            buf = new StringBuilder((names.size()) * 32);
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

    /**
     * Flattens a bean to a {@code Map}.
     * <p>
     * The returned map will contain all the properties from the bean with their actual values.
     * 
     * @param bean  the bean to generate a string for, not null
     * @return the bean as a map, not null
     */
    public static Map<String, Object> flatten(Bean bean) {
        Map<String, MetaProperty<?>> propertyMap = bean.metaBean().metaPropertyMap();
        Map<String, Object> map = new LinkedHashMap<>(propertyMap.size());
        for (Entry<String, MetaProperty<?>> entry : propertyMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(bean));
        }
        return Collections.unmodifiableMap(map);
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
    public static <T extends Bean> T clone(T original) {
        if (original == null || original instanceof ImmutableBean) {
            return original;
        }
        return cloneAlways(original);
    }

    /**
     * Clones a bean always.
     * <p>
     * This performs a deep clone. There is no protection against cycles in
     * the object graph beyond {@code StackOverflowError}.
     * This differs from {@link #clone()} in that immutable beans are also cloned.
     * 
     * @param <T>  the type of the bean
     * @param original  the original bean to clone, not null
     * @return the cloned bean, not null
     */
    public static <T extends Bean> T cloneAlways(T original) {
        @SuppressWarnings("unchecked")
        BeanBuilder<T> builder = (BeanBuilder<T>) original.metaBean().builder();
        for (MetaProperty<?> mp : original.metaBean().metaPropertyIterable()) {
            if (mp.style().isBuildable()) {
                Object value = mp.get(original);
                builder.set(mp.name(), Cloner.INSTANCE.clone(value));
            }
        }
        return builder.build();
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
            throw new IllegalArgumentException(notNullMsg(propertyName));
        }
    }

    // extracted from notNull(Object,String) to aid hotspot inlining
    private static String notNullMsg(String propertyName) {
        return "Argument '" + propertyName + "' must not be null";
    }

    /**
     * Checks if the value is not blank, throwing an exception if it is.
     * <p>
     * Validate that the specified argument is not null and has at least one non-whitespace character.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null or empty
     */
    public static void notBlank(String value, String propertyName) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(notBlank(propertyName));
        }
    }

    @SuppressWarnings("null")
    private static boolean isBlank(String str) {
        int strLen = (str != null ? str.length() : 0);
        if (strLen != 0) {
            for (int i = 0; i < strLen; i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    return false;
                }
            }
        }
        return true;
    }

    private static String notBlank(String propertyName) {
        return "Argument '" + propertyName + "' must not be empty";
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
            throw new IllegalArgumentException(notEmpty(propertyName));
        }
    }

    // extracted from notEmpty(?,String) to aid hotspot inlining
    private static String notEmpty(String propertyName) {
        return "Argument '" + propertyName + "' must not be empty";
    }

    /**
     * Checks if the collection value is not empty, throwing an exception if it is.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null or empty
     */
    public static void notEmpty(Collection<?> value, String propertyName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(notEmpty(propertyName));
        }
    }

    /**
     * Checks if the map value is not empty, throwing an exception if it is.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null or empty
     */
    public static void notEmpty(Map<?, ?> value, String propertyName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(notEmpty(propertyName));
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Extracts the collection content type as a {@code Class} from a property.
     * <p>
     * This method allows the resolution of generics in certain cases.
     * 
     * @param prop  the property to examine, not null
     * @return the collection content type, null if unable to determine or type has no generic parameters
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
     * @return the collection content type, null if unable to determine or type has no generic parameters
     */
    public static Class<?> collectionType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractTypeClass(prop, targetClass, 1, 0);
    }

    /**
     * Extracts the map value type generic type parameters as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * <p>
     * This is used when the collection generic parameter is a map or collection.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the collection content type generic parameters, empty if unable to determine, no nulls
     */
    public static List<Class<?>> collectionTypeTypes(MetaProperty<?> prop, Class<?> targetClass) {
        Type type = extractType(targetClass, prop, 1, 0);
        return extractTypeClasses(targetClass, type);
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map key type, null if unable to determine or type has no generic parameters
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
     * @return the map key type, null if unable to determine or type has no generic parameters
     */
    public static Class<?> mapKeyType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractTypeClass(prop, targetClass, 2, 0);
    }

    /**
     * Extracts the map value type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map value type, null if unable to determine or type has no generic parameters
     */
    public static Class<?> mapValueType(Property<?> prop) {
        return mapValueType(prop.metaProperty(), prop.bean().getClass());
    }

    /**
     * Extracts the map value type as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the map value type, null if unable to determine or type has no generic parameters
     */
    public static Class<?> mapValueType(MetaProperty<?> prop, Class<?> targetClass) {
        return extractTypeClass(prop, targetClass, 2, 1);
    }

    /**
     * Extracts the map value type generic type parameters as a {@code Class} from a meta-property.
     * <p>
     * The target type is the type of the object, not the declaring type of the meta-property.
     * <p>
     * This is used when the map value generic parameter is a map or collection.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @return the map value type generic parameters, empty if unable to determine, no nulls
     */
    public static List<Class<?>> mapValueTypeTypes(MetaProperty<?> prop, Class<?> targetClass) {
        Type type = extractType(targetClass, prop, 2, 1);
        return extractTypeClasses(targetClass, type);
    }

    /**
     * Low-level method to extract generic type information.
     * 
     * @param prop  the property to examine, not null
     * @param targetClass  the target type to evaluate against, not null
     * @param size  the number of generic parameters expected
     * @param index  the index of the generic parameter
     * @return the type, null if unable to determine or type has no generic parameters
     */
    public static Class<?> extractTypeClass(MetaProperty<?> prop, Class<?> targetClass, int size, int index) {
        return eraseToClass(extractType(targetClass, prop, size, index));
    }

    private static Type extractType(Class<?> targetClass, MetaProperty<?> prop, int size, int index) {
        Type genType = prop.propertyGenericType();
        if (genType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length == size) {
                Type type = types[index];
                if (type instanceof WildcardType) {
                    WildcardType wtype = (WildcardType) type;
                    if (wtype.getLowerBounds().length == 0 && wtype.getUpperBounds().length > 0) {
                        type = wtype.getUpperBounds()[0];
                    }
                }
                if (type instanceof TypeVariable) {
                    type = resolveGenerics(targetClass, (TypeVariable<?>) type);
                }
                return type;
            }
        }
        return null;
    }

    private static List<Class<?>> extractTypeClasses(Class<?> targetClass, Type type) {
        List<Class<?>> result = new ArrayList<>();
        if (type != null) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) type;
                Type[] actualTypes = pt.getActualTypeArguments();
                for (Type actualType : actualTypes) {
                    if (actualType instanceof TypeVariable) {
                        actualType = resolveGenerics(targetClass, (TypeVariable<?>) actualType);
                    }
                    Class<?> cls = eraseToClass(actualType);
                    result.add(cls != null ? cls : Object.class);
                }
            }
        }
        return result;
    }

    private static Type resolveGenerics(Class<?> targetClass, TypeVariable<?> typevar) {
        // looks up meaning of type variables like T
        Map<Type, Type> resolved = new HashMap<>();
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
                if (rawType == null) {
                    return null;
                }
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
     * Checks if two beans are equal ignoring one or more properties.
     * <p>
     * This version of {@code equalIgnoring} only checks properties at the top level.
     * For example, if a {@code Person} bean contains an {@code Address} bean then
     * only properties on the {@code Person} bean will be checked against the ignore list.
     * 
     * @param bean1  the first bean, not null
     * @param bean2  the second bean, not null
     * @param properties  the properties to ignore, not null
     * @return true if equal
     * @throws IllegalArgumentException if inputs are null
     */
    public static boolean equalIgnoring(Bean bean1, Bean bean2, MetaProperty<?>... properties) {
        JodaBeanUtils.notNull(bean1, "bean1");
        JodaBeanUtils.notNull(bean2, "bean2");
        JodaBeanUtils.notNull(properties, "properties");
        if (bean1 == bean2) {
            return true;
        }
        if (bean1.getClass() != bean2.getClass()) {
            return false;
        }
        switch (properties.length) {
            case 0:
                return bean1.equals(bean2);
            case 1: {
                MetaProperty<?> ignored = properties[0];
                for (MetaProperty<?> mp : bean1.metaBean().metaPropertyIterable()) {
                    if (ignored.equals(mp) == false && JodaBeanUtils.equal(mp.get(bean1), mp.get(bean2)) == false) {
                        return false;
                    }
                }
                return true;
            }
            default:
                Set<MetaProperty<?>> ignored = new HashSet<>(Arrays.asList(properties));
                for (MetaProperty<?> mp : bean1.metaBean().metaPropertyIterable()) {
                    if (ignored.contains(mp) == false
                            && JodaBeanUtils.equal(mp.get(bean1), mp.get(bean2)) == false) {
                        return false;
                    }
                }
                return true;
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns an iterator over all the beans contained within the bean.
     * <p>
     * The iterator is a depth-first traversal of the beans within the specified bean.
     * The first returned bean is the specified bean.
     * Beans within collections will be returned.
     * <p>
     * A cycle in the bean structure will cause an infinite loop.
     * 
     * @param bean  the bean to iterate over, not null
     * @return the iterator, not null
     */
    public static Iterator<Bean> beanIterator(Bean bean) {
        return new BeanIterator(bean);
    }

    //-------------------------------------------------------------------------
    /**
     * Chains two meta-properties together.
     * <p>
     * The resulting function takes an instance of a bean, queries using the first
     * meta-property, then queries the result using the second meta-property.
     * If the first returns null, the result will be null.
     * 
     * @param <P>  the type of the result of the chain
     * @param mp1  the first meta-property, not null
     * @param mp2  the second meta-property, not null
     * @return the chain function, not null
     */
    public static <P> Function<Bean, P> chain(MetaProperty<? extends Bean> mp1, MetaProperty<P> mp2) {
        notNull(mp1, "MetaProperty 1");
        notNull(mp1, "MetaProperty 2");
        return b -> {
            Bean first = mp1.get(b);
            return first != null ? mp2.get(first) : null;
        };
    }

    /**
     * Chains a function to a meta-property.
     * <p>
     * The resulting function takes an instance of a bean, queries using the first
     * function, then queries the result using the second meta-property.
     * If the first returns null, the result will be null.
     * 
     * @param <P>  the type of the result of the chain
     * @param fn1  the first meta-property, not null
     * @param mp2  the second meta-property, not null
     * @return the chain function, not null
     */
    public static <P> Function<Bean, P> chain(Function<Bean, ? extends Bean> fn1, MetaProperty<P> mp2) {
        notNull(fn1, "Function 1");
        notNull(fn1, "MetaProperty 2");
        return b -> {
            Bean first = fn1.apply(b);
            return first != null ? mp2.get(first) : null;
        };
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains a comparator for the specified bean query.
     * <p>
     * The result of the query must be {@link Comparable}.
     * <p>
     * To use this with a meta-property append {@code ::get} to the meta-property,
     * for example {@code Address.meta().street()::get}.
     * 
     * @param query  the query to use, not null
     * @param ascending  true for ascending, false for descending
     * @return the comparator, not null
     */
    public static Comparator<Bean> comparator(Function<Bean, ?> query, boolean ascending) {
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
    public static Comparator<Bean> comparatorAscending(Function<Bean, ?> query) {
        if (query == null) {
            throw new NullPointerException("Function must not be null");
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
    public static Comparator<Bean> comparatorDescending(Function<Bean, ?> query) {
        if (query == null) {
            throw new NullPointerException("Function must not be null");
        }
        return Collections.reverseOrder(new Comp(query));
    }

    //-------------------------------------------------------------------------
    /**
     * Comparator.
     */
    private static final class Comp implements Comparator<Bean> {
        private final Function<Bean, ?> query;

        private Comp(Function<Bean, ?> query) {
            this.query = query;
        }

        @Override
        public int compare(Bean bean1, Bean bean2) {
            @SuppressWarnings("unchecked")
            Comparable<Object> value1 = (Comparable<Object>) query.apply(bean1);
            Object value2 = query.apply(bean2);
            return value1.compareTo(value2);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Clones an object.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static class Cloner {
        public static final Cloner INSTANCE = getInstance();
        private static Cloner getInstance() {
            try {
                Class.forName("org.joda.collect.grid.Grid");
                return new CollectCloner();
            } catch (Exception | LinkageError ex) {
                try {
                    Class.forName("com.google.common.collect.Multimap");
                    return new GuavaCloner();
                } catch (Exception | LinkageError ex2) {
                    return new Cloner();
                }
            }
        }

        Cloner() {
        }

        Object clone(Object value) {
            if (value == null) {
                return value;
            } else if (value instanceof Bean) {
                return cloneAlways((Bean) value);
            } else if (value instanceof SortedSet) {
                SortedSet set = (SortedSet) value;
                return cloneIterable(set, new TreeSet(set.comparator()));
            } else if (value instanceof Set) {
                return cloneIterable((Set) value, new LinkedHashSet());
            } else if (value instanceof Iterable) {
                return cloneIterable((Iterable) value, new ArrayList());
            } else if (value instanceof SortedMap) {
                SortedMap map = (SortedMap) value;
                return cloneMap(map, new TreeMap(map.comparator()));
            } else if (value instanceof Map) {
                return cloneMap((Map) value, new LinkedHashMap());
            } else if (value.getClass().isArray()) {
                return cloneArray(value);
            } else if (value instanceof java.util.Date) {
                return ((java.util.Date) value).clone();
            }
            return value;
        }

        Object cloneIterable(Iterable original, Collection cloned) {
            for (Object item : original) {
                cloned.add(clone(item));
            }
            return cloned;
        }

        Object cloneMap(Map original, Map cloned) {
            for (Object item : original.entrySet()) {
                Entry entry = (Entry) item;
                cloned.put(clone(entry.getKey()), clone(entry.getValue()));
            }
            return cloned;
        }

        Object cloneArray(Object original) {
            int len = Array.getLength(original);
            Class<?> arrayType = original.getClass().getComponentType();
            Object copy = Array.newInstance(arrayType, len);
            for (int i = 0; i < len; i++) {
                Array.set(copy, i, clone(Array.get(original, i)));
            }
            return copy;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Clones an object.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static class GuavaCloner extends Cloner {
        GuavaCloner() {
        }

        @Override
        Object clone(Object value) {
            if (value == null) {
                return value;
            } else if (value instanceof ImmutableMap ||
                    value instanceof ImmutableCollection ||
                    value instanceof ImmutableMap ||
                    value instanceof ImmutableMultimap ||
                    value instanceof ImmutableTable) {
                return value;
            } else if (value instanceof Multiset) {
                return cloneAlways((Bean) value);
            } else if (value instanceof SortedMultiset) {
                SortedMultiset set = (SortedMultiset) value;
                return cloneIterable(set, TreeMultiset.create(set.comparator()));
            } else if (value instanceof Multiset) {
                return cloneIterable((Multiset) value, LinkedHashMultiset.create());
            } else if (value instanceof SetMultimap) {
                return cloneMultimap((Multimap) value, LinkedHashMultimap.create());
            } else if (value instanceof ListMultimap) {
                return cloneMultimap((Multimap) value, ArrayListMultimap.create());
            } else if (value instanceof Multimap) {
                return cloneMultimap((Multimap) value, ArrayListMultimap.create());
            } else if (value instanceof BiMap) {
                return cloneMap((BiMap) value, HashBiMap.create());
            } else if (value instanceof Table) {
                return cloneTable((Table) value, HashBasedTable.create());
            }
            return super.clone(value);
        }

        Object cloneMultimap(Multimap original, Multimap cloned) {
            for (Object key : original.keySet()) {
                Collection values = original.get(key);
                for (Object value : values) {
                    cloned.put(clone(key), clone(value));
                }
            }
            return cloned;
        }

        Object cloneTable(Table original, Table cloned) {
            for (Object item : original.cellSet()) {
                Table.Cell cell = (Table.Cell) item;
                cloned.put(clone(cell.getRowKey()), clone(cell.getColumnKey()), clone(cell.getValue()));
            }
            return cloned;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Clones an object.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static class CollectCloner extends GuavaCloner {
        CollectCloner() {
        }

        @Override
        Object clone(Object value) {
            if (value == null) {
                return value;
            } else if (value instanceof ImmutableGrid) {
                return value;
            } else if (value instanceof DenseGrid) {
                Grid grid = (Grid) value;
                return cloneGrid(grid, DenseGrid.create(grid.rowCount(), grid.columnCount()));
            } else if (value instanceof Grid) {
                Grid grid = (Grid) value;
                return cloneGrid(grid, SparseGrid.create(grid.rowCount(), grid.columnCount()));
            }
            return super.clone(value);
        }

        Object cloneGrid(Grid original, Grid cloned) {
            for (Object item : original.cells()) {
                Grid.Cell cell = (Grid.Cell) item;
                cloned.put(cell.getRow(), cell.getColumn(), clone(cell.getValue()));
            }
            return cloned;
        }
    }

}
