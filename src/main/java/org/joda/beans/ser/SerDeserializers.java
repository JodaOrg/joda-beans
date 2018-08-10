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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.convert.RenameHandler;

/**
 * Manages a map of deserializers that assist with data migration.
 * <p>
 * Deserializers handle situations where the data being read does not match the
 * bean in the classpath. See also {@code RenameHandler}.
 * <p>
 * Normally, it makes sense to customize the shared singleton instance, because
 * the classpath is static and fixed and the transformations are common.
 * <p>
 * Implementations must be thread-safe singletons.
 *
 * @author Stephen Colebourne
 */
public final class SerDeserializers {

    /**
     * Deserializers loaded from the classpath.
     */
    private static final Map<Class<?>, SerDeserializer> CLASSPATH_STRICT = loadFromClasspath();
    /**
     * Deserializers loaded from the classpath.
     */
    private static final Map<Class<?>, SerDeserializer> CLASSPATH_LENIENT = CLASSPATH_STRICT.entrySet().stream()
            .map(e -> new SimpleEntry<>(e.getKey(), toLenient(e.getValue())))
            .collect(toMap(e -> e.getKey(), e -> e.getValue()));
    /**
     * Shared global instance which can be mutated.
     */
    public static final SerDeserializers INSTANCE = new SerDeserializers(false);
    /**
     * Lenient instance which can be mutated.
     */
    public static final SerDeserializers LENIENT = new SerDeserializers(true);

    /**
     * Whether deserialization is lenient.
     */
    private final boolean lenient;
    /**
     * The default deserializer.
     */
    private final SerDeserializer defaultDeserializer;
    /**
     * The deserializers.
     */
    private final ConcurrentMap<Class<?>, SerDeserializer> deserializers = new ConcurrentHashMap<>();
    /**
     * The deserializer providers.
     */
    private final CopyOnWriteArrayList<SerDeserializerProvider> providers = new CopyOnWriteArrayList<>();

    /**
     * Creates an instance.
     */
    public SerDeserializers() {
        this.lenient = false;
        this.defaultDeserializer = DefaultDeserializer.INSTANCE;
        this.deserializers.putAll(CLASSPATH_STRICT);
    }

    /**
     * Creates an instance using additional providers.
     * 
     * @param providers  the providers to use
     */
    public SerDeserializers(SerDeserializerProvider... providers) {
        this(false, providers);
    }

    /**
     * Creates an instance using additional providers.
     * 
     * @param lenient  whether to deserialize leniently
     * @param providers  the providers to use
     */
    public SerDeserializers(boolean lenient, SerDeserializerProvider... providers) {
        this.lenient = lenient;
        this.defaultDeserializer = lenient ? LenientDeserializer.INSTANCE : DefaultDeserializer.INSTANCE;
        this.deserializers.putAll(lenient ? CLASSPATH_LENIENT : CLASSPATH_STRICT);
        this.providers.addAll(Arrays.asList(providers));
    }

    //-----------------------------------------------------------------------
    /**
     * Adds the deserializer to be used for the specified type.
     * 
     * @param type  the type, not null
     * @param deserializer  the deserializer, not null
     * @return this, for chaining, not null
     */
    public SerDeserializers register(Class<?> type, SerDeserializer deserializer) {
        deserializers.put(type, deserializer);
        return this;
    }

    /**
     * Gets the map of deserializers which can be modified.
     * 
     * @return the map of deserializers, not null
     */
    public ConcurrentMap<Class<?>, SerDeserializer> getDeserializers() {
        return deserializers;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds the deserializer provider to be used.
     * 
     * @param provider  the deserializer provider, not null
     * @return this, for chaining, not null
     */
    public SerDeserializers registerProvider(SerDeserializerProvider provider) {
        providers.add(provider);
        return this;
    }

    //-----------------------------------------------------------------------
    /**
     * Finds the deserializer for the specified type.
     * <p>
     * The {@code DefaultDeserializer} is used if one has not been registered.
     * 
     * @param type  the type, not null
     * @return the deserializer, not null
     */
    public SerDeserializer findDeserializer(Class<?> type) {
        SerDeserializer deser = deserializers.get(type);
        if (deser != null) {
            return deser;
        }
        for (SerDeserializerProvider provider : providers) {
            deser = provider.findDeserializer(type);
            if (deser != null) {
                return deser;
            }
        }
        return defaultDeserializer;
    }

    /**
     * Decodes the type
     * 
     * @param typeStr  the type, not null
     * @param settings  the settings, not null
     * @param basePackage  the base package, not null
     * @param knownTypes  the known types, not null
     * @param defaultType  the default type, not null
     * @return the decoded type
     * @throws ClassNotFoundException if the class is not found
     */
    public Class<?> decodeType(
            String typeStr,
            JodaBeanSer settings,
            String basePackage,
            Map<String, Class<?>> knownTypes,
            Class<?> defaultType) throws ClassNotFoundException {
        
        if (lenient) {
            return SerTypeMapper.decodeType(
                    typeStr, settings, basePackage, knownTypes, defaultType == Object.class ? String.class : defaultType);
        }
        return SerTypeMapper.decodeType(typeStr, settings, basePackage, knownTypes);
    }

    //-----------------------------------------------------------------------
    // loads config files
    private static Map<Class<?>, SerDeserializer> loadFromClasspath() {
        // log errors to System.err, as problems in static initializers can be troublesome to diagnose
        Map<Class<?>, SerDeserializer> result = new HashMap<>();
        URL url = null;
        try {
            // this is the new location of the file, working on Java 8, Java 9 class path and Java 9 module path
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = RenameHandler.class.getClassLoader();
            }
            Enumeration<URL> en = loader.getResources("META-INF/org/joda/beans/JodaBeans.ini");
            while (en.hasMoreElements()) {
                url = en.nextElement();
                List<String> lines = loadRenameFile(url);
                parseRenameFile(lines, url, result);
            }
        } catch (Error | Exception ex) {
            System.err.println("ERROR: Unable to load JodaBeans.ini: " + url + ": " + ex.getMessage());
            ex.printStackTrace();
            result.clear();
        }
        return result;
    }

