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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

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
        assertThat(SerTypeMapper.encodeType(BitSet.class, SETTINGS, "java.util.", cache)).isEqualTo("BitSet");
        assertThat(cache.get(BitSet.class)).isEqualTo("BitSet");
        // basic type
        assertThat(SerTypeMapper.encodeType(File.class, SETTINGS, "java.util.", cache)).isEqualTo("File");
        assertThat(cache).doesNotContainKey(File.class);
        // user type
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, "java.util.", cache))
            .isEqualTo("java.util.concurrent.atomic.AtomicReference");
        assertThat(cache.get(AtomicReference.class)).isEqualTo("AtomicReference");
        // user type - second occurrence
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, "java.util.", cache)).isEqualTo("AtomicReference");
        assertThat(cache.get(AtomicReference.class)).isEqualTo("AtomicReference");
        // user type - already cached name
        assertThat(SerTypeMapper.encodeType(org.joda.beans.ser.AtomicReference.class, SETTINGS, "java.util.", cache))
            .isEqualTo("org.joda.beans.ser.AtomicReference");
        assertThat(cache.get(org.joda.beans.ser.AtomicReference.class)).isEqualTo("org.joda.beans.ser.AtomicReference");
        // user type
        assertThat(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", cache))
            .isEqualTo("java.util.concurrent.atomic.AtomicIntegerArray");
        assertThat(cache.get(AtomicIntegerArray.class)).isEqualTo("AtomicIntegerArray");
        // user type - second occurrence
        assertThat(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", cache)).isEqualTo("AtomicIntegerArray");
        assertThat(cache.get(AtomicIntegerArray.class)).isEqualTo("AtomicIntegerArray");
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "java.util.", cache)).isEqualTo("org.joda.beans.ser.BigDecimal");
        assertThat(cache.get(BigDecimal.class)).isEqualTo("org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "java.util.", cache)).isEqualTo("org.joda.beans.ser.lowerCase");
        assertThat(cache.get(lowerCase.class)).isEqualTo("org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_sillyNames() {
        Map<Class<?>, String> cache = new HashMap<>();
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "org.joda.beans.ser.", cache)).isEqualTo("org.joda.beans.ser.BigDecimal");
        assertThat(cache.get(BigDecimal.class)).isEqualTo("org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "org.joda.beans.ser.", cache)).isEqualTo("org.joda.beans.ser.lowerCase");
        assertThat(cache.get(lowerCase.class)).isEqualTo("org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_noCache() {
        // user type
        assertThat(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", null))
            .isEqualTo("java.util.concurrent.atomic.AtomicIntegerArray");
        // user type - second occurrence
        assertThat(SerTypeMapper.encodeType(AtomicIntegerArray.class, SETTINGS, "java.util.", null))
            .isEqualTo("java.util.concurrent.atomic.AtomicIntegerArray");
        // user type - normal
        assertThat(SerTypeMapper.encodeType(Normal.class, SETTINGS, "org.joda.beans.ser.", null)).isEqualTo("Normal");
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(BigDecimal.class, SETTINGS, "org.joda.beans.ser.", null)).isEqualTo("org.joda.beans.ser.BigDecimal");
        // user type - silly name
        assertThat(SerTypeMapper.encodeType(lowerCase.class, SETTINGS, "org.joda.beans.ser.", null)).isEqualTo("org.joda.beans.ser.lowerCase");
    }

    @Test
    public void test_encodeType_noBasePackage() {
        Map<Class<?>, String> cache = new HashMap<>();
        // basic type
        assertThat(SerTypeMapper.encodeType(File.class, SETTINGS, null, cache)).isEqualTo("File");
        assertThat(cache).doesNotContainKey(File.class);
        // user type
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, null, cache)).isEqualTo("java.util.concurrent.atomic.AtomicReference");
        assertThat(cache.get(AtomicReference.class)).isEqualTo("AtomicReference");
        // user type - second occurrence
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS, null, cache)).isEqualTo("AtomicReference");
        assertThat(cache.get(AtomicReference.class)).isEqualTo("AtomicReference");
    }

    @Test
    public void test_encodeType_noShortTypes() {
        Map<Class<?>, String> cache = new HashMap<>();
        // base package type
        assertThat(SerTypeMapper.encodeType(BitSet.class, SETTINGS_NO_SHORT, "java.util.", cache)).isEqualTo("java.util.BitSet");
        // basic type
        assertThat(SerTypeMapper.encodeType(File.class, SETTINGS_NO_SHORT, "java.util.", cache)).isEqualTo("File");
        // user type
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS_NO_SHORT, "java.util.", cache))
            .isEqualTo("java.util.concurrent.atomic.AtomicReference");
        // user type - second occurrence
        assertThat(SerTypeMapper.encodeType(AtomicReference.class, SETTINGS_NO_SHORT, "java.util.", cache))
            .isEqualTo("java.util.concurrent.atomic.AtomicReference");
        assertThat(cache).isEmpty();
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_decodeType() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        // base package type
        assertThat(SerTypeMapper.decodeType("BitSet", SETTINGS, "java.util.", cache)).isEqualTo(BitSet.class);
        assertThat(cache.get("BitSet")).isEqualTo(BitSet.class);
        // basic type
        assertThat(SerTypeMapper.decodeType("File", SETTINGS, "java.util.", cache)).isEqualTo(File.class);
        assertThat(cache).doesNotContainKey("File");
        // user type
        assertThat(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, "java.util.", cache)).isEqualTo(AtomicReference.class);
        assertThat(cache.get("java.util.concurrent.atomic.AtomicReference")).isEqualTo(AtomicReference.class);
        assertThat(cache.get("AtomicReference")).isEqualTo(AtomicReference.class);
        // user type
        assertThat(SerTypeMapper.decodeType("AtomicReference", SETTINGS, "java.util.", cache)).isEqualTo(AtomicReference.class);
        assertThat(cache.get("java.util.concurrent.atomic.AtomicReference")).isEqualTo(AtomicReference.class);
        assertThat(cache.get("AtomicReference")).isEqualTo(AtomicReference.class);
        // user type
        assertThat(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicIntegerArray", SETTINGS, "java.util.", cache)).isEqualTo(AtomicIntegerArray.class);
        assertThat(cache.get("java.util.concurrent.atomic.AtomicIntegerArray")).isEqualTo(AtomicIntegerArray.class);
        assertThat(cache.get("AtomicIntegerArray")).isEqualTo(AtomicIntegerArray.class);
        // user type
        assertThat(SerTypeMapper.decodeType("AtomicIntegerArray", SETTINGS, "java.util.", cache)).isEqualTo(AtomicIntegerArray.class);
        assertThat(cache.get("java.util.concurrent.atomic.AtomicIntegerArray")).isEqualTo(AtomicIntegerArray.class);
        assertThat(cache.get("AtomicIntegerArray")).isEqualTo(AtomicIntegerArray.class);
    }

    @Test
    public void test_decodeType_noCache() throws Exception {
        // base package type
        assertThat(SerTypeMapper.decodeType("BitSet", SETTINGS, "java.util.", null)).isEqualTo(BitSet.class);
        // basic type
        assertThat(SerTypeMapper.decodeType("File", SETTINGS, "java.util.", null)).isEqualTo(File.class);
        // user type
        assertThat(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, "java.util.", null)).isEqualTo(AtomicReference.class);
    }

    @Test
    public void test_decodeType_noBasePackage() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        // basic type
        assertThat(SerTypeMapper.decodeType("File", SETTINGS, null, cache)).isEqualTo(File.class);
        // user type
        assertThat(SerTypeMapper.decodeType("java.util.concurrent.atomic.AtomicReference", SETTINGS, null, cache)).isEqualTo(AtomicReference.class);
    }

    @Test
    public void test_decodeType_emptyClassName() throws Exception {
        Map<String, Class<?>> cache = new HashMap<>();
        assertThatExceptionOfType(ClassNotFoundException.class)
            .isThrownBy(() -> SerTypeMapper.decodeType("", SETTINGS, "java.util.", cache));
    }

}
