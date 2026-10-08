package com.payneteasy.superfly.api.serialization;

import com.payneteasy.superfly.api.exceptions.SsoServerException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ServerExceptionSerializationTest {

    @Test
    public void serverExceptionIsRegisteredAndRecreatedWithItsType() {
        assertTrue(ExceptionSerializationHelper.isRegistered(SsoServerException.class));

        Throwable t = ExceptionSerializationHelper.createException(
                new ExceptionWrapper(SsoServerException.class.getName(), "Internal server error, errorId: 1", null));

        assertEquals(SsoServerException.class, t.getClass());
        assertEquals("Internal server error, errorId: 1", t.getMessage());
    }
}
