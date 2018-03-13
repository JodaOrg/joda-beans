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

import org.joda.beans.sample.Pair;
import org.joda.beans.sample.TweakedPair;
import org.junit.Test;

/**
 * Test Pair.
 */
public class TestPair {

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void test_equalsHashCode() {
        // even though TwekedPair adds no new properties, we treat it as different
        // this can be avoided (see Git history) but at a performance cost
        Pair a1 = new Pair();
        Pair a2 = new Pair();
        TweakedPair b = new TweakedPair();
        
        a1.setFirst("A");
        a2.setFirst("A");
        b.setFirst("A");
        
        assertEquals(a1.equals(a1), true);
        assertEquals(a1.equals(a2), true);
        assertEquals(a2.equals(a1), true);
        assertEquals(a2.equals(a2), true);
        assertEquals(a1.hashCode(), a2.hashCode());
        
        assertEquals(a1.equals(b), false);
        assertEquals(b.equals(a1), false);
        
        assertEquals(b.equals("Weird type"), false);
        assertEquals(b.equals(null), false);
    }

    @Test
    public void test_toString() {
        Pair test = new Pair();
        test.setFirst("A");
        test.setSecond("B");
        assertEquals(test.toString(), "Pair{first=A, second=B}");
    }

}
