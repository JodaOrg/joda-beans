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

/**
 * Mock JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class DoubleGenericsSimpleSuper<T, U>
        implements Bean {

    /** The normal type. */
    @PropertyDefinition
    private String baseType;
    /** The type T value. */
    @PropertyDefinition
    private T baseT;
    /** The type U value. */
    @PropertyDefinition
    private U baseU;

    /**
     * Creates an instance.
     */
    public DoubleGenericsSimpleSuper() {
    }

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code DoubleGenericsSimpleSuper}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static DoubleGenericsSimpleSuper.Meta meta() {
        return DoubleGenericsSimpleSuper.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code DoubleGenericsSimpleSuper}.
     * @param <R>  the first generic type
     * @param <S>  the second generic type
     * @param cls1  the first generic type
     * @param cls2  the second generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R, S> DoubleGenericsSimpleSuper.Meta<R, S> metaDoubleGenericsSimpleSuper(Class<R> cls1, Class<S> cls2) {
        return DoubleGenericsSimpleSuper.Meta.INSTANCE;
    }

    static {
        MetaBean.register(DoubleGenericsSimpleSuper.Meta.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DoubleGenericsSimpleSuper.Meta<T, U> metaBean() {
        return DoubleGenericsSimpleSuper.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the normal type.
     * @return the value of the property
     */
    public String getBaseType() {
        return baseType;
    }

    /**
     * Sets the normal type.
     * @param baseType  the new value of the property
     */
    public void setBaseType(String baseType) {
        this.baseType = baseType;
    }

    /**
     * Gets the the {@code baseType} property.
     * @return the property, not null
     */
    public final Property<String> baseType() {
        return metaBean().baseType().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type T value.
     * @return the value of the property
     */
    public T getBaseT() {
        return baseT;
    }

    /**
     * Sets the type T value.
     * @param baseT  the new value of the property
     */
    public void setBaseT(T baseT) {
        this.baseT = baseT;
    }

    /**
     * Gets the the {@code baseT} property.
     * @return the property, not null
     */
    public final Property<T> baseT() {
        return metaBean().baseT().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type U value.
     * @return the value of the property
     */
    public U getBaseU() {
        return baseU;
    }

    /**
     * Sets the type U value.
     * @param baseU  the new value of the property
     */
    public void setBaseU(U baseU) {
        this.baseU = baseU;
    }

    /**
     * Gets the the {@code baseU} property.
     * @return the property, not null
     */
    public final Property<U> baseU() {
        return metaBean().baseU().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public DoubleGenericsSimpleSuper<T, U> clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            DoubleGenericsSimpleSuper<?, ?> other = (DoubleGenericsSimpleSuper<?, ?>) obj;
            return JodaBeanUtils.equal(this.getBaseType(), other.getBaseType()) &&
                    JodaBeanUtils.equal(this.getBaseT(), other.getBaseT()) &&
                    JodaBeanUtils.equal(this.getBaseU(), other.getBaseU());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getBaseType());
        hash = hash * 31 + JodaBeanUtils.hashCode(getBaseT());
        hash = hash * 31 + JodaBeanUtils.hashCode(getBaseU());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("DoubleGenericsSimpleSuper{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    protected void toString(StringBuilder buf) {
        buf.append("baseType").append('=').append(JodaBeanUtils.toString(getBaseType())).append(',').append(' ');
        buf.append("baseT").append('=').append(JodaBeanUtils.toString(getBaseT())).append(',').append(' ');
        buf.append("baseU").append('=').append(JodaBeanUtils.toString(getBaseU())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code DoubleGenericsSimpleSuper}.
     * @param <T>  the type
     * @param <U>  the type
     */
    public static class Meta<T, U> extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code baseType} property.
         */
        private final MetaProperty<String> baseType = DirectMetaProperty.ofReadWrite(
                this, "baseType", DoubleGenericsSimpleSuper.class, String.class);
        /**
         * The meta-property for the {@code baseT} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<T> baseT = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "baseT", DoubleGenericsSimpleSuper.class, Object.class);
        /**
         * The meta-property for the {@code baseU} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<U> baseU = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "baseU", DoubleGenericsSimpleSuper.class, Object.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "baseType",
                "baseT",
                "baseU");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1721484885:  // baseType
                    return this.baseType;
                case 93508515:  // baseT
                    return this.baseT;
                case 93508516:  // baseU
                    return this.baseU;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends DoubleGenericsSimpleSuper<T, U>> builder() {
            return new DirectBeanBuilder<>(new DoubleGenericsSimpleSuper<T, U>());
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends DoubleGenericsSimpleSuper<T, U>> beanType() {
            return (Class) DoubleGenericsSimpleSuper.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code baseType} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> baseType() {
            return baseType;
        }

        /**
         * The meta-property for the {@code baseT} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<T> baseT() {
            return baseT;
        }

        /**
         * The meta-property for the {@code baseU} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<U> baseU() {
            return baseU;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1721484885:  // baseType
                    return ((DoubleGenericsSimpleSuper<?, ?>) bean).getBaseType();
                case 93508515:  // baseT
                    return ((DoubleGenericsSimpleSuper<?, ?>) bean).getBaseT();
                case 93508516:  // baseU
                    return ((DoubleGenericsSimpleSuper<?, ?>) bean).getBaseU();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1721484885:  // baseType
                    ((DoubleGenericsSimpleSuper<T, U>) bean).setBaseType((String) newValue);
                    return;
                case 93508515:  // baseT
                    ((DoubleGenericsSimpleSuper<T, U>) bean).setBaseT((T) newValue);
                    return;
                case 93508516:  // baseU
                    ((DoubleGenericsSimpleSuper<T, U>) bean).setBaseU((U) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
