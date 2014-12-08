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
package org.joda.beans.ser.json;

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.joda.beans.Bean;
import org.joda.beans.gen.Address;
import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmEmpty;
import org.joda.beans.gen.ImmOptional;
import org.joda.beans.gen.JodaConvertBean;
import org.joda.beans.gen.JodaConvertWrapper;
import org.joda.beans.gen.Person;
import org.joda.beans.gen.PrimitiveBean;
import org.joda.beans.gen.SimplePerson;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test property roundtrip using JSON.
 */
@Test
public class TestSerializeJson {

    public void test_writeAddress() {
        Address address = SerTestHelper.testAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        
        Address bean = (Address) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmAddress() {
        ImmAddress address = SerTestHelper.testImmAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(address);
//        System.out.println(json);
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmOptional() {
        ImmOptional optional = SerTestHelper.testImmOptional();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(optional);
//        System.out.println(json);
        
        ImmOptional bean = (ImmOptional) JodaBeanSer.PRETTY.jsonReader().read(json);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    //-----------------------------------------------------------------------
    public void test_readWriteBeanEmptyChild_pretty() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertEquals(json, "{\n \"@bean\": \"org.joda.beans.impl.flexi.FlexiBean\",\n \"element\": \"Test\",\n \"child\": {\n  \"@bean\": \"org.joda.beans.gen.ImmEmpty\"\n }\n}\n");
        FlexiBean parsed = JodaBeanSer.PRETTY.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteBeanEmptyChild_compact() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"element\":\"Test\",\"child\":{\"@bean\":\"org.joda.beans.gen.ImmEmpty\"}}");
        FlexiBean parsed = JodaBeanSer.COMPACT.jsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteJodaConvertWrapper() {
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        String json = JodaBeanSer.COMPACT.jsonWriter().write(wrapper);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.gen.JodaConvertWrapper\",\"bean\":\"Hello:9\",\"description\":\"Weird\"}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_readWriteJodaConvertBean() {
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.gen.JodaConvertBean\",\"base\":\"Hello\",\"extra\":9}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_boolean_true() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.TRUE);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":true}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_boolean_false() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.FALSE);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":false}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(new StringReader(json));
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_long() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (long) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Long\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_short() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (short) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Short\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_byte() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (byte) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Byte\",\"value\":6}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_float() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (float) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":6.0}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_float_NaN() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Float.NaN);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Float\",\"value\":\"NaN\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_float_NaN_asNull() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueFloat(Float.NaN);
        String json = "{\"@bean\":\"org.joda.beans.gen.PrimitiveBean\",\"valueFloat\":null}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_double() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (double) 6);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":6.0}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_double_alternateFormat() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", (double) 6);
        String json = "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":6.0}}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_double_NaN() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Double.NaN);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"NaN\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_double_NaN_asNull() {
        PrimitiveBean bean = new PrimitiveBean();
        bean.setValueDouble(Double.NaN);
        String json = "{\"@bean\":\"org.joda.beans.gen.PrimitiveBean\",\"valueDouble\":null}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_double_Infinity() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Double.POSITIVE_INFINITY);
        String json = JodaBeanSer.COMPACT.jsonWriter().write(bean);
        assertEquals(json, "{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\",\"data\":{\"@type\":\"Double\",\"value\":\"Infinity\"}}");
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        String json = "{\"@bean\":\"org.joda.beans.gen.JodaConvertWrapper\",\"bean\":{\"base\":\"Hello\",\"extra\":9},\"description\":\"Weird\"}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_read_nonStandard_JodaConvertBean_flattened() {
        String json = "{\"@type\":\"org.joda.beans.gen.JodaConvertBean\",\"value\":\"Hello:9\"}";
        Bean parsed = JodaBeanSer.COMPACT.jsonReader().read(json);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_noTypeAttributeAtRoot() {
        JodaBeanSer.COMPACT.jsonReader().read("{}");
    }

    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        FlexiBean parsed = JodaBeanSer.COMPACT.jsonReader().read("{}", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeAttributeNotBean() {
        JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"java.lang.Integer\"}", Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeInvalid() {
        JodaBeanSer.COMPACT.jsonReader().read("{\"@bean\":\"org.joda.beans.impl.flexi.FlexiBean\"}", SimplePerson.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeArgumentInvalid() {
        JodaBeanSer.COMPACT.jsonReader().read("{}", Integer.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.jsonWriter().write(bean);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "badFormat")
    Object[][] data_badFormat() {
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

    @Test(dataProvider = "badFormat", expectedExceptions = IllegalArgumentException.class)
    public void test_badFormat(String text) throws IOException {
        JodaBeanSer.COMPACT.jsonReader().read(text, FlexiBean.class);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_nullSettings() {
        new JodaBeanJsonWriter(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write1_nullBean() {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write2_nullBean() throws IOException {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(null, new StringBuilder());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write2_nullAppendable() throws IOException {
        new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(new FlexiBean(), null);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_nullSettings() {
        new JodaBeanJsonReader(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReader_null() {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readString_null() {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullReader() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((Reader) null, Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullType() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read(new StringReader(""), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readStringType_nullString() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read((String) null, Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readStringType_nullType() throws IOException {
        new JodaBeanJsonReader(JodaBeanSer.PRETTY).read("", null);
    }

}
