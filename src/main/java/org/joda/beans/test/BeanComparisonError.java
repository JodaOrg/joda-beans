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
package org.joda.beans.test;

import org.joda.beans.Bean;

/**
 * Error class used when two beans fail to compare.
 */
class BeanComparisonError extends AssertionError {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /**
     * The expected bean.
     */
    private final Bean expected;
    /**
     * The actual bean.
     */
    private final Bean actual;

    /**
     * Creates a new error.
     * 
     * @param message  the message, may be null
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    BeanComparisonError(String message, Bean expected, Bean actual) {
        super(message);
        this.expected = expected;
        this.actual = actual;
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the expected field.
     * @return the expected
     */
    public Bean getExpected() {
        return expected;
    }

    /**
     * Gets the actual field.
     * @return the actual
     */
    public Bean getActual() {
        return actual;
    }

}
