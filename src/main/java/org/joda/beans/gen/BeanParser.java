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

import static org.joda.beans.gen.BeanGen.CONSTRUCTOR_BY_ARGS;
import static org.joda.beans.gen.BeanGen.CONSTRUCTOR_BY_BUILDER;
import static org.joda.beans.gen.BeanGen.CONSTRUCTOR_NONE;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Parse bean information from source file.
 */
class BeanParser {

    /** Start marker. */
    private static final String AUTOGENERATED_START_TEXT = "AUTOGENERATED START";
    /** Start marker. */
    private static final String AUTOGENERATED_START = "\t//------------------------- AUTOGENERATED START -------------------------";
    /** End marker. */
    private static final String AUTOGENERATED_END = "\t//-------------------------- AUTOGENERATED END --------------------------";
    /** Pattern to find bean type. */
    // handle three comma separated generic parameters
    // handle generic parameter extends clause
    // handle extends clause with further level of generic parameters
    // handle extends clause union types without generic parameters
    private static final Pattern BEAN_TYPE = Pattern.compile(".*class +(" +
            "([A-Z][A-Za-z0-9_]+)" +
                "(?:<" +
                    "([A-Z])( +extends +[A-Za-z0-9_]+(?:[<][A-Za-z0-9_, ?]+[>])?(?:[ ]+[&][ ]+[A-Za-z0-9]+)*)?" +
                    "(?:[,] +" +
                        "([A-Z])( +extends +[A-Za-z0-9_]+(?:[<][A-Za-z0-9_, ?]+[>])?(?:[ ]+[&][ ]+[A-Za-z0-9]+)*)?" +
                        "(?:[,] +" +
                            "([A-Z])( +extends +[A-Za-z0-9_]+(?:[<][A-Za-z0-9_, ?]+[>])?(?:[ ]+[&][ ]+[A-Za-z0-9]+)*)?" +
                        ")?" +
                    ")?" +
                ">)?" +
            ").*");
    /** Pattern to find super type. */
    private static final Pattern SUPER_TYPE = Pattern.compile(".*extends +(" +
            "([A-Z][A-Za-z0-9_]+)" +
                "(?:<" +
                    "([A-Z][A-Za-z0-9_<> ]*)" +
                    "(?:[,] +" +
                        "([A-Z][A-Za-z0-9_<> ]*)?" +
                        "(?:[,] +" +
                            "([A-Z][A-Za-z0-9_<> ]*)?" +
                        ")?" +
                    ")?" +
                ">)?" +
            ").*");
    /** Pattern to find root type. */
    private static final Pattern SUPER_IMPL_TYPE = Pattern.compile(".*implements.*[ ,]((Immutable)?Bean)([ ,{]|$).*");
    /** Pattern to find serializable interface. */
    private static final Pattern SERIALIZABLE_TYPE = Pattern.compile(".*implements.*[ ,]Serializable([ ,{]|$).*");
    /** The style pattern. */
    private static final Pattern STYLE_PATTERN = Pattern.compile(".*[ ,(]style[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The metaScope pattern. */
    private static final Pattern META_SCOPE_PATTERN = Pattern.compile(".*[ ,(]metaScope[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The metaImplements pattern. */
    private static final Pattern META_IMPLEMENTS_PATTERN = Pattern.compile(".*[ ,(]metaImplements[ ]*[=][ ]*[\"]([a-zA-Z0-9_.<>]*)[\"].*");
    /** The builderScope pattern. */
    private static final Pattern BUILDER_SCOPE_PATTERN = Pattern.compile(".*[ ,(]builderScope[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The builderStyle pattern. */
    private static final Pattern BUILDER_NAME_PATTERN = Pattern.compile(".*[ ,(]builderName[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The constructorScope pattern. */
    private static final Pattern CONSTRUCTOR_SCOPE_PATTERN = Pattern.compile(".*[ ,(]constructorScope[ ]*[=][ ]*[\"]([a-zA-Z@]*)[\"].*");
    /** The factoryName pattern. */
    private static final Pattern FACTORY_NAME_PATTERN = Pattern.compile(".*[ ,(]factoryName[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The hierarchy pattern. */
    private static final Pattern HIERARCHY_PATTERN = Pattern.compile(".*[ ,(]hierarchy[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");
    /** The cacheHashCode pattern. */
    private static final Pattern CACHE_HASH_CODE_PATTERN = Pattern.compile(".*[ ,(]cacheHashCode[ ]*[=][ ]*(true|false).*");
    /** The cloneStyle pattern. */
    private static final Pattern CLONE_STYLE_PATTERN = Pattern.compile(".*[ ,(]cloneStyle[ ]*[=][ ]*[\"]([a-zA-Z]*)[\"].*");

    /** The validator pattern. */
    private static final Pattern VALIDATOR_PATTERN = Pattern.compile(
            ".*private[ ]+void[ ]+" +
            "([a-zA-Z][a-zA-Z0-9]*)[(][ ]*[)].*");
    /** The defaults pattern. */
    private static final Pattern DEFAULTS_PATTERN = Pattern.compile(
            ".*private[ ]+static[ ]+void[ ]+" +
            "([a-zA-Z][a-zA-Z0-9]*)[(][ ]*Builder[ ]+[a-zA-Z][a-zA-Z0-9]*[ ]*[)].*");

    /** The content to process. */
    private final File file;
    /** The content to process. */
    private final List<String> content;
    /** The content to process. */
    private final BeanGenConfig config;
    /** The content to process. */
    private int beanDefIndex;
    /** The start position of auto-generation. */
    private int autoStartIndex;
    /** The end position of auto-generation. */
    private int autoEndIndex;
    /** The list of property generators. */
    private List<PropertyGen> properties;

    /**
     * Constructor.
     * @param file  the file to process, not null
     * @param content  the content to process, not null
     */
    BeanParser(File file, List<String> content, BeanGenConfig config) {
        this.file = file;
        this.content = content;
        this.config = config;
    }

    //-----------------------------------------------------------------------
    File getFile() {
        return file;
    }

    String getFieldPrefix() {
        return config.getPrefix();
    }

    BeanGenConfig getConfig() {
        return config;
    }

    //-----------------------------------------------------------------------
    BeanGen parse() {
        var data = new BeanData();
        beanDefIndex = parseBeanDefinition();
        data.getCurrentImports().addAll(parseImports(beanDefIndex));
        data.setImportInsertLocation(parseImportLocation(beanDefIndex));
        if (beanDefIndex < 0) {
            return new BeanGen(file, content, config, data);
        }
        data.setBeanStyle(parseBeanStyle(beanDefIndex));
        data.resolveBeanStyle(config.getDefaultStyle());
        if (!data.isBeanStyleValid()) {
            throw new BeanCodeGenException("Invalid bean style: " + data.getBeanStyle(), file, beanDefIndex);
        }
        data.setConstructorScope(parseConstructorScope(beanDefIndex));
        if (!data.isConstructorScopeValid()) {
            throw new BeanCodeGenException("Invalid constructor scope: " + data.getConstructorScope(), file, beanDefIndex);
        }
        data.setBeanMetaScope(parseBeanMetaScope(beanDefIndex));
        if (!data.isBeanMetaScopeValid()) {
            throw new BeanCodeGenException("Invalid meta-bean scope: " + data.getBeanMetaScope(), file, beanDefIndex);
        }
        data.setBeanMetaImplements(parseBeanMetaImplements(beanDefIndex));
        data.setBeanBuilderScope(parseBeanBuilderScope(beanDefIndex));
        if (!data.isBeanBuilderScopeValid()) {
            throw new BeanCodeGenException("Invalid bean builder scope: " + data.getBeanBuilderScope(), file, beanDefIndex);
        }
        data.setBeanBuilderName(parseBeanBuilderName(beanDefIndex));
        data.setFactoryName(parseFactoryName(beanDefIndex));
        data.setCacheHashCode(parseCacheHashCode(beanDefIndex));
        data.setCloneStyle(parseCloneStyle(beanDefIndex));
        if (!data.isCloneStyleValid()) {
            throw new BeanCodeGenException("Invalid clone style: " + data.getCloneStyle(), file, beanDefIndex);
        }
        data.setImmutableConstructor(parseImmutableConstructor(beanDefIndex));
        data.setConstructable(parseConstructable(beanDefIndex));
        data.setTypeParts(parseBeanType(beanDefIndex));
        var classHeaderAfterType = classHeaderAfterType(beanDefIndex, data.getType());
        data.setSuperTypeParts(parseBeanSuperType(classHeaderAfterType));
        data.setSerializable(parseSerializable(classHeaderAfterType));
        if (parseBeanHierarchy(beanDefIndex).equals("immutable")) {
            data.setImmutable(true);
            data.setConstructorStyle(CONSTRUCTOR_BY_BUILDER);
        } else if (data.getImmutableConstructor() == CONSTRUCTOR_NONE) {
            if (data.isImmutable()) {
                if (data.isTypeFinal()) {
                    data.setConstructorStyle(CONSTRUCTOR_BY_ARGS);
                } else {
                    data.setConstructorStyle(CONSTRUCTOR_BY_BUILDER);
                }
            } else {
                if (data.isBeanStyleLight()) {
                    data.setConstructorStyle(CONSTRUCTOR_BY_ARGS);
                } else {
                    data.setConstructorStyle(CONSTRUCTOR_BY_BUILDER);
                }
            }
        } else {
            data.setConstructorStyle(data.getImmutableConstructor());
        }
        if (data.isImmutable()) {
            data.setImmutableValidator(parseImmutableValidator(beanDefIndex));
            data.setImmutableDefaults(parseImmutableDefaults(beanDefIndex));
            data.setImmutablePreBuild(parseImmutablePreBuild(beanDefIndex));
            if (data.isBeanStyleLight() && !data.isTypeFinal()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Light beans must be declared final", file, beanDefIndex);
            }
            if (data.isBeanStyleMinimal() && !data.isTypeFinal()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Minimal beans must be declared final", file, beanDefIndex);
            }
            if (data.isFactoryRequired() && !data.isRootClass()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Factory method only allowed when bean has no bean superclass", file, beanDefIndex);
            }
            if (data.isFactoryRequired() && !data.isTypeFinal()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Factory method only allowed when bean is final", file, beanDefIndex);
            }
        } else {
            if (data.isBeanStyleLight() && !data.isTypeFinal()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Light beans must be declared final", file, beanDefIndex);
            }
            if (data.isBeanStyleMinimal() && !data.isTypeFinal()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Minimal beans must be declared final", file, beanDefIndex);
            }
            if (data.isFactoryRequired()) {
                throw new BeanCodeGenException(
                        "Invalid bean style: Factory method only allowed when bean is immutable", file, beanDefIndex);
            }
        }
        properties = parseProperties(data);
        autoStartIndex = parseStartAutogen();
        autoEndIndex = parseEndAutogen();
        data.setManualSerializationId(parseManualSerializationId(beanDefIndex));
        data.setManualClone(parseManualClone(beanDefIndex));
        data.setManualEqualsHashCode(parseManualEqualsHashCode(beanDefIndex));
        data.setManualToStringCode(parseManualToStringCode(beanDefIndex));
        if (data.isImmutable()) {
            for (var prop : properties) {
                if (!prop.getData().isDerived() && !prop.getData().isFinal()) {
                    throw new BeanCodeGenException("ImmutableBean must have final properties: " +
                            data.getTypeRaw() + "." + prop.getData().getFieldName(),
                            file, prop.getData().getLineIndex());
                }
            }
        } else {
            if (data.getImmutableConstructor() > CONSTRUCTOR_NONE) {
                throw new BeanCodeGenException("Mutable beans must not specify @ImmutableConstructor: " +
                        data.getTypeRaw(), file, beanDefIndex);
            }
            if (!"smart".equals(data.getConstructorScope()) && !data.isBeanStyleLight()) {
                throw new BeanCodeGenException("Mutable beans must not specify @BeanDefinition(constructorScope): " +
                                data.getTypeRaw(), file, beanDefIndex);
            }
        }
        if (data.isCacheHashCode()) {
            data.setCacheHashCode(data.isImmutable() && !data.isManualEqualsHashCode());
        }
        return new BeanGen(file, content, config, data, properties, autoStartIndex, autoEndIndex);
    }

    private String classHeaderAfterType(int defLine, String fullType) {
        var buf = new StringBuilder(128);
        var matchedType = false;
        for (var index = defLine; index < content.size(); index++) {
            var line = content.get(index);
            if (!matchedType) {
                if (!line.contains(fullType)) {
                    continue;
                }
                matchedType = true;
                line = line.substring(line.indexOf(fullType) + fullType.length());
            }
            buf.append(line).append(' ');
            if (line.trim().endsWith("{") && !line.trim().startsWith("@")) {
                break;
            }
        }
        return buf.toString().trim();
    }

    //-----------------------------------------------------------------------
    private int parseBeanDefinition() {
        for (var index = 0; index < content.size(); index++) {
            var line = content.get(index).trim();
            if (line.startsWith("@BeanDefinition")) {
                return index;
            }
        }
        return -1;
    }

    private Set<String> parseImports(int defLine) {
        var end = defLine < 0 ? content.size() : defLine;
        Set<String> imports = new HashSet<>();
        for (var index = 0; index < end; index++) {
            if (content.get(index).startsWith("import ")) {
                var imp = content.get(index).substring(7).trim();
                imp = imp.substring(0, imp.indexOf(';'));
                if (!imp.endsWith(".*")) {
                    imports.add(imp);
                }
            }
        }
        return imports;
    }

    private int parseImportLocation(int defLine) {
        var end = defLine < 0 ? content.size() : defLine;
        var location = 0;
        for (var index = 0; index < end; index++) {
            if (content.get(index).startsWith("import ") || content.get(index).startsWith("package ")) {
                location = index;
            }
        }
        return location;
    }

    private String parseBeanStyle(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = STYLE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseConstructorScope(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = CONSTRUCTOR_SCOPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseBeanMetaScope(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = META_SCOPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseBeanMetaImplements(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = META_IMPLEMENTS_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseBeanBuilderScope(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = BUILDER_SCOPE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private String parseBeanBuilderName(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = BUILDER_NAME_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseFactoryName(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = FACTORY_NAME_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private String parseBeanHierarchy(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = HIERARCHY_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "";
    }

    private boolean parseCacheHashCode(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = CACHE_HASH_CODE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return Boolean.parseBoolean(matcher.group(1));
        }
        return false;
    }

    private String parseCloneStyle(int defLine) {
        var line = content.get(defLine).trim();
        var matcher = CLONE_STYLE_PATTERN.matcher(line);
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return "smart";
    }

    private boolean parseConstructable(int defLine) {
        for (var index = defLine; index < content.size(); index++) {
            if (content.get(index).contains(" abstract class ")) {
                return false;
            }
        }
        return true;
    }

    private String[] parseBeanType(int defLine) {
        var matcher = BEAN_TYPE.matcher("");
        for (var index = defLine; index < content.size(); index++) {
            var line = content.get(index);
            matcher.reset(line);
            if (matcher.matches()) {
                var startStr = line.substring(0, matcher.start(1));
                var fnl = startStr.contains(" final ") || startStr.startsWith("final ") ? "final" : null;
                var scope = startStr.contains("public ") ? "public" : "package";
                scope = startStr.contains("protected ") ? "protected" : scope;
                scope = startStr.contains("private ") ? "private" : scope;
                return new String[] {fnl, scope, matcher.group(1), matcher.group(2), matcher.group(3),
                        matcher.group(4), matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8)};
            }
            if (line.contains(AUTOGENERATED_START_TEXT)) {
                break;
            }
        }
        throw new BeanCodeGenException("Unable to locate bean class name", file, beanDefIndex);
    }

    private String[] parseBeanSuperType(String classHeaderAfterType) {
        // this uses classHeaderAfterType as extends has two meanings in class headers
        // search for implements
        var matcherImplements = SUPER_IMPL_TYPE.matcher(classHeaderAfterType);
        if (matcherImplements.matches()) {
            return new String[] {matcherImplements.group(1)};
        }
        // search for extends
        var matcherExtends = SUPER_TYPE.matcher(classHeaderAfterType);
        if (matcherExtends.matches()) {
            return new String[] {matcherExtends.group(1), matcherExtends.group(2), matcherExtends.group(3),
                    matcherExtends.group(4), matcherExtends.group(5)};
        }
        throw new BeanCodeGenException("Unable to locate bean superclass", file, beanDefIndex);
    }

    private boolean parseSerializable(String classHeaderAfterType) {
        return SERIALIZABLE_TYPE.matcher(classHeaderAfterType).matches();
    }

    private boolean parseManualSerializationId(int defLine) {
        for (var index = defLine; index < autoStartIndex; index++) {
            if (content.get(index).trim().startsWith("private static final long serialVersionUID")) {
                return true;
            }
        }
        return false;
    }

    private int parseImmutableConstructor(int defLine) {
        var found = CONSTRUCTOR_NONE;
        for (var index = defLine; index < content.size(); index++) {
            if (content.get(index).trim().equals("@ImmutableConstructor")) {
                if (found > 0) {
                    throw new BeanCodeGenException("Only one @ImmutableConstructor may be specified", file, index);
                }
                found = CONSTRUCTOR_BY_ARGS;
                if (index + 1 < content.size()) {
                    var nextLine = content.get(index + 1);
                    if (nextLine.contains("Builder ") || nextLine.contains("Builder<")) {
                        found = CONSTRUCTOR_BY_BUILDER;
                    }
                }
            }
        }
        return found;
    }

    private String parseImmutableValidator(int defLine) {
        var found = false;
        for (var index = defLine; index < content.size(); index++) {
            if (content.get(index).trim().equals("@ImmutableValidator")) {
                if (found) {
                    throw new BeanCodeGenException("Only one @ImmutableValidator may be specified", file, index);
                }
                found = true;
                if (index + 1 < content.size()) {
                    var nextLine = content.get(index + 1);
                    var matcher = VALIDATOR_PATTERN.matcher(nextLine);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                    throw new BeanCodeGenException(
                        "@ImmutableValidator method must be private void and no-args", file, index + 1);
                }
            }
        }
        return null;
    }

    private String parseImmutableDefaults(int defLine) {
        var found = false;
        for (var index = defLine; index < content.size(); index++) {
            if (content.get(index).trim().equals("@ImmutableDefaults")) {
                if (found) {
                    throw new BeanCodeGenException("Only one @ImmutableDefaults may be specified", file, index);
                }
                found = true;
                if (index + 1 < content.size()) {
                    var nextLine = content.get(index + 1);
                    var matcher = DEFAULTS_PATTERN.matcher(nextLine);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                    throw new BeanCodeGenException(
                        "@ImmutableDefaults method must be private static void and have one argument of type 'Builder'",
                        file, index + 1);
                }
            }
        }
        return null;
    }

    private String parseImmutablePreBuild(int defLine) {
        var found = false;
        for (var index = defLine; index < content.size(); index++) {
            if (content.get(index).trim().equals("@ImmutablePreBuild")) {
                if (found) {
                    throw new BeanCodeGenException("Only one @ImmutablePreBuild may be specified", file, index);
                }
                found = true;
                if (index + 1 < content.size()) {
                    var nextLine = content.get(index + 1);
                    var matcher = DEFAULTS_PATTERN.matcher(nextLine);
                    if (matcher.matches()) {
                        return matcher.group(1);
                    }
                    throw new BeanCodeGenException(
                        "@ImmutablePreBuild method must be private static void and have one argument of type 'Builder'",
                        file, index + 1);
                }
            }
        }
        return null;
    }

    private List<PropertyGen> parseProperties(BeanData data) {
        List<PropertyGen> props = new ArrayList<>();
        for (var index = 0; index < content.size(); index++) {
            var line = content.get(index).trim();
            var parser = new PropertyParser(this);
            if (line.startsWith("@PropertyDefinition")) {
                var prop = parser.parse(data, content, index);
                props.add(prop);
                data.getProperties().add(prop.getData());
            } else if (line.startsWith("@DerivedProperty")) {
                var prop = parser.parseDerived(data, content, index);
                props.add(prop);
                data.getProperties().add(prop.getData());
            }
        }
        return props;
    }

    private int parseStartAutogen() {
        for (var index = 0; index < content.size(); index++) {
            var line = content.get(index).trim();
            if (line.contains(" AUTOGENERATED START ")) {
                content.set(index, AUTOGENERATED_START);
                return index;
            }
        }
        for (var index = content.size() - 1; index >= 0; index--) {
            var line = content.get(index).trim();
            if (line.equals("}")) {
                content.add(index, AUTOGENERATED_START);
                return index;
            }
            if (!line.isEmpty()) {
                break;
            }
        }
        throw new BeanCodeGenException("Unable to locate start autogeneration point", file, beanDefIndex);
    }

    private int parseEndAutogen() {
        for (var index = autoStartIndex; index < content.size(); index++) {
            var line = content.get(index).trim();
            if (line.contains(" AUTOGENERATED END ")) {
                content.set(index, AUTOGENERATED_END);
                return index;
            }
        }
        content.add(autoStartIndex + 1, AUTOGENERATED_END);
        return autoStartIndex + 1;
    }

    private boolean parseManualClone(int defLine) {
        for (var index = defLine; index < autoStartIndex; index++) {
            var line = content.get(index).trim();
            if (line.startsWith("public ") && line.endsWith(" clone() {")) {
                return true;
            }
        }
        return false;
    }

    private boolean parseManualEqualsHashCode(int defLine) {
        for (var index = defLine; index < autoStartIndex; index++) {
            var line = content.get(index).trim();
            if (line.equals("public int hashCode() {") || (line.startsWith("public boolean equals(") && line.endsWith(") {"))) {
                return true;
            }
        }
        return false;
    }

    private boolean parseManualToStringCode(int defLine) {
        for (var index = defLine; index < autoStartIndex; index++) {
            var line = content.get(index).trim();
            if (line.equals("public String toString() {")) {
                return true;
            }
        }
        return false;
    }

}
