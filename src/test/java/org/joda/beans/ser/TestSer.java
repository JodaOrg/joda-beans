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
package org.joda.beans.ser;

import static org.testng.Assert.assertEquals;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.testng.annotations.Test;

/**
 * Test ser.
 */
@Test
public class TestSer {

    public void test_encodeClass() {
        Map<Class<?>, String> cache = new HashMap<Class<?>, String>();
        // base package type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(BitSet.class, "java.util.", cache), "BitSet");
        assertEquals(cache.get(BitSet.class), "BitSet");
        // basic type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(File.class, "java.util.", cache), "File");
        assertEquals(cache.containsKey(File.class), false);
        // user type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicInteger.class, "java.util.", cache), "java.util.concurrent.atomic.AtomicInteger");
        assertEquals(cache.get(AtomicInteger.class), "AtomicInteger");
        // user type - second occurrence
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicInteger.class, "java.util.", cache), "AtomicInteger");
        assertEquals(cache.get(AtomicInteger.class), "AtomicInteger");
        // user type - already cached name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(org.joda.beans.ser.AtomicInteger.class, "java.util.", cache), "org.joda.beans.ser.AtomicInteger");
        assertEquals(cache.get(org.joda.beans.ser.AtomicInteger.class), "org.joda.beans.ser.AtomicInteger");
        // user type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicLong.class, "java.util.", cache), "java.util.concurrent.atomic.AtomicLong");
        assertEquals(cache.get(AtomicLong.class), "AtomicLong");
        // user type - second occurrence
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicLong.class, "java.util.", cache), "AtomicLong");
        assertEquals(cache.get(AtomicLong.class), "AtomicLong");
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(BigDecimal.class, "java.util.", cache), "org.joda.beans.ser.BigDecimal");
        assertEquals(cache.get(BigDecimal.class), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(lowerCase.class, "java.util.", cache), "org.joda.beans.ser.lowerCase");
        assertEquals(cache.get(lowerCase.class), "org.joda.beans.ser.lowerCase");
    }

    public void test_encodeClass_sillyNames() {
        Map<Class<?>, String> cache = new HashMap<Class<?>, String>();
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(BigDecimal.class, "org.joda.beans.ser.", cache), "org.joda.beans.ser.BigDecimal");
        assertEquals(cache.get(BigDecimal.class), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(lowerCase.class, "org.joda.beans.ser.", cache), "org.joda.beans.ser.lowerCase");
        assertEquals(cache.get(lowerCase.class), "org.joda.beans.ser.lowerCase");
    }

    public void test_encodeClass_noCache() {
        // user type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicLong.class, "java.util.", null), "java.util.concurrent.atomic.AtomicLong");
        // user type - second occurrence
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicLong.class, "java.util.", null), "java.util.concurrent.atomic.AtomicLong");
        // user type - normal
        assertEquals(JodaBeanSer.PRETTY.encodeClass(Normal.class, "org.joda.beans.ser.", null), "Normal");
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(BigDecimal.class, "org.joda.beans.ser.", null), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(JodaBeanSer.PRETTY.encodeClass(lowerCase.class, "org.joda.beans.ser.", null), "org.joda.beans.ser.lowerCase");
    }

    public void test_encodeClass_noBasePackage() {
        Map<Class<?>, String> cache = new HashMap<Class<?>, String>();
        // basic type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(File.class, null, cache), "File");
        assertEquals(cache.containsKey(File.class), false);
        // user type
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicInteger.class, null, cache), "java.util.concurrent.atomic.AtomicInteger");
        assertEquals(cache.get(AtomicInteger.class), "AtomicInteger");
        // user type - second occurrence
        assertEquals(JodaBeanSer.PRETTY.encodeClass(AtomicInteger.class, null, cache), "AtomicInteger");
        assertEquals(cache.get(AtomicInteger.class), "AtomicInteger");
    }

    public void test_encodeClass_noShortTypes() {
        Map<Class<?>, String> cache = new HashMap<Class<?>, String>();
        // base package type
        assertEquals(JodaBeanSer.PRETTY.withShortTypes(false).encodeClass(BitSet.class, "java.util.", cache), "java.util.BitSet");
        // basic type
        assertEquals(JodaBeanSer.PRETTY.withShortTypes(false).encodeClass(File.class, "java.util.", cache), "File");
        // user type
        assertEquals(JodaBeanSer.PRETTY.withShortTypes(false).encodeClass(AtomicInteger.class, "java.util.", cache), "java.util.concurrent.atomic.AtomicInteger");
        // user type - second occurrence
        assertEquals(JodaBeanSer.PRETTY.withShortTypes(false).encodeClass(AtomicInteger.class, "java.util.", cache), "java.util.concurrent.atomic.AtomicInteger");
        assertEquals(cache.isEmpty(), true);
    }

    //-----------------------------------------------------------------------
    public void test_decodeClass() throws Exception {
        Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
        // base package type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("BitSet", "java.util.", cache), BitSet.class);
        assertEquals(cache.get("BitSet"), BitSet.class);
        // basic type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("File", "java.util.", cache), File.class);
        assertEquals(cache.containsKey("File"), false);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("java.util.concurrent.atomic.AtomicInteger", "java.util.", cache), AtomicInteger.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicInteger"), AtomicInteger.class);
        assertEquals(cache.get("AtomicInteger"), AtomicInteger.class);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("AtomicInteger", "java.util.", cache), AtomicInteger.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicInteger"), AtomicInteger.class);
        assertEquals(cache.get("AtomicInteger"), AtomicInteger.class);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("java.util.concurrent.atomic.AtomicLong", "java.util.", cache), AtomicLong.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicLong"), AtomicLong.class);
        assertEquals(cache.get("AtomicLong"), AtomicLong.class);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("AtomicLong", "java.util.", cache), AtomicLong.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicLong"), AtomicLong.class);
        assertEquals(cache.get("AtomicLong"), AtomicLong.class);
    }

    public void test_decodeClass_noCache() throws Exception {
        // base package type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("BitSet", "java.util.", null), BitSet.class);
        // basic type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("File", "java.util.", null), File.class);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("java.util.concurrent.atomic.AtomicInteger", "java.util.", null), AtomicInteger.class);
    }

    public void test_decodeClass_noBasePackage() throws Exception {
        Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
        // basic type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("File", null, cache), File.class);
        // user type
        assertEquals(JodaBeanSer.PRETTY.decodeClass("java.util.concurrent.atomic.AtomicInteger", null, cache), AtomicInteger.class);
    }

    @Test(expectedExceptions = ClassNotFoundException.class)
    public void test_decodeClass_emptyClassName() throws Exception {
        Map<String, Class<?>> cache = new HashMap<String, Class<?>>();
        JodaBeanSer.PRETTY.decodeClass("", "java.util.", cache);
    }

}
