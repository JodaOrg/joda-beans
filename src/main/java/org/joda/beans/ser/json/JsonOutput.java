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
package org.joda.beans.ser.json;

import java.io.IOException;
import java.util.BitSet;

/**
 * Outputter for JSON data.
 */
final class JsonOutput {

    /** encoding JSON */
    private static final String[] REPLACE = new String[128];
    static {
        for (int i = 0; i < 32; i++) {
            REPLACE[i] = String.format("\\u%04x", i);
        }
        REPLACE['\b'] = "\\b";
        REPLACE['\t'] = "\\t";
        REPLACE['\n'] = "\\n";
        REPLACE['\f'] = "\\f";
        REPLACE['\r'] = "\\r";
        REPLACE['"'] = "\\\"";
        REPLACE['\\'] = "\\\\";
        REPLACE[127] = "\\u007f";
    }

    /**
     * The appender to write to.
     */
    private final Appendable output;
    /**
     * The indent amount.
     */
    private final String indent;
    /**
     * The new line.
     */
    private final String newLine;
    /**
     * The current indent.
     */
    private String currentIndent = "";
    /**
     * The comma depth.
     */
    private int commaDepth;
    /**
     * The comma state.
     */
    private BitSet commaState = new BitSet(64);

    /**
     * Creates an instance that outputs in compact format.
     * 
     * @param output  the output to write to, not null
     */
    JsonOutput(Appendable output) {
        this(output, "", "");
    }

    /**
     * Creates an instance where the output format can be controlled.
     * 
     * @param output  the output to write to, not null
     * @param indent  the pretty format indent
     * @param newLine  the pretty format new line
     */
    JsonOutput(Appendable output, String indent, String newLine) {
        this.output = output;
        this.indent = indent;
        this.newLine = newLine;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON null.
     * 
     * @throws IOException if an error occurs
     */
    void writeNull() throws IOException {
        output.append("null");
    }

    /**
     * Writes a JSON boolean.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeBoolean(boolean value) throws IOException {
        if (value) {
            output.append("true");
        } else {
            output.append("false");
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON int.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeInt(int value) throws IOException {
        if ((value & 0xfffffff8) == 0) {
            output.append((char) (value + 48));
        } else {
            output.append(Integer.toString(value));
        }
    }

    /**
     * Writes a JSON long.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeLong(long value) throws IOException {
        output.append(Long.toString(value));
    }

    /**
     * Writes a JSON float.
     * <p>
     * This outputs the values of NaN, and Infinity as strings.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeFloat(float value) throws IOException {
        if (Float.isNaN(value) || Float.isInfinite(value)) {
            output.append('"').append(Float.toString(value)).append('"');
        } else {
            output.append(Float.toString(value));
        }
    }

    /**
     * Writes a JSON double.
     * <p>
     * This outputs the values of NaN, and Infinity as strings.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeDouble(double value) throws IOException {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            output.append('"').append(Double.toString(value)).append('"');
        } else {
            output.append(Double.toString(value));
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON string.
     * 
     * @param value  the value
     * @throws IOException if an error occurs
     */
    void writeString(String value) throws IOException {
        output.append('"');
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch < 128) {
                String replace = REPLACE[ch];
                if (replace != null) {
                    output.append(replace);
                } else {
                    output.append(ch);
                }
            } else if (ch == '\u2028') {
                output.append("\\u2028");  // match other JSON writers
            } else if (ch == '\u2029') {
                output.append("\\u2029");  // match other JSON writers
            } else {
                output.append(ch);
            }
        }
        output.append('"');
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON array start.
     * 
     * @throws IOException if an error occurs
     */
    void writeArrayStart() throws IOException {
        output.append('[');
        commaDepth++;
        commaState.clear(commaDepth);
    }

    /**
     * Writes a JSON array item start.
     * 
     * @throws IOException if an error occurs
     */
    void writeArrayItemStart() throws IOException {
        if (commaState.get(commaDepth)) {
            output.append(',');
            if (newLine.length() > 0) {
                output.append(' ');
            }
        } else {
            commaState.set(commaDepth);
        }
    }

    /**
     * Writes a JSON array end.
     * 
     * @throws IOException if an error occurs
     */
    void writeArrayEnd() throws IOException {
        output.append(']');
        commaDepth--;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON object start.
     * 
     * @throws IOException if an error occurs
     */
    void writeObjectStart() throws IOException {
        output.append('{');
        currentIndent = currentIndent + indent;
        commaDepth++;
        commaState.set(commaDepth, false);
    }

    /**
     * Writes a JSON object key.
     * <p>
     * This handles the comma, string encoded key and separator colon.
     * 
     * @param key  the item key
     * @throws IOException if an error occurs
     */
    void writeObjectKey(String key) throws IOException {
        if (commaState.get(commaDepth)) {
            output.append(',');
        } else {
            commaState.set(commaDepth, true);
        }
        output.append(newLine);
        output.append(currentIndent);
        writeString(key);
        output.append(':');
        if (newLine.length() > 0) {
            output.append(' ');
        }
    }

    /**
     * Writes a JSON object key and value.
     * 
     * @param key  the item key
     * @param value  the item value
     * @throws IOException if an error occurs
     */
    void writeObjectKeyValue(String key, String value) throws IOException {
        writeObjectKey(key);
        writeString(value);
    }

    /**
     * Writes a JSON object end.
     * 
     * @throws IOException if an error occurs
     */
    void writeObjectEnd() throws IOException {
        currentIndent = currentIndent.substring(0, currentIndent.length() - indent.length());
        if (commaState.get(commaDepth)) {
            output.append(newLine);
            output.append(currentIndent);
        }
        output.append('}');
        commaDepth--;
    }

}
