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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A meta-property, defining those aspects of a property which are not specific
 * to a particular bean, such as the property type and name.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public interface MetaProperty<P> {

    /**
     * Creates a property that binds this meta-property to a specific bean.
     * 
     * @param bean  the bean to create the property for, not null
     * @return the property, not null
     */
    Property<P> createProperty(Bean bean);

    //-----------------------------------------------------------------------
    /**
     * Gets the meta-bean which owns this meta-property.
     * <p>
     * Each meta-property is fully owned by a single bean.
     * 
     * @return the meta-bean, not null
     */
    MetaBean metaBean();

    /**
     * Gets the property name.
     * <p>
     * The JavaBean style methods getFoo() and setFoo() will lead to a property
     * name of 'foo' and so on.
     * 
     * @return the name of the property, not empty
     */
    String name();

    /**
     * Get the type that declares the property, represented as a {@code Class}.
     * 
     * @return the type declaring the property, not null
     */
    Class<?> declaringType();

    /**
     * Get the type of the property represented as a {@code Class}.
     * 
     * @return the type of the property, not null
     */
    Class<P> propertyType();

    /**
     * Gets the generic types of the property.
     * <p>
     * This provides access to the generic type declared in the source code.
     * 
     * @return the full generic type of the property, unmodifiable, not null
     */
    Type propertyGenericType();

    /**
     * Gets whether the property is read-only, read-write or write-only.
     * 
     * @return the property read-write type, not null
     */
    PropertyReadWrite readWrite();

    //-----------------------------------------------------------------------
    /**
     * Gets the annotations of the property.
     * 
     * @return the annotations, unmodifiable, not null
     */
    List<Annotation> annotations();

    /**
     * Gets a specified annotation of the property.
     * 
     * @return the annotation, not null
     * @throws NoSuchElementException if the annotation is not specified
     */
    <A extends Annotation> A annotation(Class<A> annotation);

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the property for the specified bean.
     * <p>
     * For a standard JavaBean, this is equivalent to calling <code>getFoo()</code> on the bean.
     * Alternate implementations may perform any logic to obtain the value.
     * 
     * @param bean  the bean to query, not null
     * @return the value of the property on the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws UnsupportedOperationException if the property is write-only
     */
    P get(Bean bean);

    /**
     * Sets the value of the property on the specified bean.
     * <p>
     * For a standard JavaBean, this is equivalent to calling <code>setFoo()</code> on the bean.
     * Alternate implementations may perform any logic to change the value.
     * 
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    void set(Bean bean, P value);

    /**
     * Sets the value of the property on the associated bean and returns the previous value.
     * <p>
     * This is a combination of the {@code get} and {@code set} methods that matches the definition
     * of {@code put} in a {@code Map}.
     * 
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the specified bean, may be null
     * @return the old value of the property, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    P put(Bean bean, P value);

    //-----------------------------------------------------------------------
    /**
     * Checks if this meta-property equals another.
     * <p>
     * This compares the property name and declaring type.
     * It does not compare the property or bean types.
     * 
     * @param obj  the other meta-property, null returns false
     * @return true if equal
     */
    @Override
    boolean equals(Object obj);

    /**
     * Returns a suitable hash code.
     * 
     * @return the hash code
     */
    @Override
    int hashCode();

}
