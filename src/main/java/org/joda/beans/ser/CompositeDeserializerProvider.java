package org.joda.beans.ser;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link DeserializerProvider} which combines multiple providers.
 * <p>
 * The providers are queried in order and the first non-null deserializer is returned.
 */
public final class CompositeDeserializerProvider implements DeserializerProvider {

    private final List<DeserializerProvider> deserializers;

    /**
     * Creates an instance
     *
     * @param deserializers  the deserializers
     */
    public CompositeDeserializerProvider(List<DeserializerProvider> deserializers) {
        this.deserializers = new ArrayList<DeserializerProvider>(deserializers);
    }

    @Override
    public SerDeserializer findDeserializer(Class<?> type) {
        for (DeserializerProvider provider : deserializers) {
            SerDeserializer deserializer = provider.findDeserializer(type);

            if (deserializer != null) {
                return deserializer;
            }
        }
        return null;
    }
}
