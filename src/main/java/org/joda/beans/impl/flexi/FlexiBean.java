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
package org.joda.beans.impl.flexi;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.Bean;
import org.joda.beans.DynamicBean;
import org.joda.beans.MetaBean;
import org.joda.beans.MetaProperty;
import org.joda.beans.Property;
import org.joda.beans.PropertyMap;
import org.joda.beans.impl.BasicBean;
import org.joda.beans.impl.BasicProperty;

/**
 * Implementation of a fully dynamic {@code Bean}.
 * <p>
 * Properties are dynamic, and can be added and removed at will from the map.
 * The internal storage is created lazily to allow a flexi-bean to be used as
 * a lightweight extension to another bean.
 * 
 * @author Stephen Colebourne
 */
public final class FlexiBean extends BasicBean implements DynamicBean, Serializable {
    // Alternate way to implement this would be to create a list/map of real property
    // objects which could then be properly typed

    /** Serialization version. */
    private static final long serialVersionUID = 1L;

    /** The meta-bean. */
    final FlexiMetaBean metaBean = new FlexiMetaBean();
    /** The underlying data. */
    volatile Map<String, Object> data = Collections.emptyMap();

    /**
     * Constructor.
     */
    public FlexiBean() {
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the internal data map.
     * 
     * @return the data, not null
     */
    private Map<String, Object> dataWritable() {
        if (data == Collections.EMPTY_MAP) {
            data = new HashMap<String, Object>();
        }
        return data;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number of properties.
     * 
     * @return the number of properties
     */
    public int size() {
        return data.size();
    }

    /**
     * Checks if the bean contains a specific property.
     * 
     * @param propertyName  the property name, null returns false
     * @return true if the bean contains the property
     */
    public boolean contains(String propertyName) {
        return propertyExists(propertyName);
    }

    /**
     * Gets the value of the property.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property, may be null
     */
    public Object get(String propertyName) {
        return data.get(propertyName);
    }

    /**
     * Gets the value of the property cast to a specific type.
     * 
     * @param propertyName  the property name, not empty
     * @param type  the type to cast to, not null
     * @return the value of the property, may be null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String propertyName, Class<T> type) {
        return (T) get(propertyName);
    }

    /**
     * Gets the value of the property as a {@code String}.
     * This will use {@link Object#toString()}.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property, may be null
     */
    public String getString(String propertyName) {
        Object obj = get(propertyName);
        return obj != null ? obj.toString() : null;
    }

    /**
     * Gets the value of the property as a {@code boolean}.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public boolean getBoolean(String propertyName) {
        return (Boolean) get(propertyName);
    }

    /**
     * Gets the value of the property as a {@code int}.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public int getInt(String propertyName) {
        return ((Number) get(propertyName)).intValue();
    }

    /**
     * Gets the value of the property as a {@code int} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public int getInt(String propertyName, int defaultValue) {
        Object obj = get(propertyName);
        return obj != null ? ((Number) get(propertyName)).intValue() : defaultValue;
    }

    /**
     * Gets the value of the property as a {@code long}.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public long getLong(String propertyName) {
        return ((Number) get(propertyName)).longValue();
    }

    /**
     * Gets the value of the property as a {@code long} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public long getLong(String propertyName, long defaultValue) {
        Object obj = get(propertyName);
        return obj != null ? ((Number) get(propertyName)).longValue() : defaultValue;
    }

    /**
     * Gets the value of the property as a {@code double}.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public double getDouble(String propertyName) {
        return ((Number) get(propertyName)).doubleValue();
    }

    /**
     * Gets the value of the property as a {@code double} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public double getDouble(String propertyName, double defaultValue) {
        Object obj = get(propertyName);
        return obj != null ? ((Number) get(propertyName)).doubleValue() : defaultValue;
    }

    //-----------------------------------------------------------------------
    /**
     * Adds or updates a property returning {@code this} for chaining.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     * @return {@code this} for chaining, not null
     */
    public FlexiBean append(String propertyName, Object newValue) {
        dataWritable().put(propertyName, newValue);
        return this;
    }

    /**
     * Adds or updates a property.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     */
    public void set(String propertyName, Object newValue) {
        dataWritable().put(propertyName, newValue);
    }

    /**
     * Puts the property into this bean.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     * @return the old value of the property, may be null
     */
    public Object put(String propertyName, Object newValue) {
        return dataWritable().put(propertyName, newValue);
    }

    /**
     * Puts the properties in the specified map into this bean.
     * 
     * @param map  the map of properties to add, not null
     */
    public void putAll(Map<String, Object> map) {
        if (map.size() > 0) {
            if (data == Collections.EMPTY_MAP) {
                data = new HashMap<String, Object>(map);
            } else {
                data.putAll(map);
            }
        }
    }

    /**
     * Puts the properties in the specified bean into this bean.
     * 
     * @param other  the map of properties to add, not null
     */
    public void putAll(FlexiBean other) {
        if (other.size() > 0) {
            if (data == Collections.EMPTY_MAP) {
                data = new HashMap<String, Object>(other.data);
            } else {
                data.putAll(other.data);
            }
        }
    }

    /**
     * Removes a property.
     * @param propertyName  the property name, not empty
     */
    public void remove(String propertyName) {
        propertyRemove(propertyName);
    }

    /**
     * Removes all properties.
     */
    public void clear() {
        if (data != Collections.EMPTY_MAP) {
            data.clear();
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Checks if the property exists.
     * 
     * @param propertyName  the property name, not empty
     * @return true if the property exists
     */
    public boolean propertyExists(String name) {
        return data.containsKey(name);
    }

    /**
     * Gets the value of the property.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property, may be null
     */
    public Object propertyGet(String propertyName) {
        if (propertyExists(propertyName) == false) {
            throw new NoSuchElementException("Unknown property: " + propertyName);
        }
        return data.get(propertyName);
    }

    /**
     * Sets the value of the property.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value of the property, may be null
     */
    public void propertySet(String propertyName, Object newValue) {
        dataWritable().put(propertyName, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public MetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Property<Object> property(String name) {
        if (propertyExists(name) == false) {
            throw new NoSuchElementException("Unknown property: " + name);
        }
        return BasicProperty.of(this, FlexiMetaProperty.of(metaBean, name));
    }

    @Override
    public Set<String> propertyNames() {
        return data.keySet();
    }

    @Override
    public void propertyDefine(String propertyName, Class<?> propertyType) {
        // no need to define
    }

    @Override
    public void propertyRemove(String propertyName) {
        if (data != Collections.EMPTY_MAP) {
            data.remove(propertyName);
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns a map representing the contents of the bean.
     * 
     * @return a map representing the contents of the bean, not null
     */
    public Map<String, Object> toMap() {
        if (size() == 0) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new HashMap<String, Object>(data));
    }

    //-----------------------------------------------------------------------
    /**
     * Compares this bean to another based on the property names and content.
     * 
     * @param obj  the object to compare to, null returns false
     * @return true if equal
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof FlexiBean) {
            FlexiBean other = (FlexiBean) obj;
            return this.data.equals(other.data);
        }
        return super.equals(obj);
    }

    /**
     * Returns a suitable hash code.
     * 
     * @return a hash code
     */
    @Override
    public int hashCode() {
        return data.hashCode();
    }

    /**
     * Returns a string that summarises the bean.
     * <p>
     * The string contains the class name and properties.
     * 
     * @return a summary string, not null
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + data.toString();
    }

    //-----------------------------------------------------------------------
    class FlexiMetaBean implements MetaBean {
        @Override
        public FlexiBean createBean() {
            return new FlexiBean();
        }

        @Override
        public PropertyMap createPropertyMap(Bean bean) {
            return FlexiPropertyMap.of(beanType().cast(bean));
        }

        @Override
        public Class<FlexiBean> beanType() {
            return FlexiBean.class;
        }

        @Override
        public String beanName() {
            return FlexiBean.class.getName();
        }

        @Override
        public int metaPropertyCount() {
            return FlexiBean.this.size();
        }

        @Override
        public boolean metaPropertyExists(String name) {
            return FlexiBean.this.propertyExists(name);
        }

        @Override
        public MetaProperty<Object> metaProperty(String name) {
            Object obj = get(name);
            if (obj == null) {
                throw new NoSuchElementException("Unknown property: " + name);
            }
            return FlexiMetaProperty.of(metaBean, name);
        }

        @Override
        public Iterable<MetaProperty<?>> metaPropertyIterable() {
            if (data.isEmpty()) {
                return Collections.emptySet();
            }
            return new Iterable<MetaProperty<?>>() {
                private final Iterator<String> it = data.keySet().iterator();
                @Override
                public Iterator<MetaProperty<?>> iterator() {
                    return new Iterator<MetaProperty<?>>() {
                        @Override
                        public boolean hasNext() {
                            return it.hasNext();
                        }
                        @Override
                        public MetaProperty<?> next() {
                            return FlexiMetaProperty.of(metaBean, it.next());
                        }
                        @Override
                        public void remove() {
                            throw new UnsupportedOperationException("Unmodifiable");
                        }
                        
                    };
                }
            };
        }

        @Override
        public Map<String, MetaProperty<?>> metaPropertyMap() {
            if (data.isEmpty()) {
                return Collections.emptyMap();
            }
            Map<String, MetaProperty<?>> map = new HashMap<String, MetaProperty<?>>();
            for (String name : data.keySet()) {
                map.put(name, FlexiMetaProperty.of(metaBean, name));
            }
            return Collections.unmodifiableMap(map);
        }
    }

}
