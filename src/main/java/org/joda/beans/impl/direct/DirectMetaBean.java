/*
 *  Copyright 2001-2011 Stephen Colebourne
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
package org.joda.beans.impl.direct;

import org.joda.beans.MetaProperty;
import org.joda.beans.impl.BasicMetaBean;

/**
 * A meta-bean implementation designed for use by {@code DirectBean}.
 * <p>
 * This implementation uses direct access via {@link #metaPropertyGet(String)} to avoid reflection.
 * 
 * @author Stephen Colebourne
 */
public abstract class DirectMetaBean extends BasicMetaBean {
    // overriding other methods has negligible effect considering DirectMetaPropertyMap

    /**
     * Gets the meta-property by name.
     * <p>
     * This implementation returns null, and must be overridden in subclasses.
     * 
     * @param propertyName  the property name, not null
     * @return the meta-property, null if not found
     */
    protected MetaProperty<?> metaPropertyGet(String propertyName) {
        return null;
    }

}
