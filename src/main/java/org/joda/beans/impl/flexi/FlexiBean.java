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
package org.joda.beans.impl.flexi;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.beans.DynamicBean;
import org.joda.beans.DynamicMetaBean;
import org.joda.beans.Property;
import org.joda.beans.impl.BasicProperty;

/**
 * Implementation of a fully dynamic {@code Bean}.
 * <p>
 * Properties are dynamic, and can be added and removed at will from the map.
 * The internal storage is created lazily to allow a flexi-bean to be used as
 * a lightweight extension to another bean.
 * <p>
 * Each flexi-bean has a different set of properties.
 * As such, there is one instance of meta-bean for each flexi-bean.
 * <p>
 * The keys of a flexi-bean must be simple identifiers as per '[a-zA-z_][a-zA-z0-9_]*'.
 * 
 * @author Stephen Colebourne
 */
public final class FlexiBean implements DynamicBean, Serializable {
    // Alternate way to implement this would be to create a list/map of real property
    // objects which could then be properly typed

    /** Serialization version. */
    private static final long serialVersionUID = 1L;
    /** Valid regex for keys. */
    private static final Pattern VALID_KEY = Pattern.compile("[a-zA-z_][a-zA-z0-9_]*");

    /** The meta-bean. */
    private final transient FlexiMetaBean metaBean = new FlexiMetaBean(this);  // CSIGNORE
    /** The underlying data. */
    volatile Map<String, Object> data = Collections.emptyMap();// CSIGNORE

    //-----------------------------------------------------------------------
    /**
     * Creates a standalone meta-bean.
     * <p>
     * This creates a new instance each time in line with dynamic bean principles.
     * 
     * @return the meta-bean, not null
     */
    public static DynamicMetaBean meta() {
        return new FlexiBean().metaBean();
    }

    //-----------------------------------------------------------------------
    /**
     * Constructor.
     */
    public FlexiBean() {
    }

    /**
     * Constructor that copies all the data entries from the specified bean.
     * 
     * @param copyFrom  the bean to copy from, not null
     */
    public FlexiBean(FlexiBean copyFrom) {
        putAll(copyFrom.data);
    }

    // resolve to setup transient field
    private Object readResolve() throws ObjectStreamException {
        return new FlexiBean(this);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the internal data map.
     * 
     * @return the data, not null
     */
    private Map<String, Object> dataWritable() {
        if (data == Collections.EMPTY_MAP) {
            data = new LinkedHashMap<>();
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
     * <p>
     * This returns null if the property does not exist.
     * 
     * @param propertyName  the property name, not empty
     * @return the value of the property, may be null
     */
    public Object get(String propertyName) {
        return data.get(propertyName);
    }

    /**
     * Gets the value of the property cast to a specific type.
     * <p>
     * This returns null if the property does not exist.
     * 
     * @param <T>  the value type
     * @param propertyName  the property name, not empty
     * @param type  the type to cast to, not null
     * @return the value of the property, may be null
     * @throws ClassCastException if the type is incorrect
     */
    public <T> T get(String propertyName, Class<T> type) {
        return type.cast(get(propertyName));
    }

    /**
     * Gets the value of the property as a {@code String}.
     * This will use {@link Object#toString()}.
     * <p>
     * This returns null if the property does not exist.
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
     * @throws NullPointerException if the property does not exist or is null
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
     * @throws NullPointerException if the property does not exist or is null
     */
    public int getInt(String propertyName) {
        return ((Number) get(propertyName)).intValue();
    }

    /**
     * Gets the value of the property as a {@code int} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null or invalid property
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
     * @throws NullPointerException if the property does not exist or is null
     */
    public long getLong(String propertyName) {
        return ((Number) get(propertyName)).longValue();
    }

    /**
     * Gets the value of the property as a {@code long} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null or invalid property
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
     * @throws NullPointerException if the property does not exist or is null
     */
    public double getDouble(String propertyName) {
        return ((Number) get(propertyName)).doubleValue();
    }

    /**
     * Gets the value of the property as a {@code double} using a default value.
     * 
     * @param propertyName  the property name, not empty
     * @param defaultValue  the default value for null or invalid property
     * @return the value of the property
     * @throws ClassCastException if the value is not compatible
     */
    public double getDouble(String propertyName, double defaultValue) {
        Object obj = get(propertyName);
        return obj != null ? ((Number) get(propertyName)).doubleValue() : defaultValue;
    }

    //-----------------------------------------------------------------------
    /**
     * Sets a property in this bean to the specified value.
     * <p>
     * This creates a property if one does not exist.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     * @return {@code this} for chaining, not null
     */
    public FlexiBean append(String propertyName, Object newValue) {
        put(propertyName, newValue);
        return this;
    }

    /**
     * Sets a property in this bean to the specified value.
     * <p>
     * This creates a property if one does not exist.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     */
    public void set(String propertyName, Object newValue) {
        put(propertyName, newValue);
    }

    /**
     * Sets a property in this bean to the specified value.
     * <p>
     * This creates a property if one does not exist.
     * 
     * @param propertyName  the property name, not empty
     * @param newValue  the new value, may be null
     * @return the old value of the property, may be null
     */
    public Object put(String propertyName, Object newValue) {
        if (VALID_KEY.matcher(propertyName).matches() == false) {
            throw new IllegalArgumentException("Invalid key for FlexiBean: " + propertyName);
        }
        return dataWritable().put(propertyName, newValue);
    }

    /**
     * Puts the properties in the specified map into this bean.
     * <p>
     * This creates properties if they do not exist.
     * 
     * @param map  the map of properties to add, not null
     */
    public void putAll(Map<String, ? extends Object> map) {
        if (map.size() > 0) {
            for (String key : map.keySet()) {
                if (VALID_KEY.matcher(key).matches() == false) {
                    throw new IllegalArgumentException("Invalid key for FlexiBean: " + key);
                }
            }
            if (data == Collections.EMPTY_MAP) {
                data = new LinkedHashMap<>(map);
            } else {
                data.putAll(map);
            }
        }
    }

    /**
     * Puts the properties in the specified bean into this bean.
     * <p>
     * This creates properties if they do not exist.
     * 
     * @param other  the map of properties to add, not null
     */
    public void putAll(FlexiBean other) {
        if (other.size() > 0) {
            if (data == Collections.EMPTY_MAP) {
                data = new LinkedHashMap<>(other.data);
            } else {
                data.putAll(other.data);
            }
        }
    }

    /**
     * Removes a property.
     * <p>
     * No error occurs if the property does not exist.
     * 
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
    public boolean propertyExists(String propertyName) {
        return data.containsKey(propertyName);
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
        put(propertyName, newValue);
    }

    //-----------------------------------------------------------------------
    @Override
    public DynamicMetaBean metaBean() {
        return metaBean;
    }

    @Override
    public Property<Object> property(String name) {
        return BasicProperty.of(this, FlexiMetaProperty.of(metaBean, name));
    }

    @Override
    public Set<String> propertyNames() {
        return data.keySet();
    }

    @Override
    public void propertyDefine(String propertyName, Class<?> propertyType) {
        if (propertyExists(propertyName) == false) {
            put(propertyName, null);
        }
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
        return Collections.unmodifiableMap(new LinkedHashMap<>(data));
    }

    //-----------------------------------------------------------------------
    /**
     * Clones this bean, returning an independent copy.
     * 
     * @return the clone, not null
     */
    @Override
    public FlexiBean clone() {
        return new FlexiBean(this);
    }

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

}
