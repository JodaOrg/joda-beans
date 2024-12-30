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

import static java.util.stream.Collectors.toList;
import static org.joda.beans.JodaBeanUtils.notNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A multi-stage property path.
 * <p>
 * This accepts a dot-separated path and queries the bean.
 * Each dot-separated part of the path is resolved to a meta-property.
 * Thus the path "foo.bar.baz" is equivalent to {@code bean.getFoo().getBar().getBaz()}.
 * The path lookup works even if the methods are not public.
 * <p>
 * Each part of the path may contain a suffix, such as {@code [<iterableIndex>]} or {@code [<mapKey>]}.
 * The suffix {@code [<iterableIndex>]} accesses the specified numeric index of an {@code Iterable}.
 * The suffix {@code [<mapKey>]} accesses the specified numeric index of an {@code Map}.
 * 
 * @param <P>  the type of the result
 * @since 2.11.0
 */
public final class PropertyPath<P> {

    /**
     * The path entries.
     */
    private final String propertyPath;
    /**
     * The result type.
     */
    private final Class<P> resultType;
    /**
     * The path entries.
     */
    private final List<PathEntry> pathEntries;

    /**
     * Restricted constructor.
     */
    private PropertyPath(String propertyPath, Class<P> resultType, List<PathEntry> pathEntries) {
        this.propertyPath = propertyPath;
        this.resultType = resultType;
        this.pathEntries = pathEntries;
    }

    //-------------------------------------------------------------------------
    /**
     * Obtains an instance from the path.
     * 
     * @param <P>  the type of the result
     * @param propertyPath  the path, not null
     * @param resultType  the result type, not null
     * @return the path
     * @throws IllegalArgumentException if the path has an invalid format
     */
    public static <P> PropertyPath<P> of(String propertyPath, Class<P> resultType) {
        notNull(propertyPath, "propertyPath");
        notNull(resultType, "resultType");
        var split = PathEntry.parse(propertyPath);
        return new PropertyPath<>(propertyPath, resultType, split);
    }

    //-------------------------------------------------------------------------
    /**
     * Gets a value by path from the specified  bean.
     * <p>
     * This uses the path to query the bean.
     * There is special handling for {@code Iterable}, {@code Map} and {@code Optional}.
     * If the path does not match the structure within the bean, optional empty is returned.
     * If the path finds any nulls, empty lists or empty maps, optional empty is returned.
     * 
     * @param bean  the bean to start from, not null
     * @return the value, empty if the value is null or the path fails to evaluate correctly
     */
    public Optional<P> get(Bean bean) {
        notNull(bean, "bean");
        var currentBean = bean;
        for (var i = 0; i < pathEntries.size() - 1; i++) {
            var pathEntry = pathEntries.get(i);
            var obj = pathEntry.get(currentBean);
            obj = pathEntry.extract(obj);
            if (obj instanceof Optional<?> optional) {
                obj = optional.orElse(null);
            }
            if (!(obj instanceof Bean newBean)) {
                return Optional.empty();
            }
            currentBean = newBean;
        }
        // last entry, which allows for possibility that resultType = Optional.class
        var pathEntry = pathEntries.getLast();
        var obj = pathEntry.get(currentBean);
        obj = pathEntry.extract(obj);
        if (obj == null) {
            return Optional.empty();
        }
        if (resultType.isInstance(obj)) {
            return Optional.of(resultType.cast(obj));
        } else {
            if (obj instanceof Optional<?> opt) {
                obj = opt.orElse(null);
            }
            if (resultType.isInstance(obj)) {
                return Optional.of(resultType.cast(obj));
            }
            return Optional.empty();
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Gets the property path.
     * 
     * @return the property path
     */
    public String propertyPath() {
        return propertyPath;
    }

    /**
     * Gets the result type.
     * 
     * @return the result type
     */
    public Class<P> resultType() {
        return resultType;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof PropertyPath<?> other &&
                this.propertyPath.equals(other.propertyPath) &&
                this.resultType.equals(other.resultType);
    }

    @Override
    public int hashCode() {
        return propertyPath.hashCode() ^ resultType.hashCode();
    }

    @Override
    public String toString() {
        return propertyPath + ": " + resultType.getName();
    }

    //-------------------------------------------------------------------------
    private static final class PathEntry {
        private final String propertyName;
        private final String key;
        private final int index;

        static List<PathEntry> parse(String propertyPath) {
            var split = propertyPath.split("\\.");
            return Stream.of(split)
                    .map(entryStr -> extractEntry(propertyPath, entryStr))
                    .collect(toList());
        }

        private static PathEntry extractEntry(String propertyPath, String entryStr) {
            var propName = entryStr;
            var key = (String) null;
            var index = 0;
            var start = entryStr.lastIndexOf('[');
            if (entryStr.endsWith("]") && start > 0) {
                key = entryStr.substring(start + 1, entryStr.length() - 1);
                if (key.isEmpty()) {
                    throw new IllegalArgumentException("Invalid property path, empty key: " + propertyPath);
                }
                var firstChar = key.charAt(0);
                index = -1;
                if (firstChar == '-' || (firstChar >= '0' && firstChar <= '9')) {
                    try {
                        index = Integer.parseInt(key);
                    } catch (NumberFormatException ex) {
                        // index = -1
                    }
                }
                propName = entryStr.substring(0, start);
            }
            return new PathEntry(propName, key, index);
        }

        private PathEntry(String propertyName, String key, int index) {
            this.propertyName = propertyName;
            this.key = key;
            this.index = index;
        }

        private Object get(Bean bean) {
            try {
                return bean.metaBean().metaProperty(propertyName).get(bean);
            } catch (RuntimeException ex) {
                return null;
            }
        }

        private Object extract(Object obj) {
            // maps can be queried using the [key] suffix if desired
            // an [index] suffix will be queried as a key, not an index
            if (obj instanceof Map<?, ?> map) {
                if (key == null) {
                    return extract(map.values());
                } else {
                    return map.get(key);
                }
            }

            // lists/sets can be queried using the [index] suffix if desired
            if (obj instanceof Iterable<?> itr) {
                if (key != null && index < 0) {
                    return null;
                }
                if (obj instanceof List<?> list) {
                    if (index < list.size()) {
                        return list.get(index);
                    }
                    return null;
                }
                var it = itr.iterator();
                var i = 0;
                while (it.hasNext() && i < index) {
                    it.next();
                    i++;
                }
                return it.hasNext() ? it.next() : null;
            }

            // not a collection
            if (key != null && !"0".equals(key)) {
                return null;
            }
            return obj;
        }
    }

}
