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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.joda.beans.impl.map.MapBean;
import org.junit.Test;

/**
 * Test MapBean.
 */
public class TestMapBean {

    @Test
    public void test_clone() {
        MapBean a = new MapBean();
        a.put("A", "AA");
        a.put("B", "BB");
        MapBean b = a.clone();
        
        assertEquals(a.get("A"), "AA");
        assertEquals(a.get("B"), "BB");
        assertEquals(b.get("A"), "AA");
        assertEquals(b.get("B"), "BB");
        
        a.clear();
        
        assertEquals(a.get("A"), null);
        assertEquals(a.get("B"), null);
        assertEquals(b.get("A"), "AA");
        assertEquals(b.get("B"), "BB");
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_equalsHashCode() {
        MapBean a1 = new MapBean();
        MapBean a2 = new MapBean();
        MapBean b = new MapBean();
        
        a1.put("first", "A");
        a2.put("first", "A");
        b.put("first", "B");
        
        assertEquals(a1.equals(a1), true);
        assertEquals(a1.equals(a2), true);
        assertEquals(a2.equals(a1), true);
        assertEquals(a2.equals(a2), true);
        assertEquals(a1.hashCode(), a2.hashCode());
        
        assertEquals(a1.equals(b), false);
        assertEquals(b.equals(a1), false);
        
        assertEquals(b.equals("Weird type"), false);
        assertEquals(b.equals(null), false);
    }

    @Test
    public void test_propertyDefine_propertyRemove() {
        MapBean mapBean = new MapBean();
        assertEquals(mapBean.propertyNames().size(), 0);
        mapBean.propertyDefine("name", String.class);
        assertEquals(mapBean.propertyNames().size(), 1);
        Property<Object> prop = mapBean.property("name");
        assertEquals(prop.name(), "name");
        assertEquals(prop.get(), null);
        mapBean.propertyRemove("name");
        assertEquals(mapBean.propertyNames().size(), 0);
    }

    @Test
    public void test_metaBean() {
        MapBean mapBean = new MapBean();
        DynamicMetaBean meta = mapBean.metaBean();
        assertEquals(meta.metaPropertyCount(), 0);
        
        meta.metaPropertyDefine("name", String.class);
        assertEquals(meta.metaPropertyCount(), 1);
        MetaProperty<Object> prop = meta.metaProperty("name");
        assertEquals(prop.name(), "name");
        assertEquals(prop.get(mapBean), null);
        
        meta.metaPropertyDefine("name", String.class);
        assertEquals(meta.metaPropertyCount(), 1);
        
        MetaProperty<Object> prop2 = meta.metaProperty("address");
        assertNotNull(prop2);
        assertEquals(meta.metaPropertyCount(), 1);  // meta-property object created but data not changed
    }

}
