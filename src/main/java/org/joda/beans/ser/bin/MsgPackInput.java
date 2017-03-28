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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Receives and processes MsgPack data.
 *
 * @author Stephen Colebourne
 */
abstract class MsgPackInput extends MsgPack {

    /**
     * The stream to read.
     */
    private final DataInputStream input;

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    MsgPackInput(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    MsgPackInput(InputStream stream) {
        this(new DataInputStream(stream));
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    MsgPackInput(DataInputStream stream) {
        this.input = stream;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads all the data in the stream, closing the stream.
     */
    protected void readAll() {
        try {
            try {
                int b = input.read();
                while (b >= 0) {
                    readObject(b);
                    b = input.read();
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //-----------------------------------------------------------------------
    protected void readObject(int unsigned) throws IOException {
        handleObjectStart();
        byte b = (byte) unsigned;
        if (b >= MIN_FIX_INT) {  // no need to check for b <= MAX_FIX_INT
            handleInt(b);
            
        } else if (b >= MIN_FIX_STR && b <= MAX_FIX_STR) {
            string(b - MIN_FIX_STR);
            
        } else if (b >= MIN_FIX_ARRAY && b <= MAX_FIX_ARRAY) {
            array(b - MIN_FIX_ARRAY);
            
        } else if (b >= MIN_FIX_MAP && b <= MAX_FIX_MAP) {
            map(b - MIN_FIX_MAP);
            
        } else {
            switch ((int) b) {
                case NIL:
                    handleNil();
                    break;
                case FALSE:
                    handleBoolean(false);
                    break;
                case TRUE:
                    handleBoolean(true);
                    break;
                case BIN_8: {
                    binary(input.readUnsignedByte());
                    break;
                }
                case BIN_16: {
                    binary(input.readUnsignedShort());
                    break;
                }
                case BIN_32: {
                    binary(input.readInt());
                    break;
                }
                case EXT_8: {
                    extension(input.readUnsignedByte());
                    break;
                }
                case EXT_16: {
                    extension(input.readUnsignedShort());
                    break;
                }
                case EXT_32: {
                    extension(input.readInt());
                    break;
                }
                case FLOAT_32: {
                    float value = input.readFloat();
                    handleFloat(value);
                    break;
                }
                case FLOAT_64: {
                    double value = input.readDouble();
                    handleDouble(value);
                    break;
                }
                case UINT_8: {
                    int value = input.readUnsignedByte();
                    handleInt(value);
                    break;
                }
                case UINT_16: {
                    int value = input.readUnsignedShort();
                    handleInt(value);
                    break;
                }
                case UINT_32: {
                    int val = input.readInt();
                    if (val >= 0) {
                        handleInt(val);
                    } else {
                        long value = ((long) val) & 0xFFFFFFFFL;
                        handleUnsignedLong(value);
                    }
                    break;
                }
                case UINT_64: {
                    long value = input.readLong();
                    handleUnsignedLong(value);
                    break;
                }
                case SINT_8: {
                    int value = input.readByte();
                    handleInt(value);
                    break;
                }
                case SINT_16: {
                    int value = input.readShort();
                    handleInt(value);
                    break;
                }
                case SINT_32: {
                    int value = input.readInt();
                    handleInt(value);
                    break;
                }
                case SINT_64: {
                    long value = input.readLong();
                    handleSignedLong(value);
                    break;
                }
                case FIX_EXT_1: {
                    extension(1);
                    break;
                }
                case FIX_EXT_2: {
                    extension(2);
                    break;
                }
                case FIX_EXT_4: {
                    extension(4);
                    break;
                }
                case FIX_EXT_8: {
                    extension(8);
                    break;
                }
                case FIX_EXT_16: {
                    extension(16);
                    break;
                }
                case STR_8: {
                    string(input.readUnsignedByte());
                    break;
                }
                case STR_16: {
                    string(input.readUnsignedShort());
                    break;
                }
                case STR_32: {
                    string(input.readInt());
                    break;
                }
                case ARRAY_16: {
                    array(input.readUnsignedShort());
                    break;
                }
                case ARRAY_32: {
                    array(input.readInt());
                    break;
                }
                case MAP_16: {
                    map(input.readUnsignedShort());
                    break;
                }
                case MAP_32: {
                    map(input.readInt());
                    break;
                }
                default:
                    handleUnknown(b);
            }
        }
    }

    private void string(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("String too large");
        }
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        String str = new String(bytes, UTF_8);
        handleString(str);
    }

    private void array(int size) throws IOException {
        handleArrayHeader(size);
        for (int i = 0; i < size; i++) {
            readArrayItem();
        }
    }

    protected void readArrayItem() throws IOException {
        int next = input.readUnsignedByte();
        readObject(next);
    }

    private void map(int size) throws IOException {
        handleMapHeader(size);
        for (int i = 0; i < size; i++) {
            readMapKey();
            readMapValue();
        }
    }

    protected void readMapKey() throws IOException {
        int next = input.readUnsignedByte();
        readObject(next);
    }

    protected void readMapValue() throws IOException {
        int next = input.readUnsignedByte();
        readObject(next);
    }

    private void binary(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("Binary too large");
        }
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        handleBinary(bytes);
    }

    private void extension(int size) throws IOException {
        int type = input.readByte();
        if (size < 0) {
            throw new IllegalStateException("Extension too large");
        }
        byte[] bytes = new byte[size];
        input.readFully(bytes);
        handleExtension(type, bytes);
    }

    protected void handleObjectStart() {
    }

    protected void handleBoolean(boolean bool) {
    }

    protected void handleNil() {
    }

    protected void handleInt(int value) {
    }

    protected void handleUnsignedLong(long value) {
    }

    protected void handleSignedLong(long value) {
    }

    protected void handleFloat(float value) {
    }

    protected void handleDouble(double value) {
    }

    protected void handleUnknown(byte b) {
    }

    protected void handleString(String str) {
    }

    protected void handleArrayHeader(int size) {
    }

    protected void handleMapHeader(int size) {
    }

    protected void handleBinary(byte[] bytes) throws IOException {
    }

    protected void handleExtension(int type, byte[] bytes) throws IOException {
    }

    //-----------------------------------------------------------------------
    /**
     * Skips over the next object in an input stream.
     * 
     * @param input  the input stream, not null
     * @throws IOException if an error occurs
     */
    public static void skipObject(DataInputStream input) throws IOException {
        new Skipper(input).skip(input.readByte());
    }

    private static class Skipper extends MsgPackInput {
        Skipper(DataInputStream input) {
            super(input);
        }
        void skip(int typeByte) throws IOException {
            readObject(typeByte);
        }
    }

}
