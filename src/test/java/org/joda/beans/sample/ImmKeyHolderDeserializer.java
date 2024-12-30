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
package org.joda.beans.sample;

import java.lang.reflect.Type;

import org.joda.beans.BeanBuilder;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.impl.BufferingBeanBuilder;
import org.joda.beans.impl.StandaloneMetaProperty;
import org.joda.beans.ser.DefaultDeserializer;

import com.google.common.reflect.TypeToken;

/**
 * Mock deserializer.
 */
final class ImmKeyHolderDeserializer extends DefaultDeserializer {

    @SuppressWarnings("serial")
    private static final Type VALUE_TYPE = new TypeToken<ImmGeneric<String>>() {
    }.getType();

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final StandaloneMetaProperty<ImmGeneric<String>> VALUE_PROPERTY = StandaloneMetaProperty.of(
            "value",
            ImmKeyHolder.meta(),
            (Class) ImmGeneric.class,
            VALUE_TYPE);

    ImmKeyHolderDeserializer() {
    }

    @Override
    public BeanBuilder<?> createBuilder(Class<?> beanType, MetaBean metaBean) {
        return BufferingBeanBuilder.of(metaBean);
    }

    @Override
    public MetaProperty<?> findMetaProperty(Class<?> beanType, MetaBean metaBean, String propertyName) {
        if (propertyName.equals(VALUE_PROPERTY.name())) {
            return VALUE_PROPERTY;
        } else {
            return metaBean.metaProperty(propertyName);
        }
    }

    @Override
    public void setValue(BeanBuilder<?> builder, MetaProperty<?> metaProp, Object value) {
        if (metaProp.equals(VALUE_PROPERTY)) {
            ImmGeneric<?> generic = (ImmGeneric<?>) value;
            builder.set(ImmKeyHolder.meta().value(), toImmKey(generic));
        } else {
            builder.set(metaProp, value);
        }
    }

    private ImmKey toImmKey(ImmGeneric<?> generic) {
        return ImmKey.builder()
                .name(String.valueOf(generic.getValue()))
                .build();
    }

}
