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

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.MinimalMetaBean;

/**
 * Mock JavaBean, used for testing.
 */
@BeanDefinition(style = "minimal")
public final class FieldNamesMutableMinimal implements Bean {

    /** Field named 'obj' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String obj;
    /** Field named 'other' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String other;
    /** Field named 'propertyName' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String propertyName;
    /** Field named 'newValue' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String newValue;
    /** Field named 'bean' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String bean;
    /** Field named 'beanToCopy' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private String beanToCopy;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code FieldNamesMutableMinimal}.
     */
    private static final TypedMetaBean<FieldNamesMutableMinimal> META_BEAN =
            MinimalMetaBean.of(
                    FieldNamesMutableMinimal.class,
                    new String[] {
                            "obj",
                            "other",
                            "propertyName",
                            "newValue",
                            "bean",
                            "beanToCopy"},
                    () -> new BasicBeanBuilder<>(new FieldNamesMutableMinimal()),
                    Arrays.<Function<FieldNamesMutableMinimal, Object>>asList(
                            b -> b.obj,
                            b -> b.other,
                            b -> b.propertyName,
                            b -> b.newValue,
                            b -> b.bean,
                            b -> b.beanToCopy),
                    Arrays.<BiConsumer<FieldNamesMutableMinimal, Object>>asList(
                            (b, v) -> b.setObj((String) v),
                            (b, v) -> b.setOther((String) v),
                            (b, v) -> b.setPropertyName((String) v),
                            (b, v) -> b.setNewValue((String) v),
                            (b, v) -> b.setBean((String) v),
                            (b, v) -> b.setBeanToCopy((String) v)));

    /**
     * The meta-bean for {@code FieldNamesMutableMinimal}.
     * @return the meta-bean, not null
     */
    public static TypedMetaBean<FieldNamesMutableMinimal> meta() {
        return META_BEAN;
    }

    static {
        MetaBean.register(META_BEAN);
    }

    @Override
    public TypedMetaBean<FieldNamesMutableMinimal> metaBean() {
        return META_BEAN;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'obj' to check for name clashes.
     * @param obj  the new value of the property
     */
    public void setObj(String obj) {
        this.obj = obj;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'other' to check for name clashes.
     * @param other  the new value of the property
     */
    public void setOther(String other) {
        this.other = other;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'propertyName' to check for name clashes.
     * @param propertyName  the new value of the property
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'newValue' to check for name clashes.
     * @param newValue  the new value of the property
     */
    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'bean' to check for name clashes.
     * @param bean  the new value of the property
     */
    public void setBean(String bean) {
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets field named 'beanToCopy' to check for name clashes.
     * @param beanToCopy  the new value of the property
     */
    public void setBeanToCopy(String beanToCopy) {
        this.beanToCopy = beanToCopy;
    }

    //-----------------------------------------------------------------------
    @Override
    public FieldNamesMutableMinimal clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            FieldNamesMutableMinimal other = (FieldNamesMutableMinimal) obj;
            return JodaBeanUtils.equal(this.obj, other.obj) &&
                    JodaBeanUtils.equal(this.other, other.other) &&
                    JodaBeanUtils.equal(this.propertyName, other.propertyName) &&
                    JodaBeanUtils.equal(this.newValue, other.newValue) &&
                    JodaBeanUtils.equal(this.bean, other.bean) &&
                    JodaBeanUtils.equal(this.beanToCopy, other.beanToCopy);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(obj);
        hash = hash * 31 + JodaBeanUtils.hashCode(other);
        hash = hash * 31 + JodaBeanUtils.hashCode(propertyName);
        hash = hash * 31 + JodaBeanUtils.hashCode(newValue);
        hash = hash * 31 + JodaBeanUtils.hashCode(bean);
        hash = hash * 31 + JodaBeanUtils.hashCode(beanToCopy);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(224);
        buf.append("FieldNamesMutableMinimal{");
        buf.append("obj").append('=').append(JodaBeanUtils.toString(obj)).append(',').append(' ');
        buf.append("other").append('=').append(JodaBeanUtils.toString(other)).append(',').append(' ');
        buf.append("propertyName").append('=').append(JodaBeanUtils.toString(propertyName)).append(',').append(' ');
        buf.append("newValue").append('=').append(JodaBeanUtils.toString(newValue)).append(',').append(' ');
        buf.append("bean").append('=').append(JodaBeanUtils.toString(bean)).append(',').append(' ');
        buf.append("beanToCopy").append('=').append(JodaBeanUtils.toString(beanToCopy));
        buf.append('}');
        return buf.toString();
    }

    //-------------------------- AUTOGENERATED END --------------------------
}
