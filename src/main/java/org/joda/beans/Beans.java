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
 * Utility methods for working with beans.
 * 
 * @author Stephen Colebourne
 */
public final class Beans {

    /**
     * Restricted constructor.
     */
    private Beans() {
    }

    //-------------------------------------------------------------------------
    /**
     * Checks that the argument is non-null.
     * 
     * @param obj  the object to check
     * @throws NullPointerException if the object is null
     */
    static void checkNotNull(Object obj, String message) {
        if (obj == null) {
            throw new IllegalArgumentException(message);
        }
    }

//    /**
//     * Converts a property name to a capitalized property name.
//     * @param name  the name to capitalize, not null
//     * @return the capitalized name, never null
//     */
//    public static String capitalize(String name) {
//        if (name.length() == 0) {
//            return name;
//        }
//        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
//    }
//
//    //-----------------------------------------------------------------------
//    /**
//     * Gets the contents of the specified bean converted to a modifiable {@code HashMap}.
//     * <p>
//     * The returned map will contain 
//     * 
//     * @return the modifiable map of property objects, never null
//     */
//    public static <B> Map<String, Object> toMap(Bean<B> bean) {
//        Map<String, Property<B, Object>> propertyMap = bean.propertyMap();
//        Map<String, Object> map = new HashMap<String, Object>(propertyMap.size());
//        for (Entry<String, Property<B, Object>> entry : propertyMap.entrySet()) {
//            map.put(entry.getKey(), entry.getValue().get());
//        }
//        return map;
//    }

}
