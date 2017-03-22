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

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.joda.convert.RenameHandler;

/**
 * Type mapper for Joda-Bean serialization, used by serialization implementations.
 *
 * @author Stephen Colebourne
 */
public final class SerTypeMapper {

    /**
     * Known simple classes.
     */
    private static final Map<Class<?>, String> BASIC_TYPES;
    /**
     * Known simple classes.
     */
    private static final Map<String, Class<?>> BASIC_TYPES_REVERSED;
    static {
        Map<Class<?>, String> map = new HashMap<>();
        
        map.put(String.class, "String");
        map.put(Long.class, "Long");
        map.put(Integer.class, "Integer");
        map.put(Short.class, "Short");
        map.put(Byte.class, "Byte");
        map.put(Character.class, "Character");
        map.put(Boolean.class, "Boolean");
        map.put(Double.class, "Double");
        map.put(Float.class, "Float");
        map.put(BigInteger.class, "BigInteger");
        map.put(BigDecimal.class, "BigDecimal");
        map.put(Locale.class, "Locale");
        map.put(Class.class, "Class");
        map.put(UUID.class, "UUID");
        map.put(URI.class, "URI");
        map.put(File.class, "File");
        // selection of types are the most common types suitable for reduction
        // and suitable for simple interpretation on non-Java systems
        
        Map<String, Class<?>> reversed = new HashMap<>();
        for (Entry<Class<?>, String> entry : map.entrySet()) {
            reversed.put(entry.getValue(), entry.getKey());
        }
        BASIC_TYPES = Collections.unmodifiableMap(map);
        BASIC_TYPES_REVERSED = Collections.unmodifiableMap(reversed);
    }

    /**
     * Creates an instance.
     */
    private SerTypeMapper() {
    }

    //-----------------------------------------------------------------------
    /**
     * Encodes a basic class.
     * <p>
     * This handles known simple types, like String, Integer or File, and prefixing.
     * It also allows a map of message specific shorter forms.
     * 
     * @param cls  the class to encode, not null
     * @param settings  the settings object, not null
     * @param basePackage  the base package to use with trailing dot, null if none
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the class object, null if not a basic type
     */
    public static String encodeType(Class<?> cls, final JodaBeanSer settings, final String basePackage, final Map<Class<?>, String> knownTypes) {
        // basic type
        String result = BASIC_TYPES.get(cls);
        if (result != null) {
            return result;
        }
        // handle enum subclasses
        Class<?> supr1 = cls.getSuperclass();
        if (supr1 != null) {
            Class<?> supr2 = supr1.getSuperclass();
            if (supr2 == Enum.class) {
                cls = supr1;
            }
        }
        // calculate
        if (settings.isShortTypes()) {
            if (knownTypes != null) {
                result = knownTypes.get(cls);
                if (result != null) {
                    return result;
                }
            }
            result = cls.getName();
            if (basePackage != null &&
                    result.startsWith(basePackage) &&
                    Character.isUpperCase(result.charAt(basePackage.length())) &&
                    BASIC_TYPES_REVERSED.containsKey(result.substring(basePackage.length())) == false) {
                // use short format
                result = result.substring(basePackage.length());
                if (knownTypes != null) {
                    knownTypes.put(cls, result);
                }
            } else {
                // use long format, short next time if possible
                if (knownTypes != null) {
                    String simpleName = cls.getSimpleName();
                    if (Character.isUpperCase(simpleName.charAt(0)) &&
                            BASIC_TYPES_REVERSED.containsKey(simpleName) == false &&
                            knownTypes.containsValue(simpleName) == false) {
                        knownTypes.put(cls, simpleName);
                    } else {
                        knownTypes.put(cls, result);
                    }
                }
            }
        } else {
            result = cls.getName();
        }
        return result;
    }

    /**
     * Decodes a class, throwing an exception if not found.
     * <p>
     * This uses the context class loader.
     * This handles known simple types, like String, Integer or File, and prefixing.
     * It also allows a map of message specific shorter forms.
     * 
     * @param className  the class name, not null
     * @param settings  the settings object, not null
     * @param basePackage  the base package to use with trailing dot, null if none
     * @param knownTypes  the known types map, null if not using known type shortening
     * @return the class object, not null
     * @throws ClassNotFoundException if not found
     */
    public static Class<?> decodeType(
            String className,
            JodaBeanSer settings,
            String basePackage, 
            Map<String, Class<?>> knownTypes) throws ClassNotFoundException {
        
        return decodeType0(className, settings, basePackage, knownTypes, null);
    }

    /**
     * Decodes a class, returning a default if not found.
     * <p>
     * This uses the context class loader.
     * This handles known simple types, like String, Integer or File, and prefixing.
     * It also allows a map of message specific shorter forms.
     * 
     * @param className  the class name, not null
     * @param settings  the settings object, not null
     * @param basePackage  the base package to use with trailing dot, null if none
     * @param knownTypes  the known types map, null if not using known type shortening
     * @param defaultType  the type to use as a default if the type cannot be found
     * @return the class object, not null
     * @throws ClassNotFoundException if an error occurs
     */
    public static Class<?> decodeType(
            String className,
            JodaBeanSer settings,
            String basePackage, 
            Map<String, Class<?>> knownTypes,
            Class<?> defaultType) throws ClassNotFoundException {

        return decodeType0(className, settings, basePackage, knownTypes, defaultType);
    }

    // internal type decode
    private static Class<?> decodeType0(
            String className,
            JodaBeanSer settings,
            String basePackage, 
            Map<String, Class<?>> knownTypes,
            Class<?> defaultType) throws ClassNotFoundException {

        // basic type
        Class<?> result = BASIC_TYPES_REVERSED.get(className);
        if (result != null) {
            return result;
        }
        // check cache
        if (knownTypes != null) {
            result = knownTypes.get(className);
            if (result != null) {
                return result;
            }
        }
        // calculate
        String fullName = className;
        boolean expanded = false;
        if (basePackage != null && className.length() > 0 && Character.isUpperCase(className.charAt(0))) {
            fullName = basePackage + className;
            expanded = true;
        }
        try {
            result = RenameHandler.INSTANCE.lookupType(fullName);
            if (knownTypes != null) {
                // cache full name
                knownTypes.put(fullName, result);
                if (expanded) {
                    // cache short name
                    knownTypes.put(className, result);
                } else {
                    // derive and cache short name
                    String simpleName = result.getSimpleName();
                    // handle renames
                    if (fullName.equals(result.getName()) == false &&
                            RenameHandler.INSTANCE.getTypeRenames().containsKey(fullName) &&
                            result.getEnclosingClass() == null) {
                        simpleName = fullName.substring(fullName.lastIndexOf(".") + 1);
                    }
                    if (Character.isUpperCase(simpleName.charAt(0)) &&
                            BASIC_TYPES_REVERSED.containsKey(simpleName) == false &&
                            knownTypes.containsKey(simpleName) == false) {
                        knownTypes.put(simpleName, result);
                    }
                }
            }
            return result;
        } catch (ClassNotFoundException ex) {
            // handle pathological case of package name starting with upper case
            if (fullName.equals(className) == false) {
                try {
                    result = RenameHandler.INSTANCE.lookupType(className);
                    if (knownTypes != null) {
                        knownTypes.put(className, result);
                    }
                    return result;
                } catch (ClassNotFoundException ignored) {
                }
            }
            if (defaultType == null) {
                throw ex;
            }
            return defaultType;
        }
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
