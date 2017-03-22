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
    abstract List<String> generateGetter(PropertyData prop);

    /**
     * Generates the getter method invocation.
     * This is just the method name.
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    String generateGetInvoke(PropertyData prop) {
        return "get" + prop.getUpperName() + "()";
    }

    //-----------------------------------------------------------------------
    static final class GetGetterGen extends GetterGen {
        static final GetGetterGen PUBLIC = new GetGetterGen("public ");
        static final GetGetterGen PROTECTED = new GetGetterGen("protected ");
        static final GetGetterGen PACKAGE = new GetGetterGen("");
        static final GetGetterGen PRIVATE = new GetGetterGen("private ");
        private final String access;
        static GetGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private GetGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "get", prop.getFieldName());
        }
    }

    static final class IsGetterGen extends GetterGen {
        static final IsGetterGen PUBLIC = new IsGetterGen("public ");
        static final IsGetterGen PROTECTED = new IsGetterGen("protected ");
        static final IsGetterGen PACKAGE = new IsGetterGen("");
        static final IsGetterGen PRIVATE = new IsGetterGen("private ");
        private final String access;
        static IsGetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private IsGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "is", prop.getFieldName());
        }
        @Override
        String generateGetInvoke(PropertyData prop) {
            return "is" + prop.getUpperName() + "()";
        }
    }

    static final class CloneNNGetterGen extends GetterGen {
        static final GetterGen PUBLIC = new CloneNNGetterGen("public ");
        static final GetterGen PROTECTED = new CloneNNGetterGen("protected ");
        static final GetterGen PACKAGE = new CloneNNGetterGen("");
        static final GetterGen PRIVATE = new CloneNNGetterGen("private ");
        private final String access;
        static GetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private CloneNNGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "get", prop.getFieldName() + ".clone()");
        }
    }

    static final class CloneGetterGen extends GetterGen {
        static final GetterGen PUBLIC = new CloneGetterGen("public ");
        static final GetterGen PROTECTED = new CloneGetterGen("protected ");
        static final GetterGen PACKAGE = new CloneGetterGen("");
        static final GetterGen PRIVATE = new CloneGetterGen("private ");
        private final String access;
        static GetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private CloneGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "get", "(" + prop.getFieldName() + " != null ? " + prop.getFieldName() + ".clone() : null)");
        }
    }

    static final class CloneCastNNGetterGen extends GetterGen {
        static final GetterGen PUBLIC = new CloneCastNNGetterGen("public ");
        static final GetterGen PROTECTED = new CloneCastNNGetterGen("protected ");
        static final GetterGen PACKAGE = new CloneCastNNGetterGen("");
        static final GetterGen PRIVATE = new CloneCastNNGetterGen("private ");
        private final String access;
        static GetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private CloneCastNNGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "get", "(" + prop.getFieldType() + ") " + prop.getFieldName() + ".clone()");
        }
    }

    static final class CloneCastGetterGen extends GetterGen {
        static final GetterGen PUBLIC = new CloneCastGetterGen("public ");
        static final GetterGen PROTECTED = new CloneCastGetterGen("protected ");
        static final GetterGen PACKAGE = new CloneCastGetterGen("");
        static final GetterGen PRIVATE = new CloneCastGetterGen("private ");
        private final String access;
        static GetterGen of(String access) {
            return (access.equals("private") ? PRIVATE :
                    access.equals("package") ? PACKAGE :
                    access.equals("protected") ? PROTECTED : PUBLIC);
        }
        private CloneCastGetterGen(String access) {
            this.access = access;
        }
        @Override
        List<String> generateGetter(PropertyData prop) {
            return doGenerateGetter(prop, access, "get", "(" + prop.getFieldName() + " != null ? (" + prop.getFieldType() + ") " + prop.getFieldName() + ".clone() : null)");
        }
    }

    static final class Optional8GetterGen extends GetterGen {
        static final GetterGen PUBLIC = new Optional8GetterGen();
        @Override
        List<String> generateGetter(PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add("\t/**");
            list.add("\t * Gets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @return the optional value of the property, not null");
            list.add("\t */");
            if (prop.isOverrideGet()) {
                list.add("\t@Override");
            }
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            if ("Double".equals(prop.getType())) {
                list.add("\tpublic OptionalDouble get" + prop.getUpperName() + "() {");
                list.add("\t\treturn " + prop.getFieldName() + " != null ? " +
                    "OptionalDouble.of(" + prop.getFieldName() + ") : OptionalDouble.empty();");
            } else if ("Integer".equals(prop.getType())) {
                list.add("\tpublic OptionalInt get" + prop.getUpperName() + "() {");
                list.add("\t\treturn " + prop.getFieldName() + " != null ? " +
                    "OptionalInt.of(" + prop.getFieldName() + ") : OptionalInt.empty();");
            } else if ("Long".equals(prop.getType())) {
                list.add("\tpublic OptionalLong get" + prop.getUpperName() + "() {");
                list.add("\t\treturn " + prop.getFieldName() + " != null ? " +
                    "OptionalLong.of(" + prop.getFieldName() + ") : OptionalLong.empty();");
            } else {
                list.add("\tpublic Optional<" + prop.getType() + "> get" + prop.getUpperName() + "() {");
                list.add("\t\treturn Optional.ofNullable(" + prop.getFieldName() + ");");
            }
            list.add("\t}");
            list.add("");
            return list;
        }
        @Override
        String generateGetInvoke(PropertyData prop) {
            return prop.getFieldName();
        }
    }

    static final class OptionalGuavaGetterGen extends GetterGen {
        static final GetterGen PUBLIC = new OptionalGuavaGetterGen();
        @Override
        List<String> generateGetter(PropertyData prop) {
            List<String> list = new ArrayList<>();
            list.add("\t/**");
            list.add("\t * Gets " + prop.getFirstComment());
            for (String comment : prop.getComments()) {
                list.add("\t * " + comment);
            }
            list.add("\t * @return the optional value of the property, not null");
            list.add("\t */");
            if (prop.isOverrideGet()) {
                list.add("\t@Override");
            }
            if (prop.isDeprecated()) {
                list.add("\t@Deprecated");
            }
            list.add("\tpublic Optional<" + prop.getType() + "> get" + prop.getUpperName() + "() {");
            list.add("\t\treturn Optional.fromNullable(" + prop.getFieldName() + ");");
            list.add("\t}");
            list.add("");
            return list;
        }
        @Override
        String generateGetInvoke(PropertyData prop) {
            return prop.getFieldName();
        }
    }

    static class ManualGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new ManualGetterGen();
        @Override
        List<String> generateGetter(PropertyData prop) {
            return Collections.emptyList();
        }
    }

    static class NoGetterGen extends GetterGen {
        static final GetterGen INSTANCE = new NoGetterGen();
        @Override
        List<String> generateGetter(PropertyData prop) {
            return Collections.emptyList();
        }
        @Override
        String generateGetInvoke(PropertyData prop) {
            return prop.getFieldName();
        }
    }

    private static List<String> doGenerateGetter(PropertyData prop, String access, String prefix, String expression) {
        List<String> list = new ArrayList<>();
        list.add("\t/**");
        list.add("\t * Gets " + prop.getFirstComment());
        for (String comment : prop.getComments()) {
            list.add("\t * " + comment);
        }
        list.add("\t * @return the value of the property" + prop.getNotNullJavadoc());
        list.add("\t */");
        if (prop.isOverrideGet()) {
            list.add("\t@Override");
        }
        if (prop.isDeprecated()) {
            list.add("\t@Deprecated");
        }
        list.add("\t" + access + prop.getType() + " " + prefix + prop.getUpperName() + "() {");
        list.add("\t\treturn " + expression + ";");
        list.add("\t}");
        list.add("");
        return list;
    }

}
