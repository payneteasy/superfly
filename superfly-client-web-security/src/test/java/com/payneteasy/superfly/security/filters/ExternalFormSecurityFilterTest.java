package com.payneteasy.superfly.security.filters;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.client.SSOLoginState;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.easymock.Capture;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ExternalFormSecurityFilterTest {

    private static final String STATE = "test-state-value-0123456789";

    @Test
    public void testLocalTargetUrlIsKept() {
        assertEquals("/app/page?a=1", safeTarget("/app/page?a=1"));
        assertEquals("/", safeTarget("/"));
    }

    @Test
    public void testForeignTargetUrlGoesToContextRoot() {
        String[] foreign = {
                "//evil.example", "/\\evil.example", "http://evil.example", "https://evil.example/x",
                "//evil.example/%2e%2e", "/\t/evil.example", "evil.example", "", "\\\\evil.example",
                // already decoded once by the container, still encoded once more
                "/%2F%2Fevil.example", "/%5Cevil.example", "/%09/evil.example",
                // what getParameter() returns for %2F%2Fevil
                "//evil",
        };
        for (String target : foreign) {
            assertEquals("target " + target, "/app/", safeTarget(target));
        }
        assertEquals("/app/", safeTarget(null));
    }

    @Test
    public void testSessionIdChangedBeforeContextIsStored() throws Exception {
        HttpSession session = createNiceMock(HttpSession.class);
        expect(session.getAttribute(SSOLoginState.SESSION_ATTRIBUTE)).andReturn(STATE);
        replay(session);
        HttpServletRequest request = checkTokenRequest("/page", session);
        expect(request.changeSessionId()).andReturn("new-id").once();
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        response.sendRedirect("/page");
        expectLastCall().once();
        replay(request, response);

        filter().doFilter(request, response, createNiceMock(FilterChain.class));

        verify(request, response);
    }

    @Test
    public void testSessionIdNotChangedOnInvalidToken() throws Exception {
        HttpSession session = createNiceMock(HttpSession.class);
        expect(session.getAttribute(SSOLoginState.SESSION_ATTRIBUTE)).andReturn(STATE);
        replay(session);
        HttpServletRequest request = checkTokenRequest("/page", session);
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        expect(response.getWriter()).andReturn(new java.io.PrintWriter(new java.io.StringWriter())).anyTimes();
        replay(request, response);

        SSOService service = createMock(SSOService.class);
        expect(service.exchangeSubsystemToken(anyObject(ExchangeSubsystemTokenRequest.class))).andReturn(null);
        replay(service);
        new ExternalFormSecurityFilter(new ExcludedPaths(), "sys", "https://sso", "pkg", service)
                .doFilter(request, response, createNiceMock(FilterChain.class));

        verify(request);
    }

    @Test
    public void testRedirectToLoginCarriesStateKeptInSession() throws Exception {
        HttpSession session = createNiceMock(HttpSession.class);
        Capture<Object> stored = Capture.newInstance();
        session.setAttribute(eq(SSOLoginState.SESSION_ATTRIBUTE), capture(stored));
        expectLastCall().once();
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getRequestURI()).andReturn("/app/page").anyTimes();
        expect(request.getServletPath()).andReturn("/app/page").anyTimes();
        expect(request.getSession(true)).andReturn(session).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        HttpServletResponse response = createMock(HttpServletResponse.class);
        Capture<String> location = Capture.newInstance();
        response.sendRedirect(capture(location));
        expectLastCall().once();
        replay(session, request, response);

        filter().doFilter(request, response, createNiceMock(FilterChain.class));

        verify(session, response);
        String state = (String) stored.getValue();
        assertTrue(state, state.matches("^[A-Za-z0-9_-]{43}$"));
        assertEquals("https://sso/sso/login?subsystemIdentifier=sys&targetUrl=%2Fapp%2Fpage&state=" + state,
                location.getValue());
    }

    @Test
    public void testTokenWithoutStateIsNotExchanged() throws Exception {
        assertTokenRejected(null, STATE);
    }

    @Test
    public void testTokenWithForeignStateIsNotExchanged() throws Exception {
        assertTokenRejected("foreign-state-value-0123456789", STATE);
    }

    @Test
    public void testTokenWithoutStateInSessionIsNotExchanged() throws Exception {
        assertTokenRejected(STATE, null);
    }

    @Test
    public void testTokenWithoutSessionIsNotExchanged() throws Exception {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getRequestURI()).andReturn("/check-token").anyTimes();
        expect(request.getServletPath()).andReturn("/check-token").anyTimes();
        expect(request.getPathInfo()).andReturn(null).anyTimes();
        expect(request.getParameter("subsystemToken")).andReturn("token").anyTimes();
        expect(request.getParameter("state")).andReturn(STATE).anyTimes();
        // the security context lookup creates a session; the state check must not rely on it
        HttpSession created = createNiceMock(HttpSession.class);
        replay(created);
        expect(request.getSession()).andReturn(created).anyTimes();
        expect(request.getSession(false)).andReturn(null).anyTimes();
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        expect(response.getWriter()).andReturn(new java.io.PrintWriter(new java.io.StringWriter())).anyTimes();
        SSOService service = createStrictMock(SSOService.class);
        replay(request, response, service);

        new ExternalFormSecurityFilter(new ExcludedPaths(), "sys", "https://sso", "pkg", service)
                .doFilter(request, response, createNiceMock(FilterChain.class));

        verify(service);
    }

    @Test
    public void testMatchingStateIsExchangedAndConsumed() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        HttpSession session = mapBackedSession(attributes);
        HttpServletRequest request = checkTokenRequest("/page", session);
        expect(request.changeSessionId()).andReturn("new-id").anyTimes();
        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        response.sendRedirect("/page");
        expectLastCall().once();
        replay(session, request, response);

        filter().doFilter(request, response, createNiceMock(FilterChain.class));

        verify(response);
        assertFalse("state must be one-time", attributes.containsKey(SSOLoginState.SESSION_ATTRIBUTE));
    }

    @Test
    public void testSameStateCannotBeUsedTwice() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SSOLoginState.SESSION_ATTRIBUTE, STATE);
        HttpSession session = mapBackedSession(attributes);
        replay(session);
        SSOService service = createStrictMock(SSOService.class);
        expect(service.exchangeSubsystemToken(anyObject(ExchangeSubsystemTokenRequest.class))).andReturn(user()).once();
        replay(service);
        ExternalFormSecurityFilter filter =
                new ExternalFormSecurityFilter(new ExcludedPaths(), "sys", "https://sso", "pkg", service);

        HttpServletRequest first = checkTokenRequest("/page", session);
        expect(first.changeSessionId()).andReturn("new-id").anyTimes();
        HttpServletResponse firstResponse = createNiceMock(HttpServletResponse.class);
        firstResponse.sendRedirect("/page");
        expectLastCall().once();
        replay(first, firstResponse);
        filter.doFilter(first, firstResponse, createNiceMock(FilterChain.class));
        verify(firstResponse);

        HttpServletRequest second = checkTokenRequest("/page", session);
        HttpServletResponse secondResponse = createMock(HttpServletResponse.class);
        expect(secondResponse.getWriter()).andReturn(new java.io.PrintWriter(new java.io.StringWriter())).once();
        replay(second, secondResponse);
        filter.doFilter(second, secondResponse, createNiceMock(FilterChain.class));

        verify(service, secondResponse);
    }

    /** Strict SSOService without expectations: any exchange call fails the test. */
    private static void assertTokenRejected(String requestState, String sessionState) throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        if (sessionState != null) {
            attributes.put(SSOLoginState.SESSION_ATTRIBUTE, sessionState);
        }
        HttpSession session = mapBackedSession(attributes);
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getRequestURI()).andReturn("/check-token").anyTimes();
        expect(request.getServletPath()).andReturn("/check-token").anyTimes();
        expect(request.getPathInfo()).andReturn(null).anyTimes();
        expect(request.getParameter("subsystemToken")).andReturn("token").anyTimes();
        expect(request.getParameter("targetUrl")).andReturn("/page").anyTimes();
        expect(request.getParameter("state")).andReturn(requestState).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(request.getSession(anyBoolean())).andReturn(session).anyTimes();
        HttpServletResponse response = createMock(HttpServletResponse.class);
        java.io.StringWriter body = new java.io.StringWriter();
        expect(response.getWriter()).andReturn(new java.io.PrintWriter(body, true)).once();
        SSOService service = createStrictMock(SSOService.class);
        replay(session, request, response, service);

        try {
            new ExternalFormSecurityFilter(new ExcludedPaths(), "sys", "https://sso", "pkg", service)
                    .doFilter(request, response, createNiceMock(FilterChain.class));
        } catch (AssertionError e) {
            fail("exchangeSubsystemToken must not be called: " + e.getMessage());
        }

        verify(request, response, service);
        assertTrue(body.toString().contains("Token validation failed"));
        assertFalse("state must be one-time", attributes.containsKey(SSOLoginState.SESSION_ATTRIBUTE));
    }

    private static HttpSession mapBackedSession(Map<String, Object> attributes) {
        HttpSession session = createNiceMock(HttpSession.class);
        Capture<String> name = Capture.newInstance();
        expect(session.getAttribute(capture(name))).andAnswer(() -> attributes.get(name.getValue())).anyTimes();
        Capture<String> removed = Capture.newInstance();
        session.removeAttribute(capture(removed));
        expectLastCall().andAnswer(() -> {
            attributes.remove(removed.getValue());
            return null;
        }).anyTimes();
        return session;
    }

    private static SSOUser user() {
        Map<SSORole, SSOAction[]> actions = new HashMap<>();
        actions.put(new SSORole("role"), new SSOAction[]{new SSOAction("act", false)});
        return new SSOUser("pete", actions, Collections.emptyMap());
    }

    private static String safeTarget(String target) {
        HttpServletRequest request = createNiceMock(HttpServletRequest.class);
        expect(request.getParameter("targetUrl")).andReturn(target).anyTimes();
        expect(request.getContextPath()).andReturn("/app").anyTimes();
        replay(request);
        return ExternalFormSecurityFilter.getSafeTargetUrl(request);
    }

    private static HttpServletRequest checkTokenRequest(String target, HttpSession session) {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getRequestURI()).andReturn("/check-token").anyTimes();
        expect(request.getServletPath()).andReturn("/check-token").anyTimes();
        expect(request.getPathInfo()).andReturn(null).anyTimes();
        expect(request.getParameter("subsystemToken")).andReturn("token").anyTimes();
        expect(request.getParameter("targetUrl")).andReturn(target).anyTimes();
        expect(request.getParameter("state")).andReturn(STATE).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(request.getSession(anyBoolean())).andReturn(session).anyTimes();
        return request;
    }

    private static ExternalFormSecurityFilter filter() {
        Map<SSORole, SSOAction[]> actions = new HashMap<>();
        actions.put(new SSORole("role"), new SSOAction[]{new SSOAction("act", false)});
        SSOUser user = new SSOUser("pete", actions, Collections.emptyMap());
        SSOService service = createMock(SSOService.class);
        expect(service.exchangeSubsystemToken(anyObject(ExchangeSubsystemTokenRequest.class))).andReturn(user);
        replay(service);
        return new ExternalFormSecurityFilter(new ExcludedPaths(), "sys", "https://sso", "pkg", service);
    }
}
