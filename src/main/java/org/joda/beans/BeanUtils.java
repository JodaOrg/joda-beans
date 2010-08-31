/*
 *  Copyright 2001-2010 Stephen Colebourne
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

/**
 * A set of utilities to assist when working with beans and properties.
 * 
 * @author Stephen Colebourne
 */
public final class BeanUtils {

    /**
     * Restricted constructor.
     */
    private BeanUtils() {
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
