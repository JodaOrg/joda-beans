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

import org.joda.beans.JodaBeanUtils;

/**
 * A generator of copy code.
 * 
 * @author Stephen Colebourne
 */
abstract class CopyGen {

    /**
     * Checks if a copy is possible.
     * 
     * @param prop  the property data, not null
     * @return true if a copy is possible
     */
    abstract boolean isCopyGenerated(GeneratableProperty prop);

    /**
     * Generates the copy method.
     * 
     * @param indent  the indent to use, not null
     * @param prop  the property data, not null
     * @return the generated code, not null
     */
    abstract List<String> generateCopy(String indent, GeneratableProperty prop);

    //-----------------------------------------------------------------------
    static class PatternCopyGen extends CopyGen {
        private final String copyPattern;
        PatternCopyGen(String copyPattern) {
            this.copyPattern = copyPattern;
        }
        @Override
        boolean isCopyGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateCopy(String indent, GeneratableProperty prop) {
            List<String> list = new ArrayList<String>();
            final String[] split = copyPattern.split("\n");
            for (String line : split) {
                line = line.replace("$field", "this." + prop.getFieldName());
                line = line.replace("$value", prop.getPropertyName());
                line = line.replace("$type", prop.getFieldType());
                line = line.replace("$typeRaw", prop.getTypeRaw());
                line = line.replace("$generics", prop.getTypeGenerics());
                if (split.length == 1 && line.endsWith(";") == false) {
                    line += ";";
                }
                list.add(indent + line);
            }
            return list;
        }
    }

    static class BeanCloneGen extends CopyGen {
        static final CopyGen INSTANCE = new BeanCloneGen();
        @Override
        boolean isCopyGenerated(GeneratableProperty prop) {
            return true;
        }
        @Override
        List<String> generateCopy(String indent, GeneratableProperty prop) {
            prop.getBean().ensureImport(JodaBeanUtils.class);
            List<String> list = new ArrayList<String>();
            list.add(indent + "this." + prop.getFieldName() + " = JodaBeanUtils.clone(" + prop.getPropertyName() + ");");
            return list;
        }
    }

    static class NoCopyGen extends CopyGen {
        static final CopyGen INSTANCE = new NoCopyGen();
        @Override
        boolean isCopyGenerated(GeneratableProperty prop) {
            return false;
        }
        @Override
        List<String> generateCopy(String indent, GeneratableProperty prop) {
            return Collections.emptyList();
        }
    }

}
