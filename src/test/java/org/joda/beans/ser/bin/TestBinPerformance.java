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

import java.io.IOException;
import java.time.Duration;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.ser.JodaBeanSer;
import org.joda.beans.ser.SerTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.google.common.base.Stopwatch;

/**
 * Test bean round-trip using Binary.
 */
@Disabled("Performance test - run manually when needed")
class TestBinPerformance {

    private static final int REPEAT_OUTER = 4;
    private static final int REPEAT_INNER = 20;
    private static final JodaBeanSer SER = JodaBeanSer.PRETTY.withBeanValueClasses(Set.of(ImmAddress.class));

    @Test
    void testPerformance() throws IOException {
//        var bean = SerTestHelper.testImmAddress(true);  // REPEAT_INNER = 2000
//        var bean = SerTestHelper.testBigIntegerList();  //  REPEAT_INNER = 20
        var bean = SerTestHelper.testBigAddressArray();  //  REPEAT_INNER = 20
        invokeNew(bean);
        invokeRef(bean);
        invokeStd(bean);
        invokeNew(bean);
        invokeRef(bean);
        invokeStd(bean);
        System.out.println("---");
        invokeNew(bean);
        invokeRef(bean);
        invokeStd(bean);
    }

    private void invokeNew(Bean bean) throws IOException {
        byte[] bytes = write(JodaBeanBinFormat.PACKED, bean, "PAK");
//        System.out.println(new BeanPackVisualizer(bytes).visualizeData());
        read(bytes, "PAK");
    }

    private void invokeRef(Bean bean) {
        byte[] bytes = write(JodaBeanBinFormat.REFERENCING, bean, "REF");
        read(bytes, "REF");
    }

    private void invokeStd(Bean bean) {
        byte[] bytes = write(JodaBeanBinFormat.STANDARD, bean, "STD");
        read(bytes, "STD");
    }

    //-----------------------------------------------------------------------
    private byte[] write(JodaBeanBinFormat format, Bean bean, String type) {
        byte[] bytes = null;
        var total = Duration.ZERO;
        for (int i = 0; i < REPEAT_OUTER; i++) {
            Stopwatch watch = Stopwatch.createStarted();
            for (int j = 0; j < REPEAT_INNER; j++) {
                bytes = new JodaBeanBinWriter(SER, format).write(bean);
                if (bytes.length < 100) {
                    System.out.println();
                }
            }
            watch.stop();
            total = total.plus(watch.elapsed());
        }
        System.out.println(type + "-WRITE: " + calcMillis(total) + " ms, " + bytes.length + " bytes");
        return bytes;
    }

    private void read(byte[] bytes, String type) {
        Stopwatch watch = Stopwatch.createStarted();
        for (int i = 0; i < REPEAT_OUTER * REPEAT_INNER; i++) {
            var bean = new JodaBeanBinReader(SER).read(bytes);
            if (bean instanceof Address) {
                System.out.println();
            }
        }
        watch.stop();
        System.out.println(type + "-READ: " + calcMillis(watch.elapsed()) + " ms");
    }

    private double calcMillis(Duration total) {
        return (total.dividedBy(REPEAT_OUTER).toNanos() / 1000) / 1000d;
    }

}
