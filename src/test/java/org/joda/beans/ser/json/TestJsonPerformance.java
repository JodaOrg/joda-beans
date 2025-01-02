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
package org.joda.beans.ser.json;

import java.io.IOException;
import java.time.Duration;

import org.joda.beans.sample.Address;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.base.Stopwatch;

/**
 * Test bean round-trip using JSON.
 */
@Disabled("Performance test - run manually when needed")
class TestJsonPerformance {

    private static final int REPEAT_OUTER = 20;
    private static final int REPEAT_INNER = 5000;

    @Test
    void testPerformance() throws IOException {
        var address = SerTestHelper.testAddress();
        invokeNew(address);
        invokeOld(address);
        invokeNew(address);
        invokeOld(address);
        invokeNew(address);
        invokeOld(address);
        System.out.println("---");
        invokeNew(address);
        invokeOld(address);
    }

    private void invokeNew(Address address) {
        String json;
        var total = Duration.ZERO;
        for (int i = 0; i < REPEAT_OUTER; i++) {
            Stopwatch watch = Stopwatch.createStarted();
            for (int j = 0; j < REPEAT_INNER; j++) {
                json = new JodaBeanJsonWriter(JodaBeanSer.PRETTY).write(address);
                if (json.length() < 1000) {
                    System.out.println();
                }
            }
            watch.stop();
            total = total.plus(watch.elapsed());
        }
        System.out.println("NEW-AVG-J: " + ((total.dividedBy(REPEAT_OUTER).toNanos() / 1000) / 1000d) + " ms");
    }

    private void invokeOld(Address address) {
        String json;
        var total = Duration.ZERO;
        for (int i = 0; i < REPEAT_OUTER; i++) {
            Stopwatch watch = Stopwatch.createStarted();
            for (int j = 0; j < REPEAT_INNER; j++) {
                json = new JodaBeanSimpleJsonWriter(JodaBeanSer.PRETTY).write(address);
                if (json.length() < 1000) {
                    System.out.println();
                }
            }
            watch.stop();
            total = total.plus(watch.elapsed());
        }
        System.out.println("OLD-AVG-J: " + ((total.dividedBy(REPEAT_OUTER).toNanos() / 1000) / 1000d) + " ms");
    }

}
