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
package org.joda.beans.ser.xml;

import static org.testng.Assert.assertEquals;

import org.joda.beans.Bean;
import org.joda.beans.gen.Address;
import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmEmpty;
import org.joda.beans.gen.ImmOptional;
import org.joda.beans.gen.JodaConvertBean;
import org.joda.beans.gen.JodaConvertWrapper;
import org.joda.beans.gen.Person;
import org.joda.beans.gen.SimpleName;
import org.joda.beans.gen.SimplePerson;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.Test;

/**
 * Test property roundtrip using XML.
 */
@Test
public class TestSerializeXml {

    public void test_writeAddress() {
        Address address = SerTestHelper.testAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
//        System.out.println(xml);
        
        Address bean = (Address) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmAddress() {
        ImmAddress address = SerTestHelper.testImmAddress();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(address);
        
        xml = xml.replace("185", "18<!-- comment -->5");
//        System.out.println(xml);
        
        ImmAddress bean = (ImmAddress) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmOptional() {
        ImmOptional optional = SerTestHelper.testImmOptional();
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(optional);
//        System.out.println(xml);
        
        ImmOptional bean = (ImmOptional) JodaBeanSer.PRETTY.xmlReader().read(xml);
//        System.out.println(bean);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    //-----------------------------------------------------------------------
    public void test_readWriteBeanEmptyChild_pretty() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertEquals(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<bean type=\"org.joda.beans.impl.flexi.FlexiBean\">\n <element>Test</element>\n <child type=\"org.joda.beans.gen.ImmEmpty\"/>\n</bean>\n");
        FlexiBean parsed = JodaBeanSer.PRETTY.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteBeanEmptyChild_compact() {
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertEquals(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.impl.flexi.FlexiBean\"><element>Test</element><child type=\"org.joda.beans.gen.ImmEmpty\"/></bean>");
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_readWriteJodaConvertWrapper() {
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(wrapper);
        assertEquals(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.gen.JodaConvertWrapper\"><bean>Hello:9</bean><description>Weird</description></bean>");
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_readWriteJodaConvertBean() {
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        String xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertEquals(xml, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.gen.JodaConvertBean\"><base>Hello</base><extra>9</extra></bean>");
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.gen.JodaConvertWrapper\"><bean><base>Hello</base><extra>9</extra></bean><description>Weird</description></bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        JodaConvertWrapper wrapper = new JodaConvertWrapper();
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    public void test_read_nonStandard_JodaConvertBean_flattened() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.gen.JodaConvertBean\">Hello:9</bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        JodaConvertBean bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_read_nonStandard_withCommentBeanRoot() {
        String xml = "<bean><!-- comment --><element>Test</element></bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    public void test_read_nonStandard_withCommentInProperty() {
        String xml = "<bean><element><!-- comment -->Test</element></bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    public void test_read_aliased() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><bean type=\"org.joda.beans.gen.SimpleName\">" +
        		"<firstName>A</firstName><givenName>B</givenName></bean>";
        Bean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        SimpleName bean = new SimpleName();
        bean.setForename("A");
        bean.setSurname("B");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_noBeanElementAtRoot() {
        JodaBeanSer.COMPACT.xmlReader().read("<foo></foo>", Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_noTypeAttributeAtRoot() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Bean.class);
    }

    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeAttributeNotBean() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean type=\"java.lang.Integer\"></bean>", Bean.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeInvalid() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean type=\"org.joda.beans.impl.flexi.FlexiBean\"></bean>", SimplePerson.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_read_rootTypeArgumentInvalid() {
        JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Integer.class);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void test_write_nullKeyInMap() {
        Address address = new Address();
        Person bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        JodaBeanSer.COMPACT.xmlWriter().write(bean);
    }

}
