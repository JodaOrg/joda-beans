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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatRuntimeException;
import static org.assertj.core.api.Assertions.offset;
import static org.joda.beans.ser.bin.BeanPack.BIN_16;
import static org.joda.beans.ser.bin.BeanPack.BIN_8;
import static org.joda.beans.ser.bin.BeanPack.BYTE_8;
import static org.joda.beans.ser.bin.BeanPack.CHAR_16;
import static org.joda.beans.ser.bin.BeanPack.DATE;
import static org.joda.beans.ser.bin.BeanPack.DATE_PACKED;
import static org.joda.beans.ser.bin.BeanPack.DOUBLE_64;
import static org.joda.beans.ser.bin.BeanPack.DOUBLE_ARRAY_16;
import static org.joda.beans.ser.bin.BeanPack.DOUBLE_ARRAY_8;
import static org.joda.beans.ser.bin.BeanPack.DOUBLE_INT_8;
import static org.joda.beans.ser.bin.BeanPack.DURATION;
import static org.joda.beans.ser.bin.BeanPack.FLOAT_32;
import static org.joda.beans.ser.bin.BeanPack.INSTANT;
import static org.joda.beans.ser.bin.BeanPack.INT_16;
import static org.joda.beans.ser.bin.BeanPack.INT_32;
import static org.joda.beans.ser.bin.BeanPack.LONG_16;
import static org.joda.beans.ser.bin.BeanPack.LONG_32;
import static org.joda.beans.ser.bin.BeanPack.LONG_64;
import static org.joda.beans.ser.bin.BeanPack.LONG_8;
import static org.joda.beans.ser.bin.BeanPack.MIN_FIX_STR;
import static org.joda.beans.ser.bin.BeanPack.SHORT_16;
import static org.joda.beans.ser.bin.BeanPack.STR_8;
import static org.joda.beans.ser.bin.BeanPack.TIME;
import static org.joda.beans.ser.bin.BeanPack.TYPE_CODE_GRID;
import static org.joda.beans.ser.bin.BeanPack.UTF_8;
import static org.joda.beans.ser.bin.JodaBeanBinFormat.PACKED;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmDefault;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmKey;
import org.joda.beans.sample.ImmKeyList;
import org.joda.beans.sample.ImmNamedKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.JodaConvertBean;
import org.joda.beans.sample.JodaConvertWrapper;
import org.joda.beans.sample.Pair;
import org.joda.beans.sample.PrimitiveBean;
import org.joda.beans.sample.RiskLevel;
import org.joda.beans.sample.TupleFinal;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.joda.collect.grid.DenseGrid;
import org.joda.collect.grid.SparseGrid;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.ThrowingConsumer;

import com.google.common.io.Resources;

/**
 * Test property roundtrip using binary.
 */
class TestSerializePackedBin {

