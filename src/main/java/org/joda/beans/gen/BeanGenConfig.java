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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for the code generator.
 */
public final class BeanGenConfig {

    /**
     * The copy generators.
     */
    private final Map<String, CopyGen> copyGenerators;
    /**
     * The builder types.
     */
    private final Map<String, String> builderTypes;
    /**
     * The builder generators.
     */
    private final Map<String, BuilderGen> builderGenerators;
    /**
     * The invalid immutable types.
     */
    private final Set<String> invalidImmutableTypes;
    /**
     * The immutable varargs code.
     */
    private final Map<String, String> immutableVarArgs;
    /**
     * The immutable get clones.
     */
    private final Map<String, String> immutableGetClones;
    /**
     * The indent to use.
     */
    private String indent = "    ";
    /**
     * The field prefix to use.
     */
    private String prefix = "";
    /**
     * The end of line separator
     */
    private String eol = System.lineSeparator();
    /**
     * The default style to use.
     */
    private String defaultStyle = "smart";
    /**
     * Whether to add the generated annotation.
     */
    private boolean generatedAnno;

    /**
     * Parses the configuration file.
     * <p>
     * This loads the file as an ini file in this package.
     * 
     * @param resourceLocator  the configuration resource locator, not null
     * @return the configuration
     */
    public static BeanGenConfig parse(String resourceLocator) {
        var fileName = createConfigFileName(resourceLocator);
        try (var in = BeanGenConfig.class.getResourceAsStream(fileName)) {
            if (in == null) {
                throw new IllegalArgumentException("Configuration file not found in classpath: " + fileName);
            }
            try (var reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                var lines = reader.lines()
                        .filter(line -> !line.trim().startsWith("#") && !line.trim().isEmpty())
                        .toList();
                return parse(lines);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    private static String createConfigFileName(String resourceLocator) {
        if (resourceLocator.contains("/") || resourceLocator.endsWith(".ini")) {
            return resourceLocator.startsWith("/") ? resourceLocator : "/" + resourceLocator;
        } else if (resourceLocator.equals("jdk6")) {  // compatibility
            return "/org/joda/beans/gen/jdk.ini";
        } else {
            return "/org/joda/beans/gen/" + resourceLocator + ".ini";
        }
    }

    private static BeanGenConfig parse(List<String> lines) {
        var immutableCopiers = new HashMap<String, String>();
        var mutableCopiers = new HashMap<String, String>();
        var immutableGetClones = new HashMap<String, String>();
        var immutableVarArgs = new HashMap<String, String>();
        var builderInits = new HashMap<String, String>();
        var builderTypes = new HashMap<String, String>();
        var invalidImmutableTypes = new HashSet<String>();
        for (var iterator = lines.listIterator(); iterator.hasNext();) {  // CSIGNORE
            var line = iterator.next().trim();
            switch (line) {
                case "[immutable.builder.to.immutable]" -> {
                    parseConfig(iterator, immutableCopiers);
                }
                case "[immutable.builder.to.mutable]" -> {
                    parseConfig(iterator, mutableCopiers);
                }
                case "[immutable.invalid.type]" -> {
                    while (iterator.hasNext()) {
                        line = iterator.next().trim();
                        var pos = line.indexOf('=');
                        if (pos <= 0) {
                            throw new IllegalArgumentException("Invalid ini file line: " + line);
                        }
                        var key = line.substring(0, pos).trim();
                        invalidImmutableTypes.add(key);
                    }
                }
                case "[immutable.get.clone]" -> {
                    while (iterator.hasNext()) {
                        line = iterator.next().trim();
                        if (line.startsWith("[")) {
                            iterator.previous();
                            break;
                        }
                        var pos = line.indexOf('=');
                        if (pos <= 0) {
                            throw new IllegalArgumentException("Invalid ini file line: " + line);
                        }
                        var key = line.substring(0, pos).trim();
                        var value = line.substring(pos + 1).trim();
                        if (!value.equals("clone") && !value.equals("cloneCast") && !value.equals("cloneArray")) {
                            throw new IllegalArgumentException("Value for [immutable.get.clone] must be 'clone', 'cloneCast' or 'cloneArray'");
                        }
                        immutableGetClones.put(key, value);
                    }
                }
                case "[immutable.builder.varargs]" -> {
                    parseConfig(iterator, immutableVarArgs);
                }
                case "[immutable.builder.type]" -> {
                    parseConfig(iterator, builderTypes);
                }
                case "[immutable.builder.init]" -> {
                    parseConfig(iterator, builderInits);
                }
                default -> throw new IllegalArgumentException("Invalid ini file section: " + line);
            }
        }
        // adjust to results
        var builderGenerators = new HashMap<String, BuilderGen>();
        for (var entry : builderInits.entrySet()) {
            var type = builderTypes.get(entry.getKey());
            if (type == null) {
                type = entry.getKey() + "<>";
            }
            builderGenerators.put(entry.getKey(), new BuilderGen.PatternBuilderGen(type, entry.getValue()));
        }
        var copyGenerators = new HashMap<String, CopyGen>();
        for (var entry : immutableCopiers.entrySet()) {
            var fieldType = entry.getKey();
            var immutableCopier = entry.getValue();
            var mutableCopier = mutableCopiers.get(fieldType);
            if (mutableCopier == null) {
                throw new IllegalArgumentException("[immutable.builder.to.immutable] and [immutable.builder.to.mutable] entries must match: " + fieldType);
            }
            copyGenerators.put(fieldType, new CopyGen.PatternCopyGen(immutableCopier, mutableCopier, false));
        }
        return new BeanGenConfig(copyGenerators, builderGenerators, builderTypes, invalidImmutableTypes, immutableVarArgs, immutableGetClones);
    }

    private static void parseConfig(ListIterator<String> iterator, HashMap<String, String> mutableMap) {
        while (iterator.hasNext()) {
            var line = iterator.next().trim();
            if (line.startsWith("[")) {
                iterator.previous();
                break;
            }
            var pos = line.indexOf('=');
            if (pos <= 0) {
                throw new IllegalArgumentException("Invalid ini file line: " + line);
            }
            var key = line.substring(0, pos).trim();
            var value = line.substring(pos + 1).trim();
            mutableMap.put(key, value);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     * 
     * @param copyGenerators  the copy generators, not null
     * @param builderGenerators  the builder generators, not null
     * @param builderTypes  the builder types, not null
     * @param invalidImmutableTypes  the invalid immutable types, not null
     * @param immutableVarArgs  the varargs code
     * @param immutableGetClones  the get clone code
     */
    private BeanGenConfig(
            Map<String, CopyGen> copyGenerators,
            Map<String, BuilderGen> builderGenerators,
            Map<String, String> builderTypes,
            Set<String> invalidImmutableTypes,
            Map<String, String> immutableVarArgs,
            Map<String, String> immutableGetClones) {
        this.copyGenerators = copyGenerators;
        this.builderGenerators = builderGenerators;
        this.builderTypes = builderTypes;
        this.invalidImmutableTypes = invalidImmutableTypes;
        this.immutableVarArgs = immutableVarArgs;
        this.immutableGetClones = immutableGetClones;
    }

    //-----------------------------------------------------------------------
    /**
     * The copy generators.
     * 
     * @return the generators, not null
     */
    Map<String, CopyGen> getCopyGenerators() {
        return copyGenerators;
    }

    /**
     * The builder generators.
     * 
     * @return the generators, not null
     */
    Map<String, BuilderGen> getBuilderGenerators() {
        return builderGenerators;
    }

    /**
     * The builder types.
     * 
     * @return the types, not null
     */
    public Map<String, String> getBuilderTypes() {
        return builderTypes;
    }

    /**
     * The invalid immutable types.
     * 
     * @return the invalid immutable types, not null
     */
    public Set<String> getInvalidImmutableTypes() {
        return invalidImmutableTypes;
    }

    /**
     * The builder varargs code.
     * 
     * @return the varargs, not null
     */
    public Map<String, String> getImmutableVarArgs() {
        return immutableVarArgs;
    }

    /**
     * The builder types.
     * 
     * @return the types, not null
     */
    public Map<String, String> getImmutableGetClones() {
        return immutableGetClones;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the indent to use.
     * 
     * @return the indent, not null
     */
    public String getIndent() {
        return indent;
    }

    /**
     * Sets the indent to use.
     * 
     * @param indent  the indent to use, not null
     */
    public void setIndent(String indent) {
        this.indent = indent;
    }

    /**
     * Gets the prefix to use.
     * 
     * @return the prefix, not null
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Sets the prefix to use.
     * 
     * @param prefix  the prefix to use, not null
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    /**
     * Gets the end of line separator to use.
     * 
     * @return the end of line, not null
     */
    public String getEol() {
        return eol;
    }

    /**
     * Sets the end of line separator to use.
     * 
     * @param eol  the end of line separator to use, not null
     */
    public void setEol(String eol) {
        this.eol = eol;
    }

    /**
     * Gets the default style to use.
     * 
     * @return the default style, not null
     */
    public String getDefaultStyle() {
        return defaultStyle;
    }

    /**
     * Sets the default style to use.
     * 
     * @param defaultStyle  the default style to use, not null
     */
    public void setDefaultStyle(String defaultStyle) {
        this.defaultStyle = defaultStyle;
    }

    /**
     * Gets whether to add the generated annotation.
     * 
     * @return whether to add the generated annotation, not null
     */
    public boolean isGeneratedAnno() {
        return generatedAnno;
    }

    /**
     * Sets whether to add the generated annotation.
     * 
     * @param generatedAnno  whether to add the generated annotation
     */
    public void setGeneratedAnno(boolean generatedAnno) {
        this.generatedAnno = generatedAnno;
    }

}
