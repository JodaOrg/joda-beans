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
package org.joda.beans.ser;

/**
 * Provides mime types for Joda-Beans.
 *
 * @author Stephen Colebourne
 */
public final class JodaBeanMimeType {

    /**
     * Mime type for the binary format.
     */
    public static final String BINARY = "application/vnd.org.joda.bean";
    /**
     * Mime type for the XML format.
     */
    public static final String XML = "application/vnd.org.joda.bean+xml";
    /**
     * Mime type for the JSON format.
     */
    public static final String JSON = "application/vnd.org.joda.bean+json";

    /**
     * Restricted constructor
     */
    private JodaBeanMimeType() {
    }

}
