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
package org.joda.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation defining a property.
 * 
 * @author Stephen Colebourne
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PropertyDefinition {

    /**
     * The style of the method used to query the property.
     * <p>
     * The style is a string describing the getter, typically used for code generation.
     * By default this is 'smart' which will use the source code knowledge to determine
     * what to generate. This will be a method of the form {@code isXxx()} for {@code boolean}
     * and {@code getXxx()} for all other types.
     * <p>
     * Supported style stings are:
     * <ul>
     * <li>'' - do not generate any form of getter
     * <li>'smart' - process intelligently - 'is' for boolean and 'get' for other types
     * <li>'is' - generates isXxx()
     * <li>'get' - generates getXxx()
     * <li>'manual' - a method named getXxx() must be manually provided at package scope or greater
     * </ul>
     * 
     * @return the style of the property, not null
     */
    String get() default "smart";

    /**
     * The style of the method used to mutate the property.
     * <p>
     * The style is a string describing the mutator, typically used for code generation.
     * By default this is 'smart' which will use the source code knowledge to determine
     * what to generate. This will be a method of the form {@code setXxx()} for all types unless
     * the field is {@code final}. If the field is a final {@code Collection} or {@code Map}
     * of a known type then a set method is generated using {@code addAll} or {@code puAll}
     * <p>
     * Supported style stings are:
     * <p>
     * The style is a comma separated list determining what methods the property has.
     * By default this is 'getOrIs,set' meaning that a standard getter and setter is generated.
     * You may specify multiple style strings, although some may overlap and generate
     * code that will not compile.
     * <p>
     * Standard style stings are:
     * <ul>
     * <li>'' - do not generate any form of setter
     *  for common list types or 'setClearPutAll' for common map types and FlexiBean
     * <li>'smart' - process intelligently - uses 'set' unless final, when it will use 'setClearAddAll'
     *  for common list types or 'setClearPutAll' for common map types and FlexiBean
     * <li>'set' - generates setXxx()
     * <li>'setClearAddAll' - generates setXxx() using field.clear() and field.addAll(newData)
     * <li>'setClearPutAll' - generates setXxx() using field.clear() and field.putAll(newData)
     * <li>'manual' - a method named setXxx() must be manually provided at package scope or greater
     * </ul>
     * 
     * @return the style of the property, not null
     */
    String set() default "smart";

}
