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
import java.io.Reader;

/**
 * Reader of JSON data.
 */
final class JsonInput {

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
     * The reader.
     */
    private final Reader input;
    /**
     * The reused string buffer.
     */
    private final StringBuilder buf = new StringBuilder(32);
    /**
     * The last parsed integral number.
     */
    private long integral;
    /**
     * The last parsed floating number.
     */
    private double floating;
    /**
     * The previously read character.
     */
    private Character cachedNext;
    /**
     * The previously read object key.
     */
    private String cachedObjectKey;

    /**
     * Creates an instance that parses JSON.
     * 
     * @param input  the input to read from, not null
     */
    JsonInput(Reader input) {
        this.input = input;
    }

    //-----------------------------------------------------------------------
    /**
     * Writes a JSON null.
     * 
     * @throws IOException if an error occurs
     */
    JsonEvent readEvent() throws IOException {
        char next = readNext();
        // whitespace
        while (next == ' ' || next == '\t' || next == '\n' || next == '\r') {
            next = readNext();
        }
        // identify token
        switch (next) {
            case '{':
                return JsonEvent.OBJECT;
            case '}':
                return JsonEvent.OBJECT_END;
            case '[':
                return JsonEvent.ARRAY;
            case ']':
                return JsonEvent.ARRAY_END;
            case '"':
                return JsonEvent.STRING;
            case '-':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return acceptNumber(next);
            case 'n':
                return acceptNull();
            case 't':
                return acceptTrue();
            case 'f':
                return acceptFalse();
            case ',':
                return JsonEvent.COMMA;
            case ':':
                return JsonEvent.COLON;
            default:
                throw new IllegalArgumentException("Invalid JSON data: Expected JSON character but found '" + next + "'");
        }
    }

    // store peeked value for later use
    void pushBack(char ch) throws IOException {
        cachedNext = ch;
    }

    // store peeked value for later use
    void pushBackObjectKey(String objectKey) throws IOException {
        cachedObjectKey = objectKey;
    }

    JsonEvent ensureEvent(JsonEvent event, JsonEvent expected) throws IOException {
        if (event != expected) {
            throw new IllegalArgumentException("Invalid JSON data: Expected " + expected + " but found " + event);
        }
        return event;
    }

    JsonEvent acceptEvent(JsonEvent expected) throws IOException {
        return ensureEvent(readEvent(), expected);
    }

    //-----------------------------------------------------------------------
    // expect object key and parse it
    String acceptObjectKey(JsonEvent event) throws IOException {
        ensureEvent(event, JsonEvent.STRING);
        return parseObjectKey();
    }

    // opening quite already consumed
    String parseObjectKey() throws IOException {
        if (cachedObjectKey != null) {
            String key = cachedObjectKey;
            cachedObjectKey = null;
            return key;
        }
        String str = parseString();
        acceptEvent(JsonEvent.COLON);
        return str;
    }

    //-----------------------------------------------------------------------
    // expect string and parse it
    String acceptString() throws IOException {
        acceptEvent(JsonEvent.STRING);
        return parseString();
    }

    // opening quite already consumed
    String parseString() throws IOException {
        buf.setLength(0);
        char next = readNext();
        while (next != '"') {
            if (next == '\\') {
                parseEscape();
            } else {
                buf.append(next);
            }
            next = readNext();
        }
        return buf.toString();
    }

    private void parseEscape() throws IOException {
        char next = readNext();
        switch (next) {
            case '"':
                buf.append('"');
                return;
            case '/':
                buf.append('/');
                return;
            case '\\':
                buf.append('\\');
                return;
            case 'b':
                buf.append('\b');
                return;
            case 'f':
                buf.append('\f');
                return;
            case 'n':
                buf.append('\n');
                return;
            case 'r':
                buf.append('\r');
                return;
            case 't':
                buf.append('\t');
                return;
            case 'u':
                int total = 0;
                for (int i = 0; i < 4; i++) {
                    total = total * 16 + acceptHex();
                }
                buf.append((char) total);
                return;
            default:
                throw new IllegalArgumentException("Invalid JSON data: Expected valid escape sequence but found '\\" + next + "'");
        }
    }

