/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.reflection.ReflectiveMetaBean;

/**
 * Mock address JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
public class Address extends DirectBean<Address> {

    /** The number. */
    @PropertyDefinition
    private int number;
    /** The street. */
    @PropertyDefinition
    private String street;
    /** The city. */
    @PropertyDefinition
    private String city;

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-property for the {@code number} property.
     */
    private static final MetaProperty<Address, Integer> NUMBER = DirectMetaProperty.ofReadWrite(Address.class, "number", Integer.TYPE);
    /**
     * The meta-property for the {@code street} property.
     */
    private static final MetaProperty<Address, String> STREET = DirectMetaProperty.ofReadWrite(Address.class, "street", String.class);
    /**
     * The meta-property for the {@code city} property.
     */
    private static final MetaProperty<Address, String> CITY = DirectMetaProperty.ofReadWrite(Address.class, "city", String.class);
    /**
     * The meta-bean for {@code Address}.
     */
    private static final MetaBean<Address> META = ReflectiveMetaBean.of(Address.class);

    //-----------------------------------------------------------------------
    /**
     * The meta-bean for {@code Address}.
     */
    public static final MetaBean<Address> meta() {
        return META;
    }

    @Override
    public final MetaBean<Address> metaBean() {
        return META;
    }

    @Override
    protected Object propertyGet(String propertyName) {
        switch (propertyName.hashCode()) {
            case -1034364087:  // number
                return getNumber();
            case -891990013:  // street
                return getStreet();
            case 3053931:  // city
                return getCity();
        }
        return super.propertyGet(propertyName);
    }

    @Override
    protected void propertySet(String propertyName, Object newValue) {
        switch (propertyName.hashCode()) {
            case -1034364087:  // number
                setNumber((int) (Integer) newValue);
                return;
            case -891990013:  // street
                setStreet((String) newValue);
                return;
            case 3053931:  // city
                setCity((String) newValue);
                return;
        }
        super.propertySet(propertyName, newValue);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number.
     * @return the value of the property
     */
    public int getNumber() {
        return number;
    }

    /**
     * Sets the number.
     * @param number  the new value of the property
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * Gets the the {@code number} property.
     * @return the property, not null
     */
    public final Property<Address, Integer> number() {
        return NUMBER.createProperty(this);
    }

    /**
     * The meta-property for the {@code number} property.
     */
    public static final MetaProperty<Address, Integer> numberMeta() {
        return NUMBER;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the street.
     * @return the value of the property
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street.
     * @param street  the new value of the property
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Gets the the {@code street} property.
     * @return the property, not null
     */
    public final Property<Address, String> street() {
        return STREET.createProperty(this);
    }

    /**
     * The meta-property for the {@code street} property.
     */
    public static final MetaProperty<Address, String> streetMeta() {
        return STREET;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the city.
     * @return the value of the property
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city  the new value of the property
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Gets the the {@code city} property.
     * @return the property, not null
     */
    public final Property<Address, String> city() {
        return CITY.createProperty(this);
    }

    /**
     * The meta-property for the {@code city} property.
     */
    public static final MetaProperty<Address, String> cityMeta() {
        return CITY;
    }

    //-------------------------- AUTOGENERATED END --------------------------
}