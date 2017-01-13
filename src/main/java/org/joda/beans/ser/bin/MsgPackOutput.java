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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Outputter for MsgPack data.
 *
 * @author Stephen Colebourne
 */
final class MsgPackOutput extends MsgPack {

    /**
     * The stream to write to.
     */
    private final DataOutputStream output;

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to write to, not null
     */
    MsgPackOutput(OutputStream stream) {
        this.output = new DataOutputStream(stream);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to write to, not null
     */
    MsgPackOutput(DataOutputStream stream) {
        this.output = stream;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a MessagePack nil.
     * 
     * @throws IOException if an error occurs
     */
    void writeNil() throws IOException {
        output.writeByte(NIL);
    }

    /**
     * Writes a MessagePack boolean.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeBoolean(boolean value) throws IOException {
        if (value) {
            output.writeByte(TRUE);
        } else {
            output.writeByte(FALSE);
        }
    }

    /**
     * Writes a MessagePack int.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeInt(int value) throws IOException {
        if (value < MIN_FIX_INT) {
            // large negative
            if (value >= Byte.MIN_VALUE) {
                output.writeByte(SINT_8);
                output.writeByte((byte) value);
            } else if (value >= Short.MIN_VALUE) {
                output.writeByte(SINT_16);
                output.writeShort((short) value);
            } else {
                output.writeByte(SINT_32);
                output.writeInt(value);
            }
        } else if (value < MAX_FIX_INT) {
            // in range -64 to 127
            output.writeByte(value);
        } else {
            // large positive
            if (value < 0xFF) {
                output.writeByte(UINT_8);
                output.writeByte((byte) value);
            } else if (value < 0xFFFF) {
                output.writeByte(UINT_16);
                output.writeShort((short) value);
            } else {
                output.writeByte(UINT_32);
                output.writeInt(value);
            }
        }
    }

    /**
     * Writes a MessagePack long.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeLong(long value) throws IOException {
        if (value < MIN_FIX_INT) {
            // large negative
            if (value >= Byte.MIN_VALUE) {
                output.writeByte(SINT_8);
                output.writeByte((byte) value);
            } else if (value >= Short.MIN_VALUE) {
                output.writeByte(SINT_16);
                output.writeShort((short) value);
            } else if (value >= Integer.MIN_VALUE) {
                output.writeByte(SINT_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(SINT_64);
                output.writeLong(value);
            }
        } else if (value < MAX_FIX_INT) {
            // in range -64 to 127
            output.writeByte((byte) value);
        } else {
            // large positive
            if (value < 0xFF) {
                output.writeByte(UINT_8);
                output.writeByte((byte) value);
            } else if (value < 0xFFFF) {
                output.writeByte(UINT_16);
                output.writeShort((short) value);
            } else if (value < 0xFFFFFFFFL) {
                output.writeByte(UINT_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(UINT_64);
                output.writeLong(value);
            }
        }
    }

    /**
     * Writes a MessagePack float.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeFloat(float value) throws IOException {
        output.writeByte(FLOAT_32);
        output.writeFloat(value);
    }

    /**
     * Writes a MessagePack double.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeDouble(double value) throws IOException {
        output.writeByte(FLOAT_64);
        output.writeDouble(value);
    }

    /**
     * Writes a MessagePack byte block.
     * 
     * @param bytes  the bytes, not null
     * @throws IOException if an error occurs
     */
    void writeBytes(byte[] bytes) throws IOException {
        int size = bytes.length;
        if (size < 256) {
            output.writeByte(BIN_8);
            output.writeByte(size);
        } else if (size < 65536) {
            output.writeByte(BIN_16);
            output.writeShort(size);
        } else {
            output.writeByte(BIN_32);
            output.writeInt(size);
        }
        output.write(bytes);
    }

    /**
     * Writes a MessagePack string.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeString(String value) throws IOException {
        byte[] bytes = toUTF8(value);
        int size = bytes.length;
        if (size < 32) {
            output.writeByte(MIN_FIX_STR + size);
        } else if (size < 256) {
            output.writeByte(STR_8);
            output.writeByte(size);
        } else if (size < 65536) {
            output.writeByte(STR_16);
            output.writeShort(size);
        } else {
            output.writeByte(STR_32);
            output.writeInt(size);
        }
        output.write(bytes);
    }

    private byte[] toUTF8(String value) {
        // inline common ASCII case for much better performance
        final int size = value.length();
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            char ch = value.charAt(i);
            if (ch < 128) {
                bytes[i] = (byte) ch;
            } else {
                return value.getBytes(UTF_8);
            }
        }
        return bytes;
    }

    /**
     * Writes a MessagePack array header.
     * 
     * @param size  the size
     * @throws IOException if an error occurs
     */
    void writeArrayHeader(int size) throws IOException {
        if (size < 16) {
            output.writeByte(MIN_FIX_ARRAY + size);
        } else if (size < 65536) {
            output.writeByte(ARRAY_16);
            output.writeShort(size);
        } else {
            output.writeByte(ARRAY_32);
            output.writeInt(size);
        }
    }

    /**
     * Writes a MessagePack map header.
     * 
     * @param size  the size
     * @throws IOException if an error occurs
     */
    void writeMapHeader(int size) throws IOException {
        if (size < 16) {
            output.writeByte(MIN_FIX_MAP + size);
        } else if (size < 65536) {
            output.writeByte(MAP_16);
            output.writeShort(size);
        } else {
            output.writeByte(MAP_32);
            output.writeInt(size);
        }
    }

    /**
     * Writes an extension string using FIX_EXT_1.
     * 
     * @param extensionType  the type
     * @param value  the value to write as the data
     * @throws IOException if an error occurs
     */
    void writeExtensionByte(int extensionType, int value) throws IOException {
        output.write(FIX_EXT_1);
        output.write(extensionType);
        output.write(value);
    }

    /**
     * Writes an extension string using EXT_8.
     * 
     * @param extensionType  the type
     * @param str  the string to write as the data
     * @throws IOException if an error occurs
     */
    void writeExtensionString(int extensionType, String str) throws IOException {
        byte[] bytes = str.getBytes(UTF_8);
        if (bytes.length > 256) {
            throw new IllegalArgumentException("String too long");
        }
        output.write(EXT_8);
        output.write(bytes.length);
        output.write(extensionType);
        output.write(bytes);
    }

}
