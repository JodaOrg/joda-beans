/*
 *  Copyright 2017-present Stephen Colebourne
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

import static org.junit.Assert.assertEquals;

import org.joda.beans.sample.ImmSubPersonNonFinal;
import org.junit.Test;

/**
 * Test beans extending other beans.
 */
public class TestSubBean {

    @Test
    public void test_subbean_toBuilder() {

        // This sequence needs to be split like this as forename and surname returns ImmPersonNonFinal.Builder
        // instead of ImmSubPersonNonFinal.Builder
        ImmSubPersonNonFinal.Builder builder = ImmSubPersonNonFinal.builder();
        builder
                .middleName("K.")
                .forename("John")
                .surname("Doe");
        ImmSubPersonNonFinal person = builder.build();

        ImmSubPersonNonFinal rebuilt = person.toBuilder().build();
        assertEquals(rebuilt, person);
    }
}
