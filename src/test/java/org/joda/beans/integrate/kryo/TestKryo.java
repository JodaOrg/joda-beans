/*
 *  Copyright 2001-2016 Stephen Colebourne
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
package org.joda.beans.integrate.kryo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.joda.beans.gen.Address;
import org.joda.beans.gen.ImmAddress;
import org.joda.beans.gen.ImmOptional;
import org.joda.beans.ser.SerTestHelper;
import org.joda.beans.test.BeanAssert;
import org.testng.annotations.Test;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * Test Kryo roundtrip.
 */
@Test
public class TestKryo {

    public void test_writeAddress() {
        Address address = SerTestHelper.testAddress();
        Address bean = roundtrip(address, Address.class);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmAddress() throws IOException {
        ImmAddress address = SerTestHelper.testImmAddress();
        ImmAddress bean = roundtrip(address, ImmAddress.class);
        BeanAssert.assertBeanEquals(bean, address);
    }

    public void test_writeImmOptional() {
        ImmOptional optional = SerTestHelper.testImmOptional();
        ImmOptional bean = roundtrip(optional, ImmOptional.class);
        BeanAssert.assertBeanEquals(bean, optional);
    }

    //-------------------------------------------------------------------------
    private <T> T roundtrip(T obj, Class<T> type) {
        Kryo kryo = new Kryo();
        kryo.setDefaultSerializer(new KryoJodaBeanSerializer());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output output = null;
        try {
            output = new Output(baos);
            kryo.writeObject(output, obj);
                    
        } finally {
            if (output != null) {
                output.close();
            }
        }
        byte[] bytes = baos.toByteArray();
        Input input = null;
        try {
            input = new Input(bytes);
            return kryo.readObject(input, type);
                    
        } finally {
            if (output != null) {
                output.close();
            }
        }
    }

}
