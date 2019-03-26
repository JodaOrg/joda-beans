/*
 * Copyright (C) 2019 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package org.joda.beans.ser.bin;

import org.joda.beans.MetaProperty;

import java.util.HashMap;
import java.util.Map;

public final class SerializedType {
    private final int reference;

    /**
     * The first incidence of each unique meta-property's serialization.
     */
    private final Map<MetaProperty<?>, Integer> metaProperties = new HashMap<>();

    public SerializedType(int reference) {
        this.reference = reference;
    }

    public void addMetaProperty(MetaProperty<?> metaProperty) {
        metaProperties.put(metaProperty, metaProperties.size());
    }

    public boolean hasMetaProperty(MetaProperty<?> metaProperty) {
        return metaProperties.containsKey(metaProperty);
    }

    public Integer getMetaProperty(MetaProperty<?> metaProperty) {
        return metaProperties.get(metaProperty);
    }

    /**
     * The first incidence of serialization in the output.
     */
    public int getReference() {
        return reference;
    }
}
