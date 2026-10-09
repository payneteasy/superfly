package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.client.SSOLoginState;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.After;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;

import static org.easymock.EasyMock.*;

public class SingleStepFilterSessionIdTest extends AbstractSSOUserAwareTest {

    @After
    public void clean() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void testSessionIdChangedAfterSuccessfulAuthentication() throws Exception {
        HttpServletRequest request = request();
        expect(request.changeSessionId()).andReturn("new-id").once();
        run(request, authenticatingManager(), null);
        verify(request);
    }

    @Test
    public void testSessionIdNotChangedAfterFailedAuthentication() throws Exception {
        HttpServletRequest request = request();
        AuthenticationManager manager = createMock(AuthenticationManager.class);
        expect(manager.authenticate(anyObject())).andThrow(new BadCredentialsException("bad"));
        replay(manager);
        run(request, manager, null);
        verify(request);
    }

    @Test
    public void testStrategyCanBeOverridden() throws Exception {
        HttpServletRequest request = request();
        run(request, authenticatingManager(), new NullAuthenticatedSessionStrategy());
        verify(request);
    }

    private AuthenticationManager authenticatingManager() {
        AuthenticationManager manager = createMock(AuthenticationManager.class);
        expect(manager.authenticate(anyObject()))
                .andReturn(new UsernamePasswordCheckedToken(createSSOUserWithOneRole()));
        replay(manager);
        return manager;
    }

    private HttpServletRequest request() {
        HttpServletRequest request = createMock(HttpServletRequest.class);
        HttpSession session = createNiceMock(HttpSession.class);
        expect(session.getId()).andReturn("old-id").anyTimes();
        expect(session.getAttribute(SSOLoginState.SESSION_ATTRIBUTE)).andReturn("state-1234567890abcdef").anyTimes();
        replay(session);
        expect(request.getContextPath()).andReturn("").anyTimes();
        expect(request.getMethod()).andReturn("POST").anyTimes();
        expect(request.getRequestURI()).andReturn("/j_superfly_sso_security_check").anyTimes();
        expect(request.getServletPath()).andReturn("").anyTimes();
        expect(request.getPathInfo()).andReturn("/j_superfly_sso_security_check").anyTimes();
        expect(request.getParameter("subsystemToken")).andReturn("abcdef").anyTimes();
        expect(request.getParameter("state")).andReturn("state-1234567890abcdef").anyTimes();
        expect(request.getParameter(anyObject(String.class))).andReturn(null).anyTimes();
        expect(request.getSession(anyBoolean())).andReturn(session).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(request.isRequestedSessionIdValid()).andReturn(true).anyTimes();
        expect(request.getRemoteAddr()).andReturn("192.168.0.4").anyTimes();
        expect(request.getHeader(anyObject(String.class))).andReturn(null).anyTimes();
        request.setAttribute(anyObject(String.class), anyObject());
        expectLastCall().anyTimes();
        return request;
    }

    private void run(HttpServletRequest request, AuthenticationManager manager,
                     NullAuthenticatedSessionStrategy strategy) throws Exception {
        SuperflySSOAuthenticationProcessingFilter filter = new SuperflySSOAuthenticationProcessingFilter();
        filter.setAuthenticationManager(manager);
        filter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login-failed"));
        if (strategy != null) {
            filter.setSessionAuthenticationStrategy(strategy);
        }
        filter.afterPropertiesSet();

        HttpServletResponse response = createNiceMock(HttpServletResponse.class);
        expect(response.isCommitted()).andReturn(false).anyTimes();
        expect(response.encodeRedirectURL(anyObject(String.class))).andReturn("/").anyTimes();
        FilterChain chain = createNiceMock(FilterChain.class);
        replay(request, response, chain);

        filter.doFilter(request, response, chain);
    }
}
