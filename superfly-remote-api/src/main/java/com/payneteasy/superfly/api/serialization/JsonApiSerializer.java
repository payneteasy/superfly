package com.payneteasy.superfly.api.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;

import java.lang.reflect.Type;
import java.util.Map;

/**
 * JSON-based API serializer implementation (using GSON).
 */
public class JsonApiSerializer implements ApiSerializer {

    private final Gson gson;

    public JsonApiSerializer() {
        this.gson = new GsonBuilder()
                .serializeNulls()
                //.setStrictness(Strictness.STRICT)
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .registerTypeAdapter(new TypeToken<Map<SSORole, SSOAction[]>>() {}.getType(), new SSORoleActionsMapAdapter())
                .create();
    }

    @Override
    public boolean supports(String contentType) {
        return contentType != null && contentType.toLowerCase().contains("json");
    }

    @Override
    public Object deserialize(String text, Type targetType) {
        if (targetType == Void.TYPE || text == null || text.isEmpty()) {
            return null;
        }

        // Processing primitive types
        if (isPrimitiveOrWrapper(targetType)) {
            return deserializePrimitive(text, targetType);
        }

        return gson.fromJson(text, targetType);
    }

    /**
     * Checks if the type is primitive or its wrapper
     *
     * @param type type to check
     * @return true if the type is primitive or its wrapper
     */
    private boolean isPrimitiveOrWrapper(Type type) {
        return type == Boolean.class || type == Boolean.TYPE ||
               type == Integer.class || type == Integer.TYPE ||
               type == Long.class || type == Long.TYPE ||
               type == Double.class || type == Double.TYPE ||
               type == Float.class || type == Float.TYPE ||
               type == Byte.class || type == Byte.TYPE ||
               type == Short.class || type == Short.TYPE ||
               type == Character.class || type == Character.TYPE;
    }

    /**
     * Deserializes string representation of a primitive type
     *
     * @param text string representation
     * @param targetType target type
     * @return primitive type object
     */
    private Object deserializePrimitive(String text, Type targetType) {
        text = text.trim();

        if (targetType == Boolean.class || targetType == Boolean.TYPE) {
            return "true".equalsIgnoreCase(text) || "1".equals(text);
        } else if (targetType == Integer.class || targetType == Integer.TYPE) {
            return Integer.parseInt(text);
        } else if (targetType == Long.class || targetType == Long.TYPE) {
            return Long.parseLong(text);
        } else if (targetType == Double.class || targetType == Double.TYPE) {
            return Double.parseDouble(text);
        } else if (targetType == Float.class || targetType == Float.TYPE) {
            return Float.parseFloat(text);
        } else if (targetType == Byte.class || targetType == Byte.TYPE) {
            return Byte.parseByte(text);
        } else if (targetType == Short.class || targetType == Short.TYPE) {
            return Short.parseShort(text);
        } else if (targetType == Character.class || targetType == Character.TYPE) {
            return text.isEmpty() ? 0 : text.charAt(0);
        }

        // If the type is not recognized, use standard deserialization
        return gson.fromJson(text, targetType);
    }

    @Override
    public String serialize(Object object) {
        return gson.toJson(object);
    }

    @Override
    public String getContentType() {
        return CONTENT_TYPE_JSON;
    }
}
