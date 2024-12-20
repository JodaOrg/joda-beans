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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Receives and processes BeanPack data.
 * <p>
 * This interprets based on the data in the input, and does not interpret data into beans.
 */
abstract class BeanPackInput extends BeanPack {

    /**
     * The stream to read.
     */
    private final DataInputStream input;
    /**
     * The type definitions.
     */
    private final List<String> typeDefinitions = new ArrayList<>();
    /**
     * The value definitions.
     */
    private final List<Object> valueDefinitions = new ArrayList<>();

    /**
     * Creates an instance.
     * 
     * @param bytes  the bytes to read, not null
     */
    BeanPackInput(byte[] bytes) {
        this(new DataInputStream(new ByteArrayInputStream(bytes)));
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
                acceptObject();
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    //-------------------------------------------------------------------------
    private Object acceptObject() throws IOException {
        var next = input.readByte();
        return readObject(next);
    }

    //-----------------------------------------------------------------------
    Object readObject(byte typeByte) throws IOException {
        handleObjectStart();
        if (typeByte >= MIN_FIX_INT) {  // no need to check for b <= MAX_FIX_INT
            handleInt(typeByte);
            return typeByte;

        } else if (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) {
            return parseString(typeByte - MIN_FIX_STR);

        } else if (typeByte >= MIN_FIX_ARRAY && typeByte <= MAX_FIX_ARRAY) {
            return parseArray(typeByte - MIN_FIX_ARRAY);

        } else if (typeByte >= MIN_FIX_MAP && typeByte <= MAX_FIX_MAP) {
            return parseMap(typeByte - MIN_FIX_MAP);

        } else {
            return switch (typeByte) {
                case MAP_8 -> parseMap(input.readUnsignedByte());
                case MAP_16 -> parseMap(input.readUnsignedShort());
                case MAP_32 -> parseMap(input.readInt());
                case ARRAY_8 -> parseArray(input.readUnsignedByte());
                case ARRAY_16 -> parseArray(input.readUnsignedShort());
                case ARRAY_32 -> parseArray(input.readInt());
                case STR_8 -> parseString(input.readUnsignedByte());
                case STR_16 -> parseString(input.readUnsignedShort());
                case STR_32 -> parseString(input.readInt());
                case NULL -> parseNull();
                case FALSE -> parseBoolean(false);
                case TRUE -> parseBoolean(true);
                case UNUSED -> parseUnknown(typeByte);
                case FLOAT_32 -> parseFloat(input.readFloat());
                case DOUBLE_INT_8 -> parseDouble(input.readByte());
                case DOUBLE_64 -> parseDouble(input.readDouble());
                case CHAR_16 -> parseChar(input.readChar());
                case BYTE_8 -> parseByte(input.readByte());
                case SHORT_16 -> parseShort(input.readShort());
                case INT_16 -> parseInt(input.readShort());
                case INT_32 -> parseInt(input.readInt());
                case LONG_8 -> parseLong(input.readByte());
                case LONG_16 -> parseLong(input.readShort());
                case LONG_32 -> parseLong(input.readInt());
                case LONG_64 -> parseLong(input.readLong());
                case DATE_PACKED -> parseDatePacked();
                case DATE -> parseDate();
                case TIME -> parseTime();
                case INSTANT -> parseInstant();
                case DURATION -> parseDuration();
                case BIN_8 -> parseByteArray(input.readUnsignedByte());
                case BIN_16 -> parseByteArray(input.readUnsignedShort());
                case BIN_32 -> parseByteArray(input.readInt());
                case DOUBLE_ARRAY_8 -> parseDoubleArray(input.readUnsignedByte());
                case DOUBLE_ARRAY_16 -> parseDoubleArray(input.readUnsignedShort());
                case DOUBLE_ARRAY_32 -> parseDoubleArray(input.readInt());
                case TYPE_DEFN_8 -> parseTypeName(input.readUnsignedByte());
                case TYPE_DEFN_16 -> parseTypeName(input.readUnsignedShort());
                case TYPE_REF_8 -> parseTypeReference(input.readByte());
                case TYPE_REF_16 -> parseTypeReference(input.readUnsignedShort());
                case BEAN_DEFN -> parseBean(input.readUnsignedByte());
                case VALUE_DEFN -> parseValueDefinition();
                case VALUE_REF_8 -> parseValueReference(input.readUnsignedByte());
                case VALUE_REF_16 -> parseValueReference(input.readUnsignedShort());
                case VALUE_REF_24 -> parseValueReference((input.readUnsignedByte() << 16) + input.readUnsignedShort());
                default -> parseUnknown(typeByte);
            };
        }
    }

    //-------------------------------------------------------------------------
    private Object parseMap(int size) throws IOException {
        handleMapHeader(size);
        for (var i = 0; i < size; i++) {
            readMapKey();
            readMapValue();
        }
        handleMapFooter();
        return "<bean>";  // maps are not cached as values, so this map must actually be a bean
    }

    Object readMapKey() throws IOException {
        return acceptObject();
    }

    Object readMapValue() throws IOException {
        return acceptObject();
    }

    private Object parseArray(int size) throws IOException {
        handleArrayHeader(size);
        for (var i = 0; i < size; i++) {
            readArrayItem();
        }
        handleArrayFooter();
        return "<bean>";  // maps are not cached as values, so this map must actually be a bean
    }

    Object readArrayItem() throws IOException {
        return acceptObject();
    }

    private Object parseBean(int size) throws IOException {
        handleBeanHeader(size);
        for (var i = 0; i < size * 2; i++) {
            readBeanItem();
        }
        handleBeanFooter();
        return "<bean>";
    }

    Object readBeanItem() throws IOException {
        return acceptObject();
    }

    private Object readAnnotatedValue() throws IOException {
        return acceptObject();
    }

    //-------------------------------------------------------------------------
    private Object parseNull() throws IOException {
        handleNull();
        return null;
    }

    private Object parseBoolean(boolean value) throws IOException {
        handleBoolean(value);
        return value;
    }

    private Object parseUnknown(byte value) throws IOException {
        handleUnknown(value);
        return null;
    }

    private Object parseFloat(float value) throws IOException {
        handleFloat(value);
        return null;
    }

    private Object parseDouble(double value) throws IOException {
        handleDouble(value);
        return null;
    }

    private Object parseChar(char value) throws IOException {
        handleChar(value);
        return null;
    }

    private Object parseByte(byte value) throws IOException {
        handleByte(value);
        return value;
    }

    private Object parseShort(short value) throws IOException {
        handleShort(value);
        return value;
    }

    private Object parseInt(int value) throws IOException {
        handleInt(value);
        return value;
    }

    private Object parseLong(long value) throws IOException {
        handleLong(value);
        return value;
    }

    private String parseString(int size) throws IOException {
        if (size < 0) {
            throw new IllegalStateException("String too large");
        }
        var bytes = new byte[size];
        input.readFully(bytes);
        var str = new String(bytes, UTF_8);
        handleString(str);
        if (str.length() >= MIN_LENGTH_STR_VALUE) {
            valueDefinitions.add(str);
        }
        return str;
    }

    private LocalDate parseDatePacked() throws IOException {
        var packed = input.readUnsignedShort();
        var dom = packed & 31;
        var ym = packed >> 5;
        var date = LocalDate.of((ym / 12) + 2000, (ym % 12) + 1, dom);
        handleDate(date);
        return date;
    }

    private LocalDate parseDate() throws IOException {
        var upper = input.readInt();
        var lower = input.readUnsignedByte();
        var year = upper >> 1;
        var month = ((upper & 1) << 3) + (lower >>> 5);
        var dom = lower & 31;
        var date = LocalDate.of(year, month, dom);
        handleDate(date);
        return date;
    }

    private LocalTime parseTime() throws IOException {
        var upper = input.readUnsignedShort();
        var lower = Integer.toUnsignedLong(input.readInt());
        var nod = ((long) upper << 32) + lower;
        var time = LocalTime.ofNanoOfDay(nod);
        handleTime(time);
        return time;
    }

    private Instant parseInstant() throws IOException {
        var second = input.readLong();
        var nanos = input.readInt();
        var instant = Instant.ofEpochSecond(second, nanos);
        handleInstant(instant);
        return instant;
    }

    private Duration parseDuration() throws IOException {
        var seconds = input.readLong();
        var nanos = input.readInt();
        var duration = Duration.ofSeconds(seconds, nanos);
        handleDuration(duration);
        return duration;
    }

    //-----------------------------------------------------------------------
    private byte[] parseByteArray(int size) throws IOException {
        var bytes = new byte[size];
        input.readFully(bytes);
        handleBinary(bytes);
        return bytes;
    }

    private double[] parseDoubleArray(int size) throws IOException {
        var values = new double[size];
        for (int i = 0; i < size; i++) {
            values[i] = input.readDouble();
        }
        handleDoubleArray(values);
        return values;
    }

    //-------------------------------------------------------------------------
    private Object parseTypeName(int size) throws IOException {
        var bytes = new byte[size];
        input.readFully(bytes);
        var typeName = new String(bytes, UTF_8);
        handleTypeName(typeName);
        typeDefinitions.add(typeName);
        return readAnnotatedValue();
    }

    private Object parseTypeReference(int ref) throws IOException {
        var typeName = switch (ref) {
            case TYPE_CODE_LIST -> "List";
            case TYPE_CODE_SET -> "Set";
            case TYPE_CODE_MAP -> "Map";
            case TYPE_CODE_OPTIONAL -> "Optional";
            case TYPE_CODE_MULTISET -> "Multiset";
            case TYPE_CODE_LIST_MULTIMAP -> "ListMultimap";
            case TYPE_CODE_SET_MULTIMAP -> "SetMultimap";
            case TYPE_CODE_BIMAP -> "BiMap";
            case TYPE_CODE_TABLE -> "Table";
            case TYPE_CODE_GUAVA_OPTIONAL -> "List";
            case TYPE_CODE_GRID -> "Grid";
            case TYPE_CODE_OBJECT_ARRAY -> "Object[]";
            case TYPE_CODE_STRING_ARRAY -> "String[]";
            default -> ref >= 0 ? typeDefinitions.get(ref) : "Unknown";
        };
        handleTypeReference(ref, typeName);
        return readAnnotatedValue();
    }

    private Object parseValueDefinition() throws IOException {
        handleValueDefinition();
        var value = readAnnotatedValue();
        valueDefinitions.add(value);
        return value;
    }

    private Object parseValueReference(int ref) {
        var value = valueDefinitions.get(ref);
        handleValueReference(ref, value);
        return value;
    }

    //-------------------------------------------------------------------------
    void handleObjectStart() {
    }

    void handleMapHeader(int size) {
    }

    void handleMapFooter() {
    }

    void handleArrayHeader(int size) {
    }

    void handleArrayFooter() {
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

    void handleChar(char value) {
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

    void handleTypeReference(int ref, String typeName) throws IOException {
    }

    void handleBeanHeader(int propertyCount) throws IOException {
    }

    void handleBeanFooter() throws IOException {
    }

    void handleValueDefinition() throws IOException {
    }

    void handleValueReference(int ref, Object value) {
    }

    void handleUnknown(byte b) {
    }

}
