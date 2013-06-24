/*
 *  Copyright 2001-2013 Stephen Colebourne
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
package org.joda.beans.test;

import org.joda.beans.Bean;

/**
 * Assertion class to compare beans.
 * <p>
 * This class fulfils a similar role to other assertion libraries in testing code.
 * It should generally be statically imported.
 */
public final class BeanAssert {

    /**
     * Restricted constructor.
     */
    private BeanAssert() {
    }

    //-----------------------------------------------------------------------
    /**
     * Asserts that two beans are equal, providing a better error message.
     * 
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEquals(final Bean expected, final Bean actual) {
        assertBeanEquals(null, expected, actual);
    }

    /**
     * Asserts that two beans are equal, providing a better error message.
     * 
     * @param message  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEquals(final String message, final Bean expected, final Bean actual) {
        if (expected == null) {
            throw new AssertionError(message + ": Expected bean must not be null");
        }
        if (actual == null) {
            throw new AssertionError(message + ": Actual bean must not be null");
        }
        if (expected.equals(actual) == false) {
            throw new BeanComparisonError(message, 10, expected, actual);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Asserts that two beans are equal, providing a better error message.
     * 
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEqualsFullDetail(final Bean expected, final Bean actual) {
        assertBeanEqualsFullDetail(null, expected, actual);
    }

    /**
     * Asserts that two beans are equal, providing a better error message, with
     * an unlimited number of errors reported.
     * 
     * @param message  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEqualsFullDetail(final String message, final Bean expected, final Bean actual) {
        if (expected == null) {
            throw new AssertionError(message + ": Expected bean must not be null");
        }
        if (actual == null) {
            throw new AssertionError(message + ": Actual bean must not be null");
        }
        if (expected.equals(actual) == false) {
            throw new BeanComparisonError(message, Integer.MAX_VALUE, expected, actual);
        }
    }

}
