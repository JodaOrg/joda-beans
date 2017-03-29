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
    abstract boolean isSetterGenerated(PropertyData prop);

    /**
     * Generates the setter method.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateSetter(String indent, PropertyData prop);

    /**
     * Generates the setter method invocation.
     * This is just the method name.
     * 
     * @param prop  the property data, not null
     * @param newValue  the new value, not null
     * @return the generated code, null if no setter
     */
    String generateSetInvoke(PropertyData prop, String newValue) {
        return "set" + prop.getUpperName() + "(" + newValue + ")";
    }

    //-----------------------------------------------------------------------
    static final class SetSetterGen extends SetterGen {
        static final SetSetterGen PUBLIC = new SetSetterGen("public ");
        static final SetSetterGen PROTECTED = new SetSetterGen("protected ");
        static final SetSetterGen PACKAGE = new SetSetterGen("");
        static final SetSetterGen PRIVATE = new SetSetterGen("private ");
        private final String access;
        static SetSetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private SetSetterGen(String access) {
            this.access = access;
        }
        @Override
        boolean isSetterGenerated(PropertyData prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add("\t/**");
            list.add("\t * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @param " + prop.getPropertyName() + "  the new value of the property" + prop.getNotNullJavadoc());
            list.add("\t */");
            if (prop.isOverrideSet()) {
                list.add("\t@Override");
            }
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            list.add("\t" + access + "void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
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
        private final String access;
        PatternSetterGen(String setPattern) {
            this(setPattern, "public");
        }
        PatternSetterGen(String setPattern, String access) {
            this.setPattern = setPattern;
            this.access = (access.equals("package") ? "" : access + ' ');;
        }
        @Override
        boolean isSetterGenerated(PropertyData prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add(indent + "/**");
            list.add(indent + " * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add(indent + " * " + comment);
            }
            list.add(indent + " * @param " + prop.getPropertyName() + "  the new value of the property" + prop.getNotNullJavadoc());
            list.add(indent + " */");
            if (prop.isOverrideSet()) {
                list.add("\t@Override");
            }
            if (prop.isDeprecated()) {
                list.add(indent + "@Deprecated");
            }
            list.add(indent + access + "void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
            if (prop.isValidated()) {
                list.add("\t\t" + prop.getValidationMethodName() + "(" + prop.getPropertyName() + ", \"" + prop.getPropertyName() + "\");");
            }
            final String[] split = setPattern.split("\n");
            for (String line : split) {
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", prop.getPropertyName());
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
        boolean isSetterGenerated(PropertyData prop) {
            return false;
        }
        @Override
        List<String> generateSetter(String indent, PropertyData prop) {
            return Collections.emptyList();
        }
    }

    static class FieldSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new FieldSetterGen();
        @Override
        boolean isSetterGenerated(PropertyData prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, PropertyData prop) {
            return Collections.emptyList();
        }
        @Override
        String generateSetInvoke(PropertyData prop, String newValue) {
            return prop.getFieldName() + " = " + newValue;
        }
    }

    static class ObservableSetterGen extends SetterGen {
        static final SetterGen PUBLIC = new ObservableSetterGen();
        @Override
        boolean isSetterGenerated(PropertyData prop) {
            return true;
        }
        @Override
        List<String> generateSetter(String indent, PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add("\t/**");
            list.add("\t * Sets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @param " + prop.getPropertyName() + "  the new value of the property" + prop.getNotNullJavadoc());
            list.add("\t */");
            if (prop.isOverrideSet()) {
                list.add("\t@Override");
            }
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            list.add("\tpublic void set" + prop.getUpperName() + "(" + prop.getType() +  " " + prop.getPropertyName() + ") {");
            if (prop.isValidated()) {
                list.add("\t\t" + prop.getValidationMethodName() + "(" + prop.getPropertyName() + ", \"" + prop.getPropertyName() + "\");");
            }
            String old = "old" + prop.getUpperName();
            list.add("\t\t" + prop.getFieldType() + " " + old + " = this." + prop.getFieldName() + ";");
            list.add("\t\tthis." + prop.getFieldName() + " = " + prop.getPropertyName() + ";");
            list.add("\t\tthis." + prop.getConfig().getPrefix() + "propertyChangeSupport.firePropertyChange(\"" +
                prop.getPropertyName() + "\", " + old + ", " + prop.getPropertyName() + ");");
            list.add("\t}");
            list.add("");
            return list;
        }
    }

}
