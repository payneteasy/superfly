package com.payneteasy.superfly.web.security.handler;

import com.payneteasy.superfly.web.security.exception.SubsystemNotAllowedHostException;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonAuthenticationFailureHandlerTest {

    @Test
    public void responseDoesNotEchoExceptionMessage() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        new JsonAuthenticationFailureHandler().onAuthenticationFailure(
                new MockHttpServletRequest("POST", "/remoting/sso.service/authenticate"),
                response,
                new SubsystemNotAllowedHostException("Subsystem billing (from CN=billing) not found in subsystems"));

        assertEquals(401, response.getStatus());
        String body = response.getContentAsString();
        assertTrue(body, body.contains("\"Authentication failed\""));
        assertFalse(body, body.contains("billing"));
    }
}
