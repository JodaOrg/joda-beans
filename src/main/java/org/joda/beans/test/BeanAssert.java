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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaProperty;

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
    public static void assertBeanEquals(Bean expected, Bean actual) {
        assertBeanEquals(null, expected, actual, 0d);
    }

    /**
     * Asserts that two beans are equal, providing a better error message.
     * <p>
     * Note that specifying a tolerance can mean that two beans compare as not
     * equal using {@link Object#equals(Object)} but equal using this method,
     * because the standard equals method has no tolerance.
     * 
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @param tolerance  the tolerance to use for {@code double} and {@code float}
     */
    public static void assertBeanEquals(Bean expected, Bean actual, double tolerance) {
        assertBeanEquals(null, expected, actual, tolerance);
    }

    /**
     * Asserts that two beans are equal, providing a better error message.
     * 
     * @param baseMsg  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEquals(String baseMsg, Bean expected, Bean actual) {
        assertBeanEquals(baseMsg, expected, actual, 0d);
    }

    /**
     * Asserts that two beans are equal, providing a better error message.
     * <p>
     * Note that specifying a tolerance can mean that two beans compare as not
     * equal using {@link Object#equals(Object)} but equal using this method,
     * because the standard equals method has no tolerance.
     * 
     * @param baseMsg  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @param tolerance  the tolerance to use for {@code double} and {@code float}
     */
    public static void assertBeanEquals(String baseMsg, Bean expected, Bean actual, double tolerance) {
        if (expected == null) {
            throw new AssertionError(baseMsg + ": Expected bean must not be null");
        }
        if (actual == null) {
            throw new AssertionError(baseMsg + ": Actual bean must not be null");
        }
        if (expected.equals(actual) == false) {
            String comparisonMsg = buildMessage(baseMsg, 10, expected, actual, tolerance);
            if (comparisonMsg.isEmpty()) {
                return; // no errors, just double/float within tolerance
            }
            throw new BeanComparisonError(comparisonMsg, expected, actual);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Asserts that two beans are equal, providing a better error message.
     * 
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEqualsFullDetail(Bean expected, Bean actual) {
        assertBeanEqualsFullDetail(null, expected, actual, 0d);
    }

    /**
     * Asserts that two beans are equal, providing a better error message.
     * <p>
     * Note that specifying a tolerance can mean that two beans compare as not
     * equal using {@link Object#equals(Object)} but equal using this method,
     * because the standard equals method has no tolerance.
     * 
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @param tolerance  the tolerance to use for {@code double} and {@code float}
     */
    public static void assertBeanEqualsFullDetail(Bean expected, Bean actual, double tolerance) {
        assertBeanEqualsFullDetail(null, expected, actual, tolerance);
    }

    /**
     * Asserts that two beans are equal, providing a better error message, with
     * an unlimited number of errors reported.
     * 
     * @param baseMsg  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     */
    public static void assertBeanEqualsFullDetail(String baseMsg, Bean expected, Bean actual) {
        assertBeanEqualsFullDetail(baseMsg, expected, actual, 0d);
    }

    /**
     * Asserts that two beans are equal, providing a better error message, with
     * an unlimited number of errors reported.
     * <p>
     * Note that specifying a tolerance can mean that two beans compare as not
     * equal using {@link Object#equals(Object)} but equal using this method,
     * because the standard equals method has no tolerance.
     * 
     * @param baseMsg  the message to use in any error, null uses default message
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @param tolerance  the tolerance to use for {@code double} and {@code float}
     */
    public static void assertBeanEqualsFullDetail(String baseMsg, Bean expected, Bean actual, double tolerance) {
        if (expected == null) {
            throw new AssertionError(baseMsg + ": Expected bean must not be null");
        }
        if (actual == null) {
            throw new AssertionError(baseMsg + ": Actual bean must not be null");
        }
        if (expected.equals(actual) == false) {
            String comparisonMsg = buildMessage(baseMsg, Integer.MAX_VALUE, expected, actual, tolerance);
            if (comparisonMsg.isEmpty()) {
                return; // no errors, just double/float within tolerance
            }
            throw new BeanComparisonError(comparisonMsg, expected, actual);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Compares the two beans.
     * 
     * @param baseMsg  the message, may be null
     * @param maxErrors  the maximum number of errors to report
     * @param expected  the expected value, not null
     * @param actual  the actual value, not null
     * @param tolerance  the tolerance to use for {@code double} and {@code float}
     * @return the message, not null
     */
    private static String buildMessage(String baseMsg, int maxErrors, Bean expected, Bean actual, double tolerance) {
        List<String> diffs = new ArrayList<>();
        buildMessage(diffs, "", expected, actual, tolerance);
        if (diffs.size() == 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(baseMsg != null ? baseMsg + ": " : "");
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

    private static void buildMessage(List<String> diffs, String prefix, Object expected, Object actual, double tolerance) {
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
                buildMessage(diffs, prefix + '[' + i + "]", expectedList.get(i), actualList.get(i), tolerance);
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
                buildMessage(diffs, prefix + '[' + key + "]", expectedMap.get(key), actualMap.get(key), tolerance);
            }
            return;
        }
        if (expected != null && expected.getClass() != actual.getClass()) {
            diffs.add(prefix + ": Class differs, expected " + buildSummary(expected, true) + " but was " + buildSummary(actual, true));
            return;
        }
        if (expected instanceof Bean) {
            for (MetaProperty<?> prop : ((Bean) expected).metaBean().metaPropertyIterable()) {
                buildMessage(diffs, prefix + '.' + prop.name(), prop.get((Bean) expected), prop.get((Bean) actual), tolerance);
            }
            return;
        }
        if (expected instanceof Double && actual instanceof Double && tolerance != 0d) {
            double e = (Double) expected;
            double a = (Double) actual;
            if (!JodaBeanUtils.equalWithTolerance(e, a, tolerance)) {
                diffs.add(prefix + ": Double values differ by more than allowed tolerance, expected " +
                    buildSummary(expected, true) + " but was " + buildSummary(actual, false));
            }
            return;
        }
        if (expected instanceof double[] && actual instanceof double[] && tolerance != 0d) {
            double[] e = (double[]) expected;
            double[] a = (double[]) actual;
            if (e.length != a.length) {
                diffs.add(prefix + ": Double arrays differ in length, expected " +
                                buildSummary(expected, true) + " but was " + buildSummary(actual, false));
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (!JodaBeanUtils.equalWithTolerance(e[i], a[i], tolerance)) {
                        diffs.add(prefix + ": Double arrays differ by more than allowed tolerance, expected " +
                            buildSummary(expected, true) + " but was " + buildSummary(actual, false));
                        break;
                    }
                }
            }
            return;
        }
        if (expected instanceof Float && actual instanceof Float && tolerance != 0d) {
            float e = (Float) expected;
            float a = (Float) actual;
            if (!JodaBeanUtils.equalWithTolerance(e, a, tolerance)) {
                diffs.add(prefix + ": Float values differ by more than allowed tolerance, expected " +
                    buildSummary(expected, true) + " but was " + buildSummary(actual, false));
            }
            return;
        }
        if (expected instanceof float[] && actual instanceof float[] && tolerance != 0d) {
            float[] e = (float[]) expected;
            float[] a = (float[]) actual;
            if (e.length != a.length) {
                diffs.add(prefix + ": Double arrays differ in length, expected " +
                                buildSummary(expected, true) + " but was " + buildSummary(actual, false));
            } else {
                for (int i = 0; i < a.length; i++) {
                    if (!JodaBeanUtils.equalWithTolerance(e[i], a[i], tolerance)) {
                        diffs.add(prefix + ": Float arrays differ by more than allowed tolerance, expected " +
                            buildSummary(expected, true) + " but was " + buildSummary(actual, false));
                        break;
                    }
                }
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
    private static String buildSummary(Object obj, boolean includeType) {
        String type = obj.getClass().getSimpleName();
        String toStr;
        if (obj instanceof double[]) {
            toStr = Arrays.toString((double[]) obj);
        } else if (obj instanceof float[]) {
            toStr = Arrays.toString((float[]) obj);
        } else {
            toStr = obj.toString();
        }
        if (toStr.length() > 60) {
            toStr = toStr.substring(0, 57) + "...";
        }
        return (includeType ? type + " " : "") + "<" + toStr + ">";
    }

}
