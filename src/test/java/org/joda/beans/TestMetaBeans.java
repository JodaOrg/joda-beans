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
package org.joda.beans;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class TestMetaBeans {

  @Test
  public void test_metaBeanProviderAnnotation() {
    MetaBean metaBean = MetaBeans.lookup(AnnotatedBean.class);
    assertThat(metaBean).isInstanceOf(AnnotatedMetaBean.class);
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

