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
package org.joda.beans.ser;

import java.util.Arrays;
import java.util.Currency;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.joda.beans.sample.Address;
import org.joda.beans.sample.Company;
import org.joda.beans.sample.CompanyAddress;
import org.joda.beans.sample.ImmAddress;
import org.joda.beans.sample.ImmGuava;
import org.joda.beans.sample.ImmKey;
import org.joda.beans.sample.ImmOptional;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.Person;
import org.joda.beans.sample.PrimitiveBean;
import org.joda.beans.sample.RiskLevel;
import org.joda.beans.sample.RiskPerception;
import org.joda.beans.sample.SimpleJson;
import org.joda.collect.grid.DenseGrid;
import org.joda.collect.grid.SparseGrid;

import com.google.common.base.Optional;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.ImmutableTable;

/**
 * Test helper.
 */
public class SerTestHelper {

    public static Address testAddress() {
        Person person = new Person();
        person.setForename("Etienne");
        person.setSurname("Colebourne");
        person.getExtensions().set("interests", "joda");
        person.getExtensions().set("conferenceCount", 21);
        person.getExtensions().set("quality", 'B');
        person.getExtensions().set("company", new Company("OpenGamma"));
        Address address = new Address();
        address.setOwner(person);
        address.setNumber(251);
        address.setStreet("Big Road");
        address.setCity("London & Capital of the World <!>");
        CompanyAddress workAddress = new CompanyAddress();
        workAddress.setCompanyName("OpenGamma");
        workAddress.setNumber(185);
        workAddress.setStreet("Park Street");
        workAddress.setCity("London");
        Address homeAddress = new Address();
        homeAddress.setNumber(65432);
        homeAddress.setStreet("Big Road");
        homeAddress.setCity("Bigton");
        person.setMainAddress(workAddress);
        person.getOtherAddressMap().put("home", homeAddress);
        person.getOtherAddressMap().put("work", workAddress);
        person.getOtherAddressMap().put("other", null);
        person.getAddressList().add(homeAddress);
        person.getAddressList().add(null);
        person.getAddressList().add(workAddress);
        person.getAddressesList().add(ImmutableList.of(homeAddress, workAddress));
        return address;
    }

