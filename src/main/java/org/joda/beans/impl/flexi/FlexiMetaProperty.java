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
package org.joda.beans.impl.flexi;

import org.joda.beans.Bean;
import org.joda.beans.Property;
import org.joda.beans.PropertyReadWrite;
import org.joda.beans.impl.BasicMetaProperty;
import org.joda.beans.impl.BasicProperty;

/**
 * A meta-property using a {@code FlexiBean} for storage.
 * 
 * @author Stephen Colebourne
 */
class FlexiMetaProperty extends BasicMetaProperty<Object> {

    /**
     * Factory to create a meta-property.
     * 
     * @param propertyName  the property name, not empty
     */
    static FlexiMetaProperty of(String propertyName) {
        return new FlexiMetaProperty(propertyName);
    }

    /**
     * Constructor.
     * 
     * @param propertyName  the property name, not empty
     */
    private FlexiMetaProperty(String propertyName) {
        super(FlexiBean.class, propertyName);
    }

    //-----------------------------------------------------------------------
    @Override
    public Property<Object> createProperty(Bean bean) {
        return BasicProperty.of(bean, this);
    }

    @Override
    public Class<Object> propertyType() {
        return Object.class;
    }

    @Override
    public PropertyReadWrite readWrite() {
        return PropertyReadWrite.READ_WRITE;
    }

    //-----------------------------------------------------------------------
    @Override
    public Object get(Bean bean) {
        return bean.<FlexiBean>beanData().propertyGet(name());
    }

    @Override
    public void set(Bean bean, Object value) {
        bean.<FlexiBean>beanData().propertySet(name(), value);
    }

}
