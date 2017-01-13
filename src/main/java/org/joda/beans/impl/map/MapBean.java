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
package org.joda.beans.impl.map;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.joda.beans.DynamicBean;
import org.joda.beans.DynamicMetaBean;
import org.joda.beans.Property;
import org.joda.beans.impl.BasicProperty;
import org.joda.beans.impl.flexi.FlexiBean;

/**
 * Implementation of a fully dynamic {@code Bean} based on an exposed {@code Map}.
 * <p>
 * Properties are dynamic, and can be added and removed at will from the map.
 * <p>
 * This class extends {@link HashMap}, allowing it to be used wherever a map is.
 * See {@link FlexiBean} for a map-like bean implementation that is more controlled.
 * 
 * @author Stephen Colebourne
 */
public class MapBean extends HashMap<String, Object> implements DynamicBean {

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    //-----------------------------------------------------------------------
    /**
     * Creates a standalone meta-bean.
     * <p>
     * This creates a new instance each time in line with dynamic bean principles.
     * 
     * @return the meta-bean, not null
     */
    public static DynamicMetaBean meta() {
        return new MapBean().metaBean();
    }

    //-----------------------------------------------------------------------
    /**
     * Creates an instance.
     */
    public MapBean() {
    }

    /**
     * Creates an instance.
     * 
     * @param map  the map to copy, not null
     */
    private MapBean(Map<String, Object> map) {
        super(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public DynamicMetaBean metaBean() {
        return new MapMetaBean(this);
    }

    @Override
    public Property<Object> property(String name) {
        return BasicProperty.of(this, MapBeanMetaProperty.of(metaBean(), name));
    }

    @Override
    public Set<String> propertyNames() {
        return keySet();
    }

    @Override
    public void propertyDefine(String propertyName, Class<?> propertyType) {
        if (containsKey(propertyName) == false) {
            put(propertyName, null);
        }
    }

    @Override
    public void propertyRemove(String propertyName) {
        remove(propertyName);
    }

    @Override
    public MapBean clone() {
        return new MapBean(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a string that summarises the bean.
     * <p>
     * The string contains the class name and properties.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + super.toString();
    }

}
