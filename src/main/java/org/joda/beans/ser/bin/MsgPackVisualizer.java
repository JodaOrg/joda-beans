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
import java.io.IOException;
import java.io.InputStream;

/**
 * Allows MsgPack data to be visualized.
 */
final class MsgPackVisualizer extends MsgPackInput {

    /**
     * The current indent.
     */
    private String indent = "";
    /**
     * The buffer.
     */
    private final StringBuilder buf = new StringBuilder(1024);

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    MsgPackVisualizer(byte[] bytes) {
        super(bytes);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    MsgPackVisualizer(InputStream stream) {
        super(stream);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    MsgPackVisualizer(DataInputStream stream) {
        super(stream);
    }

    //-----------------------------------------------------------------------
    /**
     * Visualizes the data in the stream.
     */
    String visualizeData() {
        readAll();
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    @Override
    void readArrayItem() throws IOException {
        indent = indent + "- ";
        super.readArrayItem();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    void readMapKey() throws IOException {
        indent = indent + "= ";
        super.readMapKey();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    void readMapValue() throws IOException {
        indent = indent + "  ";
        super.readMapValue();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    void handleObjectStart() {
        buf.append(indent);
        indent = indent.replace("-", " ").replace("=", " ");
    }

    @Override
    void handleBoolean(boolean bool) {
        buf.append(bool).append(System.lineSeparator());
    }

    @Override
    void handleNil() {
        buf.append("nil").append(System.lineSeparator());
    }

    @Override
    void handleInt(int value) {
        buf.append("int ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleUnsignedLong(long value) {
        buf.append("int ").append(value).append(" unsigned").append(System.lineSeparator());
    }

    @Override
    void handleSignedLong(long value) {
        buf.append("int ").append(value).append(" signed").append(System.lineSeparator());
    }

    @Override
    void handleFloat(float value) {
        buf.append("flt ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleDouble(double value) {
        buf.append("dbl ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleUnknown(byte b) {
        buf.append("Unknown - ").append(String.format("%02X ", b)).append(System.lineSeparator());
    }

    @Override
    void handleString(String str) {
        buf.append("str '").append(str).append('\'').append(System.lineSeparator());
    }

    @Override
    void handleArrayHeader(int size) {
        buf.append("arr (").append(size).append(")").append(System.lineSeparator());
    }

    @Override
    void handleMapHeader(int size) {
        buf.append("map (").append(size).append(")").append(System.lineSeparator());
    }

    @Override
    void handleBinary(byte[] bytes) {
        buf.append("bin '");
        for (byte b : bytes) {
            buf.append(toHex(b));
        }
        buf.append("'").append(System.lineSeparator());
    }

    @Override
    void handleExtension(int type, boolean numeric, byte[] bytes) {
        String str;
        if (numeric) {
            var value = 0;
            for (byte b : bytes) {
                value = (value << 8) | (0xFF & b);
            }
            if (bytes.length == 1) {
                value = Byte.toUnsignedInt((byte) value);
            } else if (bytes.length == 2) {
                value = Short.toUnsignedInt((short) value);
            }
            str = Integer.toString(value);
        } else {
            str = new String(bytes, UTF_8);            
        }
        buf.append("ext type=")
            .append(type)
            .append(" '")
            .append(str)
            .append("'");
        switch (type) {
            case JODA_TYPE_BEAN:
                buf.append(" (bean)");
                break;
            case JODA_TYPE_DATA:
                buf.append(" (data)");
                break;
            case JODA_TYPE_META:
                buf.append(" (meta)");
                break;
            case JODA_TYPE_REF_KEY:
                buf.append(" (refkey)");
                break;
            case JODA_TYPE_REF:
                buf.append(" (ref)");
                break;
            default:
                break;
        }
        buf.append(System.lineSeparator());
    }

}
