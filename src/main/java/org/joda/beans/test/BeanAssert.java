/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
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
            throw new BeanComparisonError(message, expected, actual);
        }
    }

}
