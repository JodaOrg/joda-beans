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

import java.lang.reflect.Modifier;
import java.net.URI;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.joda.beans.Bean;
import org.joda.beans.BeanBuilder;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;

/**
 * A utility class to assist with testing beans.
 * <p>
 * Test coverage statistics can be heavily skewed by getters, setters and generated code.
 * This class provides a solution, allowing bean test coverage to be artificially increased.
 * Always remember that the goal of artificially increasing coverage is so that you can
 * see what you really need to test, not to avoid writing tests altogether.
 */
public final class JodaBeanTests {

    /**
     * This constant can be used to pass to increase test coverage.
     * This is used by some {@link MetaBean} methods in generated classes.
     */
    public static final String TEST_COVERAGE_PROPERTY = "!ConstantUsedForTestCoveragePurposes!";

    /**
     * This constant can be used to pass to increase test coverage.
     * This is used by some {@link BeanBuilder} set methods in generated classes.
     */
    public static final String TEST_COVERAGE_STRING = "!ConstantUsedForTestCoveragePurposes!";

    //-------------------------------------------------------------------------
    /**
     * Test a mutable bean for the primary purpose of increasing test coverage.
     * 
     * @param bean  the bean to test
     */
    public static void coverMutableBean(Bean bean) {
        assertNotNull(bean, "coverImmutableBean() called with null bean");
        assertFalse(bean instanceof ImmutableBean);
        assertNotSame(JodaBeanUtils.clone(bean), bean);
        coverBean(bean);
    }

    /**
     * Test an immutable bean for the primary purpose of increasing test coverage.
     * 
     * @param bean  the bean to test
     */
    public static void coverImmutableBean(ImmutableBean bean) {
        assertNotNull(bean, "coverImmutableBean() called with null bean");
        assertSame(JodaBeanUtils.clone(bean), bean);
        coverBean(bean);
    }

    /**
     * Test a bean equals method for the primary purpose of increasing test coverage.
     * <p>
     * The two beans passed in should contain a different value for each property.
     * The method creates a cross-product to ensure test coverage of equals.
     * 
     * @param bean1  the first bean to test
     * @param bean2  the second bean to test
     */
    @SuppressWarnings("unlikely-arg-type")
    public static void coverBeanEquals(Bean bean1, Bean bean2) {
        assertNotNull(bean1, "coverBeanEquals() called with null bean");
        assertNotNull(bean2, "coverBeanEquals() called with null bean");
        assertFalse(bean1.equals(null));
        assertFalse(bean1.equals("NonBean"));
        assertTrue(bean1.equals(bean1));
        assertTrue(bean2.equals(bean2));
        ignoreThrows(() -> assertEquals(bean1, JodaBeanUtils.cloneAlways(bean1)));
        ignoreThrows(() -> assertEquals(bean2, JodaBeanUtils.cloneAlways(bean2)));
        assertTrue(bean1.hashCode() == bean1.hashCode());
        assertTrue(bean2.hashCode() == bean2.hashCode());
        if (bean1.equals(bean2) || bean1.getClass() != bean2.getClass()) {
            return;
        }
        var metaBean = bean1.metaBean();
        var buildableProps = metaBean.metaPropertyMap().values().stream()
                .filter(mp -> mp.style().isBuildable())
                .toList();
        var builtBeansSet = new HashSet<Bean>();
        builtBeansSet.add(bean1);
        builtBeansSet.add(bean2);
        for (var i = 0; i < buildableProps.size(); i++) {
            for (var j = 0; j < 2; j++) {
                try {
                    var bld = metaBean.builder();
                    for (var k = 0; k < buildableProps.size(); k++) {
                        MetaProperty<?> mp = buildableProps.get(k);
                        if (j == 0) {
                            bld.set(mp, mp.get(k < i ? bean1 : bean2));
                        } else {
                            bld.set(mp, mp.get(i <= k ? bean1 : bean2));
                        }
                    }
                    builtBeansSet.add(bld.build());
                } catch (RuntimeException ex) {
                    // ignore
                }
            }
        }
        var builtBeansList = new ArrayList<>(builtBeansSet);
        for (var i = 0; i < builtBeansList.size() - 1; i++) {
            for (var j = i + 1; j < builtBeansList.size(); j++) {
                builtBeansList.get(i).equals(builtBeansList.get(j));
            }
        }
    }

