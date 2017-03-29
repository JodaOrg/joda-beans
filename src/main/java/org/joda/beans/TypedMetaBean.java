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
 * A meta-bean that captures the type of the bean.
 * <p>
 * It is not possible to add the generic type to all beans, as the type cannot be
 * refined in hierarchies. This interface is thus useful when there are no subclasses.
 * 
 * @author Stephen Colebourne
 * @param <T>  the type of the bean
 */
public interface TypedMetaBean<T extends Bean> extends MetaBean {

    @Override
    public abstract BeanBuilder<T> builder();

    @Override
    public abstract Class<T> beanType();

}
