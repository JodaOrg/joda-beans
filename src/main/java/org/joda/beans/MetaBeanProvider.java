/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.joda.beans;

/**
 * A provider of {@link MetaBean} instances for bean classes.
 */
public interface MetaBeanProvider {

    /**
     * Returns the meta bean for the class or null if no meta bean can be found.
     *
     * @param cls the class for which the meta bean is required
     * @return the meta bean for the class or null if no meta bean can be found
     */
    MetaBean findMetaBean(Class<?> cls);
}
