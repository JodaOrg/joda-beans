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
    void readAll() {
        try {
            try {
                var b = input.read();
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
    void readObject(int unsigned) throws IOException {
        handleObjectStart();
        var b = (byte) unsigned;
        if (b >= MIN_FIX_INT) {  // no need to check for b <= MAX_FIX_INT
            handleInt(b);
            
        } else if (b >= MIN_FIX_STR && b <= MAX_FIX_STR) {
            string(b - MIN_FIX_STR);
            
        } else if (b >= MIN_FIX_ARRAY && b <= MAX_FIX_ARRAY) {
            array(b - MIN_FIX_ARRAY);
            
        } else if (b >= MIN_FIX_MAP && b <= MAX_FIX_MAP) {
            map(b - MIN_FIX_MAP);
            
        } else {
            switch (b) {
                case NIL -> handleNil();
                case FALSE -> handleBoolean(false);
                case TRUE -> handleBoolean(true);
                case BIN_8 -> binary(input.readUnsignedByte());
                case BIN_16 -> binary(input.readUnsignedShort());
                case BIN_32 -> binary(input.readInt());
                case EXT_8 -> extension(input.readUnsignedByte(), false);
                case EXT_16 -> extension(input.readUnsignedShort(), false);
                case EXT_32 -> extension(input.readInt(), false);
                case FLOAT_32 -> handleFloat(input.readFloat());
                case FLOAT_64 -> handleDouble(input.readDouble());
                case UINT_8 -> handleInt(input.readUnsignedByte());
                case UINT_16 -> handleInt(input.readUnsignedShort());
                case UINT_32 -> {
                    var val = input.readInt();
                    if (val >= 0) {
                        handleInt(val);
                    } else {
                        handleUnsignedLong(Integer.toUnsignedLong(val));
                    }
                }
                case UINT_64 -> handleUnsignedLong(input.readLong());
                case SINT_8 -> handleInt((int) input.readByte());
                case SINT_16 -> handleInt((int) input.readShort());
                case SINT_32 -> handleInt(input.readInt());
                case SINT_64 -> handleSignedLong(input.readLong());
                case FIX_EXT_1 -> extension(1, true);
                case FIX_EXT_2 -> extension(2, true);
                case FIX_EXT_4 -> extension(4, true);
                case FIX_EXT_8 -> extension(8, true);
                case FIX_EXT_16 -> extension(16, true);
                case STR_8 -> string(input.readUnsignedByte());
                case STR_16 -> string(input.readUnsignedShort());
                case STR_32 -> string(input.readInt());
                case ARRAY_16 -> array(input.readUnsignedShort());
                case ARRAY_32 -> array(input.readInt());
                case MAP_16 -> map(input.readUnsignedShort());
                case MAP_32 -> map(input.readInt());
                default -> handleUnknown(b);
            }
        }
    }

    private void string(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("String too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        var str = new String(bytes, UTF_8);
        handleString(str);
    }

    private void array(int size) throws IOException {
        handleArrayHeader(size);
        for (var i = 0; i < size; i++) {
            readArrayItem();
        }
    }

    void readArrayItem() throws IOException {
        var next = input.readUnsignedByte();
        readObject(next);
    }

    private void map(int size) throws IOException {
        handleMapHeader(size);
        for (var i = 0; i < size; i++) {
            readMapKey();
            readMapValue();
        }
    }

    void readMapKey() throws IOException {
        var next = input.readUnsignedByte();
        readObject(next);
    }

    void readMapValue() throws IOException {
        var next = input.readUnsignedByte();
        readObject(next);
    }

    private void binary(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("Binary too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        handleBinary(bytes);
    }

    private void extension(int size, boolean numeric) throws IOException {
        int type = input.readByte();
        if (size < 0) {
            throw new IllegalStateException("Extension too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        handleExtension(type, numeric, bytes);
    }

    void handleObjectStart() {
    }

    void handleBoolean(boolean bool) {
    }

    void handleNil() {
    }

    void handleInt(int value) {
    }

    void handleUnsignedLong(long value) {
    }

    void handleSignedLong(long value) {
    }

    void handleFloat(float value) {
    }

    void handleDouble(double value) {
    }

    void handleUnknown(byte b) {
    }

    void handleString(String str) {
    }

    void handleArrayHeader(int size) {
    }

    void handleMapHeader(int size) {
    }

    void handleBinary(byte[] bytes) {
    }

    void handleExtension(int type, boolean numeric, byte[] bytes) {
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
