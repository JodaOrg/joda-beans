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
package org.joda.beans.ser;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

/**
 * Test ser.
 */
public class TestSerTypeMapper {

    private static final JodaBeanSer SETTINGS = JodaBeanSer.PRETTY;
    private static final JodaBeanSer SETTINGS_NO_SHORT = JodaBeanSer.PRETTY.withShortTypes(false);

    @Test
    public void test_encodeType() {
        Map<Class<?>, String> cache = new HashMap<>();
        // base package type
        assertEquals(SerTypeMapper.encodeType(BitSet.class, SETTINGS, "java.util.", cache), "BitSet");
        assertEquals(cache.get(BitSet.class), "BitSet");
        // basic type
        assertEquals(SerTypeMapper.encodeType(File.class, SETTINGS, "java.util.", cache), "File");
        assertEquals(cache.containsKey(File.class), false);
        // user type
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, "java.util.", cache), "java.util.concurrent.atomic.AtomicReference");
        assertEquals(cache.get(AtomicReference.class), "AtomicReference");
        // user type - second occurrence
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, "java.util.", cache), "AtomicReference");
        assertEquals(cache.get(AtomicReference.class), "AtomicReference");
        // user type - already cached name
        assertEquals(SerTypeMapper.encodeType(org.joda.beans.ser.AtomicReference.class, SETTINGS, "java.util.", cache), "org.joda.beans.ser.AtomicReference");
        assertEquals(cache.get(org.joda.beans.ser.AtomicReference.class), "org.joda.beans.ser.AtomicReference");
        // user type
        assertEquals(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", cache), "java.util.concurrent.atomic.AtomicIntegerArray");
        assertEquals(cache.get(AtomicIntegerArray.class), "AtomicIntegerArray");
        // user type - second occurrence
        assertEquals(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", cache), "AtomicIntegerArray");
        assertEquals(cache.get(AtomicIntegerArray.class), "AtomicIntegerArray");
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "java.util.", cache), "org.joda.beans.ser.BigDecimal");
        assertEquals(cache.get(BigDecimal.class), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "java.util.", cache), "org.joda.beans.ser.lowerCase");
        assertEquals(cache.get(lowerCase.class), "org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_sillyNames() {
        Map<Class<?>, String> cache = new HashMap<>();
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "org.joda.beans.ser.", cache), "org.joda.beans.ser.BigDecimal");
        assertEquals(cache.get(BigDecimal.class), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "org.joda.beans.ser.", cache), "org.joda.beans.ser.lowerCase");
        assertEquals(cache.get(lowerCase.class), "org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_noCache() {
        // user type
        assertEquals(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", null), "java.util.concurrent.atomic.AtomicIntegerArray");
        // user type - second occurrence
        assertEquals(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", null), "java.util.concurrent.atomic.AtomicIntegerArray");
        // user type - normal
        assertEquals(SerTypeMapper.encodeType(Normal.class, SETTINGS, "org.joda.beans.ser.", null), "Normal");
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "org.joda.beans.ser.", null), "org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertEquals(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "org.joda.beans.ser.", null), "org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_noBasePackage() {
        Map<Class<?>, String> cache = new HashMap<>();
        // basic type
        assertEquals(SerTypeMapper.encodeType(File.class, SETTINGS, null, cache), "File");
        assertEquals(cache.containsKey(File.class), false);
        // user type
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, null, cache), "java.util.concurrent.atomic.AtomicReference");
        assertEquals(cache.get(AtomicReference.class), "AtomicReference");
        // user type - second occurrence
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, null, cache), "AtomicReference");
        assertEquals(cache.get(AtomicReference.class), "AtomicReference");
    }

    @Test
    public void test_encodeType_noShortTypes() {
        Map<Class<?>, String> cache = new HashMap<>();
        // base package type
        assertEquals(SerTypeMapper.encodeType(BitSet.class, SETTINGS_NO_SHORT, "java.util.", cache), "java.util.BitSet");
        // basic type
        assertEquals(SerTypeMapper.encodeType(File.class, SETTINGS_NO_SHORT, "java.util.", cache), "File");
        // user type
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS_NO_SHORT, "java.util.", cache), "java.util.concurrent.atomic.AtomicReference");
        // user type - second occurrence
        assertEquals(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS_NO_SHORT, "java.util.", cache), "java.util.concurrent.atomic.AtomicReference");
        assertEquals(cache.isEmpty(), true);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_decodeType() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        // base package type
        assertEquals(SerTypeMapper.decodeType("BitSet", SETTINGS, "java.util.", cache), BitSet.class);
        assertEquals(cache.get("BitSet"), BitSet.class);
        // basic type
        assertEquals(SerTypeMapper.decodeType("File", SETTINGS, "java.util.", cache), File.class);
        assertEquals(cache.containsKey("File"), false);
        // user type
        assertEquals(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, "java.util.", cache), AtomicReference.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicReference"), AtomicReference.class);
        assertEquals(cache.get("AtomicReference"), AtomicReference.class);
        // user type
        assertEquals(SerTypeMapper.decodeType("AtomicReference", SETTINGS, "java.util.", cache), AtomicReference.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicReference"), AtomicReference.class);
        assertEquals(cache.get("AtomicReference"), AtomicReference.class);
        // user type
        assertEquals(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicIntegerArray", SETTINGS, "java.util.", cache), AtomicIntegerArray.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicIntegerArray"), AtomicIntegerArray.class);
        assertEquals(cache.get("AtomicIntegerArray"), AtomicIntegerArray.class);
        // user type
        assertEquals(SerTypeMapper.decodeType("AtomicIntegerArray", SETTINGS, "java.util.", cache), AtomicIntegerArray.class);
        assertEquals(cache.get("java.util.concurrent.atomic.AtomicIntegerArray"), AtomicIntegerArray.class);
        assertEquals(cache.get("AtomicIntegerArray"), AtomicIntegerArray.class);
    }

    @Test
    public void test_decodeType_noCache() throws Exception {
        // base package type
        assertEquals(SerTypeMapper.decodeType("BitSet", SETTINGS, "java.util.", null), BitSet.class);
        // basic type
        assertEquals(SerTypeMapper.decodeType("File", SETTINGS, "java.util.", null), File.class);
        // user type
        assertEquals(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, "java.util.", null), AtomicReference.class);
    }

    @Test
    public void test_decodeType_noBasePackage() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        // basic type
        assertEquals(SerTypeMapper.decodeType("File", SETTINGS, null, cache), File.class);
        // user type
        assertEquals(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, null, cache), AtomicReference.class);
    }

    @Test(expected = ClassNotFoundException.class)
    public void test_decodeType_emptyClassName() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        SerTypeMapper.decodeType("", SETTINGS, "java.util.", cache);
    }

}
