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
package org.joda.beans.ser.xml;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.impl.BufferingBeanBuilder;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.DefaultDeserializer;
import org.joda.beans.ser.SerDeserializer;

/**
 * Mock deserializer that handles a semantic change.
 *
 * @author Stephen Colebourne
 */
public class MockSemanticChangeDeserializer extends DefaultDeserializer {

    /**
     * Singleton.
     */
    public static final SerDeserializer INSTANCE = new MockSemanticChangeDeserializer();

    /**
     * Creates an instance.
     */
    protected MockSemanticChangeDeserializer() {
    }

    //-----------------------------------------------------------------------
    @Override
    public BeanBuilder<?> createBuilder(Class<?> beanType, MetaBean metaBean) {
        return BufferingBeanBuilder.of(metaBean);
    }

    @Override
    public Object build(Class<?> beanType, BeanBuilder<?> builder) {
        BufferingBeanBuilder<?> bld = (BufferingBeanBuilder<?>) builder;
        if ("Stephen".equals(bld.getBuffer().get(SimplePerson.meta().forename())) &&
                "Colebourne".equals(bld.getBuffer().get(SimplePerson.meta().surname()))) {
            bld.set(SimplePerson.meta().forename(), "Steve");
        }
        return bld.build();
    }

}
