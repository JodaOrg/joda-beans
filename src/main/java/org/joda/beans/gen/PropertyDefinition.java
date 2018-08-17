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
     * An alternative name for the property.
     * <p>
     * The property can be looked up using the specified alias.
     * The primary name is unaffected, and the alias is only used in certain circumstances.
     * For example, {@code bean.metaProperty("alias")} and {@code bean.property("alias")}
     * will both work, as will getting and setting via an immutable bean builder.
     * <p>
     * This attribute is most useful in handling change from serialized forms.
     * 
     * @return the alias of the property, defaulted to ''
     */
    String alias() default "";

    /**
     * The style of the method used to query the property.
     * <p>
     * The style is a string describing the getter, typically used for code generation.
     * By default this is 'smart' which will use the source code knowledge to determine
     * what to generate. This will be a method of the form {@code isXxx()} for {@code boolean}
     * and {@code getXxx()} for all other types.
     * <p>
     * Supported style strings are:
     * <ul>
     * <li>'' - do not generate any form of getter
     * <li>'smart' - process intelligently - 'is' for boolean and 'get' for other types
     * <li>'private' - process as per 'smart' but set scope as private
     * <li>'package' - process as per 'smart' but set scope as package/default
     * <li>'protected' - process as per 'smart' but set scope as protected
     * <li>'is' - generates isXxx()
     * <li>'get' - generates getXxx()
     * <li>'clone' - generates getXxx() with a clone of the field (assumed to be of the correct type)
     * <li>'cloneCast' - generates getXxx() with a clone of the field with a cast to the property type
     * <li>'optional' - generate getXxx() returning a Java 8 {@code Optional} wrapper around the field,
     *  where the field itself is nullable instead of optional. {@code OptionalDouble}, {@code OptionalInt}
     *  and {@code OptionalLong} are also handled
     * <li>'optionalGuava' - generate getXxx() returning a Guava {@code Optional} wrapper around the field,
     *  where the field itself is nullable instead of optional
     * <li>'field' - generates direct access to the field, enabling a weird manual getter
     * <li>'manual' - a method named getXxx() must be manually provided at package scope or greater
     * </ul>
     * 
     * @return the getter style, defaulted to 'smart'
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
     * Standard style strings are:
     * <ul>
     * <li>'' - do not generate any form of setter
     * <li>'smart' - process intelligently - uses 'set' unless final, when it will use 'setClearAddAll'
     *  for common list types or 'setClearPutAll' for common map types and FlexiBean
     * <li>'private' - process as per 'smart' but set scope as private
     * <li>'package' - process as per 'smart' but set scope as package/default
     * <li>'protected' - process as per 'smart' but set scope as protected
     * <li>'set' - generates setXxx()
     * <li>'setClearAddAll' - generates setXxx() using field.clear() and field.addAll(newData)
     * <li>'setClearPutAll' - generates setXxx() using field.clear() and field.putAll(newData)
     * <li>'bound' - generates a bound property with {@code PropertyChangeSupport}
     * <li>'field' - generates direct access to the field, enabling a weird manual setter
     * <li>'manual' - a method named setXxx() must be manually provided at package scope or greater
     * <li>a pattern, see below
     * </ul>
     * <p>
     * A pattern can be used for special behaviour.
     * The pattern is a complete piece of code.
     * For example, 'new Foo($value)' or '$field = $value.clone()'.<br/>
     * '$field' for the field to copy into.<br/>
     * '$value' for the value to copy from.<br/>
     * '&lt;&gt;' for the generics of the type including angle brackets.<br/>
     * '\n' for a new line (all lines must then include semi-colons).<br/>
     * 
     * @return the setter style, defaulted to 'smart'
     */
    String set() default "smart";

    /**
     * Whether the generated getter should be declared with the {@code Override} annotation.
     * <p>
     * By default, the annotation is not added.
     * 
     * @return true to override the generated get method
     */
    boolean overrideGet() default false;

    /**
     * Whether the generated setter should be declared with the {@code Override} annotation.
     * <p>
     * By default, the annotation is not added.
     * 
     * @return true to override the generated set method
     */
    boolean overrideSet() default false;

    /**
     * The exposed type of the property.
     * <p>
     * The style is used to control the exposed type of the property in
     * getters and setters, or similar.
     * <p>
     * This is used when the type of the field is not the same as the type
     * that should be used in public methods such as getters and setters.
     * <p>
     * By default, the declared type will be used as the exposed type.
     * 
     * @return the exposed type, defaulted to 'smart'
     */
    String type() default "smart";

    /**
     * The exposed type of the property in the builder and associated constructor.
     * <p>
     * The style is used to control the exposed type of the property in
     * immutable builders and associated constructors, or similar.
     * <p>
     * This is used when the type of the field is not the same as the type
     * that should be used in public methods such as builder setters.
     * <p>
     * By default, the declared type will be used as the exposed type.
     * <p>
     * This is typically used to add '? extends' to collection types.
     * 
     * @return the builder type, defaulted to 'smart'
     */
    String builderType() default "smart";

    /**
     * The configuration for equals and hash code.
     * <p>
     * This flag controls generation of the {@code equals} and {@code hashCode} methods.
     * The default is 'smart'.
     * <p>
     * Standard strings are:
     * <ul>
     * <li>'omit' - omit this property from equals and hashCode
     * <li>'smart' - process intelligently, equivalent to 'field' for immutable and 'getter' for mutable
     * <li>'getter' - include in equals and hashCode using the getter
     * <li>'field' - include in equals and hashCode using the field
     * </ul>
     * 
     * @return the equals/hashCode style, defaulted to 'smart'
     */
    String equalsHashCodeStyle() default "smart";

    /**
     * The configuration for toString.
     * <p>
     * This flag controls generation of the {@code toString} method.
     * The default is 'smart'.
     * <p>
     * Standard strings are:
     * <ul>
     * <li>'omit' - omit this property from toString
     * <li>'smart' - process intelligently, equivalent to 'field' for immutable and 'getter' for mutable
     * <li>'getter' - include in toString using the getter
     * <li>'field' - include in toString using the field
     * </ul>
     * 
     * @return the toString style, defaulted to 'smart'
     */
    String toStringStyle() default "smart";

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
     * Standard validation strings are:
     * <ul>
     * <li>'' - do not generate any form of validation
     * <li>'notNull' - suitable for checking that the value is non-null,
     *  calls JodaBeanUtils.notNull() which throws an IllegalArgumentException
     * <li>'notEmpty' - suitable for checking that a string is non-null and non-empty,
     *  calls JodaBeanUtils.notEmpty() which throws an IllegalArgumentException
     * <li>'notBlank' - suitable for checking that a string is non-null and non-blank,
     *  calls JodaBeanUtils.notBlank() which throws an IllegalArgumentException
     * <li>'{className}.{staticMethodName}' - a custom validation method, described above
     * </ul>
     * 
     * @return the validation, defaulted to ''
     */
    String validate() default "";

}
