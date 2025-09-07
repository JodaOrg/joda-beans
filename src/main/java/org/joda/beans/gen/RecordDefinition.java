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
 * Annotation defining a bean for code generation.
 * <p>
 * This annotation must be used on classes that should be treated as beans.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RecordDefinition {

    /**
     * The scope of the builder class.
     * <p>
     * By default, this follows 'smart' rules, which generates a public builder.
     * Set to 'private' to generate a private builder.
     * Set to 'package' to generate a package-scoped builder.
     * Set to 'public' to generate a public builder.
     * Set to 'none' to ensure that no builder is generated.
     * 
     * @return the builder scope, defaulted to 'smart'
     */
    String builderScope() default "smart";

    /**
     * The name of the manual builder class.
     * <p>
     * This is used when there is a desire to manually write the builder.
     * By default, this is not set, and a builder is generated based on the scope.
     * Set to 'FooBuilder' to generate code that expects a manually written builder named FooBuilder
     * (which may be an inner class or a top-level class).
     * 
     * @return the builder scope, defaulted to ''
     */
    String builderName() default "";

    /**
     * The name of the factory method.
     * <p>
     * By default, this is an empty string and no factory is generated.
     * Set to 'of' to generate a factory method named 'of'.
     * 
     * @return the factory name, defaulted to ''
     */
    String factoryName() default "";

}
