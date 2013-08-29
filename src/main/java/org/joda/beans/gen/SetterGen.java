/*
 *  Copyright 2001-2013 Stephen Colebourne
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
 * A generator of set methods.
 * 
 * @author Stephen Colebourne
 */
abstract class SetterGen {

    /**
     * Checks if a setter method is possible.
     * 
     * @param prop  the property data, not null
     * @return true if a setter is possible
     */
    abstract boolean isSetterGenerated(GeneratableProperty prop);

    /**
     * Generates the setter method.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateSetter(String indent, GeneratableProperty prop);

    /**
     * Generates the setter method invocation.
     * This is just the method name.
     * 
     * @param prop  the property data, not null
     * @return the generated code, null if no setter
     */
    String generateSetInvoke(GeneratableProperty prop) {
        return "set" + prop.getUpperName();
    }

    //-----------------------------------------------------------------------
    static class SetSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, GeneratableProperty prop) {
            List<String> list = new ArrayList<String>();
            list.add("\t/**");
            list.add("\t * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @param " + prop.getPropertyName() + "  the new value of the property" +
                    (prop.isNotNull() ? ", not null" : ""));
            list.add("\t */");
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            list.add("\tpublic void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
            if (prop.isValidated()) {
                list.add("\t\t" + prop.getValidationMethodName() + "(" + prop.getPropertyName() + ", \"" + prop.getPropertyName() + "\");");
            }
            list.add("\t\tthis." + prop.getFieldName() + " = " + prop.getPropertyName() + ";");
            list.add("\t}");
            list.add("");
            return list;
        }
    }

    static class PatternSetterGen extends SetterGen {
        private final String setPattern;
        PatternSetterGen(String setPattern) {
            this.setPattern = setPattern;
        }
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, GeneratableProperty prop) {
            List<String> list = new ArrayList<String>();
            list.add(indent + "/**");
            list.add(indent + " * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add(indent + " * " + comment);
            }
            list.add(indent + " * @param " + prop.getPropertyName() + "  the new value of the property");
            list.add(indent + " */");
            if (prop.isDeprecated()) {
                list.add(indent + "@Deprecated");
            }
            list.add(indent + "public void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
            final String[] split = setPattern.split("\n");
            for (String line : split) {
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", prop.getPropertyName());
                line = line.replace("<>", prop.getTypeGenerics());
                if (split.length == 1 && line.endsWith(";") == false) {
                    line += ";";
                }
                list.add(indent + "\t" + line);
            }
            list.add(indent + "}");
            list.add("");
            return list;
        }
    }

    static class NoSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new NoSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return false;
        }
        @Override
        List<String> generateSetter(String indent, GeneratableProperty prop) {
            return Collections.emptyList();
        }
    }

}
