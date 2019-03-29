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

/**
 * Joda-Beans is a small framework that adds properties to Java, greatly enhancing JavaBeans.
 * <p>
 * The key concept is to allow each property on a bean to be accessed as an object in its own right.
 * This provides the hook for other technologies to build on, such as serialization, mapping,
 * expression languages and validation.
 */
module org.joda.beans {

    // dependency on Joda-Convert
    requires transitive org.joda.convert;
    // dependency on XML parser
    requires java.xml;
    // optional dependency on Guava
    requires static com.google.common;
    // optional dependency on Joda-Collect
    requires static org.joda.collect;

    // export all packages
    exports org.joda.beans;
    exports org.joda.beans.gen;
    exports org.joda.beans.impl;
    exports org.joda.beans.impl.direct;
    exports org.joda.beans.impl.flexi;
    exports org.joda.beans.impl.light;
    exports org.joda.beans.impl.map;
    exports org.joda.beans.impl.reflection;
    exports org.joda.beans.ser;
    exports org.joda.beans.ser.bin;
    exports org.joda.beans.ser.json;
    exports org.joda.beans.ser.map;
    exports org.joda.beans.ser.xml;
    exports org.joda.beans.test;

}
