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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.joda.beans.ResolvedType;

/**
 * Outputter for BeanPack data, which is derived from ideas in MsgPack.
 */
final class BeanPackOutput extends BeanPack {

    /**
     * Mask to check if value is a small positive integer, from 0 to 127 inclusive.
     */
    private static final int MASK_SMALL_INT_POSITIVE = 0xFFFFFF80;

    /**
     * The stream to write to.
     */
    private final DataOutputStream output;

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to write to, not null
     */
    BeanPackOutput(OutputStream stream) {
        this.output = new DataOutputStream(stream);
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a null.
     * 
     * @throws IOException if an error occurs
     */
    void writeNull() throws IOException {
        output.writeByte(NULL);
    }

    /**
     * Writes a boolean.
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
     * Writes a float.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeFloat(float value) throws IOException {
        output.writeByte(FLOAT_32);
        output.writeFloat(value);
    }

    /**
     * Writes a double.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeDouble(double value) throws IOException {
        var intValue = (int) value;
        if (value == intValue && intValue <= Byte.MAX_VALUE && intValue >= Byte.MIN_VALUE && Double.compare(value, -0d) != 0) {
            output.writeByte(DOUBLE_INT_8);
            output.writeByte(intValue);
        } else {
            output.writeByte(DOUBLE_64);
            output.writeDouble(value);
        }
    }

    /**
     * Writes a char.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeChar(char value) throws IOException {
        output.writeByte(CHAR_16);
        output.writeChar(value);
    }

    /**
     * Writes a byte.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeByte(byte value) throws IOException {
        output.writeByte(BYTE_8);
        output.writeByte(value);
    }

    /**
     * Writes a short.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeShort(short value) throws IOException {
        output.writeByte(SHORT_16);
        output.writeShort(value);
    }

    /**
     * Writes an int.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeInt(int value) throws IOException {
        if ((value & MASK_SMALL_INT_POSITIVE) == 0) {
            output.writeByte(value);
        } else if (value >= 0) {
            if (value <= Short.MAX_VALUE) {
                output.writeByte(INT_16);
                output.writeShort((short) value);
            } else {
                output.writeByte(INT_32);
                output.writeInt(value);
            }
        } else {
            if (value >= MIN_FIX_INT) {
                output.writeByte(value);
            } else if (value >= Short.MIN_VALUE) {
                output.writeByte(INT_16);
                output.writeShort((short) value);
            } else {
                output.writeByte(INT_32);
                output.writeInt(value);
            }
        }
    }

    /**
     * Writes a long.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeLong(long value) throws IOException {
        if (value >= 0) {
            if (value <= Byte.MAX_VALUE) {
                output.writeByte(LONG_8);
                output.writeByte((byte) value);
            } else if (value <= Short.MAX_VALUE) {
                output.writeByte(LONG_16);
                output.writeShort((short) value);
            } else if (value <= Integer.MAX_VALUE) {
                output.writeByte(LONG_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(LONG_64);
                output.writeLong(value);
            }
        } else {
            if (value >= Byte.MIN_VALUE) {
                output.writeByte(LONG_8);
                output.writeByte((byte) value);
            } else if (value >= Short.MIN_VALUE) {
                output.writeByte(LONG_16);
                output.writeShort((short) value);
            } else if (value >= Integer.MIN_VALUE) {
                output.writeByte(LONG_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(LONG_64);
                output.writeLong(value);
            }
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a date.
     * 
     * @param date  the date
     * @throws IOException if an error occurs
     */
    void writeDate(LocalDate date) throws IOException {
        var year = date.getYear();
        var month = date.getMonthValue();
        var dom = date.getDayOfMonth();
        if (year >= 2000 && year <= 2169) {
            var ym2000 = (year - 2000) * 12 + (month - 1);
            output.write(DATE_PACKED);
            var packed = (ym2000 << 5) + dom;
            output.writeShort(packed);
        } else {
            output.write(DATE);
            var packed = (((long) year) << 9) + (month << 5) + dom;
            output.writeInt((int) (packed >> 8));
            output.writeByte((byte) (packed & 0xFF));
        }
    }

    /**
     * Writes a time.
     * 
     * @param time  the time
     * @throws IOException if an error occurs
     */
    void writeTime(LocalTime time) throws IOException {
        var nod = time.toNanoOfDay();
        var upper = (int) (nod >>> 32);
        var lower = (int) (nod & 0xFFFFFFFFL);
        output.write(TIME);
        output.writeShort(upper);
        output.writeInt(lower);
    }

    /**
     * Writes an instant.
     * 
     * @param instant  the instant
     * @throws IOException if an error occurs
     */
    void writeInstant(Instant instant) throws IOException {
        output.write(INSTANT);
        output.writeLong(instant.getEpochSecond());
        output.writeInt(instant.getNano());
    }

    /**
     * Writes a duration.
     * 
     * @param duration  the instant
     * @throws IOException if an error occurs
     */
    void writeDuration(Duration duration) throws IOException {
        output.write(DURATION);
        output.writeLong(duration.getSeconds());
        output.writeInt(duration.getNano());
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a byte[].
     * 
     * @param bytes  the bytes, not null
     * @throws IOException if an error occurs
     */
    void writeBytes(byte[] bytes) throws IOException {
        // positive numbers only
        var size = bytes.length;
        if (size <= 0xFF) {
            output.writeByte(BIN_8);
            output.writeByte(size);
        } else if (size <= 0xFFFF) {
            output.writeByte(BIN_16);
            output.writeShort(size);
        } else {
            output.writeByte(BIN_32);
            output.writeInt(size);
        }
        output.write(bytes);
    }

    /**
     * Writes a double[].
     * 
     * @param values  the values, not null
     * @throws IOException if an error occurs
     */
    void writeDoubles(double[] values) throws IOException {
        // positive numbers only
        var size = values.length;
        if (size <= 0xFF) {
            output.writeByte(DOUBLE_ARRAY_8);
            output.writeByte(size);
        } else if (size <= 0xFFFF) {
            output.writeByte(DOUBLE_ARRAY_16);
            output.writeShort(size);
        } else {
            output.writeByte(DOUBLE_ARRAY_32);
            output.writeInt(size);
        }
        for (double value : values) {
            output.writeDouble(value);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a map header.
     * 
     * @param size  the size
     * @throws IOException if an error occurs
     */
    void writeMapHeader(int size) throws IOException {
        // positive numbers only
        if (size <= 12) {
            output.writeByte(MIN_FIX_MAP + size);
        } else if (size <= 0xFF) {
            output.writeByte(MAP_8);
            output.writeByte(size);
        } else if (size <= 0xFFFF) {
            output.writeByte(MAP_16);
            output.writeShort(size);
        } else {
            output.writeByte(MAP_32);
            output.writeInt(size);
        }
    }

    /**
     * Writes an array header.
     * 
     * @param size  the size
     * @throws IOException if an error occurs
     */
    void writeArrayHeader(int size) throws IOException {
        // positive numbers only
        if (size <= 12) {
            output.writeByte(MIN_FIX_ARRAY + size);
        } else if (size <= 0xFF) {
            output.writeByte(ARRAY_8);
            output.writeByte(size);
        } else if (size <= 0xFFFF) {
            output.writeByte(ARRAY_16);
            output.writeShort(size);
        } else {
            output.writeByte(ARRAY_32);
            output.writeInt(size);
        }
    }

    /**
     * Writes a String.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeString(String value) throws IOException {
        // Java 21 performance testing showed manually converting to UTF-8 to be slower
        var bytes = value.getBytes(UTF_8);
        var size = bytes.length;
        if (size <= (MAX_FIX_STR - MIN_FIX_STR)) {
            output.writeByte(MIN_FIX_STR + size);
        } else {
            writeStringHeaderLarge(size);
        }
        output.write(bytes);
    }

    // separate out larger strings, which may benefit hotspot
    private void writeStringHeaderLarge(int size) throws IOException {
        // positive numbers only
        if (size <= 0xFF) {
            output.writeByte(STR_8);
            output.writeByte(size);
        } else if (size <= 0xFFFF) {
            output.writeByte(STR_16);
            output.writeShort(size);
        } else {
            output.writeByte(STR_32);
            output.writeInt(size);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a type name.
     * <p>
     * The type name is a class name, not a {@link ResolvedType}.
     * 
     * @param className  the class name
     * @throws IOException if an error occurs
     */
    void writeTypeName(String className) throws IOException {
        // written directly, as this is not part of the value definition setup
        var bytes = className.getBytes(UTF_8);
        if (bytes.length <= 0xFF) {
            output.write(TYPE_DEFN_8);
            output.writeByte(bytes.length);
        } else {  // assume type name length will be < 0xFFFF
            output.write(TYPE_DEFN_16);
            output.writeShort(bytes.length);
        }
        output.write(bytes);
    }

    /**
     * Writes a type reference.
     * 
     * @param ref  the reference
     * @throws IOException if an error occurs
     */
    void writeTypeReference(int ref) throws IOException {
        // allow negative numbers >= -128
        if (ref <= 0x7F) {
            output.write(TYPE_REF_8);
            output.writeByte(ref);
        } else {
            output.write(TYPE_REF_16);
            output.writeShort(ref);
        }
    }

    /**
     * Writes a bean definition header.
     * 
     * @param propertyCount  the count of properties, must be 0 to 255
     * @throws IOException if an error occurs
     */
    void writeBeanDefinitionHeader(int propertyCount) throws IOException {
        // 0 to 255
        output.write(BEAN_DEFN);
        output.writeByte(propertyCount);
    }

    /**
     * Writes a value definition header.
     * 
     * @throws IOException if an error occurs
     */
    void writeValueDefinitionHeader() throws IOException {
        output.write(VALUE_DEFN);
    }

    /**
     * Writes a value reference.
     * 
     * @param ref  the reference
     * @throws IOException if an error occurs
     */
    void writeValueReference(int ref) throws IOException {
        // positive numbers only
        if (ref <= 0xFF) {
            output.write(VALUE_REF_8);
            output.writeByte(ref);
        } else if (ref <= 0xFFFF) {
            output.write(VALUE_REF_16);
            output.writeShort(ref);
        } else {
            output.write(VALUE_REF_24);
            output.writeByte(ref >>> 16);
            output.writeShort(ref & 0xFFFF);
        }
    }

}
