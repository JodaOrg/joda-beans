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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Receives and processes BeanPack data.
 */
abstract class BeanPackInput extends BeanPack {

    /**
     * The stream to read.
     */
    private final DataInputStream input;

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    BeanPackInput(byte[] bytes) {
        this(new ByteArrayInputStream(bytes));
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    BeanPackInput(InputStream stream) {
        this(new DataInputStream(stream));
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    BeanPackInput(DataInputStream stream) {
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
                case MAP_8 -> map(input.readUnsignedByte());
                case MAP_16 -> map(input.readUnsignedShort());
                case MAP_32 -> map(input.readInt());
                case ARRAY_8 -> array(input.readUnsignedByte());
                case ARRAY_16 -> array(input.readUnsignedShort());
                case ARRAY_32 -> array(input.readInt());
                case STR_8 -> string(input.readUnsignedByte());
                case STR_16 -> string(input.readUnsignedShort());
                case STR_32 -> string(input.readInt());
                case NULL -> handleNull();
                case FALSE -> handleBoolean(false);
                case TRUE -> handleBoolean(true);
                case UNUSED -> handleUnknown(b);
                case FLOAT_32 -> handleFloat(input.readFloat());
                case DOUBLE_INT -> handleDouble(acceptInt());
                case DOUBLE_64 -> handleDouble(input.readDouble());
                case BYTE_8 -> handleByte(input.readByte());
                case SHORT_16 -> handleShort(input.readShort());
                case INT_8 -> handleInt(input.readUnsignedByte());
                case INT_16 -> handleInt(input.readUnsignedShort());
                case INT_32 -> handleInt(input.readInt());
                case LONG_8 -> handleLong(input.readByte());
                case LONG_16 -> handleLong(input.readShort());
                case LONG_32 -> handleLong(input.readInt());
                case LONG_64 -> handleLong(input.readLong());
                case DATE_PACKED -> datePacked();
                case DATE -> date();
                case TIME -> time();
                case INSTANT -> instant();
                case DURATION -> duration();
                case BIN_8 -> byteArray(input.readUnsignedByte());
                case BIN_16 -> byteArray(input.readUnsignedShort());
                case BIN_32 -> byteArray(input.readInt());
                case DOUBLE_ARRAY_8 -> doubleArray(input.readUnsignedByte());
                case DOUBLE_ARRAY_16 -> doubleArray(input.readUnsignedShort());
                case DOUBLE_ARRAY_32 -> doubleArray(input.readInt());
                case TYPE_NAME -> typeName();
                case TYPE_REF -> handleTypeReference(acceptInt());
                case BEAN_DEFN -> handleBeanDefinition();
                case VALUE_DEFN -> handleValueDefinition();
                case VALUE_REF -> handleValueReference(acceptInt());
                default -> handleUnknown(b);
            }
        }
    }

    //-------------------------------------------------------------------------
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

    private void string(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("String too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        var str = new String(bytes, UTF_8);
        handleString(str);
    }

    private void datePacked() throws IOException {
        var packed = input.readUnsignedShort();
        var dom = packed & 31;
        var ym = packed >> 5;
        handleDate(LocalDate.of((ym / 12) + 2000, (ym % 12) + 1, dom));
    }

    private void date() throws IOException {
        var year = input.readInt();
        var month = input.readUnsignedByte();
        var dom = input.readUnsignedByte();
        handleDate(LocalDate.of(year, month, dom));
    }

    private void time() throws IOException {
        var upper = (long) input.readShort();
        var lower = Integer.toUnsignedLong(input.readInt());
        var nod = (upper << 32) + lower;
        handleTime(LocalTime.ofNanoOfDay(nod));
    }

    private void instant() throws IOException {
        var second = input.readLong();
        var nanos = input.readInt();
        handleInstant(Instant.ofEpochSecond(second, nanos));
    }

    private void duration() throws IOException {
        var seconds = input.readLong();
        var nanos = input.readInt();
        handleDuration(Duration.ofSeconds(seconds, nanos));
    }

    private void byteArray(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("Byte array too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        handleBinary(bytes);
    }

    private void doubleArray(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("Double array too large");
        }
        var values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = input.readDouble();
        }
        handleDoubleArray(values);
    }

    private void typeName() throws IOException {
        var typeByte = input.readByte();
        int size;
        if (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) {
            size = typeByte - MIN_FIX_STR;
        } else if (typeByte == STR_8) {
            size = input.readUnsignedByte();
        } else if (typeByte == STR_16) {
            size = input.readUnsignedShort();
        } else {
            throw new IllegalArgumentException(
                    "Invalid type name, expected string data type, but was: 0x" + toHex(typeByte));
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        var str = new String(bytes, UTF_8);
        handleTypeName(str);
    }

    private int acceptInt() throws IOException {
        var typeByte = input.readByte();
        if (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) {
            return typeByte;
        }
        return switch (typeByte) {
            case INT_8 -> input.readByte();
            case INT_16 -> input.readShort();
            case INT_32 -> input.readInt();
            default -> throw new IllegalArgumentException(
                    "Invalid type name, expected int data type, but was: 0x" + toHex(typeByte));
        };
    }

    void readAnnotatedValue() throws IOException {
        var next = input.readUnsignedByte();
        readObject(next);
    }

    //-------------------------------------------------------------------------
    void handleObjectStart() {
    }

    void handleMapHeader(int size) {
    }

    void handleArrayHeader(int size) {
    }

    void handleString(String str) {
    }

    void handleNull() {
    }

    void handleBoolean(boolean bool) {
    }

    void handleFloat(float value) {
    }

    void handleDouble(double value) {
    }

    void handleByte(byte value) {
    }

    void handleShort(short value) {
    }

    void handleInt(int value) {
    }

    void handleLong(long value) {
    }

    void handleDate(LocalDate date) {
    }

    void handleTime(LocalTime time) {
    }

    void handleInstant(Instant instant) {
    }

    void handleDuration(Duration duration) {
    }

    void handleBinary(byte[] bytes) {
    }

    void handleDoubleArray(double[] values) {
    }

    void handleTypeName(String typeName) throws IOException {
    }

    void handleTypeReference(int ref) throws IOException {
    }

    void handleBeanDefinition() throws IOException {
    }

    void handleValueDefinition() throws IOException {
    }

    void handleValueReference(int ref) throws IOException {
    }

    void handleUnknown(byte b) {
    }

    //-----------------------------------------------------------------------
    /**
     * Skips over the next object in an input stream.
     * 
     * @param input  the input stream, not null
     * @throws IOException if an error occurs
     */
    static void skipObject(DataInputStream input) throws IOException {
        new Skipper(input).skip(input.readByte());
    }

    private static class Skipper extends BeanPackInput {
        Skipper(DataInputStream input) {
            super(input);
        }

        void skip(int typeByte) throws IOException {
            readObject(typeByte);
        }
    }

}
