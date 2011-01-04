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
package org.joda.beans;

/**
 * An enumeration of read-write property types.
 * <p>
 * A property may be read-only, read-write or write-only.
 * This enumeration models those options.
 * 
 * @author Stephen Colebourne
 */
public enum PropertyReadWrite {

    /**
     * The property can be read and written.
     */
    READ_WRITE(),
    /**
     * The property is read-only.
     */
    READ_ONLY(),
    /**
     * The property is write-only.
     */
    WRITE_ONLY();

    //-----------------------------------------------------------------------
    /**
     * Checks whether the property is readable.
     * 
     * @return true if the property can be read
     */
    public boolean isReadable() {
        return this != WRITE_ONLY;
    }

    /**
     * Checks whether the property is writable.
     * 
     * @return true if the property can be written
     */
    public boolean isWritable() {
      return this != READ_ONLY;
    }

}
