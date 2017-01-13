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

import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.ser.DefaultDeserializer;
import org.joda.beans.ser.SerDeserializer;

/**
 * Mock deserializer that handles a renamed property.
 *
 * @author Stephen Colebourne
 */
public class MockRenameDeserializer extends DefaultDeserializer {

    /**
     * Singleton.
     */
    public static final SerDeserializer INSTANCE = new MockRenameDeserializer();

    /**
     * Creates an instance.
     */
    protected MockRenameDeserializer() {
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName) {
        if ("firstName".equals(propertyName)) {
            return metaBean.metaProperty("forename");
        }
        return super.findMetaProperty(beanType, metaBean, propertyName);
    }

}
