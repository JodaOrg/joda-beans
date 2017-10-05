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

import org.joda.beans.sample.Address;
import org.joda.beans.sample.Documentation;
import org.joda.beans.sample.Person;

/**
 * Examples using Person.
 * 
 * @author Stephen Colebourne
 */
public class Examples {

    public static void main(String[] args) {
        // create the bean the hard way - could just do new Person() instead
        Person p = Person.meta().builder().set("surname", "Smith").build();
        // set surname using normal method
        p.setSurname("Colebourne");
        // query using property method
        System.out.println(p.surname().get());
        // query using meta-property method
        System.out.println(Person.meta().surname().get(p));
        // set/get forename using property method
        p.forename().set("Stephen");
        System.out.println(p.forename().get());
        // set cars
        p.numberOfCars().set(2);
        // access all the properties
        System.out.println(p.propertyNames());
        System.out.println(JodaBeanUtils.flatten(p));
        System.out.println(p);
        // perform validation
        boolean valid =
            validateNotEmpty(p.surname()) &&
            validateNotEmpty(p.forename());
        System.out.println(valid ? "Valid" : "Not valid");
        // extensions
        p.getExtensions().set("suffix", "Jr");
        System.out.println(p.propertyNames());
        System.out.println(JodaBeanUtils.flatten(p));
        System.out.println(p);
        
        // create the bean the hard way - could just do new Address() instead
        Address a = (Address) Address.meta().builder().build();
        // set surname using normal method
        a.setStreet("Barnsnap Close");
        // query using property method
        System.out.println(a.street().get());
        // set/get forename using property method
        a.city().set("Horsham");
        System.out.println(a.city().get());
        // set cars
        a.number().set(22);
        // access all the properties
        System.out.println(a.propertyNames());
        System.out.println(JodaBeanUtils.flatten(a));
        System.out.println(a);
        // perform validation
        valid =
            validateNotEmpty(a.street()) &&
            validateNotEmpty(a.city());
        System.out.println(valid ? "Valid" : "Not valid");
        
        // generics
        Documentation<Address> d = new Documentation<>();
        d.setType("ADDRESS");
        d.setContent(a);
        Property<Address> dProp = d.content();
        Address a2 = dProp.metaProperty().get(d);
        System.out.println(a2);
        Address a3 = Documentation.metaDocumentation(Address.class).content().get(d);
        System.out.println(a3);
        Documentation<Address> d2 = Documentation.metaDocumentation(Address.class).builder().build();
        System.out.println(d2);
        
//        try {
//            Mongo mongo = new Mongo("127.0.0.1");
//            System.out.println(mongo);
//            mongo.dropDatabase("BeansTest");
//            DB db = mongo.getDB("BeansTest");
//            DBCollection coll = db.createCollection("Test", new BeanMongoDBObject(p));
//            System.out.println(coll);
//            System.out.println("Docs " + coll.getCount());
//            
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
    }

    private static boolean validateNotEmpty(Property<String> property) {
        String str = property.get();
        return (str != null && str.length() > 0);
    }

}
