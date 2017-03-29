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
 * A generator of copy code.
 * 
 * @author Stephen Colebourne
 */
abstract class CopyGen {

    static final CopyGen ASSIGN = new PatternCopyGen("$field = $value;");
    static final CopyGen CLONE = new PatternCopyGen("$value.clone()");
    static final CopyGen CLONE_CAST = new PatternCopyGen("($type) $value.clone()");

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
        private final String immutablePattern;
        private final String mutablePattern;
        PatternCopyGen(String pattern) {
            this.immutablePattern = pattern;
            this.mutablePattern = pattern;
        }
        PatternCopyGen(String immutablePattern, String mutablePattern) {
            this.immutablePattern = immutablePattern;
            this.mutablePattern = mutablePattern;
        }
        @Override
        List<String> generateCopyToImmutable(String indent, String fromBean, PropertyData prop) {
            List<String> list = new ArrayList<>();
            final String[] split = immutablePattern.split("\n");
            for (String line : split) {
                if (split.length == 1) {
                    if (line.startsWith("$field = ") == false && line.endsWith(";") == false) {
                        if (prop.isNotNull()) {
                            line = "$field = " + line + ";";
                        } else {
                            line = "$field = ($value != null ? " + line + " : null);";
                        }
                    }
                    if (line.startsWith("$field = ") == false) {
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
            List<String> list = new ArrayList<>();
            final String[] split = mutablePattern.split("\n");
            for (String line : split) {
                if (split.length == 1) {
                    if (line.startsWith("$field = ") == false && line.endsWith(";") == false) {
                        if (prop.isNotNull()) {
                            line = "$field = " + line + ";";
                        } else {
                            if (line.equals("$value")) {
                                line = "$field = $value;";
                            } else {
                                line = "$field = ($value != null ? " + line + " : null);";
                            }
                        }
                    }
                    if (line.startsWith("$field = ") == false) {
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
            return Collections.emptyList();
        }
        @Override
        List<String> generateCopyToMutable(String indent, PropertyData prop, String beanToCopyFrom) {
            return Collections.emptyList();
        }
    }

}
