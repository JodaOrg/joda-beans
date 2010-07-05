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

import java.util.NoSuchElementException;

import org.joda.beans.impl.direct.DirectBean;
import org.joda.beans.impl.direct.DirectMetaProperty;
import org.joda.beans.impl.reflection.ReflectiveMetaBean;

/**
 * Mock address JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
public class Address extends DirectBean<Address> {

    /** The number meta-property. */
    public static final MetaProperty<Address, Integer> NUMBER = DirectMetaProperty.ofReadWrite(Address.class, "number", Integer.TYPE);
    /** The number. */
    private int number;

    /** The street meta-property. */
    public static final MetaProperty<Address, String> STREET = DirectMetaProperty.ofReadWrite(Address.class, "street", String.class);
    /** The street. */
    private String street;

    /** The city meta-property. */
    public static final MetaProperty<Address, String> CITY = DirectMetaProperty.ofReadWrite(Address.class, "city", String.class);
    /** The city. */
    private String city;

    /** The meta-bean. */
    public static final MetaBean<Address> META = ReflectiveMetaBean.of(Address.class);

    //-----------------------------------------------------------------------
    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @param number  the number to set
     */
    public void setNumber(int number) {
        this.number = number;
    }

    /**
     * @return the number property
     */
    public Property<Address, Integer> number() {
        return NUMBER.createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * @param street  the street to set
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * @return the street property
     */
    public Property<Address, String> street() {
        return STREET.createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @param city  the city to set
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * @return the city property
     */
    public Property<Address, String> city() {
        return CITY.createProperty(this);
    }

    //-----------------------------------------------------------------------
    public MetaBean<Address> metaBean() {
        return META;
    }

    //-----------------------------------------------------------------------
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
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    @Override
    protected void propertySet(String propertyName, Object value) {
        switch (propertyName.hashCode()) {
            case -1034364087:  // number
                setNumber((int) (Integer) value);
                return;
            case -891990013:  // street
                setStreet((String) value);
                return;
            case 3053931:  // city
                setCity((String) value);
                return;
        }
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

}
