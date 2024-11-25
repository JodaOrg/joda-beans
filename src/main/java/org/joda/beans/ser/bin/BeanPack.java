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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Constants used in MsgPack binary serialization.
 * <p>
 * This uses the v2.0 specification of MsgPack as of 2014-01-29.
 */
abstract class BeanPack {

    /**
     * UTF-8 encoding.
     */
    static final Charset UTF_8 = StandardCharsets.UTF_8;

    // fixed-size numbers
    /**
     * Maximum fixed int (7 bits).
     */
    static final int MAX_FIX_INT = 0x7F;
    /**
     * Minimum fixed int (4 bits).
     */
    static final int MIN_FIX_INT = 0xFFFFFFF0;

    // maps
    /**
     * Min fixed map - up to length 12.
     */
    static final int MIN_FIX_MAP = 0xFFFFFF80;
    /**
     * Max fixed map.
     */
    static final int MAX_FIX_MAP = 0xFFFFFF8C;
    /**
     * Map - followed by the size as an unsigned byte.
     */
    static final int MAP_8 = 0xFFFFFF8D;
    /**
     * Map - followed by the size as an unsigned short.
     */
    static final int MAP_16 = 0xFFFFFFBE;
    /**
     * Map - followed by the size as an unsigned long.
     */
    static final int MAP_32 = 0xFFFFFFBF;

    // arrays
    /**
     * Min fixed array - up to length 12.
     */
    static final int MIN_FIX_ARRAY = 0xFFFFFF90;
    /**
     * Max fixed array.
     */
    static final int MAX_FIX_ARRAY = 0xFFFFFF9C;
    /**
     * Array - followed by the size as an unsigned byte.
     */
    static final int ARRAY_8 = 0xFFFFFF9D;
    /**
     * Array - followed by the size as an unsigned short.
     */
    static final int ARRAY_16 = 0xFFFFFF9E;
    /**
     * Array - followed by the size as an unsigned long.
     */
    static final int ARRAY_32 = 0xFFFFFF9F;

    // strings
    /**
     * Min fixed string - up to length 45.
     */
    static final int MIN_FIX_STR = 0xFFFFFFA0;
    /**
     * Max fixed string.
     */
    static final int MAX_FIX_STR = 0xFFFFFFCC;
    /**
     * String - followed by the size as an unsigned byte.
     */
    static final int STR_8 = 0xFFFFFFCD;
    /**
     * String - followed by the size as an unsigned short.
     */
    static final int STR_16 = 0xFFFFFFCE;
    /**
     * String - followed by the size as an unsigned int.
     */
    static final int STR_32 = 0xFFFFFFCF;

    // primitives
    /**
     * Null.
     */
    static final int NULL = 0xFFFFFFD0;
    /**
     * False.
     */
    static final int FALSE = 0xFFFFFFD1;
    /**
     * True.
     */
    static final int TRUE = 0xFFFFFFD2;

    // date/time
    /**
     * LocalDate (2 bytes) - packed format from year 2000 to 2170, 11 bits for year-month from 2000, 5 bits for day-of-month.
     */
    static final int DATE_PACKED = 0xFFFFFFD3;
    /**
     * LocalDate (6 bytes) - int year, byte month, byte day-of-month.
     */
    static final int DATE = 0xFFFFFFD4;
    /**
     * LocalTime - nano-of-day (6 bytes).
     */
    static final int TIME = 0xFFFFFFD5;
    /**
     * Instant - 12 bytes, 8 for the seconds and 4 for the nanoseconds.
     */
    static final int INSTANT = 0xFFFFFFD6;
    /**
     * Duration - 12 bytes, 8 for the seconds and 4 for the nanoseconds.
     */
    static final int DURATION = 0xFFFFFFD7;
    // D8 and D9 are free at present

    // byte[]/double[]
    /**
     * byte[] - followed by the size as an unsigned short.
     */
    static final int BIN_8 = 0xFFFFFFDA;
    /**
     * byte[] - followed by the size as an unsigned short.
     */
    static final int BIN_16 = 0xFFFFFFDB;
    /**
     * byte[] - followed by the size as an unsigned int.
     */
    static final int BIN_32 = 0xFFFFFFDC;
    /**
     * double[] - followed by the size as an unsigned byte.
     */
    static final int DOUBLE_ARRAY_8 = 0xFFFFFFDD;
    /**
     * double[] - followed by the size as an unsigned short.
     */
    static final int DOUBLE_ARRAY_16 = 0xFFFFFFDE;
    /**
     * double[] - followed by the size as an unsigned int.
     */
    static final int DOUBLE_ARRAY_32 = 0xFFFFFFDF;

