/*
 *  Copyright 2001-2011 Stephen Colebourne
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

import java.util.List;
import java.util.Map;

import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

/**
 * Test BeanUtils.
 */
@Test
public class TestBeanUtils {

    public void test_propertiesEqual_propertiesHashCode() {
        Pair a = new Pair();
        a.setFirst("A");
        
        FlexiBean b = new FlexiBean();
        b.set("first", "A");
        assertEquals(BeanUtils.propertiesEqual(a, b), false);
        assertEquals(BeanUtils.propertiesEqual(b, a), false);
        
        b.set("second", null);
        assertEquals(BeanUtils.propertiesEqual(a, b), true);
        assertEquals(BeanUtils.propertiesEqual(b, a), true);
        assertEquals(BeanUtils.propertiesHashCode(a), BeanUtils.propertiesHashCode(b));
        
        b.set("second", "B");
        assertEquals(BeanUtils.propertiesEqual(a, b), false);
        assertEquals(BeanUtils.propertiesEqual(b, a), false);
        
        a.setSecond("B");
        assertEquals(BeanUtils.propertiesEqual(a, b), true);
        assertEquals(BeanUtils.propertiesEqual(b, a), true);
        assertEquals(BeanUtils.propertiesHashCode(a), BeanUtils.propertiesHashCode(b));
    }

    public void test_equal() {
        assertEquals(BeanUtils.equal("A", new Character('A').toString()), true);
        assertEquals(BeanUtils.equal("A", "B"), false);
        assertEquals(BeanUtils.equal("A", null), false);
        assertEquals(BeanUtils.equal(null, "A"), false);
    }

    public void test_hashCode_Object() {
        assertEquals(BeanUtils.hashCode("A"), "A".hashCode());
        assertEquals(BeanUtils.hashCode(null), 0);
    }

    public void test_listType_Person_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertEquals(BeanUtils.listType(test), Address.class);
    }

    public void test_listType_Person_addressesList() {
        MetaProperty<List<List<Address>>> test = Person.meta().addressesList();
        
        assertEquals(BeanUtils.listType(test), List.class);
    }

    public void test_mapType_Person_otherAddressMap() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        assertEquals(BeanUtils.mapKeyType(test), String.class);
        assertEquals(BeanUtils.mapValueType(test), Address.class);
    }

}
