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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Allows BeanPack data to be visualized.
 */
final class BeanPackVisualizer extends BeanPackInput {

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
    BeanPackVisualizer(byte[] bytes) {
        super(bytes);
    }

    //-----------------------------------------------------------------------
    /**
     * Visualizes the data in the stream.
     */
    String visualizeData() {
        try {
            readAll();
            return buf.toString();
        } catch (Exception ex) {
            return buf.append("!!ERROR!!").append(System.lineSeparator()).append(ex.toString()).toString();
        }
    }

    //-----------------------------------------------------------------------
    @Override
    Object readMapKey() throws IOException {
        indent = indent + "= ";
        var value = super.readMapKey();
        indent = indent.substring(0, indent.length() - 2);
        return value;
    }

    @Override
    Object readMapValue() throws IOException {
        indent = indent + "  ";
        var value = super.readMapValue();
        indent = indent.substring(0, indent.length() - 2);
        return value;
    }

    @Override
    Object readArrayItem() throws IOException {
        indent = indent + "- ";
        var value = super.readArrayItem();
        indent = indent.substring(0, indent.length() - 2);
        return value;
    }

    @Override
    Object readBeanItem() throws IOException {
        indent = indent + "- ";
        var value = super.readBeanItem();
        indent = indent.substring(0, indent.length() - 2);
        return value;
    }

    @Override
    void handleObjectStart() {
        buf.append(indent);
        indent = indent.replace("-", " ").replace("=", " ");
    }

    //-------------------------------------------------------------------------
    @Override
    void handleNull() {
        buf.append("null").append(System.lineSeparator());
    }

    @Override
    void handleBoolean(boolean bool) {
        buf.append(bool).append(System.lineSeparator());
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
    void handleChar(char value) {
        buf.append("chr ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleByte(byte value) {
        buf.append("byt ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleShort(short value) {
        buf.append("sht ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleInt(int value) {
        buf.append("int ").append(value).append(System.lineSeparator());
    }

    @Override
    void handleLong(long value) {
        buf.append("lng ").append(value).append(System.lineSeparator());
    }

    //-------------------------------------------------------------------------
    @Override
    void handleDate(LocalDate date) {
        buf.append(date).append(System.lineSeparator());
    }

    @Override
    void handleTime(LocalTime time) {
        buf.append(time).append(System.lineSeparator());
    }

    @Override
    void handleInstant(Instant instant) {
        buf.append(instant).append(System.lineSeparator());
    }

    @Override
    void handleDuration(Duration duration) {
        buf.append(duration).append(System.lineSeparator());
    }

    //-------------------------------------------------------------------------
    @Override
    void handleMapHeader(int size) {
        buf.append("map (").append(size).append(")").append(System.lineSeparator());
    }

    @Override
    void handleArrayHeader(int size) {
        buf.append("arr (").append(size).append(")").append(System.lineSeparator());
    }

    @Override
    void handleString(String str) {
        buf.append("str '").append(str).append('\'').append(System.lineSeparator());
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
    void handleDoubleArray(double[] values) {
        buf.append("dbl [");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                buf.append(System.lineSeparator()).append("     ");
            }
            for (int j = 0; j < 8 && i < values.length; j++, i++) {
                if (j > 0) {
                    buf.append(',');
                }
                buf.append(values[i]);
            }
        }
        buf.append("]").append(System.lineSeparator());
    }

    //-------------------------------------------------------------------------
    @Override
    void handleTypeName(String typeName) throws IOException {
        buf.append("@type ").append(typeName).append(System.lineSeparator());
    }

    @Override
    void handleTypeReference(int ref, String typeName) throws IOException {
        var str = ref < 0 ? typeName : ref + " " + typeName;
        buf.append("@typeref ").append(str).append(System.lineSeparator());
    }

    @Override
    void handleBeanHeader(int propertyCount) throws IOException {
        buf.append("bean (").append(propertyCount).append(")").append(System.lineSeparator());
    }

    @Override
    void handleValueDefinition() throws IOException {
        buf.append("@value ").append(System.lineSeparator());
    }

    @Override
    void handleValueReference(int ref, Object value) {
        buf.append("ref ").append(ref).append(" '").append(value).append('\'').append(System.lineSeparator());
    }

    //-------------------------------------------------------------------------
    @Override
    void handleUnknown(byte b) {
        buf.append("Unknown - ").append(String.format("%02X ", b)).append(System.lineSeparator());
    }
}
