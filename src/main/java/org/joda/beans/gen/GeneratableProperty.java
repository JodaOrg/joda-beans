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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.beans.PropertyReadWrite;

/**
 * A bean that can be generated.
 * 
 * @author Stephen Colebourne
 */
class GeneratableProperty {

    /** Collection types. */
    private static final Set<String> COLLECTIONS = new HashSet<String>(
            Arrays.asList(
                    "Collection", "Set", "SortedSet", "NavigableSet", "List",
                    "ArrayList", "LinkedList",
                    "HashSet", "LinkedHashSet", "TreeSet", "ConcurrentSkipListSet",
                    "ImmutableCollection", "ImmutableList", "ImmutableSet", "ImmutableSortedSet"));
    /** Map types. */
    private static final Set<String> MAPS = new HashSet<String>(
            Arrays.asList(
                    "Map", "SortedMap", "NavigableMap", "ConcurrentMap", "ConcurrentNavigableMap",
                    "HashMap", "LinkedHashMap", "TreeMap", "ConcurrentHashMap", "ConcurrentSkipListMap",
                    "BiMap", "HashBiMap",
                    "ImmutableMap", "ImmutableSortedMap", "ImmutableBiMap"));

    /** Owning bean. */
    private final GeneratableBean bean;
    /** Property name. */
    private String propertyName;
    /** Field name. */
    private String fieldName;
    /** Upper property name. */
    private String upperName;
    /** Property type. */
    private String type;
    /** Property field type. */
    private String fieldType;
    /** Whether the field is declared final. */
    private boolean isFinal;
    /** The getter style. */
    private String getStyle;
    /** The setter style. */
    private String setStyle;
    /** The type style. */
    private String typeStyle;
    /** The validation string. */
    private String validation;
    /** Deprecated flag. */
    private boolean deprecated;
    /** First comment about the property. */
    private String firstComment;
    /** Other comments about the property. */
    private final List<String> comments = new ArrayList<String>();
    /** The getter generator. */
    private GetterGen getterGen;
    /** The setter generator. */
    private SetterGen setterGen;
    /** The copy generator. */
    private CopyGen copyGen;
    /** The builder generator. */
    private BuilderGen builderGen;
    /** The config. */
    private BeanGenConfig config;

