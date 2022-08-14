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
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.Pair;
import org.joda.beans.sample.Person;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;

/**
 * Test utils.
 */
public class TestJodaBeanUtils {

    //-----------------------------------------------------------------------
    @Test
    public void test_notNull_ok() {
        JodaBeanUtils.notNull("", "name");
    }

    @Test
    public void test_notNull_notOk() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notNull(null, "name"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_notBlank_String_ok() {
        JodaBeanUtils.notBlank("Blah", "name");
    }

    @Test
    public void test_notBlank_String_notOk_empty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notBlank("", "name"));
    }

    @Test
    public void test_notBlank_String_notOk_allWhitespace() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notBlank(" ", "name"));
    }

    @Test
    public void test_notBlank_String_notOk_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notBlank((String) null, "name"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_notEmpty_String_ok() {
        JodaBeanUtils.notEmpty("Blah", "name");
    }

    @Test
    public void test_notEmpty_String_notOk_empty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notBlank("", "name"));
    }

    @Test
    public void test_notEmpty_String_notOk_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notEmpty((String) null, "name"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_notEmpty_Collection_ok() {
        JodaBeanUtils.notEmpty(Arrays.asList("Blah"), "name");
    }

    @Test
    public void test_notEmpty_Collection_notOk_empty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notEmpty(new ArrayList<String>(), "name"));
    }

    @Test
    public void test_notEmpty_Collection_notOk_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notEmpty((Collection<?>) null, "name"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_notEmpty_Map_ok() {
        Map<String, String> map = new HashMap<>();
        map.put("A", "B");
        JodaBeanUtils.notEmpty(map, "name");
    }

    @Test
    public void test_notEmpty_Map_notOk_empty() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notEmpty(new HashMap<String, String>(), "name"));
    }

    @Test
    public void test_notEmpty_Map_notOk_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.notEmpty((Map<?, ?>) null, "name"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_propertiesEqual_propertiesHashCode() {
        Pair a = new Pair();
        a.setFirst("A");
        
        FlexiBean b = new FlexiBean();
        b.set("first", "A");
        assertThat(JodaBeanUtils.propertiesEqual(a, b)).isFalse();
        assertThat(JodaBeanUtils.propertiesEqual(b, a)).isFalse();
        
        b.set("second", null);
        assertThat(JodaBeanUtils.propertiesEqual(a, b)).isTrue();
        assertThat(JodaBeanUtils.propertiesEqual(b, a)).isTrue();
        assertThat(JodaBeanUtils.propertiesHashCode(a)).isEqualTo(JodaBeanUtils.propertiesHashCode(b));
        
        b.set("second", "B");
        assertThat(JodaBeanUtils.propertiesEqual(a, b)).isFalse();
        assertThat(JodaBeanUtils.propertiesEqual(b, a)).isFalse();
        
        a.setSecond("B");
        assertThat(JodaBeanUtils.propertiesEqual(a, b)).isTrue();
        assertThat(JodaBeanUtils.propertiesEqual(b, a)).isTrue();
        assertThat(JodaBeanUtils.propertiesHashCode(a)).isEqualTo(JodaBeanUtils.propertiesHashCode(b));
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_equal() {
        assertThat(JodaBeanUtils.equal("A", "AA".substring(0, 1))).isTrue();
        assertThat(JodaBeanUtils.equal("A", "B")).isFalse();
        assertThat(JodaBeanUtils.equal("A", null)).isFalse();
        assertThat(JodaBeanUtils.equal(null, "A")).isFalse();
    }

    @Test
    public void test_equal_ObjectArray() {
        Object[] a1 = new Object[] {1, 2, 3};
        Object[] a2 = new Object[] {1, 2, 3};
        Object[] b = new Object[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_IntegerArray() {
        Object[] a1 = new Integer[] {1, 2, 3};
        Object[] a2 = new Integer[] {1, 2, 3};
        Object[] b = new Integer[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_IntegerNumberArray() {
        Object[] a = new Integer[] {1, 2, 3};
        Object[] b = new Number[] {1, 2, 3};
        assertThat(JodaBeanUtils.equal(a, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a)).isFalse();
    }

    @Test
    public void test_equal_IntegerIntArray() {
        Object[] a = new Integer[] {1, 2, 3};
        int[] b = new int[] {1, 2, 3};
        assertThat(JodaBeanUtils.equal(a, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a)).isFalse();
    }

    @Test
    public void test_equal_IntArray() {
        int[] a1 = new int[] {1, 2, 3};
        int[] a2 = new int[] {1, 2, 3};
        int[] b = new int[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_MixedArrays() {
        byte[] b = new byte[] {1, 2, 3};
        short[] s = new short[] {1, 2, 3};
        int[] i = new int[] {1, 2, 3};
        long[] l = new long[] {1, 2, 4};
        boolean[] bl = new boolean[] {true, false, true};
        char[] c = new char[] {'1', '2', '3'};
        float[] f = new float[] {1f, 2f, 3f};
        double[] d = new double[] {1d, 2d, 3d};
        assertThat(JodaBeanUtils.equal(b, i)).isFalse();
        assertThat(JodaBeanUtils.equal(s, i)).isFalse();
        assertThat(JodaBeanUtils.equal(i, l)).isFalse();
        assertThat(JodaBeanUtils.equal(l, i)).isFalse();
        assertThat(JodaBeanUtils.equal(bl, i)).isFalse();
        assertThat(JodaBeanUtils.equal(c, i)).isFalse();
        assertThat(JodaBeanUtils.equal(f, i)).isFalse();
        assertThat(JodaBeanUtils.equal(d, i)).isFalse();
    }

    @Test
    public void test_equal_LongArray() {
        long[] a1 = new long[] {1, 2, 3};
        long[] a2 = new long[] {1, 2, 3};
        long[] b = new long[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_DoubleArray() {
        double[] a1 = new double[] {1, 2, 3};
        double[] a2 = new double[] {1, 2, 3};
        double[] b = new double[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_FloatArray() {
        float[] a1 = new float[] {1, 2, 3};
        float[] a2 = new float[] {1, 2, 3};
        float[] b = new float[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_ShortArray() {
        short[] a1 = new short[] {1, 2, 3};
        short[] a2 = new short[] {1, 2, 3};
        short[] b = new short[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_CharArray() {
        char[] a1 = new char[] {1, 2, 3};
        char[] a2 = new char[] {1, 2, 3};
        char[] b = new char[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_ByteArray() {
        byte[] a1 = new byte[] {1, 2, 3};
        byte[] a2 = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2, 4};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    @Test
    public void test_equal_BooleanArray() {
        boolean[] a1 = new boolean[] {true, false};
        boolean[] a2 = new boolean[] {true, false};
        boolean[] b = new boolean[] {true, true};
        assertThat(JodaBeanUtils.equal(a1, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, a2)).isTrue();
        assertThat(JodaBeanUtils.equal(a2, a1)).isTrue();
        assertThat(JodaBeanUtils.equal(a1, b)).isFalse();
        assertThat(JodaBeanUtils.equal(b, a1)).isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_equal_floats() {
        assertThat(JodaBeanUtils.equal(1.01f, 1.01f)).isTrue();
        assertThat(JodaBeanUtils.equal(1.0f, 1.2f)).isFalse();
    }

    @Test
    public void test_equalWithTolerance_floats_zeroTolerance() {
        double tolerance = 0d;
        assertThat(JodaBeanUtils.equalWithTolerance(1.01f, 1.01f, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 1.2f, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.NaN, Float.NaN, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.NaN, 1f, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, Float.NaN, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.POSITIVE_INFINITY, Float.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.MAX_VALUE, Float.POSITIVE_INFINITY, tolerance))
                .isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY, tolerance))
                .isFalse();
    }

    @Test
    public void test_equalWithTolerance_floats_someTolerance() {
        double tolerance = 0.125d;
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 1.250001f, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 1.125f, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 1.124999f, tolerance)).isTrue();

        assertThat(JodaBeanUtils.equalWithTolerance(1f, 0.875001f, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 0.875f, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, 0.874999f, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.NaN, Float.NaN, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.NaN, 1f, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1f, Float.NaN, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.POSITIVE_INFINITY, Float.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.MAX_VALUE, Float.POSITIVE_INFINITY, tolerance))
                .isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY, 0d))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Float.NEGATIVE_INFINITY, -Float.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(-Float.MAX_VALUE, Float.NEGATIVE_INFINITY, tolerance))
                .isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_equal_doubles() {
        assertThat(JodaBeanUtils.equal(1.01d, 1.01d)).isTrue();
        assertThat(JodaBeanUtils.equal(1.0d, 1.2d)).isFalse();

        assertThat(JodaBeanUtils.equal(Double.NaN, Double.NaN)).isTrue();
        assertThat(JodaBeanUtils.equal(Double.NaN, 1d)).isFalse();
        assertThat(JodaBeanUtils.equal(1d, Double.NaN)).isFalse();

        assertThat(JodaBeanUtils.equal(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isTrue();
        assertThat(JodaBeanUtils.equal(Double.POSITIVE_INFINITY, Double.MAX_VALUE)).isFalse();
        assertThat(JodaBeanUtils.equal(Double.MAX_VALUE, Double.POSITIVE_INFINITY)).isFalse();

        assertThat(JodaBeanUtils.equal(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)).isTrue();
        assertThat(JodaBeanUtils.equal(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE)).isFalse();
        assertThat(JodaBeanUtils.equal(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY)).isFalse();
    }

    @Test
    public void test_equalWithTolerance_doubles_zeroTolerance() {
        double tolerance = 0d;
        assertThat(JodaBeanUtils.equalWithTolerance(1.01d, 1.01d, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 1.2d, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.NaN, Double.NaN, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.NaN, 1d, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, Double.NaN, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.POSITIVE_INFINITY, Double.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.MAX_VALUE, Double.POSITIVE_INFINITY, tolerance))
                .isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, tolerance))
                .isFalse();
    }

    @Test
    public void test_equalWithTolerance_doubles_someTolerance() {
        double tolerance = 0.125d;
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 1.250001d, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 1.125d, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 1.124999d, tolerance)).isTrue();

        assertThat(JodaBeanUtils.equalWithTolerance(1d, 0.875001d, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 0.875d, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, 0.874999d, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.NaN, Double.NaN, tolerance)).isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.NaN, 1d, tolerance)).isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(1d, Double.NaN, tolerance)).isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, tolerance))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.POSITIVE_INFINITY, Double.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.MAX_VALUE, Double.POSITIVE_INFINITY, tolerance))
                .isFalse();

        assertThat(JodaBeanUtils.equalWithTolerance(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0d))
                .isTrue();
        assertThat(JodaBeanUtils.equalWithTolerance(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE, tolerance))
                .isFalse();
        assertThat(JodaBeanUtils.equalWithTolerance(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, tolerance))
                .isFalse();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_hashCode_Object() {
        assertThat(JodaBeanUtils.hashCode("A")).isEqualTo("A".hashCode());
        assertThat(JodaBeanUtils.hashCode(null)).isEqualTo(0);
        assertThat(JodaBeanUtils.hashCode(new byte[] { 1 })).isEqualTo(Arrays.hashCode(new byte[] { 1 }));
        assertThat(JodaBeanUtils.hashCode(new short[] { 1 })).isEqualTo(Arrays.hashCode(new short[] { 1 }));
        assertThat(JodaBeanUtils.hashCode(new int[] { 1 })).isEqualTo(Arrays.hashCode(new int[] { 1 }));
        assertThat(JodaBeanUtils.hashCode(new long[] { 1 })).isEqualTo(Arrays.hashCode(new long[] { 1 }));
        assertThat(JodaBeanUtils.hashCode(new boolean[] { true })).isEqualTo(Arrays.hashCode(new boolean[] { true }));
        assertThat(JodaBeanUtils.hashCode(new char[] { '1' })).isEqualTo(Arrays.hashCode(new char[] { '1' }));
        assertThat(JodaBeanUtils.hashCode(new float[] { 1f })).isEqualTo(Arrays.hashCode(new float[] { 1f }));
        assertThat(JodaBeanUtils.hashCode(new double[] { 1d })).isEqualTo(Arrays.hashCode(new double[] { 1d }));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_copy() {
        Person p = new Person();
        p.setForename("Stephen");
        p.setSurname("Colebourne");
        p.setExtensions(new FlexiBean());
        p.getExtensions().set("Foo", "bar");
        ImmPerson copied = JodaBeanUtils.copy(p, ImmPerson.class).build();
        assertNotSame(copied, p);
        assertThat(copied.getForename()).isEqualTo(p.getForename());
        assertThat(copied.getSurname()).isEqualTo(p.getSurname());
    }

    @Test
    public void test_copyInto() {
        Person p = new Person();
        p.setForename("Stephen");
        p.setExtensions(new FlexiBean());
        p.getExtensions().set("Foo", "bar");
        ImmPerson copied = JodaBeanUtils.copyInto(p, ImmPerson.meta(), ImmPerson.builder()).build();
        assertNotSame(copied, p);
        assertThat(copied.getForename()).isEqualTo(p.getForename());
        assertNull(copied.getSurname());
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_clone() {
        Person p = new Person();
        p.setForename("Stephen");
        p.setSurname("Colebourne");
        p.getOtherAddressMap().put("A", new Address());
        p.getOtherAddressMap().get("A").setCity("London");
        Person cloned = JodaBeanUtils.clone(p);
        assertThat(cloned).isNotSameAs(p);
        assertThat(cloned).isEqualTo(p);
        p.getOtherAddressMap().put("B", new Address());
        assertThat(cloned).isNotEqualTo(p);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_listType_Person_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertThat(JodaBeanUtils.collectionType(test, Person.class)).isEqualTo(Address.class);
    }

    @Test
    public void test_listType_Person_addressesList() {
        MetaProperty<List<List<Address>>> test = Person.meta().addressesList();
        
        assertThat(JodaBeanUtils.collectionType(test, Person.class)).isEqualTo(List.class);
    }

    @Test
    public void test_multisetType_Person_otherAddressMap() {
        MetaProperty<ImmutableMultiset<String>> test = ImmPerson.meta().codeCounts();
        
        assertThat(JodaBeanUtils.collectionType(test, Person.class)).isEqualTo(String.class);
    }

    @Test
    public void test_integerType_Person_collectionTypeInvalid() {
        MetaProperty<Integer> test = ImmPerson.meta().age();
        
        assertThat(JodaBeanUtils.collectionType(test, Person.class)).isNull();
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_collectionTypeTypes_valid() {
        MetaProperty<List<List<Address>>> test = Person.meta().addressesList();
        
        List<Class<?>> expected = ImmutableList.<Class<?>>of(Address.class);
        assertThat(JodaBeanUtils.collectionTypeTypes(test, Person.class)).isEqualTo(expected);
    }

    @Test
    public void test_collectionTypeTypes_invalidNoGenerics() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        List<Class<?>> expected = Collections.emptyList();
        assertThat(JodaBeanUtils.collectionTypeTypes(test, Person.class)).isEqualTo(expected);
    }

    @Test
    public void test_collectionTypeTypes_invalidNotCollection() {
        MetaProperty<ImmutableMap<String, List<Integer>>> test = ImmAddress.meta().listNumericInMap();
        
        List<Class<?>> expected = Collections.emptyList();
        assertThat(JodaBeanUtils.collectionTypeTypes(test, ImmAddress.class)).isEqualTo(expected);
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_mapType_Person_otherAddressMap() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        assertThat(JodaBeanUtils.mapKeyType(test, Person.class)).isEqualTo(String.class);
        assertThat(JodaBeanUtils.mapValueType(test, Person.class)).isEqualTo(Address.class);
    }

    @Test
    public void test_integerType_Person_mapKeyTypeInvalid() {
        MetaProperty<Integer> test = ImmPerson.meta().age();
        
        assertThat(JodaBeanUtils.mapKeyType(test, Person.class)).isNull();
        assertThat(JodaBeanUtils.mapValueType(test, Person.class)).isNull();
    }

    @Test
    public void test_collectionType_Person_mapKeyTypeInvalid() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertThat(JodaBeanUtils.mapKeyType(test, Person.class)).isNull();
        assertThat(JodaBeanUtils.mapValueType(test, Person.class)).isNull();
    }

    @Test
    public void test_mapType_wildcard() {
        @SuppressWarnings("unchecked")
        MetaProperty<Map<? extends Number, String>> test = ImmGuava.meta().mapWildKey();

        assertThat(JodaBeanUtils.mapKeyType(test, ImmGuava.class)).isEqualTo(Number.class);
        assertThat(JodaBeanUtils.mapValueType(test, ImmGuava.class)).isEqualTo(String.class);
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_mapValueTypeTypes_valid() {
        MetaProperty<ImmutableMap<String, List<Integer>>> test = ImmAddress.meta().listNumericInMap();
        
        List<Class<?>> expected = ImmutableList.<Class<?>>of(Integer.class);
        assertThat(JodaBeanUtils.mapValueTypeTypes(test, ImmAddress.class)).isEqualTo(expected);
    }

    @Test
    public void test_mapValueTypeTypes_invalidNoGenerics() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        List<Class<?>> expected = Collections.emptyList();
        assertThat(JodaBeanUtils.mapValueTypeTypes(test, Person.class)).isEqualTo(expected);
    }

    @Test
    public void test_mapValueTypeTypes_invalidNotMap() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        List<Class<?>> expected = Collections.emptyList();
        assertThat(JodaBeanUtils.mapValueTypeTypes(test, Person.class)).isEqualTo(expected);
    }

    //-------------------------------------------------------------------------
    @Test
    public void equalIgnoring() {
        Bean bean1 = createBean("123", "321", "name1");
        Bean bean2 = createBean("124", "321", "name1");
        // first not ignored
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean2)).isFalse();
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty("second"))).isFalse();
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty("second"), bean1.metaBean().metaProperty("name"))).isFalse();
        // first is ignored
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty("first"))).isTrue();
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean2, bean1.metaBean().metaProperty("first"), bean1.metaBean().metaProperty("second"))).isTrue();
    }

    @Test
    public void equalIgnoring_same() {
        Bean bean1 = createBean("123", "321", "name1");
        Bean bean2 = createBean("124", "321", "name1");
        assertThat(JodaBeanUtils.equalIgnoring(bean1, bean1)).isTrue();
        assertThat(JodaBeanUtils.equalIgnoring(bean2, bean2)).isTrue();
    }

    @Test
    public void equalIgnoring_nullFirst() {
        Bean bean = createBean("124", "321", "name1");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.equalIgnoring(null, bean));
    }

    @Test
    public void equalIgnoring_nullSecond() {
        Bean bean = createBean("124", "321", "name1");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.equalIgnoring(bean, null));
    }

    @Test
    public void equalIgnoring_nullArray() {
        Bean bean = createBean("124", "321", "name1");
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanUtils.equalIgnoring(bean, bean, (MetaProperty<?>[]) null));
    }

    private static Bean createBean(String first, String second, String name) {
        FlexiBean bean = new FlexiBean();
        bean.propertySet("first", first);
        bean.propertySet("second", second);
        bean.propertySet("name", name);
        return bean;
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_compare_ascending() {
        Address address1 = new Address();
        address1.setOwner(new Person());
        address1.getOwner().setSurname("Joda");
        Address address2 = new Address();
        address2.setOwner(new Person());
        address2.getOwner().setSurname("Beans");
        Function<Bean, String> bq = JodaBeanUtils.chain(Address.meta().owner(), Person.meta().surname());
        
        Comparator<Bean> asc = JodaBeanUtils.comparator(bq, true);
        assertThat(asc.compare(address1, address1) == 0).isTrue();
        assertThat(asc.compare(address1, address2) > 1).isTrue();
        assertThat(asc.compare(address2, address1) < 1).isTrue();
    }

    @Test
    public void test_compare_descending() {
        Address address1 = new Address();
        address1.setOwner(new Person());
        address1.getOwner().setSurname("Joda");
        Address address2 = new Address();
        address2.setOwner(new Person());
        address2.getOwner().setSurname("Beans");
        Function<Bean, String> bq = JodaBeanUtils.chain(Address.meta().owner(), Person.meta().surname());
        
        Comparator<Bean> desc = JodaBeanUtils.comparator(bq, false);
        assertThat(desc.compare(address1, address1) == 0).isTrue();
        assertThat(desc.compare(address1, address2) < 1).isTrue();
        assertThat(desc.compare(address2, address1) > 1).isTrue();
    }

    @Test
    public void test_compare_ascending_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> JodaBeanUtils.comparatorAscending(null));
    }

    @Test
    public void test_compare_descending_null() {
        assertThatNullPointerException()
                .isThrownBy(() -> JodaBeanUtils.comparatorDescending(null));
    }

}
