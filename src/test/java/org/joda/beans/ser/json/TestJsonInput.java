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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.offset;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Test.
 */
public class TestJsonInput {

    //-----------------------------------------------------------------------
    public static Object[][] data_string() {
        return new Object[][] {
            {"", ""},
            {"normal text", "normal text"},
            {"\\\"", "\""},
            {"\\\\", "\\"},
            {"a\\\\b", "a\\b"},
            {"a\\\"b", "a\"b"},
            {"a\\\\\\\"b", "a\\\"b"},
            {"a\\nb", "a\nb"},
            {"a\\fb", "a\fb"},
            {"a\\/b", "a/b"},
            {"foo\\r\\nbar", "foo\r\nbar"},
            {"foo\\tbar", "foo\tbar"},
            {"foo\\u2028bar\\u2029\u2030", "foo\u2028bar\u2029\u2030"},
            {"foo\\u0000bar", "foo\u0000bar"},
            {"foo\\u001bbar", "foo\u001bbar"},
            {"\\u0001\\b\\u000f\\u0010\\u001f", "\u0001\u0008\u000f\u0010\u001f"},
            {"\\u000a\\u000A\\u0010\\u001e\\u001E", "\n\n\u0010\u001e\u001e"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_parseString(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '"'));
        assertThat(input.parseString()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_parseString_endOfFile(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.parseString());
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptString(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader('"' + text + '"'));
        assertThat(input.acceptString()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptString_whitespace(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(" \t\r\n \"" + text + '"'));
        assertThat(input.acceptString()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptString_pushback(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '"'));
        input.pushBack('"');
        assertThat(input.acceptString()).isEqualTo(expected);
    }

    public static Object[][] data_stringBad() {
        return new Object[][] {
            {"\\x"},
            {"\\u1"},
            {"\\u01"},
            {"\\u001"},
            {"\\u000g"},
            {"\\urubbish"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_stringBad")
    public void test_parseString_bad(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '"'));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.parseString());
    }

    @ParameterizedTest
    @MethodSource("data_stringBad")
    public void test_acceptString_bad(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader('"' + text + '"'));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.acceptString());
    }

    @ParameterizedTest
    @MethodSource("data_stringBad")
    public void test_acceptString_bad_whitespace(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader(" \t\r\n \"" + text + '"'));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.acceptString());
    }

    //-----------------------------------------------------------------------
    @ParameterizedTest
    @MethodSource("data_string")
    public void test_parseObjectKey(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + "\":"));
        assertThat(input.parseObjectKey()).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_parseObjectKey_whitspace(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + "\" \t\n\r:"));
        assertThat(input.parseObjectKey()).isEqualTo(expected);
    }

    //-----------------------------------------------------------------------
    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptObjectKey(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + "\":"));
        assertThat(input.acceptObjectKey(JsonEvent.STRING)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptObjectKey_whitspace(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + "\" \t\n\r:"));
        assertThat(input.acceptObjectKey(JsonEvent.STRING)).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("data_string")
    public void test_acceptObjectKey_notString(String text, String expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + "\":"));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.acceptObjectKey(JsonEvent.OBJECT));
    }

    @Test
    public void test_acceptObjectKey_pushBack() throws IOException {
        JsonInput input = new JsonInput(new StringReader(":"));
        input.pushBackObjectKey("key");
        assertThat(input.acceptObjectKey(JsonEvent.STRING)).isEqualTo("key");
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_numberIntegral() {
        return new Object[][] {
            {"0", 0L},
            {"1", 1L},
            {"9", 9L},
            {"10", 10L},
            {"19", 19L},
            {"123456789", 123456789L},
            {"1234567890123456789", 1234567890123456789L},
            {"-0", 0L},
            {"-1", -1L},
            {"-9", -9L},
            {"-10", -10L},
            {"-19", -19L},
            {"-123456789", -123456789L},
            {"-1234567890123456789", -1234567890123456789L},
        };
    }

    @ParameterizedTest
    @MethodSource("data_numberIntegral")
    public void test_parseNumberIntegral(String text, long expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '}'));
        assertThat(input.readEvent()).isEqualTo(JsonEvent.NUMBER_INTEGRAL);
        assertThat(input.parseNumberIntegral()).isEqualTo(expected);
        assertThat(input.readEvent()).isEqualTo(JsonEvent.OBJECT_END);
    }

    @ParameterizedTest
    @MethodSource("data_numberIntegral")
    public void test_parseNumberIntegral_endOfFile(String text, long expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.readEvent());
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_numberFloating() {
        return new Object[][] {
            {"0.0", 0d},
            {"1.0", 1d},
            {"9.0", 9d},
            {"10.0", 10d},
            {"19.0", 19d},
            {"123456789.0", 123456789d},
            {"1234567890123456789.0", 1234567890123456789d},
            {"-0.0", 0d},
            {"-1.0", -1d},
            {"-9.0", -9d},
            {"-10.0", -10d},
            {"-19.0", -19d},
            {"-123456789.0", -123456789d},
            {"-1234567890123456789.0", -1234567890123456789d},
            {"0.0001", 0.0001d},
            {"1.12345678", 1.12345678d},
            {"9.0e2", 9.0e2d},
            {"9e2", 9e2d},
            {"123.456e20", 123.456e20d},
            {"123.456e+20", 123.456e+20d},
            {"123.456e-20", 123.456e-20d},
            {"9.0E2", 9.0e2d},
            {"9E2", 9e2d},
            {"123.456E20", 123.456e20d},
            {"123.456E+20", 123.456e+20d},
            {"123.456E-20", 123.456e-20d},
        };
    }

    @ParameterizedTest
    @MethodSource("data_numberFloating")
    public void test_parseNumberFloating(String text, double expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '}'));
        assertThat(input.readEvent()).isEqualTo(JsonEvent.NUMBER_FLOATING);
        assertThat(input.parseNumberFloating()).isCloseTo(expected, offset(0.00001d));
        assertThat(input.readEvent()).isEqualTo(JsonEvent.OBJECT_END);
    }

    @ParameterizedTest
    @MethodSource("data_numberFloating")
    public void test_parseNumberFloating_endOfFile(String text, double expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.readEvent());
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_numberBad() {
        return new Object[][] {
            {"-"},
            {"x"},
            {"e"},
            {"E"},
            {"1e"},
            {"2E"},
            {"1+"},
            {"1-"},
            {"1."},
            {"00"},
            {"001"},
            {"00.0"},
            {"1.1e3E4"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_numberBad")
    public void test_parseNumberFloating_bad(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + '}'));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.readEvent());
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_event() {
        return new Object[][] {
            {"null", JsonEvent.NULL},
            {"true", JsonEvent.TRUE},
            {"false", JsonEvent.FALSE},
            {"{", JsonEvent.OBJECT},
            {"}", JsonEvent.OBJECT_END},
            {"[", JsonEvent.ARRAY},
            {"]", JsonEvent.ARRAY_END},
            {"\"", JsonEvent.STRING},
            {"-1}", JsonEvent.NUMBER_INTEGRAL},
            {"1}", JsonEvent.NUMBER_INTEGRAL},
            {"1.0}", JsonEvent.NUMBER_FLOATING},
            {"-1.2}", JsonEvent.NUMBER_FLOATING},
            {":", JsonEvent.COLON},
            {",", JsonEvent.COMMA},
        };
    }

    @ParameterizedTest
    @MethodSource("data_event")
    public void test_readEvent(String text, JsonEvent expected) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text));
        assertThat(input.readEvent()).isEqualTo(expected);
    }

    public static Object[][] data_eventBad() {
        return new Object[][] {
            {"nul"},
            {"nulx"},
            {"nx"},
            {"tru"},
            {"trux"},
            {"tx"},
            {"fals"},
            {"fx"},
            {"x"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_eventBad")
    public void test_readEvent_bad(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.readEvent());
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_skip() {
        return new Object[][] {
            {"null"},
            {"true"},
            {"false"},
            {"\"\""},
            {"\"text\""},
            {"-1"},
            {"1"},
            {"1.0"},
            {"-1.2"},
            {"{}"},
            {"{\"a\":2}"},
            {"{\"a\":2,\"b\":{\"aa\":[1,2,3]}}"},
            {"[]"},
            {"[1,2,3]"},
            {"[1,[\"\"],{\"a\":2}]"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_skip")
    public void test_skip(String text) throws IOException {
        JsonInput input = new JsonInput(new StringReader(text + ','));
        input.skipData();
        assertThat(input.readEvent()).isEqualTo(JsonEvent.COMMA);
    }

    @Test
    public void test_skip_bad() throws IOException {
        JsonInput input = new JsonInput(new StringReader(","));
        assertThatIllegalArgumentException()
            .isThrownBy(() -> input.skipData());
    }

}
