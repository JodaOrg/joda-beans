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
import org.joda.beans.impl.direct.DirectBean;
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
public class Documentation<T> extends DirectBean {

    /** The type. */
    @PropertyDefinition
    private String type;
    /** The surname. */
    @PropertyDefinition
    private T content;
    @PropertyDefinition
    private Map<String, String> map;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code Documentation}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static Documentation.Meta meta() {
        return Documentation.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code Documentation}.
     * @param <R>  the bean's generic type
     * @param cls  the bean's generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R> Documentation.Meta<R> metaDocumentation(Class<R> cls) {
        return Documentation.Meta.INSTANCE;
    }

    static {
        MetaBean.register(Documentation.Meta.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Documentation.Meta<T> metaBean() {
        return Documentation.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type.
     * @return the value of the property
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type  the new value of the property
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the the {@code type} property.
     * @return the property, not null
     */
    public final Property<String> type() {
        return metaBean().type().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the surname.
     * @return the value of the property
     */
    public T getContent() {
        return content;
    }

    /**
     * Sets the surname.
     * @param content  the new value of the property
     */
    public void setContent(T content) {
        this.content = content;
    }

    /**
     * Gets the the {@code content} property.
     * @return the property, not null
     */
    public final Property<T> content() {
        return metaBean().content().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the map.
     * @return the value of the property
     */
    public Map<String, String> getMap() {
        return map;
    }

    /**
     * Sets the map.
     * @param map  the new value of the property
     */
    public void setMap(Map<String, String> map) {
        this.map = map;
    }

    /**
     * Gets the the {@code map} property.
     * @return the property, not null
     */
    public final Property<Map<String, String>> map() {
        return metaBean().map().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public Documentation<T> clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            Documentation<?> other = (Documentation<?>) obj;
            return JodaBeanUtils.equal(this.getType(), other.getType()) &&
                    JodaBeanUtils.equal(this.getContent(), other.getContent()) &&
                    JodaBeanUtils.equal(this.getMap(), other.getMap());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getType());
        hash = hash * 31 + JodaBeanUtils.hashCode(getContent());
        hash = hash * 31 + JodaBeanUtils.hashCode(getMap());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("Documentation{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    protected void toString(StringBuilder buf) {
        buf.append("type").append('=').append(JodaBeanUtils.toString(getType())).append(',').append(' ');
        buf.append("content").append('=').append(JodaBeanUtils.toString(getContent())).append(',').append(' ');
        buf.append("map").append('=').append(JodaBeanUtils.toString(getMap())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code Documentation}.
     * @param <T>  the type
     */
    public static class Meta<T> extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code type} property.
         */
        private final MetaProperty<String> type = DirectMetaProperty.ofReadWrite(
                this, "type", Documentation.class, String.class);
        /**
         * The meta-property for the {@code content} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<T> content = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "content", Documentation.class, Object.class);
        /**
         * The meta-property for the {@code map} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<Map<String, String>> map = DirectMetaProperty.ofReadWrite(
                this, "map", Documentation.class, (Class) Map.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "type",
                "content",
                "map");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 3575610:  // type
                    return this.type;
                case 951530617:  // content
                    return this.content;
                case 107868:  // map
                    return this.map;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends Documentation<T>> builder() {
            return new DirectBeanBuilder<>(new Documentation<T>());
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends Documentation<T>> beanType() {
            return (Class) Documentation.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code type} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> type() {
            return type;
        }

        /**
         * The meta-property for the {@code content} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<T> content() {
            return content;
        }

        /**
         * The meta-property for the {@code map} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Map<String, String>> map() {
            return map;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 3575610:  // type
                    return ((Documentation<?>) bean).getType();
                case 951530617:  // content
                    return ((Documentation<?>) bean).getContent();
                case 107868:  // map
                    return ((Documentation<?>) bean).getMap();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 3575610:  // type
                    ((Documentation<T>) bean).setType((String) newValue);
                    return;
                case 951530617:  // content
                    ((Documentation<T>) bean).setContent((T) newValue);
                    return;
                case 107868:  // map
                    ((Documentation<T>) bean).setMap((Map<String, String>) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
