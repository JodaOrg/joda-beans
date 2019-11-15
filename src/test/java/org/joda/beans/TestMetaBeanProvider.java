/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.joda.beans;

public class TestMetaBeanProvider implements MetaBeanProvider {

  @Override
  public MetaBean findMetaBean(Class<?> cls) {
    if (cls.equals(AnnotatedBean.class)) {
      return new AnnotatedMetaBean();
    } else {
      return null;
    }
  }
}
