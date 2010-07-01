/*
 *  Copyright 2001-2010 Stephen Colebourne
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

import java.util.Map;

/**
 * A map of properties that is linked to a specific bean.
 * <p>
 * For a JavaBean, this will ultimately wrap get/set method pairs.
 * Alternate implementations may perform any logic to obtain the value.
 * 
 * @param <B>  the type of the bean
 * @author Stephen Colebourne
 */
public interface PropertyMap<B> extends Map<String, Property<B, Object>> {

    /**
     * Flattens the contents of this property map to a {@code HashMap}.
     * <p>
     * The returned map will contain all the properties from the bean with their actual values.
     * 
     * @return the unmodifiable map of property name to value, never null
     */
    Map<String, Object> flatten();

}
