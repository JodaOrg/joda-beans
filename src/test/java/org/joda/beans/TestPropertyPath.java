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
package org.joda.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.Person;
import org.junit.jupiter.api.Test;

/**
 * Test utils.
 */
public class TestPropertyPath {

    //-------------------------------------------------------------------------
    @Test
    public void test_get() {
        Person person1 = new Person();
        person1.setForename("Angus");
        Address address1 = new Address();
        address1.setNumber(1);
        address1.setOwner(person1);
        Person person2 = new Person();
        person2.setForename("Bob");
        Address address2 = new Address();
        address2.setNumber(2);
        address2.setOwner(person2);
        Map<String, Address> addrMap = new HashMap<>();
        addrMap.put("Main", address1);
        addrMap.put("Other", address2);
        Date date = new Date();
        ImmPerson base = ImmPerson.builder()
                .forename("Zach")
                .surname("Tidy")
                .dateOfBirth(date)
                .mainAddress(ImmAddress.builder()
                        .street("My Street")
                        .city("Nodnol")
                        .owner(ImmPerson.builder()
                                .forename("Zoe")
                                .surname("Tidy")
                                .build())
                        .build())
                .addressList(address1, address2)
                .otherAddressMap(addrMap)
                .build();

        assertThatIllegalArgumentException().isThrownBy(() -> PropertyPath.of("rubbish[]", String.class));

        assertThat(PropertyPath.of("rubbish", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("rubbish[0]", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("rubbish[-1]", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("rubbish[Main]", String.class).get(base)).isEmpty();

        assertThat(PropertyPath.of("forename", String.class).get(base)).hasValue("Zach");
        assertThat(PropertyPath.of("forename.rubbish", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("surname", String.class).get(base)).hasValue("Tidy");
        assertThat(PropertyPath.of("dateOfBirth", Date.class).get(base)).hasValue(date);

        assertThat(PropertyPath.of("mainAddress.city", String.class).get(base)).hasValue("Nodnol");
        assertThat(PropertyPath.of("mainAddress[0].city", String.class).get(base)).hasValue("Nodnol");
        assertThat(PropertyPath.of("mainAddress[1].city", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("mainAddress[-1].city", String.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("mainAddress[Other].city", String.class).get(base)).isEmpty();

        assertThat(PropertyPath.of("addressList", Address.class).get(base)).hasValue(address1);
        assertThat(PropertyPath.of("addressList[0]", Address.class).get(base)).hasValue(address1);
        assertThat(PropertyPath.of("addressList[1]", Address.class).get(base)).hasValue(address2);
        assertThat(PropertyPath.of("addressList[2]", Address.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("addressList[-1]", Address.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("addressList[Main]", Address.class).get(base)).isEmpty();

        assertThat(PropertyPath.of("otherAddressMap", Address.class).get(base)).hasValue(address1);
        assertThat(PropertyPath.of("otherAddressMap[Main]", Address.class).get(base)).hasValue(address1);
        assertThat(PropertyPath.of("otherAddressMap[Other]", Address.class).get(base)).hasValue(address2);
        assertThat(PropertyPath.of("otherAddressMap[Rubbish]", Address.class).get(base)).isEmpty();
        assertThat(PropertyPath.of("otherAddressMap.number", Integer.class).get(base)).hasValue(1);
        assertThat(PropertyPath.of("otherAddressMap[Main].number", Integer.class).get(base)).hasValue(1);
        assertThat(PropertyPath.of("otherAddressMap[Other].number", Integer.class).get(base)).hasValue(2);
        assertThat(PropertyPath.of("otherAddressMap.owner.forename", String.class).get(base)).hasValue("Angus");
        assertThat(PropertyPath.of("otherAddressMap[Other].owner.forename", String.class).get(base)).hasValue("Bob");

        assertThat(PropertyPath.of("otherAddressMap[Other].owner.forename", String.class))
                .hasToString("otherAddressMap[Other].owner.forename: java.lang.String");
    }

}
