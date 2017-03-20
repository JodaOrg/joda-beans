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
package org.joda.beans.impl;

import org.joda.beans.MetaProperty;

/**
 * An abstract base meta-property.
 * 
 * @param <P>  the type of the property content
 * @author Stephen Colebourne
 */
public abstract class BasicMetaProperty<P> implements MetaProperty<P> {

    /** The name of the property. */
    private final String name;

    /**
     * Constructor.
     * 
     * @param propertyName  the property name, not empty
     */
    protected BasicMetaProperty(String propertyName) {
        if (propertyName == null || propertyName.length() == 0) {
            throw new NullPointerException("Property name must not be null or empty");
        }
        this.name = propertyName;
    }

    //-----------------------------------------------------------------------
    @Override
    public String name() {
        return name;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaProperty<?>) {
            MetaProperty<?> other = (MetaProperty<?>) obj;
            return name().equals(other.name()) && declaringType().equals(other.declaringType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name().hashCode() ^ declaringType().hashCode();
    }

    /**
     * Returns a string that summarises the meta-property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return declaringType().getSimpleName() + ":" + name();
    }

}
