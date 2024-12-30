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
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock JavaBean, used for testing.
 */
@BeanDefinition(builderScope = "public")
public final class FieldNamesMutable implements Bean {

    /** Field named 'obj' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String obj;
    /** Field named 'other' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String other;
    /** Field named 'propertyName' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String propertyName;
    /** Field named 'newValue' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String newValue;
    /** Field named 'bean' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String bean;
    /** Field named 'beanToCopy' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String beanToCopy;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code FieldNamesMutable}.
     * @return the meta-bean, not null
     */
    public static FieldNamesMutable.Meta meta() {
        return FieldNamesMutable.Meta.INSTANCE;
    }

    static {
        MetaBean.register(FieldNamesMutable.Meta.INSTANCE);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @return the builder, not null
     */
    public static FieldNamesMutable.Builder builder() {
        return new FieldNamesMutable.Builder();
    }

    /**
     * Restricted constructor.
     * @param builder  the builder to copy from, not null
     */
    private FieldNamesMutable(FieldNamesMutable.Builder builder) {
        this.obj = builder.obj;
        this.other = builder.other;
        this.propertyName = builder.propertyName;
        this.newValue = builder.newValue;
        this.bean = builder.bean;
        this.beanToCopy = builder.beanToCopy;
    }

    @Override
    public FieldNamesMutable.Meta metaBean() {
        return FieldNamesMutable.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'obj' to check for name clashes.
     * @param obj  the new value of the property
     */
    public void setObj(String obj) {
        this.obj = obj;
    }

    /**
     * Gets the the {@code obj} property.
     * @return the property, not null
     */
    public Property<String> obj() {
        return metaBean().obj().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'other' to check for name clashes.
     * @param other  the new value of the property
     */
    public void setOther(String other) {
        this.other = other;
    }

    /**
     * Gets the the {@code other} property.
     * @return the property, not null
     */
    public Property<String> other() {
        return metaBean().other().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'propertyName' to check for name clashes.
     * @param propertyName  the new value of the property
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the the {@code propertyName} property.
     * @return the property, not null
     */
    public Property<String> propertyName() {
        return metaBean().propertyName().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'newValue' to check for name clashes.
     * @param newValue  the new value of the property
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    /**
     * Gets the the {@code newValue} property.
     * @return the property, not null
     */
    public Property<String> newValue() {
        return metaBean().newValue().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'bean' to check for name clashes.
     * @param bean  the new value of the property
     */
    public void setBean(String bean) {
        this.bean = bean;
    }

    /**
     * Gets the the {@code bean} property.
     * @return the property, not null
     */
    public Property<String> bean() {
        return metaBean().bean().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'beanToCopy' to check for name clashes.
     * @param beanToCopy  the new value of the property
     */
    public void setBeanToCopy(String beanToCopy) {
        this.beanToCopy = beanToCopy;
    }

    /**
     * Gets the the {@code beanToCopy} property.
     * @return the property, not null
     */
    public Property<String> beanToCopy() {
        return metaBean().beanToCopy().createProperty(this);
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
    public FieldNamesMutable clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            FieldNamesMutable other = (FieldNamesMutable) obj;
            return JodaBeanUtils.equal(this.obj, other.obj) &&
                    JodaBeanUtils.equal(this.other, other.other) &&
                    JodaBeanUtils.equal(this.propertyName, other.propertyName) &&
                    JodaBeanUtils.equal(this.newValue, other.newValue) &&
                    JodaBeanUtils.equal(this.bean, other.bean) &&
                    JodaBeanUtils.equal(this.beanToCopy, other.beanToCopy);
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
        buf.append("FieldNamesMutable{");
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
     * The meta-bean for {@code FieldNamesMutable}.
     */
    public static final class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code obj} property.
         */
        private final MetaProperty<String> obj = DirectMetaProperty.ofReadWrite(
                this, "obj", FieldNamesMutable.class, String.class);
        /**
         * The meta-property for the {@code other} property.
         */
        private final MetaProperty<String> other = DirectMetaProperty.ofReadWrite(
                this, "other", FieldNamesMutable.class, String.class);
        /**
         * The meta-property for the {@code propertyName} property.
         */
        private final MetaProperty<String> propertyName = DirectMetaProperty.ofReadWrite(
                this, "propertyName", FieldNamesMutable.class, String.class);
        /**
         * The meta-property for the {@code newValue} property.
         */
        private final MetaProperty<String> newValue = DirectMetaProperty.ofReadWrite(
                this, "newValue", FieldNamesMutable.class, String.class);
        /**
         * The meta-property for the {@code bean} property.
         */
        private final MetaProperty<String> bean = DirectMetaProperty.ofReadWrite(
                this, "bean", FieldNamesMutable.class, String.class);
        /**
         * The meta-property for the {@code beanToCopy} property.
         */
        private final MetaProperty<String> beanToCopy = DirectMetaProperty.ofReadWrite(
                this, "beanToCopy", FieldNamesMutable.class, String.class);
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
                    return this.obj;
                case 106069776:  // other
                    return this.other;
                case -864691712:  // propertyName
                    return this.propertyName;
                case 1368456113:  // newValue
                    return this.newValue;
                case 3019696:  // bean
                    return this.bean;
                case -1343227808:  // beanToCopy
                    return this.beanToCopy;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public FieldNamesMutable.Builder builder() {
            return new FieldNamesMutable.Builder();
        }

        @Override
        public Class<? extends FieldNamesMutable> beanType() {
            return FieldNamesMutable.class;
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
                    return ((FieldNamesMutable) bean).obj;
                case 106069776:  // other
                    return ((FieldNamesMutable) bean).other;
                case -864691712:  // propertyName
                    return ((FieldNamesMutable) bean).propertyName;
                case 1368456113:  // newValue
                    return ((FieldNamesMutable) bean).newValue;
                case 3019696:  // bean
                    return ((FieldNamesMutable) bean).bean;
                case -1343227808:  // beanToCopy
                    return ((FieldNamesMutable) bean).beanToCopy;
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    ((FieldNamesMutable) bean).setObj((String) newValue);
                    return;
                case 106069776:  // other
                    ((FieldNamesMutable) bean).setOther((String) newValue);
                    return;
                case -864691712:  // propertyName
                    ((FieldNamesMutable) bean).setPropertyName((String) newValue);
                    return;
                case 1368456113:  // newValue
                    ((FieldNamesMutable) bean).setNewValue((String) newValue);
                    return;
                case 3019696:  // bean
                    ((FieldNamesMutable) bean).setBean((String) newValue);
                    return;
                case -1343227808:  // beanToCopy
                    ((FieldNamesMutable) bean).setBeanToCopy((String) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-----------------------------------------------------------------------
    /**
     * The bean-builder for {@code FieldNamesMutable}.
     */
    public static final class Builder extends DirectFieldsBeanBuilder<FieldNamesMutable> {

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
        private Builder(FieldNamesMutable beanToCopy) {
            this.obj = beanToCopy.obj;
            this.other = beanToCopy.other;
            this.propertyName = beanToCopy.propertyName;
            this.newValue = beanToCopy.newValue;
            this.bean = beanToCopy.bean;
            this.beanToCopy = beanToCopy.beanToCopy;
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case 109815:  // obj
                    return this.obj;
                case 106069776:  // other
                    return this.other;
                case -864691712:  // propertyName
                    return this.propertyName;
                case 1368456113:  // newValue
                    return this.newValue;
                case 3019696:  // bean
                    return this.bean;
                case -1343227808:  // beanToCopy
                    return this.beanToCopy;
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
        public FieldNamesMutable build() {
            return new FieldNamesMutable(this);
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
            buf.append("FieldNamesMutable.Builder{");
            int len = buf.length();
            toString(buf);
            if (buf.length() > len) {
                buf.setLength(buf.length() - 2);
            }
            buf.append('}');
            return buf.toString();
        }

        protected void toString(StringBuilder buf) {
            buf.append("obj").append('=').append(JodaBeanUtils.toString(obj)).append(',').append(' ');
            buf.append("other").append('=').append(JodaBeanUtils.toString(other)).append(',').append(' ');
            buf.append("propertyName").append('=').append(JodaBeanUtils.toString(propertyName)).append(',').append(' ');
            buf.append("newValue").append('=').append(JodaBeanUtils.toString(newValue)).append(',').append(' ');
            buf.append("bean").append('=').append(JodaBeanUtils.toString(bean)).append(',').append(' ');
            buf.append("beanToCopy").append('=').append(JodaBeanUtils.toString(beanToCopy)).append(',').append(' ');
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
