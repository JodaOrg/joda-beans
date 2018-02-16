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
package org.joda.beans.sample;

import java.io.Serializable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.direct.DirectFieldsBeanBuilder;
import org.joda.beans.impl.direct.DirectMetaBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.direct.DirectMetaPropertyMap;
import org.joda.collect.grid.Grid;
import org.joda.collect.grid.ImmutableGrid;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;

/**
 * Mock address JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition(constructorScope = "private")
public final class ImmAddress implements ImmutableBean, Serializable {

    /**
     * The number.
     * This will be the flat, house number or house name.
     */
    @PropertyDefinition
    private final int number;
    /**
     * The street.
     */
    @PropertyDefinition(validate = "notNull")
    private final String street;
    /**
     * The city.
     */
    @PropertyDefinition(validate = "notNull")
    private final String city;
    /**
     * The abstract number.
     */
    @PropertyDefinition
    private final Number abstractNumber;
    /**
     * The extra data.
     */
    @PropertyDefinition
    private final byte[] data;
    /**
     * The 2D array.
     */
    @PropertyDefinition
    private final String[][] array2d;
    /**
     * The owner.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmPerson owner;
    /**
     * The object field.
     */
    @PropertyDefinition
    private final Object object1;
    /**
     * The object field.
     */
    @PropertyDefinition
    private final Object object2;
    /**
     * The risk field.
     */
    @PropertyDefinition
    private final Risk risk;
    /**
     * The risk level field, testing an {@code Enum}.
     */
    @PropertyDefinition
    private final RiskLevel riskLevel;
    /**
     * The risk levels field, testing {@code EnumSet}.
     */
    @PropertyDefinition
    private final EnumSet<RiskLevel> riskLevels;
    /**
     * The serializable field.
     */
    @PropertyDefinition
    private final Serializable serializable;
    /**
     * The object in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<String, Object> objectInMap;
    /**
     * The list in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<String, List<String>> listInMap;
    /**
     * The list in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<String, List<Integer>> listNumericInMap;
    /**
     * The list in list in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<String, List<List<Integer>>> listInListInMap;
    /**
     * The object list in list in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<String, List<List<Object>>> objectListInListInMap;
    /**
     * The map in map field.
     */
    @PropertyDefinition(validate = "notNull")
    private final ImmutableMap<ImmPerson, Map<String, ImmPerson>> mapInMap;
    /**
     * The simple table.
     */
    @PropertyDefinition
    private final ImmutableTable<Integer, Integer, String> simpleTable;
    /**
     * The compound table.
     */
    @PropertyDefinition
    private final ImmutableTable<Integer, Integer, ImmPerson> compoundTable;
    /**
     * The grid.
     */
    @PropertyDefinition
    private final ImmutableGrid<ImmPerson> sparseGrid;
    /**
     * The grid.
     */
    @PropertyDefinition
    @SimpleAnnotation(first = "a",
            second = "b",
            third = "c")
    private final ImmutableGrid<ImmPerson> denseGrid;
    /**
     * The bean key and bean value field.
     */
    @PropertyDefinition(validate = "notNull")
    // comment is ignored
    private final ImmutableMap<ImmPerson, ImmAddress> beanBeanMap;
    /**
     * The array.
     */
    @PropertyDefinition
    private final double[] doubleVector;
    /**
     * The matrix.
     */
    @PropertyDefinition

    private final double[][] matrix;
    // blank line above is ignored

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code ImmAddress}.
     * @return the meta-bean, not null
     */
    public static ImmAddress.Meta meta() {
        return ImmAddress.Meta.INSTANCE;
    }

    static {
        MetaBean.register(ImmAddress.Meta.INSTANCE);
    }

    /**
     * The serialization version id.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Returns a builder used to create an instance of the bean.
     * @return the builder, not null
     */
    public static ImmAddress.Builder builder() {
        return new ImmAddress.Builder();
    }

    private ImmAddress(
            int number,
            String street,
            String city,
            Number abstractNumber,
            byte[] data,
            String[][] array2d,
            ImmPerson owner,
            Object object1,
            Object object2,
            Risk risk,
            RiskLevel riskLevel,
            Set<RiskLevel> riskLevels,
            Serializable serializable,
            Map<String, Object> objectInMap,
            Map<String, List<String>> listInMap,
            Map<String, List<Integer>> listNumericInMap,
            Map<String, List<List<Integer>>> listInListInMap,
            Map<String, List<List<Object>>> objectListInListInMap,
            Map<ImmPerson, Map<String, ImmPerson>> mapInMap,
            Table<Integer, Integer, String> simpleTable,
            Table<Integer, Integer, ImmPerson> compoundTable,
            Grid<ImmPerson> sparseGrid,
            Grid<ImmPerson> denseGrid,
            Map<ImmPerson, ImmAddress> beanBeanMap,
            double[] doubleVector,
            double[][] matrix) {
        JodaBeanUtils.notNull(street, "street");
        JodaBeanUtils.notNull(city, "city");
        JodaBeanUtils.notNull(owner, "owner");
        JodaBeanUtils.notNull(objectInMap, "objectInMap");
        JodaBeanUtils.notNull(listInMap, "listInMap");
        JodaBeanUtils.notNull(listNumericInMap, "listNumericInMap");
        JodaBeanUtils.notNull(listInListInMap, "listInListInMap");
        JodaBeanUtils.notNull(objectListInListInMap, "objectListInListInMap");
        JodaBeanUtils.notNull(mapInMap, "mapInMap");
        JodaBeanUtils.notNull(beanBeanMap, "beanBeanMap");
        this.number = number;
        this.street = street;
        this.city = city;
        this.abstractNumber = abstractNumber;
        this.data = (data != null ? data.clone() : null);
        this.array2d = array2d;
        this.owner = owner;
        this.object1 = object1;
        this.object2 = object2;
        this.risk = risk;
        this.riskLevel = riskLevel;
        this.riskLevels = (riskLevels != null ? EnumSet.copyOf(riskLevels) : null);
        this.serializable = serializable;
        this.objectInMap = ImmutableMap.copyOf(objectInMap);
        this.listInMap = ImmutableMap.copyOf(listInMap);
        this.listNumericInMap = ImmutableMap.copyOf(listNumericInMap);
        this.listInListInMap = ImmutableMap.copyOf(listInListInMap);
        this.objectListInListInMap = ImmutableMap.copyOf(objectListInListInMap);
        this.mapInMap = ImmutableMap.copyOf(mapInMap);
        this.simpleTable = (simpleTable != null ? ImmutableTable.copyOf(simpleTable) : null);
        this.compoundTable = (compoundTable != null ? ImmutableTable.copyOf(compoundTable) : null);
        this.sparseGrid = (sparseGrid != null ? ImmutableGrid.copyOf(sparseGrid) : null);
        this.denseGrid = (denseGrid != null ? ImmutableGrid.copyOf(denseGrid) : null);
        this.beanBeanMap = ImmutableMap.copyOf(beanBeanMap);
        this.doubleVector = (doubleVector != null ? doubleVector.clone() : null);
        this.matrix = matrix;
    }

    @Override
    public ImmAddress.Meta metaBean() {
        return ImmAddress.Meta.INSTANCE;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number.
     * This will be the flat, house number or house name.
     * @return the value of the property
     */
    public int getNumber() {
        return number;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the street.
     * @return the value of the property, not null
     */
    public String getStreet() {
        return street;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the city.
     * @return the value of the property, not null
     */
    public String getCity() {
        return city;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the abstract number.
     * @return the value of the property
     */
    public Number getAbstractNumber() {
        return abstractNumber;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the extra data.
     * @return the value of the property
     */
    public byte[] getData() {
        return (data != null ? data.clone() : null);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the 2D array.
     * @return the value of the property
     */
    public String[][] getArray2d() {
        return array2d;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the owner.
     * @return the value of the property, not null
     */
    public ImmPerson getOwner() {
        return owner;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the object field.
     * @return the value of the property
     */
    public Object getObject1() {
        return object1;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the object field.
     * @return the value of the property
     */
    public Object getObject2() {
        return object2;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the risk field.
     * @return the value of the property
     */
    public Risk getRisk() {
        return risk;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the risk level field, testing an {@code Enum}.
     * @return the value of the property
     */
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the risk levels field, testing {@code EnumSet}.
     * @return the value of the property
     */
    public EnumSet<RiskLevel> getRiskLevels() {
        return riskLevels;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the serializable field.
     * @return the value of the property
     */
    public Serializable getSerializable() {
        return serializable;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the object in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<String, Object> getObjectInMap() {
        return objectInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the list in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<String, List<String>> getListInMap() {
        return listInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the list in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<String, List<Integer>> getListNumericInMap() {
        return listNumericInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the list in list in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<String, List<List<Integer>>> getListInListInMap() {
        return listInListInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the object list in list in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<String, List<List<Object>>> getObjectListInListInMap() {
        return objectListInListInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the map in map field.
     * @return the value of the property, not null
     */
    public ImmutableMap<ImmPerson, Map<String, ImmPerson>> getMapInMap() {
        return mapInMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the simple table.
     * @return the value of the property
     */
    public ImmutableTable<Integer, Integer, String> getSimpleTable() {
        return simpleTable;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the compound table.
     * @return the value of the property
     */
    public ImmutableTable<Integer, Integer, ImmPerson> getCompoundTable() {
        return compoundTable;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the grid.
     * @return the value of the property
     */
    public ImmutableGrid<ImmPerson> getSparseGrid() {
        return sparseGrid;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the grid.
     * @return the value of the property
     */
    public ImmutableGrid<ImmPerson> getDenseGrid() {
        return denseGrid;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean key and bean value field.
     * @return the value of the property, not null
     */
    public ImmutableMap<ImmPerson, ImmAddress> getBeanBeanMap() {
        return beanBeanMap;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the array.
     * @return the value of the property
     */
    public double[] getDoubleVector() {
        return (doubleVector != null ? doubleVector.clone() : null);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the matrix.
     * @return the value of the property
     */
    public double[][] getMatrix() {
        return matrix;
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a builder that allows this bean to be mutated.
     * @return the mutable builder, not null
     */
    public Builder toBuilder() {
        return new Builder(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            ImmAddress other = (ImmAddress) obj;
            return (number == other.number) &&
                    JodaBeanUtils.equal(street, other.street) &&
                    JodaBeanUtils.equal(city, other.city) &&
                    JodaBeanUtils.equal(abstractNumber, other.abstractNumber) &&
                    JodaBeanUtils.equal(data, other.data) &&
                    JodaBeanUtils.equal(array2d, other.array2d) &&
                    JodaBeanUtils.equal(owner, other.owner) &&
                    JodaBeanUtils.equal(object1, other.object1) &&
                    JodaBeanUtils.equal(object2, other.object2) &&
                    JodaBeanUtils.equal(risk, other.risk) &&
                    JodaBeanUtils.equal(riskLevel, other.riskLevel) &&
                    JodaBeanUtils.equal(riskLevels, other.riskLevels) &&
                    JodaBeanUtils.equal(serializable, other.serializable) &&
                    JodaBeanUtils.equal(objectInMap, other.objectInMap) &&
                    JodaBeanUtils.equal(listInMap, other.listInMap) &&
                    JodaBeanUtils.equal(listNumericInMap, other.listNumericInMap) &&
                    JodaBeanUtils.equal(listInListInMap, other.listInListInMap) &&
                    JodaBeanUtils.equal(objectListInListInMap, other.objectListInListInMap) &&
                    JodaBeanUtils.equal(mapInMap, other.mapInMap) &&
                    JodaBeanUtils.equal(simpleTable, other.simpleTable) &&
                    JodaBeanUtils.equal(compoundTable, other.compoundTable) &&
                    JodaBeanUtils.equal(sparseGrid, other.sparseGrid) &&
                    JodaBeanUtils.equal(denseGrid, other.denseGrid) &&
                    JodaBeanUtils.equal(beanBeanMap, other.beanBeanMap) &&
                    JodaBeanUtils.equal(doubleVector, other.doubleVector) &&
                    JodaBeanUtils.equal(matrix, other.matrix);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(number);
        hash = hash * 31 + JodaBeanUtils.hashCode(street);
        hash = hash * 31 + JodaBeanUtils.hashCode(city);
        hash = hash * 31 + JodaBeanUtils.hashCode(abstractNumber);
        hash = hash * 31 + JodaBeanUtils.hashCode(data);
        hash = hash * 31 + JodaBeanUtils.hashCode(array2d);
        hash = hash * 31 + JodaBeanUtils.hashCode(owner);
        hash = hash * 31 + JodaBeanUtils.hashCode(object1);
        hash = hash * 31 + JodaBeanUtils.hashCode(object2);
        hash = hash * 31 + JodaBeanUtils.hashCode(risk);
        hash = hash * 31 + JodaBeanUtils.hashCode(riskLevel);
        hash = hash * 31 + JodaBeanUtils.hashCode(riskLevels);
        hash = hash * 31 + JodaBeanUtils.hashCode(serializable);
        hash = hash * 31 + JodaBeanUtils.hashCode(objectInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(listInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(listNumericInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(listInListInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(objectListInListInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(mapInMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(simpleTable);
        hash = hash * 31 + JodaBeanUtils.hashCode(compoundTable);
        hash = hash * 31 + JodaBeanUtils.hashCode(sparseGrid);
        hash = hash * 31 + JodaBeanUtils.hashCode(denseGrid);
        hash = hash * 31 + JodaBeanUtils.hashCode(beanBeanMap);
        hash = hash * 31 + JodaBeanUtils.hashCode(doubleVector);
        hash = hash * 31 + JodaBeanUtils.hashCode(matrix);
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(864);
        buf.append("ImmAddress{");
        buf.append("number").append('=').append(number).append(',').append(' ');
        buf.append("street").append('=').append(street).append(',').append(' ');
        buf.append("city").append('=').append(city).append(',').append(' ');
        buf.append("abstractNumber").append('=').append(abstractNumber).append(',').append(' ');
        buf.append("data").append('=').append(data).append(',').append(' ');
        buf.append("array2d").append('=').append(array2d).append(',').append(' ');
        buf.append("owner").append('=').append(owner).append(',').append(' ');
        buf.append("object1").append('=').append(object1).append(',').append(' ');
        buf.append("object2").append('=').append(object2).append(',').append(' ');
        buf.append("risk").append('=').append(risk).append(',').append(' ');
        buf.append("riskLevel").append('=').append(riskLevel).append(',').append(' ');
        buf.append("riskLevels").append('=').append(riskLevels).append(',').append(' ');
        buf.append("serializable").append('=').append(serializable).append(',').append(' ');
        buf.append("objectInMap").append('=').append(objectInMap).append(',').append(' ');
        buf.append("listInMap").append('=').append(listInMap).append(',').append(' ');
        buf.append("listNumericInMap").append('=').append(listNumericInMap).append(',').append(' ');
        buf.append("listInListInMap").append('=').append(listInListInMap).append(',').append(' ');
        buf.append("objectListInListInMap").append('=').append(objectListInListInMap).append(',').append(' ');
        buf.append("mapInMap").append('=').append(mapInMap).append(',').append(' ');
        buf.append("simpleTable").append('=').append(simpleTable).append(',').append(' ');
        buf.append("compoundTable").append('=').append(compoundTable).append(',').append(' ');
        buf.append("sparseGrid").append('=').append(sparseGrid).append(',').append(' ');
        buf.append("denseGrid").append('=').append(denseGrid).append(',').append(' ');
        buf.append("beanBeanMap").append('=').append(beanBeanMap).append(',').append(' ');
        buf.append("doubleVector").append('=').append(doubleVector).append(',').append(' ');
        buf.append("matrix").append('=').append(JodaBeanUtils.toString(matrix));
        buf.append('}');
        return buf.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code ImmAddress}.
     */
    public static final class Meta extends DirectMetaBean {
        /**
         * The singleton instance of the meta-bean.
         */
        static final Meta INSTANCE = new Meta();

        /**
         * The meta-property for the {@code number} property.
         */
        private final MetaProperty<Integer> number = DirectMetaProperty.ofImmutable(
                this, "number", ImmAddress.class, Integer.TYPE);
        /**
         * The meta-property for the {@code street} property.
         */
        private final MetaProperty<String> street = DirectMetaProperty.ofImmutable(
                this, "street", ImmAddress.class, String.class);
        /**
         * The meta-property for the {@code city} property.
         */
        private final MetaProperty<String> city = DirectMetaProperty.ofImmutable(
                this, "city", ImmAddress.class, String.class);
        /**
         * The meta-property for the {@code abstractNumber} property.
         */
        private final MetaProperty<Number> abstractNumber = DirectMetaProperty.ofImmutable(
                this, "abstractNumber", ImmAddress.class, Number.class);
        /**
         * The meta-property for the {@code data} property.
         */
        private final MetaProperty<byte[]> data = DirectMetaProperty.ofImmutable(
                this, "data", ImmAddress.class, byte[].class);
        /**
         * The meta-property for the {@code array2d} property.
         */
        private final MetaProperty<String[][]> array2d = DirectMetaProperty.ofImmutable(
                this, "array2d", ImmAddress.class, String[][].class);
        /**
         * The meta-property for the {@code owner} property.
         */
        private final MetaProperty<ImmPerson> owner = DirectMetaProperty.ofImmutable(
                this, "owner", ImmAddress.class, ImmPerson.class);
        /**
         * The meta-property for the {@code object1} property.
         */
        private final MetaProperty<Object> object1 = DirectMetaProperty.ofImmutable(
                this, "object1", ImmAddress.class, Object.class);
        /**
         * The meta-property for the {@code object2} property.
         */
        private final MetaProperty<Object> object2 = DirectMetaProperty.ofImmutable(
                this, "object2", ImmAddress.class, Object.class);
        /**
         * The meta-property for the {@code risk} property.
         */
        private final MetaProperty<Risk> risk = DirectMetaProperty.ofImmutable(
                this, "risk", ImmAddress.class, Risk.class);
        /**
         * The meta-property for the {@code riskLevel} property.
         */
        private final MetaProperty<RiskLevel> riskLevel = DirectMetaProperty.ofImmutable(
                this, "riskLevel", ImmAddress.class, RiskLevel.class);
        /**
         * The meta-property for the {@code riskLevels} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<EnumSet<RiskLevel>> riskLevels = DirectMetaProperty.ofImmutable(
                this, "riskLevels", ImmAddress.class, (Class) EnumSet.class);
        /**
         * The meta-property for the {@code serializable} property.
         */
        private final MetaProperty<Serializable> serializable = DirectMetaProperty.ofImmutable(
                this, "serializable", ImmAddress.class, Serializable.class);
        /**
         * The meta-property for the {@code objectInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<String, Object>> objectInMap = DirectMetaProperty.ofImmutable(
                this, "objectInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code listInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<String, List<String>>> listInMap = DirectMetaProperty.ofImmutable(
                this, "listInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code listNumericInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<String, List<Integer>>> listNumericInMap = DirectMetaProperty.ofImmutable(
                this, "listNumericInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code listInListInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<String, List<List<Integer>>>> listInListInMap = DirectMetaProperty.ofImmutable(
                this, "listInListInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code objectListInListInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<String, List<List<Object>>>> objectListInListInMap = DirectMetaProperty.ofImmutable(
                this, "objectListInListInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code mapInMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<ImmPerson, Map<String, ImmPerson>>> mapInMap = DirectMetaProperty.ofImmutable(
                this, "mapInMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code simpleTable} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableTable<Integer, Integer, String>> simpleTable = DirectMetaProperty.ofImmutable(
                this, "simpleTable", ImmAddress.class, (Class) ImmutableTable.class);
        /**
         * The meta-property for the {@code compoundTable} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableTable<Integer, Integer, ImmPerson>> compoundTable = DirectMetaProperty.ofImmutable(
                this, "compoundTable", ImmAddress.class, (Class) ImmutableTable.class);
        /**
         * The meta-property for the {@code sparseGrid} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableGrid<ImmPerson>> sparseGrid = DirectMetaProperty.ofImmutable(
                this, "sparseGrid", ImmAddress.class, (Class) ImmutableGrid.class);
        /**
         * The meta-property for the {@code denseGrid} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableGrid<ImmPerson>> denseGrid = DirectMetaProperty.ofImmutable(
                this, "denseGrid", ImmAddress.class, (Class) ImmutableGrid.class);
        /**
         * The meta-property for the {@code beanBeanMap} property.
         */
        @SuppressWarnings({"unchecked", "rawtypes" })
        private final MetaProperty<ImmutableMap<ImmPerson, ImmAddress>> beanBeanMap = DirectMetaProperty.ofImmutable(
                this, "beanBeanMap", ImmAddress.class, (Class) ImmutableMap.class);
        /**
         * The meta-property for the {@code doubleVector} property.
         */
        private final MetaProperty<double[]> doubleVector = DirectMetaProperty.ofImmutable(
                this, "doubleVector", ImmAddress.class, double[].class);
        /**
         * The meta-property for the {@code matrix} property.
         */
        private final MetaProperty<double[][]> matrix = DirectMetaProperty.ofImmutable(
                this, "matrix", ImmAddress.class, double[][].class);
        /**
         * The meta-properties.
         */
        private final Map<String, MetaProperty<?>> metaPropertyMap$ = new DirectMetaPropertyMap(
                this, null,
                "number",
                "street",
                "city",
                "abstractNumber",
                "data",
                "array2d",
                "owner",
                "object1",
                "object2",
                "risk",
                "riskLevel",
                "riskLevels",
                "serializable",
                "objectInMap",
                "listInMap",
                "listNumericInMap",
                "listInListInMap",
                "objectListInListInMap",
                "mapInMap",
                "simpleTable",
                "compoundTable",
                "sparseGrid",
                "denseGrid",
                "beanBeanMap",
                "doubleVector",
                "matrix");

        /**
         * Restricted constructor.
         */
        private Meta() {
        }

        @Override
        protected MetaProperty<?> metaPropertyGet(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1034364087:  // number
                    return number;
                case -891990013:  // street
                    return street;
                case 3053931:  // city
                    return city;
                case 1986500107:  // abstractNumber
                    return abstractNumber;
                case 3076010:  // data
                    return data;
                case -734443893:  // array2d
                    return array2d;
                case 106164915:  // owner
                    return owner;
                case -1659648814:  // object1
                    return object1;
                case -1659648813:  // object2
                    return object2;
                case 3500751:  // risk
                    return risk;
                case 540453365:  // riskLevel
                    return riskLevel;
                case -425814754:  // riskLevels
                    return riskLevels;
                case 861034751:  // serializable
                    return serializable;
                case -1297715720:  // objectInMap
                    return objectInMap;
                case -1244601351:  // listInMap
                    return listInMap;
                case 391098024:  // listNumericInMap
                    return listNumericInMap;
                case -940836650:  // listInListInMap
                    return listInListInMap;
                case -861321321:  // objectListInListInMap
                    return objectListInListInMap;
                case 158545403:  // mapInMap
                    return mapInMap;
                case -1429579460:  // simpleTable
                    return simpleTable;
                case 103339235:  // compoundTable
                    return compoundTable;
                case 1337284998:  // sparseGrid
                    return sparseGrid;
                case 1802377989:  // denseGrid
                    return denseGrid;
                case -2039203396:  // beanBeanMap
                    return beanBeanMap;
                case 1118070900:  // doubleVector
                    return doubleVector;
                case -1081239615:  // matrix
                    return matrix;
            }
            return super.metaPropertyGet(propertyName);
        }

        @Override
        public ImmAddress.Builder builder() {
            return new ImmAddress.Builder();
        }

        @Override
        public Class<? extends ImmAddress> beanType() {
            return ImmAddress.class;
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            return metaPropertyMap$;
        }

        //-----------------------------------------------------------------------
        /**
         * The meta-property for the {@code number} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Integer> number() {
            return number;
        }

        /**
         * The meta-property for the {@code street} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> street() {
            return street;
        }

        /**
         * The meta-property for the {@code city} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String> city() {
            return city;
        }

        /**
         * The meta-property for the {@code abstractNumber} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Number> abstractNumber() {
            return abstractNumber;
        }

        /**
         * The meta-property for the {@code data} property.
         * @return the meta-property, not null
         */
        public MetaProperty<byte[]> data() {
            return data;
        }

        /**
         * The meta-property for the {@code array2d} property.
         * @return the meta-property, not null
         */
        public MetaProperty<String[][]> array2d() {
            return array2d;
        }

        /**
         * The meta-property for the {@code owner} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmPerson> owner() {
            return owner;
        }

        /**
         * The meta-property for the {@code object1} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Object> object1() {
            return object1;
        }

        /**
         * The meta-property for the {@code object2} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Object> object2() {
            return object2;
        }

        /**
         * The meta-property for the {@code risk} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Risk> risk() {
            return risk;
        }

        /**
         * The meta-property for the {@code riskLevel} property.
         * @return the meta-property, not null
         */
        public MetaProperty<RiskLevel> riskLevel() {
            return riskLevel;
        }

        /**
         * The meta-property for the {@code riskLevels} property.
         * @return the meta-property, not null
         */
        public MetaProperty<EnumSet<RiskLevel>> riskLevels() {
            return riskLevels;
        }

        /**
         * The meta-property for the {@code serializable} property.
         * @return the meta-property, not null
         */
        public MetaProperty<Serializable> serializable() {
            return serializable;
        }

        /**
         * The meta-property for the {@code objectInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<String, Object>> objectInMap() {
            return objectInMap;
        }

        /**
         * The meta-property for the {@code listInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<String, List<String>>> listInMap() {
            return listInMap;
        }

        /**
         * The meta-property for the {@code listNumericInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<String, List<Integer>>> listNumericInMap() {
            return listNumericInMap;
        }

        /**
         * The meta-property for the {@code listInListInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<String, List<List<Integer>>>> listInListInMap() {
            return listInListInMap;
        }

        /**
         * The meta-property for the {@code objectListInListInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<String, List<List<Object>>>> objectListInListInMap() {
            return objectListInListInMap;
        }

        /**
         * The meta-property for the {@code mapInMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<ImmPerson, Map<String, ImmPerson>>> mapInMap() {
            return mapInMap;
        }

        /**
         * The meta-property for the {@code simpleTable} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableTable<Integer, Integer, String>> simpleTable() {
            return simpleTable;
        }

        /**
         * The meta-property for the {@code compoundTable} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableTable<Integer, Integer, ImmPerson>> compoundTable() {
            return compoundTable;
        }

        /**
         * The meta-property for the {@code sparseGrid} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableGrid<ImmPerson>> sparseGrid() {
            return sparseGrid;
        }

        /**
         * The meta-property for the {@code denseGrid} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableGrid<ImmPerson>> denseGrid() {
            return denseGrid;
        }

        /**
         * The meta-property for the {@code beanBeanMap} property.
         * @return the meta-property, not null
         */
        public MetaProperty<ImmutableMap<ImmPerson, ImmAddress>> beanBeanMap() {
            return beanBeanMap;
        }

        /**
         * The meta-property for the {@code doubleVector} property.
         * @return the meta-property, not null
         */
        public MetaProperty<double[]> doubleVector() {
            return doubleVector;
        }

        /**
         * The meta-property for the {@code matrix} property.
         * @return the meta-property, not null
         */
        public MetaProperty<double[][]> matrix() {
            return matrix;
        }

        //-----------------------------------------------------------------------
        @Override
        protected Object propertyGet(Bean bean, String propertyName, boolean quiet) {
            switch (propertyName.hashCode()) {
                case -1034364087:  // number
                    return ((ImmAddress) bean).getNumber();
                case -891990013:  // street
                    return ((ImmAddress) bean).getStreet();
                case 3053931:  // city
                    return ((ImmAddress) bean).getCity();
                case 1986500107:  // abstractNumber
                    return ((ImmAddress) bean).getAbstractNumber();
                case 3076010:  // data
                    return ((ImmAddress) bean).getData();
                case -734443893:  // array2d
                    return ((ImmAddress) bean).getArray2d();
                case 106164915:  // owner
                    return ((ImmAddress) bean).getOwner();
                case -1659648814:  // object1
                    return ((ImmAddress) bean).getObject1();
                case -1659648813:  // object2
                    return ((ImmAddress) bean).getObject2();
                case 3500751:  // risk
                    return ((ImmAddress) bean).getRisk();
                case 540453365:  // riskLevel
                    return ((ImmAddress) bean).getRiskLevel();
                case -425814754:  // riskLevels
                    return ((ImmAddress) bean).getRiskLevels();
                case 861034751:  // serializable
                    return ((ImmAddress) bean).getSerializable();
                case -1297715720:  // objectInMap
                    return ((ImmAddress) bean).getObjectInMap();
                case -1244601351:  // listInMap
                    return ((ImmAddress) bean).getListInMap();
                case 391098024:  // listNumericInMap
                    return ((ImmAddress) bean).getListNumericInMap();
                case -940836650:  // listInListInMap
                    return ((ImmAddress) bean).getListInListInMap();
                case -861321321:  // objectListInListInMap
                    return ((ImmAddress) bean).getObjectListInListInMap();
                case 158545403:  // mapInMap
                    return ((ImmAddress) bean).getMapInMap();
                case -1429579460:  // simpleTable
                    return ((ImmAddress) bean).getSimpleTable();
                case 103339235:  // compoundTable
                    return ((ImmAddress) bean).getCompoundTable();
                case 1337284998:  // sparseGrid
                    return ((ImmAddress) bean).getSparseGrid();
                case 1802377989:  // denseGrid
                    return ((ImmAddress) bean).getDenseGrid();
                case -2039203396:  // beanBeanMap
                    return ((ImmAddress) bean).getBeanBeanMap();
                case 1118070900:  // doubleVector
                    return ((ImmAddress) bean).getDoubleVector();
                case -1081239615:  // matrix
                    return ((ImmAddress) bean).getMatrix();
            }
            return super.propertyGet(bean, propertyName, quiet);
        }

        @Override
        protected void propertySet(Bean bean, String propertyName, Object newValue, boolean quiet) {
            metaProperty(propertyName);
            if (quiet) {
                return;
            }
            throw new UnsupportedOperationException("Property cannot be written: " + propertyName);
        }

    }

    //-----------------------------------------------------------------------
    /**
     * The bean-builder for {@code ImmAddress}.
     */
    public static final class Builder extends DirectFieldsBeanBuilder<ImmAddress> {

        private int number;
        private String street;
        private String city;
        private Number abstractNumber;
        private byte[] data;
        private String[][] array2d;
        private ImmPerson owner;
        private Object object1;
        private Object object2;
        private Risk risk;
        private RiskLevel riskLevel;
        private Set<RiskLevel> riskLevels;
        private Serializable serializable;
        private Map<String, Object> objectInMap = ImmutableMap.of();
        private Map<String, List<String>> listInMap = ImmutableMap.of();
        private Map<String, List<Integer>> listNumericInMap = ImmutableMap.of();
        private Map<String, List<List<Integer>>> listInListInMap = ImmutableMap.of();
        private Map<String, List<List<Object>>> objectListInListInMap = ImmutableMap.of();
        private Map<ImmPerson, Map<String, ImmPerson>> mapInMap = ImmutableMap.of();
        private Table<Integer, Integer, String> simpleTable;
        private Table<Integer, Integer, ImmPerson> compoundTable;
        private Grid<ImmPerson> sparseGrid;
        private Grid<ImmPerson> denseGrid;
        private Map<ImmPerson, ImmAddress> beanBeanMap = ImmutableMap.of();
        private double[] doubleVector;
        private double[][] matrix;

        /**
         * Restricted constructor.
         */
        private Builder() {
        }

        /**
         * Restricted copy constructor.
         * @param beanToCopy  the bean to copy from, not null
         */
        private Builder(ImmAddress beanToCopy) {
            this.number = beanToCopy.getNumber();
            this.street = beanToCopy.getStreet();
            this.city = beanToCopy.getCity();
            this.abstractNumber = beanToCopy.getAbstractNumber();
            this.data = (beanToCopy.getData() != null ? beanToCopy.getData().clone() : null);
            this.array2d = beanToCopy.getArray2d();
            this.owner = beanToCopy.getOwner();
            this.object1 = beanToCopy.getObject1();
            this.object2 = beanToCopy.getObject2();
            this.risk = beanToCopy.getRisk();
            this.riskLevel = beanToCopy.getRiskLevel();
            this.riskLevels = (beanToCopy.getRiskLevels() != null ? new HashSet<>(beanToCopy.getRiskLevels()) : null);
            this.serializable = beanToCopy.getSerializable();
            this.objectInMap = beanToCopy.getObjectInMap();
            this.listInMap = beanToCopy.getListInMap();
            this.listNumericInMap = beanToCopy.getListNumericInMap();
            this.listInListInMap = beanToCopy.getListInListInMap();
            this.objectListInListInMap = beanToCopy.getObjectListInListInMap();
            this.mapInMap = beanToCopy.getMapInMap();
            this.simpleTable = beanToCopy.getSimpleTable();
            this.compoundTable = beanToCopy.getCompoundTable();
            this.sparseGrid = beanToCopy.getSparseGrid();
            this.denseGrid = beanToCopy.getDenseGrid();
            this.beanBeanMap = beanToCopy.getBeanBeanMap();
            this.doubleVector = (beanToCopy.getDoubleVector() != null ? beanToCopy.getDoubleVector().clone() : null);
            this.matrix = beanToCopy.getMatrix();
        }

        //-----------------------------------------------------------------------
        @Override
        public Object get(String propertyName) {
            switch (propertyName.hashCode()) {
                case -1034364087:  // number
                    return number;
                case -891990013:  // street
                    return street;
                case 3053931:  // city
                    return city;
                case 1986500107:  // abstractNumber
                    return abstractNumber;
                case 3076010:  // data
                    return data;
                case -734443893:  // array2d
                    return array2d;
                case 106164915:  // owner
                    return owner;
                case -1659648814:  // object1
                    return object1;
                case -1659648813:  // object2
                    return object2;
                case 3500751:  // risk
                    return risk;
                case 540453365:  // riskLevel
                    return riskLevel;
                case -425814754:  // riskLevels
                    return riskLevels;
                case 861034751:  // serializable
                    return serializable;
                case -1297715720:  // objectInMap
                    return objectInMap;
                case -1244601351:  // listInMap
                    return listInMap;
                case 391098024:  // listNumericInMap
                    return listNumericInMap;
                case -940836650:  // listInListInMap
                    return listInListInMap;
                case -861321321:  // objectListInListInMap
                    return objectListInListInMap;
                case 158545403:  // mapInMap
                    return mapInMap;
                case -1429579460:  // simpleTable
                    return simpleTable;
                case 103339235:  // compoundTable
                    return compoundTable;
                case 1337284998:  // sparseGrid
                    return sparseGrid;
                case 1802377989:  // denseGrid
                    return denseGrid;
                case -2039203396:  // beanBeanMap
                    return beanBeanMap;
                case 1118070900:  // doubleVector
                    return doubleVector;
                case -1081239615:  // matrix
                    return matrix;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public Builder set(String propertyName, Object newValue) {
            switch (propertyName.hashCode()) {
                case -1034364087:  // number
                    this.number = (Integer) newValue;
                    break;
                case -891990013:  // street
                    this.street = (String) newValue;
                    break;
                case 3053931:  // city
                    this.city = (String) newValue;
                    break;
                case 1986500107:  // abstractNumber
                    this.abstractNumber = (Number) newValue;
                    break;
                case 3076010:  // data
                    this.data = (byte[]) newValue;
                    break;
                case -734443893:  // array2d
                    this.array2d = (String[][]) newValue;
                    break;
                case 106164915:  // owner
                    this.owner = (ImmPerson) newValue;
                    break;
                case -1659648814:  // object1
                    this.object1 = (Object) newValue;
                    break;
                case -1659648813:  // object2
                    this.object2 = (Object) newValue;
                    break;
                case 3500751:  // risk
                    this.risk = (Risk) newValue;
                    break;
                case 540453365:  // riskLevel
                    this.riskLevel = (RiskLevel) newValue;
                    break;
                case -425814754:  // riskLevels
                    this.riskLevels = (Set<RiskLevel>) newValue;
                    break;
                case 861034751:  // serializable
                    this.serializable = (Serializable) newValue;
                    break;
                case -1297715720:  // objectInMap
                    this.objectInMap = (Map<String, Object>) newValue;
                    break;
                case -1244601351:  // listInMap
                    this.listInMap = (Map<String, List<String>>) newValue;
                    break;
                case 391098024:  // listNumericInMap
                    this.listNumericInMap = (Map<String, List<Integer>>) newValue;
                    break;
                case -940836650:  // listInListInMap
                    this.listInListInMap = (Map<String, List<List<Integer>>>) newValue;
                    break;
                case -861321321:  // objectListInListInMap
                    this.objectListInListInMap = (Map<String, List<List<Object>>>) newValue;
                    break;
                case 158545403:  // mapInMap
                    this.mapInMap = (Map<ImmPerson, Map<String, ImmPerson>>) newValue;
                    break;
                case -1429579460:  // simpleTable
                    this.simpleTable = (Table<Integer, Integer, String>) newValue;
                    break;
                case 103339235:  // compoundTable
                    this.compoundTable = (Table<Integer, Integer, ImmPerson>) newValue;
                    break;
                case 1337284998:  // sparseGrid
                    this.sparseGrid = (Grid<ImmPerson>) newValue;
                    break;
                case 1802377989:  // denseGrid
                    this.denseGrid = (Grid<ImmPerson>) newValue;
                    break;
                case -2039203396:  // beanBeanMap
                    this.beanBeanMap = (Map<ImmPerson, ImmAddress>) newValue;
                    break;
                case 1118070900:  // doubleVector
                    this.doubleVector = (double[]) newValue;
                    break;
                case -1081239615:  // matrix
                    this.matrix = (double[][]) newValue;
                    break;
                default:
                    throw new NoSuchElementException("Unknown property: " + propertyName);
            }
            return this;
        }

        @Override
        public Builder set(MetaProperty<?> property, Object value) {
            super.set(property, value);
            return this;
        }

        @Override
        public ImmAddress build() {
            return new ImmAddress(
                    number,
                    street,
                    city,
                    abstractNumber,
                    data,
                    array2d,
                    owner,
                    object1,
                    object2,
                    risk,
                    riskLevel,
                    riskLevels,
                    serializable,
                    objectInMap,
                    listInMap,
                    listNumericInMap,
                    listInListInMap,
                    objectListInListInMap,
                    mapInMap,
                    simpleTable,
                    compoundTable,
                    sparseGrid,
                    denseGrid,
                    beanBeanMap,
                    doubleVector,
                    matrix);
        }

        //-----------------------------------------------------------------------
        /**
         * Sets the number.
         * This will be the flat, house number or house name.
         * @param number  the new value
         * @return this, for chaining, not null
         */
        public Builder number(int number) {
            this.number = number;
            return this;
        }

        /**
         * Sets the street.
         * @param street  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder street(String street) {
            JodaBeanUtils.notNull(street, "street");
            this.street = street;
            return this;
        }

        /**
         * Sets the city.
         * @param city  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder city(String city) {
            JodaBeanUtils.notNull(city, "city");
            this.city = city;
            return this;
        }

        /**
         * Sets the abstract number.
         * @param abstractNumber  the new value
         * @return this, for chaining, not null
         */
        public Builder abstractNumber(Number abstractNumber) {
            this.abstractNumber = abstractNumber;
            return this;
        }

        /**
         * Sets the extra data.
         * @param data  the new value
         * @return this, for chaining, not null
         */
        public Builder data(byte[] data) {
            this.data = data;
            return this;
        }

        /**
         * Sets the 2D array.
         * @param array2d  the new value
         * @return this, for chaining, not null
         */
        public Builder array2d(String[][] array2d) {
            this.array2d = array2d;
            return this;
        }

        /**
         * Sets the owner.
         * @param owner  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder owner(ImmPerson owner) {
            JodaBeanUtils.notNull(owner, "owner");
            this.owner = owner;
            return this;
        }

        /**
         * Sets the object field.
         * @param object1  the new value
         * @return this, for chaining, not null
         */
        public Builder object1(Object object1) {
            this.object1 = object1;
            return this;
        }

        /**
         * Sets the object field.
         * @param object2  the new value
         * @return this, for chaining, not null
         */
        public Builder object2(Object object2) {
            this.object2 = object2;
            return this;
        }

        /**
         * Sets the risk field.
         * @param risk  the new value
         * @return this, for chaining, not null
         */
        public Builder risk(Risk risk) {
            this.risk = risk;
            return this;
        }

        /**
         * Sets the risk level field, testing an {@code Enum}.
         * @param riskLevel  the new value
         * @return this, for chaining, not null
         */
        public Builder riskLevel(RiskLevel riskLevel) {
            this.riskLevel = riskLevel;
            return this;
        }

        /**
         * Sets the risk levels field, testing {@code EnumSet}.
         * @param riskLevels  the new value
         * @return this, for chaining, not null
         */
        public Builder riskLevels(Set<RiskLevel> riskLevels) {
            this.riskLevels = riskLevels;
            return this;
        }

        /**
         * Sets the {@code riskLevels} property in the builder
         * from an array of objects.
         * @param riskLevels  the new value
         * @return this, for chaining, not null
         */
        public Builder riskLevels(RiskLevel... riskLevels) {
            return riskLevels(EnumSet.copyOf(Arrays.asList(riskLevels)));
        }

        /**
         * Sets the serializable field.
         * @param serializable  the new value
         * @return this, for chaining, not null
         */
        public Builder serializable(Serializable serializable) {
            this.serializable = serializable;
            return this;
        }

        /**
         * Sets the object in map field.
         * @param objectInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder objectInMap(Map<String, Object> objectInMap) {
            JodaBeanUtils.notNull(objectInMap, "objectInMap");
            this.objectInMap = objectInMap;
            return this;
        }

        /**
         * Sets the list in map field.
         * @param listInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder listInMap(Map<String, List<String>> listInMap) {
            JodaBeanUtils.notNull(listInMap, "listInMap");
            this.listInMap = listInMap;
            return this;
        }

        /**
         * Sets the list in map field.
         * @param listNumericInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder listNumericInMap(Map<String, List<Integer>> listNumericInMap) {
            JodaBeanUtils.notNull(listNumericInMap, "listNumericInMap");
            this.listNumericInMap = listNumericInMap;
            return this;
        }

        /**
         * Sets the list in list in map field.
         * @param listInListInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder listInListInMap(Map<String, List<List<Integer>>> listInListInMap) {
            JodaBeanUtils.notNull(listInListInMap, "listInListInMap");
            this.listInListInMap = listInListInMap;
            return this;
        }

        /**
         * Sets the object list in list in map field.
         * @param objectListInListInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder objectListInListInMap(Map<String, List<List<Object>>> objectListInListInMap) {
            JodaBeanUtils.notNull(objectListInListInMap, "objectListInListInMap");
            this.objectListInListInMap = objectListInListInMap;
            return this;
        }

        /**
         * Sets the map in map field.
         * @param mapInMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder mapInMap(Map<ImmPerson, Map<String, ImmPerson>> mapInMap) {
            JodaBeanUtils.notNull(mapInMap, "mapInMap");
            this.mapInMap = mapInMap;
            return this;
        }

        /**
         * Sets the simple table.
         * @param simpleTable  the new value
         * @return this, for chaining, not null
         */
        public Builder simpleTable(Table<Integer, Integer, String> simpleTable) {
            this.simpleTable = simpleTable;
            return this;
        }

        /**
         * Sets the compound table.
         * @param compoundTable  the new value
         * @return this, for chaining, not null
         */
        public Builder compoundTable(Table<Integer, Integer, ImmPerson> compoundTable) {
            this.compoundTable = compoundTable;
            return this;
        }

        /**
         * Sets the grid.
         * @param sparseGrid  the new value
         * @return this, for chaining, not null
         */
        public Builder sparseGrid(Grid<ImmPerson> sparseGrid) {
            this.sparseGrid = sparseGrid;
            return this;
        }

        /**
         * Sets the grid.
         * @param denseGrid  the new value
         * @return this, for chaining, not null
         */
        public Builder denseGrid(Grid<ImmPerson> denseGrid) {
            this.denseGrid = denseGrid;
            return this;
        }

        /**
         * Sets the bean key and bean value field.
         * @param beanBeanMap  the new value, not null
         * @return this, for chaining, not null
         */
        public Builder beanBeanMap(Map<ImmPerson, ImmAddress> beanBeanMap) {
            JodaBeanUtils.notNull(beanBeanMap, "beanBeanMap");
            this.beanBeanMap = beanBeanMap;
            return this;
        }

        /**
         * Sets the array.
         * @param doubleVector  the new value
         * @return this, for chaining, not null
         */
        public Builder doubleVector(double... doubleVector) {
            this.doubleVector = doubleVector;
            return this;
        }

        /**
         * Sets the matrix.
         * @param matrix  the new value
         * @return this, for chaining, not null
         */
        public Builder matrix(double[][] matrix) {
            this.matrix = matrix;
            return this;
        }

        //-----------------------------------------------------------------------
        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder(864);
            buf.append("ImmAddress.Builder{");
            buf.append("number").append('=').append(JodaBeanUtils.toString(number)).append(',').append(' ');
            buf.append("street").append('=').append(JodaBeanUtils.toString(street)).append(',').append(' ');
            buf.append("city").append('=').append(JodaBeanUtils.toString(city)).append(',').append(' ');
            buf.append("abstractNumber").append('=').append(JodaBeanUtils.toString(abstractNumber)).append(',').append(' ');
            buf.append("data").append('=').append(JodaBeanUtils.toString(data)).append(',').append(' ');
            buf.append("array2d").append('=').append(JodaBeanUtils.toString(array2d)).append(',').append(' ');
            buf.append("owner").append('=').append(JodaBeanUtils.toString(owner)).append(',').append(' ');
            buf.append("object1").append('=').append(JodaBeanUtils.toString(object1)).append(',').append(' ');
            buf.append("object2").append('=').append(JodaBeanUtils.toString(object2)).append(',').append(' ');
            buf.append("risk").append('=').append(JodaBeanUtils.toString(risk)).append(',').append(' ');
            buf.append("riskLevel").append('=').append(JodaBeanUtils.toString(riskLevel)).append(',').append(' ');
            buf.append("riskLevels").append('=').append(JodaBeanUtils.toString(riskLevels)).append(',').append(' ');
            buf.append("serializable").append('=').append(JodaBeanUtils.toString(serializable)).append(',').append(' ');
            buf.append("objectInMap").append('=').append(JodaBeanUtils.toString(objectInMap)).append(',').append(' ');
            buf.append("listInMap").append('=').append(JodaBeanUtils.toString(listInMap)).append(',').append(' ');
            buf.append("listNumericInMap").append('=').append(JodaBeanUtils.toString(listNumericInMap)).append(',').append(' ');
            buf.append("listInListInMap").append('=').append(JodaBeanUtils.toString(listInListInMap)).append(',').append(' ');
            buf.append("objectListInListInMap").append('=').append(JodaBeanUtils.toString(objectListInListInMap)).append(',').append(' ');
            buf.append("mapInMap").append('=').append(JodaBeanUtils.toString(mapInMap)).append(',').append(' ');
            buf.append("simpleTable").append('=').append(JodaBeanUtils.toString(simpleTable)).append(',').append(' ');
            buf.append("compoundTable").append('=').append(JodaBeanUtils.toString(compoundTable)).append(',').append(' ');
            buf.append("sparseGrid").append('=').append(JodaBeanUtils.toString(sparseGrid)).append(',').append(' ');
            buf.append("denseGrid").append('=').append(JodaBeanUtils.toString(denseGrid)).append(',').append(' ');
            buf.append("beanBeanMap").append('=').append(JodaBeanUtils.toString(beanBeanMap)).append(',').append(' ');
            buf.append("doubleVector").append('=').append(JodaBeanUtils.toString(doubleVector)).append(',').append(' ');
            buf.append("matrix").append('=').append(JodaBeanUtils.toString(matrix));
            buf.append('}');
            return buf.toString();
        }

    }

    //-------------------------- AUTOGENERATED END --------------------------
}
