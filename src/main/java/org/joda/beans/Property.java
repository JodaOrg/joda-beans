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

/**
 * A property that is linked to a specific bean.
 * <p>
 * For a JavaBean, this will ultimately wrap a get/set method pair.
 * Alternate implementations may perform any logic to obtain the value.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public interface Property<P> {

    /**
     * Gets the bean which owns this property.
     * <p>
     * Each property is fully owned by a single bean.
     * 
     * @param <B>  the bean type
     * @return the bean, not null
     */
    public abstract <B extends Bean> B bean();

    /**
     * Gets the meta-property representing the parts of the property that are
     * common across all instances, such as the name.
     * 
     * @return the meta-property, not null
     */
    public abstract MetaProperty<P> metaProperty();

    /**
     * Gets the property name.
     * <p>
     * The JavaBean style methods getFoo() and setFoo() will lead to a property
     * name of 'foo' and so on.
     * 
     * @return the name of the property, not empty
     */
    public default String name() {
        return metaProperty().name();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the property for the associated bean.
     * <p>
     * For a JavaBean, this is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * Alternate implementations may perform any logic to obtain the value.
     * 
     * @return the value of the property on the bound bean, may be null
     * @throws UnsupportedOperationException if the property is write-only
     */
    public default P get() {
        return metaProperty().get(bean());
    }

    /**
     * Sets the value of the property on the associated bean.
     * <p>
     * The value must be of the correct type for the property.
     * See the meta-property for string conversion.
     * For a standard JavaBean, this is equivalent to calling <code>setFoo()</code> on the bean.
     * Alternate implementations may perform any logic to change the value.
     * 
     * @param value  the value to set into the property on the bean
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    public default void set(Object value) {
        metaProperty().set(bean(), value);
    }

    /**
     * Sets the value of the property on the associated bean and returns the previous value.
     * <p>
     * This is a combination of the {@code get} and {@code set} methods that matches the definition
     * of {@code put} in a {@code Map}.
     * 
     * @param value  the value to set into the property on the bean
     * @return the old value of the property, may be null
     * @throws ClassCastException if the value is of an invalid type for the property
     * @throws UnsupportedOperationException if the property is read-only
     * @throws RuntimeException if the value is rejected by the property (use appropriate subclasses)
     */
    public default P put(Object value) {
        return metaProperty().put(bean(), value);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this property equals another.
     * <p>
     * This compares the meta-property and value.
     * It does not consider the property or bean types.
     * 
     * @param obj  the other property, null returns false
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
