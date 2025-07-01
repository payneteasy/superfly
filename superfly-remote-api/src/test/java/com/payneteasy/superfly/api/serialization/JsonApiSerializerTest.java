package com.payneteasy.superfly.api.serialization;

import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonApiSerializerTest {

    private JsonApiSerializer serializer;

    @Before
    public void setUp() {
        serializer = new JsonApiSerializer();
    }

    @Test
    public void testSupportsJsonContentType() {
        assertTrue(serializer.supports("application/json"));
        assertTrue(serializer.supports("application/JSON"));
        assertTrue(serializer.supports("text/json"));
        assertFalse(serializer.supports("application/xml"));
        assertFalse(serializer.supports(null));
    }

    @Test
    public void testGetContentType() {
        assertEquals(ApiSerializer.CONTENT_TYPE_JSON, serializer.getContentType());
    }

    @Test
    public void testSerializeSimpleObject() {
        TestObject obj = new TestObject("test", 123);
        String json = serializer.serialize(obj);
        assertEquals("{\"name\":\"test\",\"value\":123}", json);
    }

    @Test
    public void testDeserializeSimpleObject() {
        String json = "{\"name\":\"test\",\"value\":123}";
        TestObject obj = (TestObject) serializer.deserialize(json, TestObject.class);
        assertEquals("test", obj.name);
        assertEquals(123, obj.value);
    }

    @Test
    public void testDeserializeEmptyString() {
        assertNull(serializer.deserialize("", TestObject.class));
        assertNull(serializer.deserialize(null, TestObject.class));
    }

    @Test
    public void testDeserializeToVoid() {
        assertNull(serializer.deserialize("anything", Void.TYPE));
    }

    @Test
    public void testDeserializeGenericTypes() {
        String json = "[{\"name\":\"test1\",\"value\":1},{\"name\":\"test2\",\"value\":2}]";
        Type listType = new TypeToken<List<TestObject>>(){}.getType();
        List<TestObject> list = (List<TestObject>) serializer.deserialize(json, listType);

        assertEquals(2, list.size());
        assertEquals("test1", list.get(0).name);
        assertEquals(1, list.get(0).value);
        assertEquals("test2", list.get(1).name);
        assertEquals(2, list.get(1).value);
    }

    @Test
    public void testDeserializeMap() {
        String json = "{\"key1\":\"value1\",\"key2\":\"value2\"}";
        Type mapType = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = (Map<String, String>) serializer.deserialize(json, mapType);

        assertEquals(2, map.size());
        assertEquals("value1", map.get("key1"));
        assertEquals("value2", map.get("key2"));
    }

    // Tests for primitive types
    @Test
    public void testDeserializeBooleanTrue() {
        // Testing string representation of boolean true
        Boolean result1 = (Boolean) serializer.deserialize("true", Boolean.class);
        assertTrue(result1);

        boolean result2 = (boolean) serializer.deserialize("true", Boolean.TYPE);
        assertTrue(result2);

        // Case sensitivity check
        Boolean result3 = (Boolean) serializer.deserialize("TRUE", Boolean.class);
        assertTrue(result3);
    }

    @Test
    public void testDeserializeBooleanFalse() {
        Boolean result1 = (Boolean) serializer.deserialize("false", Boolean.class);
        assertFalse(result1);

        boolean result2 = (boolean) serializer.deserialize("false", Boolean.TYPE);
        assertFalse(result2);
    }

    @Test
    public void testDeserializeInteger() {
        Integer result1 = (Integer) serializer.deserialize("42", Integer.class);
        assertEquals(Integer.valueOf(42), result1);

        int result2 = (int) serializer.deserialize("42", Integer.TYPE);
        assertEquals(42, result2);
    }

    @Test
    public void testDeserializeLong() {
        Long result1 = (Long) serializer.deserialize("9223372036854775807", Long.class);
        assertEquals(Long.valueOf(Long.MAX_VALUE), result1);

        long result2 = (long) serializer.deserialize("9223372036854775807", Long.TYPE);
        assertEquals(Long.MAX_VALUE, result2);
    }

    @Test
    public void testDeserializeDouble() {
        Double result1 = (Double) serializer.deserialize("3.14159", Double.class);
        assertEquals(Double.valueOf(3.14159), result1, 0.00001);

        double result2 = (double) serializer.deserialize("3.14159", Double.TYPE);
        assertEquals(3.14159, result2, 0.00001);
    }

    @Test
    public void testDeserializeFloat() {
        Float result1 = (Float) serializer.deserialize("3.14", Float.class);
        assertEquals(Float.valueOf(3.14f), result1, 0.001);

        float result2 = (float) serializer.deserialize("3.14", Float.TYPE);
        assertEquals(3.14f, result2, 0.001);
    }

    @Test
    public void testDeserializeCharacter() {
        Character result1 = (Character) serializer.deserialize("A", Character.class);
        assertEquals(Character.valueOf('A'), result1);

        char result2 = (char) serializer.deserialize("A", Character.TYPE);
        assertEquals('A', result2);
    }

    @Test
    public void testDeserializeShort() {
        Short result1 = (Short) serializer.deserialize("32767", Short.class);
        assertEquals(Short.valueOf((short) 32767), result1);

        short result2 = (short) serializer.deserialize("32767", Short.TYPE);
        assertEquals((short) 32767, result2);
    }

    @Test
    public void testDeserializeByte() {
        Byte result1 = (Byte) serializer.deserialize("127", Byte.class);
        assertEquals(Byte.valueOf((byte) 127), result1);

        byte result2 = (byte) serializer.deserialize("127", Byte.TYPE);
        assertEquals((byte) 127, result2);
    }

    @Test
    public void testSerializeExceptionWrapper() {
        // Testing serialization of ExceptionWrapper
        ExceptionWrapper wrapper = new ExceptionWrapper(
                "com.payneteasy.superfly.api.exceptions.UserExistsException",
                "User already exists",
                "UserExistsException: User already exists"
        );

        String json = serializer.serialize(wrapper);

        // Checking JSON structure
        assertTrue(json.contains("\"exceptionClass\":"));
        assertTrue(json.contains("\"message\":"));
        assertTrue(json.contains("\"detailMessage\":"));
        assertTrue(json.contains("UserExistsException"));
        assertTrue(json.contains("User already exists"));
    }

    @Test
    public void testDeserializeExceptionWrapper() {
        // Testing deserialization of ExceptionWrapper
        String json = "{\"exceptionClass\":\"com.payneteasy.superfly.api.exceptions.UserExistsException\"," +
                     "\"message\":\"User already exists\"," +
                     "\"detailMessage\":\"UserExistsException: User already exists\"}";

        ExceptionWrapper wrapper = (ExceptionWrapper) serializer.deserialize(json, ExceptionWrapper.class);

        assertNotNull(wrapper);
        assertEquals("com.payneteasy.superfly.api.exceptions.UserExistsException", wrapper.getExceptionClass());
        assertEquals("User already exists", wrapper.getMessage());
        assertEquals("UserExistsException: User already exists", wrapper.getDetailMessage());
    }

    @Test
    public void testDeserializeExceptionWrapperWithMissingFields() {
        // Testing deserialization of ExceptionWrapper with missing fields
        String json = "{\"exceptionClass\":\"com.payneteasy.superfly.api.exceptions.UserExistsException\"," +
                     "\"message\":\"User already exists\"}";

        ExceptionWrapper wrapper = (ExceptionWrapper) serializer.deserialize(json, ExceptionWrapper.class);

        assertNotNull(wrapper);
        assertEquals("com.payneteasy.superfly.api.exceptions.UserExistsException", wrapper.getExceptionClass());
        assertEquals("User already exists", wrapper.getMessage());
        assertNull(wrapper.getDetailMessage());
    }

    // Helper class for testing
    static class TestObject {
        private String name;
        private int value;

        public TestObject() {
        }

        public TestObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }
}
