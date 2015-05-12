/*
 *  Copyright 2001-2014 Stephen Colebourne
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
import java.util.Collections;
import java.util.List;

/**
 * A generator of builder code.
 * 
 * @author Stephen Colebourne
 */
abstract class BuilderGen {

    /**
     * Generates the builder field.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateField(String indent, PropertyData prop);

    /**
     * Generates the builder exposed type.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    String generateType(PropertyData prop) {
        return prop.getType();
    }

    //-----------------------------------------------------------------------
    static class PatternBuilderGen extends BuilderGen {
        private final String type;
        private final String init;
        PatternBuilderGen(String type, String init) {
            this.type = type;
            this.init = init;
        }
        @Override
        List<String> generateField(String indent, PropertyData prop) {
            List<String> list = new ArrayList<String>();
            if (prop.isNotNull()) {
                String init = this.init;
                init = init.replace("<>", PropertyGen.resolveWildcard(prop.getTypeGenerics()));
                list.add(indent + "private " + generateType(prop) + " " + prop.getFieldName() + " = " + init + ";");
            } else {
                list.add(indent + "private " + generateType(prop) + " " + prop.getFieldName() + ";");
            }
            return list;
        }
        @Override
        String generateType(PropertyData prop) {
            String result = type.replace("<>", prop.getTypeGenerics());
            if (result.contains("<? extends>")) {
                result = makeExtends(type.replace("<? extends>", prop.getTypeGenerics()));
            }
            return result;
        }
    }

    static class SimpleBuilderGen extends BuilderGen {
        private final String type;
        SimpleBuilderGen(String type) {
            this.type = type;
        }
        @Override
        List<String> generateField(String indent, PropertyData prop) {
            List<String> list = new ArrayList<String>();
            list.add(indent + "private " + generateType(prop) + " " + prop.getFieldName() + ";");
            return list;
        }
        @Override
        String generateType(PropertyData prop) {
            String result = type.replace("<>", prop.getTypeGenerics());
            if (result.contains("<? extends>")) {
                result = makeExtends(type.replace("<? extends>", prop.getTypeGenerics()));
            }
            return result;
        }
    }

    static class NoBuilderGen extends BuilderGen {
        static final BuilderGen INSTANCE = new NoBuilderGen();
        @Override
        List<String> generateField(String indent, PropertyData prop) {
            return Collections.emptyList();
        }
    }

    static String makeExtends(String type) {
        int pos = type.indexOf("<");
        if (pos >= 0 && type.endsWith(">")) {
            String generics = type.substring(pos + 1, type.length() - 1);
            return type.substring(0, pos + 1) + adjustExtends(generics) + ">";
        }
        return type;
    }

    private static String adjustExtends(String generics) {
        // search and handle types like Map<A, B>, Map<List<A>, B>, Map<List<A>, List<B>>
        boolean hasGenerics = false;
        int open = 0;
        for (int i = 0; i < generics.length(); i++) {
            char ch = generics.charAt(i);
            if (ch == '<') {
                open++;
                hasGenerics = true;
            } else if (ch == '>') {
                open--;
            } else if (ch == ',' && open == 0) {
                String part1 = generics.substring(0, i);
                String part2 = generics.substring(i + 1);
                String mid = ",";
                while (part2.startsWith(" ")) {
                    part2 = part2.substring(1);
                    mid += " ";
                }
                if (hasGenerics) {
                    return part1 + mid + adjustExtends(part2);
                }
                return adjustExtends(part1) + mid + adjustExtends(part2);
            }
        }
        // Only add extends if type has no generics, is not String and does not already have a wildcard
        if (hasGenerics) {
            return generics;
        } else if (generics.equals("Object")) {
            return "?";
        } else if (!generics.startsWith("?") && !generics.equals("String")) {
            return "? extends " + generics;
        } else {
            return generics;
        }
    }

}
