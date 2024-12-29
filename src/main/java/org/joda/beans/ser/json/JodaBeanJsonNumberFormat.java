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

/**
 * Provides control over the format of unusual JSON numeric values.
 * 
 * @since 3.0.0
 */
public enum JodaBeanJsonNumberFormat {

    /**
     * Format using {@code NAN_AS_NULL} when using simple JSON, and {@code STRINGS} when using normal JSON.
     */
    COMPATIBLE_V2,
    /**
     * Format NaN as {@code null} and Infinity as a string.
     * <p>
     * The value will be sent as literal {@code null}, the string {@code "Infinity"} or the string {@code "-Infinity"}.
     */
    NAN_AS_NULL,
    /**
     * Format NaN and Infinity as a string.
     * <p>
     * The value will be sent as the string {@code "NaN"}, {@code "Infinity"} or {@code "-Infinity"}.
     */
    STRING,
    /**
     * Format NaN and Infinity as a literal, as per 'The JSON5 Data Interchange Format'.
     * <p>
     * The value will be sent as the literal {@code NaN}, {@code Infinity} or {@code -Infinity}.
     */
    LITERAL;

}
