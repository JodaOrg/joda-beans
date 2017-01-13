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
package org.joda.beans.impl.direct;

import org.joda.beans.Bean;
import org.joda.beans.impl.BasicBeanBuilder;

/**
 * A builder implementation designed for use by the code generator.
 * <p>
 * This implementation adds validation on top of basic builder functionality.
 * 
 * @author Stephen Colebourne
 * @param <T> the bean type
 */
public class DirectBeanBuilder<T extends Bean> extends BasicBeanBuilder<T> {

    /**
     * Constructs the builder wrapping the target bean.
     * 
     * @param bean  the target bean, not null
     */
    public DirectBeanBuilder(T bean) {
        super(bean);
    }

    //-----------------------------------------------------------------------
    @Override
    protected void validate(T bean) {
        ((DirectMetaBean) bean.metaBean()).validate(bean);
    }

}
