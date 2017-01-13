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
import java.nio.charset.Charset;

/**
 * Constants used in MsgPack binary serialization.
 * <p>
 * This uses the v2.0 specification of MsgPack as of 2014-01-29.
 *
 * @author Stephen Colebourne
 */
abstract class MsgPack {

    /**
     * UTF-8 encoding.
     */
    static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * Maximum fixed int.
     */
    static final int MAX_FIX_INT = 0x7F;
    /**
     * Minimum fixed int.
     */
    static final int MIN_FIX_INT = 0xFFFFFFE0;
    /**
     * Min fixed map - up to length 15.
     */
    static final int MIN_FIX_MAP = 0xFFFFFF80;
    /**
     * Max fixed map.
     */
    static final int MAX_FIX_MAP = 0xFFFFFF8F;
    /**
     * Min fixed array - up to length 15.
     */
    static final int MIN_FIX_ARRAY = 0xFFFFFF90;
    /**
     * Max fixed array.
     */
    static final int MAX_FIX_ARRAY = 0xFFFFFF9F;
    /**
     * Min fixed string - up to length 31.
     */
    static final int MIN_FIX_STR = 0xFFFFFFA0;
    /**
     * Max fixed string.
     */
    static final int MAX_FIX_STR = 0xFFFFFFBF;
    /**
     * Nil.
     */
    static final int NIL = 0xFFFFFFC0;
    /**
     * False.
     */
    static final int FALSE = 0xFFFFFFC2;
    /**
     * True.
     */
    static final int TRUE = 0xFFFFFFC3;
    /**
     * Binary - up to length 255.
     */
    static final int BIN_8 = 0xFFFFFFC4;
    /**
     * Binary - up to length (2^16)-1.
     */
    static final int BIN_16 = 0xFFFFFFC5;
    /**
     * Binary - up to length (2^32)-1.
     */
    static final int BIN_32 = 0xFFFFFFC6;
    /**
     * Extension - up to length 255.
     */
    static final int EXT_8 = 0xFFFFFFC7;
    /**
     * Extension - up to length (2^16)-1.
     */
    static final int EXT_16 = 0xFFFFFFC8;
    /**
     * Extension - up to length (2^32)-1.
     */
    static final int EXT_32 = 0xFFFFFFC9;
    /**
     * Float - 4 bytes.
     */
    static final int FLOAT_32 = 0xFFFFFFCA;
    /**
     * Double - 8 bytes.
     */
    static final int FLOAT_64 = 0xFFFFFFCB;
    /**
     * Int (unsigned) - up to 1 byte.
     */
    static final int UINT_8 = 0xFFFFFFCC;
    /**
     * Int (unsigned) - up to 2 bytes.
     */
    static final int UINT_16 = 0xFFFFFFCD;
    /**
     * Int (unsigned) - up to 4 bytes.
     */
    static final int UINT_32 = 0xFFFFFFCE;
    /**
     * Int (unsigned) - up to 8 bytes.
     */
    static final int UINT_64 = 0xFFFFFFCF;
    /**
     * Int (signed) - up to 1 byte.
     */
    static final int SINT_8 = 0xFFFFFFD0;
    /**
     * Int (signed) - up to 2 bytes.
     */
    static final int SINT_16 = 0xFFFFFFD1;
    /**
     * Int (signed) - up to 4 bytes.
     */
    static final int SINT_32 = 0xFFFFFFD2;
    /**
     * Int (signed) - up to 8 bytes.
     */
    static final int SINT_64 = 0xFFFFFFD3;
    /**
     * Fixed extension - 1 byte.
     */
    static final int FIX_EXT_1 = 0xFFFFFFD4;
    /**
     * Fixed extension - 2 bytes.
     */
    static final int FIX_EXT_2 = 0xFFFFFFD5;
    /**
     * Fixed extension - 4 bytes.
     */
    static final int FIX_EXT_4 = 0xFFFFFFD6;
    /**
     * Fixed extension - 8 bytes.
     */
    static final int FIX_EXT_8 = 0xFFFFFFD7;
    /**
     * Fixed extension - 16 bytes.
     */
    static final int FIX_EXT_16 = 0xFFFFFFD8;
    /**
     * String - up to length 255.
     */
    static final int STR_8 = 0xFFFFFFD9;
    /**
     * String - up to length (2^16)-1.
     */
    static final int STR_16 = 0xFFFFFFDA;
    /**
     * String - up to length (2^32)-1.
     */
    static final int STR_32 = 0xFFFFFFDB;
    /**
     * Array - up to length (2^16)-1.
     */
    static final int ARRAY_16 = 0xFFFFFFDC;
    /**
     * Array - up to length (2^32)-1.
     */
    static final int ARRAY_32 = 0xFFFFFFDD;
    /**
     * Map - up to length (2^16)-1.
     */
    static final int MAP_16 = 0xFFFFFFDE;
    /**
     * Map - up to length (2^32)-1.
     */
    static final int MAP_32 = 0xFFFFFFDF;

    /**
     * Extension type code for a Joda-Bean bean-type.
     */
    static final int JODA_TYPE_BEAN = 32;
    /**
     * Extension type code for a Joda-Bean simple-type.
     */
    static final int JODA_TYPE_DATA = 33;
    /**
     * Extension type code for a Joda-Bean meta-type.
     */
    static final int JODA_TYPE_META = 34;

    //-----------------------------------------------------------------------
    /**
     * Converts a byte to a hex string for debugging.
     * 
     * @param b  the byte
     * @return the two character hex equivalent, not null
     */
    protected static String toHex(int b) {
        return String.format("%02X", (byte) b);
    }

    protected static boolean isMap(int typeByte) throws IOException {
        return (typeByte >= MIN_FIX_MAP && typeByte <= MAX_FIX_MAP) || typeByte == MAP_16 || typeByte == MAP_32;
    }

    protected static boolean isArray(int typeByte) throws IOException {
        return (typeByte >= MIN_FIX_ARRAY && typeByte <= MAX_FIX_ARRAY) || typeByte == ARRAY_16 || typeByte == ARRAY_32;
    }

    protected static boolean isString(int typeByte) throws IOException {
        return (typeByte >= MIN_FIX_STR && typeByte <= MAX_FIX_STR) || typeByte == STR_8 || typeByte == STR_16 || typeByte == STR_32;
    }

    protected static boolean isIntegral(int typeByte) throws IOException {
        return (typeByte >= MIN_FIX_INT && typeByte <= MAX_FIX_INT) ||
                typeByte == UINT_8 || typeByte == UINT_16 || typeByte == UINT_32 || typeByte == UINT_64 ||
                typeByte == SINT_8 || typeByte == SINT_16 || typeByte == SINT_32 || typeByte == SINT_64;
    }

}
