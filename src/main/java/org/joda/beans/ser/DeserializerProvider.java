package org.joda.beans.ser;

/**
 * A provider of {@link SerDeserializer} instances for serializing and deserializing beans.
 * <p>
 * Implementations of this interface can introspect the bean type when choosing a deserializer.
 * This allows deserializers to be provided that can handle multiple bean types, for example all beans
 * in a particular package, any bean with a particular supertype or with a particular annotation.
 */
public interface DeserializerProvider {

    /**
     * Returns a deserializer for the type or null if there is no deserializer available.
     *
     * @param type  the type for which a deserializer is required
     * @return a deserializer for the type or null if there is no deserializer available
     */
    SerDeserializer findDeserializer(Class<?> type);
}
