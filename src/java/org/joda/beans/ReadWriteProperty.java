/*
 *  Copyright 2001-2007 Stephen Colebourne
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
 * An enumeration of read-write property states.
 * 
 * @author Stephen Colebourne
 */
public enum ReadWriteProperty {

    /**
     * The property can be read and written.
     */
    READ_WRITE(),

    /**
     * The property is read-only.
     */
    READ_ONLY() {
        @Override
        public boolean isWritable() {
            return false;
        }
    },

    /**
     * The property is write-only.
     */
    WRITE_ONLY() {
        @Override
        public boolean isReadable() {
            return false;
        }
    };

    //-----------------------------------------------------------------------
    /**
     * Gets whether the property is readable.
     * 
     * @return true if the property can be read
     */
    public boolean isReadable() {
        return true;
    }

    /**
     * Gets whether the property is writable.
     * 
     * @return true if the property can be written
     */
    public boolean isWritable() {
        return true;
    }

}
