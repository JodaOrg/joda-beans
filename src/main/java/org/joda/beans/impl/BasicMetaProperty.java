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
package org.joda.beans.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;

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
    public Property<P> createProperty(Bean bean) {
        return BasicProperty.of(bean, this);
    }

    @Override
    public String name() {
        return name;
    }

    //-----------------------------------------------------------------------
    @Override
    public P put(Bean bean, P value) {
        P old = get(bean);
        set(bean, value);
        return old;
    }

    //-----------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public <A extends Annotation> A annotation(Class<A> annotationClass) {
        List<Annotation> annotations = annotations();
        for (Annotation annotation : annotations) {
            if (annotationClass.isInstance(annotation)) {
                return (A) annotation;
            }
        }
        throw new NoSuchElementException("Unknown annotation: " + annotationClass.getName());
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MetaProperty<?>) {
            MetaProperty<?> other = (MetaProperty<?>) obj;
            return name().equals(other.name());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    /**
     * Returns a string that summarises the meta-property.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return name;
    }

}