    // loads a single rename file
    private static List<String> loadRenameFile(URL url) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
            return reader.lines()
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(toList());
        }
    }

    // parses a single rename file
    private static void parseRenameFile(List<String> lines, URL url, Map<Class<?>, SerDeserializer> map) {
        // format allows multiple [deserializers] so file can be merged
        boolean deserializers = false;
        for (String line : lines) {
            try {
                if (line.equals("[deserializers]")) {
                    deserializers = true;
                } else if (deserializers) {
                    int equalsPos = line.indexOf('=');
                    String beanName = equalsPos >= 0 ? line.substring(0, equalsPos).trim() : line;
                    String deserName = equalsPos >= 0 ? line.substring(equalsPos + 1).trim() : line;
                    registerFromClasspath(beanName, deserName, map);
                } else {
                    throw new IllegalArgumentException("JodaBeans.ini must start with [deserializers]");
                }
            } catch (Exception ex) {
                System.err.println("ERROR: Invalid JodaBeans.ini: " + url + ": " + ex.getMessage());
            }
        }
    }

    // parses and registers the classes
    private static void registerFromClasspath(
            String beanName, String deserName, Map<Class<?>, SerDeserializer> map) throws Exception {

        Class<? extends Bean> beanClass = Class.forName(beanName).asSubclass(Bean.class);
        Class<?> deserClass = Class.forName(deserName);

        Field field = null;
        SerDeserializer deser;
        try {
            field = deserClass.getDeclaredField("DESERIALIZER");
            if (!Modifier.isStatic(field.getModifiers())) {
                throw new IllegalStateException("Field " + field + " must be static");
            }
            deser = SerDeserializer.class.cast(field.get(null));
        } catch (NoSuchFieldException ex) {
            Constructor<?> cons = null;
            try {
                cons = deserClass.getConstructor();
                deser = SerDeserializer.class.cast(cons.newInstance());
            } catch (NoSuchMethodException ex2) {
                throw new IllegalStateException(
                        "Class " + deserClass.getName() + " must have field DESERIALIZER or a no-arg constructor");
            } catch (IllegalAccessException ex2) {
                cons.setAccessible(true);
                deser = SerDeserializer.class.cast(cons.newInstance());
            }
        } catch (IllegalAccessException ex) {
            field.setAccessible(true);
            deser = SerDeserializer.class.cast(field.get(null));
        }
        map.put(beanClass, deser);
    }

    // makes the deserializer lenient
    private static SerDeserializer toLenient(SerDeserializer underlying) {
        return new SerDeserializer() {

            @Override
            public MetaBean findMetaBean(Class<?> beanType) {
                return underlying.findMetaBean(beanType);
            }

            @Override
            public BeanBuilder<?> createBuilder(Class<?> beanType, MetaBean metaBean) {
                return underlying.createBuilder(beanType, metaBean);
            }

            @Override
            public MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName) {
                // dynamic beans force code by exception
                try {
                    return underlying.findMetaProperty(beanType, metaBean, propertyName);
                } catch (NoSuchElementException ex) {
                    return null;
                }
            }

            @Override
            public void setValue(BeanBuilder<?> builder, MetaProperty<?> metaProp, Object value) {
                underlying.setValue(builder, metaProp, value);
            }

            @Override
            public Object build(Class<?> beanType, BeanBuilder<?> builder) {
                return underlying.build(beanType, builder);
            }
        };
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
