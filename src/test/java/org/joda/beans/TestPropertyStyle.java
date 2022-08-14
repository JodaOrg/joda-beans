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

import org.junit.jupiter.api.Test;

/**
 * Test PropertyStyle.
 */
public class TestPropertyStyle {

    @Test
    public void test_READ_ONLY() {
        assertThat(PropertyStyle.READ_ONLY.isReadable()).isTrue();
        assertThat(PropertyStyle.READ_ONLY.isWritable()).isFalse();
        assertThat(PropertyStyle.READ_ONLY.isBuildable()).isFalse();
        assertThat(PropertyStyle.READ_ONLY.isReadOnly()).isTrue();
        assertThat(PropertyStyle.READ_ONLY.isDerived()).isFalse();
        assertThat(PropertyStyle.READ_ONLY.isSerializable()).isFalse();
    }

    @Test
    public void test_READ_WRITE() {
        assertThat(PropertyStyle.READ_WRITE.isReadable()).isTrue();
        assertThat(PropertyStyle.READ_WRITE.isWritable()).isTrue();
        assertThat(PropertyStyle.READ_WRITE.isBuildable()).isTrue();
        assertThat(PropertyStyle.READ_WRITE.isReadOnly()).isFalse();
        assertThat(PropertyStyle.READ_WRITE.isDerived()).isFalse();
        assertThat(PropertyStyle.READ_WRITE.isSerializable()).isTrue();
    }

    @Test
    public void test_WRITE_ONLY() {
        assertThat(PropertyStyle.WRITE_ONLY.isReadable()).isFalse();
        assertThat(PropertyStyle.WRITE_ONLY.isWritable()).isTrue();
        assertThat(PropertyStyle.WRITE_ONLY.isBuildable()).isTrue();
        assertThat(PropertyStyle.WRITE_ONLY.isReadOnly()).isFalse();
        assertThat(PropertyStyle.WRITE_ONLY.isDerived()).isFalse();
        assertThat(PropertyStyle.WRITE_ONLY.isSerializable()).isFalse();
    }

    @Test
    public void test_DERIVED() {
        assertThat(PropertyStyle.DERIVED.isReadable()).isTrue();
        assertThat(PropertyStyle.DERIVED.isWritable()).isFalse();
        assertThat(PropertyStyle.DERIVED.isBuildable()).isFalse();
        assertThat(PropertyStyle.DERIVED.isReadOnly()).isTrue();
        assertThat(PropertyStyle.DERIVED.isDerived()).isTrue();
        assertThat(PropertyStyle.DERIVED.isSerializable()).isFalse();
    }

    @Test
    public void test_IMMUTABLE() {
        assertThat(PropertyStyle.IMMUTABLE.isReadable()).isTrue();
        assertThat(PropertyStyle.IMMUTABLE.isWritable()).isFalse();
        assertThat(PropertyStyle.IMMUTABLE.isBuildable()).isTrue();
        assertThat(PropertyStyle.IMMUTABLE.isReadOnly()).isTrue();
        assertThat(PropertyStyle.IMMUTABLE.isDerived()).isFalse();
        assertThat(PropertyStyle.IMMUTABLE.isSerializable()).isTrue();
    }

}