    @Test
    void test_writeAddress() throws IOException {
        var bean = SerTestHelper.testAddress();

        var bytes = JodaBeanSer.PRETTY.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/Address1.packbinstr");

        var parsed = (Address) JodaBeanSer.PRETTY.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmAddress() throws IOException {
        var bean = SerTestHelper.testImmAddress(false);
        var bytes = JodaBeanSer.PRETTY.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmAddress1.packbinstr");

        var parsed = (ImmAddress) JodaBeanSer.PRETTY.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmAddressCached() throws IOException {
        var bean = SerTestHelper.testImmAddress(false);
        var bytes = JodaBeanSer.PRETTY.withBeanValueClasses(Set.of(ImmPerson.class)).binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmAddressCached1.packbinstr");

        var parsed = (ImmAddress) JodaBeanSer.PRETTY.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeImmOptional() throws IOException {
        var bean = SerTestHelper.testImmOptional();
        var bytes = JodaBeanSer.PRETTY.withIncludeDerived(true).binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmOptional1.packbinstr");

        var parsed = (ImmOptional) JodaBeanSer.PRETTY.binReader().read(bytes);
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
        var bytes = JodaBeanSer.PRETTY.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/ImmArrays1.packbinstr");

        var parsed = JodaBeanSer.PRETTY.binReader().read(bytes, ImmArrays.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeCollections() throws IOException {
        var bean = SerTestHelper.testCollections(true);
        var bytes = JodaBeanSer.PRETTY.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));
        assertEqualsSerialization(bytes, "/org/joda/beans/ser/Collections1.packbinstr");

        @SuppressWarnings("unchecked")
        var parsed = (ImmGuava<String>) JodaBeanSer.PRETTY.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    private static void assertEqualsSerialization(byte[] actualBytes, String expectedResource) throws IOException {
        var url = TestSerializePackedBin.class.getResource(expectedResource);
        var expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        var actual = JodaBeanBinReader.visualize(actualBytes);
        assertThat(actual.trim().replace(lineSeparator(), "\n")).isEqualTo(expected.trim().replace(lineSeparator(), "\n"));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_writeJodaConvertInterface() {
        var bean = SerTestHelper.testGenericInterfaces();

        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeIntermediateInterface() {
        var bean = SerTestHelper.testIntermediateInterfaces();

        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmKeyList.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_writeJodaConvert() {
        // immutable bean that is serialized as joda convert
        var bean = ImmNamedKey.of("name");

        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean);
//        System.out.println(JodaBeanBinReader.visualize(bytes));

        var parsed = (ImmNamedKey) JodaBeanSer.COMPACT.binReader().read(bytes);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_readWrite_primitives() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(6);
        out.writeString("tru");
        out.writeBoolean(true);
        out.writeString("fal");
        out.writeBoolean(false);
        out.writeString("byt");
        out.writeByte((byte) 1);
        out.writeString("sht");
        out.writeShort((short) 2);
        out.writeString("flt");
        out.writeFloat(1.2f);
        out.writeString("dbl");
        out.writeDouble(1.8d);
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("tru", Boolean.TRUE);
        bean.set("fal", Boolean.FALSE);
        bean.set("byt", Byte.valueOf((byte) 1));
        bean.set("sht", Short.valueOf((short) 2));
        bean.set("flt", Float.valueOf(1.2f));
        bean.set("dbl", Double.valueOf(1.8d));
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_read_primitiveTypeChanged() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("a");
        out.writeInt(6);
        out.writeString("b");
        out.writeLong(5);
        var bytes = baos.toByteArray();
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_readWrite_booleanAsObject() throws Throwable {
        assertValueSerializesAsObject(Boolean.TRUE, BeanPack.TRUE, out -> {});
        assertValueSerializesAsObject(Boolean.FALSE, BeanPack.FALSE, out -> {});
    }

    @Test
    void test_readWrite_charAsObject() throws Throwable {
        assertValueSerializesAsObject(Character.MAX_VALUE, CHAR_16, out -> out.writeChar(Character.MAX_VALUE));
        assertValueSerializesAsObject(Character.MIN_VALUE, CHAR_16, out -> out.writeChar(Character.MIN_VALUE));
    }

    @Test
    void test_readWrite_byteAsObject() throws Throwable {
        assertValueSerializesAsObject(Byte.MAX_VALUE, BYTE_8, out -> out.writeByte(Byte.MAX_VALUE));
        assertValueSerializesAsObject(Byte.MIN_VALUE, BYTE_8, out -> out.writeByte(Byte.MIN_VALUE));
    }

    @Test
    void test_readWrite_shortAsObject() throws Throwable {
        assertValueSerializesAsObject(Short.MAX_VALUE, SHORT_16, out -> out.writeShort(Short.MAX_VALUE));
        assertValueSerializesAsObject(Short.MIN_VALUE, SHORT_16, out -> out.writeShort(Short.MIN_VALUE));
    }

    @Test
    void test_readWrite_intAsObject() throws Throwable {
        assertValueSerializesAsObject(127, 127, out -> {});
        assertValueSerializesAsObject(0, 0, out -> {});
        assertValueSerializesAsObject(-16, -16, out -> {});
        assertValueSerializesAsObject(-17, INT_16, out -> out.writeShort(-17));
        assertValueSerializesAsObject(-128, INT_16, out -> out.writeShort(-128));
        assertValueSerializesAsObject((int) Short.MAX_VALUE, INT_16, out -> out.writeShort(Short.MAX_VALUE));
        assertValueSerializesAsObject((int) Short.MIN_VALUE, INT_16, out -> out.writeShort(Short.MIN_VALUE));
        assertValueSerializesAsObject(Integer.MAX_VALUE, INT_32, out -> out.writeInt(Integer.MAX_VALUE));
        assertValueSerializesAsObject(Integer.MIN_VALUE, INT_32, out -> out.writeInt(Integer.MIN_VALUE));
    }

    @Test
    void test_readWrite_longAsObject() throws Throwable {
        assertValueSerializesAsObject(127L, LONG_8, out -> out.writeByte(127));
        assertValueSerializesAsObject(0L, LONG_8, out -> out.writeByte(0));
        assertValueSerializesAsObject(-128L, LONG_8, out -> out.writeByte(-128));
        assertValueSerializesAsObject((long) Short.MAX_VALUE, LONG_16, out -> out.writeShort(Short.MAX_VALUE));
        assertValueSerializesAsObject((long) Short.MIN_VALUE, LONG_16, out -> out.writeShort(Short.MIN_VALUE));
        assertValueSerializesAsObject((long) Integer.MAX_VALUE, LONG_32, out -> out.writeInt(Integer.MAX_VALUE));
        assertValueSerializesAsObject((long) Integer.MIN_VALUE, LONG_32, out -> out.writeInt(Integer.MIN_VALUE));
        assertValueSerializesAsObject(Long.MAX_VALUE, LONG_64, out -> out.writeLong(Long.MAX_VALUE));
        assertValueSerializesAsObject(Long.MIN_VALUE, LONG_64, out -> out.writeLong(Long.MIN_VALUE));
    }

    @Test
    void test_readWrite_floatAsObject() throws Throwable {
        assertValueSerializesAsObject(Float.MAX_VALUE, FLOAT_32, out -> out.writeFloat(Float.MAX_VALUE));
        assertValueSerializesAsObject(Float.MIN_VALUE, FLOAT_32, out -> out.writeFloat(Float.MIN_VALUE));
        assertValueSerializesAsObject(Float.NaN, FLOAT_32, out -> out.writeFloat(Float.NaN));
        assertValueSerializesAsObject(Float.POSITIVE_INFINITY, FLOAT_32, out -> out.writeFloat(Float.POSITIVE_INFINITY));
        assertValueSerializesAsObject(Float.NEGATIVE_INFINITY, FLOAT_32, out -> out.writeFloat(Float.NEGATIVE_INFINITY));
    }

    @Test
    void test_readWrite_doubleAsObject() throws Throwable {
        assertValueSerializesAsObject(0d, DOUBLE_INT_8, out -> out.writeByte(0));
        assertValueSerializesAsObject((double) 127, DOUBLE_INT_8, out -> out.writeByte(127));
        assertValueSerializesAsObject((double) -1, DOUBLE_INT_8, out -> out.writeByte(-1));
        assertValueSerializesAsObject((double) -128, DOUBLE_INT_8, out -> out.writeByte(-128));

        assertValueSerializesAsObject(-0d, DOUBLE_64, out -> out.writeDouble(-0f));
        assertValueSerializesAsObject(1.1d, DOUBLE_64, out -> out.writeDouble(1.1d));
        assertValueSerializesAsObject(Double.MAX_VALUE, DOUBLE_64, out -> out.writeDouble(Double.MAX_VALUE));
        assertValueSerializesAsObject(Double.MIN_VALUE, DOUBLE_64, out -> out.writeDouble(Double.MIN_VALUE));
        assertValueSerializesAsObject(Double.NaN, DOUBLE_64, out -> out.writeDouble(Double.NaN));
        assertValueSerializesAsObject(Double.POSITIVE_INFINITY, DOUBLE_64, out -> out.writeDouble(Double.POSITIVE_INFINITY));
        assertValueSerializesAsObject(Double.NEGATIVE_INFINITY, DOUBLE_64, out -> out.writeDouble(Double.NEGATIVE_INFINITY));
    }

    @Test
    void test_readWrite_dateAsObject() throws Throwable {
        assertValueSerializesAsObject(LocalDate.of(2024, 6, 1), DATE_PACKED, out -> out.writeShort((short) ((24 * 12 + 5 << 5) + 1)));
        assertValueSerializesAsObject(LocalDate.of(2000, 1, 1), DATE_PACKED, out -> out.writeShort((short) ((0 * 12 + 0 << 5) + 1)));
        assertValueSerializesAsObject(LocalDate.of(2169, 12, 31), DATE_PACKED, out -> out.writeShort((short) ((169 * 12 + 11 << 5) + 31)));
        assertValueSerializesAsObject(LocalDate.of(2170, 1, 1), DATE, out -> {
            out.writeByte((byte) 0);
            out.writeInt((2170 << 9) + (1 << 5) + 1);
        });
        assertValueSerializesAsObject(LocalDate.of(1999, 12, 31), DATE, out -> {
            out.writeByte((byte) 0);
            out.writeInt((1999 << 9) + (12 << 5) + 31);
        });
        assertValueSerializesAsObject(LocalDate.MAX, DATE, out -> {
            out.writeByte((byte) (999999999 >> 23));
            out.writeInt((999999999 << 9) + (12 << 5) + 31);
        });
        assertValueSerializesAsObject(LocalDate.MIN, DATE, out -> {
            out.writeByte((byte) (-999999999 >> 23));
            out.writeInt((-999999999 << 9) + (1 << 5) + 1);
        });
    }

    @Test
    void test_readWrite_timeAsObject() throws Throwable {
        assertValueSerializesAsObject(LocalTime.MAX, TIME, out -> {
            out.writeShort((short) (LocalTime.MAX.toNanoOfDay() >> 32));
            out.writeInt((int) LocalTime.MAX.toNanoOfDay());
        });
        assertValueSerializesAsObject(LocalTime.MIN, TIME, out -> {
            out.writeShort((byte) 0);
            out.writeInt(0);
        });
    }

    @Test
    void test_readWrite_instantAsObject() throws Throwable {
        assertValueSerializesAsObject(Instant.MAX, INSTANT, out -> {
            out.writeLong(Instant.MAX.getEpochSecond());
            out.writeInt(Instant.MAX.getNano());
        });
        assertValueSerializesAsObject(Instant.EPOCH, INSTANT, out -> {
            out.writeLong(Instant.EPOCH.getEpochSecond());
            out.writeInt(Instant.EPOCH.getNano());
        });
        assertValueSerializesAsObject(Instant.MIN, INSTANT, out -> {
            out.writeLong(Instant.MIN.getEpochSecond());
            out.writeInt(Instant.MIN.getNano());
        });
    }

    @Test
    void test_readWrite_durationAsObject() throws Throwable {
        assertValueSerializesAsObject(Duration.ofSeconds(Long.MAX_VALUE, 999_999_999), DURATION, out -> {
            out.writeLong(Long.MAX_VALUE);
            out.writeInt(999_999_999);
        });
        assertValueSerializesAsObject(Duration.ofSeconds(Long.MIN_VALUE), DURATION, out -> {
            out.writeLong(Long.MIN_VALUE);
            out.writeInt(0);
        });
    }

    @Test
    void test_readWrite_stringAsObject() throws Throwable {
        assertValueSerializesAsObject("", MIN_FIX_STR + 0, out -> {});
        assertValueSerializesAsObject("A", MIN_FIX_STR + 1, out -> out.write("A".getBytes(UTF_8)));
        assertValueSerializesAsObject("A".repeat(12), MIN_FIX_STR + 12, out -> out.write("A".repeat(12).getBytes(UTF_8)));
        assertValueSerializesAsObject("A".repeat(40), MIN_FIX_STR + 40, out -> out.write("A".repeat(40).getBytes(UTF_8)));
        assertValueSerializesAsObject("A".repeat(41), STR_8, out -> {
            out.writeByte(41);
            out.write("A".repeat(41).getBytes(UTF_8));
        });
        assertValueSerializesAsObject("A".repeat(255), STR_8, out -> {
            out.writeByte(255);
            out.write("A".repeat(255).getBytes(UTF_8));
        });
    }

    @Test
    void test_readWrite_byteArrayAsObject() throws Throwable {
        assertValueSerializesAsObject(new byte[] {}, BIN_8, out -> out.writeByte(0));
        assertValueSerializesAsObject(new byte[] {5}, BIN_8, out -> {
            out.writeByte(1);
            out.writeByte(5);
        });
        assertValueSerializesAsObject(new byte[255], BIN_8, out -> {
            out.writeByte(255);
            out.write(new byte[255]);
        });
        assertValueSerializesAsObject(new byte[256], BIN_16, out -> {
            out.writeShort(256);
            out.write(new byte[256]);
        });
    }

    @Test
    void test_readWrite_doubleArrayAsObject() throws Throwable {
        assertValueSerializesAsObject(new double[] {}, DOUBLE_ARRAY_8, out -> out.writeByte(0));
        assertValueSerializesAsObject(new double[] {5.23d}, DOUBLE_ARRAY_8, out -> {
            out.writeByte(1);
            out.writeDouble(5.23d);
        });
        assertValueSerializesAsObject(new double[255], DOUBLE_ARRAY_8, out -> {
            out.writeByte(255);
            out.write(new byte[255 * 8]);
        });
        assertValueSerializesAsObject(new double[256], DOUBLE_ARRAY_16, out -> {
            out.writeShort(256);
            out.write(new byte[256 * 8]);
        });
    }

    private static void assertValueSerializesAsObject(Object value, int typeByte, ThrowingConsumer<DataOutputStream> fn) throws Throwable {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        baos.write(typeByte);
        fn.accept(new DataOutputStream(baos));
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("value", value);
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-------------------------------------------------------------------------
    @Test
    void test_readWrite_optionalAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("present");
        out.writeTypeReference(BeanPack.TYPE_CODE_OPTIONAL);
        out.writeArrayHeader(1);
        out.writeInt(6);
        out.writeString("empty");
        out.writeTypeReference(BeanPack.TYPE_CODE_OPTIONAL);
        out.writeArrayHeader(0);
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("present", Optional.of(6));
        bean.set("empty", Optional.empty());
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWrite_stringArrayAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        out.writeTypeReference(BeanPack.TYPE_CODE_STRING_ARRAY);
        out.writeArrayHeader(2);
        out.writeString("A");
        out.writeString("B");
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("value", new String[] {"A", "B"});
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWrite_objectArrayAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        out.writeTypeReference(BeanPack.TYPE_CODE_OBJECT_ARRAY);
        out.writeArrayHeader(2);
        out.writeString("A");
        out.writeInt(6);
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("value", new Object[] {"A", 6});
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWrite_intArrayAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        out.writeTypeName("int[]");
        out.writeArrayHeader(2);
        out.writeInt(4);
        out.writeInt(6);
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("value", new int[] {4, 6});
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-------------------------------------------------------------------------
    @Test
    void test_readWrite_beanValueClass() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(3);
        out.writeString("value1");
        out.writeValueDefinitionHeader();
        out.writeTypeName(ImmKey.class.getName());
        out.writeBeanDefinitionHeader(1);
        out.writeString("name");
        out.writeString("A");
        out.writeString("value2");
        out.writeValueDefinitionHeader();
        out.writeTypeReference(0);
        out.writeArrayHeader(1);
        out.writeString("B");
        out.writeString("value3");
        out.writeValueReference(2);
        var expected = baos.toByteArray();

        var bean = new FlexiBean();
        bean.set("value1", ImmKey.builder().name("A").build());
        bean.set("value2", ImmKey.builder().name("B").build());
        bean.set("value3", ImmKey.builder().name("A").build());
        var bytes = JodaBeanSer.COMPACT.withBeanValueClasses(Set.of(ImmKey.class)).binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
        assertThat(parsed.get("value1")).isSameAs(parsed.get("value3"));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_readWrite_sparseGridAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        out.writeTypeReference(TYPE_CODE_GRID);
        out.writeArrayHeader(3);
        out.writeInt(4);
        out.writeInt(2);
        out.writeArrayHeader(6);
        out.writeInt(0);
        out.writeInt(1);
        out.writeString("A");
        out.writeInt(1);
        out.writeInt(0);
        out.writeString("B");
        var expected = baos.toByteArray();

        var grid = SparseGrid.create(4, 2);
        grid.put(0, 1, "A");
        grid.put(1, 0, "B");
        var bean = new FlexiBean();
        bean.set("value", grid);
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    void test_readWrite_denseGridAsObject() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString("value");
        out.writeTypeReference(TYPE_CODE_GRID);
        out.writeArrayHeader(3);
        out.writeInt(-2);
        out.writeInt(2);
        out.writeArrayHeader(4);
        out.writeNull();
        out.writeString("A");
        out.writeString("B");
        out.writeNull();
        var expected = baos.toByteArray();

        var grid = DenseGrid.create(2, 2);
        grid.put(0, 1, "A");
        grid.put(1, 0, "B");
        var bean = new FlexiBean();
        bean.set("value", grid);
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-------------------------------------------------------------------------
    @Test
    void test_read_typeConvertToDouble() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueDouble(126);
        assertIntegralConvert(bean, "valueDouble", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueDouble", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueDouble", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueDouble", out -> out.writeByte((byte) 126));
    }

    @Test
    void test_read_typeConvertToFloat() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueFloat(126);
        assertIntegralConvert(bean, "valueFloat", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueFloat", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueFloat", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueFloat", out -> out.writeByte((byte) 126));
    }

    @Test
    void test_read_typeConvertToLong() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueLong(126);
        assertIntegralConvert(bean, "valueLong", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueLong", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueLong", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueLong", out -> out.writeByte((byte) 126));
    }

    @Test
    void test_read_typeConvertToInt() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueInt(126);
        assertIntegralConvert(bean, "valueInt", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueInt", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueInt", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueInt", out -> out.writeByte((byte) 126));
        assertIntegralConvertBad("valueInt", out -> out.writeLong(((long) Integer.MAX_VALUE) + 1));
        assertIntegralConvertBad("valueInt", out -> out.writeLong(((long) Integer.MIN_VALUE) - 1));
    }

    @Test
    void test_read_typeConvertToShort() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueShort((short) 126);
        assertIntegralConvert(bean, "valueShort", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueShort", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueShort", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueShort", out -> out.writeByte((byte) 126));
        assertIntegralConvertBad("valueShort", out -> out.writeInt(Short.MAX_VALUE + 1));
        assertIntegralConvertBad("valueShort", out -> out.writeInt(Short.MIN_VALUE - 1));
        assertIntegralConvertBad("valueShort", out -> out.writeLong(((long) Short.MAX_VALUE) + 1));
        assertIntegralConvertBad("valueShort", out -> out.writeLong(((long) Short.MIN_VALUE) - 1));
    }

    @Test
    void test_read_typeConvertToByte() throws Throwable {
        var bean = new PrimitiveBean();
        bean.setValueByte((byte) 126);
        assertIntegralConvert(bean, "valueByte", out -> out.writeLong(126));
        assertIntegralConvert(bean, "valueByte", out -> out.writeInt(126));
        assertIntegralConvert(bean, "valueByte", out -> out.writeShort((short) 126));
        assertIntegralConvert(bean, "valueByte", out -> out.writeByte((byte) 126));
        assertIntegralConvertBad("valueByte", out -> out.writeLong(128));
        assertIntegralConvertBad("valueByte", out -> out.writeLong(-129));
        assertIntegralConvertBad("valueByte", out -> out.writeInt(128));
        assertIntegralConvertBad("valueByte", out -> out.writeInt(-129));
        assertIntegralConvertBad("valueByte", out -> out.writeShort((short) 128));
        assertIntegralConvertBad("valueByte", out -> out.writeShort((short) -129));
    }

    private static void assertIntegralConvert(PrimitiveBean bean, String fieldName, ThrowingConsumer<BeanPackOutput> outFn)
            throws Throwable {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString(fieldName);
        outFn.accept(out);
        BeanAssert.assertBeanEquals(bean, JodaBeanSer.COMPACT.binReader().read(baos.toByteArray(), PrimitiveBean.class));
    }

    private static void assertIntegralConvertBad(String fieldName, ThrowingConsumer<BeanPackOutput> outFn) throws Throwable {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(1);
        out.writeString(fieldName);
        outFn.accept(out);
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(baos.toByteArray(), PrimitiveBean.class));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_read_beanProperty_typeDefinitionInRemovedSubTree_explicitPropertyType() throws Throwable {
        var bean = new Pair();
        bean.setFirst(RiskLevel.HIGH);
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#removed' of type TupleFinal
        out.writeString("removed");
        out.writeTypeName(TupleFinal.class.getName());  // removed property defined with explicit type
        out.writeMapHeader(1);
        out.writeString("first");
        out.writeTypeName(RiskLevel.class.getName());
        out.writeString("LOW");
        // property 'Pair#first' of type RiskLevel
        out.writeString("first");
        out.writeValueDefinitionHeader();
        out.writeTypeReference(1);
        out.writeString(RiskLevel.HIGH.name());
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    @Test
    void test_read_beanProperty_typeDefinitionInRemovedSubTree_inferredPropertyTypeForBeanInMapFormat() throws Throwable {
        var bean = new Pair();
        bean.setFirst(RiskLevel.HIGH);
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#removed' where the removed property was used to infer the type
        out.writeString("removed");
        out.writeMapHeader(1);
        out.writeString("first");
        out.writeTypeName(RiskLevel.class.getName());
        out.writeString("LOW");
        // property 'Pair#first' of type RiskLevel
        out.writeString("first");
        out.writeValueDefinitionHeader();
        out.writeTypeReference(0);
        out.writeString(RiskLevel.HIGH.name());
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    @Test
    void test_read_beanProperty_typeDefinitionInRemovedSubTree_inferredPropertyTypeForBeanInArrayFormat() throws Throwable {
        var bean = new Pair();
        bean.setFirst(RiskLevel.HIGH);
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#removed' where the removed property was used to infer the type
        out.writeString("removed");
        out.writeArrayHeader(1);  // note that in reality there would have been a bean definition somewhere
        out.writeTypeName(RiskLevel.class.getName());
        out.writeString("LOW");
        // property 'Pair#first' of type RiskLevel
        out.writeString("first");
        out.writeValueDefinitionHeader();
        out.writeTypeReference(0);
        out.writeString(RiskLevel.HIGH.name());
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    @Test
    void test_read_beanProperty_typeDefinitionInRemovedSubTree_typeNoLongerExistsButIsNotReferenced() throws Throwable {
        var bean = new Pair();
        bean.setFirst(RiskLevel.HIGH);
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#removed' where the removed property was used to infer the type
        out.writeString("removed");
        out.writeArrayHeader(1);  // note that in reality there would have been a bean definition somewhere
        out.writeTypeName("my.BadType");
        out.writeString("LOW");
        // property 'Pair#first' of type RiskLevel
        out.writeString("first");
        out.writeValueDefinitionHeader();
        out.writeTypeName(RiskLevel.class.getName());
        out.writeString(RiskLevel.HIGH.name());
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    @Test
    void test_read_beanProperty_beanAndValueDefinitionInRemovedSubTree() throws Throwable {
        var bean = new Pair();
        var subPair = new Pair();
        bean.setFirst(subPair);
        subPair.setFirst(2.5d);
        subPair.setSecond(1.5d);
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#removed'
        out.writeString("removed");
        out.writeTypeName(Pair.class.getName());
        out.writeBeanDefinitionHeader(2);
        out.writeString("first");
        out.writeString("second");
        out.writeValueDefinitionHeader();
        out.writeDouble(1.5d);
        out.writeValueDefinitionHeader();
        out.writeDouble(2.5d);
        // property 'Pair#first' of type Pair
        out.writeString("first");
        out.writeTypeReference(0);
        out.writeArrayHeader(2);
        out.writeValueReference(4);
        out.writeValueReference(3);
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_read_arrayMultidimensional() throws Throwable {
        var bean = new Pair();
        bean.setFirst(new String[][] {{"1", "2"}, {"3"}});
        bean.setSecond(new Number[][][] {{{1}, {2.5}}, {{3}}});
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        // property 'Pair#first'
        out.writeString("first");
        out.writeTypeName("String[][]");
        out.writeArrayHeader(2);
        out.writeArrayHeader(2);
        out.writeString("1");
        out.writeString("2");
        out.writeArrayHeader(1);
        out.writeString("3");
        // property 'Pair#second'
        out.writeString("second");
        out.writeTypeName("java.lang.Number[][][]");
        out.writeArrayHeader(2);
        out.writeArrayHeader(2);
        out.writeArrayHeader(1);
        out.writeInt(1);
        out.writeArrayHeader(1);
        out.writeDouble(2.5d);
        out.writeArrayHeader(1);
        out.writeArrayHeader(1);
        out.writeInt(3);
        var ser = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT);
        BeanAssert.assertBeanEquals(bean, ser.binReader().read(baos.toByteArray(), Pair.class));
    }

    //-------------------------------------------------------------------------
    @Test
    void test_read_optionalTypeToDefaulted() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeString("");
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, ImmDefault.class);
        assertThat(parsed.getValue()).isEqualTo("Defaulted");
    }

    @Test
    void test_readWriteJodaConvertWrapper() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("bean");
        out.writeValueDefinitionHeader();
        out.writeTypeName(JodaConvertBean.class.getName());
        out.writeString("Hello:9");
        out.writeString("description");
        out.writeString("Weird");
        var expected = baos.toByteArray();

        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(wrapper, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    void test_readWriteJodaConvertBean() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("base");
        out.writeString("Hello");
        out.writeString("extra");
        out.writeInt(9);
        var expected = baos.toByteArray();

        var bean = new JodaConvertBean("Hello:9");
        var bytes = JodaBeanSer.COMPACT.binWriter(PACKED).write(bean, false);
        assertThat(bytes).isEqualTo(expected);
        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_read_JodaConvertWrapper_beanNotJodaConvertAndNoDefinition() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("bean");
        out.writeMapHeader(2);
        out.writeString("base");
        out.writeString("Hello");
        out.writeString("extra");
        out.writeInt(9);
        out.writeString("description");
        out.writeString("Weird");
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    void test_read_JodaConvertWrapper_beanJodaConvert() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(2);
        out.writeString("bean");
        out.writeString("Hello:9");
        out.writeString("description");
        out.writeString("Weird");
        var bytes = baos.toByteArray();

        var parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    void test_read_invalidFormat_sizeOneArrayAtRoot() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(1);
        out.writeInt(3);
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    void test_read_wrongVersion() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(0);
        out.writeNull();
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    void test_read_rootTypeNotSpecified_FlexiBean() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();
        assertThatNoException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }

    @Test
    void test_read_rootTypeNotSpecified_Bean() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    void test_read_rootTypeValid_Bean() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeTypeName(FlexiBean.class.getName());
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();
        assertThatNoException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    void test_read_rootTypeInvalid_Bean() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeTypeName(String.class.getName());
        out.writeMapHeader(0);
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class));
    }

    @Test
    void test_read_rootTypeInvalid_incompatible() throws IOException {
        var baos = new ByteArrayOutputStream();
        var out = new BeanPackOutput(baos);
        out.writeArrayHeader(3);
        out.writeInt(3);
        out.writeNull();
        out.writeTypeName(Company.class.getName());
        var bytes = baos.toByteArray();
        assertThatRuntimeException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class));
    }
}
