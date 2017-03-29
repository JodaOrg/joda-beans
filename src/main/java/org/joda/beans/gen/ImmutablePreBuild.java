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
 * Annotation defining a method that is to be called just before a code generated immutable bean is built.
 * <p>
 * This is used when an immutable bean wants to perform an action on the builder just
 * before it is built into a bean. The action might include validation (normally done
 * using {@link ImmutableValidator}) and defaulting (normally done using {@link ImmutableDefaults}).
 * <p>
 * The special use case foe this annotation is the ability to default the value of one property
 * from the value of another. For example, consider a bean with two dates, where one is derived
 * from the other (such as the second being the first adjusted to a valid business day).
 * Use of this annotation allows the second date to be set to the same as the first date if
 * a value is not set.
 * <p>
 * The method must be a private static void instance method that takes a single argument of the type 'Builder'.
 * The method will be called at the start of the {@code build()} method of the builder.
 * For example:
 * <pre>
 *   {@literal @}ImmutablePreBuild
 *   private static void preBuild(Builder builder) {
 *     if (builder.date2 == null) {
 *       builder.date2 = builder.date1;  // default date2 to be same as date1
 *     }
 *   }
 * </pre>
 * 
 * @author Stephen Colebourne
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ImmutablePreBuild {

}
