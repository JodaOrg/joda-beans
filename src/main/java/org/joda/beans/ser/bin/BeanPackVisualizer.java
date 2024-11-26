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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

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
     * The value definitions.
     */
    private final List<Object> valueDefinitions = new ArrayList<>();
    /**
     * The last string that was read.
     */
    private String lastString;

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    BeanPackVisualizer(byte[] bytes) {
        super(bytes);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    BeanPackVisualizer(InputStream stream) {
        super(stream);
    }

    /**
     * Creates an instance.
     * 
     * @param stream  the stream to read from, not null
     */
    BeanPackVisualizer(DataInputStream stream) {
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
    void readArrayItem() throws IOException {
        indent = indent + "- ";
        super.readArrayItem();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    void readMapKey() throws IOException {
        indent = indent + "= ";
        super.readMapKey();
        indent = indent.substring(0, indent.length() - 2);
    }

    @Override
    void readMapValue() throws IOException {
        indent = indent + "  ";
        super.readMapValue();
        indent = indent.substring(0, indent.length() - 2);
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
        lastString = str;
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
            for (int j = 0; j < 4 && i < values.length; j++, i++) {
                buf.append(values[i]).append(',');
            }
            buf.append(System.lineSeparator()).append("     ");
        }
        buf.append("]").append(System.lineSeparator());
    }

    @Override
    void handleTypeName(String typeName) throws IOException {
        buf.append("@type ").append(typeName).append(System.lineSeparator());
        readAnnotatedValue();
    }

    @Override
    void handleTypeReference(int ref) throws IOException {
        buf.append("@typeref ").append(ref).append(System.lineSeparator());
        readAnnotatedValue();
    }

    @Override
    void handleBeanDefinition() throws IOException {
        buf.append("@beandefn ").append(System.lineSeparator());
        readAnnotatedValue();
    }

    @Override
    void handleValueDefinition() throws IOException {
        buf.append("@valuedefn ").append(System.lineSeparator());
        readAnnotatedValue();
        valueDefinitions.add(lastString);
    }

    @Override
    void handleValueReference(int ref) throws IOException {
        buf.append("ref ").append(ref).append(" '").append(valueDefinitions.get(ref)).append('\'').append(System.lineSeparator());
    }

    @Override
    void handleUnknown(byte b) {
        buf.append("Unknown - ").append(String.format("%02X ", b)).append(System.lineSeparator());
    }
}
