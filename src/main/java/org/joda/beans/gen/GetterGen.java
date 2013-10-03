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
    static final class GetGetterGen extends GetterGen {
        static final GetGetterGen PUBLIC = new GetGetterGen("public");
        static final GetGetterGen PRIVATE = new GetGetterGen("private");
        private final String access;
        static GetGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE : PUBLIC);
        }
        private GetGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, access, "get", prop.getFieldName());
        }
    }

    static final class IsGetterGen extends GetterGen {
        static final IsGetterGen PUBLIC = new IsGetterGen("public");
        static final IsGetterGen PRIVATE = new IsGetterGen("private");
        private final String access;
        static IsGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE : PUBLIC);
        }
        private IsGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, access, "is", prop.getFieldName());
        }
        @Override
        String generateGetInvoke(GeneratableProperty prop) {
            return "is" + prop.getUpperName() + "()";
        }
    }

    static final class CloneGetterGen extends GetterGen {
        static final CloneGetterGen PUBLIC = new CloneGetterGen("public");
        static final CloneGetterGen PRIVATE = new CloneGetterGen("private");
        private final String access;
        static CloneGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE : PUBLIC);
        }
        private CloneGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, access, "get", "(" + prop.getFieldName() + " != null ? " + prop.getFieldName() + ".clone() : null)");
        }
    }

    static final class CloneCastGetterGen extends GetterGen {
        static final CloneCastGetterGen PUBLIC = new CloneCastGetterGen("public");
        static final CloneCastGetterGen PRIVATE = new CloneCastGetterGen("private");
        private final String access;
        static CloneCastGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE : PUBLIC);
        }
        private CloneCastGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(GeneratableProperty prop) {
            return doGenerateGetter(prop, access, "get", "(" + prop.getFieldName() + " != null ? (" + prop.getFieldType() + ") " + prop.getFieldName() + ".clone() : null)");
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

    private static List<String> doGenerateGetter(GeneratableProperty prop, String access, String prefix, String expression) {
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
        list.add("\t" + access + " " + prop.getType() + " " + prefix + prop.getUpperName() + "() {");
        list.add("\t\treturn " + expression + ";");
        list.add("\t}");
        list.add("");
        return list;
    }

}
