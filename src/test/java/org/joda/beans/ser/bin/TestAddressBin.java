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
package org.joda.beans.ser.bin;

import static org.testng.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.joda.beans.Bean;
import org.joda.beans.gen.Address;
import org.joda.beans.gen.Company;
import org.joda.beans.gen.CompanyAddress;
import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmPerson;
import org.joda.beans.gen.JodaConvertBean;
import org.joda.beans.gen.JodaConvertWrapper;
import org.joda.beans.gen.Person;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.Test;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableTable;

/**
 * Test property roundtrip using binary.
 */
@Test
public class TestAddressBin {

    public void test_writeAddress() throws IOException {
        Person person = new Person();
        person.setForename("Etienne");
        person.setSurname("Colebourne");
        person.getExtensions().set("interests", "joda");
        person.getExtensions().set("conferenceCount", 21);
        person.getExtensions().set("quality", 'B');
        person.getExtensions().set("company", new Company("OpenGamma"));
        Address address = new Address();
        address.setOwner(person);
        address.setNumber(251);
        address.setStreet("Big Road");
        address.setCity("London & Capital of the World <!>");
        CompanyAddress workAddress = new CompanyAddress();
        workAddress.setCompanyName("OpenGamma");
        workAddress.setNumber(185);
        workAddress.setStreet("Park Street");
        workAddress.setCity("London");
        Address homeAddress = new Address();
        homeAddress.setNumber(65432);
        homeAddress.setStreet("Big Road");
        homeAddress.setCity("Bigton");
        person.setMainAddress(workAddress);
        person.getOtherAddressMap().put("home", homeAddress);
        person.getOtherAddressMap().put("work", workAddress);
        person.getOtherAddressMap().put("other", null);
        person.getAddressList().add(homeAddress);
        person.getAddressList().add(null);
        person.getAddressList().add(workAddress);
        person.getAddressesList().add(ImmutableList.of(homeAddress, workAddress));
        
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(address);
//        new MsgPackVisualizer(bytes).visualize();
        
        Address bean = (Address) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @SuppressWarnings("unchecked")
    public void test_writeImmAddress() throws IOException {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        map.put("A", Arrays.asList("B", "b"));
        Map<String, List<Integer>> map2 = new HashMap<String, List<Integer>>();
        map2.put("A", Arrays.asList(3, 2, 1));
        Map<String, List<List<Integer>>> map3 = new HashMap<String, List<List<Integer>>>();
        map3.put("A", Arrays.asList(Arrays.asList(3, 2, 1)));
        Map<ImmPerson, Map<String, ImmPerson>> map4 = new HashMap<ImmPerson, Map<String, ImmPerson>>();
        Map<String, List<List<Object>>> map5 = new HashMap<String, List<List<Object>>>();
        List<Object> objects1 = Arrays.<Object>asList(Currency.getInstance("GBP"), TimeZone.getTimeZone("Europe/London"));
        List<Object> objects2 = Arrays.<Object>asList(Locale.CANADA_FRENCH, Long.valueOf(2));
//      (Object[]) new String[] {"Str", "Arr"}, Integer.valueOf(3));
//      List<Object> objects2 = Arrays.<Object>asList((Object[]) new Double[] {1.2d, 3.4d}, "Hello");
        map5.put("A", Arrays.asList(objects1));
        map5.put("B", Arrays.asList(objects2));
        ImmPerson person = ImmPerson.builder()
            .forename("Etienne")
            .middleNames(new String[] {"K", "T"})
            .surname("Colebourne")
            .addressList(Arrays.asList(new Address()))
            .codeCounts(ImmutableMultiset.of("A", "A", "B"))
            . build();
        ImmPerson child = ImmPerson.builder()
                .forename("Etiennette")
                .surname("Colebourne")
                . build();
        ImmPerson child2 = ImmPerson.builder()
                .forename("Kylie")
                .surname("Colebourne")
                . build();
        ImmAddress childAddress = ImmAddress.builder()
                .owner(child)
                .number(185)
                .street("Park Street")
                .city("London")
                .data(new byte[] {64, 65, 66})
                .build();
        map4.put(child, ImmutableMap.of("sibling", child2));
        HashBasedTable<Integer, Integer, ImmPerson> table = HashBasedTable.create();
        table.put(1, 1, person);
        table.put(1, 2, child);
        table.put(2, 1, child2);
        ImmAddress address = ImmAddress.builder()
            .owner(person)
            .number(185)
            .street("Park Street")
            .city("London & Capital of the World <!>\n")
            .listInMap(map)
            .listNumericInMap(map2)
            .listInListInMap(map3)
            .objectListInListInMap(map5)
            .mapInMap(map4)
            .simpleTable(ImmutableTable.of(1, 1, "Hello"))
            .compoundTable(table)
            .beanBeanMap(ImmutableMap.of(child, childAddress))
            .build();
        
        byte[] bytes = JodaBeanSer.PRETTY.binWriter().write(address);
//        new MsgPackVisualizer(bytes).visualize();
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.binReader().read(bytes);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    //-----------------------------------------------------------------------
    public void test_readWrite_primitives() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
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
        out.close();
        byte[] expected = baos.toByteArray();
        
        FlexiBean bean = new FlexiBean();
        bean.set("tru", Boolean.TRUE);
        bean.set("fal", Boolean.FALSE);
        bean.set("byt", Byte.valueOf((byte) 1));
        bean.set("sht", Short.valueOf((short) 2));
        bean.set("flt", Float.valueOf(1.2f));
        bean.set("dbl", Double.valueOf(1.8d));
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(bean, false);
        assertEquals(bytes, expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteJodaConvertWrapper() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
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
        out.close();
        byte[] expected = baos.toByteArray();
        
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(wrapper, false);
        assertEquals(bytes, expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_readWriteJodaConvertBean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
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
        out.close();
        byte[] expected = baos.toByteArray();
        
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        byte[] bytes = JodaBeanSer.COMPACT.binWriter().write(bean, false);
        assertEquals(bytes, expected);
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_read_nonStandard_JodaConvertWrapper_expanded() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
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
        out.close();
        byte[] bytes = baos.toByteArray();
        
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertWrapper.class);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_read_nonStandard_JodaConvertBean_flattened() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_STR + 7);
        out.writeBytes("Hello:9");
        out.writeByte(9);
        out.close();
        byte[] bytes = baos.toByteArray();
        
        Bean parsed = JodaBeanSer.COMPACT.binReader().read(bytes, JodaConvertBean.class);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_invalidFormat_sizeOneArrayAtRoot() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 1);
        out.writeByte(1);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_wrongVersion() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(-1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
    }

    @Test
    public void test_read_rootTypeNotSpecified_FlexiBean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_rootTypeNotSpecified_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 0);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class);
    }

    @Test
    public void test_read_rootTypeValid_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 1);
        out.writeByte(MsgPack.EXT_8);
        out.writeByte(FlexiBean.class.getName().length());
        out.writeByte(MsgPack.JODA_TYPE_BEAN);
        out.write(FlexiBean.class.getName().getBytes(MsgPack.UTF_8));
        out.writeByte(MsgPack.NIL);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_rootTypeInvalid_Bean() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 1);
        out.writeByte(MsgPack.EXT_8);
        out.writeByte(String.class.getName().length());
        out.writeByte(MsgPack.JODA_TYPE_BEAN);
        out.write(String.class.getName().getBytes(MsgPack.UTF_8));
        out.writeByte(MsgPack.NIL);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_rootTypeInvalid_incompatible() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 1);
        out.writeByte(MsgPack.EXT_8);
        out.writeByte(Company.class.getName().length());
        out.writeByte(MsgPack.JODA_TYPE_BEAN);
        out.write(Company.class.getName().getBytes(MsgPack.UTF_8));
        out.writeByte(MsgPack.NIL);
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, FlexiBean.class);
    }

    @Test(expectedExceptions = RuntimeException.class)
    public void test_read_invalidFormat_noNilValueAfterType() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        out.writeByte(MsgPack.MIN_FIX_ARRAY + 2);
        out.writeByte(1);
        out.writeByte(MsgPack.MIN_FIX_MAP + 1);
        out.writeByte(MsgPack.EXT_8);
        out.writeByte(FlexiBean.class.getName().length());
        out.writeByte(MsgPack.JODA_TYPE_BEAN);
        out.write(FlexiBean.class.getName().getBytes(MsgPack.UTF_8));
        out.writeByte(MsgPack.TRUE);  // should be NIL
        out.close();
        byte[] bytes = baos.toByteArray();
        JodaBeanSer.COMPACT.binReader().read(bytes, Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.binWriter().write(bean);
    }

}
