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
import static org.assertj.core.api.Assertions.offset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.impl.flexi.FlexiBean;
import org.junit.jupiter.api.Test;

/**
 * Test {@link FlexiBean}.
 */
class TestFlexiBean {

    @Test
    void test_constructor() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
    }

    @Test
    void test_constructor_copy() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = new FlexiBean(base);
        assertThat(test).isNotSameAs(base);
        assertThat(test).isEqualTo(base);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_serialization() throws Exception {
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

    //-------------------------------------------------------------------------
    @Test
    void test_clone() {
        FlexiBean base = new FlexiBean();
        base.set("a", "x");
        base.set("b", "y");
        FlexiBean test = base.clone();
        assertThat(test).isNotSameAs(base);
        assertThat(test).isEqualTo(base);
    }

    @Test
    void test_equalsHashCode() {
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

        Object obj = "Weird type";
        assertThat(b.equals(obj)).isFalse();
        assertThat(b.equals(null)).isFalse();
    }

    @Test
    void test_toString() {
        FlexiBean test = new FlexiBean();
        test.set("a", "b");
        assertThat(test.toString()).isEqualTo("FlexiBean{a=b}");
    }

    //-------------------------------------------------------------------------
    @Test
    void test_basics() {
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
    void test_type_string() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", "x");
        assertThat(test.get("a")).isEqualTo("x");
        assertThat(test.get("a", String.class)).isEqualTo("x");
        assertThat(test.getString("a")).isEqualTo("x");
        assertThat(test.getString("b")).isNull();
    }

    @Test
    void test_type_long() {
        final long LONG_TEST_VALUE = 2L;
        final long LONG_DEFAULT_VALUE = 1L;

        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Long.valueOf(LONG_TEST_VALUE));
        assertThat(test.get("a")).isEqualTo(Long.valueOf(LONG_TEST_VALUE));
        assertThat(test.get("a", Long.class)).isEqualTo(Long.valueOf(LONG_TEST_VALUE));
        assertThat(test.getLong("a")).isEqualTo(LONG_TEST_VALUE);
        assertThat(test.getLong("a", LONG_DEFAULT_VALUE)).isEqualTo(LONG_TEST_VALUE);
        assertThat(test.getLong("b", LONG_DEFAULT_VALUE)).isEqualTo(LONG_DEFAULT_VALUE);
    }

    @Test
    void test_type_int() {
        final int INT_TEST_VALUE = 2;
        final int INT_DEFAULT_VALUE = 1;

        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Integer.valueOf(INT_TEST_VALUE));
        assertThat(test.get("a")).isEqualTo(Integer.valueOf(INT_TEST_VALUE));
        assertThat(test.get("a", Integer.class)).isEqualTo(Integer.valueOf(INT_TEST_VALUE));
        assertThat(test.getInt("a")).isEqualTo(INT_TEST_VALUE);
        assertThat(test.getInt("a", INT_DEFAULT_VALUE)).isEqualTo(INT_TEST_VALUE);
        assertThat(test.getInt("b", INT_DEFAULT_VALUE)).isEqualTo(INT_DEFAULT_VALUE);
    }

    @Test
    void test_type_double() {
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
    void test_type_boolean() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.set("a", Boolean.TRUE);
        assertThat(test.get("a")).isEqualTo(Boolean.TRUE);
        assertThat(test.get("a", Boolean.class)).isEqualTo(Boolean.TRUE);
        assertThat(test.getBoolean("a")).isTrue();
    }

    //-------------------------------------------------------------------------
    @Test
    void test_propertyDefine_propertyRemove() {
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
    void test_metaBean() {
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
        assertThat(prop2).isNotNull();
        assertThat(meta.metaPropertyCount()).isEqualTo(1); // meta-property object created but data not changed
    }

    //-----------------------------------------------------------------------
    @Test
    void test_putAll() {
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
    void test_remove() {
        FlexiBean test = new FlexiBean();
        assertThat(test.size()).isEqualTo(0);
        test.remove("a");
        assertThat(test.size()).isEqualTo(0);
        test.put("a", "x");
        test.remove("a");
        assertThat(test.size()).isEqualTo(0);
    }

    @Test
    void test_toMap() {
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

    //-------------------------------------------------------------------------
    @Test
    void test_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.propertyDefine("bad-name", String.class));
    }

    @Test
    void test_append_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.append("bad-name", "a"));
    }

    @Test
    void test_set_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.set("bad-name", "a"));
    }

    @Test
    void test_put_invalidProperty() {
        FlexiBean flexi = new FlexiBean();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> flexi.put("bad-name", "a"));
    }

    @Test
    void test_putAll_invalidKey() {
        FlexiBean test = new FlexiBean();
        Map<String, Object> map = new HashMap<>();
        map.put("1", "x");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> test.putAll(map));
    }

}
