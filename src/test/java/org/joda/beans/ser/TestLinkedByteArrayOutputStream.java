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

import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * Test {@link LinkedByteArrayOutputStream}.
 */
class TestLinkedByteArrayOutputStream {

    @Test
    void test_empty() {
        try (var test = new LinkedByteArrayOutputStream()) {
            assertThat(test).hasToString("");
            assertThat(test.toByteArray()).isEqualTo(new byte[0]);
            assertThat(test.size()).isEqualTo(0);
        }
    }

    @Test
    void test_writeByte() {
        try (var test = new LinkedByteArrayOutputStream()) {
            test.write(33);
            assertThat(test).hasToString("21");
            assertThat(test.toByteArray()).isEqualTo(new byte[] {33});
            assertThat(test.size()).isEqualTo(1);
        }
    }

    @Test
    void test_writeByte_growCapacity() {
        try (var test = new LinkedByteArrayOutputStream()) {
            test.write(new byte[1024]);
            test.write(33);
            assertThat(test.toString()).hasSize(2050).endsWith("0021");
            assertThat(test.toByteArray()).hasSize(1025).endsWith(new byte[] {33});
        }
    }

    @Test
    void test_writeByteArray_empty() {
        try (var test = new LinkedByteArrayOutputStream()) {
            test.write(new byte[0]);
            assertThat(test).hasToString("");
            assertThat(test.toByteArray()).isEqualTo(new byte[0]);
            assertThat(test.size()).isEqualTo(0);
        }
    }

    @Test
    void test_writeByteArray_normal() {
        try (var test = new LinkedByteArrayOutputStream()) {
            var bytes = new byte[] {33, 34, 35, 36, 37};
            test.write(bytes, 1, 3);
            assertThat(test).hasToString("222324");
            assertThat(test.toByteArray()).isEqualTo(new byte[] {34, 35, 36});
            assertThat(test.size()).isEqualTo(3);
        }
    }

    @Test
    void test_writeByteArray_growCapacityExact() {
        try (var test = new LinkedByteArrayOutputStream()) {
            var bytes = new byte[] {33, 34, 35, 36, 37};
            test.write(new byte[1024]);
            test.write(bytes, 1, 4);
            assertThat(test.toString()).hasSize(2056).endsWith("22232425");
            assertThat(test.toByteArray()).hasSize(1028).endsWith(new byte[] {34, 35, 36, 37});
            assertThat(test.size()).isEqualTo(1028);
            assertThat(test.toByteArray()).isEqualTo(test.toByteArray());
        }
    }

    @Test
    void test_writeByteArray_growCapacitySplit() {
        try (var test = new LinkedByteArrayOutputStream()) {
            var bytes = new byte[] {33, 34, 35, 36, 37};
            test.write(new byte[1022]);
            test.write(bytes, 0, 3);
            assertThat(test.toString()).hasSize(2050).endsWith("212223");
            assertThat(test.toByteArray()).hasSize(1025).endsWith(new byte[] {33, 34, 35});
            assertThat(test.size()).isEqualTo(1025);
            assertThat(test.toByteArray()).isEqualTo(test.toByteArray());
        }
    }

    @Test
    void test_writeByteArray_large() {
        try (var test = new LinkedByteArrayOutputStream()) {
            var bytes = new byte[2048];
            Arrays.fill(bytes, (byte) 33);
            test.write(new byte[1022]);
            test.write(bytes);
            test.write(34);
            assertThat(test.toString()).hasSize((1022 + 2048 + 1) * 2).endsWith("212122");
            assertThat(test.toByteArray()).hasSize(1022 + 2048 + 1).endsWith(new byte[] {33, 33, 34});
            assertThat(test.size()).isEqualTo(1022 + 2048 + 1);
            assertThat(test.toByteArray()).isEqualTo(test.toByteArray());
        }
    }

}
