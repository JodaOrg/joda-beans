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

import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.SimpleJson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test property roundtrip using JSON.
 */
@Test
public class TestSerializeJsonSimple {

    public void test_writeSimpleJson() {
        SimpleJson bean = SerTestHelper.testSimpleJson();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        
        SimpleJson parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, SimpleJson.class);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_writeImmOptional() {
        ImmOptional bean = SerTestHelper.testImmOptional();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
//        System.out.println(json);
        
        ImmOptional parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, ImmOptional.class);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_readWriteBeanEmptyChild_pretty() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", new HashMap<String, String>());
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertEquals(json, "{\n \"element\": \"Test\",\n \"child\": {}\n}\n");
        FlexiBean parsed = JodaBeanSer.PRETTY.simpleJsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteBeanEmptyChild_compact() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", new HashMap<String, String>());
        String json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertEquals(json, "{\"element\":\"Test\",\"child\":{}}");
        FlexiBean parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(json, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_readWrite_boolean_true() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.TRUE);
        String json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertEquals(json, "{\"data\":true}");
        FlexiBean parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(new StringReader(json), FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWrite_boolean_false() {
        FlexiBean bean = new FlexiBean();
        bean.set("data", Boolean.FALSE);
        String json = JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
        assertEquals(json, "{\"data\":false}");
        FlexiBean parsed = JodaBeanSer.COMPACT.simpleJsonReader().read(new StringReader(json), FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_read_emptyFlexiBean() {
        FlexiBean parsed = JodaBeanSer.COMPACT.simpleJsonReader().read("{}", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test(expectedExceptions = ClassCastException.class)
    public void test_read_rootTypeArgumentIncorrect() {
        JodaBeanSer.COMPACT.simpleJsonReader().read("{}", Integer.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.simpleJsonWriter().write(bean);
    }

    //-----------------------------------------------------------------------
    @DataProvider(name = "badFormat")
    Object[][] data_badFormat() {
        return new Object[][] {
            {"{,}"},
            {"{1,2}"},
            {"{\"a\",6}"},
            {"{\"a\":[}}"},
        };
    }

    @Test(dataProvider = "badFormat", expectedExceptions = IllegalArgumentException.class)
    public void test_badFormat(String text) throws IOException {
        JodaBeanSer.COMPACT.simpleJsonReader().read(text, FlexiBean.class);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_nullSettings() {
        new JodaBeanSimpleJsonWriter(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write1_nullBean() {
        new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write2_nullBean() throws IOException {
        new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(null, new StringBuilder());
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_writer_write2_nullAppendable() throws IOException {
        new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(new FlexiBean(), null);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_nullSettings() {
        new JodaBeanSimpleJsonReader(null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReader_null() {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((Reader) null, FlexiBean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readString_null() {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((String) null, FlexiBean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullReader() throws IOException {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((Reader) null, Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readReaderType_nullType() throws IOException {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read(new StringReader(""), null);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readStringType_nullString() throws IOException {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read((String) null, Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_reader_readStringType_nullType() throws IOException {
        new JodaBeanSimpleJsonReader(JodaBeanSer.PRETTY).read("", null);
    }

}
