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

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.impl.reflection.ReflectiveMetaBean;

/**
 * Mock reflective bean, used for testing.
 * 
 * @author Stephen Colebourne
 */
public final class ReflectiveMutable implements Bean {

    /**
     * The number.
     */
    private int number;
    /**
     * The number.
     */
    private boolean flag;
    /**
     * The street.
     */
    private String street;
    /**
     * The city.
     */
    private String city;

    /**
     * The meta-bean.
     */
    public static final TypedMetaBean<ReflectiveMutable> META_BEAN =
            ReflectiveMetaBean.of(ReflectiveMutable.class, "number", "flag", "street", "city");

    @Override
    public TypedMetaBean<ReflectiveMutable> metaBean() {
        return META_BEAN;
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
     * Gets the number.
     * @return the value of the property
     */
    public boolean isFlag() {
        return flag;
    }

    /**
     * Sets the number.
     * @param flag  the new value of the property
     */
    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    /**
     * Gets the street.
     * @return the value of the property
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the street.
     * @param street  the new value of the property, not null
     */
    public void setStreet(String street) {
        JodaBeanUtils.notNull(street, "street");
        this.street = street;
    }

    /**
     * Gets the city.
     * @return the value of the property
     */
    public String getCity() {
        return city;
    }

    /**
     * Sets the city.
     * @param city  the new value of the property, not null
     */
    public void setCity(String city) {
        JodaBeanUtils.notNull(city, "city");
        this.city = city;
    }

}
