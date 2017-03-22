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
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A bean that can be generated.
 * 
 * @author Stephen Colebourne
 */
class BeanData {

    /** The list of current imports. */
    private final SortedSet<String> currentImports = new TreeSet<>();
    /** The list of new imports. */
    private final SortedSet<String> newImports = new TreeSet<>();
    /** The last import line. */
    private int lastImportLine;
    /** The bean style. */
    private String beanStyle;
    /** The bean meta scope. */
    private String beanMetaScope;
    /** The bean builder scope. */
    private String beanBuilderScope;
    /** The factory method name. */
    private String factoryName;
    /** Whether to cache the hash code. */
    private boolean cacheHashCode;
    /** Whether the class is immutable. */
    private boolean immutable;
    /** Whether the class can be constructed. */
    private boolean constructable;
    /** Whether the class has a manual constructor for immutable beans. */
    private int immutableConstructor;
    /** The method name of the immutable validator. */
    private String immutableValidator;
    /** The method name of the immutable defaults. */
    private String immutableDefaults;
    /** The method name of the immutable pre-build. */
    private String immutablePreBuild;
    /** The style of constructor to generate. */
    private int constructorStyle;
    /** The generated constructor scope. */
    private String constructorScope;
    /** The full type of the bean class. */
    private String typeFull;
    /** The simple name of the bean class. */
    private String typeRaw;
    /** The name clause of the generic. */
    private String[] typeGenericName;
    /** The extends clause of the generic. */
    private String[] typeGenericExtends;
    /** Whether the type is final with no subclasses. */
    private boolean typeFinal;
    /** The scope of the type. */
    private String typeScope;
    /** Whether the type is a root with no bean super-classes. */
    private boolean root;
    /** The full name of the bean superclass. */
    private String superTypeFull;
    /** The simple name of the bean superclass. */
    private String superTypeRaw;
    /** The generic argument of the bean superclass. */
    private String superTypeGeneric;
    /** The list of properties, in the order they are declared. */
    private List<PropertyData> properties = new ArrayList<>();
    /** The serializable flag. */
    private boolean serializable;
    /** The manual serialization version id flag. */
    private boolean manualSerVersionId;
    /** Does the class have a manual clone. */
    private boolean manualClone;
    /** Does the class have a manual equals or hash code. */
    private boolean manualEqualsHashCode;
    /** Does the class have a manual toString. */
    private boolean manualToStringCode;
    /** The style for Object#clone */
    private String cloneStyle;

    /**
     * Constructor.
     */
    BeanData() {
    }

    /**
     * Gets the current set of imports.
     * @return the imports
     */
    public SortedSet<String> getCurrentImports() {
        return currentImports;
    }

    /**
     * Gets the new imports.
     * @return the imports
     */
    public SortedSet<String> getNewImports() {
        return newImports;
    }

    /**
     * Ensures an import is present.
     * @param cls  the class, not null
     */
    public void ensureImport(Class<?> cls) {
        if (currentImports.contains(cls.getName()) == false) {
            newImports.add(cls.getName());
        }
    }

    /**
     * Gets the import insert location.
     * @return the insert location
     */
    public int getImportInsertLocation() {
        return lastImportLine;
    }

