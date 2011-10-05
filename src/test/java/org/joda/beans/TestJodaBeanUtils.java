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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotSame;

import java.util.List;
import java.util.Map;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.Pair;
import org.joda.beans.gen.Person;
import org.joda.beans.impl.flexi.FlexiBean;
import org.testng.annotations.Test;

/**
 * Test BeanUtils.
 */
@Test
public class TestJodaBeanUtils {

    public void test_metaBean() {
        assertEquals(JodaBeanUtils.metaBean(Person.class), Person.meta());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_metaBean_notFound() {
        JodaBeanUtils.metaBean(String.class);
    }

    //-----------------------------------------------------------------------
    public void test_propertiesEqual_propertiesHashCode() {
        Pair a = new Pair();
        a.setFirst("A");
        
        FlexiBean b = new FlexiBean();
        b.set("first", "A");
        assertEquals(JodaBeanUtils.propertiesEqual(a, b), false);
        assertEquals(JodaBeanUtils.propertiesEqual(b, a), false);
        
        b.set("second", null);
        assertEquals(JodaBeanUtils.propertiesEqual(a, b), true);
        assertEquals(JodaBeanUtils.propertiesEqual(b, a), true);
        assertEquals(JodaBeanUtils.propertiesHashCode(a), JodaBeanUtils.propertiesHashCode(b));
        
        b.set("second", "B");
        assertEquals(JodaBeanUtils.propertiesEqual(a, b), false);
        assertEquals(JodaBeanUtils.propertiesEqual(b, a), false);
        
        a.setSecond("B");
        assertEquals(JodaBeanUtils.propertiesEqual(a, b), true);
        assertEquals(JodaBeanUtils.propertiesEqual(b, a), true);
        assertEquals(JodaBeanUtils.propertiesHashCode(a), JodaBeanUtils.propertiesHashCode(b));
    }

    //-------------------------------------------------------------------------
    public void test_equal() {
        assertEquals(JodaBeanUtils.equal("A", new Character('A').toString()), true);
        assertEquals(JodaBeanUtils.equal("A", "B"), false);
        assertEquals(JodaBeanUtils.equal("A", null), false);
        assertEquals(JodaBeanUtils.equal(null, "A"), false);
    }

    public void test_equal_ObjectArray() {
        Object[] a1 = new Object[] {1, 2, 3};
        Object[] a2 = new Object[] {1, 2, 3};
        Object[] b = new Object[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_IntegerArray() {
        Object[] a1 = new Integer[] {1, 2, 3};
        Object[] a2 = new Integer[] {1, 2, 3};
        Object[] b = new Integer[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_IntegerNumberArray() {
        Object[] a = new Integer[] {1, 2, 3};
        Object[] b = new Number[] {1, 2, 3};
        assertEquals(JodaBeanUtils.equal(a, b), false);
        assertEquals(JodaBeanUtils.equal(b, a), false);
    }

    public void test_equal_IntegerIntArray() {
        Object[] a = new Integer[] {1, 2, 3};
        int[] b = new int[] {1, 2, 3};
        assertEquals(JodaBeanUtils.equal(a, b), false);
        assertEquals(JodaBeanUtils.equal(b, a), false);
    }

    public void test_equal_IntArray() {
        int[] a1 = new int[] {1, 2, 3};
        int[] a2 = new int[] {1, 2, 3};
        int[] b = new int[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_LongArray() {
        long[] a1 = new long[] {1, 2, 3};
        long[] a2 = new long[] {1, 2, 3};
        long[] b = new long[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_DoubleArray() {
        double[] a1 = new double[] {1, 2, 3};
        double[] a2 = new double[] {1, 2, 3};
        double[] b = new double[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_FloatArray() {
        float[] a1 = new float[] {1, 2, 3};
        float[] a2 = new float[] {1, 2, 3};
        float[] b = new float[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_ShortArray() {
        short[] a1 = new short[] {1, 2, 3};
        short[] a2 = new short[] {1, 2, 3};
        short[] b = new short[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_CharArray() {
        char[] a1 = new char[] {1, 2, 3};
        char[] a2 = new char[] {1, 2, 3};
        char[] b = new char[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_ByteArray() {
        byte[] a1 = new byte[] {1, 2, 3};
        byte[] a2 = new byte[] {1, 2, 3};
        byte[] b = new byte[] {1, 2, 4};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    public void test_equal_BooleanArray() {
        boolean[] a1 = new boolean[] {true, false};
        boolean[] a2 = new boolean[] {true, false};
        boolean[] b = new boolean[] {true, true};
        assertEquals(JodaBeanUtils.equal(a1, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, a2), true);
        assertEquals(JodaBeanUtils.equal(a2, a1), true);
        assertEquals(JodaBeanUtils.equal(a1, b), false);
        assertEquals(JodaBeanUtils.equal(b, a1), false);
    }

    //-----------------------------------------------------------------------
    public void test_hashCode_Object() {
        assertEquals(JodaBeanUtils.hashCode("A"), "A".hashCode());
        assertEquals(JodaBeanUtils.hashCode(null), 0);
    }

    //-----------------------------------------------------------------------
    public void test_clone() {
        Person p = new Person();
        p.setForename("Stephen");
        p.setSurname("Colebourne");
        p.getOtherAddressMap().put("A", new Address());
        p.getOtherAddressMap().get("A").setCity("London");
        Person cloned = JodaBeanUtils.clone(p);
        assertNotSame(cloned, p);
        assertEquals(cloned, p);
        p.getOtherAddressMap().put("B", new Address());
        assertFalse(cloned.equals(p));
    }

    //-----------------------------------------------------------------------
    public void test_listType_Person_addressList() {
        MetaProperty<List<Address>> test = Person.meta().addressList();
        
        assertEquals(JodaBeanUtils.collectionType(test, Person.class), Address.class);
    }

    public void test_listType_Person_addressesList() {
        MetaProperty<List<List<Address>>> test = Person.meta().addressesList();
        
        assertEquals(JodaBeanUtils.collectionType(test, Person.class), List.class);
    }

    public void test_mapType_Person_otherAddressMap() {
        MetaProperty<Map<String, Address>> test = Person.meta().otherAddressMap();
        
        assertEquals(JodaBeanUtils.mapKeyType(test, Person.class), String.class);
        assertEquals(JodaBeanUtils.mapValueType(test, Person.class), Address.class);
    }

}
