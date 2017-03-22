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

import static java.util.stream.Collectors.joining;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.joda.beans.PropertyStyle;

/**
 * A bean that can be generated.
 * 
 * @author Stephen Colebourne
 */
class PropertyData {

    /** Collection types. */
    private static final Set<String> COLLECTIONS = new HashSet<>(
            Arrays.asList(
                    "Collection", "Set", "SortedSet", "NavigableSet", "List",
                    "ArrayList", "LinkedList",
                    "HashSet", "LinkedHashSet", "TreeSet", "ConcurrentSkipListSet, EnumSet",
                    "ImmutableCollection", "ImmutableList", "ImmutableSet", "ImmutableSortedSet"));
    /** Set types. */
    private static final Set<String> SETS = new HashSet<>(
            Arrays.asList(
                    "Set", "SortedSet", "NavigableSet",
                    "HashSet", "LinkedHashSet", "TreeSet", "ConcurrentSkipListSet, EnumSet",
                    "ImmutableSet", "ImmutableSortedSet"));
    /** Set types. */
    private static final Set<String> SORTED_SETS = new HashSet<>(
            Arrays.asList(
                    "SortedSet", "NavigableSet",
                    "TreeSet", "ConcurrentSkipListSet",
                    "ImmutableSortedSet"));
    /** Map types. */
    private static final Set<String> MAPS = new HashSet<>(
            Arrays.asList(
                    "Map", "SortedMap", "NavigableMap", "ConcurrentMap", "ConcurrentNavigableMap",
                    "HashMap", "LinkedHashMap", "TreeMap", "ConcurrentHashMap", "ConcurrentSkipListMap",
                    "BiMap", "HashBiMap",
                    "ImmutableMap", "ImmutableSortedMap", "ImmutableBiMap"));

    /** Owning bean. */
    private final BeanData bean;
    /** Annotation line index for {@code PropertyDefinition} in input file. */
    private final int lineIndex;
    /** Property name. */
    private String propertyName;
    /** Field name. */
    private String fieldName;
    /** Meta field name. */
    private String metaFieldName;
    /** Upper property name. */
    private String upperName;
    /** Property type. */
    private String type;
    /** The builder type. */
    private String builderType;
    /** Property field type. */
    private String fieldType;
    /** Whether the field is declared final. */
    private boolean isFinal;
    /** The field initializer. */
    private String initializer;
    /** The alias. */
    private String alias;
    /** The getter style. */
    private String getStyle;
    /** The setter style. */
    private String setStyle;
    /** The override style. */
    private boolean overrideGet;
    /** The override style. */
    private boolean overrideSet;
    /** The type style. */
    private String typeStyle;
    /** The builder type style. */
    private String builderTypeStyle;
    /** The equals hashCode style. */
    private String equalsHashCodeStyle;
    /** The toString style. */
    private String toStringStyle;
    /** The validation string. */
    private String validation;
    /** Deprecated flag. */
    private boolean deprecated;
    /** First comment about the property. */
    private String firstComment;
    /** Other comments about the property. */
    private final List<String> comments = new ArrayList<>();
    /** The getter generator. */
    private GetterGen getterGen;
    /** The setter generator. */
    private SetterGen setterGen;
    /** The flag for bound properties. */
    private boolean bound;
    /** The copy generator. */
    private CopyGen copyGen;
    /** The builder generator. */
    private BuilderGen builderGen;
    /** The config. */
    private BeanGenConfig config;

