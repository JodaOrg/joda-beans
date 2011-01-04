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

import org.testng.annotations.Test;

/**
 * Test BasicBean.
 */
@Test
public class TestBasicBean {

    public void test_equals() {
        Person a1 = new Person();
        Person a2 = new Person();
        Person b = new Person();
        
        a1.setForename("A");
        a2.setForename("A");
        b.setForename("B");
        
        assertEquals(a1.equals(a1), true);
        assertEquals(a1.equals(a2), true);
        assertEquals(a2.equals(a1), true);
        assertEquals(a2.equals(a2), true);
        
        assertEquals(a1.equals(b), false);
        assertEquals(b.equals(a1), false);
        
        assertEquals(b.equals("Weird type"), false);
        assertEquals(b.equals(null), false);
    }

    public void test_hashCode() {
        Person a1 = new Person();
        Person a2 = new Person();
        
        a1.setForename("A");
        a2.setForename("A");
        
        assertEquals(a1.hashCode(), a2.hashCode());
    }

    public void test_toString() {
        Person a = new Person();
        a.setForename("A");
        a.setSurname("B");
        
        assertEquals(a.toString().startsWith("Person{"), true);
        assertEquals(a.toString().endsWith("}"), true);
        assertEquals(a.toString().contains("forename=A"), true);
        assertEquals(a.toString().contains("surname=B"), true);
    }

}