    /**
     * Constructor.
     */
    GeneratableProperty(GeneratableBean bean, BeanGenConfig config) {
        this.bean = bean;
        this.config = config;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean.
     * @return the bean, not null
     */
    public GeneratableBean getBean() {
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
        String rawType = fieldType;
        String generics = "";
        if (rawType.contains("<")) {
            rawType = rawType.substring(0, fieldType.indexOf('<'));
            generics = fieldType.substring(fieldType.indexOf('<'));
        }
        if (getTypeStyle().equals("smart")) {
            setType(fieldType);
        } else if (getTypeStyle().length() > 0) {
            if (getTypeStyle().contains("<>")) {
                setType(getTypeStyle().replace("<>", generics));
            } else {
                setType(getTypeStyle() + generics);
            }
        } else {
            setType(fieldType);
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
     * @param type  the field is final flag
     */
    public void setFinal(boolean isFinal) {
        this.isFinal = isFinal;
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
     * @return true if generified
     */
    public boolean isGenericWildcardParamType() {
        return type.endsWith("<?>");
    }

    /**
     * Gets the parameterisation of the property.
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
     * Gets the raw type of the property without generics.
     * @return the raw type, not null
     */
    public String getRawType() {
        int pos = type.indexOf("<");
        return (pos < 0 ? type : type.substring(0, pos));
    }

    /**
     * Checks if the type is the generic type of the bean.
     * For example, if the property is of type T in a bean of Foo[T].
     * @return true if matches
     */
    public boolean isBeanGenericType() {
        return type.equals(bean.getTypeGenericName(false));
    }

    /**
     * Checks if the property is generic in some way.
     * @return true if generic
     */
    public boolean isGeneric() {
        return isGenericParamType() || isBeanGenericType();
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
     */
    public void resolveGetterGen() {
        if (getGetStyle() == null) {
            setGetStyle("");
        }
        final String style = getGetStyle();
        if (style.equals("get")) {
            getterGen = GetterGen.GetGetterGen.INSTANCE;
        } else if (style.equals("is")) {
            getterGen = GetterGen.IsGetterGen.INSTANCE;
        } else if (style.equals("smart")) {
            String clone = config.getImmutableGetClones().get(getFieldTypeRaw());
            if ("clone".equals(clone)) {
                getterGen = GetterGen.CloneGetterGen.INSTANCE;
            } else if ("cloneCast".equals(clone)) {
                getterGen = GetterGen.CloneCastGetterGen.INSTANCE;
            } else if (getType().equals("boolean")) {
                getterGen = GetterGen.IsGetterGen.INSTANCE;
            } else {
                getterGen = GetterGen.GetGetterGen.INSTANCE;
            }
        } else if (style.equals("")) {
            getterGen = GetterGen.NoGetterGen.INSTANCE;
        } else if (style.equals("clone")) {
            getterGen = GetterGen.CloneGetterGen.INSTANCE;
        } else if (style.equals("cloneCast")) {
            getterGen = GetterGen.CloneCastGetterGen.INSTANCE;
        } else if (style.equals("manual")) {
            getterGen = GetterGen.ManualGetterGen.INSTANCE;
        } else {
            throw new RuntimeException("Unable to locate setter generator '" + style + "'" +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName());
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
     */
    public void resolveSetterGen() {
        if (getSetStyle() == null) {
            setSetStyle("");
        }
        final String style = getSetStyle().replace("\\n", "\n");
        if (style.equals("set")) {
            setterGen = SetterGen.SetSetterGen.INSTANCE;
        } else if (style.equals("setClearAddAll")) {
            setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.addAll($value);");
        } else if (style.equals("setClearPutAll")) {
            setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.putAll($value);");
        } else if (style.equals("smart")) {
            if (isDerived()) {
                setterGen = SetterGen.NoSetterGen.INSTANCE;
            } else if (isFinal()) {
                if (isCollectionType()) {
                    setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.addAll($value);");
                } else if (isMapType()) {
                    setterGen = new SetterGen.PatternSetterGen("$field.clear();\n$field.putAll($value);");
                } else {
                    setterGen = SetterGen.NoSetterGen.INSTANCE;
                }
            } else {
                setterGen = SetterGen.SetSetterGen.INSTANCE;
            }
        } else if (style.equals("")) {
            setterGen = SetterGen.NoSetterGen.INSTANCE;
        } else if (style.equals("manual")) {
            setterGen = SetterGen.NoSetterGen.INSTANCE;
        } else if (style.contains("$field") || style.contains("$value")) {
            if (style.contains("$field") || style.contains("\n")) {
                setterGen = new SetterGen.PatternSetterGen(style);
            } else {
                setterGen = new SetterGen.PatternSetterGen("$field = " + style);
            }
        } else {
            throw new RuntimeException("Unable to locate setter generator '" + style + "'" +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName());
        }
    }

    /**
     * Gets the setter generator.
     * @return the setter generator
     */
    public SetterGen getSetterGen() {
        return setterGen;
    }

    //-----------------------------------------------------------------------
    /**
     * Resolves the copy generator.
     */
    public void resolveCopyGen() {
        if (getBean().isMutable()) {
            return;  // no copying
        }
        if (config.getInvalidImmutableTypes().contains(getFieldTypeRaw())) {
            throw new RuntimeException("Invalid collection type for immutable bean: " + getFieldTypeRaw() +
                    " in " + getBean().getTypeRaw() + "." + getPropertyName());
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
            return;  // no builder
        }
        if (isDerived()) {
            builderGen = BuilderGen.NoBuilderGen.INSTANCE;
        } else {
            BuilderGen builder = config.getBuilderGenerators().get(getFieldTypeRaw());
            if (builder != null) {
                builderGen = builder;
            } else {
                builderGen = new BuilderGen.SimpleBuilderGen(getFieldType());
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
     * Checks if this property is a known collection type.
     * 
     * @return true if it is a known collection type
     */
    public boolean isCollectionType() {
        return isGeneric() && COLLECTIONS.contains(getRawType());
    }

    /**
     * Checks if this property is a known map type.
     * 
     * @return true if it is a known map type
     */
    public boolean isMapType() {
        return "FlexiBean".equals(getType()) || (isGeneric() && MAPS.contains(getRawType()));
    }

    /**
     * Gets the read-write flag.
     * 
     * @return the read write
     */
    public PropertyReadWrite getReadWrite() {
        if (getBean().isImmutable()) {
            return PropertyReadWrite.READ_ONLY;
        }
        if (getGetStyle().length() > 0 && getSetStyle().length() > 0 && (getSetterGen().isSetterGenerated(this) || getSetStyle().equals("manual"))) {
            return PropertyReadWrite.READ_WRITE;
        }
        if (getGetStyle().length() > 0) {
            return PropertyReadWrite.READ_ONLY;
        }
        if (getSetStyle().length() > 0) {
            return PropertyReadWrite.WRITE_ONLY;
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
        return getValidation().equals("notNull") || getValidation().equals("notEmpty");
    }

    /**
     * Gets the validation method name.
     * @return the method name
     */
    public String getValidationMethodName() {
        if (isValidated() == false) {
            throw new IllegalStateException();
        }
        if (getValidation().equals("notNull") || getValidation().equals("notEmpty")) {
            return "JodaBeanUtils." + getValidation();
        }
        return getValidation();  // method in bean or static
    }

    /**
     * Gets the raw type of the property.
     * @return the raw type
     */
    public String getTypeRaw() {
        final String type = getType();
        if (type.contains("<")) {
            return type.substring(0, type.indexOf('<'));
        }
        return type;
    }

    /**
     * Gets the raw type of the property.
     * @return the raw type
     */
    public String getFieldTypeRaw() {
        final String type = getFieldType();
        if (type.contains("<")) {
            return type.substring(0, type.indexOf('<'));
        }
        return type;
    }

    /**
     * Gets the generic part of the property type.
     * <p>
     * For example, "Foo&lt;String&gt;" will return "&lt;String&gt;".
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

}
