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

import org.joda.beans.impl.map.MapBean;
import org.junit.jupiter.api.Test;

/**
 * Test {@link MapBean}.
 */
class TestMapBean {

    @Test
    void test_clone() {
        MapBean a = new MapBean();
        a.put("A", "AA");
        a.put("B", "BB");
        MapBean b = a.clone();
        
        assertThat(a.get("A")).isEqualTo("AA");
        assertThat(a.get("B")).isEqualTo("BB");
        assertThat(b.get("A")).isEqualTo("AA");
        assertThat(b.get("B")).isEqualTo("BB");
        
        a.clear();
        
        assertThat(a.get("A")).isNull();
        assertThat(a.get("B")).isNull();
        assertThat(b.get("A")).isEqualTo("AA");
        assertThat(b.get("B")).isEqualTo("BB");
    }

    @Test
    void test_equalsHashCode() {
        MapBean a1 = new MapBean();
        MapBean a2 = new MapBean();
        MapBean b = new MapBean();
        
        a1.put("first", "A");
        a2.put("first", "A");
        b.put("first", "B");
        
        assertThat(a1.equals(a1)).isTrue();
        assertThat(a1.equals(a2)).isTrue();
        assertThat(a2.equals(a1)).isTrue();
        assertThat(a2.equals(a2)).isTrue();
        assertThat(a1.hashCode()).isEqualTo(a2.hashCode());
        
        assertThat(a1.equals(b)).isFalse();
        assertThat(b.equals(a1)).isFalse();
        
        Object obj = "Weird type";
        assertThat(b.equals(obj)).isFalse();
        assertThat(b.equals(null)).isFalse();
    }

    @Test
    void test_propertyDefine_propertyRemove() {
        MapBean mapBean = new MapBean();
        assertThat(mapBean.propertyNames().size()).isEqualTo(0);
        mapBean.propertyDefine("name", String.class);
        assertThat(mapBean.propertyNames().size()).isEqualTo(1);
        Property<Object> prop = mapBean.property("name");
        assertThat(prop.name()).isEqualTo("name");
        assertThat(prop.get()).isNull();
        mapBean.propertyRemove("name");
        assertThat(mapBean.propertyNames().size()).isEqualTo(0);
    }

    @Test
    void test_metaBean() {
        MapBean mapBean = new MapBean();
        DynamicMetaBean meta = mapBean.metaBean();
        assertThat(meta.metaPropertyCount()).isEqualTo(0);
        
        meta.metaPropertyDefine("name", String.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(1);
        MetaProperty<Object> prop = meta.metaProperty("name");
        assertThat(prop.name()).isEqualTo("name");
        assertThat(prop.get(mapBean)).isNull();
        
        meta.metaPropertyDefine("name", String.class);
        assertThat(meta.metaPropertyCount()).isEqualTo(1);
        
        MetaProperty<Object> prop2 = meta.metaProperty("address");
        assertThat(prop2).isNotNull();
        assertThat(meta.metaPropertyCount()).isEqualTo(1); // meta-property object created but data not changed
    }

}
