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
package org.joda.beans.impl.flexi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Test FlexiBean.
 */
public class TestFlexiBean {

    @Test
    public void test_constructor() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
    }

    @Test
    public void test_constructor_copy() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = new FlexiBean(base);
        assertNotSame(test, base);
        assertEquals(test, base);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_basics() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        assertEquals(test.contains("a"), false);
        assertEquals(test.contains("b"), false);
        assertEquals(test.get("a"), null);
        assertEquals(test.get("b"), null);
        
        test.set("a", "x");
        assertEquals(test.size(), 1);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), false);
        assertEquals(test.get("a"), "x");
        assertEquals(test.get("b"), null);
        
        test.set("b", "y");
        assertEquals(test.size(), 2);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), true);
        assertEquals(test.get("a"), "x");
        assertEquals(test.get("b"), "y");
        
        test.set("b", "z");
        assertEquals(test.size(), 2);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), true);
        assertEquals(test.get("a"), "x");
        assertEquals(test.get("b"), "z");
        
        test.remove("b");
        assertEquals(test.size(), 1);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), false);
        assertEquals(test.get("a"), "x");
        assertEquals(test.get("b"), null);
    }

    @Test
    public void test_type_string() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.set("a", "x");
        assertEquals(test.get("a"), "x");
        assertEquals(test.get("a", String.class), "x");
        assertEquals(test.getString("a"), "x");
        assertEquals(test.getString("b"), null);
    }

    @Test
    public void test_type_long() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.set("a", Long.valueOf(2));
        assertEquals(test.get("a"), Long.valueOf(2));
        assertEquals(test.get("a", Long.class), Long.valueOf(2));
        assertEquals(test.getLong("a"), 2L);
        assertEquals(test.getLong("a", 1L), 2);
        assertEquals(test.getLong("b", 1L), 1);
    }

    @Test
    public void test_type_int() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.set("a", Integer.valueOf(2));
        assertEquals(test.get("a"), Integer.valueOf(2));
        assertEquals(test.get("a", Integer.class), Integer.valueOf(2));
        assertEquals(test.getInt("a"), 2);
        assertEquals(test.getInt("a", 1), 2);
        assertEquals(test.getInt("b", 1), 1);
    }

    @Test
    public void test_type_double() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.set("a", Double.valueOf(1.2d));
        assertEquals(test.get("a"), Double.valueOf(1.2d));
        assertEquals(test.get("a", Double.class), Double.valueOf(1.2d));
        assertEquals(test.getDouble("a"), 1.2d, 0.0001d);
        assertEquals(test.getDouble("a", 0.5d), 1.2d, 0.0001d);
        assertEquals(test.getDouble("b", 0.5d), 0.5d, 0.0001d);
    }

    @Test
    public void test_type_boolean() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.set("a", Boolean.TRUE);
        assertEquals(test.get("a"), Boolean.TRUE);
        assertEquals(test.get("a", Boolean.class), Boolean.TRUE);
        assertEquals(test.getBoolean("a"), true);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_putAll() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        Map<String, Object> map = new HashMap<>();
        test.putAll(map);
        assertEquals(test.size(), 0);
        map.put("a", "x");
        map.put("b", "y");
        test.putAll(map);
        assertEquals(test.size(), 2);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), true);
        map.clear();
        map.put("c", "z");
        test.putAll(map);
        assertEquals(test.size(), 3);
        assertEquals(test.contains("a"), true);
        assertEquals(test.contains("b"), true);
        assertEquals(test.contains("c"), true);
    }

    @Test
    public void test_remove() {
        FlexiBean test = new FlexiBean();
        assertEquals(test.size(), 0);
        test.remove("a");
        assertEquals(test.size(), 0);
        test.put("a", "x");
        test.remove("a");
        assertEquals(test.size(), 0);
    }

    @Test
    public void test_toMap() {
        FlexiBean base = new FlexiBean();
        Map<String, Object> test = base.toMap();
        assertEquals(test.size(), 0);
        base.put("a", "x");
        base.put("b", "y");
        test = base.toMap();
        assertEquals(test.size(), 2);
        assertEquals(test.containsKey("a"), true);
        assertEquals(test.containsKey("b"), true);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_clone() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = base.clone();
        assertNotSame(test, base);
        assertEquals(test, base);
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_equalsHashCode() {
        FlexiBean a1 = new FlexiBean();
        a1.set("a", "b");
        FlexiBean a2 = new FlexiBean();
        a2.set("a", "b");
        FlexiBean b = new FlexiBean();
        b.set("a", "c");
        
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
    public void test_toString() {
        FlexiBean test = new FlexiBean();
        test.set("a", "b");
        assertEquals(test.toString(), "FlexiBean{a=b}");
    }

}
