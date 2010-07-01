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

import org.joda.beans.impl.StandardMetaBean;
import org.joda.beans.impl.StandardMetaProperty;


/**
 * Mock person JavaBean, used for testing.
 * 
 * @author Stephen Colebourne
 */
public class Person implements Bean<Person> {

    /** The forename meta-property. */
    public static final MetaProperty<Person, String> FORENAME = StandardMetaProperty.of(Person.class, "forename");
    /** The forename. */
    private String forename;

    /** The surname meta-property. */
    public static final MetaProperty<Person, String> SURNAME = StandardMetaProperty.of(Person.class, "surname");
    /** The surname. */
    private String surname;

    /** The number of cars meta-property. */
    public static final MetaProperty<Person, Integer> NUMBER_OF_CARS = StandardMetaProperty.of(Person.class, "numberOfCars");
    /** The surname. */
    private int numberOfCars;

    /** The meta-bean. */
    public static final MetaBean<Person> META = StandardMetaBean.of(Person.class);

    //-----------------------------------------------------------------------
    /**
     * @return the forename
     */
    public String getForename() {
        return forename;
    }

    /**
     * @param forename  the forename to set
     */
    public void setForename(String forename) {
        this.forename = forename;
    }

    /**
     * @return the forename property
     */
    public Property<Person, String> forename() {
        return FORENAME.createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * @return the surname
     */
    public String getSurname() {
        return surname;
    }

    /**
     * @param surname  the surname to set
     */
    public void setSurname(String surname) {
        this.surname = surname;
    }

    /**
     * @return the surname property
     */
    public Property<Person, String> surname() {
        return SURNAME.createProperty(this);
    }

    //-----------------------------------------------------------------------
    /**
     * @return the number of cars
     */
    public int getNumberOfCars() {
        return numberOfCars;
    }

    /**
     * @param numberOfCars  the number of cars to set
     */
    public void setNumberOfCars(int numberOfCars) {
        this.numberOfCars = numberOfCars;
    }

    /**
     * @return the number of cars property
     */
    public Property<Person, Integer> numberOfCars() {
        return NUMBER_OF_CARS.createProperty(this);
    }

    //-----------------------------------------------------------------------
    public Person bean() {
        return this;
    }

    public MetaBean<Person> metaBean() {
        return META;
    }

    public boolean propertyExists(String propertyName) {
        return metaBean().metaPropertyExists(propertyName);
    }

    public Property<Person, Object> property(String propertyName) {
        return metaBean().metaProperty(propertyName).createProperty(this);
    }

    public PropertyMap<Person> propertyMap() {
        return metaBean().createPropertyMap(this);
    }

}
