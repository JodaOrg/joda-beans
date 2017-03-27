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
import java.lang.reflect.Type;
import java.util.List;
import java.util.NoSuchElementException;

import org.joda.beans.impl.BasicProperty;
import org.joda.convert.StringConvert;

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
     * <p>
     * This method returns a {@code Property} instance that connects this meta-property to the specified bean.
     * The result can be queried and passed around without further reference to the bean.
     * 
     * @param bean  the bean to create the property for, not null
     * @return the property, not null
     */
    public default Property<P> createProperty(Bean bean) {
        return BasicProperty.of(bean, this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the meta-bean which owns this meta-property.
     * <p>
     * Each meta-property is fully owned by a single bean.
     * 
     * @return the meta-bean, not null
     */
    public abstract MetaBean metaBean();

    /**
     * Gets the property name.
     * <p>
     * The JavaBean style methods getFoo() and setFoo() will lead to a property
     * name of 'foo' and so on.
     * 
     * @return the name of the property, not empty
     */
    public abstract String name();

    /**
     * Get the type that declares the property, represented as a {@code Class}.
     * <p>
     * This is the type of the bean where the property is declared.
     * 
     * @return the type declaring the property, not null
     */
    public abstract Class<?> declaringType();

    /**
     * Get the type of the property represented as a {@code Class}.
     * <p>
     * This is the type of the property.
     * For example, the surname of a person would typically be a {@code String}.
     * 
     * @return the type of the property, not null
     */
    public abstract Class<P> propertyType();

    /**
     * Gets the generic types of the property.
     * <p>
     * This provides access to the generic type declared in the source code.
     * 
     * @return the full generic type of the property, unmodifiable, not null
     */
    public abstract Type propertyGenericType();

    /**
     * Gets the style of the property, such as read-only, read-write or write-only.
     * <p>
     * Rather than testing against specific values, it is strongly recommended to
     * call the helper methods on the returned style.
     * 
     * @return the property style, not null
     */
    public abstract PropertyStyle style();

    //-----------------------------------------------------------------------
    /**
     * Gets the annotations of the property.
     * <p>
     * The annotations are queried from the property.
     * This is typically accomplished by querying the annotations of the underlying
     * instance variable however any strategy is permitted.
     * 
     * @return the annotations, unmodifiable, not null
     */
    public abstract List<Annotation> annotations();

    /**
     * Gets an annotation from the property.
     * <p>
     * The annotations are queried from the property.
     * This is typically accomplished by querying the annotations of the underlying
     * instance variable however any strategy is permitted..
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
    public abstract P get(Bean bean);

    /**
     * Sets the value of the property on the specified bean.
     * <p>
     * The value must be of the correct type for the property.
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
    public abstract void set(Bean bean, Object value);

    /**
     * Sets the value of the property on the associated bean and returns the previous value.
     * <p>
     * The value must be of the correct type for the property.
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
    public default P put(Bean bean, Object value) {
        P old = get(bean);
        set(bean, value);
        return old;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the property for the specified bean converted to a string.
     * <p>
     * This converts the result of {@link #get(Bean)} to a standard format string.
     * Conversion uses Joda-Convert.
     * Not all object types can be converted to a string, see Joda-Convert.
     * <p>
     * For a standard JavaBean, this is equivalent to calling <code>getFoo()</code> on the bean.
     * Alternate implementations may perform any logic to obtain the value.
     * 
     * @param bean  the bean to query, not null
     * @return the value of the property on the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws UnsupportedOperationException if the property is write-only
     * @throws RuntimeException if the value cannot be converted to a string (use appropriate subclasses)
     */
    public default String getString(Bean bean) {
        return getString(bean, JodaBeanUtils.stringConverter());
    }

    /**
     * Gets the value of the property for the specified bean converted to a string.
     * <p>
     * This converts the result of {@link #get(Bean)} to a standard format string using the supplied converter.
     * Not all object types can be converted to a string, see Joda-Convert.
     * <p>
     * For a standard JavaBean, this is equivalent to calling <code>getFoo()</code> on the bean.
     * Alternate implementations may perform any logic to obtain the value.
     *
     * @param bean  the bean to query, not null
     * @param stringConvert  the converter to use, not null
     * @return the value of the property on the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws UnsupportedOperationException if the property is write-only
     * @throws RuntimeException if the value cannot be converted to a string (use appropriate subclasses)
     */
    public default String getString(Bean bean, StringConvert stringConvert) {
        P value = get(bean);
        return stringConvert.convertToString(propertyType(), value);
    }

    /**
     * Sets the value of the property on the specified bean from a string by conversion.
     * <p>
     * This converts the string to the correct type for the property and then sets it
     * using {@link #set(Bean, Object)}. Conversion uses Joda-Convert.
     * 
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    public default void setString(Bean bean, String value) {
        setString(bean, value, JodaBeanUtils.stringConverter());
    }

    /**
     * Sets the value of the property on the specified bean from a string by conversion.
     * <p>
     * This converts the string to the correct type for the property using the supplied converter and then sets it
     * using {@link #set(Bean, Object)}.
     *
     * @param bean  the bean to update, not null
     * @param value  the value to set into the property on the specified bean, may be null
     * @param stringConvert  the converter, not null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    public default void setString(Bean bean, String value, StringConvert stringConvert) {
        set(bean, stringConvert.convertFromString(propertyType(), value));
    }

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
    public abstract boolean equals(Object obj);

    /**
     * Returns a suitable hash code.
     * 
     * @return the hash code
     */
    @Override
    public abstract int hashCode();

}
