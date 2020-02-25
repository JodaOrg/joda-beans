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
