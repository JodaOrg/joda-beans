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

/**
 * Examples using Person.
 * 
 * @author Stephen Colebourne
 */
public class Examples {

    public static void main(String[] args) {
        // create the bean the hard way - could just do new Person() instead
        Person p = Person.META.createBean().bean();
        // set surname using normal method
        p.setSurname("Colebourne");
        // query using property method
        System.out.println(p.surname().get());
        // set/get forename using property method
        p.forename().set("Stephen");
        System.out.println(p.forename().get());
        // set cars
        p.numberOfCars().set(2);
        // access all the properties
        System.out.println(p.propertyMap().values());
        System.out.println(p.metaBean().metaPropertyMap().values());
        System.out.println(p.propertyMap().flatten());
        // perform validation
        boolean valid =
            validateNotEmpty(p.surname()) &&
            validateNotEmpty(p.forename());
        System.out.println(valid ? "Valid" : "Not valid");
    }

    private static boolean validateNotEmpty(Property<?, String> property) {
        String str = property.get();
        return (str != null && str.length() > 0);
    }

}