    // provide test coverage to all beans
    private static void coverBean(Bean bean) {
        coverProperties(bean);
        coverNonProperties(bean);
        coverEquals(bean);
    }

    // cover parts of a bean that are property-based
    private static void coverProperties(Bean bean) {
        var metaBean = bean.metaBean();
        var metaPropMap = metaBean.metaPropertyMap();
        assertNotNull(metaPropMap);
        assertEquals(metaBean.metaPropertyCount(), metaPropMap.size());
        for (var mp : metaBean.metaPropertyIterable()) {
            assertTrue(metaBean.metaPropertyExists(mp.name()));
            assertEquals(metaBean.metaProperty(mp.name()), mp);
            // Ensure we don't use interned value
            assertEquals(metaBean.metaProperty(new String(mp.name())), mp);
            assertEquals(metaPropMap.containsValue(mp), true);
            assertEquals(metaPropMap.containsKey(mp.name()), true);
            if (mp.style().isReadable()) {
                ignoreThrows(() -> mp.get(bean));
            } else {
                assertThrows(() -> mp.get(bean), UnsupportedOperationException.class);
            }
            if (mp.style().isWritable()) {
                ignoreThrows(() -> mp.set(bean, ""));
            } else {
                assertThrows(() -> mp.set(bean, ""), UnsupportedOperationException.class);
            }
            if (mp.style().isBuildable()) {
                ignoreThrows(() -> metaBean.builder().get(mp));
                ignoreThrows(() -> metaBean.builder().get(mp.name()));
                for (Object setValue : sampleValues(mp)) {
                    ignoreThrows(() -> metaBean.builder().set(mp, setValue));
                }
                for (Object setValue : sampleValues(mp)) {
                    ignoreThrows(() -> metaBean.builder().set(mp.name(), setValue));
                }
            }
            ignoreThrows(() -> {
                var m = metaBean.getClass().getDeclaredMethod(mp.name());
                m.setAccessible(true);
                m.invoke(metaBean);
            });
            ignoreThrows(() -> {
                var m = metaBean.getClass().getDeclaredMethod(
                        "propertySet", Bean.class, String.class, Object.class, Boolean.TYPE);
                m.setAccessible(true);
                m.invoke(metaBean, bean, mp.name(), "", true);
            });
        }
        ignoreThrows(() -> {
            var m = metaBean.getClass().getDeclaredMethod(
                    "propertyGet", Bean.class, String.class, Boolean.TYPE);
            m.setAccessible(true);
            m.invoke(metaBean, bean, "Not a real property name", true);
        });
        MetaProperty<String> fakeMetaProp = StandaloneMetaProperty.of("fake", metaBean, String.class);
        ignoreThrows(() -> metaBean.builder().set(fakeMetaProp, JodaBeanTests.TEST_COVERAGE_STRING));
        ignoreThrows(() -> metaBean.builder().set(JodaBeanTests.TEST_COVERAGE_PROPERTY, JodaBeanTests.TEST_COVERAGE_STRING));
        ignoreThrows(() -> bean.property(JodaBeanTests.TEST_COVERAGE_PROPERTY));
    }

