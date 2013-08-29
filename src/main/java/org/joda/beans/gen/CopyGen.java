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
 * A generator of copy code.
 * 
 * @author Stephen Colebourne
 */
abstract class CopyGen {

    static final CopyGen ASSIGN = new PatternCopyGen("$field = $value;");
    static final CopyGen CLONE = new PatternCopyGen("$field = ($value != null ? $value.clone() : null);");
    static final CopyGen CLONE_CAST = new PatternCopyGen("$field = ($value != null ? ($type) $value.clone() : null);");

    /**
     * Generates the copy to immutable lines.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateCopyToImmutable(String indent, GeneratableProperty prop);

    /**
     * Generates the copy to mutable lines.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @param beanToCopyFrom  the variable name of the bean, not null
     * @return the generated code, not null
     */
    abstract List<String> generateCopyToMutable(String indent, GeneratableProperty prop, String beanToCopyFrom);

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
        List<String> generateCopyToImmutable(String indent, GeneratableProperty prop) {
            List<String> list = new ArrayList<String>();
            final String[] split = immutablePattern.split("\n");
            for (String line : split) {
                if (split.length == 1) {
                    if (line.endsWith(";") == false) {
                        line += ";";
                    }
                    if (line.startsWith("$field = ") == false) {
                        line = "$field = " + line;
                    }
                }
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", prop.getPropertyName());
                line = line.replace("$type", prop.getFieldType());
                line = line.replace("$typeRaw", prop.getTypeRaw());
                line = line.replace("$generics", prop.getTypeGenerics());
                line = line.replace("<>", prop.getTypeGenerics());
                list.add(indent + line);
            }
            return list;
        }
        @Override
        List<String> generateCopyToMutable(String indent, GeneratableProperty prop, String beanToCopyFrom) {
            List<String> list = new ArrayList<String>();
            final String[] split = mutablePattern.split("\n");
            for (String line : split) {
                if (split.length == 1) {
                    if (line.endsWith(";") == false) {
                        line += ";";
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
                line = line.replace("<>", prop.getTypeGenerics());
                list.add(indent + line);
            }
            return list;
        }
    }

    static class NoCopyGen extends CopyGen {
        static final CopyGen INSTANCE = new NoCopyGen();
        @Override
        List<String> generateCopyToImmutable(String indent, GeneratableProperty prop) {
            return Collections.emptyList();
        }
        @Override
        List<String> generateCopyToMutable(String indent, GeneratableProperty prop, String beanToCopyFrom) {
            return Collections.emptyList();
        }
    }

}
