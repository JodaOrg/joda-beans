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
 * A dynamic meta-bean which works with {@code DynamicBean}.
 * <p>
 * A dynamic bean can have properties added or removed at any time.
 * As such, there is a different meta-bean for each dynamic bean.
 * The meta-bean allows meta-properties to be created on demand.
 * 
 * @author Stephen Colebourne
 */
public interface DynamicMetaBean extends MetaBean {

    /**
     * Creates a bean builder that can be used to create an instance of this bean.
     * <p>
     * All properties added to the builder will be created and appear in the result. 
     * 
     * @return the bean builder, not null
     * @throws UnsupportedOperationException if the bean cannot be created
     */
    @Override
    public abstract BeanBuilder<? extends DynamicBean> builder();

    /**
     * Get the type of the bean represented as a {@code Class}.
     * 
     * @return the type of the bean, not null
     */
    @Override
    public abstract Class<? extends DynamicBean> beanType();

    /**
     * Gets a meta-property by name.
     * <p>
     * This will not throw an exception if the meta-property name does not exist.
     * Whether a meta-property is immediately created or not is implementation dependent.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, not null
     * @return the meta property, not null
     */
    @Override
    public abstract <R> MetaProperty<R> metaProperty(String propertyName);

    //-----------------------------------------------------------------------
    /**
     * Defines a property for the bean.
     * <p>
     * Some implementations will automatically add properties, in which case this
     * method will have no effect.
     * 
     * @param propertyName  the property name to check, not empty, not null
     * @param propertyType  the property type, not null
     */
    public abstract void metaPropertyDefine(String propertyName, Class<?> propertyType);

    /**
     * Removes a property by name.
     * 
     * @param propertyName  the property name to remove, null ignored
     */
    public abstract void metaPropertyRemove(String propertyName);

}