    // beans and references
    /**
     * Bean with full definition - followed by the string type name and null terminated array of property names and values.
     */
    static final int BEAN_DEFN = 0xFFFFFFE0;
    /**
     * Reference to a bean - followed by an int reference to the bean definition and array of property values.
     */
    static final int BEAN_REF = 0xFFFFFFE1;
    /**
     * Type name - followed by the string type name and the actual value.
     */
    static final int TYPE_NAME = 0xFFFFFFE2;
    /**
     * Reference to a type name - followed by an int reference and the actual value.
     */
    static final int TYPE_REF = 0xFFFFFFE3;
    /**
     * Reference to a previous string value - followed by an int reference.
     */
    static final int VALUE_REF = 0xFFFFFFE4;

    // numbers
    /**
     * Float - 4 bytes.
     */
    static final int FLOAT_32 = 0xFFFFFFE5;
    /**
     * Double - 8 bytes.
     */
    static final int DOUBLE_64 = 0xFFFFFFE6;
    /**
     * Long (signed) - up to 8 bytes.
     */
    static final int BYTE_8 = 0xFFFFFFE7;
    /**
     * Long (signed) - up to 8 bytes.
     */
    static final int SHORT_16 = 0xFFFFFFE8;
    /**
     * Int (signed) - up to 1 byte.
     */
    static final int INT_8 = 0xFFFFFFE9;
    /**
     * Int (signed) - up to 2 bytes.
     */
    static final int INT_16 = 0xFFFFFFEA;
    /**
     * Int (signed) - up to 4 bytes.
     */
    static final int INT_32 = 0xFFFFFFEB;
    /**
     * Long (signed) - up to 1 byte.
     */
    static final int LONG_8 = 0xFFFFFFEC;
    /**
     * Long (signed) - up to 2 bytes.
     */
    static final int LONG_16 = 0xFFFFFFED;
    /**
     * Long (signed) - up to 4 bytes.
     */
    static final int LONG_32 = 0xFFFFFFEE;
    /**
     * Long (signed) - up to 8 bytes.
     */
    static final int LONG_64 = 0xFFFFFFEF;

    //-------------------------------------------------------------------------
    /**
     * Set type code, followed by an array of values.
     */
    static final int TYPE_CODE_LIST = -1;
    /**
     * Set type code, followed by an array of values.
     */
    static final int TYPE_CODE_SET = -2;
    /**
     * Set type code, followed by an array of values.
     */
    static final int TYPE_CODE_MAP = -3;
    /**
     * Optional type code, followed by a value, where a null value means empty.
     */
    static final int TYPE_CODE_OPTIONAL = -4;
    /**
     * Multimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_LIST_MULTIMAP = -5;
    /**
     * SetMultimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_SET_MULTIMAP = -6;
    /**
     * Bimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_BIMAP = -7;
    /**
     * Multiset type code, followed by a map of values.
     */
    static final int TYPE_CODE_MULTISET = -8;
    /**
     * Table type code.
     */
    static final int TYPE_CODE_TABLE = -9;
    /**
     * Grid type code.
     */
    static final int TYPE_CODE_GRID = -10;
    /**
     * Object[] type code, followed by an array of values.
     */
    static final int TYPE_CODE_OBJECT_ARRAY = -11;
    /**
     * String[] type code, followed by an array of values.
     */
    static final int TYPE_CODE_STRING_ARRAY = -12;

    //-----------------------------------------------------------------------
    /**
     * Converts a byte to a hex string for debugging.
     * 
     * @param b  the byte
     * @return the two character hex equivalent, not null
     */
    static String toHex(int b) {
        return String.format("%02X", (byte) b);
    }

    static boolean isMap(int typeByte) {
        return (typeByte & MIN_FIX_MAP) == MIN_FIX_MAP;
    }

    static boolean isArray(int typeByte) {
        return (typeByte & MIN_FIX_ARRAY) == MIN_FIX_ARRAY;
    }

    static boolean isString(int typeByte) {
        return typeByte >= MIN_FIX_STR && typeByte <= STR_32;
    }

    static boolean isIntegral(int typeByte) {
        return (typeByte >= FLOAT_32 && typeByte <= MAX_FIX_INT);
    }

}
