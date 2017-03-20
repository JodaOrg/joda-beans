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
package org.joda.beans.gen;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.ImmutableBean;
import org.joda.beans.ImmutableConstructor;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock JavaBean, used for testing.
 * This tests a generic ? property with a manual argument constructor.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public final class ImmDocumentationHolder<T>
        implements ImmutableBean, Serializable {

    /** Serialization. */
    private static final long serialVersionUID = 1L;

    /** The documentation. */
    @PropertyDefinition
    private final Documentation<T> documentation;

    @ImmutableConstructor
    private ImmDocumentationHolder(
            Documentation<T> documentation) {
        this.documentation = documentation;
    }

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code ImmDocumentationHolder}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static ImmDocumentationHolder.Meta meta() {
        return ImmDocumentationHolder.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code ImmDocumentationHolder}.
     * @param <R>  the bean's generic type
     * @param cls  the bean's generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R> ImmDocumentationHolder.Meta<R> metaImmDocumentationHolder(Class<R> cls) {
        return ImmDocumentationHolder.Meta.INSTANCE;
    }

    static {
        JodaBeanUtils.registerMetaBean(ImmDocumentationHolder.Meta.INSTANCE);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @param <T>  the type
     * @return the builder, not null
     */
    public static <T> ImmDocumentationHolder.Builder<T> builder() {
        return new ImmDocumentationHolder.Builder<>();
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmDocumentationHolder.Meta<T> metaBean() {
        return ImmDocumentationHolder.Meta.INSTANCE;
    }

    @Override
    public <R> Property<R> property(String propertyName) {
        return metaBean().<R>metaProperty(propertyName).createProperty(this);
    }

    @Override
    public Set<String> propertyNames() {
        return metaBean().metaPropertyMap().keySet();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the documentation.
     * @return the value of the property
     */
    public Documentation<T> getDocumentation() {
        return documentation;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a builder that allows this bean to be mutated.
     * @return the mutable builder, not null
     */
    public Builder<T> toBuilder() {
        return new Builder<>(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            ImmDocumentationHolder<?> other = (ImmDocumentationHolder<?>) obj;
            return JodaBeanUtils.equal(documentation, other.documentation);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(documentation);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);
        buf.append("ImmDocumentationHolder{");
        buf.append("documentation").append('=').append(JodaBeanUtils.toString(documentation));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code ImmDocumentationHolder}.
     * @param <T>  the type
     */
    public static final class Meta<T> extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code documentation} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<Documentation<T>> documentation = DirectMetaProperty.ofImmutable(
                this, "documentation", ImmDocumentationHolder.class, (Class) Documentation.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "documentation");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 1587405498:  // documentation
                    return documentation;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public ImmDocumentationHolder.Builder<T> builder() {
            return new ImmDocumentationHolder.Builder<>();
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends ImmDocumentationHolder<T>> beanType() {
            return (Class) ImmDocumentationHolder.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code documentation} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Documentation<T>> documentation() {
            return documentation;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 1587405498:  // documentation
                    return ((ImmDocumentationHolder<?>) bean).getDocumentation();
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
     * The bean-builder for {@code ImmDocumentationHolder}.
     * @param <T>  the type
     */
    public static final class Builder<T> extends DirectFieldsBeanBuilder<ImmDocumentationHolder<T>> {

        private Documentation<T> documentation;

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        private Builder(ImmDocumentationHolder<T> beanToCopy) {
            this.documentation = beanToCopy.getDocumentation();
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case 1587405498:  // documentation
                    return documentation;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder<T> set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case 1587405498:  // documentation
                    this.documentation = (Documentation<T>) newValue;
                    break;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
            return this;
        }

        @Override
        public Builder<T> set(MetaProperty<?> property, Object value) {
            super.set(property, value);
            return this;
        }

        /**
         * @deprecated Use Joda-Convert in application code
         */
        @Override
        @Deprecated
        public Builder<T> setString(String propertyName, String value) {
            setString(meta().metaProperty(propertyName), value);
            return this;
        }

        /**
         * @deprecated Use Joda-Convert in application code
         */
        @Override
        @Deprecated
        public Builder<T> setString(MetaProperty<?> property, String value) {
            super.setString(property, value);
            return this;
        }

        /**
         * @deprecated Loop in application code
         */
        @Override
        @Deprecated
        public Builder<T> setAll(Map<String, ? extends Object> propertyValueMap) {
            super.setAll(propertyValueMap);
            return this;
        }

        @Override
        public ImmDocumentationHolder<T> build() {
            return new ImmDocumentationHolder<>(
                    documentation);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets the documentation.
         * @param documentation  the new value
         * @return this, for chaining, not null
         */
        public Builder<T> documentation(Documentation<T> documentation) {
            this.documentation = documentation;
            return this;
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(64);
            buf.append("ImmDocumentationHolder.Builder{");
            buf.append("documentation").append('=').append(JodaBeanUtils.toString(documentation));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
