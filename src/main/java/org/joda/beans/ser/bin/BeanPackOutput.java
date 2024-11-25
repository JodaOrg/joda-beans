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

import org.joda.beans.MetaBean;

/**
 * Outputter for BeanPack data, which is derived from ideas in MsgPack.
 */
final class BeanPackOutput extends BeanPack {

    /**
     * Mask to check if value is a small positive integer.
     */
    private static final int MASK_SMALL_INT_POSITIVE = 0xFFFFFF80;
    /**
     * Mask to check if value is a signed byte.
     */
    private static final int MASK_INT8 = 0x7FFFFF80;
    /**
     * Mask to check if value is a signed short.
     */
    private static final int MASK_INT16 = 0x7FFF8000;
    /**
     * Mask to check if value is a small positive integer.
     */
    private static final long MASK_SMALL_LONG_POSITIVE = 0xFFFF_FFFF_FFFF_FF80L;
    /**
     * Mask to check if value is a signed byte.
     */
    private static final long MASK_LONG8 = 0x7FFF_FFFF_FFFF_FF80L;
    /**
     * Mask to check if value is a signed short.
     */
    private static final long MASK_LONG16 = 0x7FFF_FFFF_FFFF_8000L;
    /**
     * Mask to check if value is a signed int.
     */
    private static final long MASK_LONG32 = 0x7FFF_FFFF_8000_0000L;

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

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to write to, not null
     */
    BeanPackOutput(DataOutputStream stream) {
        this.output = stream;
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
     * Writes an int.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeInt(int value) throws IOException {
        if (value >= 0) {
            if ((value & MASK_SMALL_INT_POSITIVE) == 0) {
                output.writeByte(value);
            } else if ((value & MASK_INT8) == 0) {
                output.writeByte(INT_8);
                output.writeByte((byte) value);
            } else if ((value & MASK_INT16) == 0) {
                output.writeByte(INT_16);
                output.writeShort((short) value);
            } else {
                output.writeByte(INT_32);
                output.writeInt(value);
            }
        } else {
            if (value >= MIN_FIX_INT) {
                output.writeByte(value);
            } else if (value >= -256) {
                output.writeByte(INT_8);
                output.writeByte((byte) value);
            } else if (value >= -256 * 256) {
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
            if ((value & MASK_SMALL_LONG_POSITIVE) == 0) {
                output.writeByte((byte) value);
            } else if ((value & MASK_LONG8) == 0) {
                output.writeByte(LONG_8);
                output.writeByte((byte) value);
            } else if ((value & MASK_LONG16) == 0) {
                output.writeByte(LONG_16);
                output.writeShort((short) value);
            } else if ((value & MASK_LONG32) == 0) {
                output.writeByte(LONG_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(LONG_64);
                output.writeInt((int) value);
            }
        } else {
            if (value >= MIN_FIX_INT) {
                output.writeByte((byte) value);
            } else if (value >= -256) {
                output.writeByte(LONG_8);
                output.writeByte((byte) value);
            } else if (value >= -256 * 256) {
                output.writeByte(LONG_16);
                output.writeShort((short) value);
            } else if (value >= -256L * 256 * 256 * 256) {
                output.writeByte(LONG_32);
                output.writeInt((int) value);
            } else {
                output.writeByte(LONG_64);
                output.writeInt((int) value);
            }
        }
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
     * Writes a byte.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeByte(byte value) throws IOException {
        output.writeByte(SHORT_16);
        output.writeByte(value);
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
        output.writeByte(DOUBLE_64);
        output.writeDouble(value);
    }

    /**
     * Writes a double[].
     * 
     * @param values  the values, not null
     * @throws IOException if an error occurs
     */
    void writeDoubles(double[] values) throws IOException {
        var size = values.length;
        if (size <= 255) {
            output.writeByte(DOUBLE_ARRAY_8);
            output.writeShort(size);
        } else if (size <= 65535) {
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

    /**
     * Writes a byte[].
     * 
     * @param bytes  the bytes, not null
     * @throws IOException if an error occurs
     */
    void writeBytes(byte[] bytes) throws IOException {
        var size = bytes.length;
        if (size <= 255) {
            output.writeByte(BIN_8);
            output.writeShort(size);
        } else if (size <= 65535) {
            output.writeByte(BIN_16);
            output.writeShort(size);
        } else {
            output.writeByte(BIN_32);
            output.writeInt(size);
        }
        output.write(bytes);
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
        if (size <= 45) {
            output.writeByte(MIN_FIX_STR + size);
        } else if (size < 256) {
            output.writeByte(STR_8);
            output.writeByte(size);
        } else {
            writeStringHeaderLarge(size);
        }
        output.write(bytes);
    }

    // separate out larger strings, which may benefit hotspot
    private void writeStringHeaderLarge(int size) throws IOException {
        if (size < 65536) {
            output.writeByte(STR_16);
            output.writeShort(size);
        } else {
            output.writeByte(STR_32);
            output.writeInt(size);
        }
    }

    /**
     * Writes a MessagePack array header.
     * 
     * @param size  the size
     * @throws IOException if an error occurs
     */
    void writeArrayHeader(int size) throws IOException {
        if (size <= 12) {
            output.writeByte(MIN_FIX_ARRAY + size);
        } else if (size < 256) {
            output.writeByte(ARRAY_8);
            output.writeByte(size);
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
        if (size <= 12) {
            output.writeByte(MIN_FIX_MAP + size);
        } else if (size < 256) {
            output.writeByte(MAP_8);
            output.writeByte(size);
        } else if (size < 65536) {
            output.writeByte(MAP_16);
            output.writeShort(size);
        } else {
            output.writeByte(MAP_32);
            output.writeInt(size);
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
        if (year >= 2000 && year <= 2170) {
            var ym2000 = (year - 2000) * 12 + (month - 1);
            output.write(DATE_PACKED);
            var packed = ym2000 << 5 + dom;
            output.writeShort(packed);
        } else {
            output.write(DATE);
            output.writeInt(year);
            output.writeByte(month);
            output.writeByte(dom);
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
        var lower = (int) (nod & 0xFFFFFFFF);
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
        output.write(INSTANT);
        output.writeLong(duration.getSeconds());
        output.writeInt(duration.getNano());
    }

    //-------------------------------------------------------------------------
    /**
     * Writes a bean definition.
     * 
     * @param metaBean  the meta-bean
     * @param className  the class name
     * @throws IOException if an error occurs
     */
    void writeBeanDefinitionHeader(MetaBean metaBean, String className) throws IOException {
        output.write(BEAN_DEFN);
        output.write(className.getBytes(UTF_8));
    }

    /**
     * Writes a bean reference.
     * 
     * @param ref  the reference
     * @throws IOException if an error occurs
     */
    void writeBeanReference(int ref) throws IOException {
        output.write(BEAN_REF);
        writeInt(ref);
    }

    /**
     * Writes a type name.
     * 
     * @param className  the class name
     * @throws IOException if an error occurs
     */
    void writeTypeName(String className) throws IOException {
        output.write(TYPE_NAME);
        output.write(className.getBytes(UTF_8));
    }

    /**
     * Writes a type reference.
     * 
     * @param ref  the reference
     * @throws IOException if an error occurs
     */
    void writeTypeReference(int ref) throws IOException {
        output.write(TYPE_REF);
        writeInt(ref);
    }

    /**
     * Writes a value reference.
     * 
     * @param ref  the reference
     * @throws IOException if an error occurs
     */
    void writeValueReference(int ref) throws IOException {
        output.write(VALUE_REF);
        writeInt(ref);
    }

}
