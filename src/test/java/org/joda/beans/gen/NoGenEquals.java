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
package org.joda.beans.gen;

import java.util.Map;

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.BeanBuilder;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock used for test equals.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class NoGenEquals extends DirectBean {

    /**
     * The value.
     */
    @PropertyDefinition
    private Object value;

    @Override
    public boolean equals(Object obj) {
        // should block equals generation
        return obj == this;
    }

    //------------------------- AUTOGENERATED START -------------------------
    ///CLOVER:OFF
    /**
     * The meta-bean for {@code NoGenEquals}.
     * @return the meta-bean, not null
     */
    public static NoGenEquals.Meta meta() {
        return NoGenEquals.Meta.INSTANCE;
    }
    static {
        JodaBeanUtils.registerMetaBean(NoGenEquals.Meta.INSTANCE);
    }

    @Override
    public NoGenEquals.Meta metaBean() {
        return NoGenEquals.Meta.INSTANCE;
    }

    @Override
    protected Object propertyGet(String propertyName, boolean quiet) {
        switch (propertyName.hashCode()) {
            case 111972721:  // value
                return getValue();
        }
        return super.propertyGet(propertyName, quiet);
    }

    @Override
    protected void propertySet(String propertyName, Object newValue, boolean quiet) {
        switch (propertyName.hashCode()) {
            case 111972721:  // value
                setValue((Object) newValue);
                return;
        }
        super.propertySet(propertyName, newValue, quiet);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value.
     * @return the value of the property
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value.
     * @param value  the new value of the property
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the the {@code value} property.
     * @return the property, not null
     */
    public final Property<Object> value() {
        return metaBean().value().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code NoGenEquals}.
     */
    public static class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code value} property.
         */
        private final MetaProperty<Object> value = DirectMetaProperty.ofReadWrite(
                this, "value", NoGenEquals.class, Object.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "value");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 111972721:  // value
                    return value;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends NoGenEquals> builder() {
            return new DirectBeanBuilder<NoGenEquals>(new NoGenEquals());
        }

        @Override
        public Class<? extends NoGenEquals> beanType() {
            return NoGenEquals.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code value} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Object> value() {
            return value;
        }

    }

    ///CLOVER:ON
    //-------------------------- AUTOGENERATED END --------------------------
}