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
package org.joda.beans.ser.bin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;

import org.joda.beans.sample.Address;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.junit.jupiter.api.Test;

import com.google.common.base.Stopwatch;

/**
 * Test property roundtrip using Binary.
 */
public class TestBinPerformance {

    private static final int REPEAT_OUTER = 20;
    private static final int REPEAT_INNER = 5000;

    @Test
    public void test_writeAddress2() throws IOException {
        Address address = SerTestHelper.testAddress();
        invokeNew3(address);
        invokeOld(address);
        invokeNew3(address);
        invokeOld(address);
        System.out.println("---");
        invokeNew3(address);
        invokeOld(address);
    }

    private void invokeNew3(Address address) throws IOException {
//        invokeOld(address);
        byte[] bytes = null;
        var total = Duration.ZERO;
        for (int i = 0; i < REPEAT_OUTER; i++) {
            Stopwatch watch = Stopwatch.createStarted();
            for (int j = 0; j < REPEAT_INNER; j++) {
                var baos = new ByteArrayOutputStream(1024);
                new JodaBeanPackedBinWriter(JodaBeanSer.PRETTY, baos).write(address);
                bytes = baos.toByteArray();
                if (bytes.length < 100) {
                    System.out.println();
                }
            }
            watch.stop();
            total = total.plus(watch.elapsed());
        }
        System.out.println(new BeanPackVisualizer(bytes).visualizeData());
        System.out.println("NEW-AVG-B: " + ((total.dividedBy(REPEAT_OUTER).toNanos() / 1000) / 1000d) + " ms");
    }

    private void invokeOld(Address address) {
        byte[] bytes = null;
        var total = Duration.ZERO;
        for (int i = 0; i < REPEAT_OUTER; i++) {
            Stopwatch watch = Stopwatch.createStarted();
            for (int j = 0; j < REPEAT_INNER; j++) {
                bytes = new JodaBeanBinWriter(JodaBeanSer.PRETTY).write(address);
                if (bytes.length < 100) {
                    System.out.println();
                }
            }
            watch.stop();
            total = total.plus(watch.elapsed());
        }
//        System.out.println(new MsgPackVisualizer(bytes).visualizeData());
        System.out.println("OLD-AVG-B: " + ((total.dividedBy(REPEAT_OUTER).toNanos() / 1000) / 1000d) + " ms");
    }

}
