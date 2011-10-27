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
package org.joda.beans.query;

import java.util.Comparator;

import org.joda.beans.Bean;
import org.joda.beans.BeanQuery;
import org.joda.beans.MetaBean;

/**
 * A set of utilities to assist when working with beans and properties.
 * 
 * @author Stephen Colebourne
 */
public final class MetaPropertyComparator implements Comparator<Bean> {

    /**
     * The cache of meta-beans.
     */
    private final BeanQuery<?> query;

    //-------------------------------------------------------------------------
    /**
     * Obtains a comparator .
     * <p>
     * This only works for those beans that have registered their meta-beans.
     * See {@link #registerMetaBean(MetaBean)}.
     * 
     * @param cls  the class to get the meta-bean for, not null
     * @return the meta-bean, not null
     * @throws IllegalArgumentException if unable to obtain the meta-bean
     */
    public static MetaPropertyComparator ofAscending(BeanQuery<?> query) {
        if (query == null) {
            throw new NullPointerException("BeanQuery must not be null");
        }
        return new MetaPropertyComparator(query);
    }

    //-----------------------------------------------------------------------
    /**
     * Restricted constructor.
     */
    private MetaPropertyComparator(BeanQuery<?> query) {
        this.query = query;
    }

    //-------------------------------------------------------------------------
    @Override
    public int compare(Bean bean1, Bean bean2) {
        @SuppressWarnings("unchecked")
        Comparable<Object> value1 = (Comparable<Object>) query.get(bean1);
        Object value2 = query.get(bean2);
        return value1.compareTo(value2);
    }

}
