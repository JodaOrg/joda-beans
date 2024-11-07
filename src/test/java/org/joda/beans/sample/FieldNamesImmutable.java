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
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock JavaBean, used for testing.
 */
@BeanDefinition(builderScope = "public", factoryName = "of")
public final class FieldNamesImmutable implements ImmutableBean {

    /** Field named 'obj' to check for name clashes. */
    @PropertyDefinition
    private final String obj;
    /** Field named 'other' to check for name clashes. */
    @PropertyDefinition
    private final String other;
    /** Field named 'propertyName' to check for name clashes. */
    @PropertyDefinition
    private final String propertyName;
    /** Field named 'newValue' to check for name clashes. */
    @PropertyDefinition
    private final String newValue;
    /** Field named 'bean' to check for name clashes. */
    @PropertyDefinition
    private final String bean;
    /** Field named 'beanToCopy' to check for name clashes. */
    @PropertyDefinition
    private final String beanToCopy;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code FieldNamesImmutable}.
     * @return the meta-bean, not null
     */
    public static FieldNamesImmutable.Meta meta() {
        return FieldNamesImmutable.Meta.INSTANCE;
    }

    static {
        MetaBean.register(FieldNamesImmutable.Meta.INSTANCE);
    }

    /**
     * Obtains an instance.
     * @param obj  the value of the property
     * @param other  the value of the property
     * @param propertyName  the value of the property
     * @param newValue  the value of the property
     * @param bean  the value of the property
     * @param beanToCopy  the value of the property
     * @return the instance
     */
    public static FieldNamesImmutable of(
            String obj,
            String other,
            String propertyName,
            String newValue,
            String bean,
            String beanToCopy) {
        return new FieldNamesImmutable(
            obj,
            other,
            propertyName,
            newValue,
            bean,
            beanToCopy);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @return the builder, not null
     */
    public static FieldNamesImmutable.Builder builder() {
        return new FieldNamesImmutable.Builder();
    }

    private FieldNamesImmutable(
            String obj,
            String other,
            String propertyName,
            String newValue,
            String bean,
            String beanToCopy) {
        this.obj = obj;
        this.other = other;
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.bean = bean;
        this.beanToCopy = beanToCopy;
    }

    @Override
    public FieldNamesImmutable.Meta metaBean() {
        return FieldNamesImmutable.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'obj' to check for name clashes.
     * @return the value of the property
     */
    public String getObj() {
        return obj;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'other' to check for name clashes.
     * @return the value of the property
     */
    public String getOther() {
        return other;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'propertyName' to check for name clashes.
     * @return the value of the property
     */
    public String getPropertyName() {
        return propertyName;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'newValue' to check for name clashes.
     * @return the value of the property
     */
    public String getNewValue() {
        return newValue;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'bean' to check for name clashes.
     * @return the value of the property
     */
    public String getBean() {
        return bean;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets field named 'beanToCopy' to check for name clashes.
     * @return the value of the property
     */
    public String getBeanToCopy() {
        return beanToCopy;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a builder that allows this bean to be mutated.
     * @return the mutable builder, not null
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            FieldNamesImmutable other = (FieldNamesImmutable) obj;
            return JodaBeanUtils.equal(this.obj, other.obj) &&
                    JodaBeanUtils.equal(this.other, other.other) &&
                    JodaBeanUtils.equal(propertyName, other.propertyName) &&
                    JodaBeanUtils.equal(newValue, other.newValue) &&
                    JodaBeanUtils.equal(bean, other.bean) &&
                    JodaBeanUtils.equal(beanToCopy, other.beanToCopy);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(obj);
        hash = hash * 31 + JodaBeanUtils.hashCode(other);
        hash = hash * 31 + JodaBeanUtils.hashCode(propertyName);
        hash = hash * 31 + JodaBeanUtils.hashCode(newValue);
        hash = hash * 31 + JodaBeanUtils.hashCode(bean);
        hash = hash * 31 + JodaBeanUtils.hashCode(beanToCopy);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(224);
        buf.append("FieldNamesImmutable{");
        buf.append("obj").append('=').append(JodaBeanUtils.toString(obj)).append(',').append(' ');
        buf.append("other").append('=').append(JodaBeanUtils.toString(other)).append(',').append(' ');
        buf.append("propertyName").append('=').append(JodaBeanUtils.toString(propertyName)).append(',').append(' ');
        buf.append("newValue").append('=').append(JodaBeanUtils.toString(newValue)).append(',').append(' ');
        buf.append("bean").append('=').append(JodaBeanUtils.toString(bean)).append(',').append(' ');
        buf.append("beanToCopy").append('=').append(JodaBeanUtils.toString(beanToCopy));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code FieldNamesImmutable}.
     */
    public static final class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code obj} property.
         */
        private final MetaProperty<String> obj = DirectMetaProperty.ofImmutable(
                this, "obj", FieldNamesImmutable.class, String.class);
        /**
         * The meta-property for the {@code other} property.
         */
        private final MetaProperty<String> other = DirectMetaProperty.ofImmutable(
                this, "other", FieldNamesImmutable.class, String.class);
        /**
         * The meta-property for the {@code propertyName} property.
         */
        private final MetaProperty<String> propertyName = DirectMetaProperty.ofImmutable(
                this, "propertyName", FieldNamesImmutable.class, String.class);
        /**
         * The meta-property for the {@code newValue} property.
         */
        private final MetaProperty<String> newValue = DirectMetaProperty.ofImmutable(
                this, "newValue", FieldNamesImmutable.class, String.class);
        /**
         * The meta-property for the {@code bean} property.
         */
        private final MetaProperty<String> bean = DirectMetaProperty.ofImmutable(
                this, "bean", FieldNamesImmutable.class, String.class);
        /**
         * The meta-property for the {@code beanToCopy} property.
         */
        private final MetaProperty<String> beanToCopy = DirectMetaProperty.ofImmutable(
                this, "beanToCopy", FieldNamesImmutable.class, String.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "obj",
                "other",
                "propertyName",
                "newValue",
                "bean",
                "beanToCopy");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    return obj;
                case 106069776:  // other
                    return other;
                case -864691712:  // propertyName
                    return this.propertyName;
                case 1368456113:  // newValue
                    return newValue;
                case 3019696:  // bean
                    return bean;
                case -1343227808:  // beanToCopy
                    return beanToCopy;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public FieldNamesImmutable.Builder builder() {
            return new FieldNamesImmutable.Builder();
        }

        @Override
        public Class<? extends FieldNamesImmutable> beanType() {
            return FieldNamesImmutable.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code obj} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> obj() {
            return obj;
        }

        /**
         * The meta-property for the {@code other} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> other() {
            return other;
        }

        /**
         * The meta-property for the {@code propertyName} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> propertyName() {
            return propertyName;
        }

        /**
         * The meta-property for the {@code newValue} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> newValue() {
            return newValue;
        }

        /**
         * The meta-property for the {@code bean} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> bean() {
            return bean;
        }

        /**
         * The meta-property for the {@code beanToCopy} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> beanToCopy() {
            return beanToCopy;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    return ((FieldNamesImmutable) bean).getObj();
                case 106069776:  // other
                    return ((FieldNamesImmutable) bean).getOther();
                case -864691712:  // propertyName
                    return ((FieldNamesImmutable) bean).getPropertyName();
                case 1368456113:  // newValue
                    return ((FieldNamesImmutable) bean).getNewValue();
                case 3019696:  // bean
                    return ((FieldNamesImmutable) bean).getBean();
                case -1343227808:  // beanToCopy
                    return ((FieldNamesImmutable) bean).getBeanToCopy();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            metaProperty(propertyName);
            if (quiet) {
                return;
            }
            throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
        }

    }

    //-----------------------------------------------------------------------
    /**
     * The bean-builder for {@code FieldNamesImmutable}.
     */
    public static final class Builder extends DirectFieldsBeanBuilder<FieldNamesImmutable> {

        private String obj;
        private String other;
        private String propertyName;
        private String newValue;
        private String bean;
        private String beanToCopy;

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        private Builder(FieldNamesImmutable beanToCopy) {
            this.obj = beanToCopy.getObj();
            this.other = beanToCopy.getOther();
            this.propertyName = beanToCopy.getPropertyName();
            this.newValue = beanToCopy.getNewValue();
            this.bean = beanToCopy.getBean();
            this.beanToCopy = beanToCopy.getBeanToCopy();
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    return obj;
                case 106069776:  // other
                    return other;
                case -864691712:  // propertyName
                    return this.propertyName;
                case 1368456113:  // newValue
                    return newValue;
                case 3019696:  // bean
                    return bean;
                case -1343227808:  // beanToCopy
                    return beanToCopy;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @Override
        public Builder set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    this.obj = (String) newValue;
                    break;
                case 106069776:  // other
                    this.other = (String) newValue;
                    break;
                case -864691712:  // propertyName
                    this.propertyName = (String) newValue;
                    break;
                case 1368456113:  // newValue
                    this.newValue = (String) newValue;
                    break;
                case 3019696:  // bean
                    this.bean = (String) newValue;
                    break;
                case -1343227808:  // beanToCopy
                    this.beanToCopy = (String) newValue;
                    break;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
            return this;
        }

        @Override
        public Builder set(MetaProperty<?> property, Object value) {
            super.set(property, value);
            return this;
        }

        @Override
        public FieldNamesImmutable build() {
            return new FieldNamesImmutable(
                    obj,
                    other,
                    propertyName,
                    newValue,
                    bean,
                    beanToCopy);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets field named 'obj' to check for name clashes.
         * @param obj  the new value
         * @return this, for chaining, not null
         */
        public Builder obj(String obj) {
            this.obj = obj;
            return this;
        }

        /**
         * Sets field named 'other' to check for name clashes.
         * @param other  the new value
         * @return this, for chaining, not null
         */
        public Builder other(String other) {
            this.other = other;
            return this;
        }

        /**
         * Sets field named 'propertyName' to check for name clashes.
         * @param propertyName  the new value
         * @return this, for chaining, not null
         */
        public Builder propertyName(String propertyName) {
            this.propertyName = propertyName;
            return this;
        }

        /**
         * Sets field named 'newValue' to check for name clashes.
         * @param newValue  the new value
         * @return this, for chaining, not null
         */
        public Builder newValue(String newValue) {
            this.newValue = newValue;
            return this;
        }

        /**
         * Sets field named 'bean' to check for name clashes.
         * @param bean  the new value
         * @return this, for chaining, not null
         */
        public Builder bean(String bean) {
            this.bean = bean;
            return this;
        }

        /**
         * Sets field named 'beanToCopy' to check for name clashes.
         * @param beanToCopy  the new value
         * @return this, for chaining, not null
         */
        public Builder beanToCopy(String beanToCopy) {
            this.beanToCopy = beanToCopy;
            return this;
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(224);
            buf.append("FieldNamesImmutable.Builder{");
            buf.append("obj").append('=').append(JodaBeanUtils.toString(obj)).append(',').append(' ');
            buf.append("other").append('=').append(JodaBeanUtils.toString(other)).append(',').append(' ');
            buf.append("propertyName").append('=').append(JodaBeanUtils.toString(propertyName)).append(',').append(' ');
            buf.append("newValue").append('=').append(JodaBeanUtils.toString(newValue)).append(',').append(' ');
            buf.append("bean").append('=').append(JodaBeanUtils.toString(bean)).append(',').append(' ');
            buf.append("beanToCopy").append('=').append(JodaBeanUtils.toString(beanToCopy));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
