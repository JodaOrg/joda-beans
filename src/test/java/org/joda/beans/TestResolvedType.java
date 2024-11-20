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
package org.joda.beans;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.joda.beans.sample.AbstractResult;
import org.joda.beans.sample.Address;
import org.joda.beans.sample.AddressResult;
import org.joda.beans.sample.CompanyAddress;
import org.joda.beans.sample.CompanyAddressMidResult;
import org.joda.beans.sample.CompanyAddressResult;
import org.joda.beans.sample.DoubleGenericsNoExtendsNoSuper;
import org.joda.beans.sample.DoubleGenericsWithExtendsNoSuper;
import org.joda.beans.sample.GenericArray;
import org.joda.beans.sample.GenericInterfaceBase;
import org.joda.beans.sample.GenericInterfaceChild;
import org.joda.beans.sample.GenericInterfaceImpl;
import org.joda.beans.sample.GenericInterfaceMid;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmArrays;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.MidAbstractResult;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.RiskLevel;
import org.joda.beans.sample.SubDecimal;
import org.joda.convert.StringConvert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;

/**
 * Test {@link ResolvedType}.
 */
public class TestResolvedType {

    @SuppressWarnings("serial")
    static Object[][] data_resolvedTypes() {
        return new Object[][] {
                {ResolvedType.of(String.class),
                        String.class, List.of(),
                        "String"},
                {ResolvedType.of(List.class),
                            List.class, List.of(),
                            "List"},
                {ResolvedType.from(String.class),
                        String.class, List.of(),
                        "String"},
                {ResolvedType.from(List.class),
                        List.class, List.of(ResolvedType.OBJECT),
                        "List<Object>"},
                {ResolvedType.ofFlat(String.class),
                        String.class, List.of(),
                        "String"},
                {ResolvedType.ofFlat(List.class, String.class),
                        List.class, List.of(ResolvedType.of(String.class)),
                        "List<String>"},
                {ResolvedType.of(List.class, ResolvedType.STRING),
                        List.class, List.of(ResolvedType.of(String.class)),
                        "List<String>"},
                {ResolvedType.ofFlat(Map.class, String.class, Number.class),
                        Map.class, List.of(ResolvedType.of(String.class), ResolvedType.of(Number.class)),
                        "Map<String, Number>"},
                {ResolvedType.of(String[].class),
                        String[].class, List.of(),
                        "String[]"},
                {ResolvedType.ofFlat(List[].class, String.class),
                        List[].class, List.of(ResolvedType.of(String.class)),
                        "List<String>[]"},
                {ResolvedType.of(String[][].class),
                        String[][].class, List.of(),
                        "String[][]"},
                {ResolvedType.ofFlat(List[][].class, String.class),
                        List[][].class, List.of(ResolvedType.of(String.class)),
                        "List<String>[][]"},
                {ResolvedType.from(List[][].class),
                        List[][].class, List.of(ResolvedType.of(Object.class)),
                        "List<Object>[][]"},
                {ResolvedType.ofFlat(Map[].class, String.class, List[].class),
                        Map[].class, List.of(ResolvedType.STRING, ResolvedType.of(List[].class)),
                        "Map<String, List[]>[]"},
                {ResolvedType.of(Map[].class, ResolvedType.STRING, ResolvedType.ofFlat(List[].class, Number.class)),
                        Map[].class, List.of(ResolvedType.STRING, ResolvedType.of(List[].class, ResolvedType.of(Number.class))),
                        "Map<String, List<Number>[]>[]"},
                {ResolvedType.from(Person.meta().addressList().propertyGenericType(), Person.class),
                        List.class, List.of(ResolvedType.of(Address.class)),
                        "List<org.joda.beans.sample.Address>"},
                {ResolvedType.from(Person.meta().addressesList().propertyGenericType(), Person.class),
                        List.class, List.of(ResolvedType.ofFlat(List.class, Address.class)),
                        "List<List<org.joda.beans.sample.Address>>"},
                {ResolvedType.from(ImmPerson.meta().codeCounts().propertyGenericType(), ImmPerson.class),
                        ImmutableMultiset.class, List.of(ResolvedType.of(String.class)),
                        "com.google.common.collect.ImmutableMultiset<String>"},
                {ResolvedType.from(ImmAddress.meta().listNumericInMap().propertyGenericType(), ImmAddress.class),
                        ImmutableMap.class, List.of(ResolvedType.of(String.class), ResolvedType.ofFlat(List.class, Integer.class)),
                        "com.google.common.collect.ImmutableMap<String, List<Integer>>"},
                {ResolvedType.from(ImmGuava.meta().mapWildKey().propertyGenericType(), ImmGuava.class),
                        ImmutableMap.class, List.of(ResolvedType.of(Number.class), ResolvedType.of(String.class)),
                        "com.google.common.collect.ImmutableMap<Number, String>"},
                {ResolvedType.from(ImmGuava.meta().listWildExtendsComparable().propertyGenericType(), ImmGuava.class),
                        ImmutableList.class, List.of(ResolvedType.ofFlat(Comparable.class, Object.class)),
                        "com.google.common.collect.ImmutableList<java.lang.Comparable<Object>>"},
                {ResolvedType.from(ImmGuava.meta().listWildExtendsT().propertyGenericType(), ImmGuava.class),
                        ImmutableList.class, List.of(ResolvedType.of(Comparable.class, ResolvedType.of(Object.class))),
                        "com.google.common.collect.ImmutableList<java.lang.Comparable<Object>>"},
                {ResolvedType.from(AddressResult.meta().docs().propertyGenericType(), AddressResult.class),
                        List.class, List.of(ResolvedType.of(Address.class)),
                        "List<org.joda.beans.sample.Address>"},
                {ResolvedType.from(AddressResult.meta().docs().propertyGenericType(), CompanyAddressResult.class),
                        List.class, List.of(ResolvedType.of(CompanyAddress.class)),
                        "List<org.joda.beans.sample.CompanyAddress>"},
                {ResolvedType.from(AddressResult.meta().docs().propertyGenericType(), MidAbstractResult.class),
                        List.class, List.of(ResolvedType.of(Address.class)),
                        "List<org.joda.beans.sample.Address>"},
                {ResolvedType.from(AddressResult.meta().docs().propertyGenericType(), CompanyAddressMidResult.class),
                        List.class, List.of(ResolvedType.of(CompanyAddress.class)),
                        "List<org.joda.beans.sample.CompanyAddress>"},
                {ResolvedType.from(ImmArrays.meta().intArray().propertyGenericType(), ImmArrays.class),
                        int[].class, List.of(),
                        "int[]"},
                {ResolvedType.from(GenericArray.meta().values().propertyGenericType(), GenericArray.class),
                        Address[].class, List.of(),
                        "org.joda.beans.sample.Address[]"},
                {ResolvedType.from(
                        DoubleGenericsNoExtendsNoSuper.meta().typeTArray().propertyGenericType(),
                        DoubleGenericsNoExtendsNoSuper.class),
                        Object[].class, List.of(),
                        "Object[]"},
                {ResolvedType.from(
                        DoubleGenericsWithExtendsNoSuper.meta().typeTArray().propertyGenericType(),
                        DoubleGenericsWithExtendsNoSuper.class),
                        Serializable[].class, List.of(),
                        "java.io.Serializable[]"},
                {ResolvedType.from(
                        DoubleGenericsWithExtendsNoSuper.meta().typeTArrayOfList().propertyGenericType(),
                        DoubleGenericsWithExtendsNoSuper.class),
                        List[].class, List.of(ResolvedType.of(Serializable.class)),
                        "List<java.io.Serializable>[]"},
                {ResolvedType.from(
                        DoubleGenericsWithExtendsNoSuper.meta().typeTArray2dOfList().propertyGenericType(),
                        DoubleGenericsWithExtendsNoSuper.class),
                        List[][].class, List.of(ResolvedType.of(Serializable.class)),
                        "List<java.io.Serializable>[][]"},
                {ResolvedType.from(GenericInterfaceBase.class.getMethods()[0].getGenericReturnType(), GenericInterfaceBase.class),
                        List.class, List.of(ResolvedType.of(Serializable.class)),
                        "List<java.io.Serializable>"},
                {ResolvedType.from(GenericInterfaceBase.class.getMethods()[0].getGenericReturnType(), GenericInterfaceChild.class),
                        List.class, List.of(ResolvedType.of(Number.class)),
                        "List<Number>"},
                {ResolvedType.from(GenericInterfaceBase.class.getMethods()[0].getGenericReturnType(), GenericInterfaceMid.class),
                        List.class, List.of(ResolvedType.of(BigDecimal.class)),
                        "List<java.math.BigDecimal>"},
                {ResolvedType.from(GenericInterfaceBase.class.getMethods()[0].getGenericReturnType(), GenericInterfaceImpl.class),
                        List.class, List.of(ResolvedType.of(SubDecimal.class)),
                        "List<org.joda.beans.sample.SubDecimal>"},
                {ResolvedType.from(List.class, Object.class),
                        List.class, List.of(ResolvedType.of(Object.class)),
                        "List<Object>"},
                {ResolvedType.from(List.class, Object.class),
                        List.class, List.of(ResolvedType.of(Object.class)),
                        "List<Object>"},
                {ResolvedType.from(AbstractResult.class, AbstractResult.class),
                        AbstractResult.class, List.of(ResolvedType.of(Address.class)),
                        "org.joda.beans.sample.AbstractResult<org.joda.beans.sample.Address>"},
                {ResolvedType.from(AbstractResult.class, CompanyAddressResult.class),
                        AbstractResult.class, List.of(ResolvedType.of(CompanyAddress.class)),
                        "org.joda.beans.sample.AbstractResult<org.joda.beans.sample.CompanyAddress>"},
                // recursive generics
                {ResolvedType.of(Enum.class),
                        Enum.class, List.of(),
                        "java.lang.Enum"},
                {ResolvedType.from(Enum.class),
                        Enum.class, List.of(ResolvedType.ofFlat(Enum.class, Object.class)),
                        "java.lang.Enum<java.lang.Enum<Object>>"},
                {ResolvedType.from(Enum.class, Thread.State.class),  // also test nested classes
                        Enum.class, List.of(ResolvedType.of(Thread.State.class)),
                        "java.lang.Enum<java.lang.Thread$State>"},
                // enums with class-per-constant
                {ResolvedType.from(Enum.class, RiskLevel.class),
                        Enum.class, List.of(ResolvedType.of(RiskLevel.class)),
                        "java.lang.Enum<org.joda.beans.sample.RiskLevel>"},
                {ResolvedType.from(Enum.class, RiskLevel.HIGH.getClass()),
                        Enum.class, List.of(ResolvedType.of(RiskLevel.class)),
                        "java.lang.Enum<org.joda.beans.sample.RiskLevel>"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_resolvedTypes")
    void test_basics(
            ResolvedType test,
            Class<?> expectedRawType,
            List<ResolvedType> expectedArgTypes,
            String expectedToString) {

        assertThat(test.getRawType()).isEqualTo(expectedRawType);
        assertThat(test.getArguments()).containsExactlyElementsOf(expectedArgTypes);

        assertThat(test).hasToString(expectedToString);
        assertThat(test).hasSameHashCodeAs(test);
        assertThat(test.equals(test)).isTrue();
        assertThat(test.equals(new Object())).isFalse();
        assertThat(test.equals(ResolvedType.of(TestResolvedType.class))).isFalse();
        assertThat(test.equals(ResolvedType.ofFlat(List.class, TestResolvedType.class))).isFalse();
    }

    @ParameterizedTest
    @MethodSource("data_resolvedTypes")
    void test_queries(
            ResolvedType test,
            Class<?> expectedRawType,
            List<ResolvedType> expectedArgTypes,
            String expectedToString) {

        if (expectedArgTypes.isEmpty()) {
            assertThat(test.getArgumentOrDefault(0)).isEqualTo(ResolvedType.OBJECT);
            assertThat(test.isRaw()).isEqualTo(expectedRawType.getTypeParameters().length != 0);
        } else if (expectedArgTypes.size() == 1) {
            assertThat(test.getArgumentOrDefault(0)).isEqualTo(expectedArgTypes.get(0));
            assertThat(test.getArgumentOrDefault(1)).isEqualTo(ResolvedType.OBJECT);
            assertThat(test.isRaw()).isFalse();
        } else if (expectedArgTypes.size() == 2) {
            assertThat(test.getArgumentOrDefault(0)).isEqualTo(expectedArgTypes.get(0));
            assertThat(test.getArgumentOrDefault(1)).isEqualTo(expectedArgTypes.get(1));
            assertThat(test.getArgumentOrDefault(2)).isEqualTo(ResolvedType.OBJECT);
            assertThat(test.isRaw()).isFalse();
        }
        if (expectedRawType.isArray()) {
            assertThat(test.isArray()).isTrue();
            assertThat(test.toComponentType())
                    .isEqualTo(ResolvedType.of(expectedRawType.getComponentType(), test.getArguments().toArray(new ResolvedType[0])));
        } else {
            assertThat(test.isArray()).isFalse();
            assertThatIllegalStateException()
                    .isThrownBy(() -> test.toComponentType())
                    .withMessage("Unable to get component type for " + expectedToString + ", type is not an array");
        }
        assertThat(test.toArrayType().toComponentType()).isEqualTo(test);
        assertThat(test.isPrimitive()).isEqualTo(expectedRawType.isPrimitive());
    }

    @ParameterizedTest
    @MethodSource("data_resolvedTypes")
    void test_jodaConvert(
            ResolvedType test,
            Class<?> expectedRawType,
            List<ResolvedType> expectedArgTypes,
            String expectedToString) {

        var str = StringConvert.INSTANCE.convertToString(test);
        assertThat(str).isEqualTo(expectedToString);
        var obj = StringConvert.INSTANCE.convertFromString(ResolvedType.class, str);
        assertThat(obj).isEqualTo(test);
    }

    @SuppressWarnings("serial")
    static Object[][] data_invalidParse() {
        return new Object[][] {
                {"String,"},
                {"String<"},
                {"String>"},
                {"String]"},
                {"String[]]"},
                {"List<>"},
                {"List<<String"},
                {"List<[]String"},
                {"List<>String"},
                {"List<String><"},
                {"List<String>>"},
                {"List<String>[]<"},
                {"List<String"},
                {"List<List<String"},
                {"List<List<String>"},
                {"List<String>[]String"},
                {"List<String>String"},
        };
    }

    @ParameterizedTest
    @MethodSource("data_invalidParse")
    void test_invalidParse(String stringToParse) {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> ResolvedType.parse(stringToParse))
                .withMessageMatching(
                        "Unable to parse ResolvedType( from '" + Pattern.quote(stringToParse) + "', invalid format|, class not found: .*)");
    }

    @Test
    void test_enumSubclass() {
        var test = ResolvedType.of(RiskLevel.HIGH.getClass());
        assertThat(test.getRawType().getSuperclass()).isEqualTo(RiskLevel.class);
    }
}
