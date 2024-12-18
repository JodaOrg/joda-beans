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

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.beans.impl.direct.DirectPrivateBeanBuilder;

/**
 * Mock JavaBean, used for testing.
 */
@BeanDefinition(builderScope = "private", factoryName = "of")
public final class ImmDoubleArray<T> implements Serializable, ImmutableBean {

    /** The double values. */
    @PropertyDefinition(validate = "notNull")
    private final double[] values;

    /** The second lot of values. */
    @PropertyDefinition(validate = "notNull")
    private final double[] values2;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code ImmDoubleArray}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static ImmDoubleArray.Meta meta() {
        return ImmDoubleArray.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code ImmDoubleArray}.
     * @param <R>  the bean's generic type
     * @param cls  the bean's generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R> ImmDoubleArray.Meta<R> metaImmDoubleArray(Class<R> cls) {
        return ImmDoubleArray.Meta.INSTANCE;
    }

    static {
        MetaBean.register(ImmDoubleArray.Meta.INSTANCE);
    }

    /**
     * The serialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Obtains an instance.
     * @param <T>  the type
     * @param values  the value of the property, not null
     * @param values2  the value of the property, not null
     * @return the instance
     */
    public static <T> ImmDoubleArray<T> of(
            double[] values,
            double[] values2) {
        return new ImmDoubleArray<>(
            values,
            values2);
    }

    private ImmDoubleArray(
            double[] values,
            double[] values2) {
        JodaBeanUtils.notNull(values, "values");
        JodaBeanUtils.notNull(values2, "values2");
        this.values = values.clone();
        this.values2 = values2.clone();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmDoubleArray.Meta<T> metaBean() {
        return ImmDoubleArray.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the double values.
     * @return the value of the property, not null
     */
    public double[] getValues() {
        return values.clone();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the second lot of values.
     * @return the value of the property, not null
     */
    public double[] getValues2() {
        return values2.clone();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            ImmDoubleArray<?> other = (ImmDoubleArray<?>) obj;
            return JodaBeanUtils.equal(this.values, other.values) &&
                    JodaBeanUtils.equal(this.values2, other.values2);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(values);
        hash = hash * 31 + JodaBeanUtils.hashCode(values2);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(96);
        buf.append("ImmDoubleArray{");
        buf.append("values").append('=').append(JodaBeanUtils.toString(values)).append(',').append(' ');
        buf.append("values2").append('=').append(JodaBeanUtils.toString(values2));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code ImmDoubleArray}.
     * @param <T>  the type
     */
    public static final class Meta<T> extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code values} property.
         */
        private final MetaProperty<double[]> values = DirectMetaProperty.ofImmutable(
                this, "values", ImmDoubleArray.class, double[].class);
        /**
         * The meta-property for the {@code values2} property.
         */
        private final MetaProperty<double[]> values2 = DirectMetaProperty.ofImmutable(
                this, "values2", ImmDoubleArray.class, double[].class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "values",
                "values2");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -823812830:  // values
                    return this.values;
                case 231606096:  // values2
                    return this.values2;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends ImmDoubleArray<T>> builder() {
            return new ImmDoubleArray.Builder<>();
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends ImmDoubleArray<T>> beanType() {
            return (Class) ImmDoubleArray.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code values} property.
         * @return the meta-property, not null
         */
        public MetaProperty<double[]> values() {
            return values;
        }

        /**
         * The meta-property for the {@code values2} property.
         * @return the meta-property, not null
         */
        public MetaProperty<double[]> values2() {
            return values2;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -823812830:  // values
                    return ((ImmDoubleArray<?>) bean).getValues();
                case 231606096:  // values2
                    return ((ImmDoubleArray<?>) bean).getValues2();
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
     * The bean-builder for {@code ImmDoubleArray}.
     * @param <T>  the type
     */
    private static final class Builder<T> extends DirectPrivateBeanBuilder<ImmDoubleArray<T>> {

        private double[] values;
        private double[] values2;

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case -823812830:  // values
                    return this.values;
                case 231606096:  // values2
                    return this.values2;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @Override
        public Builder<T> set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case -823812830:  // values
                    this.values = (double[]) newValue;
                    break;
                case 231606096:  // values2
                    this.values2 = (double[]) newValue;
                    break;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
            return this;
        }

        @Override
        public ImmDoubleArray<T> build() {
            return new ImmDoubleArray<>(
                    values,
                    values2);
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(96);
            buf.append("ImmDoubleArray.Builder{");
            buf.append("values").append('=').append(JodaBeanUtils.toString(values)).append(',').append(' ');
            buf.append("values2").append('=').append(JodaBeanUtils.toString(values2));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
