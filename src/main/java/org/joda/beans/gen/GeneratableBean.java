/*
 *  Copyright 2001-2010 Stephen Colebourne
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
 * A bean that can be generated.
 * 
 * @author Stephen Colebourne
 */
class GeneratableBean {

    /** The simple name of the bean class. */
    private String type;
    /** The simple name of the bean superclass. */
    private String superType;
    /** The list of properties, in the order they are declared. */
    private List<GeneratableProperty> properties = new ArrayList<GeneratableProperty>();

    /**
     * Constructor.
     */
    GeneratableBean() {
    }

    /**
     * Gets the bean type.
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the bean type.
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the bean superclass type.
     * @return the superclass
     */
    public String getSuperType() {
        return superType;
    }

    /**
     * Sets the bean superclass type.
     * @param superType the superclass to set
     */
    public void setSuperType(String superType) {
        this.superType = superType;
    }

    /**
     * Gets the modifiable list of properties.
     * @return the properties, not null
     */
    public List<GeneratableProperty> getProperties() {
        return properties;
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if this bean has a bean superclass.
     * @param true if this is a subclass of another bean
     */
    public boolean isSubclass() {
        return superType.equals("DirectBean") == false;
    }

    /**
     * Checks if the bean is parameterized with generics.
     * @return true if generified
     */
    public boolean isGenericParamType() {
        return type.indexOf("<") >= 0;
    }

    /**
     * Gets the parameterization of the bean.
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
     * Gets the raw type of the bean without generics.
     * @return the raw type, not null
     */
    public String getRawType() {
        int pos = type.indexOf("<");
        return (pos < 0 ? type : type.substring(0, pos));
    }

    /**
     * Gets the raw type of the bean without generics.
     * @return the raw type, not null
     */
    public String getRawSuperType() {
        int pos = superType.indexOf("<");
        return (pos < 0 ? superType : superType.substring(0, pos));
    }

}
