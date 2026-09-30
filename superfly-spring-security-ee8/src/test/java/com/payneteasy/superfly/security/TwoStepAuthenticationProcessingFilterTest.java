package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class TwoStepAuthenticationProcessingFilterTest extends AbstractSSOUserAwareTest {

    private TwoStepAuthenticationProcessingFilter filter;
    private AuthenticationManager authenticationManager;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() {
        filter = new TwoStepAuthenticationProcessingFilter();
        filter.setSubsystemIdentifier("my-subsystem");
        authenticationManager = EasyMock.createMock(AuthenticationManager.class);
        filter.setAuthenticationManager(authenticationManager);
        request = EasyMock.createMock(HttpServletRequest.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        session = EasyMock.createMock(HttpSession.class);
    }

    @After
    public void tearDown() {
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    public void stepOne_passesUsernamePasswordAndRequestInfoToManager() {
        EasyMock.expect(request.getParameter("username")).andReturn("pete").anyTimes();
        EasyMock.expect(request.getParameter("password")).andReturn("secret").anyTimes();
        EasyMock.expect(request.getRemoteAddr()).andReturn("10.0.0.1");
        Authentication result = EasyMock.createMock(Authentication.class);
        EasyMock.expect(authenticationManager.authenticate(EasyMock.anyObject(Authentication.class)))
                .andAnswer(() -> {
                    UsernamePasswordAuthRequestInfoAuthenticationToken token =
                            (UsernamePasswordAuthRequestInfoAuthenticationToken) EasyMock.getCurrentArguments()[0];
                    assertEquals("pete", token.getName());
                    assertEquals("secret", token.getCredentials());
                    assertEquals("10.0.0.1", token.getAuthRequestInfo().getIpAddress());
                    assertEquals("my-subsystem", token.getAuthRequestInfo().getSubsystemIdentifier());
                    return result;
                });
        EasyMock.replay(request, response, authenticationManager, result);

        assertSame(result, filter.attemptAuthentication(request, response));

        EasyMock.verify(request, authenticationManager);
    }

    @Test
    public void stepTwo_selectsRoleFromSessionUserAndClearsIt() {
        SSOUser user = createSSOUser(2);
        EasyMock.expect(request.getParameter("username")).andReturn(null).anyTimes();
        EasyMock.expect(request.getParameter("j_role")).andReturn("role1").anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY)).andReturn(user);
        session.removeAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY);
        Authentication result = EasyMock.createMock(Authentication.class);
        EasyMock.expect(authenticationManager.authenticate(EasyMock.anyObject(Authentication.class)))
                .andAnswer(() -> {
                    SSOUserAndSelectedRoleAuthenticationToken token =
                            (SSOUserAndSelectedRoleAuthenticationToken) EasyMock.getCurrentArguments()[0];
                    assertEquals("role1", token.getSsoRole().getName());
                    return result;
                });
        EasyMock.replay(request, response, session, authenticationManager, result);

        assertSame(result, filter.attemptAuthentication(request, response));

        EasyMock.verify(session, authenticationManager);
    }

    @Test(expected = BadCredentialsException.class)
    public void stepTwo_whenSessionUserMissing_throwsSessionExpired() {
        EasyMock.expect(request.getParameter("username")).andReturn(null).anyTimes();
        EasyMock.expect(request.getParameter("j_role")).andReturn("role1").anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY)).andReturn(null);
        session.removeAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY);
        EasyMock.replay(request, response, session, authenticationManager);

        filter.attemptAuthentication(request, response);
    }

    @Test(expected = BadCredentialsException.class)
    public void stepTwo_whenRoleUnknown_throwsBadCredentials() {
        EasyMock.expect(request.getParameter("username")).andReturn(null).anyTimes();
        EasyMock.expect(request.getParameter("j_role")).andReturn("nope").anyTimes();
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(session.getAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY))
                .andReturn(createSSOUser(2));
        session.removeAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY);
        EasyMock.replay(request, response, session, authenticationManager);

        filter.attemptAuthentication(request, response);
    }

    @Test(expected = IllegalStateException.class)
    public void neitherStep_throwsIllegalState() {
        EasyMock.expect(request.getParameter(EasyMock.anyObject(String.class))).andReturn(null).anyTimes();
        EasyMock.replay(request, response, authenticationManager);

        filter.attemptAuthentication(request, response);
    }
}
