/*
 *  Copyright 2001-2014 Stephen Colebourne
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
package org.joda.beans.integrate.freemarker;

import org.joda.beans.Bean;
import org.joda.beans.MetaProperty;

import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

/**
 * Template model converting a Joda-Bean to a Freemarker model.
 * <p>
 * Although this class is public, it should not normally be use directly.
 * Follow the instructions in {@link FreemarkerObjectWrapper} to use this class.
 */
public class FreemarkerTemplateModel
        extends WrappingTemplateModel
        implements TemplateHashModelEx, AdapterTemplateModel {

    /**
     * The bean being wrapped.
     */
    private final Bean bean;

    /**
     * Creates an instance of the model.
     * @param bean  the bean being wrapped, not null
     * @param wrapper  the default wrapper for further wrapping, not null
     */
    public FreemarkerTemplateModel(final Bean bean, final FreemarkerObjectWrapper wrapper) {
        super(wrapper);
        this.bean = bean;
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the value for the specified key, wrapping the result in another model.
     * @param key  the property name, not null
     * @return the model, null if not found
     */
    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        MetaProperty<?> metaProperty = bean.metaBean().metaPropertyMap().get(key);
        if (metaProperty == null) {
            // try standard approach via BeanModel for non-bean properties and methods
            BeanModel model = new BeanModel(bean, (BeansWrapper) getObjectWrapper());
            TemplateModel result = model.get(key);
            if (result instanceof SimpleScalar) {
                // have to map empty string to null
                String str = ((SimpleScalar) result).getAsString();
                if (str == null || str.isEmpty()) {
                    return null;
                }
            }
            return result;
        }
        return wrap(metaProperty.get(bean));
    }

    /**
     * Checks if there are no properties.
     * @return true if no properties
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Gets the number of properties.
     * @return the number of properties
     */
    @Override
    public int size() {
        return bean.metaBean().metaPropertyCount();
    }

    /**
     * Gets the full set of property names, allowing the bean to be accessed as a sequence.
     * @return the property names, not null
     */
    @Override
    public TemplateCollectionModel keys() {
        return new SimpleCollection(bean.propertyNames(), getObjectWrapper());
    }

    /**
     * Gets the full set of property values, allowing the bean to be accessed as a sequence.
     * @return the wrapped property values, not null
     */
    @Override
    public TemplateCollectionModel values() {
        return new SimpleCollection(bean.metaBean().createPropertyMap(bean).flatten().values(), getObjectWrapper());
    }

    /**
     * Unwraps the model, returning the bean.
     * @param hint  the class hint
     * @return the underlying bean, not null
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Object getAdaptedObject(Class hint) {
        return bean;
    }

}
