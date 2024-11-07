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
package org.joda.beans.ser.json;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test.
 */
public class TestJsonOutput {

    private StringBuilder buf;
    private JsonOutput outputCompact;
    private JsonOutput outputPretty;

    @BeforeEach
    public void setUp() {
        buf = new StringBuilder();
        outputCompact = new JsonOutput(buf);
        outputPretty = new JsonOutput(buf, " ", "\n");
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_string() {
        return new Object[][] {
            {"", ""},
            {"\"", "\\\""},
            {"\\", "\\\\"},
            {"a\\b", "a\\\\b"},
            {"a\"b", "a\\\"b"},
            {"a\\\"b", "a\\\\\\\"b"},
            {"a\nb", "a\\nb"},
            {"foo\r\nbar", "foo\\r\\nbar"},
            {"foo\tbar", "foo\\tbar"},
            {"foo\u2028bar\u2029\u2030", "foo\\u2028bar\\u2029\u2030"},
            {"foo\u0000bar", "foo\\u0000bar"},
            {"foo\u001bbar", "foo\\u001bbar"},
            {"\u0001\u0008\u000f\u0010\u001f", "\\u0001\\b\\u000f\\u0010\\u001f"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_writeString(String input, String expected) {
        outputCompact.writeString(input);
        assertThat(buf.toString()).isEqualTo('"' + expected + '"');
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_int() {
        return new Object[][] {
            {0, "0"},
            {1, "1"},
            {2, "2"},
            {1234567, "1234567"},
            {-1, "-1"},
            {-1234567, "-1234567"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_int")
    public void test_writeInt(int input, String expected) {
        outputCompact.writeInt(input);
        assertThat(buf.toString()).isEqualTo(expected);
    }

    public static Object[][] data_long() {
        return new Object[][] {
            {0, "0"},
            {1, "1"},
            {2, "2"},
            {1234567, "1234567"},
            {-1, "-1"},
            {-1234567, "-1234567"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_long")
    public void test_writeLong(long input, String expected) {
        outputCompact.writeLong(input);
        assertThat(buf.toString()).isEqualTo(expected);
    }

    public static Object[][] data_double() {
        return new Object[][] {
            {0d, "0.0"},
            {1d, "1.0"},
            {2d, "2.0"},
            {1234567d, "1234567.0"},
            {-1d, "-1.0"},
            {-1234567d, "-1234567.0"},
            {0.000001d, "1.0E-6"},
            {0.1234d, "0.1234"},
            {Double.NaN, "\"NaN\""},
            {Double.POSITIVE_INFINITY, "\"Infinity\""},
            {Double.NEGATIVE_INFINITY, "\"-Infinity\""},
        };
    }

    @ParameterizedTest
    @MethodSource("data_double")
    public void test_writeDouble(double input, String expected) {
        outputCompact.writeDouble(input);
        assertThat(buf.toString()).isEqualTo(expected);
    }

    public static Object[][] data_float() {
        return new Object[][] {
            {0f, "0.0"},
            {1f, "1.0"},
            {2f, "2.0"},
            {1234567f, "1234567.0"},
            {-1f, "-1.0"},
            {-1234567f, "-1234567.0"},
            {0.000001f, "1.0E-6"},
            {0.1234f, "0.1234"},
            {Float.NaN, "\"NaN\""},
            {Float.POSITIVE_INFINITY, "\"Infinity\""},
            {Float.NEGATIVE_INFINITY, "\"-Infinity\""},
        };
    }

    @ParameterizedTest
    @MethodSource("data_float")
    public void test_writeFloat(float input, String expected) {
        outputCompact.writeFloat(input);
        assertThat(buf.toString()).isEqualTo(expected);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_writeNull() {
        outputCompact.writeNull();
        assertThat(buf.toString()).isEqualTo("null");
    }

    @Test
    public void test_writeBoolean_true() {
        outputCompact.writeBoolean(true);
        assertThat(buf.toString()).isEqualTo("true");
    }

    @Test
    public void test_writeBoolean_false() {
        outputCompact.writeBoolean(false);
        assertThat(buf.toString()).isEqualTo("false");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_write_array0() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[]");
    }

    @Test
    public void test_write_array1() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\"]");
    }

    @Test
    public void test_write_array2() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("b");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\",\"b\"]");
    }

    @Test
    public void test_write_array3() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("b");
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("c");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\",\"b\",\"c\"]");
    }

    @Test
    public void test_write_arrayDeep0() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayItemStart();
        outputCompact.writeArrayStart();
        outputCompact.writeArrayEnd();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("c");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\",[],\"c\"]");
    }

    @Test
    public void test_write_arrayDeep1() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayItemStart();
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("b1");
        outputCompact.writeArrayEnd();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("c");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\",[\"b1\"],\"c\"]");
    }

    @Test
    public void test_write_arrayDeep2() {
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("a");
        outputCompact.writeArrayItemStart();
        outputCompact.writeArrayStart();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("b1");
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("b2");
        outputCompact.writeArrayEnd();
        outputCompact.writeArrayItemStart();
        outputCompact.writeString("c");
        outputCompact.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\",[\"b1\",\"b2\"],\"c\"]");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_write_object0() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{}");
    }

    @Test
    public void test_write_object1() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKey("a");
        outputCompact.writeString("aa");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\"}");
    }

    @Test
    public void test_write_object2() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKey("a");
        outputCompact.writeString("aa");
        outputCompact.writeObjectKey("b");
        outputCompact.writeString("bb");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\",\"b\":\"bb\"}");
    }

    @Test
    public void test_write_object3() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("a", "aa");
        outputCompact.writeObjectKeyValue("b", "bb");
        outputCompact.writeObjectKeyValue("c", "cc");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\",\"b\":\"bb\",\"c\":\"cc\"}");
    }

    @Test
    public void test_write_objectDeep0() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("a", "aa");
        outputCompact.writeObjectKey("b");
        outputCompact.writeObjectStart();
        outputCompact.writeObjectEnd();
        outputCompact.writeObjectKeyValue("c", "cc");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\",\"b\":{},\"c\":\"cc\"}");
    }

    @Test
    public void test_write_objectDeep1() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("a", "aa");
        outputCompact.writeObjectKey("b");
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("bb", "bbb");
        outputCompact.writeObjectEnd();
        outputCompact.writeObjectKeyValue("c", "cc");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\",\"b\":{\"bb\":\"bbb\"},\"c\":\"cc\"}");
    }

    @Test
    public void test_write_objectDeep2() {
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("a", "aa");
        outputCompact.writeObjectKey("b");
        outputCompact.writeObjectStart();
        outputCompact.writeObjectKeyValue("bb1", "bbb1");
        outputCompact.writeObjectKeyValue("bb2", "bbb2");
        outputCompact.writeObjectEnd();
        outputCompact.writeObjectKeyValue("c", "cc");
        outputCompact.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\"a\":\"aa\",\"b\":{\"bb1\":\"bbb1\",\"bb2\":\"bbb2\"},\"c\":\"cc\"}");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_write_array0_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[]");
    }

    @Test
    public void test_write_array1_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\"]");
    }

    @Test
    public void test_write_array2_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("b");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\", \"b\"]");
    }

    @Test
    public void test_write_array3_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("b");
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("c");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\", \"b\", \"c\"]");
    }

    @Test
    public void test_write_arrayDeep0_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayItemStart();
        outputPretty.writeArrayStart();
        outputPretty.writeArrayEnd();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("c");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\", [], \"c\"]");
    }

    @Test
    public void test_write_arrayDeep1_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayItemStart();
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("b1");
        outputPretty.writeArrayEnd();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("c");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\", [\"b1\"], \"c\"]");
    }

    @Test
    public void test_write_arrayDeep2_pretty() {
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("a");
        outputPretty.writeArrayItemStart();
        outputPretty.writeArrayStart();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("b1");
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("b2");
        outputPretty.writeArrayEnd();
        outputPretty.writeArrayItemStart();
        outputPretty.writeString("c");
        outputPretty.writeArrayEnd();
        assertThat(buf.toString()).isEqualTo("[\"a\", [\"b1\", \"b2\"], \"c\"]");
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_write_object0_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{}");
    }

    @Test
    public void test_write_object1_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKey("a");
        outputPretty.writeString("aa");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\n \"a\": \"aa\"\n}");
    }

    @Test
    public void test_write_object2_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKey("a");
        outputPretty.writeString("aa");
        outputPretty.writeObjectKey("b");
        outputPretty.writeString("bb");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\n \"a\": \"aa\",\n \"b\": \"bb\"\n}");
    }

    @Test
    public void test_write_object3_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("a", "aa");
        outputPretty.writeObjectKeyValue("b", "bb");
        outputPretty.writeObjectKeyValue("c", "cc");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\n \"a\": \"aa\",\n \"b\": \"bb\",\n \"c\": \"cc\"\n}");
    }

    @Test
    public void test_write_objectDeep0_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("a", "aa");
        outputPretty.writeObjectKey("b");
        outputPretty.writeObjectStart();
        outputPretty.writeObjectEnd();
        outputPretty.writeObjectKeyValue("c", "cc");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\n \"a\": \"aa\",\n \"b\": {},\n \"c\": \"cc\"\n}");
    }

    @Test
    public void test_write_objectDeep1_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("a", "aa");
        outputPretty.writeObjectKey("b");
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("bb", "bbb");
        outputPretty.writeObjectEnd();
        outputPretty.writeObjectKeyValue("c", "cc");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("{\n \"a\": \"aa\",\n \"b\": {\n  \"bb\": \"bbb\"\n },\n \"c\": \"cc\"\n}");
    }

    @Test
    public void test_write_objectDeep2_pretty() {
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("a", "aa");
        outputPretty.writeObjectKey("b");
        outputPretty.writeObjectStart();
        outputPretty.writeObjectKeyValue("bb1", "bbb1");
        outputPretty.writeObjectKeyValue("bb2", "bbb2");
        outputPretty.writeObjectEnd();
        outputPretty.writeObjectKeyValue("c", "cc");
        outputPretty.writeObjectEnd();
        assertThat(buf.toString()).isEqualTo("""
                {
                 "a": "aa",
                 "b": {
                  "bb1": "bbb1",
                  "bb2": "bbb2"
                 },
                 "c": "cc"
                }\
                """);
    }

}
