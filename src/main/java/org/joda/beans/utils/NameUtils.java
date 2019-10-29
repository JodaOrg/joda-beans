/*
 *  Copyright 2019-present Stephen Colebourne
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
package org.joda.beans.utils;

import java.util.Locale;

/**
 * Utility methods for working with names of things.
 */
public final class NameUtils {

  private NameUtils() {

  }

  /**
   * Capitalize (i.e. upper-case) the first character of a string.
   *
   * @param string  the string
   * @return the string with the first character capitalized
   */
  public static String capitalize(String string) {
    return string.substring(0, 1).toUpperCase(Locale.ENGLISH) + string.substring(1);
  }

  /**
   * Lower-case the first character of a string.
   *
   * @param string  the string
   * @return the string with the first character lower-cased
   */
  public static String decapitalize(String string) {
    return string.substring(0, 1).toLowerCase(Locale.ENGLISH) + string.substring(1);
  }
}
