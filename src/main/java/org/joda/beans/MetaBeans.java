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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.impl.map.MapBean;

/**
 * Utilities for registered meta-beans.
 */
final class MetaBeans {

    /**
     * The cache of meta-beans.
     */
    private static final ConcurrentHashMap<Class<?>, MetaBean> META_BEANS = new ConcurrentHashMap<>();

    /**
     * The cache of meta-bean providers; access is guarded by a lock on {@code MetaBeans.class}.
     */
    private static final Map<Class<?>, MetaBeanProvider> META_BEAN_PROVIDERS = new HashMap<>();

    /**
     * Restricted constructor.
     */
    private MetaBeans() {
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the meta-bean for a class.
     * <p>
     * This only works for those beans that have registered their meta-beans.
     * <p>
     * A {@code Class} may use a static initializer block to call {@code register}.
     * The edge case where the class is loaded but not initialized is handled
     * by forcing the class to be initialized if necessary.
     * 
     * @param cls  the class to get the meta-bean for, not null
     * @return the meta-bean, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    static MetaBean lookup(Class<?> cls) {
        MetaBean meta = META_BEANS.get(cls);
        if (meta == null) {
            return metaBeanLookup(cls);
        }
        return meta;
    }

    // lookup the MetaBean outside the fast path, aiding hotspot inlining
    private static MetaBean metaBeanLookup(Class<?> cls) {
        // handle dynamic beans
        if (cls == FlexiBean.class) {
            return new FlexiBean().metaBean();
        } else if (cls == MapBean.class) {
            return new MapBean().metaBean();
        } else if (DynamicBean.class.isAssignableFrom(cls)) {
            try {
                return cls.asSubclass(DynamicBean.class).getDeclaredConstructor().newInstance().metaBean();
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException ex) {
                throw new IllegalArgumentException("Unable to find meta-bean for a DynamicBean: " + cls.getName(), ex);
            }
        }
        // a Class can be loaded without being initialized
        // in this state, the static initializers have not run, and thus the metabean not registered
        // here initialization is forced to handle that scenario
        try {
            cls = Class.forName(cls.getName(), true, cls.getClassLoader());
        } catch (ClassNotFoundException | Error ex) {
            // should be impossible
            throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName(), ex);
        }
        MetaBean meta = META_BEANS.get(cls);
        if (meta != null) {
            return meta;
        }
        MetaProvider providerAnnotation = findProviderAnnotation(cls);
        if (providerAnnotation != null) {
            // Synchronization is necessary to prevent a race condition where the same meta-bean is registered twice
            synchronized (MetaBeans.class) {
                // Re-check in case the meta-bean has been added by another thread since we checked above
                meta = META_BEANS.get(cls);
                if (meta != null) {
                    return meta;
                }
                Class<? extends MetaBeanProvider> providerClass = providerAnnotation.value();
                try {
                    MetaBeanProvider provider = META_BEAN_PROVIDERS.get(providerClass);
                    if (provider == null) {
                        provider = providerClass.getDeclaredConstructor().newInstance();
                        META_BEAN_PROVIDERS.put(providerClass, provider);
                    }
                    meta = provider.findMetaBean(cls);
                    if (meta == null) {
                        throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName());
                    }
                    register(meta);
                    return meta;
                } catch (Exception e) {
                    throw new IllegalStateException("Unable to create instance of " + providerClass.getName() +
                        " to provide meta bean for " + cls.getName(), e);
                }
            }
        }
        throw new IllegalArgumentException("Unable to find meta-bean: " + cls.getName());
    }

    // returns the MetaProvider annotation from the class or null if none can be found.
    // the class and all its superclasses and interfaces are searched.
    // if the annotation is found in multiple places then it is undefined which is returned.
    private static MetaProvider findProviderAnnotation(Class<?> cls) {
        MetaProvider providerAnnotation = cls.getAnnotation(MetaProvider.class);
        if (providerAnnotation != null) {
            return providerAnnotation;
        }
        for (Class<?> implementedInterface : cls.getInterfaces()) {
            providerAnnotation = implementedInterface.getAnnotation(MetaProvider.class);
            if (providerAnnotation != null) {
                return providerAnnotation;
            }
        }
        Class<?> superclass = cls.getSuperclass();
        if (superclass.equals(Object.class)) {
            return null;
        }
        return findProviderAnnotation(superclass);
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
    static void register(MetaBean metaBean) {
        Class<? extends Bean> type = metaBean.beanType();
        if (META_BEANS.putIfAbsent(type, metaBean) != null) {
            throw new IllegalArgumentException("Cannot register class twice: " + type.getName());
        }
    }

}
