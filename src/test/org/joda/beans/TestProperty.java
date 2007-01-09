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

/**
 * Represents a property that is linked to a specific bean.
 * <p>
 * Normally, this will be implemented by wrapping a get/set method pair.
 * However, it can also be implemented in other ways, such as accessing
 * a map.
 * 
 * @author Stephen Colebourne
 */
public class TestProperty {

    public static void main(String[] args) {
        // create the bean the hard way - could just do new Person() instead
        Person p = Person.META.createBean().bean();
        // set surname using normal method
        p.setSurname("Colebourne");
        // query using property method
        System.out.println(p.propertySurname().get());
        // set/get forename using property method
        p.propertyForename().set("Stephen");
        System.out.println(p.propertyForename().get());
        // access all the properties
        System.out.println(p.propertyMap());
        System.out.println(p.metaBean().metaPropertyMap());
        // perform validation
        boolean valid =
            validateNotEmpty(p.propertySurname()) &&
            validateNotEmpty(p.propertyForename());
        System.out.println(valid ? "Valid" : "Not valid");
    }

    private static boolean validateNotEmpty(Property<?, String> property) {
        String str = property.get();
        return (str != null && str.length() > 0);
    }

}
