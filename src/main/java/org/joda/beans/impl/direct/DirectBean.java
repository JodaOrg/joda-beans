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

import java.util.NoSuchElementException;
import java.util.Set;

import org.joda.beans.BeanUtils;
import org.joda.beans.impl.BasicBean;

/**
 * A bean implementation designed for use by the code generator.
 * <p>
 * This implementation uses direct access via {@link #propertyGet(String)} and
 * {@link #propertySet(String, Object)} to avoid reflection.
 * <p>
 * For code generation, the bean must directly extend this class and have a
 * no-arguments constructor.
 * 
 * @author Stephen Colebourne
 */
public abstract class DirectBean extends BasicBean {

    /**
     * Gets the value of the property.
     * 
     * @param propertyName  the property name, not null
     * @return the value of the property, may be null
     * @throws NoSuchElementException if the property name is invalid
     */
    protected Object propertyGet(String propertyName) {
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    /**
     * Sets the value of the property.
     * 
     * @param propertyName  the property name, not null
     * @param value  the value of the property, may be null
     * @throws NoSuchElementException if the property name is invalid
     */
    protected void propertySet(String propertyName, Object value) {
        throw new NoSuchElementException("Unknown property: " + propertyName);
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean equals(Object obj) {
        // override to gain better performance using propertyGet(String)
        if (obj == this) {
            return true;
        }
        if (obj != null && getClass() == obj.getClass()) {
            DirectBean other = (DirectBean) obj;
            Set<String> names = propertyNames();
            if (names.equals(other.propertyNames()) == false) {
                return false;
            }
            for (String name : names) {
                Object value1 = propertyGet(name);
                Object value2 = other.propertyGet(name);
                if (BeanUtils.equal(value1, value2) == false) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        // override to gain better performance using propertyGet(String)
        int hash = getClass().hashCode();
        Set<String> names = propertyNames();
        for (String name : names) {
            Object value = propertyGet(name);
            hash += (name.hashCode() ^ (value == null ? 0 : value.hashCode()));
        }
        return hash;
    }

    @Override
    public String toString() {
        // override to gain better performance using propertyGet(String)
        Set<String> names = propertyNames();
        StringBuilder buf = new StringBuilder((names.size()) * 32 + 32);
        buf.append(getClass().getSimpleName());
        buf.append('{');
        if (names.size() > 0) {
            for (String name : names) {
                Object value = propertyGet(name);
                buf.append(name).append('=').append(value).append(',').append(' ');
            }
            buf.setLength(buf.length() - 2);
        }
        buf.append('}');
        return buf.toString();
    }

}
