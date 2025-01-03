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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.offset;
import static org.joda.beans.ser.bin.JodaBeanBinFormat.REFERENCING;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.joda.beans.ImmutableBean;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmDefault;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmGeneric;
import org.joda.beans.sample.ImmGenericArray;
import org.joda.beans.sample.ImmGenericCollections;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmJodaConvertBean;
import org.joda.beans.sample.ImmJodaConvertWrapper;
import org.joda.beans.sample.ImmKeyHolder;
import org.joda.beans.sample.ImmKeyList;
import org.joda.beans.sample.ImmNamedKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmTreeNode;
import org.joda.beans.sample.JodaConvertInterface;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.jupiter.api.Test;

import com.google.common.io.Resources;

/**
 * Test property roundtrip using referencing binary.
 */
class TestSerializeReferencingBin {

    @Test
    void test_writeImmAddress() throws IOException {
        var bean = SerTestHelper.testImmAddress(true);
        var bytes = JodaBeanSer.PRETTY.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmAddress1.refbinstr");

        var parsed = (ImmAddress) JodaBeanSer.PRETTY.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmOptional() throws IOException {
        // derived properties are not supported
        var bean = SerTestHelper.testImmOptional();
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmOptional1.refbinstr");

        var parsed = (ImmOptional) JodaBeanSer.COMPACT.binReader().read(bytes);
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
        var bytes = JodaBeanSer.PRETTY.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmArrays1.refbinstr");

        var parsed = JodaBeanSer.PRETTY.binReader().read(bytes, ImmArrays.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeCollections() throws IOException {
        var bean = SerTestHelper.testCollections(true);
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/Collections1.refbinstr");

        @SuppressWarnings("unchecked")
        var parsed = (ImmGuava<String>) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    private void assertEqualsSerialization(byte[] actualBytes, String expectedResource) throws IOException {
        var url = TestSerializeReferencingBin.class.getResource(expectedResource);
        var expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        var actual = new MsgPackVisualizer(actualBytes).visualizeData();
        assertThat(actual.trim().replace(lineSeparator(), "\n")).isEqualTo(expected.trim().replace(lineSeparator(), "\n"));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_writeJodaConvertInterface() {
        var bean = SerTestHelper.testGenericInterfaces();

        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        var parsed = (ImmGenericCollections<JodaConvertInterface>) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeJodaConvertInterfaceCollections() {
        var bean = SerTestHelper.testGenericInterfacesCollections();

        var bytes = JodaBeanSer.COMPACT.binWriterReferencing().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(parsed, bean);
    }

    @Test
    void test_writeIntermediateInterface() {
        var bean = SerTestHelper.testIntermediateInterfaces();

        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmKeyList.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeJodaConvert() {
        // immutable bean that is serialized as joda convert
        var bean = ImmNamedKey.of("name");

        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = (ImmNamedKey) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeGenericCollections() {
        var bean = SerTestHelper.testGenericNestedCollections();
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        var parsed = (ImmGenericCollections<Map<ImmJodaConvertBean, String>>) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeFirstInstanceOfBeanHasNullProperties() {
        var bean = SerTestHelper.testGenericArrayWithNulls();
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        @SuppressWarnings("unchecked")
        var parsed = (ImmGenericArray<ImmGeneric<?>>) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeTree() {
        var bean = SerTestHelper.testTree();
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);
        var regularBytes = JodaBeanSer.COMPACT.binWriter().write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = (ImmTreeNode) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
        assertThat((double) bytes.length).isLessThan(regularBytes.length / 2d);
    }

    //-------------------------------------------------------------------------
    @Test
    void test_read_deserializerReferencesUnseenClass() {
        var immKeyHolder = SerTestHelper.testImmKeyHolder();
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(immKeyHolder);
        var bean = (ImmKeyHolder) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, immKeyHolder);
    }

    @Test
    void test_read_primitiveTypeChanged() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
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
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }

    @Test
    void test_read_optionalTypeToDefaulted() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2); // version
            out.writeByte(0); // refs

            //classes
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(ImmDefault.class.getName().length());
            out.writeBytes(ImmDefault.class.getName());
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(MsgPack.MIN_FIX_STR + 5);
            out.writeBytes("value");

            // Data
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(MsgPack.FIX_EXT_1);
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.writeByte(0);
            out.writeByte(MsgPack.NIL); // value not set
        }
        var bytes = baos.toByteArray();

        ImmDefault parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDefault.class);
        assertThat(parsed.getValue()).isEqualTo("Defaulted");
    }

    @Test
    void test_read_wrongVersion() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(-1);
        }
        var bytes = baos.toByteArray();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class));
    }

    @Test
    void test_read_wrongVersionTooHigh() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(3);
        }
        var bytes = baos.toByteArray();
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_readWriteJodaConvertWrapper() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
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
        var expected = baos.toByteArray();

        var bean = new ImmJodaConvertBean("Hello:9");
        var wrapper = ImmJodaConvertWrapper.of(bean, "Weird");
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(wrapper);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertWrapper.class);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    void test_readWriteJodaConvertBean() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
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
        var expected = baos.toByteArray();

        var bean = new ImmJodaConvertBean("Hello:9");
        var bytes = JodaBeanSer.COMPACT.binWriter(REFERENCING).write(bean);

        assertThat(bytes).isEqualTo(expected);

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_read_nonStandard_JodaConvertWrapper_expanded() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
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
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertWrapper.class);
        var bean = new ImmJodaConvertBean("Hello:9");
        var wrapper = ImmJodaConvertWrapper.of(bean, "Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    void test_read_nonStandard_JodaConvertBean_flattened() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_MAP);
            out.writeByte(MsgPack.MIN_FIX_STR + 7);
            out.writeBytes("Hello:9");
        }
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class);
        var bean = new ImmJodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_read_invalidFormat_sizeOneArrayAtRoot() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(2);
        }
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class));
    }

    @Test
    void test_read_rootTypeNotSpecified_Bean() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP);
        }
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmutableBean.class));
    }

    @Test
    void test_read_rootTypeInvalid_Bean() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
            out.writeByte(1);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.EXT_8);
            out.writeByte(String.class.getName().length());
            out.writeByte(MsgPack.JODA_TYPE_BEAN);
            out.write(String.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.NIL);
        }
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmutableBean.class));
    }

    @Test
    void test_read_rootTypeInvalid_incompatible() throws IOException {
        var baos = new ByteArrayOutputStream();
        try (var out = new DataOutputStream(baos)) {
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 4);
            out.writeByte(2);
            out.writeByte(0);
            out.writeByte(MsgPack.MIN_FIX_MAP + 1);
            out.writeByte(MsgPack.STR_8);
            out.writeByte(Company.class.getName().length());
            out.write(Company.class.getName().getBytes(MsgPack.UTF_8));
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.writeByte(MsgPack.STR_8);
            var companyName = "companyName".getBytes(MsgPack.UTF_8);
            out.write(companyName.length);
            out.write(companyName);
            out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
            out.write(MsgPack.NIL);
        }
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, ImmJodaConvertBean.class));
    }

}
