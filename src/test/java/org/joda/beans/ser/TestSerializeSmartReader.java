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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmEmpty;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.SimpleJson;
import org.joda.beans.test.BeanAssert;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.primitives.Bytes;
import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;

/**
 * Test smart reader.
 */
@RunWith(DataProviderRunner.class)
public class TestSerializeSmartReader {

    @Test
    public void test_binary_address()  throws IOException {
        Address bean = SerTestHelper.testAddress();
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    @Test
    public void test_binary_immAddress()  throws IOException {
        ImmAddress bean = SerTestHelper.testImmAddress();
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    @Test
    public void test_binary_optional()  throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    @Test
    public void test_binary_collections()  throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_binaryReferencing_optional()  throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        byte[] bytes = JodaBeanSer.PRETTY.binWriterReferencing().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    @Test
    public void test_binaryReferencing_collections()  throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        byte[] bytes = JodaBeanSer.PRETTY.binWriterReferencing().write(bean);
        Bean roundtrip = JodaBeanSer.PRETTY.smartReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, roundtrip);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_json_address()  throws IOException {
        Address bean = SerTestHelper.testAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, Address.class);
    }

    @Test
    public void test_json_immAddress()  throws IOException {
        ImmAddress bean = SerTestHelper.testImmAddress();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmAddress.class);
    }

    @Test
    public void test_json_optional()  throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmOptional.class);
    }

    @Test
    public void test_json_collections()  throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        String json = JodaBeanSer.PRETTY.jsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmGuava.class);
    }

    @Test
    public void test_json_minimal() throws IOException {
        assertTrue(JodaBeanSer.COMPACT.smartReader().isKnownFormat(new byte[] {'{', '}'}));
        assertTrue(JodaBeanSer.COMPACT.smartReader().isKnownFormat(new byte[] {'{', '\n', ' ', '}'}));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_simpleJson_empty()  throws IOException {
        ImmEmpty bean = ImmEmpty.builder().build();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmEmpty.class);
    }

    @Test
    public void test_simpleJson_basic()  throws IOException {
        SimpleJson bean = SerTestHelper.testSimpleJson();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, SimpleJson.class);
    }

    @Test
    public void test_simpleJson_optional()  throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmOptional.class);
    }

    @Test
    public void test_simpleJson_collections()  throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        String json = JodaBeanSer.PRETTY.simpleJsonWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, json, bean, ImmGuava.class);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_xml_address()  throws IOException {
        Address bean = SerTestHelper.testAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, xml, bean, Address.class);
    }

    @Test
    public void test_xml_immAddress()  throws IOException {
        ImmAddress bean = SerTestHelper.testImmAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, xml, bean, ImmAddress.class);
    }

    @Test
    public void test_xml_optional()  throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, xml, bean, ImmOptional.class);
    }

    @Test
    public void test_xml_collections()  throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertCharsets(JodaBeanSer.PRETTY, xml, bean, ImmGuava.class);
    }

    @Test
    public void test_xml_minimal() throws IOException {
        byte[] bytes = "<bean></bean>".getBytes(StandardCharsets.UTF_8);
        assertTrue(JodaBeanSer.COMPACT.smartReader().isKnownFormat(bytes));
        assertTrue(JodaBeanSer.COMPACT.smartReader().isKnownFormat(bytes));
    }

    //-----------------------------------------------------------------------
    private static <T extends Bean> void assertCharsets(JodaBeanSer settings, String text, T bean, Class<T> type) {
        byte[] json8Bytes = text.getBytes(StandardCharsets.UTF_8);
        assertTrue(settings.smartReader().isKnownFormat(json8Bytes));
        T smart = settings.smartReader().read(json8Bytes, type);
        BeanAssert.assertBeanEquals(bean, smart);

        byte[] utf8Bytes = Bytes.concat(new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf}, json8Bytes);
        assertTrue(settings.smartReader().isKnownFormat(utf8Bytes));
        T smart8 = settings.smartReader().read(utf8Bytes, type);
        BeanAssert.assertBeanEquals(bean, smart8);
    }

    //-----------------------------------------------------------------------
    @DataProvider
    public static Object[][] data_badFormat() {
        return new Object[][] {
            {"xml"},
            {"<beax"},
            {"{,}"},
            {"{  \t\r\n "},
            {"{1,2}"},
            {"{\"a\",6}"},
            {"{\"a\":[}}"},
            {""},
        };
    }

    @Test(expected = IllegalArgumentException.class)
    @UseDataProvider("data_badFormat")
    public void test_badFormat(String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        JodaBeanSer.COMPACT.smartReader().read(bytes, FlexiBean.class);
    }

    @UseDataProvider("data_badFormat")
    public void test_isKnownFormat_false(String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(bytes));
    }

    @Test
    public void test_isKnownFormat_utf8_wrong() throws IOException {
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xbf, '?'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xef, (byte) 0xbb, (byte) 0xb0, '<'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xef, (byte) 0xb0, (byte) 0xbf, '<'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xe0, (byte) 0xbb, (byte) 0xbf, '<'}));
    }

    @Test
    public void test_isKnownFormat_utf16le_wrong() throws IOException {
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xff, (byte) 0xfe, '?', (byte) 0x00}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xff, (byte) 0xfe, '<', (byte) 0x01}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xff, (byte) 0xf0, '<', (byte) 0x00}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xf0, (byte) 0xfe, '<', (byte) 0x00}));
    }

    @Test
    public void test_isKnownFormat_utf16be_wrong() throws IOException {
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xfe, (byte) 0xff, (byte) 0x00, '?'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xfe, (byte) 0xff, (byte) 0x01, '<'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xfe, (byte) 0xf0, (byte) 0x00, '<'}));
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(
                new byte[] {(byte) 0xf0, (byte) 0xff, (byte) 0x00, '<'}));
    }

    @Test
    public void test_isKnownFormat_binary_false() throws IOException {
        byte[] bytes = new byte[] {(byte) 0x92, (byte) 0x00};
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(bytes));
    }

    @Test
    public void test_isKnownFormat_binaryRef_false() throws IOException {
        byte[] bytes = new byte[] {(byte) 0x94, (byte) 0x00};
        assertFalse(JodaBeanSer.COMPACT.smartReader().isKnownFormat(bytes));
    }

}
