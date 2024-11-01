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
package org.joda.beans;

import java.util.Collection;
import java.util.Map;

/**
 * A bean walk handler, where method are invoked to represent the content of the walked bean.
 */
public interface BeanWalkHandler {

    /**
     * Handles an object, switching based on the runtime type.
     * 
     * @param declaredType  the declared type of the object
     * @param value  the value
     */
    public default void handleObject(ResolvedType declaredType, Object value) {
        switch (value) {
            case null -> handleNull(declaredType);
            case Bean bean -> handleBean(declaredType, bean);
            case Collection<?> coll -> handleCollection(declaredType, coll);
            case Map<?, ?> map -> handleMap(declaredType, map);
            default -> handleLeaf(declaredType, value);
        }
    }

    /**
     * Handles null.
     * 
     * @param declaredType  the declared type, not null
     */
    public default void handleNull(ResolvedType declaredType) {
        // do nothing
    }

    /**
     * Handles a bean.
     * 
     * @param declaredType  the declared type, not null
     * @param bean  the bean, not null
     */
    public default void handleBean(ResolvedType declaredType, Bean bean) {
        bean.walk(this);
    }

    /**
     * Handles a collection, looping over the collection items.
     * 
     * @param declaredType  the declared type, not null
     * @param collection the collection
     */
    public default void handleCollection(ResolvedType declaredType, Collection<?> collection) {
        var itemType = declaredType.getArgument(1, 0);
        for (var item : collection) {
            handleCollectionItem(itemType, item);
        }
    }

    /**
     * Handles an item in a collection.
     * 
     * @param itemType  the declared type of the collection item
     * @param value  the collection item
     */
    public default void handleCollectionItem(ResolvedType itemType, Object value) {
        handleObject(itemType, value);
    }

    /**
     * Handles a map, looping over the map entries.
     * 
     * @param declaredType  the declared type, not null
     * @param map  the map
     */
    public default void handleMap(ResolvedType declaredType, Map<?, ?> map) {
        var keyType = declaredType.getArgument(2, 0);
        var valueType = declaredType.getArgument(2, 1);
        for (var entry : map.entrySet()) {
            handleMapEntry(keyType, entry.getKey(), valueType, entry.getValue());
        }
    }

    /**
     * Handles an entry in a map.
     * 
     * @param keyType  the declared type of the map key
     * @param key  the map key
     * @param valueType  the declared type of the map value
     * @param value  the map value
     */
    public default void handleMapEntry(ResolvedType keyType, Object key, ResolvedType valueType, Object value) {
        handleObject(keyType, key);
        handleObject(valueType, value);
    }

    /**
     * Handles a leaf object.
     * <p>
     * This method is only called when the object is not null, not a bean, not a collection and not a map.
     * 
     * @param declaredType  the declared type, not null
     * @param value  the value, not null
     */
    public default void handleLeaf(ResolvedType declaredType, Object value) {
        // do nothing
    }

    //-------------------------------------------------------------------------
    /**
     * Handles a property on a bean.
     * 
     * @param metaProperty  the meta-property
     * @param bean  the current bean
     * @param value  the property value
     */
    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, Object value) {
        var resolvedType = ResolvedType.of(metaProperty.propertyGenericType(), bean.getClass());
        handleObject(resolvedType, value);
    }

//    /**
//     * Handles a property where the property is a {@code String}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, String value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a primitive {@code long}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, long value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a primitive {@code int}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, int value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a primitive {@code double}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, double value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a primitive {@code float}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, float value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a primitive {@code boolean}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param value  the property value
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, boolean value) {
//        handleProperty(metaProperty, bean, value);
//    }
//
//    /**
//     * Handles a property where the property is a {@code Collection}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param collection  the collection
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, Collection<?> collection) {
//        handleCollection(metaProperty.propertyGenericType(), collection);
//    }
//
//    /**
//     * Handles a property where the property is a {@code Collection}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param itemType the declared type of the collection item
//     * @param collection the collection
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, Type itemType, Collection<?> collection) {
//        handleCollection(itemType, collection);
//    }
//
//    /**
//     * Handles a property where the property is a {@code Map}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean  the current bean
//     * @param map  the map
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, Map<?, ?> map) {
//        var keyType = JodaBeanUtils.mapKeyType(metaProperty, bean.getClass());
//        var valueType = JodaBeanUtils.mapValueType(metaProperty, bean.getClass());
//        handleProperty(metaProperty, bean, keyType, valueType, map);
//    }
//
//    /**
//     * Handles a property where the property is a {@code Map}.
//     * 
//     * @param metaProperty  the meta-property
//     * @param bean   the current bean
//     * @param keyType  the declared type of the map key
//     * @param valueType  the declared type of the map value
//     * @param map  the map
//     */
//    public default void handleProperty(MetaProperty<?> metaProperty, Bean bean, Class<?> keyType, Class<?> valueType, Map<?, ?> map) {
//        for (var entry : map.entrySet()) {
//            handleMapEntry(keyType, entry.getKey(), valueType, entry.getValue());
//        }
//    }

}
