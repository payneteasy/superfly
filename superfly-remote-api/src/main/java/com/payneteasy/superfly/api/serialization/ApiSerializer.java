package com.payneteasy.superfly.api.serialization;

import java.lang.reflect.Type;

/**
 * Serializer interface for API.
 * Provides methods for serializing and deserializing objects.
 */
public interface ApiSerializer {

    /**
     * Supported content formats
     */
    String CONTENT_TYPE_JSON = "application/json";
    String CONTENT_TYPE_XML = "application/xml";

    /**
     * Checks if the serializer supports the specified content type
     *
     * @param contentType MIME content type
     * @return true if supported, otherwise false
     */
    boolean supports(String contentType);

    /**
     * Deserializes text into an object of the specified type
     *
     * @param text        Text to deserialize
     * @param targetType  Target type
     * @return Object of the specified type
     */
    Object deserialize(String text, Type targetType);

    /**
     * Serializes an object into text
     *
     * @param object Object to serialize
     * @return Serialized text
     */
    String serialize(Object object);

    /**
     * Returns the MIME content type that this serializer processes
     *
     * @return MIME content type
     */
    String getContentType();
}
