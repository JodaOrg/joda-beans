/*
 *  Copyright 2001-2012 Stephen Colebourne
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A generator of set methods.
 * 
 * @author Stephen Colebourne
 */
abstract class SetterGen {

    /** The known setter generators. */
    static final Map<String, SetterGen> SETTERS = new HashMap<String, SetterGen>();
    static {
        SETTERS.put("", NoSetterGen.INSTANCE);
        SETTERS.put("smart", SmartSetterGen.INSTANCE);
        SETTERS.put("set", SetSetterGen.INSTANCE);
        SETTERS.put("setClearAddAll", SetClearAddAllSetterGen.INSTANCE);
        SETTERS.put("setClearPutAll", SetClearPutAllSetterGen.INSTANCE);
        SETTERS.put("manual", NoSetterGen.INSTANCE);
    }

    /**
     * Generates the setter method.
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    static SetterGen of(GeneratableProperty prop) {
        SetterGen gen = SETTERS.get(prop.getSetStyle());
        if (gen == null) {
            throw new RuntimeException("Unable to locate setter generator '" + prop.getSetStyle() + "'");
        }
        return gen;
    }

    //-----------------------------------------------------------------------
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
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateSetter(GeneratableProperty prop);

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
    static class SmartSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SmartSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            if (prop.isFinal()) {
                if (prop.isCollectionType() || prop.isMapType()) {
                    return true;
                }
                return false;
            } else {
                return SetSetterGen.INSTANCE.isSetterGenerated(prop);
            }
        }
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            if (prop.isFinal()) {
                if (prop.isCollectionType()) {
                    return SetClearAddAllSetterGen.INSTANCE.generateSetter(prop);
                }
                if (prop.isMapType()) {
                    return SetClearPutAllSetterGen.INSTANCE.generateSetter(prop);
                }
                return Collections.emptyList();
            } else {
                return SetSetterGen.INSTANCE.generateSetter(prop);
            }
        }
        @Override
        String generateSetInvoke(GeneratableProperty prop) {
            if (prop.isFinal()) {
                if (prop.isCollectionType()) {
                    return SetClearAddAllSetterGen.INSTANCE.generateSetInvoke(prop);
                }
                if (prop.isMapType()) {
                    return SetClearPutAllSetterGen.INSTANCE.generateSetInvoke(prop);
                }
                return null;
            } else {
                return SetSetterGen.INSTANCE.generateSetInvoke(prop);
            }
        }
    }

    static class SetSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
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

    static class SetClearAddAllSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetClearAddAllSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return doGenerateBulkSetter(prop, "addAll");
        }
    }

    static class SetClearPutAllSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new SetClearPutAllSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return doGenerateBulkSetter(prop, "putAll");
        }
    }

    static class NoSetterGen extends SetterGen {
        static final SetterGen INSTANCE = new NoSetterGen();
        @Override
        boolean isSetterGenerated(GeneratableProperty prop) {
            return false;
        }
        @Override
        List<String> generateSetter(GeneratableProperty prop) {
            return Collections.emptyList();
        }
    }

    private static List<String> doGenerateBulkSetter(GeneratableProperty prop, String alterMethod) {
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