    public static ImmAddress testImmAddress() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("A", Arrays.asList("B", "b"));
        Map<String, List<Integer>> map2 = new HashMap<>();
        map2.put("A", Arrays.asList(3, 2, 1));
        Map<String, List<List<Integer>>> map3 = new HashMap<>();
        map3.put("A", Arrays.asList(Arrays.asList(3, 2, 1)));
        Map<ImmPerson, Map<String, ImmPerson>> map4 = new HashMap<>();
        Map<String, List<List<Object>>> map5 = new HashMap<>();
        PrimitiveBean primitives = new PrimitiveBean();
        primitives.setValueLong(1L);
        primitives.setValueInt(2);
        primitives.setValueShort((short) 3);
        primitives.setValueByte((byte) 4);
        primitives.setValueDouble(5.0d);
        primitives.setValueFloat(6.0f);
        primitives.setValueChar('7');
        primitives.setValueBoolean(true);
        List<Object> objects1 = Arrays.<Object>asList(Currency.getInstance("GBP"), TimeZone.getTimeZone("Europe/London"));
        List<Object> objects2 = Arrays.<Object>asList(Locale.CANADA_FRENCH, Long.valueOf(2), primitives);
        map5.put("A", Arrays.asList(objects1));
        map5.put("B", Arrays.asList(objects2));
        Map<String, Object> map6 = new HashMap<>();
        map6.put("A", "Abba");
        map6.put("B", ImmutableSet.of("a", "b"));
        map6.put("C", ImmutableSet.copyOf(objects2));
        map6.put("D", ImmutableMap.of("d", 1, "e", 2));
        ImmPerson person = ImmPerson.builder()
            .forename("Etienne")
            .middleNames("K", "T")
            .surname("Colebourne")
            .addressList(Arrays.asList(new Address()))
            .codeCounts(ImmutableMultiset.of("A", "A", "B"))
            . build();
        ImmPerson child = ImmPerson.builder()
                .forename("Etiennette")
                .surname("Colebourne")
                . build();
        ImmPerson child2 = ImmPerson.builder()
                .forename("Kylie")
                .surname("Colebourne")
                . build();
        ImmAddress childAddress = ImmAddress.builder()
                .owner(child)
                .number(185)
                .street("Park Street")
                .city("London")
                .risk(RiskLevel.LOW)
                .riskLevel(RiskLevel.HIGH)
                .riskLevels(EnumSet.of(RiskLevel.LOW, RiskLevel.MEDIUM))
                .object1(RiskLevel.MEDIUM)
                .object2(RiskPerception.LOW)
                .data(new byte[] {64, 65, 66})
                .build();
        map4.put(child, ImmutableMap.of("sibling", child2));
        HashBasedTable<Integer, Integer, ImmPerson> table = HashBasedTable.create();
        table.put(1, 1, person);
        table.put(1, 2, child);
        table.put(2, 1, child2);
        SparseGrid<ImmPerson> sparseGrid = SparseGrid.create(5, 5);
        sparseGrid.put(1, 1, child2);
        DenseGrid<ImmPerson> denseGrid = DenseGrid.create(2, 3);
        denseGrid.put(0, 0, child);
        denseGrid.put(1, 1, child2);
        ImmAddress address = ImmAddress.builder()
            .owner(person)
            .number(185)
            .street("Park Street")
            .city("London & Capital of the World <!>\n")
            .abstractNumber(Short.valueOf((short) 89))
            .array2d(new String[][] {{"a"}, {}, {"b", "c"}})
            .object1(ImmutableList.of("a", "b", "c"))
            .object2(ImmutableMap.of("d", 1, Currency.getInstance("GBP"), 2))
            .serializable(ImmutableList.of("a", "b", "c"))
            .objectInMap(map6)
            .listInMap(map)
            .listNumericInMap(map2)
            .listInListInMap(map3)
            .objectListInListInMap(map5)
            .mapInMap(map4)
            .simpleTable(ImmutableTable.of(1, 1, "Hello"))
            .compoundTable(table)
            .sparseGrid(sparseGrid)
            .denseGrid(denseGrid)
            .beanBeanMap(ImmutableMap.of(child, childAddress))
            .doubleVector(new double[] {1.1, 2.2, 3.3})
            .matrix(new double[][] {{1.1, 2.2}, {3.2}})
            .build();
        return address;
    }

    public static ImmOptional testImmOptional() {
        ImmOptional optional = ImmOptional.builder()
            .optString(Optional.of("A"))
            .build();
        return optional;
    }

    public static ImmGuava<String> testCollections() {
        ImmutableList<String> list = ImmutableList.of("A", "B");
        ImmutableSet<String> set = ImmutableSet.of("A", "B");
        ImmutableSortedSet<String> sortedSet = ImmutableSortedSet.of("A", "B");
        ImmutableMap<String, String> map = ImmutableMap.of("A", "AA", "B", "BB");
        ImmutableSortedMap<String, String> sortedMap = ImmutableSortedMap.of("A", "AA", "B", "BB");
        ImmutableBiMap<String, String> bimap = ImmutableBiMap.of("A", "AA", "B", "BB");
        return ImmGuava.<String> builder()
            .list(list)
            .listInterface(list)
            .set(set)
            .setInterface(set)
            .sortedSet(sortedSet)
            .sortedSetInterface(sortedSet)
            .map(map)
            .mapInterface(map)
            .sortedMap(sortedMap)
            .sortedMapInterface(sortedMap)
            .biMap(bimap)
            .biMapInterface(bimap)
            .build();
    }

    public static SimpleJson testSimpleJson() {
        Map<String, List<String>> map = new HashMap<>();
        map.put("A", Arrays.asList("B", "b"));
        Map<String, List<Integer>> map2 = new HashMap<>();
        map2.put("A", Arrays.asList(3, 2, 1));
        ImmKey key1 = ImmKey.builder().name("Cat").build();
        ImmKey key2 = ImmKey.builder().name("Dog").build();
        SimpleJson result = SimpleJson.builder()
                .primitiveChar('a')
                .primitiveByte((byte) 23)
                .primitiveShort((short) 12)
                .primitiveInt(9)
                .primitiveLong(6)
                .primitiveFloat(3.4f)
                .primitiveDouble(2.3d)
                .primitiveDoubleNaN(Double.NaN)
                .primitiveDoubleInf(Double.POSITIVE_INFINITY)
                .abstractNumber(Integer.valueOf(29))
                .arrayByte(new byte[] {64, 65, 66})
                .array2d(new String[][] {{"a"}, {}, {"b", "c"}})
                .string("ABBA")
                .bean(key1)
                .object1(ImmutableList.of("a", "b", "c"))
                .object2(ImmutableMap.of("d", 1, "12", "2"))
                .risk(RiskLevel.LOW)
                .riskLevel(RiskLevel.HIGH)
                .riskLevels(EnumSet.of(RiskLevel.LOW, RiskLevel.MEDIUM))
                .stringList(ImmutableList.of("a", "b", "c"))
                .beanList(ImmutableList.of(key1, key2))
                .stringMap(ImmutableMap.of("a", "A", "b", "B"))
                .intKeyMap(ImmutableMap.of(1, "A", 2, "B"))
                .beanMap(ImmutableMap.of("a", key1, "b", key2))
                .listInMap(map)
                .listNumericInMap(map2)
                .build();
        return result;
    }

}
