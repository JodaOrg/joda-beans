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
package org.joda.beans.gen;

/**
 * A generator of collection code.
 */
abstract class CollectionGen {

  /**
   * Generates code to add an element to a collection.
   *
   * @param existing  the name of an existing collection or an expression evaluating to an instance of a collection, not null
   * @param collectionTypeParams  the type params of the collection, may be empty
   * @param value  the name of the value to add to the collection or an expression evaluating to an instance of a collection, not null
   * @return the generated code, not null
   */
  abstract String generateAddToCollection(String existing, String collectionTypeParams, String value);

  /**
   * Generates code to add a new key/value pair to a map.
   *
   * @param existing  the name of an existing map or an expression evaluating to an instance of a map, not null
   * @param mapTypeParams  the type params of the map, may be empty
   * @param key  the name of the key to add to the map or an expression evaluating to an instance of the key type, not null
   * @param value  the name of the value to add to the map or an expression evaluating to an instance of the value type, not null
   * @return the generated code, not null
   */
  abstract String generateAddToMap(String existing, String mapTypeParams, String key, String value);

  static class PatternCollectionGen extends CollectionGen {
    private final String pattern;

    PatternCollectionGen(String pattern) {
      this.pattern = pattern;
    }

    @Override
    String generateAddToCollection(String existing, String collectionTypeParams, String value) {
      return generateAddToMap(existing, collectionTypeParams, "", value);
    }

    @Override
    String generateAddToMap(String existing, String mapTypeParams, String key, String value) {
      return pattern.replaceAll("\\$existing", existing).replaceAll("\\$params", mapTypeParams).replaceAll("\\$value", value).replaceAll("\\$key", key);
    }
  }
}