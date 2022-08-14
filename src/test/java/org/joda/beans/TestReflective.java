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
package org.joda.beans;

import static org.assertj.core.api.Assertions.assertThat;

import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.sample.ReflectiveMutable;
import org.joda.beans.ser.JodaBeanSer;
import org.junit.Test;

/**
 * Test {@code ReflectiveMetaBean}.
 */
public class TestReflective {

    @Test
    public void test_mutable() {
        ReflectiveMutable bean = (ReflectiveMutable) ReflectiveMutable.META_BEAN.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set(StandaloneMetaProperty.of("city", ReflectiveMutable.META_BEAN, String.class), "Smallville")
                .build();
        
        assertThat(bean.getNumber()).isEqualTo(12);
        assertThat(bean.getCity()).isEqualTo("Smallville");
        assertThat(bean.getStreet()).isEqualTo("Park Lane");
        
        bean.setCity("Nodnol");
        assertThat(bean.getCity()).isEqualTo("Nodnol");
        
        bean.property("city").set("Paris");
        assertThat(bean.getCity()).isEqualTo("Paris");
        
        bean.metaBean().metaProperty("city").set(bean, "London");
        assertThat(bean.getCity()).isEqualTo("London");
        
        assertThat(bean.metaBean().beanType()).isEqualTo(ReflectiveMutable.class);
        assertThat(bean.metaBean().metaPropertyCount()).isEqualTo(4);
        assertThat(bean.metaBean().metaPropertyExists("number")).isTrue();
        assertThat(bean.metaBean().metaPropertyExists("foobar")).isFalse();
        
        MetaProperty<Object> mp = bean.metaBean().metaProperty("number");
        assertThat(mp.propertyType()).isEqualTo(int.class);
        assertThat(mp.declaringType()).isEqualTo(ReflectiveMutable.class);
        assertThat(mp.get(bean)).isEqualTo(12);
        assertThat(mp.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        MetaProperty<Object> mp2 = bean.metaBean().metaProperty("street");
        assertThat(mp2.propertyType()).isEqualTo(String.class);
        assertThat(mp2.propertyGenericType()).isEqualTo(String.class);
        assertThat(mp2.declaringType()).isEqualTo(ReflectiveMutable.class);
        assertThat(mp2.get(bean)).isEqualTo("Park Lane");
        assertThat(mp2.style()).isEqualTo(PropertyStyle.READ_WRITE);
        
        assertThat(JodaBeanSer.PRETTY.xmlWriter().write(bean)).contains("<street>Park Lane<");
    }

}
