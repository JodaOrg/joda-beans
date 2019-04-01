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

import java.io.DataInputStream;
import java.io.InputStream;

/**
 * Allows MsgPack data written in the compact binary format to be visualized.
 *
 * @author Will Nicholson
 */
final class CompactMsgPackVisualizer extends MsgPackVisualizer {

    /**
     * Creates an instance.
     *
     * @param bytes  the bytes to read, not null
     */
    CompactMsgPackVisualizer(byte[] bytes) {
        super(bytes);
    }

    /**
     * Creates an instance.
     *
     * @param stream  the stream to read from, not null
     */
    CompactMsgPackVisualizer(InputStream stream) {
        super(stream);
    }

    /**
     * Creates an instance.
     *
     * @param stream  the stream to read from, not null
     */
    CompactMsgPackVisualizer(DataInputStream stream) {
        super(stream);
    }

    //-----------------------------------------------------------------------

    @Override
    protected void handleExtension(int type, byte[] bytes) {
        if (type == JODA_TYPE_BEAN || type == JODA_TYPE_DATA || type == JODA_TYPE_META ||
            type == JODA_TYPE_REF_KEY || type == JODA_TYPE_REF) {

            int value = 0;
            for (byte b : bytes) {
                value = (value << 8) | b;
            }
            buf.append("ext type=").append(type).append(" '").append(value).append("'")
                .append(System.lineSeparator());
        } else {
            buf.append("ext type=").append(type).append(" '");
            for (byte b : bytes) {
                buf.append(toHex(b));
            }
            buf.append("'").append(System.lineSeparator());
        }
    }

}
