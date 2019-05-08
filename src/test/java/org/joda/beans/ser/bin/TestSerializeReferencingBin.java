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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmGeneric;
import org.joda.beans.sample.ImmGenericArray;
import org.joda.beans.sample.ImmGenericCollections;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmJodaConvertBean;
import org.joda.beans.sample.ImmJodaConvertWrapper;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmTreeNode;
import org.joda.beans.sample.JodaConvertInterface;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.Test;

/**
 * Test property roundtrip using referencing binary.
 */
public class TestSerializeReferencingBin {

    @Test
    public void test_writeImmOptional() {
        ImmOptional optional = SerTestHelper.testImmOptional();
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(optional);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        ImmOptional bean = (ImmOptional) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeCollections() {
        ImmGuava<String> optional = SerTestHelper.testCollections();
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(optional);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        ImmGuava<String> bean = (ImmGuava<String>) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeJodaConvertInterface() {
        ImmGenericCollections<JodaConvertInterface> array = SerTestHelper.testGenericInterfaces();

        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(array);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        ImmGenericCollections<JodaConvertInterface> bean =
                (ImmGenericCollections<JodaConvertInterface>) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, array);
    }

    @Test
    public void test_writeGenericCollections() {
        ImmGenericCollections<Map<ImmJodaConvertBean, String>> generics = SerTestHelper.testGenericNestedCollections();
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(generics);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        ImmGenericCollections<Map<ImmJodaConvertBean, String>> bean =
                (ImmGenericCollections<Map<ImmJodaConvertBean, String>>) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, generics);
    }

    @Test
    public void test_writeFirstInstanceOfBeanHasNullProperties() {
        ImmGenericArray<ImmGeneric<?>> optional = SerTestHelper.testGenericArrayWithNulls();
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(optional);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        ImmGenericArray<ImmGeneric<?>> bean = (ImmGenericArray<ImmGeneric<?>>) JodaBeanSer.COMPACT.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeTree() {
        ImmTreeNode treeNode = SerTestHelper.testTree();
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(treeNode);
        byte[] regularBytes = JodaBeanSer.COMPACT.binWriter().write(treeNode);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        ImmTreeNode bean = (ImmTreeNode) JodaBeanSer.COMPACT.binReader().read(bytes);
        //System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, treeNode);
        assertTrue(bytes.length < regularBytes.length / 2d);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2); // version
            out.writeByte(0); // refs

            //classes
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmDoubleFloat.class.getName().length());
            out.writeBytes(ImmDoubleFloat.class.getName());
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 1);
            out.writeBytes("a");
            out.writeByte(MsgPack.MIN_FIX_STR + 1);
            out.writeBytes("b");

            // Data
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 3);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(0); // First class

            out.writeByte(6); // a
            out.writeByte(5); // b
        }
        byte[] bytes = baos.toByteArray();

        ImmDoubleFloat parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
        assertEquals(6, parsed.getA(), 1e-10);
        assertEquals(5, parsed.getB(), 1e-10);
    }

    @Test(expected = RuntimeException.class)
    public void test_read_wrongVersion() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(-1);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_read_wrongVersionTooHigh() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(3);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWriteJodaConvertWrapper() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);

            out.writeByte(MsgPack.MIN_FIX_MAP + 1);

            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmJodaConvertWrapper.class.getName().length());
            out.writeBytes(ImmJodaConvertWrapper.class.getName());
            out.write(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("bean");
            out.writeByte(MsgPack.MIN_FIX_STR + 11);
            out.writeBytes("description");

            out.writeByte(MsgPack.MIN_FIX_ARRAY + 3);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(0);

            out.writeByte(MsgPack.MIN_FIX_STR + 7);
            out.writeBytes("Hello:9");

            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Weird");
        }
        byte[] expected = baos.toByteArray();

        ImmJodaConvertBean bean = new ImmJodaConvertBean("Hello:9");
        ImmJodaConvertWrapper wrapper = ImmJodaConvertWrapper.of(bean, "Weird");
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(wrapper);
        assertArrayEquals(bytes, expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertWrapper.class);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmJodaConvertBean.class.getName().length());
            out.writeBytes(ImmJodaConvertBean.class.getName());
            out.write(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("base");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("extra");

            out.writeByte(MsgPack.MIN_FIX_ARRAY + 3);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Hello");
            out.writeByte(9);
        }
        byte[] expected = baos.toByteArray();

        ImmJodaConvertBean bean = new ImmJodaConvertBean("Hello:9");
        byte[] bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(bean);

        assertArrayEquals(expected, bytes);

        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);

            out.writeByte(MsgPack.MIN_FIX_MAP + 2);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmJodaConvertWrapper.class.getName().length());
            out.writeBytes(ImmJodaConvertWrapper.class.getName());
            out.write(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("bean");
            out.writeByte(MsgPack.MIN_FIX_STR + 11);
            out.writeBytes("description");

            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmJodaConvertBean.class.getName().length());
            out.writeBytes(ImmJodaConvertBean.class.getName());
            out.write(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.MIN_FIX_STR + 4);
            out.writeBytes("base");
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("extra");

            out.writeByte(MsgPack.MIN_FIX_ARRAY + 3);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(0);

            out.writeByte(MsgPack.MIN_FIX_ARRAY + 3);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Hello");
            out.writeByte(9);

            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("Weird");
        }
        byte[] bytes = baos.toByteArray();


        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertWrapper.class);
        ImmJodaConvertBean bean = new ImmJodaConvertBean("Hello:9");
        ImmJodaConvertWrapper wrapper = ImmJodaConvertWrapper.of(bean, "Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_MAP);
            out.writeByte(MsgPack.MIN_FIX_STR + 7);
            out.writeBytes("Hello:9");
        }
        byte[] bytes = baos.toByteArray();

        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
        ImmJodaConvertBean bean = new ImmJodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expected = RuntimeException.class)
    public void test_read_invalidFormat_sizeOneArrayAtRoot() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(2);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_read_rootTypeNotSpecified_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmutableBean.class);
    }

    @Test(expected = RuntimeException.class)
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
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmutableBean.class);
    }

    @Test(expected = RuntimeException.class)
    public void test_read_rootTypeInvalid_incompatible() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(Company.class.getName().length());
            out.write(Company.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(MsgPack.STR_8);
            byte[] companyName = "companyName".getBytes(MsgPack.UTF_8);
            out.write(companyName.length);
            out.write(companyName);
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.write(MsgPack.NIL);
        }
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
    }

}
