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
import java.util.List;

/**
 * A generator of copy code.
 */
abstract class CopyGen {

    /**
     * Generates the copy to immutable lines.
     * 
     * @param indent  the indent to use, not null
     * @param fromBean  the source code for the bean to copy from, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateCopyToImmutable(String indent, String fromBean, PropertyData prop);

    /**
     * Generates the copy to mutable lines.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @param beanToCopyFrom  the variable name of the bean, not null
     * @return the generated code, not null
     */
    abstract List<String> generateCopyToMutable(String indent, PropertyData prop, String beanToCopyFrom);

    //-----------------------------------------------------------------------
    static class PatternCopyGen extends CopyGen {
        static final CopyGen ASSIGN = new PatternCopyGen("$field = $value;", true);
        static final CopyGen CLONE = new PatternCopyGen("$value.clone()", false);
        static final CopyGen CLONE_CAST = new PatternCopyGen("($type) $value.clone()", false);
        static final CopyGen CLONE_ARRAY = new PatternCopyGen("($type) JodaBeanUtils.cloneArray($value)", true);

        private final String immutablePattern;
        private final String mutablePattern;
        private final boolean nullSafe;

        PatternCopyGen(String pattern, boolean nullSafe) {
            this(pattern, pattern, nullSafe);
        }

        PatternCopyGen(String immutablePattern, String mutablePattern, boolean nullSafe) {
            this.immutablePattern = immutablePattern;
            this.mutablePattern = mutablePattern;
            this.nullSafe = nullSafe;
        }
        @Override
        List<String> generateCopyToImmutable(String indent, String fromBean, PropertyData prop) {
            var list = new ArrayList<String>();
            var split = immutablePattern.split("\n");
            for (var line : split) {
                if (split.length == 1) {
                    if (!line.startsWith("$field = ") && !line.endsWith(";")) {
                        if (nullSafe || prop.isNotNull()) {
                            line = "$field = " + line + ";";
                        } else {
                            line = "$field = ($value != null ? " + line + " : null);";
                        }
                    }
                    if (!line.startsWith("$field = ")) {
                        line = "$field = " + line;
                    }
                }
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", fromBean + (fromBean.isEmpty() ? prop.getPropertyName() : prop.getFieldName()));
                line = line.replace("$type", prop.getFieldType());
                line = line.replace("$typeRaw", prop.getTypeRaw());
                line = line.replace("$generics", prop.getTypeGenerics());
                list.add(indent + line);
            }
            return list;
        }
        @Override
        List<String> generateCopyToMutable(String indent, PropertyData prop, String beanToCopyFrom) {
            var list = new ArrayList<String>();
            var split = mutablePattern.split("\n");
            for (var line : split) {
                if (split.length == 1) {
                    if (!line.startsWith("$field = ") && !line.endsWith(";")) {
                        if (nullSafe || prop.isNotNull()) {
                            line = "$field = " + line + ";";
                        } else {
                            if (line.equals("$value")) {
                                line = "$field = $value;";
                            } else {
                                line = "$field = ($value != null ? " + line + " : null);";
                            }
                        }
                    }
                    if (!line.startsWith("$field = ")) {
                        line = "$field = " + line;
                    }
                }
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", beanToCopyFrom + "." + prop.getGetterGen().generateGetInvoke(prop));
                line = line.replace("$type", prop.getFieldType());
                line = line.replace("$typeRaw", prop.getTypeRaw());
                line = line.replace("$generics", prop.getTypeGenerics());
                list.add(indent + line);
            }
            return list;
        }
    }

    static class NoCopyGen extends CopyGen {
        static final CopyGen INSTANCE = new NoCopyGen();
        @Override
        List<String> generateCopyToImmutable(String indent, String fromBean, PropertyData prop) {
            return List.of();
        }
        @Override
        List<String> generateCopyToMutable(String indent, PropertyData prop, String beanToCopyFrom) {
            return List.of();
        }
    }

}
