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
