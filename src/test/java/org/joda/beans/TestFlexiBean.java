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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.joda.beans.impl.flexi.FlexiBean;
import org.junit.Test;

/**
 * Test FlexiBean.
 */
public class TestFlexiBean {

    @Test
    public void test_serialization() throws Exception {
        FlexiBean test = new FlexiBean();
        test.put("name", "Etienne");
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(test);
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray())) {
            try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                Object obj = ois.readObject();
                assertThat(test).isEqualTo(obj);
            }
        }
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_equalsHashCode() {
        FlexiBean a1 = new FlexiBean();
        FlexiBean a2 = new FlexiBean();
        FlexiBean b = new FlexiBean();
        
        a1.set("first", "A");
        a2.set("first", "A");
        b.set("first", "B");
        
        assertThat(a1.equals(a1)).isTrue();
        assertThat(a1.equals(a2)).isTrue();
        assertThat(a2.equals(a1)).isTrue();
        assertThat(a2.equals(a2)).isTrue();
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        
        assertThat(a1.equals(b)).isFalse();
        assertThat(b.equals(a1)).isFalse();
        
        assertThat(b.equals("Weird type")).isFalse();
        assertThat(b.equals(null)).isFalse();
    }

    @Test
    public void test_propertyDefine_propertyRemove() {
        FlexiBean flexi = new FlexiBean();
        assertThat(flexi.propertyNames().size()).isEqualTo(0);
        flexi.propertyDefine("name", String.class);
        assertThat(flexi.propertyNames().size()).isEqualTo(1);
        Property<Object> prop = flexi.property("name");
        assertThat(prop.name()).isEqualTo("name");
        assertThat(prop.get()).isNull();
        flexi.propertyRemove("name");
        assertThat(flexi.propertyNames().size()).isEqualTo(0);
    }

    @Test
    public void test_metaBean() {
        FlexiBean flexi = new FlexiBean();
        DynamicMetaBean meta = flexi.metaBean();
        assertThat(meta.metaPropertyCount()).isEqualTo(0);
        
        meta.metaPropertyDefine("name", String.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(1);
        MetaProperty<Object> prop = meta.metaProperty("name");
        assertThat(prop.name()).isEqualTo("name");
        assertThat(prop.get(flexi)).isNull();
        
        meta.metaPropertyDefine("name", String.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(1);
        
        MetaProperty<Object> prop2 = meta.metaProperty("address");
        assertNotNull(prop2);
        assertThat(meta.metaPropertyCount()).isEqualTo(1); // meta-property object created but data not changed
    }

    @Test
    public void test_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.propertyDefine("bad-name", String.class));
    }

    @Test
    public void test_append_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.append("bad-name", "a"));
    }

    @Test
    public void test_set_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.set("bad-name", "a"));
    }

    @Test
    public void test_put_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.put("bad-name", "a"));
    }

}
