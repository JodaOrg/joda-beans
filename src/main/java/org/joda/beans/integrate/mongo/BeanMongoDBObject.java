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
package org.joda.beans.integrate.mongo;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.joda.beans.Bean;

import com.mongodb.DBObject;

/**
 * Allows a Joda-Bean to be passed directly to MongoDB.
 *
 * @author Stephen Colebourne
 */
public class BeanMongoDBObject implements DBObject {

    /**
     * The underlying bean.
     */
    private final Bean bean;
    /**
     * The Mongo partial flag.
     */
    private boolean partial;

    /**
     * Creates an instance wrapping a bean.
     * @param bean  the bean to wrap, not null
     */
    public BeanMongoDBObject(Bean bean) {
        this.bean = bean;
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean containsField(String name) {
        return bean.propertyNames().contains(name);
    }

    /**
     * {@inheritDoc}
     * @deprecated Use containsField()
     */
    @Override
    @Deprecated
    public boolean containsKey(String name) {
        return containsField(name);
    }

    @Override
    public Object get(String name) {
        return bean.property(name).get();
    }

    @Override
    public Object put(String name, Object value) {
        return bean.property(name).put(value);
    }

    @Override
    public void putAll(DBObject object) {
        for (String name : object.keySet()) {
            put(name, object.get(name));
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void putAll(Map map) {
        Map<Object, Object> castMap = map;
        for (Entry<Object, Object> entry : castMap.entrySet()) {
            put(entry.getKey().toString(), map.get(entry.getValue()));
        }
    }

    @Override
    public Object removeField(String name) {
        throw new UnsupportedOperationException("Remove unsupported");
    }

    @Override
    public Set<String> keySet() {
        return bean.propertyNames();
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Map toMap() {
        return bean.metaBean().createPropertyMap(bean).flatten();
    }

    //-----------------------------------------------------------------------
    @Override
    public boolean isPartialObject() {
        return partial;
    }

    @Override
    public void markAsPartialObject() {
        partial = true;
    }

}
