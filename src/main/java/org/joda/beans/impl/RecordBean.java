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
package org.joda.beans.impl;

import java.lang.invoke.MethodHandles;

import org.joda.beans.ImmutableBean;
import org.joda.beans.JodaBeanUtils;
import org.joda.beans.MetaBean;
import org.joda.beans.TypedMetaBean;

/**
 * A bean that is implemented using the record language feature.
 * <p>
 * Simply add {@code implements RecordBean<MyRecord>} to the record to turn it into a bean.
 * There is no need to add annotations. Derived properties are not supported.
 * <p>
 * For public records, this is the approach to use:.
 * {@snippet lang="java":
 *  public static record StringIntPair(String first, int second) implements RecordBean<StrngIntPair> {
 *  }
 * }
 * <p>
 * For non-public records, this is the approach to use:
 * {@snippet lang="java":
 *  private static record StringLongPair(String first, long second) implements RecordBean<StringLongPair> {
 *    static {
 *      RecordBean.register(StringLongPair.class, MethodHandles.lookup());
 *    }
 *  }
 * }
 * <p>
 * Note that a public record within a module that doesn't export the record will need to adopt the
 * non-public approach.
 * 
 * @param <T> the record bean type
 * @since 3.0.0
 */
public interface RecordBean<T extends RecordBean<T>> extends ImmutableBean {

    /**
     * Registers a meta-bean for the specified record.
     * <p>
     * See the class-level Javadoc to understand when this method should be used.
     * <p>
     * Note that this method must only be called once for each class, and never concurrently.
     * If you follow one of the two patterns in the class-level Javadoc everything will be fine.
     * 
     * @param <T>  the type of the record
     * @param recordClass  the record class, not null
     * @param lookup  the lookup object, granting permission to non-accessible methods
     * @return the meta-bean
     * @throws RuntimeException if unable to register the record
     */
    public static <T extends Record & ImmutableBean> MetaBean register(Class<T> recordClass, MethodHandles.Lookup lookup) {
        JodaBeanUtils.notNull(recordClass, "recordClass");
        JodaBeanUtils.notNull(lookup, "lookup");
        validateRecordClass(recordClass);
        var metaBean = new RecordMetaBean<>(recordClass, lookup);
        MetaBean.register(metaBean);
        return metaBean;
    }

    // Class could be erased, thus we double-check it
    private static <T extends Record & ImmutableBean> void validateRecordClass(Class<T> recordClass) {
        if (!recordClass.isRecord()) {
            throw new IllegalArgumentException(
                    "RecordBean can only be used with records: " + recordClass.getName());
        }
        if (!ImmutableBean.class.isAssignableFrom(recordClass)) {
            throw new IllegalArgumentException(
                    "RecordBean can only be used with classes that implement ImmutableBean: " + recordClass.getName());
        }
    }

    //-------------------------------------------------------------------------
    @Override
    @SuppressWarnings("unchecked")
    public default TypedMetaBean<T> metaBean() {
        return (TypedMetaBean<T>) MetaBean.of(getClass());
    }

}
