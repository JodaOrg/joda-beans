/*
 *  Copyright 2001-2011 Stephen Colebourne
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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.flexi.FlexiBean;

/**
 * A set of utilities to assist when working with beans and properties.
 * 
 * @author Stephen Colebourne
 */
public final class JodaBeanUtils {

    /**
     * Restricted constructor.
     */
    private JodaBeanUtils() {
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
        return obj1 == obj2 || (obj1 != null && obj1.equals(obj2));
    }

    /**
     * Returns a hash code for an object handling null.
     * 
     * @param obj  the object, may be null
     * @return the hash code
     */
    public static int hashCode(Object obj) {
        return obj == null ? 0 : obj.hashCode();
    }

    /**
     * Returns a hash code for a {@code long}.
     * 
     * @param obj  the object, may be null
     * @return the hash code
     */
    public static int hashCode(long value) {
        return (int) (value ^ value >>> 32);
    }

    /**
     * Returns a hash code for a {@code float}.
     * 
     * @param obj  the object, may be null
     * @return the hash code
     */
    public static int hashCode(float value) {
        return Float.floatToIntBits(value);
    }

    /**
     * Returns a hash code for a {@code double}.
     * 
     * @param obj  the object, may be null
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
     * Checks if the value is not null, throwing an exception if it is.
     * 
     * @param value  the value to check, may be null
     * @param propertyName  the property name, should not be null
     * @throws IllegalArgumentException if the value is null
     */
    public static void notNull(String value, String propertyName) {
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
     * Extracts the list content type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the list content type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a list
     */
    public static Class<?> listType(MetaProperty<?> prop) {
        if (List.class.isAssignableFrom(prop.propertyType()) == false) {
            throw new IllegalArgumentException("Property is not a List");
        }
        return extractType(prop, 1, 0);
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a list
     */
    public static Class<?> mapKeyType(MetaProperty<?> prop) {
        if (Map.class.isAssignableFrom(prop.propertyType()) == false) {
            throw new IllegalArgumentException("Property is not a Map");
        }
        return extractType(prop, 2, 0);
    }

    /**
     * Extracts the map key type as a {@code Class} from a meta-property.
     * 
     * @param prop  the property to examine, not null
     * @return the map key type, null if unable to determine
     * @throws IllegalArgumentException if the property is not a list
     */
    public static Class<?> mapValueType(MetaProperty<?> prop) {
        if (Map.class.isAssignableFrom(prop.propertyType()) == false) {
            throw new IllegalArgumentException("Property is not a Map");
        }
        return extractType(prop, 2, 1);
    }

    private static Class<?> extractType(MetaProperty<?> prop, int size, int index) {
        Type genType = prop.propertyGenericType();
        if (genType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genType;
            Type[] types = pt.getActualTypeArguments();
            if (types.length == size) {
                if (types[index] instanceof Class<?>) {
                    return (Class<?>) types[index];
                }
                if (types[index] instanceof ParameterizedType) {
                    Type rawType = ((ParameterizedType) types[index]).getRawType();
                    if (rawType instanceof Class<?>) {
                        return (Class<?>) rawType;
                    }
                }
            }
        }
        return null;
    }

}
