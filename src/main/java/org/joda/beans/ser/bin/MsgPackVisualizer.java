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
class MsgPackVisualizer extends MsgPackInput {

    /**
     * The current indent.
     */
    protected String indent = "";
    /**
     * The buffer.
     */
    protected StringBuilder buf = new StringBuilder(1024);

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
    protected void readArrayItem() throws IOException {
        indent = indent + "- ";
        super.readArrayItem();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    protected void readMapKey() throws IOException {
        indent = indent + "= ";
        super.readMapKey();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    protected void readMapValue() throws IOException {
        indent = indent + "  ";
        super.readMapValue();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    protected void handleObjectStart() {
        buf.append(indent);
        indent = indent.replace("-", " ").replace("=", " ");
    }

    @Override
    protected void handleBoolean(boolean bool) {
        buf.append(bool).append(System.lineSeparator());
    }

    @Override
    protected void handleNil() {
        buf.append("nil").append(System.lineSeparator());
    }

    @Override
    protected void handleInt(int value) {
        buf.append("int " + value).append(System.lineSeparator());
    }

    @Override
    protected void handleUnsignedLong(long value) {
        buf.append("int " + value + " unsigned").append(System.lineSeparator());
    }

    @Override
    protected void handleSignedLong(long value) {
        buf.append("int " + value + " signed").append(System.lineSeparator());
    }

    @Override
    protected void handleFloat(float value) {
        buf.append("flt " + value).append(System.lineSeparator());
    }

    @Override
    protected void handleDouble(double value) {
        buf.append("dbl " + value).append(System.lineSeparator());
    }

    @Override
    protected void handleUnknown(byte b) {
        buf.append("Unknown - " + String.format("%02X ", b)).append(System.lineSeparator());;
    }

    @Override
    protected void handleString(String str) {
        buf.append("str '" + str + '\'').append(System.lineSeparator());
    }

    @Override
    protected void handleArrayHeader(int size) {
        buf.append("arr (" + size + ")").append(System.lineSeparator());
    }

    @Override
    protected void handleMapHeader(int size) {
        buf.append("map (" + size + ")").append(System.lineSeparator());
    }

    @Override
    protected void handleBinary(byte[] bytes) {
        buf.append("bin '");
        for (byte b : bytes) {
            buf.append(toHex(b));
        }
        buf.append("'").append(System.lineSeparator());
    }

    @Override
    protected void handleExtension(int type, byte[] bytes) throws IOException {
        if (type == JODA_TYPE_BEAN || type == JODA_TYPE_DATA || type == JODA_TYPE_META) {
            String str = new String(bytes, UTF_8);
            buf.append("ext type=" + type + " '" + str + "'").append(System.lineSeparator());
        } else {
            buf.append("ext type=" + type + " '");
            for (byte b : bytes) {
                buf.append(toHex(b));
            }
            buf.append("'").append(System.lineSeparator());
        }
    }

}