    /**
     * Sets the import insert location.
     * @param location  the insert location
     */
    public void setImportInsertLocation(int location) {
        lastImportLine = location;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean style.
     * @return the flag
     */
    public String getBeanStyle() {
        return beanStyle;
    }

    /**
     * Sets the bean style.
     * @param beanStyle  the flag
     */
    public void setBeanStyle(String beanStyle) {
        this.beanStyle = beanStyle;
    }

    /**
     * Resolves the bean style.
     * @param defaultStyle  the default style
     */
    public void resolveBeanStyle(String defaultStyle) {
        if ("smart".equals(beanStyle)) {
            setBeanStyle(defaultStyle);
        }
    }

    /**
     * Is the bean style indicating that properties should be generated.
     * @return the flag
     */
    public boolean isBeanStyleValid() {
        return "full".equals(beanStyle) || "smart".equals(beanStyle) ||
                "minimal".equals(beanStyle) || "light".equals(beanStyle);
    }

    /**
     * Is the bean style indicating that no meta and builder should be generated.
     * @return the flag
     */
    public boolean isBeanStyleLight() {
        return "light".equals(beanStyle);
    }

    /**
     * Is the bean style minimal.
     * @return the flag
     */
    public boolean isBeanStyleMinimal() {
        return "minimal".equals(beanStyle);
    }

    /**
     * Is the bean style indicating that no meta and builder should be generated.
     * @return the flag
     */
    public boolean isBeanStyleLightOrMinimal() {
        return isBeanStyleLight() || isBeanStyleMinimal();
    }

    /**
     * Is the bean style indicating that properties should be generated.
     * @return the flag
     */
    public boolean isBeanStyleGenerateProperties() {
        return "full".equals(beanStyle) || ("smart".equals(beanStyle) && isImmutable() == false);
    }

    /**
     * Is the bean style indicating that properties should be generated.
     * @return the flag
     */
    public boolean isBeanStyleGenerateMetaProperties() {
        return ("full".equals(beanStyle) || "smart".equals(beanStyle)) && !isMetaScopePrivate();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean meta scope.
     * @return the scope
     */
    public String getBeanMetaScope() {
        return beanMetaScope;
    }

    /**
     * Sets the bean meta scope.
     * @param metaScope  the scope
     */
    public void setBeanMetaScope(String metaScope) {
        this.beanMetaScope = metaScope;
    }

    /**
     * Is the meta scope valid.
     * @return the flag
     */
    public boolean isBeanMetaScopeValid() {
        return "smart".equals(beanMetaScope) ||
                "private".equals(beanMetaScope) ||
                "package".equals(beanMetaScope) ||
                "public".equals(beanMetaScope);
    }

    /**
     * Gets the effective scope to use in the meta.
     * @return the scope
     */
    public String getEffectiveMetaScope() {
        String scope = beanMetaScope;
        if ("smart".equals(scope)) {
            scope = typeScope;
        }
        return "package".equals(scope) ? "" : scope + " ";
    }

    /**
     * Checks the meta-bean scope.
     * @return the scope
     */
    public boolean isMetaScopePrivate() {
        return "private".equals(beanMetaScope) || isBeanStyleLight();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the bean builder scope.
     * @return the scope
     */
    public String getBeanBuilderScope() {
        return beanBuilderScope;
    }

    /**
     * Sets the bean builder scope.
     * @param builderScope  the scope
     */
    public void setBeanBuilderScope(String builderScope) {
        this.beanBuilderScope = builderScope;
    }

    /**
     * Is the builder scope valid.
     * @return the flag
     */
    public boolean isBeanBuilderScopeValid() {
        return "smart".equals(beanBuilderScope) ||
                "private".equals(beanBuilderScope) ||
                "package".equals(beanBuilderScope) ||
                "public".equals(beanBuilderScope);
    }

    /**
     * Gets the effective scope to use in the builder.
     * @return the scope
     */
    public String getEffectiveBuilderScope() {
        String scope = beanBuilderScope;
        if ("smart".equals(scope)) {
            scope = typeScope;
        }
        return "package".equals(scope) ? "" : scope + " ";
    }

    /**
     * Is the effective scope to use in the builder public.
     * @return the scope
     */
    public boolean isEffectiveBuilderScopeVisible() {
        return ("smart".equals(beanBuilderScope) || "public".equals(beanBuilderScope) || "package".equals(beanBuilderScope)) &&
                !isBeanStyleLight();
    }

    /**
     * Is the scope to use in the builder public.
     * @return the scope
     */
    public boolean isBuilderScopeVisible() {
        return "public".equals(beanBuilderScope) || "package".equals(beanBuilderScope);
    }

    /**
     * Is the builder generated
     * @return true if generated
     */
    public boolean isSkipBuilderGeneration() {
        return (isMutable() && isBuilderScopeVisible() == false) || isBeanStyleLight();
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the factory name.
     * @return the factory name
     */
    public boolean isFactoryRequired() {
        return factoryName.length() > 0;
    }

    /**
     * Gets the factory name.
     * @return the factory name
     */
    public String getFactoryName() {
        return factoryName;
    }

    /**
     * Sets the factory name.
     * @param name  the factory name
     */
    public void setFactoryName(String name) {
        factoryName = name;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets whether to cache the hash code.
     * @return the flag
     */
    public boolean isCacheHashCode() {
        return cacheHashCode;
    }

    /**
     * Sets whether to cache the hash code.
     * @param cacheHashCode  the flag
     */
    public void setCacheHashCode(boolean cacheHashCode) {
        this.cacheHashCode = cacheHashCode;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets whether property change support is needed.
     * @return the flag
     */
    public boolean isPropertyChangeSupport() {
        for (PropertyData prop : properties) {
            if (prop.isBound()) {
                return true;
            }
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets whether the bean is immutable.
     * @return the flag
     */
    public boolean isImmutable() {
        return immutable;
    }

    /**
     * Gets whether the bean is mutable.
     * @return the flag
     */
    public boolean isMutable() {
        return !immutable;
    }

    /**
     * Sets whether the bean is immutable.
     * @param immutable  the flag
     */
    public void setImmutable(boolean immutable) {
        this.immutable = immutable;
    }

    /**
     * Checks whether the bean can be constructed.
     * @return the flag
     */
    public boolean isConstructable() {
        return constructable;
    }

    /**
     * Sets whether the bean can be constructed.
     * @param constructable  the flag
     */
    public void setConstructable(boolean constructable) {
        this.constructable = constructable;
    }

    /**
     * Gets whether the bean has a manual constructor to use.
     * @return the flag, zero for none, one for builder based, two for argument based
     */
    public int getImmutableConstructor() {
        return immutableConstructor;
    }

    /**
     * Sets whether the bean has a manual constructor to use.
     * @param manualConstructor  the flag
     */
    public void setImmutableConstructor(int manualConstructor) {
        this.immutableConstructor = manualConstructor;
    }

    /**
     * Gets whether the bean has a validator.
     * @return the method name
     */
    public String getImmutableValidator() {
        return immutableValidator;
    }

    /**
     * Sets whether the bean has a validator.
     * @param immutableValidator  the method name
     */
    public void setImmutableValidator(String immutableValidator) {
        this.immutableValidator = immutableValidator;
    }

    /**
     * Gets whether the bean has an apply defaults method.
     * @return the method name
     */
    public String getImmutableDefaults() {
        return immutableDefaults;
    }

    /**
     * Sets whether the bean has an apply defaults method.
     * @param immutableDefaults  the method name
     */
    public void setImmutableDefaults(String immutableDefaults) {
        this.immutableDefaults = immutableDefaults;
    }

    /**
     * Gets whether the bean has a pre-build method.
     * @return the method name
     */
    public String getImmutablePreBuild() {
        return immutablePreBuild;
    }

    /**
     * Sets whether the bean has a pre-build method.
     * @param immutablePreBuild  the method name
     */
    public void setImmutablePreBuild(String immutablePreBuild) {
        this.immutablePreBuild = immutablePreBuild;
    }

    /**
     * Gets the constructor style to generate.
     * @return the flag, zero for none, one for builder based, two for argument based
     */
    public int getConstructorStyle() {
        return constructorStyle;
    }

    /**
     * Sets the constructor style to generate.
     * @param constructorStyle  the constructor style
     */
    public void setConstructorStyle(int constructorStyle) {
        this.constructorStyle = constructorStyle;
    }

    /**
     * Gets the constructor scope to generate.
     * @return the scope
     */
    public String getConstructorScope() {
        return constructorScope;
    }

    /**
     * Sets the constructor scope to generate.
     * @param constructorScope  the constructor scope
     */
    public void setConstructorScope(String constructorScope) {
        this.constructorScope = constructorScope;
    }

    /**
     * Is the constructor scope valid.
     * @return true if valid
     */
    public boolean isConstructorScopeValid() {
        return "smart".equals(constructorScope) ||
                "private".equals(constructorScope) ||
                "package".equals(constructorScope) ||
                "protected".equals(constructorScope) ||
                "public".equals(constructorScope) ||
                "public@ConstructorProperties".equals(constructorScope);
    }

    /**
     * Gets the effective scope to use in the constructor.
     * @return the scope
     */
    public String getEffectiveConstructorScope() {
        if ("smart".equals(constructorScope)) {
            return isTypeFinal() ? "private " : "protected ";
        } else if ("package".equals(constructorScope)) {
            return "";
        } else if ("public@ConstructorProperties".equals(constructorScope)) {
            return "public ";
        }
        return constructorScope + " ";
    }

    /**
     * Checks if the scope indicates the need for the ConstructorProperties annotation.
     * @return true if the annotation is needed
     */
    public boolean isConstructorPropertiesAnnotation() {
        return "public@ConstructorProperties".equals(constructorScope);
    }

    /**
     * Checks whether the bean is serializable.
     * @return the flag
     */
    public boolean isSerializable() {
        return serializable;
    }

    /**
     * Sets whether the bean is serializable.
     * @param serializable  the flag
     */
    public void setSerializable(boolean serializable) {
        this.serializable = serializable;
    }

    /**
     * Checks whether the bean has a manual serialization id.
     * @return the flag
     */
    public boolean isManualSerializationId() {
        return manualSerVersionId;
    }

    /**
     * Sets whether the bean has a manual serialization id.
     * @param manualSerVersionId  the flag
     */
    public void setManualSerializationId(boolean manualSerVersionId) {
        this.manualSerVersionId = manualSerVersionId;
    }

    /**
     * Checks if the clone is manual.
     * @return true if manual
     */
    public boolean isManualClone() {
        return manualClone;
    }

    /**
     * Sets if the clone is manual.
     * @param manualClone  true if manual
     */
    public void setManualClone(boolean manualClone) {
        this.manualClone = manualClone;
    }

    /**
     * Checks if the equals/hashCode is manual.
     * @return true if manual
     */
    public boolean isManualEqualsHashCode() {
        return manualEqualsHashCode;
    }

    /**
     * Sets if the equals/hashCode is manual.
     * @param manualEqualsHashCode  true if manual
     */
    public void setManualEqualsHashCode(boolean manualEqualsHashCode) {
        this.manualEqualsHashCode = manualEqualsHashCode;
    }

    /**
     * Checks if the toString is manual.
     * @return true if manual
     */
    public boolean isManualToStringCode() {
        return manualToStringCode;
    }

    /**
     * Sets if the toString is manual.
     * @param manualToStringCode  true if manual
     */
    public void setManualToStringCode(boolean manualToStringCode) {
        this.manualToStringCode = manualToStringCode;
    }

    /**
     * Gets the clone style.
     * @return the clone style
     */
    public String getCloneStyle() {
        return cloneStyle;
    }

    /**
     * Sets the clone style.
     * @param cloneStyle  the clone style
     */
    public void setCloneStyle(String cloneStyle) {
        this.cloneStyle = cloneStyle;
    }

    /**
     * Is the clone style valid.
     * @return true if valid
     */
    public boolean isCloneStyleValid() {
        return "smart".equals(cloneStyle) ||
                "omit".equals(cloneStyle) ||
                "generate".equals(cloneStyle);
    }

    /**
     * Is the clone method to be skiped.
     * @return true to generate
     */
    public boolean isSkipCloneGeneration() {
        return ("smart".equals(cloneStyle) && isImmutable()) || "omit".equals(cloneStyle);
    }

    /**
     * Sets the bean type.
     * @param parts  the type to set
     */
    public void setTypeParts(String[] parts) {
        this.typeFinal = parts[0] != null;
        this.typeScope = parts[1];
        this.typeFull = parts[2];
        this.typeRaw = parts[3];
        if (parts[8] != null) {
            this.typeGenericName = new String[] {parts[4], parts[6], parts[8]};
            this.typeGenericExtends = new String[3];
            this.typeGenericExtends[0] = parts[5] != null ? parts[5] : "";
            this.typeGenericExtends[1] = parts[7] != null ? parts[7] : "";
            this.typeGenericExtends[2] = parts[9] != null ? parts[9] : "";
        } else if (parts[6] != null) {
            this.typeGenericName = new String[] {parts[4], parts[6]};
            this.typeGenericExtends = new String[2];
            this.typeGenericExtends[0] = parts[5] != null ? parts[5] : "";
            this.typeGenericExtends[1] = parts[7] != null ? parts[7] : "";
        } else if (parts[4] != null) {
            this.typeGenericName = new String[] {parts[4]};
            this.typeGenericExtends = new String[1];
            this.typeGenericExtends[0] = parts[5] != null ? parts[5] : "";
        } else {
            this.typeGenericName = new String[0];
            this.typeGenericExtends = new String[0];
        }
    }

    /**
     * Sets the bean superclass type.
     * @param parts  the superclass to set
     */
    public void setSuperTypeParts(String[] parts) {
        if (parts.length == 1) {
            this.root = true;
            this.immutable = "ImmutableBean".equals(parts[0]);
            this.superTypeFull = "";
            this.superTypeRaw = "";
            this.superTypeGeneric = "";
        } else {
            this.root = "DirectBean".equals(parts[0]);
            this.immutable = false;
            this.superTypeFull = parts[0];
            this.superTypeRaw = parts[1];
            if (parts[4] != null) {
                this.superTypeGeneric = parts[2] + ", " + parts[3] + ", " + parts[4];
            } else if (parts[3] != null) {
                this.superTypeGeneric = parts[2] + ", " + parts[3];
            } else if (parts[2] != null) {
                this.superTypeGeneric = parts[2];
            } else {
                this.superTypeGeneric = "";
            }
        }
    }

    /**
     * Gets the modifiable list of properties.
     * @return the properties, not null
     */
    public List<PropertyData> getProperties() {
        return properties;
    }

    /**
     * Checks if the type is final.
     * @return true if manual
     */
    public boolean isTypeFinal() {
        return typeFinal;
    }

    /**
     * Sets if the type is final.
     * @param typeFinal  true if final, false if subclassable
     */
    public void setTypeFinal(boolean typeFinal) {
        this.typeFinal = typeFinal;
    }

    /**
     * Gets the scope of the type.
     * @return true if manual
     */
    public String getTypeScope() {
        return typeScope;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this bean is a superclass.
     * @return true if this is a subclass of another bean
     */
    public boolean isSubClass() {
        return !root;
    }

    /**
     * Checks if this bean is the root class in a hierarchy.
     * @return true if this is the root class with no bean superclasses
     */
    public boolean isRootClass() {
        return root;
    }

    /**
     * Checks if this bean directly extends {@code DirectBean}.
     * @return true if this extends DirectBean
     */
    public boolean isExtendsDirectBean() {
        return "DirectBean".equals(superTypeFull);
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the bean is parameterised with generics.
     * @return true if generified
     */
    public boolean isTypeGeneric() {
        return typeGenericName.length > 0;
    }

    /**
     * Gets the number of generic type parameters.
     * @return zero if no type parameters, one or two if it has type parameters
     */
    public int getTypeGenericCount() {
        return typeGenericName.length;
    }

    /**
     * Gets the bean type, such as '{@code Foo<T extends Bar>}'.
     * @return the type
     */
    public String getType() {
        return typeFull;
    }

    /**
     * Gets the parameterisation of the bean including extends clause, such as '{@code <T extends Bar>}'.
     * @param includeBrackets  whether to include the brackets
     * @return the generic type, or a blank string if not generic, not null
     */
    public String getTypeGeneric(boolean includeBrackets) {
        if (isTypeGeneric() == false) {
            return "";
        }
        String result = typeGenericName[0] + typeGenericExtends[0];
        if (typeGenericExtends.length > 1) {
            result += ", " + typeGenericName[1] + typeGenericExtends[1];
            if (typeGenericExtends.length > 2) {
                result += ", " + typeGenericName[2] + typeGenericExtends[2];
            }
        }
        return includeBrackets && result.length() > 0 ? '<' + result + '>' : result;
    }

    /**
     * Gets the name of the parameterisation of the bean, such as '{@code <T, U>}'.
     * @param includeBrackets  whether to include the brackets
     * @return the generic type name, or a blank string if not generic, not null
     */
    public String getTypeGenericName(boolean includeBrackets) {
        if (isTypeGeneric() == false) {
            return "";
        }
        String result = typeGenericName[0];
        if (typeGenericExtends.length > 1) {
            result += ", " + typeGenericName[1];
            if (typeGenericExtends.length > 2) {
                result += ", " + typeGenericName[2];
            }
        }
        return includeBrackets && result.length() > 0 ? '<' + result + '>' : result;
    }

    /**
     * Gets the diamond operator if generic.
     * @return the generic type name, or a blank string if not generic, not null
     */
    public String getTypeGenericDiamond() {
        return isTypeGeneric() ? "<>" : "";
    }

    /**
     * Gets the type with the diamond operator if generic.
     * @return the type name, with generic diamond if necessary, not null
     */
    public String getTypeWithDiamond() {
        return isTypeGeneric() ? getTypeRaw() + "<>" : getTypeRaw();
    }

    /**
     * Gets the name of the parameterisation of the bean, such as '{@code <T>}'.
     * @param typeParamIndex  the zero-based index of the type parameter
     * @param includeBrackets  whether to include brackets
     * @return the generic type name, not null
     */
    public String getTypeGenericName(int typeParamIndex, boolean includeBrackets) {
        String result = typeGenericName[typeParamIndex];
        return includeBrackets && result.length() > 0 ? '<' + result + '>' : result;
    }

    /**
     * Gets the extends clause of the parameterisation of the bean, such as '{@code  extends Foo}'.
     * @param typeParamIndex  the zero-based index of the type parameter
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeGenericErased(int typeParamIndex) {
        String extend = typeGenericExtends[typeParamIndex];
        return extend.startsWith(" extends ") ? extend.substring(9) : "Object";
    }

    /**
     * Gets the extends clause of the parameterisation of the bean, such as '{@code  extends Foo}'.
     * @param typeParamIndex  the zero-based index of the type parameter
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeGenericExtends(int typeParamIndex) {
        return typeGenericExtends[typeParamIndex];
    }

    /**
     * Gets the extends clause of the parameterisation of the bean, such as '{@code  extends Foo}'.
     * @param typeParamIndex  the zero-based index of the type parameter
     * @param typeParamNames  the type parameter names
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeGenericExtends(int typeParamIndex, String[] typeParamNames) {
        String genericClause = typeGenericExtends[typeParamIndex];
        genericClause = genericClause.replace("<" + typeGenericName[typeParamIndex] + ">", "<" + typeParamNames[typeParamIndex] + ">");
        for (int i = 0; i < typeGenericName.length; i++) {
            genericClause = genericClause.replace("<" + typeGenericName[i] + ">", "<" + typeParamNames[i] + ">");
            genericClause = genericClause.replace(" extends " + typeGenericName[i] + ">", " extends " + typeParamNames[i] + ">");
            genericClause = genericClause.replace(" super " + typeGenericName[i] + ">", " super " + typeParamNames[i] + ">");
        }
        return genericClause;
    }

    /**
     * Gets the full type of the bean with simple parameterization, such as '{@code Foo<T>}'.
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeNoExtends() {
        return typeRaw + getTypeGenericName(true);
    }

    /**
     * Gets the raw type of the bean without generics, such as '{@code Foo}'.
     * @return the raw type, not null
     */
    public String getTypeRaw() {
        return typeRaw;
    }

    /**
     * Gets the full type of the bean with wildcarded parameterization, such as '{@code Foo<?>}'.
     * @return the wildcarded type, not null
     */
    public String getTypeWildcard() {
        if (isTypeGeneric() == false) {
            return typeRaw;
        }
        String result = "?";
        if (typeGenericExtends.length > 1) {
            result += ", ?";
            if (typeGenericExtends.length > 2) {
                result += ", ?";
            }
        }
        return typeRaw + '<' + result + '>';
    }

    /**
     * Checks if the type specified is one of the bean's type parameters.
     * @param type  the type
     * @return true if a type parameter of this bean
     */
    public boolean isTypeGenerifiedBy(String type) {
        if (typeGenericName.length > 2 && typeGenericName[2].equals(type)) {
            return true;
        }
        if (typeGenericName.length > 1 && typeGenericName[1].equals(type)) {
            return true;
        }
        if (typeGenericName.length > 0 && typeGenericName[0].equals(type)) {
            return true;
        }
        return false;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the super bean is parameterised with generics.
     * @return true if generified
     */
    public boolean isSuperTypeGeneric() {
        return superTypeGeneric.length() > 0;
    }

    /**
     * Gets the bean superclass type.
     * @return the superclass
     */
    public String getSuperType() {
        return superTypeFull;
    }

    /**
     * Gets the parameterisation of the super bean.
     * @param includeBrackets  whether to include the brackets
     * @return the generic type, or a blank string if not generic, not null
     */
    public String getSuperTypeGeneric(boolean includeBrackets) {
        return includeBrackets && superTypeGeneric.length() > 0 ? '<' + superTypeGeneric + '>' : superTypeGeneric;
    }

    /**
     * Gets the raw type of the super bean without generics.
     * @return the raw type, not null
     */
    public String getSuperTypeRaw() {
        return superTypeRaw;
    }

    /**
     * Checks if any property is validated.
     * @return true if validated
     */
    public boolean isValidated() {
        for (PropertyData property : properties) {
            if (property.isValidated()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the scope of nested Meta and Builder classes.
     * @return the scope, not null
     */
    public String getNestedClassConstructorScope() {
        return (isTypeFinal() ? "private" : "protected");
    }

}
