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
 * Annotation defining which method is to be used to apply the default property values
 * when code generating immutable beans.
 * <p>
 * Each non-collection property in an immutable bean normally has to be initialized before use.
 * This annotation allows default values to be set when creating the builder.
 * Note that the defaults apply to the builder, not to the constructor of the bean.
 * <p>
 * The method must be a private static void instance method that takes a single argument of the type 'Builder'.
 * Private is necessary as it is called from the builder constructor.
 * For example:
 * <pre>
 *   {@literal @}ImmutableDefaults
 *   private static void applyDefaults(Builder builder) {
 *     builder.group(Group.STANDARD);  // default the group property to 'STANDARD'
 *   }
 * </pre>
 * 
 * @author Stephen Colebourne
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ImmutableDefaults {

}
