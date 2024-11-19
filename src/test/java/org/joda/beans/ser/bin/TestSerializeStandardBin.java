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
package org.joda.beans.ser.bin;

import static java.lang.System.lineSeparator;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.offset;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmDefault;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmGenericCollections;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmKeyList;
import org.joda.beans.sample.ImmNamedKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.JodaConvertBean;
import org.joda.beans.sample.JodaConvertInterface;
import org.joda.beans.sample.JodaConvertWrapper;
import org.joda.beans.sample.Person;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

/**
 * Test property roundtrip using binary.
 */
public class TestSerializeStandardBin {

    @Test
    public void test_writeAddress() throws IOException {
        Address bean = SerTestHelper.testAddress();

        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/Address1.binstr");

        Address parsed = (Address) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeImmAddress() throws IOException {
        ImmAddress bean = SerTestHelper.testImmAddress(false);
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmAddress1.binstr");

        ImmAddress parsed = (ImmAddress) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeImmOptional() throws IOException {
        ImmOptional bean = SerTestHelper.testImmOptional();
        byte[] bytes = JodaBeanSer.PRETTY.withIncludeDerived(true).binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmOptional1.binstr");

        ImmOptional parsed = (ImmOptional) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeImmArrays() throws IOException {
        ImmArrays bean = ImmArrays.of(
                new int[] {1, 3, 2},
                new long[] {1, 4, 3},
                new double[] {1.1, 2.2, 3.3},
                new boolean[] {true, false},
                new int[][] {{1, 2}, {2}, {}},
                new boolean[][] {{true, false}, {false}, {}});
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmArrays1.binstr");

        ImmArrays parsed = JodaBeanSer.PRETTY.binReader().read(bytes, ImmArrays.class);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeCollections() throws IOException {
        ImmGuava<String> bean = SerTestHelper.testCollections();
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/Collections1.binstr");

        @SuppressWarnings("unchecked")
        ImmGuava<String> parsed = (ImmGuava<String>) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    private void assertEqualsSerialization(byte[] actualBytes, String expectedResource) throws IOException {
        URL url = TestSerializeStandardBin.class.getResource(expectedResource);
        String expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        String actual = new MsgPackVisualizer(actualBytes).visualizeData();
        assertThat(actual.trim().replace(lineSeparator(), "\n")).isEqualTo(expected.trim().replace(lineSeparator(), "\n"));
    }

    //-------------------------------------------------------------------------
    @Test
    public void test_writeJodaConvertInterface() {
        ImmGenericCollections<JodaConvertInterface> array = SerTestHelper.testGenericInterfaces();
        
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(array);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        
        @SuppressWarnings("unchecked")
        ImmGenericCollections<JodaConvertInterface> bean =
                (ImmGenericCollections<JodaConvertInterface>) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, array);
    }

    @Test
    public void test_writeIntermediateInterface() {
        ImmKeyList array = SerTestHelper.testIntermediateInterfaces();

        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(array);
        //        System.out.println(JodaBeanBinReader.visualize(bytes));

        ImmKeyList bean = JodaBeanSer.COMPACT.binReader().read(bytes, ImmKeyList.class);
        //        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, array);
    }

    @Test
    public void test_writeJodaConvert() {
        // immutable bean that is serialized as joda convert
        ImmNamedKey key = ImmNamedKey.of("name");

        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(key);
        //        System.out.println(JodaBeanBinReader.visualize(bytes));

        ImmNamedKey bean = (ImmNamedKey) JodaBeanSer.COMPACT.binReader().read(bytes);
        //        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, key);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWrite_primitives() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 6);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("tru");
            out.writeByte(MsgPack.TRUE);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("fal");
            out.writeByte(MsgPack.FALSE);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("byt");
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(4);
            out.writeByte(MsgPack.JODA_TYPE_DATA);
            out.writeBytes("Byte");
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("sht");
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(5);
            out.writeByte(MsgPack.JODA_TYPE_DATA);
            out.writeBytes("Short");
            out.writeByte(2);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("flt");
            out.writeByte(MsgPack.FLOAT_32);
            out.writeFloat(1.2f);
            out.writeByte(MsgPack.MIN_FIX_STR + 3);
            out.writeBytes("dbl");
            out.writeByte(MsgPack.FLOAT_64);
            out.writeDouble(1.8d);
        }
        byte[] expected = baos.toByteArray();

        FlexiBean bean = new FlexiBean();
        bean.set("tru", Boolean.TRUE);
        bean.set("fal", Boolean.FALSE);
        bean.set("byt", Byte.valueOf((byte) 1));
        bean.set("sht", Short.valueOf((short) 2));
        bean.set("flt", Float.valueOf(1.2f));
        bean.set("dbl", Double.valueOf(1.8d));
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 1);
            out.writeBytes("a");
            out.writeByte(6);
            out.writeByte(MsgPack.MIN_FIX_STR + 1);
            out.writeBytes("b");
            out.writeByte(5);
        }
        byte[] bytes = baos.toByteArray();
        ImmDoubleFloat parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }
    
    @Test
    public void test_read_optionalTypeToDefaulted() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP);
        }
        byte[] bytes = baos.toByteArray();
        
        ImmDefault parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDefault.class);
        assertThat(parsed.getValue()).isEqualTo("Defaulted");
    }

    @Test
    public void test_readWriteJodaConvertWrapper() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("bean");
            out.writeByte(MsgPack.MIN_FIX_STR + 7);
            out.writeBytes("Hello:9");
            out.writeByte(MsgPack.MIN_FIX_STR + 11);
            out.writeBytes("description");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Weird");
        }
        byte[] expected = baos.toByteArray();

        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(wrapper, false);
        assertThat(bytes).isEqualTo(expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("base");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Hello");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("extra");
            out.writeByte(9);
        }
        byte[] expected = baos.toByteArray();

        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("bean");
            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("base");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Hello");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("extra");
            out.writeByte(9);
            out.writeByte(MsgPack.MIN_FIX_STR + 11);
            out.writeBytes("description");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Weird");
        }
        byte[] bytes = baos.toByteArray();

        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_STR + 7);
            out.writeBytes("Hello:9");
            out.writeByte(9);
        }
        byte[] bytes = baos.toByteArray();

        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertBean.class);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_invalidFormat_sizeOneArrayAtRoot() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(1);
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    public void test_read_wrongVersion() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(-1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    public void test_read_rootTypeNotSpecified_FlexiBean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
    }

    @Test
    public void test_read_rootTypeNotSpecified_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    public void test_read_rootTypeValid_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(FlexiBean.class.getName().length());
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.write(FlexiBean.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.NIL);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class);
    }

    @Test
    public void test_read_rootTypeInvalid_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(String.class.getName().length());
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.write(String.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.NIL);
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    public void test_read_rootTypeInvalid_incompatible() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(Company.class.getName().length());
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.write(Company.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.NIL);
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    public void test_read_invalidFormat_noNilValueAfterType() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(FlexiBean.class.getName().length());
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.write(FlexiBean.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.TRUE);  // should be NIL
        }
        byte[] bytes = baos.toByteArray();
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    public void test_read_byteArray_nullByteArray() {
	assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read((byte[]) null, Company.class));
    }

    @Test
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        assertThatRuntimeException()
            .isThrownBy(() -> JodaBeanSer.COMPACT.binWriter().write(bean));
    }

}
