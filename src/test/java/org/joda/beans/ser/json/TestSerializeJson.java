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
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmEmpty;
import org.joda.beans.sample.ImmKey;
import org.joda.beans.sample.ImmMappedKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.JodaConvertBean;
import org.joda.beans.sample.JodaConvertWrapper;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.PrimitiveBean;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

/**
 * Test property roundtrip using JSON.
 */
public class TestSerializeJson {

    @Test
    public void test_writeAddress() throws IOException {
        var address = SerTestHelper.testAddress();
        var json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Address.json");
        
        var bean = (Address) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmAddress() throws IOException {
        var address = SerTestHelper.testImmAddress();
        var json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmAddress.json");
        
        var bean = (ImmAddress) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmOptional() throws IOException {
        var optional = SerTestHelper.testImmOptional();
        var json = JodaBeanSer.PRETTY.withIncludeDerived(true).jsonWriter().write(optional);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmOptional.json");
        
        var bean = (ImmOptional) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeImmArrays() throws IOException {
        var bean = ImmArrays.of(
                new int[] {1, 3, 2},
                new long[] {1, 4, 3},
                new double[] {1.1, 2.2, 3.3},
                new boolean[] {true, false},
                new int[][] {{1, 2}, {2}, {}},
                new boolean[][] {{true, false}, {false}, {}});
        var json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmArrays.json");

        var parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, ImmArrays.class);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeCollections() throws IOException {
        var optional = SerTestHelper.testCollections();
        var json = JodaBeanSer.PRETTY.jsonWriter().write(optional);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Collections.json");
        
        var bean = JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeJodaConvertInterface() {
        var array = SerTestHelper.testGenericInterfaces();
        
        var json = JodaBeanSer.PRETTY.jsonWriter().write(array);
//        System.out.println(json);
        
        var bean = JodaBeanSer.COMPACT.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, array);
    }

    private void assertEqualsSerialization(String json, String expectedResource) throws IOException {
        var url = TestSerializeJson.class.getResource(expectedResource);
        var expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        assertThat(json.trim().replace(System.lineSeparator(), "\n"))
            .isEqualTo(expected.trim().replace(System.lineSeparator(), "\n"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWriteBeanEmptyChild_pretty() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        var json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\n \"@bean\": \"org.joda.beans.impl.flexi.FlexiBean\",\n \"element\": \"Test\",\n \"child\": {\n  \"@bean\": \"org.joda.beans.sample.ImmEmpty\"\n }\n}\n");
        var parsed = JodaBeanSer.PRETTY.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteBeanEmptyChild_compact() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"element\":\"Test\",\"child\":{\"@bean\":\"org.joda.beans.sample.ImmEmpty\"}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        var json = "{\"a\":6,\"b\":5}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }

    @Test
    public void test_readWriteJodaConvertWrapper() {
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        var json = JodaBeanSer.COMPACT.jsonWriter().write(wrapper);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.sample.JodaConvertWrapper\",\"bean\":\"Hello:9\",\"description\":\"Weird\"}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() {
        var bean = new JodaConvertBean("Hello:9");
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.sample.JodaConvertBean\",\"base\":\"Hello\",\"extra\":9}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_boolean_true() {
        var bean = new FlexiBean();
        bean.set("data", Boolean.TRUE);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":true}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_boolean_false() {
        var bean = new FlexiBean();
        bean.set("data", Boolean.FALSE);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":false}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_long() {
        var bean = new FlexiBean();
        bean.set("data", (long) 6);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Long\",\"value\":6}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_short() {
        var bean = new FlexiBean();
        bean.set("data", (short) 6);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Short\",\"value\":6}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_byte() {
        var bean = new FlexiBean();
        bean.set("data", (byte) 6);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Byte\",\"value\":6}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float() {
        var bean = new FlexiBean();
        bean.set("data", (float) 6);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":6.0}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float_NaN() {
        var bean = new FlexiBean();
        bean.set("data", Float.NaN);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":\"NaN\"}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float_NaN_asNull() {
        var bean = new PrimitiveBean();
        bean.setValueFloat(Float.NaN);
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":null}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double() {
        var bean = new FlexiBean();
        bean.set("data", (double) 6);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":6.0}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_alternateFormat() {
        var bean = new FlexiBean();
        bean.set("data", (double) 6);
        var json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":6.0}}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_NaN() {
        var bean = new FlexiBean();
        bean.set("data", Double.NaN);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"NaN\"}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_Infinity() {
        var bean = new FlexiBean();
        bean.set("data", Double.POSITIVE_INFINITY);
        var json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertThat(json)
            .isEqualTo("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"Infinity\"}}");
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_double_integer_flexiWithTypeAnnotation() {
        var bean = new FlexiBean();
        bean.set("data", Double.valueOf(6));
        var json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"6\"}}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_double_fromInteger() {
        var bean = new PrimitiveBean();
        bean.setValueDouble(6d);
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":6}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_double_fromIntegerTooBig() {
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":123456789123456789}";
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read(json));
    }

    @Test
    public void test_read_float_fromInteger() {
        var bean = new PrimitiveBean();
        bean.setValueFloat(6f);
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":6}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_float_fromIntegerTooBig() {
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":123456789123456789}";
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read(json));
    }

    @Test
    public void test_read_double_NaN_asNull() {
        var bean = new PrimitiveBean();
        bean.setValueDouble(Double.NaN);
        var json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":null}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        var json =
                "{\"@bean\":\"org.joda.beans.sample.JodaConvertWrapper\",\"bean\":{\"base\":\"Hello\",\"extra\":9},\"description\":\"Weird\"}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() {
        var json = "{\"@type\":\"org.joda.beans.sample.JodaConvertBean\",\"value\":\"Hello:9\"}";
        var parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        var bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteInterfaceKeyMap() {
        var key1 = ImmKey.builder().name("Alpha").build();
        var person1 = ImmPerson.builder().forename("Bob").surname("Builder").build();
        var key2 = ImmKey.builder().name("Beta").build();
        var person2 = ImmPerson.builder().forename("Dana").surname("Dash").build();
        var mapped = ImmMappedKey.builder().data(ImmutableMap.of(key1, person1, key2, person2)).build();
        var json = JodaBeanSer.PRETTY.jsonWriter().write(mapped);
        
        var bean = (ImmMappedKey) JodaBeanSer.PRETTY.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, mapped);
    }

    @Test
    public void test_read_badTypeInMap() {
        var json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"element\":{" +
                "\"@meta\": \"Map\"," +
                "\"value\": [[\"work\", {\"@type\": \"com.foo.UnknownEnum\",\"value\": \"BIGWIG\"}]]}}";
        var parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).jsonReader().read(json, FlexiBean.class);
        var bean = new FlexiBean();
        bean.set("element", ImmutableMap.of("work", "BIGWIG"));  // converted to a string
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_ignoreProperty() {
        var xml = "{\"name\":\"foo\",\"wibble\":\"ignored\"}";
        var parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).jsonReader().read(xml, ImmKey.class);
        var bean = ImmKey.builder().name("foo").build();
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_noTypeAttributeAtRoot() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read("{}"));
    }

    @Test
    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        var parsed = JodaBeanSer.COMPACT.jsonReader().read("{}", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test
    public void test_read_rootTypeAttributeNotBean() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"java.lang.Integer\"}", Bean.class));
    }

    @Test
    public void test_read_rootTypeInvalid() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\"}", SimplePerson.class));
    }

    @Test
    public void test_read_rootTypeArgumentInvalid() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read("{}", Integer.class));
    }

    @Test
    public void test_write_nullKeyInMap() {
        var address = new Address();
        var bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonWriter().write(bean));
    }

    //-----------------------------------------------------------------------
    public static Object[][] data_badFormat() {
        return new Object[][] {
            {"{,}"},
            {"{1,2}"},
            {"{\"a\",6}"},
            {"{\"a\":[}}"},
            {"{\"a\":{\"@type\":\"Integer\",\"notvalue\":6}}"},
            {"{\"a\":{\"@type\":\"Integer\",\"value\":[]}}"},
            {"{\"a\":{\"@type\":\"Integer\",\"value\":" + (((long) Integer.MAX_VALUE) + 1) + "}}"},
            {"{\"a\":{\"@type\":\"Integer\",\"value\":" + (((long) Integer.MIN_VALUE) - 1) + "}}"},
            {"{\"a\":{\"@type\":\"Short\",\"value\":" + ((Short.MAX_VALUE) + 1) + "}}"},
            {"{\"a\":{\"@type\":\"Short\",\"value\":" + ((Short.MIN_VALUE) - 1) + "}}"},
            {"{\"a\":{\"@type\":\"Byte\",\"value\":128}}"},
            {"{\"a\":{\"@type\":\"Byte\",\"value\":-129}}"},
            {"{\"a\":{\"@meta\":\"List\",\"notvalue\":[]}}"},
            {"{\"a\":{\"@meta\":\"List\",\"value\":{}}}"},
            {"{\"a\":{\"@meta\":\"Map\",\"value\":6}}"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_badFormat")
    public void test_badFormat(String text) throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.jsonReader().read(text, FlexiBean.class));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_writer_nullSettings() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonWriter(null));
    }

    @Test
    public void test_writer_write1_nullBean() {
        assertThatIllegalArgumentException()
            .isThrownBy(() ->  new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null));
    }

    @Test
    public void test_writer_write2_nullBean() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null, new StringBuilder()));
    }

    @Test
    public void test_writer_write2_nullAppendable() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(new FlexiBean(), null));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_reader_nullSettings() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(null));
    }

    @Test
    public void test_reader_readReader_null() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null));
    }

    @Test
    public void test_reader_readString_null() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null));
    }

    @Test
    public void test_reader_readReaderType_nullReader() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null, Bean.class));
    }

    @Test
    public void test_reader_readReaderType_nullType() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read(new StringReader(""), null));
    }

    @Test
    public void test_reader_readStringType_nullString() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null, Bean.class));
    }

    @Test
    public void test_reader_readStringType_nullType() throws IOException {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> new JodaBeanJsonReader(JodaBeanSer.PRETTY).read("", null));
    }

}
