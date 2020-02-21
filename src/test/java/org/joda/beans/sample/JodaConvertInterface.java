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

import org.joda.convert.FromString;
import org.joda.convert.ToString;

/**
 * Mock JodaConvert interface, used for testing.
 */
public interface JodaConvertInterface extends IKey {

    @FromString
    public static JodaConvertInterface of(String uniqueName) {
        return uniqueName.equalsIgnoreCase(First.INSTANCE.toString()) ?
            First.INSTANCE :
            Second.INSTANCE;
    }

    @ToString
    @Override
    public abstract String toString();
    
    @Override
    public default String getName() {
        return toString();
    }

    final class First implements JodaConvertInterface {

        static final First INSTANCE = new First();

        @Override
        public String toString() {
            return "First";
        }
    }

    final class Second implements JodaConvertInterface {

        static final Second INSTANCE = new Second();

        @Override
        public String toString() {
            return "Second";
        }
    }
}
