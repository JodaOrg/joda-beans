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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

/**
 * Assists with serialization and deserialization of optional properties.
 */
public class SerOptional {

    /**
     * Map of known optional types.
     */
    private static final Map<Class<?>, Object[]> OPTIONALS;
    static {
        Map<Class<?>, Object[]> map = new HashMap<>();
        try {
            Class<?> cls = Class.forName("com.google.common.base.Optional");
            Method create = cls.getMethod("of", Object.class);
            Object nullConstant = cls.getMethod("absent").invoke(null);
            Method isPresent = cls.getMethod("isPresent");
            Method get = cls.getMethod("get");
            map.put(cls, new Object[] {create, nullConstant, isPresent, get});
        } catch (Exception ex) {
            // ignore
        }
        try {
            Class<?> cls = Class.forName("java.util.Optional");
            Method create = cls.getMethod("of", Object.class);
            Object nullConstant = cls.getMethod("empty").invoke(null);
            Method isPresent = cls.getMethod("isPresent");
            Method get = cls.getMethod("get");
            map.put(cls, new Object[] {create, nullConstant, isPresent, get});
        } catch (Exception ex) {
            // ignore
        }
        try {
            Class<?> cls = Class.forName("java.util.OptionalDouble");
            Method create = cls.getMethod("of", Double.TYPE);
            Object nullConstant = cls.getMethod("empty").invoke(null);
            Method isPresent = cls.getMethod("isPresent");
            Method get = cls.getMethod("getAsDouble");
            map.put(cls, new Object[] {create, nullConstant, isPresent, get});
        } catch (Exception ex) {
            // ignore
        }
        try {
            Class<?> cls = Class.forName("java.util.OptionalInt");
            Method create = cls.getMethod("of", Integer.TYPE);
            Object nullConstant = cls.getMethod("empty").invoke(null);
            Method isPresent = cls.getMethod("isPresent");
            Method get = cls.getMethod("getAsInt");
            map.put(cls, new Object[] {create, nullConstant, isPresent, get});
        } catch (Exception ex) {
            // ignore
        }
        try {
            Class<?> cls = Class.forName("java.util.OptionalLong");
            Method create = cls.getMethod("of", Long.TYPE);
            Object nullConstant = cls.getMethod("empty").invoke(null);
            Method isPresent = cls.getMethod("isPresent");
            Method get = cls.getMethod("getAsLong");
            map.put(cls, new Object[] {create, nullConstant, isPresent, get});
        } catch (Exception ex) {
            // ignore
        }
        if (map.isEmpty()) {
            OPTIONALS = Collections.emptyMap();
        } else {
            OPTIONALS = map;
        }
    }

    /**
     * Extracts the value of the property from a bean, unwrapping any optional.
     * 
     * @param metaProp  the property to query, not null
     * @param bean  the bean to query, not null
     * @return the value of the property, with any optional wrapper removed
     */
    public static Object extractValue(MetaProperty<?> metaProp, Bean bean) {
        Object value = metaProp.get(bean);
        if (value != null) {
            Object[] helpers = OPTIONALS.get(metaProp.propertyType());
            if (helpers != null) {
                try {
                    boolean present = (Boolean) ((Method) helpers[2]).invoke(value);
                    if (present) {
                        value = ((Method) helpers[3]).invoke(value);
                    } else {
                        value = null;
                    }
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        return value;
    }

    /**
     * Extracts the value of the property from a bean, unwrapping any optional.
     * 
     * @param metaProp  the property to query, not null
     * @param beanType  the type of the bean, not null
     * @return the type of the property with any optional wrapper removed
     */
    public static Class<?> extractType(MetaProperty<?> metaProp, Class<?> beanType) {
        Class<?> type = metaProp.propertyType();
        Object[] helpers = OPTIONALS.get(type);
        if (helpers != null) {
            try {
                Class<?> genericType = JodaBeanUtils.extractTypeClass(metaProp, beanType, 1, 0);
                type = (genericType != null ? genericType : type);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return type;
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
        Object[] helpers = OPTIONALS.get(metaProp.propertyType());
        if (helpers != null) {
            try {
                if (value != null) {
                    value = ((Method) helpers[0]).invoke(null, value);
                } else {
                    value = helpers[1];
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return value;
    }

}
