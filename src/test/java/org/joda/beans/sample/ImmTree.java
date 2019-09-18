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

import java.util.List;
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

import com.google.common.collect.ImmutableList;

/**
 * Mock immutable tree node.
 */
@BeanDefinition(cacheHashCode = true, factoryName = "of")
public final class ImmTree<T> implements ImmutableBean{

    @PropertyDefinition(validate = "notNull")
    private final String name;
    @PropertyDefinition(validate = "notNull")
    private final T value;
    @PropertyDefinition(validate = "notNull")
    private final List<ImmTree<T>> childList;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code ImmTree}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static ImmTree.Meta meta() {
        return ImmTree.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code ImmTree}.
     * @param <R>  the bean's generic type
     * @param cls  the bean's generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R> ImmTree.Meta<R> metaImmTree(Class<R> cls) {
        return ImmTree.Meta.INSTANCE;
    }

    static {
        MetaBean.register(ImmTree.Meta.INSTANCE);
    }

    /**
     * The cached hash code, using the racy single-check idiom.
     */
    private transient int cacheHashCode;

    /**
     * Obtains an instance.
     * @param <T>  the type
     * @param name  the value of the property, not null
     * @param value  the value of the property, not null
     * @param childList  the value of the property, not null
     * @return the instance
     */
    public static <T> ImmTree<T> of(
            String name,
            T value,
            List<ImmTree<T>> childList) {
        return new ImmTree<>(
            name,
            value,
            childList);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @param <T>  the type
     * @return the builder, not null
     */
    public static <T> ImmTree.Builder<T> builder() {
        return new ImmTree.Builder<>();
    }

    private ImmTree(
            String name,
            T value,
            List<ImmTree<T>> childList) {
        JodaBeanUtils.notNull(name, "name");
        JodaBeanUtils.notNull(value, "value");
        JodaBeanUtils.notNull(childList, "childList");
        this.name = name;
        this.value = value;
        this.childList = ImmutableList.copyOf(childList);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ImmTree.Meta<T> metaBean() {
        return ImmTree.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the name.
     * @return the value of the property, not null
     */
    public String getName() {
        return name;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the value.
     * @return the value of the property, not null
     */
    public T getValue() {
        return value;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the childList.
     * @return the value of the property, not null
     */
    public List<ImmTree<T>> getChildList() {
        return childList;
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
            ImmTree<?> other = (ImmTree<?>) obj;
            return JodaBeanUtils.equal(name, other.name) &&
                    JodaBeanUtils.equal(value, other.value) &&
                    JodaBeanUtils.equal(childList, other.childList);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = cacheHashCode;
        if (hash == 0) {
            hash = getClass().hashCode();
            hash = hash * 31 + JodaBeanUtils.hashCode(name);
            hash = hash * 31 + JodaBeanUtils.hashCode(value);
            hash = hash * 31 + JodaBeanUtils.hashCode(childList);
            cacheHashCode = hash;
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("ImmTree{");
        buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
        buf.append("value").append('=').append(JodaBeanUtils.toString(value)).append(',').append(' ');
        buf.append("childList").append('=').append(JodaBeanUtils.toString(childList));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code ImmTree}.
     * @param <T>  the type
     */
    public static final class Meta<T> extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code name} property.
         */
        private final MetaProperty<String> name = DirectMetaProperty.ofImmutable(
                this, "name", ImmTree.class, String.class);
        /**
         * The meta-property for the {@code value} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<T> value = (DirectMetaProperty) DirectMetaProperty.ofImmutable(
                this, "value", ImmTree.class, Object.class);
        /**
         * The meta-property for the {@code childList} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<List<ImmTree<T>>> childList = DirectMetaProperty.ofImmutable(
                this, "childList", ImmTree.class, (Class) List.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "name",
                "value",
                "childList");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 3373707:  // name
                    return name;
                case 111972721:  // value
                    return value;
                case -95409190:  // childList
                    return childList;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public ImmTree.Builder<T> builder() {
            return new ImmTree.Builder<>();
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends ImmTree<T>> beanType() {
            return (Class) ImmTree.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code name} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> name() {
            return name;
        }

        /**
         * The meta-property for the {@code value} property.
         * @return the meta-property, not null
         */
        public MetaProperty<T> value() {
            return value;
        }

        /**
         * The meta-property for the {@code childList} property.
         * @return the meta-property, not null
         */
        public MetaProperty<List<ImmTree<T>>> childList() {
            return childList;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 3373707:  // name
                    return ((ImmTree<?>) bean).getName();
                case 111972721:  // value
                    return ((ImmTree<?>) bean).getValue();
                case -95409190:  // childList
                    return ((ImmTree<?>) bean).getChildList();
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
     * The bean-builder for {@code ImmTree}.
     * @param <T>  the type
     */
    public static final class Builder<T> extends DirectFieldsBeanBuilder<ImmTree<T>> {

        private String name;
        private T value;
        private List<ImmTree<T>> childList = ImmutableList.of();

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        private Builder(ImmTree<T> beanToCopy) {
            this.name = beanToCopy.getName();
            this.value = beanToCopy.getValue();
            this.childList = ImmutableList.copyOf(beanToCopy.getChildList());
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case 3373707:  // name
                    return name;
                case 111972721:  // value
                    return value;
                case -95409190:  // childList
                    return childList;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder<T> set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case 3373707:  // name
                    this.name = (String) newValue;
                    break;
                case 111972721:  // value
                    this.value = (T) newValue;
                    break;
                case -95409190:  // childList
                    this.childList = (List<ImmTree<T>>) newValue;
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

        @Override
        public ImmTree<T> build() {
            return new ImmTree<>(
                    name,
                    value,
                    childList);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets the name.
         * @param name  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder<T> name(String name) {
            JodaBeanUtils.notNull(name, "name");
            this.name = name;
            return this;
        }

        /**
         * Sets the value.
         * @param value  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder<T> value(T value) {
            JodaBeanUtils.notNull(value, "value");
            this.value = value;
            return this;
        }

        /**
         * Sets the childList.
         * @param childList  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder<T> childList(List<ImmTree<T>> childList) {
            JodaBeanUtils.notNull(childList, "childList");
            this.childList = childList;
            return this;
        }

        /**
         * Sets the {@code childList} property in the builder
         * from an array of objects.
         * @param childList  the new value, not null
         * @return this, for chaining, not null
         */
        @SafeVarargs
        public final Builder<T> childList(ImmTree<T>... childList) {
            return childList(ImmutableList.copyOf(childList));
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(128);
            buf.append("ImmTree.Builder{");
            buf.append("name").append('=').append(JodaBeanUtils.toString(name)).append(',').append(' ');
            buf.append("value").append('=').append(JodaBeanUtils.toString(value)).append(',').append(' ');
            buf.append("childList").append('=').append(JodaBeanUtils.toString(childList));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
