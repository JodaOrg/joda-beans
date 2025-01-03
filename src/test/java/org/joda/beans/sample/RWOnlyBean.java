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
import org.joda.beans.gen.DerivedProperty;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock used for test equals.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class RWOnlyBean extends DirectBean {

    /**
     * The read only property.
     */
    @PropertyDefinition(set = "")
    private Object ro = null;  // trailing comment
    /**
     * The write only property.
     */
    @PropertyDefinition(get = "")
    private Object wo;  // trailing comment
    /**
     * The final read only property.
     */
    @PropertyDefinition
    private final Object fin;
    /**
     * The private get/set property.
     */
    @PropertyDefinition(get = "private", set = "private")
    private String priv;
    /**
     * The package-scoped get/set property.
     */
    @PropertyDefinition(get = "package", set = "package")
    private String pkg;
    /**
     * The protected get/set property.
     */
    @PropertyDefinition(get = "protected", set = "protected")
    private String prot;
    /**
     * The field-based get/set property.
     */
    @PropertyDefinition(get = "field", set = "field")
    private String field;

    public RWOnlyBean() {
        fin = "";
    }

    /**
     * A manual get property, no set.
     */
    @PropertyDefinition(get = "manual", set = "")
    private String manualGet;

    public String getManualGet() {
        return "goo";
    }

    /**
     * Gets the value of a derived property.
     * 
     * @return derived
     */
    @DerivedProperty
    public String getDerived() {
        return "drv";
    }

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code RWOnlyBean}.
     * @return the meta-bean, not null
     */
    public static RWOnlyBean.Meta meta() {
        return RWOnlyBean.Meta.INSTANCE;
    }

    static {
        MetaBean.register(RWOnlyBean.Meta.INSTANCE);
    }

    @Override
    public RWOnlyBean.Meta metaBean() {
        return RWOnlyBean.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the read only property.
     * @return the value of the property
     */
    public Object getRo() {
        return ro;
    }

    /**
     * Gets the the {@code ro} property.
     * @return the property, not null
     */
    public final Property<Object> ro() {
        return metaBean().ro().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Sets the write only property.
     * @param wo  the new value of the property
     */
    public void setWo(Object wo) {
        this.wo = wo;
    }

    /**
     * Gets the the {@code wo} property.
     * @return the property, not null
     */
    public final Property<Object> wo() {
        return metaBean().wo().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the final read only property.
     * @return the value of the property
     */
    public Object getFin() {
        return fin;
    }

    /**
     * Gets the the {@code fin} property.
     * @return the property, not null
     */
    public final Property<Object> fin() {
        return metaBean().fin().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the private get/set property.
     * @return the value of the property
     */
    private String getPriv() {
        return priv;
    }

    /**
     * Sets the private get/set property.
     * @param priv  the new value of the property
     */
    private void setPriv(String priv) {
        this.priv = priv;
    }

    /**
     * Gets the the {@code priv} property.
     * @return the property, not null
     */
    public final Property<String> priv() {
        return metaBean().priv().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the package-scoped get/set property.
     * @return the value of the property
     */
    String getPkg() {
        return pkg;
    }

    /**
     * Sets the package-scoped get/set property.
     * @param pkg  the new value of the property
     */
    void setPkg(String pkg) {
        this.pkg = pkg;
    }

    /**
     * Gets the the {@code pkg} property.
     * @return the property, not null
     */
    public final Property<String> pkg() {
        return metaBean().pkg().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the protected get/set property.
     * @return the value of the property
     */
    protected String getProt() {
        return prot;
    }

    /**
     * Sets the protected get/set property.
     * @param prot  the new value of the property
     */
    protected void setProt(String prot) {
        this.prot = prot;
    }

    /**
     * Gets the the {@code prot} property.
     * @return the property, not null
     */
    public final Property<String> prot() {
        return metaBean().prot().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the the {@code field} property.
     * @return the property, not null
     */
    public final Property<String> field() {
        return metaBean().field().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the the {@code manualGet} property.
     * @return the property, not null
     */
    public final Property<String> manualGet() {
        return metaBean().manualGet().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the the {@code derived} property.
     * 
     * @return the property, not null
     */
    public final Property<String> derived() {
        return metaBean().derived().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public RWOnlyBean clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            RWOnlyBean other = (RWOnlyBean) obj;
            return JodaBeanUtils.equal(this.getRo(), other.getRo()) &&
                    JodaBeanUtils.equal(this.wo, other.wo) &&
                    JodaBeanUtils.equal(this.getFin(), other.getFin()) &&
                    JodaBeanUtils.equal(this.getPriv(), other.getPriv()) &&
                    JodaBeanUtils.equal(this.getPkg(), other.getPkg()) &&
                    JodaBeanUtils.equal(this.getProt(), other.getProt()) &&
                    JodaBeanUtils.equal(this.field, other.field) &&
                    JodaBeanUtils.equal(this.getManualGet(), other.getManualGet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getRo());
        hash = hash * 31 + JodaBeanUtils.hashCode(wo);
        hash = hash * 31 + JodaBeanUtils.hashCode(getFin());
        hash = hash * 31 + JodaBeanUtils.hashCode(getPriv());
        hash = hash * 31 + JodaBeanUtils.hashCode(getPkg());
        hash = hash * 31 + JodaBeanUtils.hashCode(getProt());
        hash = hash * 31 + JodaBeanUtils.hashCode(field);
        hash = hash * 31 + JodaBeanUtils.hashCode(getManualGet());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(320);
        buf.append("RWOnlyBean{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    protected void toString(StringBuilder buf) {
        buf.append("ro").append('=').append(JodaBeanUtils.toString(getRo())).append(',').append(' ');
        buf.append("wo").append('=').append(JodaBeanUtils.toString(wo)).append(',').append(' ');
        buf.append("fin").append('=').append(JodaBeanUtils.toString(getFin())).append(',').append(' ');
        buf.append("priv").append('=').append(JodaBeanUtils.toString(getPriv())).append(',').append(' ');
        buf.append("pkg").append('=').append(JodaBeanUtils.toString(getPkg())).append(',').append(' ');
        buf.append("prot").append('=').append(JodaBeanUtils.toString(getProt())).append(',').append(' ');
        buf.append("field").append('=').append(JodaBeanUtils.toString(field)).append(',').append(' ');
        buf.append("manualGet").append('=').append(JodaBeanUtils.toString(getManualGet())).append(',').append(' ');
        buf.append("derived").append('=').append(JodaBeanUtils.toString(getDerived())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code RWOnlyBean}.
     */
    public static class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code ro} property.
         */
        private final MetaProperty<Object> ro = DirectMetaProperty.ofReadOnly(
                this, "ro", RWOnlyBean.class, Object.class);
        /**
         * The meta-property for the {@code wo} property.
         */
        private final MetaProperty<Object> wo = DirectMetaProperty.ofWriteOnly(
                this, "wo", RWOnlyBean.class, Object.class);
        /**
         * The meta-property for the {@code fin} property.
         */
        private final MetaProperty<Object> fin = DirectMetaProperty.ofReadOnly(
                this, "fin", RWOnlyBean.class, Object.class);
        /**
         * The meta-property for the {@code priv} property.
         */
        private final MetaProperty<String> priv = DirectMetaProperty.ofReadWrite(
                this, "priv", RWOnlyBean.class, String.class);
        /**
         * The meta-property for the {@code pkg} property.
         */
        private final MetaProperty<String> pkg = DirectMetaProperty.ofReadWrite(
                this, "pkg", RWOnlyBean.class, String.class);
        /**
         * The meta-property for the {@code prot} property.
         */
        private final MetaProperty<String> prot = DirectMetaProperty.ofReadWrite(
                this, "prot", RWOnlyBean.class, String.class);
        /**
         * The meta-property for the {@code field} property.
         */
        private final MetaProperty<String> field = DirectMetaProperty.ofReadWrite(
                this, "field", RWOnlyBean.class, String.class);
        /**
         * The meta-property for the {@code manualGet} property.
         */
        private final MetaProperty<String> manualGet = DirectMetaProperty.ofReadOnly(
                this, "manualGet", RWOnlyBean.class, String.class);
        /**
         * The meta-property for the {@code derived} property.
         */
        private final MetaProperty<String> derived = DirectMetaProperty.ofDerived(
                this, "derived", RWOnlyBean.class, String.class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "ro",
                "wo",
                "fin",
                "priv",
                "pkg",
                "prot",
                "field",
                "manualGet",
                "derived");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case 3645:  // ro
                    return this.ro;
                case 3800:  // wo
                    return this.wo;
                case 101387:  // fin
                    return this.fin;
                case 3449519:  // priv
                    return this.priv;
                case 111052:  // pkg
                    return this.pkg;
                case 3449703:  // prot
                    return this.prot;
                case 97427706:  // field
                    return this.field;
                case 93508016:  // manualGet
                    return this.manualGet;
                case 1556125213:  // derived
                    return this.derived;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends RWOnlyBean> builder() {
            return new DirectBeanBuilder<>(new RWOnlyBean());
        }

        @Override
        public Class<? extends RWOnlyBean> beanType() {
            return RWOnlyBean.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code ro} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Object> ro() {
            return ro;
        }

        /**
         * The meta-property for the {@code wo} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Object> wo() {
            return wo;
        }

        /**
         * The meta-property for the {@code fin} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<Object> fin() {
            return fin;
        }

        /**
         * The meta-property for the {@code priv} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> priv() {
            return priv;
        }

        /**
         * The meta-property for the {@code pkg} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> pkg() {
            return pkg;
        }

        /**
         * The meta-property for the {@code prot} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> prot() {
            return prot;
        }

        /**
         * The meta-property for the {@code field} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> field() {
            return field;
        }

        /**
         * The meta-property for the {@code manualGet} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> manualGet() {
            return manualGet;
        }

        /**
         * The meta-property for the {@code derived} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> derived() {
            return derived;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 3645:  // ro
                    return ((RWOnlyBean) bean).getRo();
                case 3800:  // wo
                    if (quiet) {
                        return null;
                    }
                    throw new UnsupportedOperationException("Property cannot be read: wo");
                case 101387:  // fin
                    return ((RWOnlyBean) bean).getFin();
                case 3449519:  // priv
                    return ((RWOnlyBean) bean).getPriv();
                case 111052:  // pkg
                    return ((RWOnlyBean) bean).getPkg();
                case 3449703:  // prot
                    return ((RWOnlyBean) bean).getProt();
                case 97427706:  // field
                    return ((RWOnlyBean) bean).field;
                case 93508016:  // manualGet
                    return ((RWOnlyBean) bean).getManualGet();
                case 1556125213:  // derived
                    return ((RWOnlyBean) bean).getDerived();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case 3645:  // ro
                    if (quiet) {
                        return;
                    }
                    throw new UnsupportedOperationException("Property cannot be written: ro");
                case 3800:  // wo
                    ((RWOnlyBean) bean).setWo((Object) newValue);
                    return;
                case 101387:  // fin
                    if (quiet) {
                        return;
                    }
                    throw new UnsupportedOperationException("Property cannot be written: fin");
                case 3449519:  // priv
                    ((RWOnlyBean) bean).setPriv((String) newValue);
                    return;
                case 111052:  // pkg
                    ((RWOnlyBean) bean).setPkg((String) newValue);
                    return;
                case 3449703:  // prot
                    ((RWOnlyBean) bean).setProt((String) newValue);
                    return;
                case 97427706:  // field
                    ((RWOnlyBean) bean).field = (String) newValue;
                    return;
                case 93508016:  // manualGet
                    if (quiet) {
                        return;
                    }
                    throw new UnsupportedOperationException("Property cannot be written: manualGet");
                case 1556125213:  // derived
                    if (quiet) {
                        return;
                    }
                    throw new UnsupportedOperationException("Property cannot be written: derived");
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
