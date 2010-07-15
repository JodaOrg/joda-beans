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
package org.joda.beans.impl.direct;

import java.util.NoSuchElementException;

import org.joda.beans.impl.BasicBean;

/**
 * A base bean implementation used by the code generator.
 * <p>
 * This implementation is used to avoid reflection.
 * The bean must directly extend this class and have a no-arguments constructor
 * of at least package scope.
 * 
 * @author Stephen Colebourne
 */
public abstract class DirectBean extends BasicBean {

    /**
     * Gets the value of the property.
     * 
     * @param propertyName  the property name, not null
     * @return the value of the property, may be null
     * @throws NoSuchElementException if the property name is invalid
     */
    protected Object propertyGet(String propertyName) {
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    /**
     * Sets the value of the property.
     * 
     * @param propertyName  the property name, not null
     * @param value  the value of the property, may be null
     * @throws NoSuchElementException if the property name is invalid
     */
    protected void propertySet(String propertyName, Object value) {
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

}
