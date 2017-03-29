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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * A meta-bean, defining those aspects of a bean which are not specific
 * to a particular instance, such as the type and set of meta-properties.
 * <p>
 * This interface can be thought of as the equivalent of {@link Class} but for beans.
 * In most cases the meta-bean will be code generated and the concrete class will have additional methods.
 * 
 * @author Stephen Colebourne
 */
public interface MetaBean {

    /**
     * Obtains a meta-bean from a {@code Class}.
     * <p>
     * This will return a meta-bean if it has been registered, or if the class
     * implements {@link DynamicBean} and has a no-args constructor.
     * Note that the common case where the meta-bean is registered by a static initializer is handled.
     * 
     * @param cls  the class to get the meta-bean for, not null
     * @return the meta-bean associated with the class, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static MetaBean of(Class<?> cls) {
        return MetaBeans.lookup(cls);
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
    public static void register(MetaBean metaBean) {
        MetaBeans.register(metaBean);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks whether this bean is buildable or not.
     * <p>
     * A buildable bean can be constructed using {@link #builder()}.
     * If this method returns true then {@code builder()} must return a valid builder.
     * If this method returns false then {@code builder()} must throw {@link UnsupportedOperationException}.
     * 
     * @return true if this bean is buildable
     */
    public abstract boolean isBuildable();

    /**
     * Creates a bean builder that can be used to create an instance of this bean.
     * <p>
     * The builder is used in two main ways.
     * The first is to allow immutable beans to be constructed.
     * The second is to enable automated tools like serialization/deserialization.
     * <p>
     * The builder can be thought of as a {@code Map} of {@link MetaProperty} to value.
     * Note that the implementation is not necessarily an actual map.
     * 
     * @return the bean builder, not null
     * @throws UnsupportedOperationException if the bean cannot be created
     */
    public abstract BeanBuilder<? extends Bean> builder();

    //-----------------------------------------------------------------------
    /**
     * Gets the bean name, which is normally the fully qualified class name of the bean.
     * <p>
     * This is primarily used for human-readable output.
     * 
     * @return the name of the bean, not empty
     */
    public default String beanName() {
        return beanType().getName();
    }

    /**
     * Get the type of the bean, represented as a {@code Class}.
     * <p>
     * A {@code MetaBean} can be thought of as the equivalent of {@link Class} but for beans.
     * This method allows the actual {@code Class} instance of the bean to be obtained.
     * 
     * @return the type of the bean, not null
     */
    public abstract Class<? extends Bean> beanType();

    //-----------------------------------------------------------------------
    /**
     * Counts the number of properties.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method returns the count of properties.
     * 
     * @return the number of properties
     */
    public default int metaPropertyCount() {
        return metaPropertyMap().size();
    }

    /**
     * Checks if a property exists.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method checks whether there is a property with the specified name.
     * 
     * @param propertyName  the property name to check, null returns false
     * @return true if the property exists
     */
    public default boolean metaPropertyExists(String propertyName) {
        return metaPropertyMap().containsKey(propertyName);
    }

    /**
     * Gets a meta-property by name.
     * <p>
     * Each meta-bean manages a single bean with a known set of properties.
     * This method returns the property with the specified name.
     * <p>
     * The base interface throws an exception if the name is not recognised.
     * By contrast, the {@code DynamicMetaBean} subinterface creates the property on demand.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, not null
     * @return the meta property, not null
     * @throws NoSuchElementException if the property name is invalid
     */
    @SuppressWarnings("unchecked")
    public default <R> MetaProperty<R> metaProperty(String propertyName) {
        MetaProperty<?> mp = metaPropertyMap().get(propertyName);
        if (mp == null) {
            throw new NoSuchElementException("Unknown property: " + propertyName);
        }
        return (MetaProperty<R>) mp;
    }

    /**
     * Gets an iterator of meta-properties.
     * <p>
     * This method returns an {@code Iterable}, which is simpler than a {@code Map}.
     * As a result, implementations may be able to optimise, and so this method should be
     * preferred to {@link #metaPropertyMap()} where a choice is possible.
     * 
     * @return the unmodifiable map of meta property objects, not null
     */
    public default Iterable<MetaProperty<?>> metaPropertyIterable() {
        return metaPropertyMap().values();
    }

    /**
     * Gets the map of meta-properties, keyed by property name.
     * <p>
     * Where possible, use {@link #metaPropertyIterable()} instead as it typically has better performance.
     * 
     * @return the unmodifiable map of meta property objects, not null
     */
    public abstract Map<String, MetaProperty<?>> metaPropertyMap();

    /**
     * Gets the annotations associated with this bean.
     * <p>
     * The annotations are queried from the bean.
     * This is typically accomplished by querying the annotations of an underlying
     * {@link Class} however any strategy is permitted.
     * <p>
     * If the implementation has a mutable set of annotations, then the result of
     * this method must stream over those annotations in existence when this method
     * is called to avoid concurrency issues.
     * <p>
     * The default implementation uses the annotations from {@link #beanType()}.
     * 
     * @return the annotations, unmodifiable, not null
     */
    public default List<Annotation> annotations() {
        return Collections.unmodifiableList(Arrays.asList(beanType().getAnnotations()));
    }

    /**
     * Gets an annotation from the bean.
     * <p>
     * The annotations are queried from the bean.
     * This is typically accomplished by querying the annotations of an underlying
     * {@link Class} however any strategy is permitted.
     * 
     * @param <A>  the annotation type
     * @param annotationClass  the annotation class to find, not null
     * @return the annotation, not null
     * @throws NoSuchElementException if the annotation is not specified
     */
    @SuppressWarnings("unchecked")
    public default <A extends Annotation> A annotation(Class<A> annotationClass) {
        List<Annotation> annotations = annotations();
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation)) {
                return (A) annotation;
            }
        }
        throw new NoSuchElementException("Unknown annotation: " + annotationClass.getName());
    }

}
