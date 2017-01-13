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
package org.joda.beans.ser.xml;

import javax.xml.namespace.QName;

/**
 * Constants used in XML.
 *
 * @author Stephen Colebourne
 */
final class JodaBeanXml {

    /**
     * XML bean tag.
     */
    public static final String BEAN = "bean";
    /**
     * XML bean QName.
     */
    public static final QName BEAN_QNAME = new QName(null, BEAN);
    /**
     * XML item tag.
     */
    public static final String ITEM = "item";
    /**
     * XML item QName.
     */
    public static final QName ITEM_QNAME = new QName(null, ITEM);
    /**
     * XML entry tag.
     */
    public static final String ENTRY = "entry";
    /**
     * XML entry QName.
     */
    public static final QName ENTRY_QNAME = new QName(null, ENTRY);
    /**
     * XML type attribute.
     */
    public static final String TYPE = "type";
    /**
     * XML type QName.
     */
    public static final QName TYPE_QNAME = new QName(null, TYPE);
    /**
     * XML key attribute.
     */
    public static final String KEY = "key";
    /**
     * XML key QName.
     */
    public static final QName KEY_QNAME = new QName(null, KEY);
    /**
     * XML rows attribute.
     */
    public static final String ROWS = "rows";
    /**
     * XML rows QName.
     */
    public static final QName ROWS_QNAME = new QName(null, ROWS);
    /**
     * XML cols attribute.
     */
    public static final String COLS = "cols";
    /**
     * XML cols QName.
     */
    public static final QName COLS_QNAME = new QName(null, COLS);
    /**
     * XML row attribute.
     */
    public static final String ROW = "row";
    /**
     * XML row QName.
     */
    public static final QName ROW_QNAME = new QName(null, ROW);
    /**
     * XML col attribute.
     */
    public static final String COL = "col";
    /**
     * XML col QName.
     */
    public static final QName COL_QNAME = new QName(null, COL);
    /**
     * XML count attribute.
     */
    public static final String COUNT = "count";
    /**
     * XML count QName.
     */
    public static final QName COUNT_QNAME = new QName(null, COUNT);
    /**
     * XML meta-type attribute.
     */
    public static final String METATYPE = "metatype";
    /**
     * XML meta-type QName.
     */
    public static final QName METATYPE_QNAME = new QName(null, METATYPE);
    /**
     * XML null attribute.
     */
    public static final String NULL = "null";
    /**
     * XML null QName.
     */
    public static final QName NULL_QNAME = new QName(null, NULL);

    /**
     * Restricted constructor.
     */
    private JodaBeanXml() {
    }

}
