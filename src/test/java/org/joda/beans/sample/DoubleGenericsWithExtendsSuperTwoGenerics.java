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
import java.util.List;
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
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;

/**
 * Mock JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition
public class DoubleGenericsWithExtendsSuperTwoGenerics<T extends Serializable, U extends Number>
        extends DoubleGenericsSimpleSuper<T, Number> {

    /** The normal type. */
    @PropertyDefinition
    private String normalType;
    /** The type T value. */
    @PropertyDefinition
    private T typeT;
    /** The type U value. */
    @PropertyDefinition
    private U typeU;
    /** The type T value. */
    @PropertyDefinition
    private List<T> typeTList;
    /** The type U value. */
    @PropertyDefinition
    private List<U> typeUList;
    /** The type T value. */
    @PropertyDefinition
    private T[] typeTArray;
    /** The type U value. */
    @PropertyDefinition
    private U[] typeUArray;

    /**
     * Creates an instance.
     */
    public DoubleGenericsWithExtendsSuperTwoGenerics() {
    }

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code DoubleGenericsWithExtendsSuperTwoGenerics}.
     * @return the meta-bean, not null
     */
    @SuppressWarnings("rawtypes")
    public static DoubleGenericsWithExtendsSuperTwoGenerics.Meta meta() {
        return DoubleGenericsWithExtendsSuperTwoGenerics.Meta.INSTANCE;
    }

    /**
     * The meta-bean for {@code DoubleGenericsWithExtendsSuperTwoGenerics}.
     * @param <R>  the first generic type
     * @param <S>  the second generic type
     * @param cls1  the first generic type
     * @param cls2  the second generic type
     * @return the meta-bean, not null
     */
    @SuppressWarnings("unchecked")
    public static <R extends Serializable, S extends Number> DoubleGenericsWithExtendsSuperTwoGenerics.Meta<R, S> metaDoubleGenericsWithExtendsSuperTwoGenerics(Class<R> cls1, Class<S> cls2) {
        return DoubleGenericsWithExtendsSuperTwoGenerics.Meta.INSTANCE;
    }

    static {
        MetaBean.register(DoubleGenericsWithExtendsSuperTwoGenerics.Meta.INSTANCE);
    }

    @SuppressWarnings("unchecked")
    @Override
    public DoubleGenericsWithExtendsSuperTwoGenerics.Meta<T, U> metaBean() {
        return DoubleGenericsWithExtendsSuperTwoGenerics.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the normal type.
     * @return the value of the property
     */
    public String getNormalType() {
        return normalType;
    }

    /**
     * Sets the normal type.
     * @param normalType  the new value of the property
     */
    public void setNormalType(String normalType) {
        this.normalType = normalType;
    }

    /**
     * Gets the the {@code normalType} property.
     * @return the property, not null
     */
    public final Property<String> normalType() {
        return metaBean().normalType().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type T value.
     * @return the value of the property
     */
    public T getTypeT() {
        return typeT;
    }

    /**
     * Sets the type T value.
     * @param typeT  the new value of the property
     */
    public void setTypeT(T typeT) {
        this.typeT = typeT;
    }

    /**
     * Gets the the {@code typeT} property.
     * @return the property, not null
     */
    public final Property<T> typeT() {
        return metaBean().typeT().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type U value.
     * @return the value of the property
     */
    public U getTypeU() {
        return typeU;
    }

    /**
     * Sets the type U value.
     * @param typeU  the new value of the property
     */
    public void setTypeU(U typeU) {
        this.typeU = typeU;
    }

    /**
     * Gets the the {@code typeU} property.
     * @return the property, not null
     */
    public final Property<U> typeU() {
        return metaBean().typeU().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type T value.
     * @return the value of the property
     */
    public List<T> getTypeTList() {
        return typeTList;
    }

    /**
     * Sets the type T value.
     * @param typeTList  the new value of the property
     */
    public void setTypeTList(List<T> typeTList) {
        this.typeTList = typeTList;
    }

    /**
     * Gets the the {@code typeTList} property.
     * @return the property, not null
     */
    public final Property<List<T>> typeTList() {
        return metaBean().typeTList().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type U value.
     * @return the value of the property
     */
    public List<U> getTypeUList() {
        return typeUList;
    }

    /**
     * Sets the type U value.
     * @param typeUList  the new value of the property
     */
    public void setTypeUList(List<U> typeUList) {
        this.typeUList = typeUList;
    }

    /**
     * Gets the the {@code typeUList} property.
     * @return the property, not null
     */
    public final Property<List<U>> typeUList() {
        return metaBean().typeUList().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type T value.
     * @return the value of the property
     */
    public T[] getTypeTArray() {
        return typeTArray;
    }

    /**
     * Sets the type T value.
     * @param typeTArray  the new value of the property
     */
    public void setTypeTArray(T[] typeTArray) {
        this.typeTArray = typeTArray;
    }

    /**
     * Gets the the {@code typeTArray} property.
     * @return the property, not null
     */
    public final Property<T[]> typeTArray() {
        return metaBean().typeTArray().createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the type U value.
     * @return the value of the property
     */
    public U[] getTypeUArray() {
        return typeUArray;
    }

    /**
     * Sets the type U value.
     * @param typeUArray  the new value of the property
     */
    public void setTypeUArray(U[] typeUArray) {
        this.typeUArray = typeUArray;
    }

    /**
     * Gets the the {@code typeUArray} property.
     * @return the property, not null
     */
    public final Property<U[]> typeUArray() {
        return metaBean().typeUArray().createProperty(this);
    }

    //-----------------------------------------------------------------------
    @Override
    public DoubleGenericsWithExtendsSuperTwoGenerics<T, U> clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            DoubleGenericsWithExtendsSuperTwoGenerics<?, ?> other = (DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) obj;
            return JodaBeanUtils.equal(this.getNormalType(), other.getNormalType()) &&
                    JodaBeanUtils.equal(this.getTypeT(), other.getTypeT()) &&
                    JodaBeanUtils.equal(this.getTypeU(), other.getTypeU()) &&
                    JodaBeanUtils.equal(this.getTypeTList(), other.getTypeTList()) &&
                    JodaBeanUtils.equal(this.getTypeUList(), other.getTypeUList()) &&
                    JodaBeanUtils.equal(this.getTypeTArray(), other.getTypeTArray()) &&
                    JodaBeanUtils.equal(this.getTypeUArray(), other.getTypeUArray()) &&
                    super.equals(obj);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 31 + JodaBeanUtils.hashCode(getNormalType());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeT());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeU());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeTList());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeUList());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeTArray());
        hash = hash * 31 + JodaBeanUtils.hashCode(getTypeUArray());
        return hash ^ super.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(256);
        buf.append("DoubleGenericsWithExtendsSuperTwoGenerics{");
        int len = buf.length();
        toString(buf);
        if (buf.length() > len) {
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

    @Override
    protected void toString(StringBuilder buf) {
        super.toString(buf);
        buf.append("normalType").append('=').append(JodaBeanUtils.toString(getNormalType())).append(',').append(' ');
        buf.append("typeT").append('=').append(JodaBeanUtils.toString(getTypeT())).append(',').append(' ');
        buf.append("typeU").append('=').append(JodaBeanUtils.toString(getTypeU())).append(',').append(' ');
        buf.append("typeTList").append('=').append(JodaBeanUtils.toString(getTypeTList())).append(',').append(' ');
        buf.append("typeUList").append('=').append(JodaBeanUtils.toString(getTypeUList())).append(',').append(' ');
        buf.append("typeTArray").append('=').append(JodaBeanUtils.toString(getTypeTArray())).append(',').append(' ');
        buf.append("typeUArray").append('=').append(JodaBeanUtils.toString(getTypeUArray())).append(',').append(' ');
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code DoubleGenericsWithExtendsSuperTwoGenerics}.
     * @param <T>  the type
     * @param <U>  the type
     */
    public static class Meta<T extends Serializable, U extends Number> extends DoubleGenericsSimpleSuper.Meta<T, Number> {
        /**
         * The singleton instance of the meta-bean.
         */
        @SuppressWarnings("rawtypes")
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code normalType} property.
         */
        private final MetaProperty<String> normalType = DirectMetaProperty.ofReadWrite(
                this, "normalType", DoubleGenericsWithExtendsSuperTwoGenerics.class, String.class);
        /**
         * The meta-property for the {@code typeT} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<T> typeT = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "typeT", DoubleGenericsWithExtendsSuperTwoGenerics.class, Object.class);
        /**
         * The meta-property for the {@code typeU} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<U> typeU = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "typeU", DoubleGenericsWithExtendsSuperTwoGenerics.class, Object.class);
        /**
         * The meta-property for the {@code typeTList} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<List<T>> typeTList = DirectMetaProperty.ofReadWrite(
                this, "typeTList", DoubleGenericsWithExtendsSuperTwoGenerics.class, (Class) List.class);
        /**
         * The meta-property for the {@code typeUList} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<List<U>> typeUList = DirectMetaProperty.ofReadWrite(
                this, "typeUList", DoubleGenericsWithExtendsSuperTwoGenerics.class, (Class) List.class);
        /**
         * The meta-property for the {@code typeTArray} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<T[]> typeTArray = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "typeTArray", DoubleGenericsWithExtendsSuperTwoGenerics.class, Object[].class);
        /**
         * The meta-property for the {@code typeUArray} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<U[]> typeUArray = (DirectMetaProperty) DirectMetaProperty.ofReadWrite(
                this, "typeUArray", DoubleGenericsWithExtendsSuperTwoGenerics.class, Object[].class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, (DirectMetaPropertyMap) super.metaPropertyMap(),
                "normalType",
                "typeT",
                "typeU",
                "typeTList",
                "typeUList",
                "typeTArray",
                "typeUArray");

        /**
         * Restricted constructor.
         */
        protected Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1255672639:  // normalType
                    return this.normalType;
                case 110843994:  // typeT
                    return this.typeT;
                case 110843995:  // typeU
                    return this.typeU;
                case 508018712:  // typeTList
                    return this.typeTList;
                case 508942233:  // typeUList
                    return this.typeUList;
                case -1441181153:  // typeTArray
                    return this.typeTArray;
                case -1412552002:  // typeUArray
                    return this.typeUArray;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public BeanBuilder<? extends DoubleGenericsWithExtendsSuperTwoGenerics<T, U>> builder() {
            return new DirectBeanBuilder<>(new DoubleGenericsWithExtendsSuperTwoGenerics<T, U>());
        }

        @SuppressWarnings({"unchecked", "rawtypes" })
        @Override
        public Class<? extends DoubleGenericsWithExtendsSuperTwoGenerics<T, U>> beanType() {
            return (Class) DoubleGenericsWithExtendsSuperTwoGenerics.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code normalType} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<String> normalType() {
            return normalType;
        }

        /**
         * The meta-property for the {@code typeT} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<T> typeT() {
            return typeT;
        }

        /**
         * The meta-property for the {@code typeU} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<U> typeU() {
            return typeU;
        }

        /**
         * The meta-property for the {@code typeTList} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<List<T>> typeTList() {
            return typeTList;
        }

        /**
         * The meta-property for the {@code typeUList} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<List<U>> typeUList() {
            return typeUList;
        }

        /**
         * The meta-property for the {@code typeTArray} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<T[]> typeTArray() {
            return typeTArray;
        }

        /**
         * The meta-property for the {@code typeUArray} property.
         * @return the meta-property, not null
         */
        public final MetaProperty<U[]> typeUArray() {
            return typeUArray;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1255672639:  // normalType
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getNormalType();
                case 110843994:  // typeT
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeT();
                case 110843995:  // typeU
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeU();
                case 508018712:  // typeTList
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeTList();
                case 508942233:  // typeUList
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeUList();
                case -1441181153:  // typeTArray
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeTArray();
                case -1412552002:  // typeUArray
                    return ((DoubleGenericsWithExtendsSuperTwoGenerics<?, ?>) bean).getTypeUArray();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1255672639:  // normalType
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setNormalType((String) newValue);
                    return;
                case 110843994:  // typeT
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeT((T) newValue);
                    return;
                case 110843995:  // typeU
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeU((U) newValue);
                    return;
                case 508018712:  // typeTList
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeTList((List<T>) newValue);
                    return;
                case 508942233:  // typeUList
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeUList((List<U>) newValue);
                    return;
                case -1441181153:  // typeTArray
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeTArray((T[]) newValue);
                    return;
                case -1412552002:  // typeUArray
                    ((DoubleGenericsWithExtendsSuperTwoGenerics<T, U>) bean).setTypeUArray((U[]) newValue);
                    return;
            }
            super.propertySet(bean, propertyName, newValue, quiet);
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
