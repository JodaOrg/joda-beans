/*
 *  Copyright 2001-2016 Stephen Colebourne
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
 * Annotation defining a bean for code generation.
 * <p>
 * This annotation must be used on classes that should be treated as beans.
 * 
 * @author Stephen Colebourne
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BeanDefinition {

    /**
     * The style of bean generation.
     * <p>
     * By default, this follows 'smart' rules.
     * Set to 'minimal' to generate a minimal amount of code.
     * Set to 'full' to generate the full code.
     * Set to 'light' to generate a light immutable bean using reflection internally.
     */
    String style() default "smart";

    /**
     * The scope of the generated constructor.
     * <p>
     * Only applicable to immutable beans. By default, this follows 'smart'
     * rules, which generate a private constructor when needed by the builder.
     * Set to 'private' to generate a private constructor.
     * Set to 'package' to generate a package-scoped constructor.
     * Set to 'protected' to generate a protected constructor.
     * Set to 'public' to generate a public constructor.
     * Set to 'public@ConstructorProperties' to generate a public constructor.
     */
    String constructorScope() default "smart";

    /**
     * The scope of the meta-bean class.
     * <p>
     * By default, this follows 'smart' rules, which generate a public meta-bean.
     * Set to 'private' to generate a private meta-bean.
     * Set to 'package' to generate a package-scoped meta-bean.
     * Set to 'public' to generate a public meta-bean.
     */
    String metaScope() default "smart";

    /**
     * The scope of the builder class.
     * <p>
     * By default, this follows 'smart' rules, which generate a public builder for
     * immutable beans and no builder for mutable beans.
     * Set to 'private' to generate a private builder.
     * Set to 'package' to generate a package-scoped builder.
     * Set to 'public' to generate a public builder.
     */
    String builderScope() default "smart";

    /**
     * The name of the factory method.
     * <p>
     * By default, this is an empty string and no factory is generated.
     * Set to 'of' to generate a factory method named 'of.
     */
    String factoryName() default "";

    /**
     * Information about the bean hierarchy.
     * <p>
     * This is needed to add information that cannot be derived.
     * Set to 'immutable' for a subclass of an immutable bean.
     */
    String hierarchy() default "";

    /**
     * Whether to generate code to cache the hash code.
     * <p>
     * Setting this to true will cause the hash code to be cached using the racy single check idiom.
     * The setting only applies to immutable beans.
     */
    boolean cacheHashCode() default false;

    /**
     * Whether to skip override of {@link Object#clone()}.
     * <p>
     * Setting this to true will cause the bean generator to omit overriding {@link Object#clone()}.
     * The setting only applies to mutable beans (note that {@link Object#clone()} will never be overridden
     * for immutable beans).
     */
    boolean skipOverrideClone() default false;


}
