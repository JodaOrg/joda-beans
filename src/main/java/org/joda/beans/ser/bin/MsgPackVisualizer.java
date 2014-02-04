/*
 *  Copyright 2001-2014 Stephen Colebourne
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
 *
 * @author Stephen Colebourne
 */
public final class MsgPackVisualizer extends MsgPackInput {

    /**
     * The current indent.
     */
    private String indent = "";

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    public MsgPackVisualizer(byte[] bytes) {
        super(bytes);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    public MsgPackVisualizer(InputStream stream) {
        super(stream);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    public MsgPackVisualizer(DataInputStream stream) {
        super(stream);
    }

    //-----------------------------------------------------------------------
    /**
     * Visualizes the data in the stream.
     */
    public void visualize() {
        readAll();
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
        System.out.print(indent);
        indent = indent.replace("-", " ").replace("=", " ");
    }

    @Override
    protected void handleBoolean(boolean bool) {
        System.out.println(bool);
    }

    @Override
    protected void handleNil() {
        System.out.println("nil");
    }

    @Override
    protected void handleInt(int value) {
        System.out.println("int " + value);
    }

    @Override
    protected void handleUnsignedLong(long value) {
        System.out.println("int " + value + " unsigned");
    }

    @Override
    protected void handleSignedLong(long value) {
        System.out.println("int " + value + " signed");
    }

    @Override
    protected void handleFloat(float value) {
        System.out.println("flt " + value);
    }

    @Override
    protected void handleDouble(double value) {
        System.out.println("dbl " + value);
    }

    @Override
    protected void handleUnknown(byte b) {
        System.out.println("Unknown - " + String.format("%02X ", b));
    }

    @Override
    protected void handleString(String str) {
        System.out.println("str '" + str + '\'');
    }

    @Override
    protected void handleArrayHeader(int size) {
        System.out.println("arr (" + size + ")");
    }

    @Override
    protected void handleMapHeader(int size) {
        System.out.println("map (" + size + ")");
    }

    @Override
    protected void handleBinary(byte[] bytes) {
        System.out.print("bin '");
        for (byte b : bytes) {
            System.out.print(toHex(b));
        }
        System.out.println("'");
    }

    @Override
    protected void handleExtension(int type, byte[] bytes) throws IOException {
        if (type == JODA_TYPE_BEAN || type == JODA_TYPE_DATA || type == JODA_TYPE_META) {
            String str = new String(bytes, UTF_8);
            System.out.println("ext type=" + type + " '" + str + "'");
        } else {
            System.out.print("ext type=" + type + " '");
            for (byte b : bytes) {
                System.out.print(toHex(b));
            }
            System.out.println("'");
        }
    }

}