    // cover parts of a bean that are not property-based
    private static void coverNonProperties(Bean bean) {
        var metaBean = bean.metaBean();
        assertFalse(metaBean.metaPropertyExists(""));
        assertThrows(() -> metaBean.builder().get("foo_bar"), NoSuchElementException.class);
        assertThrows(() -> metaBean.builder().set("foo_bar", ""), NoSuchElementException.class);
        assertThrows(() -> metaBean.metaProperty("foo_bar"), NoSuchElementException.class);

        if (metaBean instanceof DirectMetaBean) {
            var dummy = DirectMetaProperty.ofReadWrite(metaBean, "foo_bar", metaBean.beanType(), String.class);
            assertThrows(() -> dummy.get(bean), NoSuchElementException.class);
            assertThrows(() -> dummy.set(bean, ""), NoSuchElementException.class);
            assertThrows(() -> dummy.setString(bean, ""), NoSuchElementException.class);
            assertThrows(() -> metaBean.builder().get(dummy), NoSuchElementException.class);
            assertThrows(() -> metaBean.builder().set(dummy, ""), NoSuchElementException.class);
        }

        var propertyNameSet = bean.propertyNames();
        assertNotNull(propertyNameSet);
        for (String propertyName : propertyNameSet) {
            assertNotNull(bean.property(propertyName));
        }
        assertThrows(() -> bean.property(""), NoSuchElementException.class);

        var beanClass = bean.getClass();
        ignoreThrows(() -> {
            var m = beanClass.getDeclaredMethod("meta");
            m.setAccessible(true);
            m.invoke(null);
        });
        ignoreThrows(() -> {
            var m = beanClass.getDeclaredMethod("meta" + beanClass.getSimpleName(), Class.class);
            m.setAccessible(true);
            m.invoke(null, String.class);
        });
        ignoreThrows(() -> {
            var m = beanClass.getDeclaredMethod("meta" + beanClass.getSimpleName(), Class.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, String.class, String.class);
        });
        ignoreThrows(() -> {
            var m = beanClass.getDeclaredMethod("meta" + beanClass.getSimpleName(), Class.class, Class.class, Class.class);
            m.setAccessible(true);
            m.invoke(null, String.class, String.class, String.class);
        });

        ignoreThrows(() -> {
            var m = bean.getClass().getDeclaredMethod("builder");
            m.setAccessible(true);
            m.invoke(null);
        });
        ignoreThrows(() -> {
            var m = bean.getClass().getDeclaredMethod("toBuilder");
            m.setAccessible(true);
            m.invoke(bean);
        });

        assertNotNull(bean.toString());
        assertNotNull(metaBean.toString());
        assertNotNull(metaBean.builder().toString());
    }

    // different combinations of values to cover equals()
    @SuppressWarnings("unlikely-arg-type")
    private static void coverEquals(Bean bean) {
        // create beans with different data and compare each to the input bean
        // this will normally trigger each of the possible branches in equals
        var buildableProps = bean.metaBean().metaPropertyMap().values().stream()
                .filter(mp -> mp.style().isBuildable())
                .toList();
        for (var i = 0; i < buildableProps.size(); i++) {
            try {
                var bld = bean.metaBean().builder();
                for (var j = 0; j < buildableProps.size(); j++) {
                    var mp = buildableProps.get(j);
                    if (j < i) {
                        bld.set(mp, mp.get(bean));
                    } else {
                        var samples = sampleValues(mp);
                        bld.set(mp, samples.get(0));
                    }
                }
                var built = bld.build();
                coverBeanEquals(bean, built);
                assertEquals(built, built);
                assertEquals(built.hashCode(), built.hashCode());
            } catch (RuntimeException ex) {
                // ignore
            }
        }
        // cover the remaining equals edge cases
        assertFalse(bean.equals(null));
        assertFalse(bean.equals("NonBean"));
        assertTrue(bean.equals(bean));
        ignoreThrows(() -> assertEquals(bean, JodaBeanUtils.cloneAlways(bean)));
        assertTrue(bean.hashCode() == bean.hashCode());
    }

    // sample values for setters
    private static List<?> sampleValues(MetaProperty<?> mp) {
        var type = mp.propertyType();
        // enum constants
        if (Enum.class.isAssignableFrom(type)) {
            return Arrays.asList(type.getEnumConstants());
        }
        // lookup pre-canned samples
        var sample = SAMPLES.get(type);
        if (sample != null) {
            return sample;
        }
        // find any potential declared constants, using some plural rules
        var typeName = type.getName();
        var samples = new ArrayList<>();
        samples.addAll(buildSampleConstants(type, type));
        ignoreThrows(() -> {
            // cat -> cats
            samples.addAll(buildSampleConstants(Class.forName(typeName + "s"), type));
        });
        ignoreThrows(() -> {
            // dish -> dishes
            samples.addAll(buildSampleConstants(Class.forName(typeName + "es"), type));
        });
        ignoreThrows(() -> {
            // lady -> ladies
            samples.addAll(buildSampleConstants(Class.forName(typeName.substring(0, typeName.length() - 1) + "ies"), type));
        });
        ignoreThrows(() -> {
            // index -> indices
            samples.addAll(buildSampleConstants(Class.forName(typeName.substring(0, typeName.length() - 2) + "ices"), type));
        });
        // none
        return samples;
    }

