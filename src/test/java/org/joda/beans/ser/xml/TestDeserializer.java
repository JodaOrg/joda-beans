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

import static org.junit.Assert.fail;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.sample.SimplePerson;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerDeserializers;
import org.joda.beans.test.BeanAssert;
import org.joda.convert.RenameHandler;
import org.junit.Test;

/**
 * Test deserialization using XML.
 */
public class TestDeserializer {

    @Test
    public void test_read_renamedType() {
        String xml = "<bean type=\"org.jodabeans.FlexibleBean\"><surname>Smith</surname></bean>";
        try {
            JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
            fail();
        } catch (RuntimeException ex) {
            // expected
        }
        RenameHandler.INSTANCE.renamedType("org.jodabeans.FlexibleBean", FlexiBean.class);
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        expected.set("surname", "Smith");
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test
    public void test_read_renamedTypeLower() {
        String xml = "<bean>" +
                "<extra type=\"org.jodabeans.SPerson\"><surname>Smith</surname></extra>" +
                "<person type=\"SPerson\"><surname>Jones</surname></person>" +
                "</bean>";
        try {
            JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
            fail();
        } catch (RuntimeException ex) {
            // expected
        }
        RenameHandler.INSTANCE.renamedType("org.jodabeans.SPerson", SimplePerson.class);
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        SimplePerson person1 = new SimplePerson();
        person1.setSurname("Smith");
        SimplePerson person2 = new SimplePerson();
        person2.setSurname("Jones");
        expected.set("extra", person1);
        expected.set("person", person2);
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test
    public void test_read_withSemanticChangeDeserializer() {
        SerDeserializers desers = new SerDeserializers();
        desers.register(SimplePerson.class, MockSemanticChangeDeserializer.INSTANCE);
        String xml = "<bean>" +
                "<person1 type=\"org.joda.beans.sample.SimplePerson\"><forename>John</forename><surname>Smith</surname></person1>" +
                "<person2 type=\"org.joda.beans.sample.SimplePerson\"><forename>Stephen</forename><surname>Colebourne</surname></person2>" +
                "</bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.withDeserializers(desers).xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        SimplePerson person1 = new SimplePerson();
        person1.setForename("John");
        person1.setSurname("Smith");
        SimplePerson person2 = new SimplePerson();
        person2.setForename("Steve");  // changed
        person2.setSurname("Colebourne");
        expected.set("person1", person1);
        expected.set("person2", person2);
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test
    public void test_read_withRenameDeserializer() {
        SerDeserializers desers = new SerDeserializers();
        desers.register(SimplePerson.class, MockRenameDeserializer.INSTANCE);
        String xml = "<bean>" +
                "<person1 type=\"org.joda.beans.sample.SimplePerson\"><firstName>John</firstName><surname>Smith</surname></person1>" +
                "<person2 type=\"org.joda.beans.sample.SimplePerson\"><firstName>Stephen</firstName><surname>Colebourne</surname></person2>" +
                "</bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.withDeserializers(desers).xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        SimplePerson person1 = new SimplePerson();
        person1.setForename("John");
        person1.setSurname("Smith");
        SimplePerson person2 = new SimplePerson();
        person2.setForename("Stephen");
        person2.setSurname("Colebourne");
        expected.set("person1", person1);
        expected.set("person2", person2);
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test
    public void test_read_withTypeChangeDeserializer() {
        SerDeserializers desers = new SerDeserializers();
        desers.register(SimplePerson.class, MockTypeChangeDeserializer.INSTANCE);
        String xml = "<bean>" +
                "<person1 type=\"org.joda.beans.sample.SimplePerson\"><numberOfCars>None</numberOfCars><surname>Smith</surname></person1>" +
                "<person2 type=\"org.joda.beans.sample.SimplePerson\"><numberOfCars>Two</numberOfCars><surname>Colebourne</surname></person2>" +
                "</bean>";
        FlexiBean parsed = JodaBeanSer.COMPACT.withDeserializers(desers).xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        SimplePerson person1 = new SimplePerson();
        person1.setNumberOfCars(0);
        person1.setSurname("Smith");
        SimplePerson person2 = new SimplePerson();
        person2.setNumberOfCars(2);
        person2.setSurname("Colebourne");
        expected.set("person1", person1);
        expected.set("person2", person2);
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test(expected = RuntimeException.class)
    public void test_read_withBadEntity() {
        SerDeserializers desers = new SerDeserializers();
        desers.register(SimplePerson.class, MockTypeChangeDeserializer.INSTANCE);
        String xml = "<?xml version=\"1.0\" encoding =\"UTF-8\"?>" +
             "<!DOCTYPE foobar[<!ENTITY x100 \"foobar\">";
        for (int i = 99; i > 0; i--) {
            xml += "<!ENTITY  x" + i + " \"&x" + (i + 1) + ";&x" + (i + 1) + ";\">";
        }
        xml += "]><bean>" +
                "<person1 type=\"org.joda.beans.sample.SimplePerson\"><numberOfCars>None</numberOfCars><surname>Smith &x1;</surname></person1>" +
                "<person2 type=\"org.joda.beans.sample.SimplePerson\"><numberOfCars>Two</numberOfCars><surname>Colebourne</surname></person2>" +
                "</bean>";
        JodaBeanSer.COMPACT.withDeserializers(desers).xmlReader().read(xml, FlexiBean.class);
    }

}
