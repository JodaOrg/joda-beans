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

import java.util.Map;

/**
 * A builder for a bean, providing a safe way to create it.
 * <p>
 * This interface allows a bean to be created even if it is immutable.
 * 
 * @param <T>  the type of the bean
 * @author Stephen Colebourne
 */
public interface BeanBuilder<T extends Bean> {

    /**
     * Sets the value of a single property into the builder.
     * <p>
     * This will normally behave as per a {@code Map}, however it may not
     * and as a general rule callers should only set each property once.
     * 
     * @param propertyName  the property name, not null
     * @param value  the property value, may be null
     * @return {@code this}, for chaining, not null
     * @throws RuntimeException optionally thrown if the property name is invalid
     */
    BeanBuilder<T> set(String propertyName, Object value);

    /**
     * Sets the value of a single property into the builder.
     * This method takes a String value and attempts to convert it to
     * the expected type for the property.
     * <p>
     * This will normally behave as per a {@code Map}, however it may not
     * and as a general rule callers should only set each property once.
     * 
     * @param propertyName  the property name, not null
     * @param value  the property value, may be null
     * @return {@code this}, for chaining, not null
     * @throws RuntimeException optionally thrown if the property name is invalid
     */
    BeanBuilder<T> setString(String propertyName, String value);

    /**
     * Sets the value of a map of properties into the builder.
     * <p>
     * Each map entry is used as the input to {@link #set(String, Object)}.
     * <p>
     * This will normally behave as per a {@code Map}, however it may not
     * and as a general rule callers should only set each property once.
     * 
     * @param propertyValueMap  the property name to value map, not null
     * @return {@code this}, for chaining, not null
     * @throws RuntimeException optionally thrown if a property name is invalid
     */
    BeanBuilder<T> setAll(Map<String, ? extends Object> propertyValueMap);

    /**
     * Builds the bean from the state of the builder.
     * <p>
     * Once this method has been called, the builder is in an invalid state.
     * The effect of further method calls is undetermined.
     * 
     * @return the created bean, not null
     */
    T build();

}
