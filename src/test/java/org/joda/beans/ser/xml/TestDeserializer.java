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

import static org.testng.Assert.fail;

import org.joda.beans.gen.SimplePerson;
import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.test.BeanAssert;
import org.joda.convert.RenameHandler;
import org.testng.annotations.Test;

/**
 * Test deserialization using XML.
 */
@Test
public class TestDeserializer {

    @Test
    public void test_read_renamedType() {
        String xml = "<bean type=\"org.joda.beans.FlexibleBean\"><surname>Smith</surname></bean>";
        try {
            JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
            fail();
        } catch (RuntimeException ex) {
            // expected
        }
        RenameHandler.INSTANCE.renamedType("org.joda.beans.FlexibleBean", FlexiBean.class);
        FlexiBean parsed = JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
        FlexiBean expected = new FlexiBean();
        expected.set("surname", "Smith");
        BeanAssert.assertBeanEquals(expected, parsed);
    }

    @Test
    public void test_read_renamedTypeLower() {
        String xml = "<bean>" +
                "<extra type=\"org.joda.beans.SPerson\"><surname>Smith</surname></extra>" +
                "<person type=\"SPerson\"><surname>Jones</surname></person>" +
                "</bean>";
        try {
            JodaBeanSer.COMPACT.xmlReader().read(xml, FlexiBean.class);
            fail();
        } catch (RuntimeException ex) {
            // expected
        }
        RenameHandler.INSTANCE.renamedType("org.joda.beans.SPerson", SimplePerson.class);
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

}
