package com.payneteasy.superfly.web.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.Authentication;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

public class SubsystemAuthenticationConverterTest {

    private SubsystemAuthenticationConverter converter;

    @Before
    public void setUp() {
        converter = new SubsystemAuthenticationConverter();
    }

    @Test
    public void testNullNameHeaderReturnsNull() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getHeader("X-Subsystem-Name")).andReturn(null);
        expect(request.getHeader("X-Subsystem-Token")).andReturn("token");
        replay(request);

        assertNull(converter.convert(request));

        verify(request);
    }

    @Test
    public void testNullTokenHeaderReturnsNull() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getHeader("X-Subsystem-Name")).andReturn("subsystem");
        expect(request.getHeader("X-Subsystem-Token")).andReturn(null);
        replay(request);

        assertNull(converter.convert(request));

        verify(request);
    }

    @Test
    public void testBothHeadersMissingReturnsNull() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getHeader("X-Subsystem-Name")).andReturn(null);
        expect(request.getHeader("X-Subsystem-Token")).andReturn(null);
        replay(request);

        assertNull(converter.convert(request));

        verify(request);
    }

    @Test
    public void testBothHeadersPresentReturnsUnauthenticatedToken() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getHeader("X-Subsystem-Name")).andReturn("my-subsystem");
        expect(request.getHeader("X-Subsystem-Token")).andReturn("my-token");
        replay(request);

        Authentication auth = converter.convert(request);

        verify(request);
        assertNotNull(auth);
        assertFalse("Token from Converter must be unauthenticated", auth.isAuthenticated());
        assertEquals("my-subsystem", auth.getPrincipal());
        assertEquals("my-token", auth.getCredentials());
    }
}
