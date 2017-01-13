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
package org.joda.beans.integrate.freemarker;


import org.joda.beans.Bean;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Freemarker support class for Joda-Beans.
 * <p>
 * This class allows Joda-Beans to be used in the Freemarker templating system.
 * When creating a Freemarker {@code Configuration}, simply set call
 * {@code setObjectWrapper(ObjectWrapper)} with an instance of this class.
 */
public class FreemarkerObjectWrapper extends DefaultObjectWrapper {

    /**
     * Creates a new instance.
     */
    public FreemarkerObjectWrapper() {
    }

    //-------------------------------------------------------------------------
    /**
     * Overrides to trap instances of {@code Bean} and handle them.
     * 
     * @param obj  the object to wrap, not null
     * @return the template model, not null
     * @throws TemplateModelException if unable to create the model
     */
    @Override
    public TemplateModel wrap(Object obj) throws TemplateModelException {
        if (obj instanceof Bean) {
            return new FreemarkerTemplateModel((Bean) obj, this);
        }
        return super.wrap(obj);
    }

    //-----------------------------------------------------------------------
    @Override
    public String toString() {
        return "FreemarkerObjectWrapper";
    }

}
