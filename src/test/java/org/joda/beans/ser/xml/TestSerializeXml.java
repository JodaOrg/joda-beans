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
package org.joda.beans.ser.xml;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

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
import org.joda.beans.sample.SimpleName;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;

/**
 * Test property roundtrip using XML.
 */
public class TestSerializeXml {

    @Test
    public void test_writeAddress() {
        Address address = SerTestHelper.testAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
//        System.out.println(xml);
        
        Address bean = (Address) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmAddress() {
        ImmAddress address = SerTestHelper.testImmAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
        
        xml = xml.replace("185", "18<!-- comment -->5");
//        System.out.println(xml);
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    @Test
    public void test_writeImmOptional() {
        ImmOptional optional = SerTestHelper.testImmOptional();
        String xml = JodaBeanSer.PRETTY.withIncludeDerived(true).xmlWriter().write(optional);
//        System.out.println(xml);
        
        ImmOptional bean = (ImmOptional) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    @Test
    public void test_writeCollections() {
        ImmGuava<String> optional = SerTestHelper.testCollections();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(optional);
//        System.out.println(xml);
        
        @SuppressWarnings("unchecked")
        ImmGuava<String> bean = (ImmGuava<String>) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWriteBeanEmptyChild_pretty() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertEquals(xml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bean type=\"org.joda.beans.impl.flexi.FlexiBean\">\n <element>Test</element>\n <child type=\"org.joda.beans.sample.ImmEmpty\"/>\n</bean>\n");
        FlexiBean parsed = JodaBeanSer.PRETTY.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteBeanEmptyChild_compact() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertEquals(xml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.impl.flexi.FlexiBean\"><element>Test</element><child type=\"org.joda.beans.sample.ImmEmpty\"/></bean>");
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteJodaConvertWrapper() {
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(wrapper);
        assertEquals(xml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.sample.JodaConvertWrapper\"><bean>Hello:9</bean><description>Weird</description></bean>");
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() {
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertEquals(xml,
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.sample.JodaConvertBean\"><base>Hello</base><extra>9</extra></bean>");
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        String json = "<bean><a>6</a><b>5</b></bean>}";
        ImmDoubleFloat parsed = JodaBeanSer.COMPACT.xmlReader().read(json, ImmDoubleFloat.class);
        assertEquals(6, parsed.getA(), 1e-10);
        assertEquals(5, parsed.getB(), 1e-10);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.sample.JodaConvertWrapper\"><bean><base>Hello</base><extra>9</extra></bean><description>Weird</description></bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() {
        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.sample.JodaConvertBean\">Hello:9</bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_nonStandard_withCommentBeanRoot() {
        String xml = "<bean><!-- comment --><element>Test</element></bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_nonStandard_withCommentInProperty() {
        String xml = "<bean><element><!-- comment -->Test</element></bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_aliased() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.sample.SimpleName\">" +
        		"<firstName>A</firstName><givenName>B</givenName></bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        SimpleName bean = new SimpleName();
        bean.setForename("A");
        bean.setSurname("B");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteInterfaceKeyMap() {
        ImmKey key1 = ImmKey.builder().name("Alpha").build();
        ImmPerson person1 = ImmPerson.builder().forename("Bob").surname("Builder").build();
        ImmKey key2 = ImmKey.builder().name("Beta").build();
        ImmPerson person2 = ImmPerson.builder().forename("Dana").surname("Dash").build();
        ImmMappedKey mapped = ImmMappedKey.builder().data(ImmutableMap.of(key1, person1, key2, person2)).build();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(mapped);
        
        ImmMappedKey bean = (ImmMappedKey) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, mapped);
    }

    @Test
    public void test_read_badTypeInMap() {
        String xml = "<bean><element metatype=\"Map\"><entry><item>work</item><item type=\"com.foo.UnknownEnum\">BIGWIG</item></entry></element></bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).xmlReader().read(xml, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", ImmutableMap.of("work", "BIGWIG"));  // converted to a string
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_ignoreProperty() {
        String xml = "<bean><name>foo</name><wibble>ignored</wibble></bean>";
        ImmKey parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).xmlReader().read(xml, ImmKey.class);
        ImmKey bean = ImmKey.builder().name("foo").build();
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expected = IllegalArgumentException.class)
    public void test_read_noBeanElementAtRoot() {
        JodaBeanSer.COMPACT.xmlReader().read("<foo></foo>", Bean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_noTypeAttributeAtRoot() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Bean.class);
    }

    @Test
    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeAttributeNotBean() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean type=\"java.lang.Integer\"></bean>", Bean.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeInvalid() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean type=\"org.joda.beans.impl.flexi.FlexiBean\"></bean>", SimplePerson.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_read_rootTypeArgumentInvalid() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Integer.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.xmlWriter().write(bean);
    }

}
