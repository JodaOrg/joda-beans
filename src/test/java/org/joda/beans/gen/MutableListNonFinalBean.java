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

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.BeanDefinition;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

/**
 * Mock JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition(builderScope = "public")
public class MutableListNonFinalBean implements Bean {

    @PropertyDefinition
    private List<String> strings;

    @PropertyDefinition
    private Map<String, Integer> numberMap;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code MutableListNonFinalBean}.
     * @return the meta-bean, not null
     */
    public static MutableListNonFinalBean.Meta meta() {
        return MutableListNonFinalBean.Meta.INSTANCE;
    }

    static {
        JodaBeanUtils.registerMetaBean(MutableListNonFinalBean.Meta.INSTANCE);
    }

    /**
     * Returns a builder used to create an instance of the bean.
     * @return the builder, not null
     */
    public static MutableListNonFinalBean.Builder builder() {
        return new MutableListNonFinalBean.Builder();
    }

    /**
     * Restricted constructor.
     * @param builder  the builder to copy from, not null
     */
    protected MutableListNonFinalBean(MutableListNonFinalBean.Builder builder) {
        this.strings = builder.strings;
        this.numberMap = builder.numberMap;
    }

    @Override
    public MutableListNonFinalBean.Meta metaBean() {
        return MutableListNonFinalBean.Meta.INSTANCE;
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
     * Gets the strings.
     * @return the value of the property
     */
    public List<String> getStrings() {
        return strings;
    }

    /**
     * Sets the strings.
     * @param strings  the new value of the property
     */
    public void setStrings(List<String> strings) {
        this.strings = strings;
    }

    /**
     * Gets the the {@code strings} property.
     * @return the property, not null
     */
    public final Property<List<String>> strings() {
        return metaBean().strings().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the numberMap.
     * @return the value of the property
     */
    public Map<String, Integer> getNumberMap() {
        return numberMap;
    }

    /**
     * Sets the numberMap.
     * @param numberMap  the new value of the property
     */
    public void setNumberMap(Map<String, Integer> numberMap) {
        this.numberMap = numberMap;
    }

    /**
     * Gets the the {@code numberMap} property.
     * @return the property, not null
     */
    public final Property<Map<String, Integer>> numberMap() {
        return metaBean().numberMap().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public MutableListNonFinalBean clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            MutableListNonFinalBean other = (MutableListNonFinalBean) obj;
            return JodaBeanUtils.equal(getStrings(), other.getStrings()) &&
                    JodaBeanUtils.equal(getNumberMap(), other.getNumberMap());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getStrings());
        hash = hash * 31 + JodaBeanUtils.hashCode(getNumberMap());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(96);
        buf.append("MutableListNonFinalBean{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    protected void toString(StringBuilder buf) {
        buf.append("strings").append('=').append(JodaBeanUtils.toString(getStrings())).append(',').append(' ');
        buf.append("numberMap").append('=').append(JodaBeanUtils.toString(getNumberMap())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code MutableListNonFinalBean}.
     */
    public static class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code strings} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<List<String>> strings = DirectMetaProperty.ofReadWrite(
                this, "strings", MutableListNonFinalBean.class, (Class) List.class);
        /**
         * The meta-property for the {@code numberMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<Map<String, Integer>> numberMap = DirectMetaProperty.ofReadWrite(
                this, "numberMap", MutableListNonFinalBean.class, (Class) Map.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "strings",
                "numberMap");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1881759102:  // strings
                    return strings;
                case 1649910099:  // numberMap
                    return numberMap;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public MutableListNonFinalBean.Builder builder() {
            return new MutableListNonFinalBean.Builder();
        }

        @Override
        public Class<? extends MutableListNonFinalBean> beanType() {
            return MutableListNonFinalBean.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code strings} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<List<String>> strings() {
            return strings;
        }

        /**
         * The meta-property for the {@code numberMap} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Map<String, Integer>> numberMap() {
            return numberMap;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1881759102:  // strings
                    return ((MutableListNonFinalBean) bean).getStrings();
                case 1649910099:  // numberMap
                    return ((MutableListNonFinalBean) bean).getNumberMap();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1881759102:  // strings
                    ((MutableListNonFinalBean) bean).setStrings((List<String>) newValue);
                    return;
                case 1649910099:  // numberMap
                    ((MutableListNonFinalBean) bean).setNumberMap((Map<String, Integer>) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-----------------------------------------------------------------------
    /**
     * The bean-builder for {@code MutableListNonFinalBean}.
     */
    public static class Builder extends DirectFieldsBeanBuilder<MutableListNonFinalBean> {

        private List<String> strings;
        private Map<String, Integer> numberMap;

        /**
         * Restricted constructor.
         */
        protected Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        protected Builder(MutableListNonFinalBean beanToCopy) {
            this.strings = (beanToCopy.getStrings() != null ? ImmutableList.copyOf(beanToCopy.getStrings()) : null);
            this.numberMap = (beanToCopy.getNumberMap() != null ? ImmutableMap.copyOf(beanToCopy.getNumberMap()) : null);
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1881759102:  // strings
                    return strings;
                case 1649910099:  // numberMap
                    return numberMap;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case -1881759102:  // strings
                    this.strings = (List<String>) newValue;
                    break;
                case 1649910099:  // numberMap
                    this.numberMap = (Map<String, Integer>) newValue;
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

        /**
         * @deprecated Use Joda-Convert in application code
         */
        @Override
        @Deprecated
        public Builder setString(String propertyName, String value) {
            setString(meta().metaProperty(propertyName), value);
            return this;
        }

        /**
         * @deprecated Use Joda-Convert in application code
         */
        @Override
        @Deprecated
        public Builder setString(MetaProperty<?> property, String value) {
            super.setString(property, value);
            return this;
        }

        /**
         * @deprecated Loop in application code
         */
        @Override
        @Deprecated
        public Builder setAll(Map<String, ? extends Object> propertyValueMap) {
            super.setAll(propertyValueMap);
            return this;
        }

        @Override
        public MutableListNonFinalBean build() {
            return new MutableListNonFinalBean(this);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets the strings.
         * @param strings  the new value
         * @return this, for chaining, not null
         */
        public Builder strings(List<String> strings) {
            this.strings = strings;
            return this;
        }

        /**
         * Sets the {@code strings} property in the builder
         * from an array of objects.
         * @param strings  the new value
         * @return this, for chaining, not null
         */
        public Builder strings(String... strings) {
            return strings(ImmutableList.copyOf(strings));
        }

        /**
         * Sets the numberMap.
         * @param numberMap  the new value
         * @return this, for chaining, not null
         */
        public Builder numberMap(Map<String, Integer> numberMap) {
            this.numberMap = numberMap;
            return this;
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(96);
            buf.append("MutableListNonFinalBean.Builder{");
            int len = buf.length();
            toString(buf);
            if (buf.length() > len) {
                buf.setLength(buf.length() - 2);
            }
            buf.append('}');
            return buf.toString();
        }

        protected void toString(StringBuilder buf) {
            buf.append("strings").append('=').append(JodaBeanUtils.toString(strings)).append(',').append(' ');
            buf.append("numberMap").append('=').append(JodaBeanUtils.toString(numberMap)).append(',').append(' ');
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
