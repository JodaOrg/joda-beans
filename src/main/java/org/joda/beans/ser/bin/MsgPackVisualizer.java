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
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Allows MsgPack data to be visualized.
 *
 * @author Stephen Colebourne
 * @deprecated Use {@link JodaBeanBinReader#visualize(byte[])}
 */
@Deprecated
public final class MsgPackVisualizer extends MsgPackInput {

    /**
     * The current indent.
     */
    private String indent = "";
    /**
     * The buffer.
     */
    private StringWriter buf = new StringWriter(512);
    /**
     * The writer.
     */
    private PrintWriter writer = new PrintWriter(new StringWriter(512));

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
        writer.flush();
        System.out.println(buf.toString());
    }

    String visualizeData() {
        readAll();
        writer.flush();
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
        writer.print(indent);
        indent = indent.replace("-", " ").replace("=", " ");
    }

    @Override
    protected void handleBoolean(boolean bool) {
        writer.println(bool);
    }

    @Override
    protected void handleNil() {
        writer.println("nil");
    }

    @Override
    protected void handleInt(int value) {
        writer.println("int " + value);
    }

    @Override
    protected void handleUnsignedLong(long value) {
        writer.println("int " + value + " unsigned");
    }

    @Override
    protected void handleSignedLong(long value) {
        writer.println("int " + value + " signed");
    }

    @Override
    protected void handleFloat(float value) {
        writer.println("flt " + value);
    }

    @Override
    protected void handleDouble(double value) {
        writer.println("dbl " + value);
    }

    @Override
    protected void handleUnknown(byte b) {
        writer.println("Unknown - " + String.format("%02X ", b));
    }

    @Override
    protected void handleString(String str) {
        writer.println("str '" + str + '\'');
    }

    @Override
    protected void handleArrayHeader(int size) {
        writer.println("arr (" + size + ")");
    }

    @Override
    protected void handleMapHeader(int size) {
        writer.println("map (" + size + ")");
    }

    @Override
    protected void handleBinary(byte[] bytes) {
        writer.print("bin '");
        for (byte b : bytes) {
            writer.print(toHex(b));
        }
        writer.println("'");
    }

    @Override
    protected void handleExtension(int type, byte[] bytes) throws IOException {
        if (type == JODA_TYPE_BEAN || type == JODA_TYPE_DATA || type == JODA_TYPE_META) {
            String str = new String(bytes, UTF_8);
            writer.println("ext type=" + type + " '" + str + "'");
        } else {
            writer.print("ext type=" + type + " '");
            for (byte b : bytes) {
                writer.print(toHex(b));
            }
            writer.println("'");
        }
    }

}
