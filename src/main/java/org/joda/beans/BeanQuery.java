/*
 *  Copyright 2001-2014 Stephen Colebourne
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
 * A query based on beans.
 * <p>
 * The query interface provides the simplest way to query beans.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public interface BeanQuery<P> {

    /**
     * Queries a value from the specified bean.
     * <p>
     * This returns a value of some kind derived from the specified bean.
     * This might be a property value or some other derived value.
     * 
     * @param bean  the bean to query, not null
     * @return the value derived from the specified bean, may be null
     * @throws ClassCastException if the bean is of an incorrect type
     * @throws UnsupportedOperationException if unable to query
     */
    P get(Bean bean);

}
