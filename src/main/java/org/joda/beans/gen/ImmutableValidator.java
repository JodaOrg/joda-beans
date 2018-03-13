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
 * Annotation defining which method is to be used to validate the bean
 * when code generating immutable beans.
 * <p>
 * Each property in a bean can be independently validated.
 * This annotation allows properties to be cross-checked at the end of the constructor.
 * <p>
 * The method must be a private void instance method and take no arguments.
 * Private is necessary as it is called from the constructor.
 * For example:
 * <pre>
 *   {@literal @}ImmutableValidator
 *   private void validate() {
 *     if (age != null &amp;&amp; age {@literal <} 0) {
 *       throw new IllegalArgumentException("Age must not be negative if specified")
 *     }
 *   }
 * </pre>
 * 
 * @author Stephen Colebourne
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface ImmutableValidator {

}
