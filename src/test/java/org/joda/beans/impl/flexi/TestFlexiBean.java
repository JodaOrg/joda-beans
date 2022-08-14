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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Test FlexiBean.
 */
public class TestFlexiBean {

    @Test
    public void test_constructor() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
    }

    @Test
    public void test_constructor_copy() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = new FlexiBean(base);
        assertThat(test).isNotSameAs(base);
        assertThat(test).isEqualTo(base);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_basics() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        assertThat(test.contains("a")).isFalse();
        assertThat(test.contains("b")).isFalse();
        assertThat(test.get("a")).isNull();
        assertThat(test.get("b")).isNull();
        
        test.set("a", "x");
        assertThat(test.size()).isEqualTo(1);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isFalse();
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("b")).isNull();
        
        test.set("b", "y");
        assertThat(test.size()).isEqualTo(2);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isTrue();
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("b")).isEqualTo("y");
        
        test.set("b", "z");
        assertThat(test.size()).isEqualTo(2);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isTrue();
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("b")).isEqualTo("z");
        
        test.remove("b");
        assertThat(test.size()).isEqualTo(1);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isFalse();
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("b")).isNull();
    }

    @Test
    public void test_type_string() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", "x");
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("a", String.class)).isEqualTo("x");
        assertThat(test.getString("a")).isEqualTo("x");
        assertThat(test.getString("b")).isNull();
    }

    @Test
    public void test_type_long() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Long.valueOf(2));
        assertThat(test.get("a")).isEqualTo(Long.valueOf(2));
        assertThat(test.get("a", Long.class)).isEqualTo(Long.valueOf(2));
        assertThat(test.getLong("a")).isEqualTo(2L);
        assertThat(test.getLong("a", 1L)).isEqualTo(2);
        assertThat(test.getLong("b", 1L)).isEqualTo(1);
    }

    @Test
    public void test_type_int() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Integer.valueOf(2));
        assertThat(test.get("a")).isEqualTo(Integer.valueOf(2));
        assertThat(test.get("a", Integer.class)).isEqualTo(Integer.valueOf(2));
        assertThat(test.getInt("a")).isEqualTo(2);
        assertThat(test.getInt("a", 1)).isEqualTo(2);
        assertThat(test.getInt("b", 1)).isEqualTo(1);
    }

    @Test
    public void test_type_double() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Double.valueOf(1.2d));
        assertThat(test.get("a")).isEqualTo(Double.valueOf(1.2d));
        assertThat(test.get("a", Double.class)).isEqualTo(Double.valueOf(1.2d));
        assertThat(test.getDouble("a")).isEqualTo(1.2d);
        assertThat(test.getDouble("a", 0.5d)).isEqualTo(1.2d);
        assertThat(test.getDouble("b", 0.5d)).isEqualTo(0.5d, offset(0.0001d));
    }

    @Test
    public void test_type_boolean() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Boolean.TRUE);
        assertThat(test.get("a")).isEqualTo(Boolean.TRUE);
        assertThat(test.get("a", Boolean.class)).isEqualTo(Boolean.TRUE);
        assertThat(test.getBoolean("a")).isTrue();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_putAll() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        Map<String, Object> map = new HashMap<>();
        test.putAll(map);
        assertThat(test.size()).isEqualTo(0);
        map.put("a", "x");
        map.put("b", "y");
        test.putAll(map);
        assertThat(test.size()).isEqualTo(2);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isTrue();
        map.clear();
        map.put("c", "z");
        test.putAll(map);
        assertThat(test.size()).isEqualTo(3);
        assertThat(test.contains("a")).isTrue();
        assertThat(test.contains("b")).isTrue();
        assertThat(test.contains("c")).isTrue();
    }

    @Test
    public void test_remove() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.remove("a");
        assertThat(test.size()).isEqualTo(0);
        test.put("a", "x");
        test.remove("a");
        assertThat(test.size()).isEqualTo(0);
    }

    @Test
    public void test_toMap() {
        FlexiBean base = new FlexiBean();
        Map<String, Object> test = base.toMap();
        assertThat(test.size()).isEqualTo(0);
        base.put("a", "x");
        base.put("b", "y");
        test = base.toMap();
        assertThat(test.size()).isEqualTo(2);
        assertThat(test.containsKey("a")).isTrue();
        assertThat(test.containsKey("b")).isTrue();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_clone() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = base.clone();
        assertThat(test).isNotSameAs(base);
        assertThat(test).isEqualTo(base);
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
    public void test_toString() {
        FlexiBean test = new FlexiBean();
        test.set("a", "b");
        assertThat(test.toString()).isEqualTo("FlexiBean{a=b}");
    }

}
