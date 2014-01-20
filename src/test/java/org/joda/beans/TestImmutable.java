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
package org.joda.beans;

import static org.testng.Assert.assertEquals;

import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmPerson;
import org.testng.annotations.Test;

/**
 * Test property using Person.
 */
@Test
public class TestImmutable {

    public void test_bean() {
        ImmAddress address = ImmAddress.builder()
                .number(12)
                .street("Park Lane")
                .city("Smallville")
                .owner(ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Lane");
    }

    public void test_builder() {
        ImmAddress address = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Lane");
    }

    public void test_with() {
        ImmAddress address = ImmAddress.builder()
                .set("number", 12)
                .set("street", "Park Lane")
                .set("city", "Smallville")
                .set("owner", ImmPerson.builder().forename("John").surname("Doggett").build())
                .build();
        
        address = address.toBuilder().street("Park Road").build();
        
        assertEquals(address.getCity(), "Smallville");
        assertEquals(address.getStreet(), "Park Road");
    }

}
