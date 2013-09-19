/*
 *  Copyright 2001-2013 Stephen Colebourne
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
 * A dynamic meta-bean, defining those aspects of a bean which are not specific
 * to a particular instance, such as the type and set of meta-properties.
 * The dynamic aspect allows properties to be defined, such as during building.
 * 
 * @author Stephen Colebourne
 */
public interface DynamicMetaBean extends MetaBean {

    /**
     * Creates a bean builder that can be used to create an instance of this bean.
     * 
     * @return the bean builder, not null
     * @throws UnsupportedOperationException if the bean cannot be created
     */
    @Override
    BeanBuilder<? extends DynamicBean> builder();

    /**
     * Get the type of the bean represented as a {@code Class}.
     * 
     * @return the type of the bean, not null
     */
    @Override
    Class<? extends DynamicBean> beanType();

    /**
     * Gets a meta-property by name, creating it if it does not exist.
     * 
     * @param <R>  the property type, optional, enabling auto-casting
     * @param propertyName  the property name to retrieve, null throws NoSuchElementException
     * @return the meta property, not null
     */
    @Override
    <R> MetaProperty<R> metaProperty(String propertyName);

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
    void metaPropertyDefine(String propertyName, Class<?> propertyType);

    /**
     * Removes a property by name.
     * 
     * @param propertyName  the property name to remove, null ignored
     */
    void metaPropertyRemove(String propertyName);

}
