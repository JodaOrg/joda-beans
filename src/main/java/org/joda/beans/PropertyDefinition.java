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
package org.joda.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation defining a property for code generation.
 * <p>
 * This annotation must be used on all private instance variables that
 * should be treated as properties.
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
     * Standard style stings are:
     * <ul>
     * <li>'' - do not generate any form of setter
     * <li>'smart' - process intelligently - uses 'set' unless final, when it will use 'setClearAddAll'
     *  for common list types or 'setClearPutAll' for common map types and FlexiBean
     * <li>'set' - generates setXxx()
     * <li>'setClearAddAll' - generates setXxx() using field.clear() and field.addAll(newData)
     * <li>'setClearPutAll' - generates setXxx() using field.clear() and field.putAll(newData)
     * <li>'manual' - a method named setXxx() must be manually provided at package scope or greater
     * <li>a pattern, see below
     * </ul>
     * <p>
     * A pattern can be used for special behaviour.
     * The pattern is a complete piece of code.
     * For example, 'new Foo($value)' or '$field = $value.clone()'.
     * '$field' for the field to copy into.
     * '$value' for the value to copy from.
     * '\n' for a new line (all lines must then include semi-colons).
     */
    String set() default "smart";

    /**
     * The code used to create a copy of the property.
     * <p>
     * The style is used to control the code that creates a copy of the property value.
     * This is used with immutable beans.
     * <p>
     * Standard style stings are:
     * <ul>
     * <li>'' - do not generate any form of copy
     * <li>'smart' - process intelligently, see below
     * <li>'bean' - uses JodaBeanUtils.clone()
     * <li>'clone' - uses the clone() method on the target object which must return the correct type
     * <li>a pattern, see below
     * </ul>
     * <p>
     * The default 'smart' value will handle Guava immutable classes and the main
     * JDK collection/map types, plus {@code FlexiBean}.
     * Any class not recognised will simply be assigned.
     * It is strongly recommended to use Guava collections where possible.
     * <p>
     * A pattern can be used for special behaviour.
     * The pattern is a complete piece of code.
     * For example, 'new Foo($value)'.
     * '$field' for the field to copy into.
     * '$value' for the value to copy from.
     * '$type' for the type including generics.
     * '$typeRaw' for the type excluding generics.
     * '$generics' for the generics of the type including angle brackets.
     * '\n' for a new line (all lines must then include semi-colons).
     */
    String copy() default "smart";

    /**
     * The exposed type of the property.
     * <p>
     * The style is used to control the exposed type of the property in
     * getters and setters, or similar.
     * <p>
     * This is used when the type of the field is not the same as the type
     * as should be used in public methods such as getters and setters.
     * <p>
     * By default, recognised collection types from the JDK and Guava will automatically
     * be simplified to the matching interface. Thus, a field of type
     * {@code ImmutableList} or {@code ArrayList} will be exposed as {@code List}.
     * To avoid that behaviour, set the exposed type to a blank string
     */
    String type() default "smart";

    /**
     * The validator to use.
     * <p>
     * The property value may be validated by specifying this attribute.
     * By default no validation is performed.
     * The code generator places the validation into the set method and ensures that
     * new objects are validated correctly.
     * <p>
     * Custom validations, are written by writing a static method and referring to it.
     * For example, {@code public void checkMyValue(Integer val, String propertyName) ...}
     * The method generally has a {@code void} return, throwing an exception if validation fails.
     * There must be two arguments, the value and the property name. The value may be the
     * property type or a superclass (like Object). The property name should be a String.
     * <p>
     * Standard validation stings are:
     * <ul>
     * <li>'' - do not generate any form of validation
     * <li>'notNull' - suitable for checking that the value is non-null,
     *  calls JodaBeanUtils.notNull() which throws an IllegalArgumentException
     * <li>'notEmpty' - suitable for checking that a string is non-null and non-empty,
     *  calls JodaBeanUtils.notEmpty() which throws an IllegalArgumentException
     * <li>'{className}.{staticMethodName}' - a custom validation method, described above
     * </ul>
     */
    String validate() default "";

}
