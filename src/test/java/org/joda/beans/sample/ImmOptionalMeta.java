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

import org.joda.beans.MetaProperty;

import com.google.common.base.Optional;

/**
 * Mock.
 */
public interface ImmOptionalMeta {

    /**
     * The meta-property for the {@code optString} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<Optional<String>> optString();

    /**
     * The meta-property for the {@code optStringEmpty} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<Optional<String>> optStringEmpty();

    /**
     * The meta-property for the {@code optStringGetter} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<String> optStringGetter();

    /**
     * The meta-property for the {@code optLongGetter} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<Long> optLongGetter();

    /**
     * The meta-property for the {@code optIntGetter} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<Integer> optIntGetter();

    /**
     * The meta-property for the {@code optDoubleGetter} property.
     * @return the meta-property, not null
     */
    public abstract MetaProperty<Double> optDoubleGetter();

}
