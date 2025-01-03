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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.joda.beans.Bean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;
import org.joda.beans.gen.BeanDefinition;
import org.joda.beans.gen.PropertyDefinition;
import org.joda.beans.impl.BasicBeanBuilder;
import org.joda.beans.impl.direct.MinimalMetaBean;

/**
 * Mock minimal bean, used for testing.
 * 
 * @author Stephen Colebourne
 */
@BeanDefinition(style = "minimal")
public final class MinimalMutableGeneric<T extends Number> implements Bean, Serializable {

    /**
     * The number.
     */
    @PropertyDefinition
    private T number;
    /**
     * The number.
     */
    @PropertyDefinition
    private final List<T> list = new ArrayList<>();
    /**
     * The number.
     */
    @PropertyDefinition
    private final Map<String, T> map = new HashMap<>();

    //------------------------- AUTOGENERATED START -------------------------
    /**
     * The meta-bean for {@code MinimalMutableGeneric}.
     */
    @SuppressWarnings({"unchecked", "rawtypes" })
    private static final MetaBean META_BEAN =
            MinimalMetaBean.of(
                    MinimalMutableGeneric.class,
                    new String[] {
                            "number",
                            "list",
                            "map"},
                    () -> new BasicBeanBuilder<>(new MinimalMutableGeneric<>()),
                    Arrays.<Function<MinimalMutableGeneric, Object>>asList(
                            b -> b.getNumber(),
                            b -> b.getList(),
                            b -> b.getMap()),
                    Arrays.<BiConsumer<MinimalMutableGeneric, Object>>asList(
                            (b, v) -> b.setNumber((Number) v),
                            (b, v) -> b.setList((List<Number>) v),
                            (b, v) -> b.setMap((Map<String, Number>) v)));

    /**
     * The meta-bean for {@code MinimalMutableGeneric}.
     * @return the meta-bean, not null
     */
    public static MetaBean meta() {
        return META_BEAN;
    }

    static {
        MetaBean.register(META_BEAN);
    }

    /**
     * The serialization version id.
     */
    private static final long serialVersionUID = 1L;

    @Override
    @SuppressWarnings("unchecked")
    public TypedMetaBean<MinimalMutableGeneric<T>> metaBean() {
        return (TypedMetaBean<MinimalMutableGeneric<T>>) META_BEAN;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number.
     * @return the value of the property
     */
    public T getNumber() {
        return number;
    }

    /**
     * Sets the number.
     * @param number  the new value of the property
     */
    public void setNumber(T number) {
        this.number = number;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number.
     * @return the value of the property, not null
     */
    public List<T> getList() {
        return list;
    }

    /**
     * Sets the number.
     * @param list  the new value of the property, not null
     */
    public void setList(List<T> list) {
        JodaBeanUtils.notNull(list, "list");
        this.list.clear();
        this.list.addAll(list);
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the number.
     * @return the value of the property, not null
     */
    public Map<String, T> getMap() {
        return map;
    }

    /**
     * Sets the number.
     * @param map  the new value of the property, not null
     */
    public void setMap(Map<String, T> map) {
        JodaBeanUtils.notNull(map, "map");
        this.map.clear();
        this.map.putAll(map);
    }

    //-----------------------------------------------------------------------
    @Override
    public MinimalMutableGeneric<T> clone() {
        return JodaBeanUtils.cloneAlways(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            MinimalMutableGeneric<?> other = (MinimalMutableGeneric<?>) obj;
            return JodaBeanUtils.equal(this.getNumber(), other.getNumber()) &&
                    JodaBeanUtils.equal(this.getList(), other.getList()) &&
                    JodaBeanUtils.equal(this.getMap(), other.getMap());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = getClass().hashCode();
        hash = hash * 31 + JodaBeanUtils.hashCode(getNumber());
        hash = hash * 31 + JodaBeanUtils.hashCode(getList());
        hash = hash * 31 + JodaBeanUtils.hashCode(getMap());
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(128);
        buf.append("MinimalMutableGeneric{");
        buf.append("number").append('=').append(JodaBeanUtils.toString(getNumber())).append(',').append(' ');
        buf.append("list").append('=').append(JodaBeanUtils.toString(getList())).append(',').append(' ');
        buf.append("map").append('=').append(JodaBeanUtils.toString(getMap()));
        buf.append('}');
        return buf.toString();
    }

    //-------------------------- AUTOGENERATED END --------------------------
}
