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
import static org.testng.Assert.assertNotSame;

import java.util.Arrays;
import java.util.Date;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.ClonePerson;
import org.joda.beans.gen.Company;
import org.testng.annotations.Test;

/**
 * Test property using ClonePerson.
 */
@Test
public class TestClone {

    public void test_bean() {
        ClonePerson base = new ClonePerson();
        base.setSurname("Cable");
        base.setMiddleNames(new String[] {"A", "B", "C"});
        base.setFirstNames(Arrays.asList("Vince", "Matey"));
        base.setDateOfBirth(new Date());
        Address address = new Address();
        address.setCity("London");
        base.setAddresses(Arrays.asList(address));
        Company company = new Company();
        company.setCompanyName("Government");
        base.setCompanies(new Company[] {company});
        base.setAmounts(new int[] {1, 2});
        
        ClonePerson cloned = base.clone();
        assertEquals(cloned, base);
        assertNotSame(cloned.getMiddleNames(), base.getMiddleNames());
        assertNotSame(cloned.getFirstNames(), base.getFirstNames());
        assertNotSame(cloned.getDateOfBirth(), base.getDateOfBirth());
        assertNotSame(cloned.getAddresses(), base.getAddresses());
        assertNotSame(cloned.getAddresses().get(0), base.getAddresses().get(0));
        assertNotSame(cloned.getCompanies(), base.getCompanies());
        assertNotSame(cloned.getCompanies()[0], base.getCompanies()[0]);
        assertNotSame(cloned.getAmounts(), base.getAmounts());
    }

}
