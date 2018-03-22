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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmEmpty;
import org.joda.beans.sample.ImmGuava;
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
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test property roundtrip using JSON.
 */
@RunWith(DataProviderRunner.class)
public class TestSerializeJson {

    @Test
    public void test_writeAddress() throws IOException {
        Address address = SerTestHelper.testAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Address.json");
        
        Address bean = (Address) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmAddress() throws IOException {
        ImmAddress address = SerTestHelper.testImmAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmAddress.json");
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmOptional() throws IOException {
        ImmOptional optional = SerTestHelper.testImmOptional();
        String json = JodaBeanSer.PRETTY.withIncludeDerived(true).jsonWriter().write(optional);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/ImmOptional.json");
        
        ImmOptional bean = (ImmOptional) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeCollections() throws IOException {
        ImmGuava<String> optional = SerTestHelper.testCollections();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(optional);
//        System.out.println(json);
        assertEqualsSerialization(json, "/org/joda/beans/ser/Collections.json");
        
        @SuppressWarnings("unchecked")
        ImmGuava<String> bean = (ImmGuava<String>) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    private void assertEqualsSerialization(String json, String expectedResource) throws IOException {
        URL url = TestSerializeJson.class.getResource(expectedResource);
        String expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        assertEquals(json.trim().replace(System.lineSeparator(), "\n"), expected.trim().replace(System.lineSeparator(), "\n"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWriteBeanEmptyChild_pretty() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertEquals(json,
                "{\n \"@bean\": \"org.joda.beans.impl.flexi.FlexiBean\",\n \"element\": \"Test\",\n \"child\": {\n  \"@bean\": \"org.joda.beans.sample.ImmEmpty\"\n }\n}\n");
        FlexiBean parsed = JodaBeanSer.PRETTY.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteBeanEmptyChild_compact() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json,
                "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"element\":\"Test\",\"child\":{\"@bean\":\"org.joda.beans.sample.ImmEmpty\"}}");
        FlexiBean parsed = JodaBeanSer.COMPACT.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        String json = "{\"a\":6,\"b\":5}";
        ImmDoubleFloat parsed = JodaBeanSer.COMPACT.jsonReader().read(json, ImmDoubleFloat.class);
        assertEquals(6, parsed.getA(), 1e-10);
        assertEquals(5, parsed.getB(), 1e-10);
    }

    @Test
    public void test_readWriteJodaConvertWrapper() {
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        String json = JodaBeanSer.COMPACT.jsonWriter().write(wrapper);
        assertEquals(json,
                "{\"@bean\":\"org.joda.beans.sample.JodaConvertWrapper\",\"bean\":\"Hello:9\",\"description\":\"Weird\"}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() {
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.sample.JodaConvertBean\",\"base\":\"Hello\",\"extra\":9}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_boolean_true() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.TRUE);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":true}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_boolean_false() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.FALSE);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":false}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_long() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (long) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Long\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_short() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (short) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Short\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_byte() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (byte) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Byte\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (float) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":6.0}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float_NaN() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Float.NaN);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":\"NaN\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_float_NaN_asNull() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueFloat(Float.NaN);
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":null}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (double) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":6.0}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_alternateFormat() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (double) 6);
        String json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":6.0}}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_NaN() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Double.NaN);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"NaN\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWrite_double_Infinity() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Double.POSITIVE_INFINITY);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"Infinity\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_double_integer_flexiWithTypeAnnotation() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Double.valueOf(6));
        String json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"6\"}}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_double_fromInteger() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueDouble(6d);
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":6}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_double_fromIntegerTooBig() {
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":123456789123456789}";
        JodaBeanSer.COMPACT.jsonReader().read(json);
    }

    @Test
    public void test_read_float_fromInteger() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueFloat(6f);
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":6}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_float_fromIntegerTooBig() {
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueFloat\":123456789123456789}";
        JodaBeanSer.COMPACT.jsonReader().read(json);
    }

    @Test
    public void test_read_double_NaN_asNull() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueDouble(Double.NaN);
        String json = "{\"@bean\":\"org.joda.beans.sample.PrimitiveBean\",\"valueDouble\":null}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        String json =
                "{\"@bean\":\"org.joda.beans.sample.JodaConvertWrapper\",\"bean\":{\"base\":\"Hello\",\"extra\":9},\"description\":\"Weird\"}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() {
        String json = "{\"@type\":\"org.joda.beans.sample.JodaConvertBean\",\"value\":\"Hello:9\"}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteInterfaceKeyMap() {
        ImmKey key1 = ImmKey.builder().name("Alpha").build();
        ImmPerson person1 = ImmPerson.builder().forename("Bob").surname("Builder").build();
        ImmKey key2 = ImmKey.builder().name("Beta").build();
        ImmPerson person2 = ImmPerson.builder().forename("Dana").surname("Dash").build();
        ImmMappedKey mapped = ImmMappedKey.builder().data(ImmutableMap.of(key1, person1, key2, person2)).build();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(mapped);
        
        ImmMappedKey bean = (ImmMappedKey) JodaBeanSer.PRETTY.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, mapped);
    }

    @Test
    public void test_read_badTypeInMap() {
        String json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"element\":{" +
                "\"@meta\": \"Map\"," +
                "\"value\": [[\"work\", {\"@type\": \"com.foo.UnknownEnum\",\"value\": \"BIGWIG\"}]]}}";
        FlexiBean parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).jsonReader().read(json, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", ImmutableMap.of("work", "BIGWIG"));  // converted to a string
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_ignoreProperty() {
        String xml = "{\"name\":\"foo\",\"wibble\":\"ignored\"}";
        ImmKey parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).jsonReader().read(xml, ImmKey.class);
        ImmKey bean = ImmKey.builder().name("foo").build();
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_read_noTypeAttributeAtRoot() {
        JodaBeanSer.COMPACT.jsonReader().read("{}");
    }

    @Test
    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        FlexiBean parsed = JodaBeanSer.COMPACT.jsonReader().read("{}", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeAttributeNotBean() {
        JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"java.lang.Integer\"}", Bean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeInvalid() {
        JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\"}", SimplePerson.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeArgumentInvalid() {
        JodaBeanSer.COMPACT.jsonReader().read("{}", Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.jsonWriter().write(bean);
    }

    //-----------------------------------------------------------------------
    @DataProvider
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
            {"{\"a\":{\"@type\":\"Short\",\"value\":" + (((int) Short.MAX_VALUE) + 1) + "}}"},
            {"{\"a\":{\"@type\":\"Short\",\"value\":" + (((int) Short.MIN_VALUE) - 1) + "}}"},
            {"{\"a\":{\"@type\":\"Byte\",\"value\":128}}"},
            {"{\"a\":{\"@type\":\"Byte\",\"value\":-129}}"},
            {"{\"a\":{\"@meta\":\"List\",\"notvalue\":[]}}"},
            {"{\"a\":{\"@meta\":\"List\",\"value\":{}}}"},
            {"{\"a\":{\"@meta\":\"Map\",\"value\":6}}"},
        };
    }

    @Test(expected = IllegalArgumentException.class)
    @UseDataProvider("data_badFormat")
    public void test_badFormat(String text) throws IOException {
        JodaBeanSer.COMPACT.jsonReader().read(text, FlexiBean.class);
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_writer_nullSettings() {
        new JodaBeanJsonWriter(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_writer_write1_nullBean() {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_writer_write2_nullBean() throws IOException {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null, new StringBuilder());
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_writer_write2_nullAppendable() throws IOException {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(new FlexiBean(), null);
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_reader_nullSettings() {
        new JodaBeanJsonReader(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readReader_null() {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readString_null() {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullReader() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null, Bean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullType() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read(new StringReader(""), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readStringType_nullString() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null, Bean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_reader_readStringType_nullType() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read("", null);
    }

}
