/*
 *  Copyright 2001-2013 Stephen Colebourne
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
package org.joda.beans;

/**
 * An enumeration of property styles.
 * <p>
 * A property may be read-only, read-write or write-only.
 * This enumeration models those options.
 * <p>
 * It is strongly recommended to use the methods, not compare against the enum values.
 * 
 * @author Stephen Colebourne
 */
public enum PropertyStyle {

    /**
     * The property can be read and written.
     */
    READ_WRITE,
    /**
     * The property is read-only.
     */
    READ_ONLY,
    /**
     * The property is write-only.
     */
    WRITE_ONLY,
    /**
     * The property is derived.
     * It is read-only.
     */
    DERIVED,
    /**
     * The property is immutable.
     * It can be read and written via the builder.
     */
    IMMUTABLE;

    //-----------------------------------------------------------------------
    /**
     * Checks whether the property is readable.
     * 
     * @return true if the property can be read
     */
    public boolean isReadable() {
        return this == READ_WRITE || this == READ_ONLY || this == DERIVED || this == IMMUTABLE;
    }

    /**
     * Checks whether the property is writable.
     * 
     * @return true if the property can be written
     */
    public boolean isWritable() {
        return this == READ_WRITE || this == WRITE_ONLY;
    }

    /**
     * Checks whether the property can be used in the builder.
     * 
     * @return true if the property can be used in the builder
     */
    public boolean isBuildable() {
        return this == READ_WRITE || this == WRITE_ONLY || this == IMMUTABLE;
    }

    /**
     * Checks whether the property is derived.
     * 
     * @return true if the property is derived
     */
    public boolean isDerived() {
        return this == DERIVED;
    }

    /**
     * Checks whether the property can be serialized in a round-trip.
     * 
     * @return true if the property is serializable
     */
    public boolean isSerializable() {
        return this == READ_WRITE || this == IMMUTABLE;
    }

}
