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
package org.joda.beans.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generator of set methods.
 * 
 * @author Stephen Colebourne
 */
abstract class SetterGen {

    /** Collection types. */
    private static final Set<String> COLLECTIONS = new HashSet<String>(
            Arrays.asList(
                    "Collection", "Set", "SortedSet", "NavigableSet", "List",
                    "ArrayList", "LinkedList",
                    "HashSet", "LinkedHashSet", "TreeSet", "ConcurrentSkipListSet"));
    /** Map types. */
    private static final Set<String> MAPS = new HashSet<String>(
            Arrays.asList(
                    "Map", "SortedMap", "NavigableMap", "ConcurrentMap", "ConcurrentNavigableMap",
                    "HashMap", "LinkedHashMap", "TreeMap", "ConcurrentHashMap", "ConcurrentSkipListMap"));

    /**
     * Generates the setter method.
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateSetter(GeneratableProperty prop);

    /**
     * Generates the setter method invocation.
     * This is just the method name.
     * @param prop  the property data, not null
     * @return the generated code, null if no setter
     */
    String generateSetInvoke(GeneratableProperty prop) {
        return "set" + prop.getUpperName();
    }

    //-----------------------------------------------------------------------
    static class SmartSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SmartSetterGen();
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            if (prop.isFinal()) {
                if (isCollection(prop)) {
                    return SetCollectionSetterGen.INSTANCE.generateSetter(prop);
                }
                if (isMap(prop)) {
                    return SetMapSetterGen.INSTANCE.generateSetter(prop);
                }
                return Collections.emptyList();
            } else {
                return SetSetterGen.INSTANCE.generateSetter(prop);
            }
        }
        @Override
        String generateSetInvoke(GeneratableProperty prop) {
            if (prop.isFinal()) {
                if (isCollection(prop)) {
                    return SetCollectionSetterGen.INSTANCE.generateSetInvoke(prop);
                }
                if (isMap(prop)) {
                    return SetMapSetterGen.INSTANCE.generateSetInvoke(prop);
                }
                return null;
            } else {
                return SetSetterGen.INSTANCE.generateSetInvoke(prop);
            }
        }
        private static boolean isCollection(GeneratableProperty prop) {
            return prop.isGeneric() && COLLECTIONS.contains(prop.getRawType());
        }
        private static boolean isMap(GeneratableProperty prop) {
            return "FlexiBean".equals(prop.getType()) || (prop.isGeneric() && MAPS.contains(prop.getRawType()));
        }
    }

    static class SetSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetSetterGen();
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            List<String> list = new ArrayList<String>();
            list.add("\t/**");
            list.add("\t * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @param " + prop.getPropertyName() + "  the new value of the property");
            list.add("\t */");
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            list.add("\tpublic void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
            list.add("\t\tthis." + prop.getFieldName() + " = " + prop.getPropertyName() + ";");
            list.add("\t}");
            list.add("");
            return list;
        }
    }

    static class SetCollectionSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetCollectionSetterGen();
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return generateBulkSetter(prop, "addAll");
        }
    }

    static class SetMapSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetMapSetterGen();
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return generateBulkSetter(prop, "putAll");
        }
    }

    static class ManualSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new ManualSetterGen();
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return Collections.emptyList();
        }
    }

    private static List<String> generateBulkSetter(GeneratableProperty prop, String alterMethod) {
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Sets " + prop.getFirstComment());
        for (String comment : prop.getComments()) {
            list.add("\t * " + comment);
        }
        list.add("\t * @param " + prop.getPropertyName() + "  the new value of the property");
        list.add("\t */");
        if (prop.isDeprecated()) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
        list.add("\t\tthis." + prop.getFieldName() + ".clear();");
        list.add("\t\tthis." + prop.getFieldName() + "." + alterMethod + "(" + prop.getPropertyName() + ");");
        list.add("\t}");
        list.add("");
        return list;
    }
}
