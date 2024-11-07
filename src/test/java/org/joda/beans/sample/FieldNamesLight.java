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

import java.lang.invoke.MethodHandles;

import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.light.LightMetaBean;

/**
 * Mock JavaBean, used for testing.
 */
@BeanDefinition(style = "light", builderScope = "public")
public final class FieldNamesLight implements ImmutableBean {

    /** Field named 'obj' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String obj;
    /** Field named 'other' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String other;
    /** Field named 'propertyName' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String propertyName;
    /** Field named 'newValue' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String newValue;
    /** Field named 'bean' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String bean;
    /** Field named 'beanToCopy' to check for name clashes. */
    @PropertyDefinition(get = "field")
    private final String beanToCopy;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code FieldNamesLight}.
     */
    private static final TypedMetaBean<FieldNamesLight> META_BEAN =
            LightMetaBean.of(
                    FieldNamesLight.class,
                    MethodHandles.lookup(),
                    new String[] {
                            "obj",
                            "other",
                            "propertyName",
                            "newValue",
                            "bean",
                            "beanToCopy"},
                    new Object[0]);

    /**
     * The meta-bean for {@code FieldNamesLight}.
     * @return the meta-bean, not null
     */
    public static TypedMetaBean<FieldNamesLight> meta() {
        return META_BEAN;
    }

    static {
        MetaBean.register(META_BEAN);
    }

    private FieldNamesLight(
            String obj,
            String other,
            String propertyName,
            String newValue,
            String bean,
            String beanToCopy) {
        this.obj = obj;
        this.other = other;
        this.propertyName = propertyName;
        this.newValue = newValue;
        this.bean = bean;
        this.beanToCopy = beanToCopy;
    }

    @Override
    public TypedMetaBean<FieldNamesLight> metaBean() {
        return META_BEAN;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            FieldNamesLight other = (FieldNamesLight) obj;
            return JodaBeanUtils.equal(this.obj, other.obj) &&
                    JodaBeanUtils.equal(this.other, other.other) &&
                    JodaBeanUtils.equal(propertyName, other.propertyName) &&
                    JodaBeanUtils.equal(newValue, other.newValue) &&
                    JodaBeanUtils.equal(bean, other.bean) &&
                    JodaBeanUtils.equal(beanToCopy, other.beanToCopy);
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
        buf.append("FieldNamesLight{");
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
