package com.payneteasy.superfly.web.security;

import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserInfoServiceAcegiRemoteAddressTest {

    private final UserInfoServiceAcegi service = new UserInfoServiceAcegi();

    @After
    public void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    public void remoteAddressComesFromCurrentRequest() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        assertEquals("203.0.113.9", service.getRemoteAddress());
    }

    @Test
    public void noRemoteAddressOutsideOfRequest() {
        assertNull(service.getRemoteAddress());
    }
}
