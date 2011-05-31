/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
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
