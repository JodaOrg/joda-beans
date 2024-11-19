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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.offset;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.joda.beans.Bean;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmDoubleFloat;
import org.joda.beans.sample.ImmEmpty;
import org.joda.beans.sample.ImmGenericCollections;
import org.joda.beans.sample.ImmKey;
import org.joda.beans.sample.ImmMappedKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.JodaConvertBean;
import org.joda.beans.sample.JodaConvertInterface;
import org.joda.beans.sample.JodaConvertWrapper;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.SimpleName;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.ser.json.TestSerializeJson;
import org.joda.beans.test.BeanAssert;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

/**
 * Test property roundtrip using XML.
 */
public class TestSerializeXml {

    @Test
    public void test_writeAddress() throws IOException {
        var bean = SerTestHelper.testAddress();
        var xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
//        System.out.println(xml);
        assertEqualsSerialization(xml, "/org/joda/beans/ser/Address.xml");

        var parsed = (Address) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeToAppendable() throws IOException {
        var bean = SerTestHelper.testAddress();
        var output = new CharArrayWriter();
        JodaBeanSer.PRETTY.xmlWriter().write(bean, output);
        var xml = output.toString();

        var parsed = (Address) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeImmAddress() throws IOException {
        var bean = SerTestHelper.testImmAddress(false);
        var xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
//        System.out.println(xml);
        assertEqualsSerialization(xml, "/org/joda/beans/ser/ImmAddress.xml");

        xml = xml.replace("185", "18<!-- comment -->5");

        var parsed = (ImmAddress) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeImmOptional() throws IOException {
        var bean = SerTestHelper.testImmOptional();
        var xml = JodaBeanSer.PRETTY.withIncludeDerived(true).xmlWriter().write(bean);
//        System.out.println(xml);
        assertEqualsSerialization(xml, "/org/joda/beans/ser/ImmOptional.xml");

        var parsed = (ImmOptional) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeCollections() throws IOException {
        var bean = SerTestHelper.testCollections();
        var xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
//        System.out.println(xml);
        assertEqualsSerialization(xml, "/org/joda/beans/ser/Collections.xml");

        var parsed = JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_writeJodaConvertInterface() {
        var bean = SerTestHelper.testGenericInterfaces();

        var xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
//        System.out.println(xml);

        @SuppressWarnings("unchecked")
        var parsed = (ImmGenericCollections<JodaConvertInterface>) JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    private void assertEqualsSerialization(String xml, String expectedResource) throws IOException {
        var url = TestSerializeJson.class.getResource(expectedResource);
        var expected = Resources.asCharSource(url, StandardCharsets.UTF_8).read();
        assertThat(xml.trim().replace(System.lineSeparator(), "\n"))
                .isEqualTo(expected.trim().replace(System.lineSeparator(), "\n"));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_readWriteBeanEmptyChild_pretty() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        var xml = JodaBeanSer.PRETTY.xmlWriter().write(bean);
        assertThat(xml)
                .isEqualTo("""
                    <?xml version="1.0" encoding="UTF-8"?>
                    <bean type="org.joda.beans.impl.flexi.FlexiBean">
                     <element>Test</element>
                     <child type="org.joda.beans.sample.ImmEmpty"/>
                    </bean>
                    """);
        var parsed = JodaBeanSer.PRETTY.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteBeanEmptyChild_compact() {
        var bean = new FlexiBean();
        bean.set("element", "Test");
        bean.set("child", ImmEmpty.builder().build());
        var xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertThat(xml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<bean type=\"org.joda.beans.impl.flexi.FlexiBean\">" +
                        "<element>Test</element><child type=\"org.joda.beans.sample.ImmEmpty\"/></bean>");
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteJodaConvertWrapper() {
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        var xml = JodaBeanSer.COMPACT.xmlWriter().write(wrapper);
        assertThat(xml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<bean type=\"org.joda.beans.sample.JodaConvertWrapper\">" +
                        "<bean>Hello:9</bean><description>Weird</description></bean>");
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_readWriteJodaConvertBean() {
        var bean = new JodaConvertBean("Hello:9");
        var xml = JodaBeanSer.COMPACT.xmlWriter().write(bean);
        assertThat(xml)
                .isEqualTo("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<bean type=\"org.joda.beans.sample.JodaConvertBean\">" +
                        "<base>Hello</base><extra>9</extra></bean>");
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_primitiveTypeChanged() throws IOException {
        var xml = "<bean><a>6</a><b>5</b></bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, ImmDoubleFloat.class);
        assertThat(parsed.getA()).isCloseTo(6, offset(1e-10));
        assertThat(parsed.getB()).isCloseTo(5, offset(1e-10));
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_nonStandard_JodaConvertWrapper_expanded() {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<bean type=\"org.joda.beans.sample.JodaConvertWrapper\">" +
                "<bean><base>Hello</base><extra>9</extra></bean><description>Weird</description></bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        var wrapper = new JodaConvertWrapper();
        var bean = new JodaConvertBean("Hello:9");
        wrapper.setBean(bean);
        wrapper.setDescription("Weird");
        BeanAssert.assertBeanEquals(wrapper, parsed);
    }

    @Test
    public void test_read_nonStandard_JodaConvertBean_flattened() {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<bean type=\"org.joda.beans.sample.JodaConvertBean\">Hello:9</bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        var bean = new JodaConvertBean("Hello:9");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_nonStandard_withCommentBeanRoot() {
        var xml = "<bean><!-- comment --><element>Test</element></bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        var bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_nonStandard_withCommentInProperty() {
        var xml = "<bean><element><!-- comment -->Test</element></bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        var bean = new FlexiBean();
        bean.set("element", "Test");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_aliased() {
        var xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<bean type=\"org.joda.beans.sample.SimpleName\">" +
                "<firstName>A</firstName><givenName>B</givenName></bean>";
        var parsed = JodaBeanSer.COMPACT.xmlReader().read(xml);
        var bean = new SimpleName();
        bean.setForename("A");
        bean.setSurname("B");
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_readWriteInterfaceKeyMap() {
        var key1 = ImmKey.builder().name("Alpha").build();
        var person1 = ImmPerson.builder().forename("Bob").surname("Builder").build();
        var key2 = ImmKey.builder().name("Beta").build();
        var person2 = ImmPerson.builder().forename("Dana").surname("Dash").build();
        var mapped = ImmMappedKey.builder().data(ImmutableMap.of(key1, person1, key2, person2)).build();
        var xml = JodaBeanSer.PRETTY.xmlWriter().write(mapped);

        var bean = (ImmMappedKey) JodaBeanSer.PRETTY.xmlReader().read(xml);
        BeanAssert.assertBeanEquals(bean, mapped);
    }

    @Test
    public void test_read_badTypeInMap() {
        var xml = "<bean><element metatype=\"Map\"><entry><item>work</item>" +
                "<item type=\"com.foo.UnknownEnum\">BIGWIG</item></entry></element></bean>";
        var parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).xmlReader().read(xml, FlexiBean.class);
        var bean = new FlexiBean();
        bean.set("element", ImmutableMap.of("work", "BIGWIG"));  // converted to a string
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    @Test
    public void test_read_ignoreProperty() {
        var xml = "<bean><name>foo</name><wibble>ignored</wibble></bean>";
        var parsed = JodaBeanSer.COMPACT.withDeserializers(SerDeserializers.LENIENT).xmlReader().read(xml, ImmKey.class);
        var bean = ImmKey.builder().name("foo").build();
        BeanAssert.assertBeanEquals(bean, parsed);
    }

    //-----------------------------------------------------------------------
    @Test
    public void test_read_noBeanElementAtRoot() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlReader().read("<foo></foo>", Bean.class));
    }

    @Test
    public void test_read_noTypeAttributeAtRoot() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Bean.class));
    }

    @Test
    public void test_read_noTypeAttributeAtRootButTypeSpecified() {
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", FlexiBean.class);
        BeanAssert.assertBeanEquals(new FlexiBean(), parsed);
    }

    @Test
    public void test_read_rootTypeAttributeNotBean() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlReader()
                        .read("<bean type=\"java.lang.Integer\"></bean>", Bean.class));
    }

    @Test
    public void test_read_rootTypeInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlReader()
                        .read("<bean type=\"org.joda.beans.impl.flexi.FlexiBean\"></bean>", SimplePerson.class));
    }

    @Test
    public void test_read_rootTypeArgumentInvalid() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlReader().read("<bean></bean>", Integer.class));
    }

    @Test
    public void test_write_nullKeyInMap() {
        var address = new Address();
        var bean = new Person();
        bean.getOtherAddressMap().put(null, address);
        assertThatIllegalArgumentException()
                .isThrownBy(() -> JodaBeanSer.COMPACT.xmlWriter().write(bean));
    }

}
