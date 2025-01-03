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
 * Mock bean for tolerance testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition(cacheHashCode = true, factoryName = "create")
public final class ImmTolerance implements ImmutableBean {

    @PropertyDefinition
    private final double value;
    @PropertyDefinition
    private final double[] array;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code ImmTolerance}.
     * @return the meta-bean, not null
     */
    public static ImmTolerance.Meta meta() {
        return ImmTolerance.Meta.INSTANCE;
    }

    static {
        MetaBean.register(ImmTolerance.Meta.INSTANCE);
    }

    /**
     * The cached hash code, using the racy single-check idiom.
     */
    private transient int cacheHashCode;

    /**
     * Obtains an instance.
     * @param value  the value of the property
     * @param array  the value of the property
     * @return the instance
     */
    public static ImmTolerance create(
            double value,
            double[] array) {
        return new ImmTolerance(
            value,
            array);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @return the builder, not null
     */
    public static ImmTolerance.Builder builder() {
        return new ImmTolerance.Builder();
    }

    private ImmTolerance(
            double value,
            double[] array) {
        this.value = value;
        this.array = (array != null ? array.clone() : null);
    }

    @Override
    public ImmTolerance.Meta metaBean() {
        return ImmTolerance.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value.
     * @return the value of the property
     */
    public double getValue() {
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the array.
     * @return the value of the property
     */
    public double[] getArray() {
        return (array != null ? array.clone() : null);
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
            ImmTolerance other = (ImmTolerance) obj;
            return JodaBeanUtils.equal(this.value, other.value) &&
                    JodaBeanUtils.equal(this.array, other.array);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = cacheHashCode;
        if (hash == 0) {
            hash = getClass().hashCode();
            hash = hash * 31 + JodaBeanUtils.hashCode(value);
            hash = hash * 31 + JodaBeanUtils.hashCode(array);
            cacheHashCode = hash;
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(96);
        buf.append("ImmTolerance{");
        buf.append("value").append('=').append(JodaBeanUtils.toString(value)).append(',').append(' ');
        buf.append("array").append('=').append(JodaBeanUtils.toString(array));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code ImmTolerance}.
     */
    public static final class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code value} property.
         */
        private final MetaProperty<Double> value = DirectMetaProperty.ofImmutable(
                this, "value", ImmTolerance.class, Double.TYPE);
        /**
         * The meta-property for the {@code array} property.
         */
        private final MetaProperty<double[]> array = DirectMetaProperty.ofImmutable(
                this, "array", ImmTolerance.class, double[].class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "value",
                "array");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 111972721:  // value
                    return this.value;
                case 93090393:  // array
                    return this.array;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public ImmTolerance.Builder builder() {
            return new ImmTolerance.Builder();
        }

        @Override
        public Class<? extends ImmTolerance> beanType() {
            return ImmTolerance.class;
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
        public MetaProperty<Double> value() {
            return value;
        }

        /**
         * The meta-property for the {@code array} property.
         * @return the meta-property, not null
         */
        public MetaProperty<double[]> array() {
            return array;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 111972721:  // value
                    return ((ImmTolerance) bean).getValue();
                case 93090393:  // array
                    return ((ImmTolerance) bean).getArray();
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
     * The bean-builder for {@code ImmTolerance}.
     */
    public static final class Builder extends DirectFieldsBeanBuilder<ImmTolerance> {

        private double value;
        private double[] array;

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        private Builder(ImmTolerance beanToCopy) {
            this.value = beanToCopy.getValue();
            this.array = (beanToCopy.getArray() != null ? beanToCopy.getArray().clone() : null);
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case 111972721:  // value
                    return this.value;
                case 93090393:  // array
                    return this.array;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @Override
        public Builder set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case 111972721:  // value
                    this.value = (Double) newValue;
                    break;
                case 93090393:  // array
                    this.array = (double[]) newValue;
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
        public ImmTolerance build() {
            return new ImmTolerance(
                    value,
                    array);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets the value.
         * @param value  the new value
         * @return this, for chaining, not null
         */
        public Builder value(double value) {
            this.value = value;
            return this;
        }

        /**
         * Sets the array.
         * @param array  the new value
         * @return this, for chaining, not null
         */
        public Builder array(double... array) {
            this.array = array;
            return this;
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(96);
            buf.append("ImmTolerance.Builder{");
            buf.append("value").append('=').append(JodaBeanUtils.toString(value)).append(',').append(' ');
            buf.append("array").append('=').append(JodaBeanUtils.toString(array));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
