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

import static org.junit.Assert.assertEquals;

import org.joda.beans.sample.SimpleName;
import org.junit.Test;

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
        
        assertEquals(person1, person2);
        assertEquals(person1.hashCode(), person2.hashCode());
        assertEquals(person1.toString(), "SimpleName{forename=Etienne, middleNames=[Yakusa, Mohito], surname=Colebourne}");
    }

}
