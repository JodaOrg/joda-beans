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
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.DefaultDeserializer;
import org.joda.beans.ser.SerDeserializer;

/**
 * Mock deserializer that handles a renamed property.
 *
 * @author Stephen Colebourne
 */
public class MockTypeChangeDeserializer extends DefaultDeserializer {

    /**
     * Singleton.
     */
    public static final SerDeserializer INSTANCE = new MockTypeChangeDeserializer();

    /**
     * The number of cars String property.
     */
    private MetaProperty<String> NUMBER_OF_CARS_STRING = StandaloneMetaProperty.of("numberOfCars", SimplePerson.meta(), String.class);

    /**
     * Creates an instance.
     */
    protected MockTypeChangeDeserializer() {
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName) {
        if ("numberOfCars".equals(propertyName)) {
            return NUMBER_OF_CARS_STRING;
        }
        return super.findMetaProperty(beanType, metaBean, propertyName);
    }

    @Override
    public void setValue(BeanBuilder<?> builder, MetaProperty<?> metaProp, Object value) {
      Object valueToSet = value;
        if (metaProp == NUMBER_OF_CARS_STRING && value != null) {
            String oldValue = value.toString();
            if (oldValue.equals("None")) {
              valueToSet = 0;
            } else if (oldValue.equals("One")) {
              valueToSet = 1;
            } else if (oldValue.equals("Two")) {
              valueToSet = 2;
            } else if (oldValue.equals("Lots")) {
              valueToSet = 3;
            }
        }
        super.setValue(builder, metaProp, valueToSet);
    }

}
