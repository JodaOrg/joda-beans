/*
 *  Copyright 2001-2013 Stephen Colebourne
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

/**
 * Test FlexiBean.
 */
@Test
public class TestFlexiBean {

    public void test_equalsHashCode() {
        FlexiBean a1 = new FlexiBean();
        FlexiBean a2 = new FlexiBean();
        FlexiBean b = new FlexiBean();
        
        a1.set("first", "A");
        a2.set("first", "A");
        b.set("first", "B");
        
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

    public void test_propertyDefine_propertyRemove() {
        FlexiBean flexi = new FlexiBean();
        assertEquals(flexi.propertyNames().size(), 0);
        flexi.propertyDefine("name", String.class);
        assertEquals(flexi.propertyNames().size(), 1);
        Property<Object> prop = flexi.property("name");
        assertEquals(prop.name(), "name");
        assertEquals(prop.get(), null);
        flexi.propertyRemove("name");
        assertEquals(flexi.propertyNames().size(), 0);
    }

    public void test_metaBean() {
        FlexiBean flexi = new FlexiBean();
        DynamicMetaBean meta = flexi.metaBean();
        assertEquals(meta.metaPropertyCount(), 0);
        
        meta.metaPropertyDefine("name", String.class);
        assertEquals(meta.metaPropertyCount(), 1);
        MetaProperty<Object> prop = meta.metaProperty("name");
        assertEquals(prop.name(), "name");
        assertEquals(prop.get(flexi), null);
        
        meta.metaPropertyDefine("name", String.class);
        assertEquals(meta.metaPropertyCount(), 1);
        
        MetaProperty<Object> prop2 = meta.metaProperty("address");
        assertNotNull(prop2);
        assertEquals(meta.metaPropertyCount(), 1);  // meta-property object created but data not changed
    }

}
