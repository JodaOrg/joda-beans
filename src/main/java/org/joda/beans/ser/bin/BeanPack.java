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
    /**
     * The smallest length of string that is cached.
     */
    static final int MIN_LENGTH_STR_VALUE = 3;

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
    static final int MAP_16 = 0xFFFFFF8E;
    /**
     * Map - followed by the size as an unsigned long.
     */
    static final int MAP_32 = 0xFFFFFF8F;

    // arrays
    /**
     * Min fixed array - up to length 12.
     */
    static final int MIN_FIX_ARRAY = 0xFFFFFF90;  // must be same as MsgPack
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
     * Min fixed string - up to length 40.
     */
    static final int MIN_FIX_STR = 0xFFFFFFA0;
    /**
     * Max fixed string.
     */
    static final int MAX_FIX_STR = 0xFFFFFFC8;
    /**
     * String - followed by the size as an unsigned byte.
     */
    static final int STR_8 = 0xFFFFFFC9;
    /**
     * String - followed by the size as an unsigned short.
     */
    static final int STR_16 = 0xFFFFFFCA;
    /**
     * String - followed by the size as an unsigned int.
     */
    static final int STR_32 = 0xFFFFFFCB;

    // primitives
    /**
     * Null.
     */
    static final int NULL = 0xFFFFFFCC;
    /**
     * False.
     */
    static final int FALSE = 0xFFFFFFCD;
    /**
     * True.
     */
    static final int TRUE = 0xFFFFFFCE;
    /**
     * Unused.
     */
    static final int UNUSED = 0xFFFFFFCF;

    // numbers
    /**
     * Float - followed by 4 bytes.
     */
    static final int FLOAT_32 = 0xFFFFFFD0;
    /**
     * Double in 1-byte int format - followed by 1 byte signed int
     */
    static final int DOUBLE_INT_8 = 0xFFFFFFD1;
    /**
     * Double - followed by 8 bytes.
     */
    static final int DOUBLE_64 = 0xFFFFFFD2;
    /**
     * Char (unsigned) - followed by 2 bytes.
     */
    static final int CHAR_16 = 0xFFFFFFD3;
    /**
     * Byte (signed) - followed by 1 byte.
     */
    static final int BYTE_8 = 0xFFFFFFD4;
    /**
     * Short (signed) - followed by 2 bytes.
     */
    static final int SHORT_16 = 0xFFFFFFD5;
    /**
     * Int (signed) - followed by 2 bytes.
     */
    static final int INT_16 = 0xFFFFFFD6;
    /**
     * Int (signed) - followed by 4 bytes.
     */
    static final int INT_32 = 0xFFFFFFD7;
    /**
     * Long (signed) - followed by 1 byte.
     */
    static final int LONG_8 = 0xFFFFFFD8;
    /**
     * Long (signed) - followed by 2 bytes.
     */
    static final int LONG_16 = 0xFFFFFFD9;
    /**
     * Long (signed) - followed by 4 bytes.
     */
    static final int LONG_32 = 0xFFFFFFDA;
    /**
     * Long (signed) - followed by 8 bytes.
     */
    static final int LONG_64 = 0xFFFFFFDB;

    // date/time
    /**
     * LocalDate (2 bytes) - packed format from year 2000 to 2169 inclusive,
     * 11 bits for year-month from 2000, 5 bits for 1-based day-of-month.
     */
    static final int DATE_PACKED = 0xFFFFFFDC;
    /**
     * LocalDate (5 bytes) - 27 bits for year-month, 5 bits for 1-based day-of-month.
     */
    static final int DATE = 0xFFFFFFDD;
    /**
     * LocalTime (6 bytes) - 6 byte nano-of-day.
     */
    static final int TIME = 0xFFFFFFDE;
    /**
     * Instant (12 bytes) - 8 for the seconds and 4 for the nanoseconds.
     */
    static final int INSTANT = 0xFFFFFFDF;
    /**
     * Duration (12 bytes) - 8 for the seconds and 4 for the nanoseconds.
     */
    static final int DURATION = 0xFFFFFFE0;

    // byte[]/double[]
    /**
     * byte[] - followed by the size as an unsigned short.
     */
    static final int BIN_8 = 0xFFFFFFE1;
    /**
     * byte[] - followed by the size as an unsigned short.
     */
    static final int BIN_16 = 0xFFFFFFE2;
    /**
     * byte[] - followed by the size as an unsigned int.
     */
    static final int BIN_32 = 0xFFFFFFE3;
    /**
     * double[] - followed by the size as an unsigned byte.
     */
    static final int DOUBLE_ARRAY_8 = 0xFFFFFFE4;
    /**
     * double[] - followed by the size as an unsigned short.
     */
    static final int DOUBLE_ARRAY_16 = 0xFFFFFFE5;
    /**
     * double[] - followed by the size as an unsigned int.
     */
    static final int DOUBLE_ARRAY_32 = 0xFFFFFFE6;

    // types and references
    /**
     * Type definition - followed by a 1 byte length, UTF-8 string and the actual value.
     */
    static final int TYPE_DEFN_8 = 0xFFFFFFE7;
    /**
     * Type definition - followed by a 2 byte length, UTF-8 string and the actual value.
     */
    static final int TYPE_DEFN_16 = 0xFFFFFFE8;
    /**
     * Reference to a type name - followed by a 1 byte int and the actual value.
     */
    static final int TYPE_REF_8 = 0xFFFFFFE9;
    /**
     * Reference to a type name - followed by a 2 byte int and the actual value.
     */
    static final int TYPE_REF_16 = 0xFFFFFFEA;
    /**
     * Bean with full definition - followed by a 1 byte int count of properties, then each property name,
     * then each property value, nulls replacing non-serialized entries.
     * Beans with 256 or more properties are not recorded as bean definitions.
     */
    static final int BEAN_DEFN = 0xFFFFFFEB;
    /**
     * Value with full definition - followed by the value.
     */
    static final int VALUE_DEFN = 0xFFFFFFEC;
    /**
     * Reference to a previous value - followed by a 1 byte int and the actual value.
     */
    static final int VALUE_REF_8 = 0xFFFFFFED;
    /**
     * Reference to a previous value - followed by a 2 byte int and the actual value.
     */
    static final int VALUE_REF_16 = 0xFFFFFFEE;
    /**
     * Reference to a previous value - followed by a 3 byte int and the actual value.
     */
    static final int VALUE_REF_24 = 0xFFFFFFEF;

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
     * Multiset type code, followed by a map of values.
     */
    static final int TYPE_CODE_MULTISET = -5;
    /**
     * Multimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_LIST_MULTIMAP = -6;
    /**
     * SetMultimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_SET_MULTIMAP = -7;
    /**
     * Bimap type code, followed by a map of values.
     */
    static final int TYPE_CODE_BIMAP = -8;
    /**
     * Table type code.
     */
    static final int TYPE_CODE_TABLE = -9;
    /**
     * Optional (Guava) type code, followed by a value, where a null value means empty.
     */
    static final int TYPE_CODE_GUAVA_OPTIONAL = -10;
    /**
     * Grid type code.
     */
    static final int TYPE_CODE_GRID = -11;
    /**
     * Object[] type code, followed by an array of values.
     */
    static final int TYPE_CODE_OBJECT_ARRAY = -12;
    /**
     * String[] type code, followed by an array of values.
     */
    static final int TYPE_CODE_STRING_ARRAY = -13;

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
}
