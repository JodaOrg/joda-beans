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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.offset;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.SimpleJson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.io.Resources;

/**
 * Test property roundtrip using JSON.
 */
class TestSerializeJsonSimple {

    @Test
    void test_writeSimpleJson() throws IOException {
        var bean = SerTestHelper.testSimpleJson();
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/SimpleJson2.simplejson");

        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, SimpleJson.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmOptional() throws IOException {
        var bean = SerTestHelper.testImmOptional();
        var json = JodaBeanSer.PRETTY.withIncludeDerived(true).simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmOptional2.simplejson");

        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, ImmOptional.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmArrays() throws IOException {
        var bean = ImmArrays.of(
                new int[] {1, 3, 2},
                new long[] {1, 4, 3},
                new double[] {1.1, 2.2, 3.3},
                new boolean[] {true, false},
                new int[][] {{1, 2}, {2}, {}},
                new boolean[][] {{true, false}, {false}, {}});
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmArrays2.simplejson");

        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, ImmArrays.class);
        BeanAssert.assertBeanEquals(bean, parsed);

        var oldParsed = loadAndParse("/org/joda/beans/ser/ImmArrays1.simplejson", ImmArrays.class);
        BeanAssert.assertBeanEquals(bean, oldParsed);
    }

    @Test
    void test_writeAddress() throws IOException {
        var bean = SerTestHelper.testAddress();
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Address2.simplejson");
        // no round trip with simple JSON, but check that it parses
        assertThatNoException()
                .isThrownBy(() -> JodaBeanSer.PRETTY.withDeserializers(SerDeserializers.LENIENT)
                        .simpleJsonReader().read(json, Address.class));
    }

    @Test
    void test_writeImmAddress() throws IOException {
        var bean = SerTestHelper.testImmAddress(false).toBuilder()
                .mapInMap(new HashMap<>())
                .beanBeanMap(new HashMap<>())
                .build();
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmAddress2.simplejson");
        // no round trip with simple JSON, but check that it parses
        assertThatNoException()
                .isThrownBy(() -> loadAndParse("/org/joda/beans/ser/ImmAddress2.simplejson", ImmAddress.class));
        assertThatNoException()
                .isThrownBy(() -> loadAndParse("/org/joda/beans/ser/ImmAddress1.simplejson", ImmAddress.class));
    }

    @Test
    void test_writeCollections() throws IOException {
        var bean = SerTestHelper.testCollections(false);
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Collections2.simplejson");

        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, ImmGuava.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    private void assertEqualsSerialization(String json, String expectedResource) throws IOException {
        var url = TestSerializeJson.class.getResource(expectedResource);
        var expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        assertThat(json.trim().replace(System.lineSeparator(), "\n"))
                .isEqualTo(expected.trim().replace(System.lineSeparator(), "\n"));
    }

    private <T> T loadAndParse(String expectedResource, Class<T> type) throws IOException {
        var url = TestSerializeJson.class.getResource(expectedResource);
        var text = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        return JodaBeanSer.PRETTY.simpleJsonReader().read(text, type);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_readWriteBeanEmptyChild_pretty() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", new HashMap<String, String>());
        var json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertThat(json).isEqualTo("{\n \"element\": \"Test\",\n \"child\": {}\n}\n");
        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWriteBeanEmptyChild_compact() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", new HashMap<String, String>());
        var json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertThat(json).isEqualTo("{\"element\":\"Test\",\"child\":{}}");
        var parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_read_primitiveTypeChanged() throws IOException {
        var json = "{\"a\":6,\"b\":5}";
        var parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(json, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_readWrite_boolean_true() {
        var bean = new FlexiBean();
        bean.set("data", Boolean.TRUE);
        var json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertThat(json).isEqualTo("{\"data\":true}");
        var parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(new StringReader(json), FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWrite_boolean_false() {
        var bean = new FlexiBean();
        bean.set("data", Boolean.FALSE);
        var json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertThat(json).isEqualTo("{\"data\":false}");
        var parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(new StringReader(json), FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_read_emptyFlexiBean() {
        var parsed = JodaBeanSer.COMPACT.simpleJsonReader().read("{}", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test
    void test_read_rootTypeArgumentIncorrect() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.simpleJsonReader().read("{}", Integer.class));
    }

    @Test
    void test_write_nullKeyInMap() {
        var address = new Address();
        var bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.simpleJsonWriter().write(bean));
    }

    //-----------------------------------------------------------------------
    static Object[][] data_badFormat() {
        return new Object[][] {
                {"{,}"},
                {"{1,2}"},
                {"{\"a\",6}"},
                {"{\"a\":[}}"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_badFormat")
    void test_badFormat(String text) throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.simpleJsonReader().read(text, FlexiBean.class));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_writer_nullSettings() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonWriter(null));
    }

    @Test
    void test_writer_write1_nullBean() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(null));
    }

    @Test
    void test_writer_write2_nullBean() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(null, new StringBuilder()));
    }

    @Test
    void test_writer_write2_nullAppendable() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(new FlexiBean(), null));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_reader_nullSettings() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(null));
    }

    @Test
    void test_reader_readReader_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((Reader) null, FlexiBean.class));
    }

    @Test
    void test_reader_readString_null() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((String) null, FlexiBean.class));
    }

    @Test
    void test_reader_readReaderType_nullReader() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((Reader) null, Bean.class));
    }

    @Test
    void test_reader_readReaderType_nullType() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read(new StringReader(""), null));
    }

    @Test
    void test_reader_readStringType_nullString() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((String) null, Bean.class));
    }

    @Test
    void test_reader_readStringType_nullType() throws IOException {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read("", null));
    }

}
