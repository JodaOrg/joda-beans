/*
 *  Copyright 2001-2015 Stephen Colebourne
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
package org.joda.beans;

import static org.testng.Assert.assertEquals;

import org.joda.beans.gen.Light;
import org.joda.beans.gen.ImmPerson;
import org.testng.annotations.Test;

/**
 * Test style=light.
 */
@Test
public class TestLight {

    public void test_builder() {
        ImmPerson person = ImmPerson.builder().forename("John").surname("Doggett").build();
        Light bean = (Light) Light.meta().builder()
                .setString("number", "12")
                .setString("street", "Park Lane")
                .setString("city", "Smallville")
                .set("owner", person)
                .build();
        
        assertEquals(bean.getNumber(), 12);
        assertEquals(bean.getCity(), "Smallville");
        assertEquals(bean.getStreetName(), "Park Lane");
        assertEquals(bean.getOwner(), person);
    }

}
