package com.payneteasy.superfly.security.filters;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ExternalFormSecurityFilterTest {

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
