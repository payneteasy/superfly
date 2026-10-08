package com.payneteasy.superfly.web.controller.api;

import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class GlobalExceptionHandlerTest {

    @Test
    public void exceptionMessageIsNotReturnedToClient() {
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler()
                .handleException(new IllegalStateException("could not execute procedure ss_secret_proc"));

        assertEquals(500, response.getStatusCode().value());
        assertFalse(response.getBody().toString().contains("ss_secret_proc"));
        assertNotNull(response.getBody().get("errorId"));
    }

    @Test
    public void exceptionWithoutMessageIsHandled() {
        ResponseEntity<Map<String, String>> response = new GlobalExceptionHandler().handleException(new NullPointerException());

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody().get("errorId"));
    }
}