    /**
     * Constructor.
     */
    PropertyData(BeanData bean, BeanGenConfig config, int lineIndex) {
        this.bean = bean;
        this.config = config;
        this.lineIndex = lineIndex;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean.
     * @return the bean, not null
     */
    public BeanData getBean() {
        return bean;
    }

    /**
     * Gets the configuration.
     * 
     * @return the configuration, not null
     */
    public BeanGenConfig getConfig() {
        return config;
    }

    /**
     * Sets the configuration.
     * 
     * @param config  the new configuration, not null
     */
    public void setConfig(BeanGenConfig config) {
        this.config = config;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the property line index.
     * @return the property line index
     */
    public int getLineIndex() {
        return lineIndex;
    }

    /**
     * Gets the property name.
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Sets the property name.
     * @param propertyName  the property name to set
     */
    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    /**
     * Gets the field name.
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name.
     * @param fieldName  the field name to set
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the meta field name.
     * @return the meta field name
     */
    public String getMetaFieldName() {
        return metaFieldName;
    }

    /**
     * Sets the meta field name.
     * @param metaFieldName  the meta field name to set
     */
    public void setMetaFieldName(String metaFieldName) {
        this.metaFieldName = metaFieldName;
    }

    /**
     * Gets the upper property name.
     * @return the upper name
     */
    public String getUpperName() {
        return upperName;
    }

    /**
     * Sets the upper property name.
     * @param upperName  the upper name to set
     */
    public void setUpperName(String upperName) {
        this.upperName = upperName;
    }

    /**
     * Gets the type.
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type.
     * @param type  the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the builder type.
     * @return the type
     */
    public String getBuilderType() {
        return builderType;
    }

    /**
     * Sets the builder type.
     * @param builderType  the type to set
     */
    public void setBuilderType(String builderType) {
        this.builderType = builderType;
    }

    /**
     * Gets the field type.
     * @return the field type
     */
    public String getFieldType() {
        return fieldType;
    }

    /**
     * Sets the field type.
     * @param fieldType  the field type to set
     */
    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Resolves the field type.
     */
    public void resolveType() {
        if (getTypeStyle() == null) {
            setTypeStyle("");
        }
        final String fieldType = getFieldType();
        String generics = "";
        if (fieldType.contains("<")) {
            generics = fieldType.substring(fieldType.indexOf('<'));
        }
        if (getTypeStyle().equals("smart")) {
            setType(fieldType);
        } else if (getTypeStyle().length() > 0) {
            if (getTypeStyle().contains("<>")) {
                setType(getTypeStyle().replace("<>", generics));
            } else if (getTypeStyle().contains("<")) {
                setType(getTypeStyle());
            } else {
                setType(getTypeStyle() + generics);
            }
        } else {
            setType(fieldType);
        }
    }

    /**
     * Resolves the field builder type.
     */
    public void resolveBuilderType() {
        if (getBuilderTypeStyle() == null) {
            setBuilderTypeStyle("");
        }
        final String fieldType = getFieldType();
        String generics = "";
        if (fieldType.contains("<")) {
            generics = fieldType.substring(fieldType.indexOf('<'));
        }
        if (getBuilderTypeStyle().equals("smart")) {
            setBuilderType(fieldType);
        } else if (getBuilderTypeStyle().length() > 0) {
            if (getBuilderTypeStyle().contains("<>")) {
                setBuilderType(getBuilderTypeStyle().replace("<>", generics));
            } else if (getBuilderTypeStyle().contains("<")) {
                setBuilderType(getBuilderTypeStyle());
            } else {
                setBuilderType(getBuilderTypeStyle() + generics);
            }
        } else {
            setBuilderType(fieldType);
        }
    }

    /**
     * Gets whether the field is declared final.
     * @return the type
     */
    public boolean isFinal() {
        return isFinal;
    }

    /**
     * Sets whether the field is declared final.
     * @param isFinal  the field is final flag
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
    }

    /**
     * Gets the field initializer.
     * @return the initializer
     */
    public String getInitializer() {
        return initializer;
    }

    /**
     * Sets the field initializer.
     * @param initializer  the field initializer
     */
    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }

    /**
     * Gets the alias.
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets the alias.
     * @param alias  the alias
     */
    public void setAlias(String alias) {
        this.alias = (alias != null && alias.isEmpty() == false ? alias : null);
    }

    /**
     * Gets the getter style.
     * @return the getter style
     */
    public String getGetStyle() {
        return getStyle;
    }

    /**
     * Sets the getter style.
     * @param getStyle  the getter style to set
     */
    public void setGetStyle(String getStyle) {
        this.getStyle = getStyle;
    }

    /**
     * Gets the type style.
     * @return the type style
     */
    public String getTypeStyle() {
        return typeStyle;
    }

    /**
     * Sets the type style.
     * @param typeStyle  the type style to set
     */
    public void setTypeStyle(String typeStyle) {
        this.typeStyle = typeStyle;
    }

    /**
     * Gets the type builder type style.
     * @return the builder type style
     */
    public String getBuilderTypeStyle() {
        return builderTypeStyle;
    }

    /**
     * Sets the builder type style.
     * @param builderTypeStyle  the builder type style to set
     */
    public void setBuilderTypeStyle(String builderTypeStyle) {
        this.builderTypeStyle = builderTypeStyle;
    }

    /**
     * Gets the equals hashCode style.
     * @return the equals hashCode style
     */
    public String getEqualsHashCodeStyle() {
        return equalsHashCodeStyle;
    }

    /**
     * Sets the equals hashCode style.
     * @param equalsHashCodeStyle  the equals hashCode style
     */
    public void setEqualsHashCodeStyle(String equalsHashCodeStyle) {
        this.equalsHashCodeStyle = equalsHashCodeStyle;
    }

    /**
     * Resolves the equals hashCode generator.
     * @param file  the file
     * @param lineIndex  the line index
     */
    public void resolveEqualsHashCodeStyle(File file, int lineIndex) {
        if (equalsHashCodeStyle.equals("smart")) {
            equalsHashCodeStyle = (bean.isImmutable() ? "field" : "getter");
        }
        if (equalsHashCodeStyle.equals("omit") ||
                equalsHashCodeStyle.equals("getter") ||
                equalsHashCodeStyle.equals("field")) {
            return;
        }
        throw new BeanCodeGenException("Invalid equals/hashCode style: " + equalsHashCodeStyle +
                " in " + getBean().getTypeRaw() + "." + getPropertyName(), file, lineIndex);
    }

    /**
     * Gets the toString style.
     * @return the toString style
     */
    public String getToStringStyle() {
        return toStringStyle;
    }

    /**
     * Sets the toString style.
     * @param toStringStyle  the toString style
     */
    public void setToStringStyle(String toStringStyle) {
        this.toStringStyle = toStringStyle;
    }

    /**
     * Resolves the toString generator.
     * @param file  the file
     * @param lineIndex  the line index
     */
    public void resolveToStringStyle(File file, int lineIndex) {
        if (toStringStyle.equals("smart")) {
            toStringStyle = (bean.isImmutable() ? "field" : "getter");
        }
        if (toStringStyle.equals("omit") ||
                toStringStyle.equals("getter") ||
                toStringStyle.equals("field")) {
            return;
        }
        throw new BeanCodeGenException("Invalid toString style: " + toStringStyle +
                " in " + getBean().getTypeRaw() + "." + getPropertyName(), file, lineIndex);
    }

    /**
     * Gets the setter style.
     * @return the setter style
     */
    public String getSetStyle() {
        return setStyle;
    }

    /**
     * Sets the setter style.
     * @param setStyle  the setter style to set
     */
    public void setSetStyle(String setStyle) {
        this.setStyle = setStyle;
    }

    /**
     * Gets the override get flag.
     * @return the setter style
     */
    public boolean isOverrideGet() {
        return overrideGet;
    }

    /**
     * Sets the override get flag.
     * @param overrideGet  the setter style to set
     */
    public void setOverrideGet(boolean overrideGet) {
        this.overrideGet = overrideGet;
    }

    /**
     * Gets the override get flag.
     * @return the setter style
     */
    public boolean isOverrideSet() {
        return overrideSet;
    }

    /**
     * Sets the override get flag.
     * @param overrideSet  the setter style to set
     */
    public void setOverrideSet(boolean overrideSet) {
        this.overrideSet = overrideSet;
    }

    /**
     * Gets the validation.
     * @return the validation
     */
    public String getValidation() {
        return validation;
    }

    /**
     * Sets the validation.
     * @param validation  the validation to set
     */
    public void setValidation(String validation) {
        this.validation = validation;
    }

    /**
     * Resolves validation.
     */
    public void resolveValidation() {
        if (isFinal() && getInitializer().length() > 0 &&  getValidation().length() == 0) {
            setValidation("notNull");
        }
    }

    /**
     * Checks if the property is deprecated.
     * @return the deprecated flag
     */
    public boolean isDeprecated() {
        return deprecated;
    }

    /**
     * Sets if the property is deprecated.
     * @param deprecated  the deprecated to set
     */
    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    /**
     * Gets the first comment line.
     * @return the first comment
     */
    public String getFirstComment() {
        return firstComment;
    }

    /**
     * Sets the first comment line.
     * @param firstComment  the first comment to set
     */
    public void setFirstComment(String firstComment) {
        this.firstComment = firstComment;
    }

    /**
     * Gets the remaining comments.
     * @return the remaining comments, not null
     */
    public List<String> getComments() {
        return comments;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the property is parameterised with generics.
     * @return true if generified
     */
    public boolean isGenericParamType() {
        return type.indexOf("<") >= 0;
    }

    /**
     * Checks if the property is parameterised with generics.
     * {@code Foo<?>} will return true, {@code Foo<? extends Number> will return false}.
     * @return true if generified
     */
    public boolean isGenericWildcardParamType() {
        return type.endsWith("<?>");
    }

    /**
     * Gets the parameterisation of the property.
     * {@code Foo<String>} will return {@code String}.
     * @return the generic type, or a blank string if not generic, not null
     */
    public String getGenericParamType() {
        int pos = type.indexOf("<");
        if (pos < 0) {
            return "";
        }
        return type.substring(pos + 1, type.length() - 1);
    }

    /**
     * Checks if the type is the generic type of the bean.
     * For example, if the property is of type T or T[] in a bean of Foo[T].
     * @return true if matches
     */
    public boolean isBeanGenericType() {
        String stripped = type;
        if (isArrayType()) {
            stripped = type.substring(0, type.length() - 2);
        }
        return bean.isTypeGenerifiedBy(stripped);
    }

    /**
     * Checks if the property is generic in some way.
     * @return true if generic
     */
    public boolean isGeneric() {
        return isGenericParamType() || isBeanGenericType();
    }

    /**
     * Gets the raw type of the property without generics.
     * {@code Foo<String>} will return {@code Foo}.
     * @return the raw type
     */
    public String getTypeRaw() {
        int pos = type.indexOf("<");
        return (pos < 0 ? type : type.substring(0, pos));
    }

    /**
     * Gets the raw type of the property.
     * @return the raw type
     */
    public String getFieldTypeRaw() {
        int pos = fieldType.indexOf("<");
        return (pos < 0 ? fieldType : fieldType.substring(0, pos));
    }

    /**
     * Gets the generic part of the property type.
     * <p>
     * For example, "{@literal Foo<String>}" will return "{@literal <String>}".
     * 
     * @return the generic part of the type, not null
     */
    public String getTypeGenerics() {
        final String type = getType();
        if (type.contains("<")) {
            return type.substring(type.indexOf('<'));
        }
        return "";
    }

    /**
     * Gets the generic part of the property type.
     * <p>
     * For example, "{@literal Foo<String>}" will return "String".
     * 
     * @return the generic part of the type, empty if not generic, not null
     */
    public String getTypeGenericsSimple() {
        final String type = getType();
        if (type.contains("<")) {
            return type.substring(type.indexOf('<') + 1, type.length() - 1);
        }
        return "";
    }

    /**
     * Gets the type of the property, erasing generics attached to the bean.
     * {@code Foo<T>} will return {@code Foo<Number>} where {@code T extends Number}.
     * @return the raw type
     */
    public String getTypeBeanErased() {
        if (isBeanGenericType()) {
            for (int i = 0; i < bean.getTypeGenericCount(); i++) {
                if (type.equals(bean.getTypeGenericName(i, false))) {
                    return bean.getTypeGenericErased(i);
                }
            }
        }
        String generic = getTypeGenericsSimple();
        if (generic.isEmpty()) {
            return type;
        }
        StringTokenizer tkn = new StringTokenizer(generic, ",");
        List<String> altered = new ArrayList<>();
        while (tkn.hasMoreTokens()) {
            String genericType = tkn.nextToken().trim();
            String erased = genericType;
            if (bean.isTypeGenerifiedBy(genericType)) {
                for (int i = 0; i < bean.getTypeGenericCount(); i++) {
                    if (genericType.equals(bean.getTypeGenericName(i, false))) {
                        erased = bean.getTypeGenericErased(i);
                    }
                }
            }
            altered.add(erased);
        }
        return getTypeRaw() + "<" + altered.stream().collect(joining(", ")) + ">";
    }

    /**
     * Checks if the property is derived.
     * @return true if derived
     */
    public boolean isDerived() {
        return fieldName == null;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the getter generator.
     * @param file  the file
     * @param lineIndex  the line index
     */
    public void resolveGetterGen(File file, int lineIndex) {
        if (getGetStyle() == null) {
            setGetStyle("");
        }
        String style = getGetStyle();
        String access = "public";
        if (style.equals("private")) {
            style = "smart";
            access = "private";
        } else if (style.equals("package")) {
            style = "smart";
            access = "package";
        } else if (style.equals("protected")) {
            style = "smart";
            access = "protected";
        }
        if (style.equals("get")) {
            getterGen = GetterGen.GetGetterGen.PUBLIC;
        } else if (style.equals("is")) {
            getterGen = GetterGen.IsGetterGen.PUBLIC;
        } else if (style.equals("smart")) {
            if (bean.isImmutable()) {
                String clone = config.getImmutableGetClones().get(getFieldTypeRaw());
                if ("clone".equals(clone)) {
                    getterGen = isNotNull() ?
                                    GetterGen.CloneNNGetterGen.of(access) :
                                    GetterGen.CloneGetterGen.of(access);
                } else if ("cloneCast".equals(clone)) {
                    getterGen = isNotNull() ?
                                    GetterGen.CloneCastNNGetterGen.of(access) :
                                    GetterGen.CloneCastGetterGen.of(access);
                } else if (getType().equals("boolean")) {
                    getterGen = GetterGen.IsGetterGen.of(access);
                } else {
                    getterGen = GetterGen.GetGetterGen.of(access);
                }
            } else if (getType().equals("boolean")) {
                getterGen = GetterGen.IsGetterGen.of(access);
            } else {
                getterGen = GetterGen.GetGetterGen.of(access);
            }
        } else if (style.equals("")) {
            getterGen = GetterGen.NoGetterGen.INSTANCE;
        } else if (style.equals("field")) {
            getterGen = GetterGen.NoGetterGen.INSTANCE;
        } else if (style.equals("clone")) {
            getterGen = isNotNull() ? GetterGen.CloneNNGetterGen.PUBLIC : GetterGen.CloneGetterGen.PUBLIC;
        } else if (style.equals("cloneCast")) {
            getterGen = isNotNull() ? GetterGen.CloneCastNNGetterGen.PUBLIC : GetterGen.CloneCastGetterGen.PUBLIC;
        } else if (style.equals("optional")) {
            getterGen = GetterGen.Optional8GetterGen.PUBLIC;
        } else if (style.equals("optionalGuava")) {
            getterGen = GetterGen.OptionalGuavaGetterGen.PUBLIC;
        } else if (style.equals("manual")) {
            getterGen = GetterGen.ManualGetterGen.INSTANCE;
        } else {
            throw new BeanCodeGenException("Unable to locate getter generator '" + style + "'" +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName(), file, lineIndex);
        }
    }

    /**
     * Gets the getter generator.
     * @return the getter generator
     */
    public GetterGen getGetterGen() {
        return getterGen;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the setter generator.
     * @param file  the file
     * @param lineIndex  the line index
     */
    public void resolveSetterGen(File file, int lineIndex) {
        if (getSetStyle() == null) {
            setSetStyle("");
        }
        String style = getSetStyle().replace("\\n", "\n");
        String access = "public";
        if (style.equals("private")) {
            style = "smart";
            access = "private";
        } else if (style.equals("package")) {
            style = "smart";
            access = "package";
        } else if (style.equals("protected")) {
            style = "smart";
            access = "protected";
        }
        if (style.equals("set")) {
            setterGen = SetterGen.SetSetterGen.PUBLIC;
        } else if (style.equals("setClearAddAll")) {
            setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.addAll($value);");
        } else if (style.equals("setClearPutAll")) {
            setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.putAll($value);");
        } else if (style.equals("bound")) {
            if (isFinal()) {
                throw new IllegalArgumentException("Final field must not have a bound setter");
            } else {
                setterGen = SetterGen.ObservableSetterGen.PUBLIC;
                bound = true;
            }
        } else if (style.equals("smart")) {
            if (isDerived()) {
                setterGen = SetterGen.NoSetterGen.INSTANCE;
            } else if (isFinal()) {
                if (isCollectionType()) {
                    setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.addAll($value);", access);
                } else if (isMapType()) {
                    setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.putAll($value);", access);
                } else {
                    setterGen = SetterGen.NoSetterGen.INSTANCE;
                }
            } else {
                setterGen = SetterGen.SetSetterGen.of(access);
            }
        } else if (style.equals("")) {
            setterGen = SetterGen.NoSetterGen.INSTANCE;
        } else if (style.equals("field")) {
            setterGen = SetterGen.FieldSetterGen.INSTANCE;
        } else if (style.equals("manual")) {
            setterGen = SetterGen.NoSetterGen.INSTANCE;
        } else if (style.contains("$field") || style.contains("$value")) {
            if (style.contains("$field") || style.contains("\n")) {
                setterGen = new SetterGen.PatternSetterGen(style);
            } else {
                setterGen = new SetterGen.PatternSetterGen("$field = " + style);
            }
        } else {
            throw new BeanCodeGenException("Unable to locate setter generator '" + style + "'" +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName(), file, lineIndex);
        }
    }

    /**
     * Gets the setter generator.
     * @return the setter generator
     */
    public SetterGen getSetterGen() {
        return setterGen;
    }

    /**
     * Gets the setter scope.
     * @return the setter scope
     */
    public String getSetterScope() {
        if (getSetStyle().equals("private")) {
            return "private";
        }
        return "public";
    }

    /**
     * Gets whether the property is bound.
     * @return true if bound
     */
    public boolean isBound() {
        return bound;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the copy generator.
     * @param file  the file
     * @param lineIndex  the line index
     */
    public void resolveCopyGen(File file, int lineIndex) {
        if (getBean().isMutable() && getBean().isBuilderScopeVisible() == false) {
            return;  // no copying
        }
        if (config.getInvalidImmutableTypes().contains(getFieldTypeRaw())) {
            throw new BeanCodeGenException("Invalid collection type for immutable bean: " + getFieldTypeRaw() +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName(), file, lineIndex);
        }
        if (isDerived()) {
            copyGen = CopyGen.NoCopyGen.INSTANCE;
        } else {
            CopyGen copier = config.getCopyGenerators().get(getFieldTypeRaw());
            if (copier != null) {
                copyGen = copier;
            } else {
                String clone = config.getImmutableGetClones().get(getFieldTypeRaw());
                if (clone != null) {
                    if (clone.equals("clone")) {
                        copyGen = CopyGen.CLONE;
                    } else {
                        copyGen = CopyGen.CLONE_CAST;
                    }
                } else {
                    copyGen = CopyGen.ASSIGN;
                }
            }
        }
    }

    /**
     * Gets the copy generator.
     * @return the copy generator
     */
    public CopyGen getCopyGen() {
        return copyGen;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the copy generator.
     */
    public void resolveBuilderGen() {
        if (getBean().isMutable()) {
            if (!getBean().isBuilderScopeVisible() && !getBean().isBeanStyleLightOrMinimal()) {
                return;  // no builder
            }
        }
        if (isDerived()) {
            builderGen = BuilderGen.NoBuilderGen.INSTANCE;
        } else {
            BuilderGen builder = config.getBuilderGenerators().get(getFieldTypeRaw());
            if (builder != null) {
                builderGen = builder;
            } else {
                builderGen = new BuilderGen.SimpleBuilderGen();
            }
        }
    }

    /**
     * Gets the builder generator.
     * @return the builder generator
     */
    public BuilderGen getBuilderGen() {
        return builderGen;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this property is an array type.
     * 
     * @return true if it is an array type
     */
    public boolean isArrayType() {
        return getType().endsWith("[]");
    }

    /**
     * Checks if this property is an array type.
     * 
     * @return true if it is an array type
     */
    public boolean isGenericArrayType() {
        return getType().endsWith("[]") && getType().length() == 3;
    }

    /**
     * Checks if this property is a known collection type.
     * 
     * @return true if it is a known collection type
     */
    public boolean isCollectionType() {
        return isGeneric() && COLLECTIONS.contains(getTypeRaw());
    }

    /**
     * Checks if this property is a known set type.
     * 
     * @return true if it is a known set type
     */
    public boolean isSetType() {
        return isGeneric() && SETS.contains(getTypeRaw());
    }

    /**
     * Checks if this property is a known sorted set type.
     * 
     * @return true if it is a known set type
     */
    public boolean isSortedSetType() {
        return isGeneric() && SORTED_SETS.contains(getTypeRaw());
    }

    /**
     * Checks if this property is a known map type.
     * 
     * @return true if it is a known map type
     */
    public boolean isMapType() {
        return "FlexiBean".equals(getType()) || (isGeneric() && MAPS.contains(getTypeRaw()));
    }

    /**
     * Gets the read-write flag.
     * 
     * @return the read write
     */
    public PropertyStyle getStyle() {
        if (isDerived()) {
            return PropertyStyle.DERIVED;
        }
        if (getBean().isImmutable()) {
            return PropertyStyle.IMMUTABLE;
        }
        if (getGetStyle().length() > 0 && getSetStyle().length() > 0 && (getSetterGen().isSetterGenerated(this) || getSetStyle().equals("manual"))) {
            return PropertyStyle.READ_WRITE;
        }
        if (getGetStyle().length() > 0) {
            if (bean.isBuilderScopeVisible()) {
                return PropertyStyle.READ_ONLY_BUILDABLE;
            } else {
                return PropertyStyle.READ_ONLY;
            }
        }
        if (getSetStyle().length() > 0) {
            return PropertyStyle.WRITE_ONLY;
        }
        throw new RuntimeException("Property must have a getter or setter: " +
                getBean().getTypeRaw() + "." + getPropertyName());
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the validation is non-null.
     * @return true if non-null
     */
    public boolean isValidated() {
        return getValidation() != null && getValidation().length() > 0;
    }

    /**
     * Checks if the validation is non-null.
     * @return true if non-null
     */
    public boolean isNotNull() {
        return getValidation().equals("notNull") ||
                getValidation().equals("notEmpty") ||
                getValidation().equals("notBlank");
    }

    /**
     * Gets the validation non-null Javadoc.
     * @return the non-null text
     */
    public String getNotNullJavadoc() {
        if (getValidation().equals("notNull")) {
            return ", not null";
        }
        if (getValidation().equals("notEmpty")) {
            return ", not empty";
        }
        if (getValidation().equals("notBlank")) {
            return ", not blank";
        }
        return "";
    }

    /**
     * Gets the validation method name.
     * @return the method name
     */
    public String getValidationMethodName() {
        if (isValidated() == false) {
            throw new IllegalStateException();
        }
        if (getValidation().equals("notNull") ||
                getValidation().equals("notEmpty") ||
                getValidation().equals("notBlank")) {
            return "JodaBeanUtils." + getValidation();
        }
        return getValidation();  // method in bean or static
    }

    /**
     * Gets the varargs code.
     * 
     * @return the varargs code, null if not applicable
     */
    public String getVarArgsCode() {
        return config.getImmutableVarArgs().get(getTypeRaw());
    }

}
