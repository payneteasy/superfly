package com.payneteasy.superfly.api.serialization;

import com.payneteasy.superfly.api.exceptions.SsoAuthException;
import com.payneteasy.superfly.api.exceptions.UserExistsException;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExceptionWrapperTest {

    @Test
    public void testCreateFromException() {
        // Create a test exception
        UserExistsException exception = new UserExistsException("User already exists");

        // Создаем обертку из исключения
        ExceptionWrapper wrapper = ExceptionWrapper.from(exception);

        // Проверяем, что поля обертки заполнены корректно
        assertEquals(UserExistsException.class.getName(), wrapper.getExceptionClass());
        assertEquals("User already exists", wrapper.getMessage());
        assertTrue(wrapper.getDetailMessage().contains("User already exists"));
    }

    @Test
    public void testCreateFromNestedExceptions() {
        // Создаем вложенное исключение
        Exception cause = new Exception("Original cause");
        SsoAuthException exception = new SsoAuthException("Authentication failed", cause);

        // Create a wrapper from the exception
        ExceptionWrapper wrapper = ExceptionWrapper.from(exception);

        // Check that the wrapper fields are filled correctly
        assertEquals(SsoAuthException.class.getName(), wrapper.getExceptionClass());
        assertEquals("Authentication failed", wrapper.getMessage());
        assertTrue(wrapper.getDetailMessage().contains("Authentication failed"));
    }

    @Test
    public void testNoArgsConstructor() {
        // Check the constructor without arguments
        ExceptionWrapper wrapper = new ExceptionWrapper();
        assertNull(wrapper.getExceptionClass());
        assertNull(wrapper.getMessage());
        assertNull(wrapper.getDetailMessage());

        // Set values through setters
        wrapper.setExceptionClass("test.Exception");
        wrapper.setMessage("Test message");
        wrapper.setDetailMessage("Detailed message");

        // Check that values are set
        assertEquals("test.Exception", wrapper.getExceptionClass());
        assertEquals("Test message", wrapper.getMessage());
        assertEquals("Detailed message", wrapper.getDetailMessage());
    }

    @Test
    public void testAllArgsConstructor() {
        // Check the constructor with all arguments
        ExceptionWrapper wrapper = new ExceptionWrapper(
            "test.Exception",
            "Test message",
            "Detailed message"
        );

        assertEquals("test.Exception", wrapper.getExceptionClass());
        assertEquals("Test message", wrapper.getMessage());
        assertEquals("Detailed message", wrapper.getDetailMessage());
    }

    @Test
    public void testEqualsAndHashCode() {
        // Create two identical objects
        ExceptionWrapper wrapper1 = new ExceptionWrapper(
            "test.Exception",
            "Test message",
            "Detailed message"
        );

        ExceptionWrapper wrapper2 = new ExceptionWrapper(
            "test.Exception",
            "Test message",
            "Detailed message"
        );

        // Check equals and hashCode
        assertEquals(wrapper1, wrapper2);
        assertEquals(wrapper1.hashCode(), wrapper2.hashCode());

        // Check with other values
        ExceptionWrapper wrapper3 = new ExceptionWrapper(
            "another.Exception",
            "Test message",
            "Detailed message"
        );

        assertNotEquals(wrapper1, wrapper3);
    }
}
