/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.util.Arrays;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.Person;
import org.joda.beans.query.ChainedBeanQuery;
import org.testng.annotations.Test;

/**
 * Test query.
 */
@Test
public class TestBeanQuery {

    public void test_simple() {
        Address address = new Address();
        address.setStreet("Broadway");
        
        BeanQuery<String> bq1 = address.street().metaProperty();
        assertEquals(bq1.get(address), "Broadway");
        
        BeanQuery<String> bq2 = address.metaBean().street();
        assertEquals(bq2.get(address), "Broadway");
    }

    //-------------------------------------------------------------------------
    public void test_chained() {
        Address address = new Address();
        address.setOwner(new Person());
        address.getOwner().setSurname("Joda");
        
        ChainedBeanQuery<String> bq = ChainedBeanQuery.of(Address.meta().owner(), Person.meta().surname());
        assertEquals(bq.get(address), "Joda");
    }

    public void test_chained_toString() {
        Address address = new Address();
        address.setOwner(new Person());
        address.getOwner().setSurname("Joda");
        
        ChainedBeanQuery<String> bq = ChainedBeanQuery.of(Address.meta().owner(), Person.meta().surname());
        assertEquals(bq.toString(), "Address:owner.Person:surname");
    }

    @SuppressWarnings("unchecked")
    public void test_chained_getChain() {
        Address address = new Address();
        address.setOwner(new Person());
        address.getOwner().setSurname("Joda");
        
        ChainedBeanQuery<String> bq = ChainedBeanQuery.of(Address.meta().owner(), Person.meta().surname());
        assertEquals(bq.getChain(), Arrays.asList(Address.meta().owner(), Person.meta().surname()));
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_2_null1() {
        ChainedBeanQuery.of(null, Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_2_null2() {
        ChainedBeanQuery.of(Address.meta().owner(), null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_3_null1() {
        ChainedBeanQuery.of(null, Address.meta().owner(), Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_3_null2() {
        ChainedBeanQuery.of(Address.meta().owner(), null, Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_3_null3() {
        ChainedBeanQuery.of(Address.meta().owner(), Address.meta().owner(), null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_4_null1() {
        ChainedBeanQuery.of(null, Address.meta().owner(), Address.meta().owner(), Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_4_null2() {
        ChainedBeanQuery.of(Address.meta().owner(), null, Address.meta().owner(), Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_4_null3() {
        ChainedBeanQuery.of(Address.meta().owner(), Address.meta().owner(), null, Address.meta().owner());
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void test_chained_4_null4() {
        ChainedBeanQuery.of(Address.meta().owner(), Address.meta().owner(), Address.meta().owner(), null);
    }

}
