/*
 *  Copyright 2001-2007 Stephen Colebourne
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

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a property that is linked to a specific bean.
 * <p>
 * Normally, this will be implemented by wrapping a get/set method pair.
 * However, it can also be implemented in other ways, such as accessing
 * a map.
 * 
 * @author Stephen Colebourne
 */
public class Person implements Bean<Person> {

    /** The forename meta property. */
    private static final MetaProperty<Person, String> FORENAME =
        new DescriptorMetaProperty<Person, String>(Person.class, "forename");
    /** The forename. */
    private String forename;

    /** The surname meta property. */
    private static final MetaProperty<Person, String> SURNAME =
        new DescriptorMetaProperty<Person, String>(Person.class, "surname");
    /** The surname. */
    private String surname;

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
    public Property<Person, String> propertyForename() {
        return metaPropertyForename().createProperty(this);
    }

    /**
     * @return the forename meta property
     */
    public static MetaProperty<Person, String> metaPropertyForename() {
        return FORENAME;
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
    public Property<Person, String> propertySurname() {
        return metaPropertySurname().createProperty(this);
    }

    /**
     * @return the forename meta property
     */
    public static MetaProperty<Person, String> metaPropertySurname() {
        return SURNAME;
    }

    //-----------------------------------------------------------------------
    public Person bean() {
        return this;
    }

    public MetaBean<Person> metaBean() {
        return META;
    }

    public Property<Person, ?> property(String propertyName) {
        return propertyMap().get(propertyName);
    }

    public Map<String, Property<Person, ?>> propertyMap() {
        // need a specialist map implementation here
        Map<String, Property<Person, ?>> map = new HashMap<String, Property<Person,?>>();
        map.put("forename", propertyForename());
        map.put("surname", propertySurname());
        return map;
    }

    public static final MetaBean<Person> META = new MetaBean<Person>() {

        public Bean<Person> createBean() {
            return new Person();
        }

        public String getName() {
            return Person.class.getName();
        }

        public Class<Person> getType() {
            return Person.class;
        }

        public MetaProperty<Person, ?> metaProperty(String propertyName) {
            return metaPropertyMap().get(propertyName);
        }

        public Map<String, MetaProperty<Person, ?>> metaPropertyMap() {
            // need a specialist map implementation here
            Map<String, MetaProperty<Person, ?>> map = new HashMap<String, MetaProperty<Person,?>>();
            map.put("forename", metaPropertyForename());
            map.put("surname", metaPropertySurname());
            return map;
        }
        
    };

}
