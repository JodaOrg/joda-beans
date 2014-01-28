/*
 *  Copyright 2001-2014 Stephen Colebourne
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

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
     * @param maxErrors  the maximum number of errors to report
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public BeanComparisonError(final String message, final int maxErrors, final Bean expected, final Bean actual) {
        super(buildMessage(message, maxErrors, expected, actual));
        this.expected = expected;
        this.actual = actual;
    }

    //-----------------------------------------------------------------------
    /**
     * Compares the two beans.
     * 
     * @param message  the message, may be null
     * @param maxErrors  the maximum number of errors to report
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @return the message, not null
     */
    private static String buildMessage(final String message, final int maxErrors, final Bean expected, final Bean actual) {
        List<String> diffs = new ArrayList<String>();
        buildMessage(diffs, "", expected, actual);
        StringBuilder buf = new StringBuilder();
        buf.append(message != null ? message + ": " : "");
        buf.append("Bean did not equal expected. Differences:");
        int size = diffs.size();
        if (size > maxErrors) {
            diffs = diffs.subList(0, maxErrors);
        }
        for (String diff : diffs) {
            buf.append('\n').append(diff);
        }
        if (size > maxErrors) {
            buf.append("\n...and " + (size - 10) + " more differences");
        }
        return buf.toString();
    }

    private static void buildMessage(final List<String> diffs, final String prefix, final Object expected, final Object actual) {
        if (expected == null && actual == null) {
            return;
        }
        if (expected == null && actual != null) {
            diffs.add(prefix + ": Expected null, but was " + buildSummary(actual, true));
            return;
        }
        if (expected != null && actual == null) {
            diffs.add(prefix + ": Was null, but expected " + buildSummary(expected, true));
            return;
        }
        if (expected instanceof List && actual instanceof List) {
            List<?> expectedList = (List<?>) expected;
            List<?> actualList = (List<?>) actual;
            if (expectedList.size() != actualList.size()) {
                diffs.add(prefix + ": List size differs, expected " + expectedList.size() + " but was " + actualList.size());
                return;
            }
            for (int i = 0; i < expectedList.size(); i++) {
                buildMessage(diffs, prefix + '[' + i + "]", expectedList.get(i), actualList.get(i));
            }
            return;
        }
        if (expected instanceof Map && actual instanceof Map) {
            Map<?, ?> expectedMap = (Map<?, ?>) expected;
            Map<?, ?> actualMap = (Map<?, ?>) actual;
            if (expectedMap.size() != actualMap.size()) {
                diffs.add(prefix + ": Map size differs, expected " + expectedMap.size() + " but was " + actualMap.size());
                return;
            }
            if (expectedMap.keySet().equals(actualMap.keySet()) == false) {
                diffs.add(prefix + ": Map keyset differs, expected " + buildSummary(expectedMap.keySet(), false) + " but was " + buildSummary(actualMap.keySet(), false));
                return;
            }
            for (Object key : expectedMap.keySet()) {
                buildMessage(diffs, prefix + '[' + key + "]", expectedMap.get(key), actualMap.get(key));
            }
            return;
        }
        if (expected.getClass() != actual.getClass()) {
            diffs.add(prefix + ": Class differs, expected " + buildSummary(expected, true) + " but was " + buildSummary(actual, true));
            return;
        }
        if (expected instanceof Bean) {
            for (MetaProperty<?> prop : ((Bean) expected).metaBean().metaPropertyIterable()) {
                buildMessage(diffs, prefix + '.' + prop.name(), prop.get((Bean) expected), prop.get((Bean) actual));
            }
            return;
        }
        if (JodaBeanUtils.equal(expected, actual) == false) {
            diffs.add(prefix + ": Content differs, expected " + buildSummary(expected, true) + " but was " + buildSummary(actual, false));
            return;
        }
        return;  // equal
    }

    /**
     * Builds a summary of an object.
     * 
     * @param obj  the object to summarise, not null
     */
    private static String buildSummary(final Object obj, final boolean includeType) {
        String type = obj.getClass().getSimpleName();
        String toStr = obj.toString();
        if (toStr.length() > 60) {
            toStr = toStr.substring(0, 57) + "...";
        }
        return (includeType ? type + " " : "") + "<" + toStr + ">";
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
