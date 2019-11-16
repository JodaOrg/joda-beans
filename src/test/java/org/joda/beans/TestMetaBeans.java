package org.joda.beans;

import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class TestMetaBeans {

  @Test
  public void test_metaBeanProviderAnnotation() {
    MetaBean metaBean = MetaBeans.lookup(AnnotatedBean.class);
    assertTrue(metaBean instanceof AnnotatedMetaBean);
  }
}

// --------------------------------------------------------------------------------------------------

@MetaProvider(TestMetaBeanProvider.class)
class AnnotatedBean implements Bean {

  @Override
  public MetaBean metaBean() {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }

  @Override
  public <R> Property<R> property(String propertyName) {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }

  @Override
  public Set<String> propertyNames() {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }
}

class AnnotatedMetaBean implements MetaBean {

  @Override
  public boolean isBuildable() {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }

  @Override
  public BeanBuilder<? extends Bean> builder() {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }

  @Override
  public Class<? extends Bean> beanType() {
    return AnnotatedBean.class;
  }

  @Override
  public Map<String, MetaProperty<?>> metaPropertyMap() {
    throw new UnsupportedOperationException("This method is not needed for testing");
  }
}

