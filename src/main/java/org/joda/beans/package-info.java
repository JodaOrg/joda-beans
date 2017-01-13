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

/**
 * Base interfaces and annotations defining Joda-Beans.
 * <p>
 * Joda-Beans is a library that can be used to provide enhanced Java Beans.
 * These extensions provide the tools for framework writers to access bean and property
 * information in a consistent and fast manner, typically without reflection.
 * <p>
 * A Joda-Bean implements the {@code Bean} interface. In turn, this requires the
 * creation of a {@code MetaBean} implementation, typically an inner class.
 * Both also require the provision of implementations of {@code Property} and
 * {@code MetaProperty} to express the properties of the bean.
 * <p>
 * Other packages provide implementations of the interfaces and a code generator.
 */
package org.joda.beans;
