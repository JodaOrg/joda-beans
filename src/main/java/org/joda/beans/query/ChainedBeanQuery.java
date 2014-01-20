/*
 *  Copyright 2001-2014 Stephen Colebourne
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
package org.joda.beans.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.joda.beans.Bean;
import org.joda.beans.BeanQuery;
import org.joda.beans.MetaProperty;

/**
 * A chained query, that allows two or more queries to be joined.
 * <p>
 * For example, consider a structure where class A has a property b of type B,
 * and class B has a property c of type C. The compound query allows property
 * c to be accessed directly from an instance of A.
 * 
 * @param <P>  the type of the result of the query
 * @author Stephen Colebourne
 */
public final class ChainedBeanQuery<P> implements BeanQuery<P> {

    /**
     * The list of queries.
     */
    private final List<BeanQuery<? extends Bean>> chain;
    /**
     * The last query.
     */
    private final BeanQuery<P> last;

    /**
     * Obtains a chained query from two other queries.
     * <p>
     * {@link MetaProperty} implements {@link BeanQuery}, so typically the parameters
     * are in fact meta-properties.
     * 
     * @param <P>  the result type
     * @param prop1  the first query, not null
     * @param prop2  the second query, not null
     * @return the compound query, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static <P> ChainedBeanQuery<P> of(BeanQuery<? extends Bean> prop1, BeanQuery<P> prop2) {
        if (prop1 == null || prop2 == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        List<BeanQuery<? extends Bean>> list = Collections.<BeanQuery<? extends Bean>>singletonList(prop1);
        return new ChainedBeanQuery<P>(list, prop2);
    }

    /**
     * Obtains a chained query from three queries.
     * <p>
     * {@link MetaProperty} implements {@link BeanQuery}, so typically the parameters
     * are in fact meta-properties.
     * 
     * @param <P>  the result type
     * @param prop1  the first query, not null
     * @param prop2  the second query, not null
     * @param prop3  the third query, not null
     * @return the compound query, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static <P> ChainedBeanQuery<P> of(BeanQuery<? extends Bean> prop1, BeanQuery<? extends Bean> prop2, BeanQuery<P> prop3) {
        if (prop1 == null || prop2 == null || prop3 == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        List<BeanQuery<? extends Bean>> list = new ArrayList<BeanQuery<? extends Bean>>();
        list.add(prop1);
        list.add(prop2);
        return new ChainedBeanQuery<P>(list, prop3);
    }

    /**
     * Obtains a chained query from four queries.
     * <p>
     * {@link MetaProperty} implements {@link BeanQuery}, so typically the parameters
     * are in fact meta-properties.
     * 
     * @param <P>  the result type
     * @param prop1  the first query, not null
     * @param prop2  the second query, not null
     * @param prop3  the third query, not null
     * @param prop4  the fourth query, not null
     * @return the compound query, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static <P> ChainedBeanQuery<P> of(BeanQuery<? extends Bean> prop1, BeanQuery<? extends Bean> prop2, BeanQuery<? extends Bean> prop3, BeanQuery<P> prop4) {
        if (prop1 == null || prop2 == null || prop3 == null || prop4 == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        List<BeanQuery<? extends Bean>> list = new ArrayList<BeanQuery<? extends Bean>>();
        list.add(prop1);
        list.add(prop2);
        list.add(prop3);
        return new ChainedBeanQuery<P>(list, prop4);
    }

    //-------------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    private ChainedBeanQuery(List<BeanQuery<? extends Bean>> metaProperties, BeanQuery<P> last) {
        this.chain = metaProperties;
        this.last = last;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the list of queries being chained.
     * <p>
     * {@link MetaProperty} implements {@link BeanQuery}, so typically the chain
     * is formed from meta-properties.
     * 
     * @return the list of all meta-properties being chained, not null
     */
    public List<BeanQuery<?>> getChain() {
        List<BeanQuery<?>> list = new ArrayList<BeanQuery<?>>(chain);
        list.add(last);
        return list;
    }

    //-------------------------------------------------------------------------
    @Override
    public P get(Bean bean) {
        for (BeanQuery<? extends Bean> mp : chain) {
            bean = mp.get(bean);
        }
        return last.get(bean);
    }

    //-------------------------------------------------------------------------
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(64);
        for (BeanQuery<? extends Bean> mp : chain) {
            buf.append(mp).append('.');
        }
        buf.append(last);
        return buf.toString();
    }

}
