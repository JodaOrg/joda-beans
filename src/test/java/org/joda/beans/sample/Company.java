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
 * Mock address JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class Company
        implements Bean {

    /** The company name. */
    @PropertyDefinition
    private String companyName;

    /**
     * Creates an instanec.
     */
    public Company() {
    }

    /**
     * Creates an instanec.
     * 
     * @param name  the name
     */
    public Company(String name) {
        companyName = name;
    }

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code Company}.
     * @return the meta-bean, not null
     */
    public static Company.Meta meta() {
        return Company.Meta.INSTANCE;
    }

    static {
        MetaBean.register(Company.Meta.INSTANCE);
    }

    @Override
    public Company.Meta metaBean() {
        return Company.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the company name.
     * @return the value of the property
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * Sets the company name.
     * @param companyName  the new value of the property
     */
    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    /**
     * Gets the the {@code companyName} property.
     * @return the property, not null
     */
    public final Property<String> companyName() {
        return metaBean().companyName().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public Company clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            Company other = (Company) obj;
            return JodaBeanUtils.equal(this.getCompanyName(), other.getCompanyName());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getCompanyName());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);
        buf.append("Company{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    protected void toString(StringBuilder buf) {
        buf.append("companyName").append('=').append(JodaBeanUtils.toString(getCompanyName())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code Company}.
     */
    public static class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code companyName} property.
         */
        private final MetaProperty<String> companyName = DirectMetaProperty.ofReadWrite(
                this, "companyName", Company.class, String.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "companyName");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -508582744:  // companyName
                    return this.companyName;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends Company> builder() {
            return new DirectBeanBuilder<>(new Company());
        }

        @Override
        public Class<? extends Company> beanType() {
            return Company.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code companyName} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> companyName() {
            return companyName;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -508582744:  // companyName
                    return ((Company) bean).getCompanyName();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -508582744:  // companyName
                    ((Company) bean).setCompanyName((String) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
