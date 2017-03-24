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
     * Is special initialization needed.
     * 
     * @param prop  the property data, not null
     * @return true if special
     */
    abstract boolean isSpecialInit(PropertyData prop);

    /**
     * Generates the init string.
     * 
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract String generateInit(PropertyData prop);

    /**
     * Generates the builder exposed type.
     * 
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    String generateType(PropertyData prop) {
        return prop.getBuilderType();
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
            List<String> list = new ArrayList<>();
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
        boolean isSpecialInit(PropertyData prop) {
            return true;
        }
        @Override
        String generateInit(PropertyData prop) {
            return init;
        }
        @Override
        String generateType(PropertyData prop) {
            if (!"smart".equals(prop.getBuilderTypeStyle())) {
                return prop.getBuilderType().replace("<>", prop.getTypeGenerics());
            }
            return type.replace("<>", prop.getTypeGenerics());
        }
    }

    static class SimpleBuilderGen extends BuilderGen {
        SimpleBuilderGen() {
        }
        @Override
        List<String> generateField(String indent, PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add(indent + "private " + generateType(prop) + " " + prop.getFieldName() + ";");
            return list;
        }
        @Override
        boolean isSpecialInit(PropertyData prop) {
            return false;
        }
        @Override
        String generateInit(PropertyData prop) {
            return defaultType(prop);
        }
        @Override
        String generateType(PropertyData prop) {
            return prop.getBuilderType().replace("<>", prop.getTypeGenerics());
        }
    }

    static class NoBuilderGen extends BuilderGen {
        static final BuilderGen INSTANCE = new NoBuilderGen();
        @Override
        List<String> generateField(String indent, PropertyData prop) {
            return Collections.emptyList();
        }
        @Override
        boolean isSpecialInit(PropertyData prop) {
            return false;
        }
        @Override
        String generateInit(PropertyData prop) {
            return defaultType(prop);
        }
    }

    private static String defaultType(PropertyData prop) {
        if (prop.getType().equals("long")) {
            return "0L";
        } else if (prop.getType().equals("int")) {
            return "0";
        } else if (prop.getType().equals("short")) {
            return "Short.valueOf(0)";
        } else if (prop.getType().equals("byte")) {
            return "Byte.valueOf(0)";
        } else if (prop.getType().equals("double")) {
            return "0d";
        } else if (prop.getType().equals("float")) {
            return "0f";
        } else if (prop.getType().equals("char")) {
            return "Character.valueOf((char) 0)";
        } else if (prop.getType().equals("boolean")) {
            return "Boolean.FALSE";
        }
        return "null";
    }

}
