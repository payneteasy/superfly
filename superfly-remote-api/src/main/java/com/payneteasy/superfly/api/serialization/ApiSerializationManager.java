package com.payneteasy.superfly.api.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * API serialization manager that selects an appropriate serializer
 * based on the content type.
 */
public class ApiSerializationManager {

    private final List<ApiSerializer> serializers = new ArrayList<>();
    private final ApiSerializer defaultSerializer;

    /**
     * Creates a new serialization manager with JSON serializer by default
     */
    public ApiSerializationManager(ApiSerializer ...apiSerializers) {
        this.defaultSerializer = new JsonApiSerializer();
        serializers.add(defaultSerializer);
    }

    /**
     * Adds a new serializer
     *
     * @param serializer Serializer to add
     * @return this for method chaining
     */
    public ApiSerializationManager addSerializer(ApiSerializer serializer) {
        serializers.add(serializer);
        return this;
    }

    /**
     * Selects a serializer that supports the specified content type
     *
     * @param contentType Content type
     * @return Appropriate serializer or default serializer
     */
    public ApiSerializer getSerializer(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return defaultSerializer;
        }

        return serializers.stream()
                .filter(serializer -> serializer.supports(contentType))
                .findFirst()
                .orElse(defaultSerializer);
    }

    /**
     * Deserializes text into an object of the specified type
     *
     * @param text        Text to deserialize
     * @param targetType  Target type
     * @param contentType Content type
     * @return Object of the specified type
     */
    public Object deserialize(String text, Type targetType, String contentType) {
        return getSerializer(contentType).deserialize(text, targetType);
    }

    /**
     * Serializes an object to text
     *
     * @param object      Object to serialize
     * @param contentType Content type
     * @return Serialized text
     */
    public String serialize(Object object, String contentType) {
        return getSerializer(contentType).serialize(object);
    }

    /**
     * Serializes an object to text using the default serializer
     *
     * @param object Object to serialize
     * @return Serialized text
     */
    public String serialize(Object object) {
        return defaultSerializer.serialize(object);
    }

    /**
     * Returns the content type used by the default serializer
     *
     * @return Default content type
     */
    public String getDefaultContentType() {
        return defaultSerializer.getContentType();
    }
}
