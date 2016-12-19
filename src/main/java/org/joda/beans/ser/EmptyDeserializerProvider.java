package org.joda.beans.ser;

/**
 * A deserializer provider which never returns a deserializer.
 */
public class EmptyDeserializerProvider implements DeserializerProvider {

    /**
     * The single shared instance of this class.
     */
    public static final DeserializerProvider INSTANCE = new EmptyDeserializerProvider();

    private EmptyDeserializerProvider() {
    }

    /**
     * Returns null.
     *
     * @param type  not used
     * @return null
     */
    @Override
    public SerDeserializer findDeserializer(Class<?> type) {
        return null;
    }
}
