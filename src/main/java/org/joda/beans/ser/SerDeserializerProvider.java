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
package org.joda.beans.ser;

/**
 * Provides access to deserializers.
 * <p>
 * This plugin point allows instances to {@link SerDeserializer} to be created.
 * Implementations of this interface can introspect the bean type when choosing a deserializer.
 * This allows deserializers to be provided that can handle multiple bean types, for example all beans
 * in a particular package, any bean with a particular supertype or with a particular annotation.
 *
 * @author Stephen Colebourne
 */
public interface SerDeserializerProvider {

    /**
     * Finds the deserializer for the specified type.
     * <p>
     * If the type is not known, the implementation must return null.
     * 
     * 
     * @param beanType  the type being processed, not null
     * @return the deserializer, null if this provider does not support the type
     */
    public abstract SerDeserializer findDeserializer(Class<?> beanType);

}
