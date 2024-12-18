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
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.joda.beans.impl.flexi.FlexiBean;
import org.joda.beans.impl.map.MapBean;
import org.joda.beans.sample.ImmPerson;
import org.joda.beans.sample.MetaBeanLoad;
import org.junit.jupiter.api.Test;

/**
 * Test {@link MetaBean}.
 */
class TestMetaBean {

    //-----------------------------------------------------------------------
    @Test
    void test_registerMetaBean() {
        // register once OK
        assertThat(ImmPerson.meta()).isNotNull();
        // register second time not OK
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MetaBean.register(ImmPerson.meta()));
    }

    //-----------------------------------------------------------------------
    @Test
    void test_metaBean() {
        MetaBean metaBean = MetaBean.of(MetaBeanLoad.class);
        assertThat(metaBean).isNotNull();
        assertThat(metaBean).isEqualTo(MetaBeanLoad.meta());
    }

    @Test
    void test_metaBean_FlexiBean() {
        assertThat(MetaBean.of(FlexiBean.class).builder().build().getClass()).isEqualTo(FlexiBean.class);
    }

    @Test
    void test_metaBean_MapBean() {
        assertThat(MetaBean.of(MapBean.class).builder().build().getClass()).isEqualTo(MapBean.class);
    }

    @Test
    void test_metaBean_notFound() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> MetaBean.of(String.class));
    }

}
