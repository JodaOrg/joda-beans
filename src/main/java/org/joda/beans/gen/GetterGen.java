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
 * A generator of get methods.
 * 
 * @author Stephen Colebourne
 */
abstract class GetterGen {

    /**
     * Generates the getter method.
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateGetter(GeneratableProperty prop);

    /**
     * Generates the getter method invocation.
     * This is just the method name.
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    String generateGetInvoke(GeneratableProperty prop) {
        return "get" + prop.getUpperName() + "()";
    }

    //-----------------------------------------------------------------------
    static class GetGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new GetGetterGen();
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, "get");
        }
    }

    static class IsGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new IsGetterGen();
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, "is");
        }
        @Override
        String generateGetInvoke(GeneratableProperty prop) {
            return "is" + prop.getUpperName() + "()";
        }
    }

    static class ManualGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new ManualGetterGen();
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return Collections.emptyList();
        }
    }

    static class NoGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new NoGetterGen();
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return Collections.emptyList();
        }
        @Override
        String generateGetInvoke(GeneratableProperty prop) {
            return prop.getFieldName();
        }
    }

    private static List<String> doGenerateGetter(GeneratableProperty prop, String prefix) {
        List<String> list = new ArrayList<String>();
        list.add("\t/**");
        list.add("\t * Gets " + prop.getFirstComment());
        for (String comment : prop.getComments()) {
            list.add("\t * " + comment);
        }
        list.add("\t * @return the value of the property" + (prop.isNotNull() ? ", not null" : ""));
        list.add("\t */");
        if (prop.isDeprecated()) {
            list.add("\t@Deprecated");
        }
        list.add("\tpublic " + prop.getType() + " " + prefix + prop.getUpperName() + "() {");
        list.add("\t\treturn " + prop.getFieldName() + ";");
        list.add("\t}");
        list.add("");
        return list;
    }

}
