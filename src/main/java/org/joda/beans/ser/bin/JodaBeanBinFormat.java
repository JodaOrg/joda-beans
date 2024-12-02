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
package org.joda.beans.ser.bin;

/**
 * Provides control over the binary format.
 * 
 * @since 3.0.0
 */
public enum JodaBeanBinFormat {

    /**
     * The Standard format, version 1
     * <p>
     * The binary format is based on MessagePack v2.0.
     * Each bean is output as a map using the property name.
     * <p>
     * Most simple types, defined by Joda-Convert, are output as MessagePack strings.
     * However, MessagePack nil, boolean, float, integral and bin types are also used
     * for null, byte[] and the Java numeric primitive types (excluding char).
     * <p>
     * Beans are output using MessagePack maps where the key is the property name.
     * Collections are output using MessagePack maps or arrays.
     * Multisets are output as a map of value to count.
     * <p>
     * If a collection contains a collection then addition meta-type information is
     * written to aid with deserialization.
     * At this level, the data read back may not be identical to that written.
     * <p>
     * Where necessary, the Java type is sent using an 'ext' entity.
     * Three 'ext' types are used, one each for beans, meta-type and simple.
     * The class name is passed as the 'ext' data.
     * The 'ext' value is sent as an additional key-value pair for beans, with the
     * 'ext' as the key and 'nil' as the value. Where the additional type information
     * is not about a bean, a tuple is written using a size 1 map where the key is the
     * 'ext' data and the value is the data being annotated.
     * <p>
     * Type names are shortened by the package of the root type if possible.
     * Certain basic types are also handled, such as String, Integer, File and URI.
     */
    STANDARD(1),
    /**
     * The Referencing format, version 2
     * <p>
     * The referencing format is based on the standard format.
     * As a more complex format, it is intended to be consumed only by Joda-Beans
     * (whereas the standard format could be consumed by any consumer using MsgPack).
     * Thus this format is not fully documented and may change over time.
     * <p>
     * The referencing format only supports serialization of instances of {@code ImmutableBean}
     * and other basic types. If any mutable beans are encountered during traversal an exception will be thrown.
     * <p>
     * An initial pass of the bean is used to build up a map of unique immutable beans
     * and unique immutable instances of other classes (based on an equality check).
     * Then the class and property names for each bean class is serialized up front as a map of class name to list of
     * property names, along with class information for any class where type information would be required when parsing
     * and is not available on the metabean for the enclosing bean object.
     * <p>
     * Each unique immutable bean is output as a list of each property value using the fixed
     * property order previously serialized. Subsequent instances of unique objects (defined by an
     * equality check) are replaced by references to the first serialized instance.
     * <p>
     * The Java type names are sent using an 'ext' entity.
     * Five 'ext' types are used, one each for beans, meta-type and simple, reference keys and reference lookups.
     * The class name is passed as the 'ext' data.
     * The 'ext' value is sent as the first item in an array of property values for beans, an integer referring to the
     * location in the initial class mapping.
     * Where the additional type information is not about a bean, a tuple is written using a size 1 map where the key is
     * the 'ext' data and the value is the data being annotated.
     * <p>
     * For references, when an object will be referred back to it is written as a map of size one with 'ext' as the key
     * and the object that should be referred to as the value.
     * When that same object is referred back to it is written as 'ext' with the data from the initial 'ext'.
     */
    REFERENCING(2);

    /**
     * The format version.
     */
    private final int version;

    private JodaBeanBinFormat(int version) {
        this.version = version;
    }

    /**
     * Returns the version number used to identify the format in the file.
     * 
     * @return the version
     */
    public int version() {
        return version;
    }

}
