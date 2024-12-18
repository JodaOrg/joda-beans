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
package org.joda.beans.sample;

import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.base.Optional;

/**
 * Mock.
 */
@BeanDefinition
public final class MutableOptional implements Bean {

    // this works but not in serialization
    @PropertyDefinition(validate = "notNull")
    private Optional<String> optString;
    // these are the recommended approach, nullable fields, optional getters
    // remove "Guava" to test Java 8 generation
    @PropertyDefinition(get = "optionalGuava")
    private String optStringGetter;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code MutableOptional}.
     * @return the meta-bean, not null
     */
    public static MutableOptional.Meta meta() {
        return MutableOptional.Meta.INSTANCE;
    }

    static {
        MetaBean.register(MutableOptional.Meta.INSTANCE);
    }

    @Override
    public MutableOptional.Meta metaBean() {
        return MutableOptional.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the optString.
     * @return the value of the property, not null
     */
    public Optional<String> getOptString() {
        return optString;
    }

    /**
     * Sets the optString.
     * @param optString  the new value of the property, not null
     */
    public void setOptString(Optional<String> optString) {
        JodaBeanUtils.notNull(optString, "optString");
        this.optString = optString;
    }

    /**
     * Gets the the {@code optString} property.
     * @return the property, not null
     */
    public Property<Optional<String>> optString() {
        return metaBean().optString().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the optStringGetter.
     * @return the optional value of the property, not null
     */
    public Optional<String> getOptStringGetter() {
        return Optional.fromNullable(optStringGetter);
    }

    /**
     * Sets the optStringGetter.
     * @param optStringGetter  the new value of the property
     */
    public void setOptStringGetter(String optStringGetter) {
        this.optStringGetter = optStringGetter;
    }

    /**
     * Gets the the {@code optStringGetter} property.
     * @return the property, not null
     */
    public Property<String> optStringGetter() {
        return metaBean().optStringGetter().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public MutableOptional clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            MutableOptional other = (MutableOptional) obj;
            return JodaBeanUtils.equal(this.getOptString(), other.getOptString()) &&
                    JodaBeanUtils.equal(this.optStringGetter, other.optStringGetter);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getOptString());
        hash = hash * 31 + JodaBeanUtils.hashCode(optStringGetter);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(96);
        buf.append("MutableOptional{");
        buf.append("optString").append('=').append(JodaBeanUtils.toString(getOptString())).append(',').append(' ');
        buf.append("optStringGetter").append('=').append(JodaBeanUtils.toString(optStringGetter));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code MutableOptional}.
     */
    public static final class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code optString} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<Optional<String>> optString = DirectMetaProperty.ofReadWrite(
                this, "optString", MutableOptional.class, (Class) Optional.class);
        /**
         * The meta-property for the {@code optStringGetter} property.
         */
        private final MetaProperty<String> optStringGetter = DirectMetaProperty.ofReadWrite(
                this, "optStringGetter", MutableOptional.class, String.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "optString",
                "optStringGetter");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 1220339876:  // optString
                    return this.optString;
                case -740642097:  // optStringGetter
                    return this.optStringGetter;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends MutableOptional> builder() {
            return new DirectBeanBuilder<>(new MutableOptional());
        }

        @Override
        public Class<? extends MutableOptional> beanType() {
            return MutableOptional.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code optString} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Optional<String>> optString() {
            return optString;
        }

        /**
         * The meta-property for the {@code optStringGetter} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> optStringGetter() {
            return optStringGetter;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 1220339876:  // optString
                    return ((MutableOptional) bean).getOptString();
                case -740642097:  // optStringGetter
                    return ((MutableOptional) bean).optStringGetter;
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 1220339876:  // optString
                    ((MutableOptional) bean).setOptString((Optional<String>) newValue);
                    return;
                case -740642097:  // optStringGetter
                    ((MutableOptional) bean).setOptStringGetter((String) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

        @Override
        protected void validate(Bean bean) {
            JodaBeanUtils.notNull(((MutableOptional) bean).optString, "optString");
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
