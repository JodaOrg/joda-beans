/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.joda.beans;

/**
 *
 */
interface MetaBeanProvider {

    MetaBean findMetaBean(Class<?> cls);
}
