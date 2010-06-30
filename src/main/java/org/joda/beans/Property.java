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

/**
 * A property that is linked to a specific bean.
 * <p>
 * For a JavaBean, this will ultimately wrap a get/set method pair.
 * Alternate implementations may perform any logic to obtain the value.
 * 
 * @param <B>  the type of the bean
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public interface Property<B, P> {

    /**
     * Gets the bean which owns this property.
     * <p>
     * Each property is fully owned by a single bean.
     * 
     * @return the bean, never null
     */
    B bean();

    /**
     * Gets the meta-property representing the parts of the property that are
     * common across all instances, such as the name.
     * 
     * @return the meta-property, never null
     */
    MetaProperty<B, P> metaProperty();

    //-----------------------------------------------------------------------
    /**
     * Gets the value of the property for the associated bean.
     * <p>
     * For a JavaBean, this is the equivalent to calling <code>getFoo()</code> on the bean itself.
     * Alternate implementations may perform any logic to obtain the value.
     * 
     * @return the value of the property on the bound bean
     * @throws UnsupportedOperationException if the property is write-only
     */
    P get();

    /**
     * Sets the value of the property on the associated bean.
     * <p>
     * For a standard JavaBean, this is equivalent to calling <code>setFoo()</code> on the bean.
     * Alternate implementations may perform any logic to change the value.
     * 
     * @param value  the value to set into the property on the bean
     * @throws UnsupportedOperationException if the property is read-only
     */
    void set(P value);

}
