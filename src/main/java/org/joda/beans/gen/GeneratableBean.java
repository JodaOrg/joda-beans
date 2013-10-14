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
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A bean that can be generated.
 * 
 * @author Stephen Colebourne
 */
class GeneratableBean {

    /** The list of current imports. */
    private final SortedSet<String> currentImports = new TreeSet<String>();
    /** The list of new imports. */
    private final SortedSet<String> newImports = new TreeSet<String>();
    /** The last import line. */
    private int lastImportLine;
    /** The bean style. */
    private String beanStyle;
    /** Whether the class is immutable. */
    private boolean immutable;
    /** Whether the class can be constructed. */
    private boolean constructable;
    /** Whether the class has a manual construcor for immutable beans. */
    private boolean immutableConstructor;
    /** The full type of the bean class. */
    private String typeFull;
    /** The simple name of the bean class. */
    private String typeRaw;
    /** The name clause of the generic. */
    private String typeGenericName;
    /** The extends clause of the generic. */
    private String typeGenericExtends;
    /** Whether the type is final with no subclasses. */
    private boolean typeFinal;
    /** Whether the type is a root with no bean super-classes. */
    private boolean root;
    /** The full name of the bean superclass. */
    private String superTypeFull;
    /** The simple name of the bean superclass. */
    private String superTypeRaw;
    /** The generic argument of the bean superclass. */
    private String superTypeGeneric;
    /** The list of properties, in the order they are declared. */
    private List<GeneratableProperty> properties = new ArrayList<GeneratableProperty>();
    /** Does the class have a manual clone. */
    private boolean manualClone;
    /** Does the class have a manual equals or hash code. */
    private boolean manualEqualsHashCode;
    /** Does the class have a manual toString. */
    private boolean manualToStringCode;

    /**
     * Constructor.
     */
    GeneratableBean() {
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
     * Is the bean style indicating that properties should be generated.
     * @return the flag
     */
    public boolean isBeanStyleValid() {
        return "full".equals(beanStyle) || "smart".equals(beanStyle) || "minimal".equals(beanStyle);
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
        return "full".equals(beanStyle) || "smart".equals(beanStyle);
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
     * Checks whether the bean has a manual constructor to use.
     * @return the flag
     */
    public boolean isImmutableConstructor() {
        return immutableConstructor;
    }

    /**
     * Sets whether the bean has a manual constructor to use.
     * @param constructable  the flag
     */
    public void setImmutableConstructor(boolean manualConstructor) {
        this.immutableConstructor = manualConstructor;
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
     * Sets the bean type.
     * @param parts  the type to set
     */
    public void setTypeParts(String[] parts) {
        this.typeFinal = parts[0] != null;
        this.typeFull = parts[1];
        this.typeRaw = parts[2];
        this.typeGenericName = parts[3] != null ? parts[3] : "";
        this.typeGenericExtends = parts[4] != null ? parts[4] : "";
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
            this.superTypeGeneric = parts[2] != null ? parts[2] : "";
        }
    }

    /**
     * Gets the modifiable list of properties.
     * @return the properties, not null
     */
    public List<GeneratableProperty> getProperties() {
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

    //-----------------------------------------------------------------------
    /**
     * Checks if this bean is a superclass.
     * @param true if this is a subclass of another bean
     */
    public boolean isSubClass() {
        return !root;
    }

    /**
     * Checks if this bean is the root class in a hierarchy.
     * @param true if this is the root class with no bean superclasses
     */
    public boolean isRootClass() {
        return root;
    }

    /**
     * Checks if this bean directly extends {@code DirectBean}.
     * @param true if this extends DirectBean
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
        return typeGenericName.length() > 0;
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
        String result = typeGenericName + typeGenericExtends;
        return includeBrackets && result.length() > 0 ? '<' + result + '>' : result;
    }

    /**
     * Gets the name of the parameterisation of the bean, such as '{@code <T>}'.
     * @param includeBrackets  whether to include the brackets
     * @return the generic type name, or a blank string if not generic, not null
     */
    public String getTypeGenericName(boolean includeBrackets) {
        return includeBrackets && typeGenericName.length() > 0 ? '<' + typeGenericName + '>' : typeGenericName;
    }

    /**
     * Gets the extends clause of the parameterisation of the bean, such as '{@code  extends Foo}'.
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeGenericExtends() {
        return typeGenericExtends;
    }

    /**
     * Gets the full type of the bean with simple parameterization, such as '{@code Foo<T>}'.
     * @return the generic type extends clause, or a blank string if not generic or no extends, not null
     */
    public String getTypeNoExtends() {
        return typeFull.replace(typeGenericExtends, "");
    }

    /**
     * Gets the raw type of the bean without generics, such as '{@code Foo}'.
     * @return the raw type, not null
     */
    public String getTypeRaw() {
        return typeRaw;
    }

    /**
     * Gets the wildcarded type.
     * @return the wildcarded type, not null
     */
    public String getTypeWildcard() {
        return typeRaw + (isTypeGeneric() ? "<?>" : "");
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
        for (GeneratableProperty property : properties) {
            if (property.isValidated()) {
                return true;
            }
        }
        return false;
    }

}
