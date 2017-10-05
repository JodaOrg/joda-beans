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

/**
 * Mock person interface, used for testing.
 */
public interface IPerson {

    /**
     * Gets the forename.
     * @return the value of the property
     */
    String getForename();

    /**
     * Gets the surname.
     * @return the value of the property
     */
    String getSurname();

    /**
     * Sets the surname.
     * @param surname  the new value of the property
     */
    void setSurname(String surname);

}