    // adds sample constants to the 
    private static List<Object> buildSampleConstants(Class<?> queryType, Class<?> targetType) {
        var samples = new ArrayList<>();
        for (var field : queryType.getFields()) {
            if (field.getType() == targetType &&
                    Modifier.isPublic(field.getModifiers()) &&
                    Modifier.isStatic(field.getModifiers()) &&
                    Modifier.isFinal(field.getModifiers()) &&
                    !field.isSynthetic()) {
                ignoreThrows(() -> samples.add(field.get(null)));
            }
        }
        return samples;
    }

    private static final Map<Class<?>, List<?>> SAMPLES;
    static {
        var map = new HashMap<Class<?>, List<?>>();
        map.put(String.class, List.of("Hello", "Goodbye", " ", ""));
        map.put(Byte.class, List.of((byte) 0, (byte) 1));
        map.put(Byte.TYPE, List.of((byte) 0, (byte) 1));
        map.put(Short.class, List.of((short) 0, (short) 1));
        map.put(Short.TYPE, List.of((short) 0, (short) 1));
        map.put(Integer.class, List.of(0, 1));
        map.put(Integer.TYPE, List.of(0, 1));
        map.put(Long.class, List.of((long) 0, (long) 1));
        map.put(Long.TYPE, List.of((long) 0, (long) 1));
        map.put(Float.class, List.of((float) 0, (float) 1));
        map.put(Float.TYPE, List.of((float) 0, (float) 1));
        map.put(Double.class, List.of((double) 0, (double) 1));
        map.put(Double.TYPE, List.of((double) 0, (double) 1));
        map.put(Character.class, List.of(' ', 'A', 'z'));
        map.put(Character.TYPE, List.of(' ', 'A', 'z'));
        map.put(Boolean.class, List.of(Boolean.TRUE, Boolean.FALSE));
        map.put(Boolean.TYPE, List.of(Boolean.TRUE, Boolean.FALSE));
        map.put(LocalDate.class, List.of(LocalDate.now(ZoneOffset.UTC), LocalDate.of(2012, 6, 30)));
        map.put(LocalTime.class, List.of(LocalTime.now(ZoneOffset.UTC), LocalTime.of(11, 30)));
        map.put(LocalDateTime.class, List.of(LocalDateTime.now(ZoneOffset.UTC), LocalDateTime.of(2012, 6, 30, 11, 30)));
        map.put(OffsetTime.class, List.of(OffsetTime.now(ZoneOffset.UTC), OffsetTime.of(11, 30, 0, 0, ZoneOffset.ofHours(1))));
        map.put(OffsetDateTime.class, List.of(
                OffsetDateTime.now(ZoneOffset.UTC),
                OffsetDateTime.of(2012, 6, 30, 11, 30, 0, 0, ZoneOffset.ofHours(1))));
        map.put(ZonedDateTime.class, List.of(
                ZonedDateTime.now(ZoneOffset.UTC),
                ZonedDateTime.of(2012, 6, 30, 11, 30, 0, 0, ZoneId.systemDefault())));
        map.put(Instant.class, List.of(Instant.now(), Instant.EPOCH));
        map.put(Year.class, List.of(Year.now(ZoneOffset.UTC), Year.of(2012)));
        map.put(YearMonth.class, List.of(YearMonth.now(ZoneOffset.UTC), YearMonth.of(2012, 6)));
        map.put(MonthDay.class, List.of(MonthDay.now(ZoneOffset.UTC), MonthDay.of(12, 25)));
        map.put(Month.class, List.of(Month.JULY, Month.DECEMBER));
        map.put(DayOfWeek.class, List.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY));
        map.put(URI.class, List.of(URI.create("http://www.opengamma.com"), URI.create("http://www.joda.org")));
        map.put(Class.class, List.of(Throwable.class, RuntimeException.class, String.class));
        map.put(Object.class, List.of("", 6));
        map.put(Collection.class, List.of(new ArrayList<>()));
        map.put(List.class, List.of(new ArrayList<>()));
        map.put(Set.class, List.of(new HashSet<>()));
        map.put(SortedSet.class, List.of(new TreeSet<>()));
        try {
            var cls = Class.forName("com.google.common.collect.ImmutableList");
            var method = cls.getDeclaredMethod("of");
            map.put(cls, List.of(method.invoke(null)));
        } catch (Exception ex) {
            // ignore
        }
        try {
            var cls = Class.forName("com.google.common.collect.ImmutableSet");
            var method = cls.getDeclaredMethod("of");
            map.put(cls, List.of(method.invoke(null)));
        } catch (Exception ex) {
            // ignore
        }
        try {
            var cls = Class.forName("com.google.common.collect.ImmutableSortedSet");
            var method = cls.getDeclaredMethod("naturalOrder");
            map.put(cls, List.of(method.invoke(null)));
        } catch (Exception ex) {
            // ignore
        }
        try {
            var cls = Class.forName("com.google.common.collect.ImmutableMap");
            var method = cls.getDeclaredMethod("of");
            map.put(cls, List.of(method.invoke(null)));
        } catch (Exception ex) {
            // ignore
        }
        SAMPLES = map;
    }

    //-----------------------------------------------------------------------
    private static void assertNotNull(Object obj) {
        if (obj == null) {
            throw new AssertionError("Expected (a != null), but found (a == null)");
        }
    }

    private static void assertNotNull(Object obj, String message) {
        if (obj == null) {
            throw new AssertionError(message);
        }
    }

    private static void assertSame(Object a, Object b) {
        if (a != b) {
            throw new AssertionError("Expected (a == b), but found (a != b)");
        }
    }

    private static void assertNotSame(Object a, Object b) {
        if (a == b) {
            throw new AssertionError("Expected (a != b), but found (a == b)");
        }
    }

    private static void assertEquals(Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionError("Expected " + expected + ", but found " + actual);
        }
    }

    private static void assertEquals(int actual, int expected) {
        if (actual != expected) {
            throw new AssertionError("Expected " + expected + ", but found " + actual);
        }
    }

    private static void assertTrue(boolean actual) {
        if (!actual) {
            throw new AssertionError("Expected value to be true, but was false");
        }
    }

    private static void assertFalse(boolean actual) {
        if (actual) {
            throw new AssertionError("Expected value to be false, but was true");
        }
    }

    //-----------------------------------------------------------------------
    private static void assertThrows(AssertRunnable runner, Class<? extends Throwable> expected) {
        assertNotNull(runner, "assertThrows() called with null AssertRunnable");
        assertNotNull(expected, "assertThrows() called with null expected Class");

        try {
            runner.run();
            throw new AssertionError("Expected " + expected.getSimpleName() + " but code succeeded normally");
        } catch (AssertionError ex) {
            throw ex;
        } catch (Throwable ex) {
            if (!expected.isInstance(ex)) {
                throw new AssertionError(
                        "Expected " + expected.getSimpleName() + " but received " + ex.getClass().getSimpleName(), ex);
            }
        }
    }

    private static void ignoreThrows(AssertRunnable runner) {
        assertNotNull(runner, "ignoreThrows() called with null AssertRunnable");
        try {
            runner.run();
        } catch (Throwable ex) {
            // ignore
        }
    }

    @FunctionalInterface
    interface AssertRunnable {

        /**
         * Used to wrap code that is expected to throw an exception.
         * 
         * @throws Throwable the expected result
         */
        void run() throws Throwable;

    }
}
