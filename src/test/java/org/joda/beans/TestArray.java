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

import org.joda.beans.sample.SimpleName;
import org.junit.jupiter.api.Test;

/**
 * Test arrays using SimpleName.
 */
public class TestArray {

    @Test
    public void test_bean() {
        SimpleName person1 = new SimpleName();
        person1.setForename("Etienne");
        person1.setMiddleNames(new String[] {"Yakusa", "Mohito"});
        person1.setSurname("Colebourne");
        SimpleName person2 = new SimpleName();
        person2.setForename("Etienne");
        person2.setMiddleNames(new String[] {"Yakusa", "Mohito"});
        person2.setSurname("Colebourne");
        
        assertThat(person1).isEqualTo(person2);
        assertThat(person1.hashCode()).isEqualTo(person2.hashCode());
        assertThat(person1).hasToString(
                "SimpleName{forename=Etienne, middleNames=[Yakusa, Mohito], surname=Colebourne}");
    }

}
