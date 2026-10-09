package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.client.SSOLoginState;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

public class SuperflySSOAuthenticationProcessingFilterStateTest {

    private static final String STATE = "test-state-value-0123456789";

    private SuperflySSOAuthenticationProcessingFilter filter;
    private AuthenticationManager authenticationManager;
    private Map<String, Object> sessionAttributes;

    @Before
    public void setUp() {
        authenticationManager = createStrictMock(AuthenticationManager.class);
        filter = new SuperflySSOAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        sessionAttributes = new HashMap<>();
    }

    @Test
    public void testTokenWithoutStateIsRejected() {
        sessionAttributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        assertRejected(request(null, session()));
    }

    @Test
    public void testTokenWithForeignStateIsRejected() {
        sessionAttributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        assertRejected(request("foreign-state-value-0123456789", session()));
    }

    @Test
    public void testTokenWithoutStateInSessionIsRejected() {
        assertRejected(request(STATE, session()));
    }

    @Test
    public void testTokenWithoutSessionIsRejected() {
        assertRejected(request(STATE, null));
    }

    @Test
    public void testMatchingStateIsAuthenticatedAndConsumed() {
        sessionAttributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        Authentication result = createMock(Authentication.class);
        expect(authenticationManager.authenticate(anyObject(CompoundAuthentication.class))).andReturn(result).once();
        replay(authenticationManager, result);

        assertSame(result, filter.attemptAuthentication(request(STATE, session()), createMock(HttpServletResponse.class)));

        verify(authenticationManager);
        assertFalse("state must be one-time", sessionAttributes.containsKey(SSOLoginState.SESSION_ATTRIBUTE));
    }

    @Test
    public void testSameStateCannotBeUsedTwice() {
        sessionAttributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        HttpSession session = session();
        Authentication result = createMock(Authentication.class);
        expect(authenticationManager.authenticate(anyObject(CompoundAuthentication.class))).andReturn(result).once();
        replay(authenticationManager, result);

        filter.attemptAuthentication(request(STATE, session), createMock(HttpServletResponse.class));
        try {
            filter.attemptAuthentication(request(STATE, session), createMock(HttpServletResponse.class));
            fail("a used state must be rejected");
        } catch (AuthenticationException expected) {
            // the manager must have been called only once
        }

        verify(authenticationManager);
    }

    private void assertRejected(HttpServletRequest request) {
        replay(authenticationManager);
        try {
            filter.attemptAuthentication(request, createMock(HttpServletResponse.class));
            fail("expected an AuthenticationException");
        } catch (AuthenticationException expected) {
            // the token must not reach the authentication manager
        }
        verify(authenticationManager);
        assertFalse("state must be one-time", sessionAttributes.containsKey(SSOLoginState.SESSION_ATTRIBUTE));
    }

    private HttpServletRequest request(String state, HttpSession session) {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getParameter("subsystemToken")).andReturn("abcdef").anyTimes();
        expect(request.getParameter("targetUrl")).andReturn("/target").anyTimes();
        expect(request.getParameter("state")).andReturn(state).anyTimes();
        expect(request.getSession(false)).andReturn(session).anyTimes();
        replay(request);
        return request;
    }

    private HttpSession session() {
        HttpSession session = createNiceMock(HttpSession.class);
        Capture<String> name = Capture.newInstance();
        expect(session.getAttribute(capture(name))).andAnswer(() -> sessionAttributes.get(name.getValue())).anyTimes();
        Capture<String> removed = Capture.newInstance();
        session.removeAttribute(capture(removed));
        expectLastCall().andAnswer(() -> {
            sessionAttributes.remove(removed.getValue());
            return null;
        }).anyTimes();
        replay(session);
        return session;
    }
}
