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
package org.joda.beans.impl.direct;

import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.test.JodaBeanTests;

/**
 * A meta-bean implementation designed for use by the code generator.
 * 
 * @author Stephen Colebourne
 */
public abstract class DirectMetaBean implements MetaBean {
    // overriding other methods has negligible effect considering DirectMetaPropertyMap

    /**
     * This constant can be used to pass into {@code setString()} to increase test coverage.
     */
    public static final String TEST_COVERAGE_STRING = "!ConstantUsedForTestCoveragePurposes!";

    @Override
    public boolean isBuildable() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> MetaProperty<R> metaProperty(String propertyName) {
        MetaProperty<?> mp = metaPropertyGet(propertyName);
        if (mp == null) {
            return metaPropertyNotFound(propertyName);
        }
        return (MetaProperty<R>) mp;
    }

    @SuppressWarnings("unchecked")
    private <R> MetaProperty<R> metaPropertyNotFound(String propertyName) {
        if (propertyName == JodaBeanTests.TEST_COVERAGE_PROPERTY) {
            // cast is unsafe unless R is String, but code only used in test coverage scenarios
            return (MetaProperty<R>) StandaloneMetaProperty.of(propertyName, this, String.class);
        }
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    /**
     * Gets the meta-property by name.
     * <p>
     * This implementation returns null, and must be overridden in subclasses.
     * 
     * @param propertyName  the property name, not null
     * @return the meta-property, null if not found
     */
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
        return null;
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the value of the property.
     * 
     * @param bean  the bean to query, not null
     * @param propertyName  the property name, not null
     * @param quiet  true to return null if unable to read
     * @return the value of the property, may be null
     * @throws NoSuchElementException if the property name is invalid
     */
    protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
        // used to enable 100% test coverage in beans
        if (quiet) {
            return null;
        }
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    /**
     * Sets the value of the property.
     * 
     * @param bean  the bean to update, not null
     * @param propertyName  the property name, not null
     * @param value  the value of the property, may be null
     * @param quiet  true to take no action if unable to write
     * @throws NoSuchElementException if the property name is invalid
     */
    protected void propertySet(Bean bean, String propertyName, Object value, boolean quiet) {
        // used to enable 100% test coverage in beans
        if (quiet) {
            return;
        }
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    /**
     * Validates the values of the properties.
     * 
     * @param bean  the bean to validate, not null
     * @throws RuntimeException if a property is invalid
     */
    protected void validate(Bean bean) {
    }

    /**
     * Returns a string that summarises the meta-bean.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return "MetaBean:" + beanType().getSimpleName();
    }

}