    private int acceptHex() throws IOException {
        char next = readNext();
        if (next >= '0' && next <= '9') {
            return next - 48;
        }
        if (next >= 'a' && next <= 'f') {
            return next - 97 + 10;
        }
        if (next >= 'A' && next <= 'F') {
            return next - 65 + 10;
        }
        throw new IllegalArgumentException("Invalid JSON data: Expected hex but found '" + next + "'");
    }

    //-----------------------------------------------------------------------
    // number already parsed
    long parseNumberIntegral() {
        return integral;
    }

    // number already parsed
    double parseNumberFloating() {
        return floating;
    }

    private JsonEvent acceptNumber(char first) throws IOException {
        buf.setLength(0);
        buf.append(first);
        char last = first;
        char next = readNext();
        while ((next >= '0' && next <= '9') || next == '.' || next == '-' || next == '+' || next == 'e' || next == 'E') {
            buf.append((char) next);
            last = next;
            next = readNext();
        }
        pushBack(next);
        if (last < '0' || last > '9') {
            throw new IllegalArgumentException("Invalid JSON data: Expected number but found invalid last char '" + last + "'");
        }
        String str = buf.toString();
        if (str.equals("0")) {
            integral = 0;
            return JsonEvent.NUMBER_INTEGRAL;
        } else if (str.startsWith("0") && str.charAt(1) != '.') {
            throw new IllegalArgumentException("Invalid JSON data: Expected number but found zero at start");
        } else if (str.contains(".") || str.contains("e") || str.contains("E")) {
            floating = Double.parseDouble(str);
            return JsonEvent.NUMBER_FLOATING;
        } else {
            integral = Long.parseLong(str);
            return JsonEvent.NUMBER_INTEGRAL;
        }
    }

    //-----------------------------------------------------------------------
    private JsonEvent acceptNull() throws IOException {
        acceptChar('u');
        acceptChar('l');
        acceptChar('l');
        return JsonEvent.NULL;
    }

    private JsonEvent acceptTrue() throws IOException {
        acceptChar('r');
        acceptChar('u');
        acceptChar('e');
        return JsonEvent.TRUE;
    }

    private JsonEvent acceptFalse() throws IOException {
        acceptChar('a');
        acceptChar('l');
        acceptChar('s');
        acceptChar('e');
        return JsonEvent.FALSE;
    }

    private void acceptChar(char ch) throws IOException {
        char next = readNext();
        if (next != ch) {
            throw new IllegalArgumentException("Invalid JSON data: Expected '" + ch + "' but found '" + next + "'");
        }
    }

    //-----------------------------------------------------------------------
    private char readNext() throws IOException {
        if (cachedNext != null) {
            char next = cachedNext.charValue();
            cachedNext = null;
            return next;
        }
        int next = input.read();
        if (next == -1) {
            throw new IllegalArgumentException("Invalid JSON data: End of file");
        }
        return (char) next;
    }

    void skipData() throws IOException {
        skipData(readEvent());
    }

    private void skipData(JsonEvent event) throws IOException {
        switch (event) {
            case OBJECT:
                event = readEvent();
                while (event != JsonEvent.OBJECT_END) {
                    acceptObjectKey(event);
                    skipData();
                    event = acceptObjectSeparator();
                }
                break;
            case ARRAY:
                event = readEvent();
                while (event != JsonEvent.ARRAY_END) {
                    skipData(event);
                    event = acceptArraySeparator();
                }
                break;
            case STRING:
                parseString();
                break;
            case NULL:
            case TRUE:
            case FALSE:
            case NUMBER_FLOATING:
            case NUMBER_INTEGRAL:
                break;
            default:
                throw new IllegalArgumentException("Invalid JSON data: Expected data item but found " + event);
        }
    }

    //-----------------------------------------------------------------------
    // accepts a comma or object end
    JsonEvent acceptObjectSeparator() throws IOException {
        JsonEvent event = readEvent();
        if (event == JsonEvent.COMMA) {
            return readEvent();  // leniently allow comma before objectEnd
        } else {
            return ensureEvent(event, JsonEvent.OBJECT_END);
        }
    }

    // accepts a comma or array end
    JsonEvent acceptArraySeparator() throws IOException {
        JsonEvent event = readEvent();
        if (event == JsonEvent.COMMA) {
            return readEvent();  // leniently allow comma before arrayEnd
        } else {
            return ensureEvent(event, JsonEvent.ARRAY_END);
        }
    }

}
