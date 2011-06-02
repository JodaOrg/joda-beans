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

    public void test_equalsHashCode_DirectBean() {
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

}
