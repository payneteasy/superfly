package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertEquals;

public class TwoStepAuthenticationProcessingFilterEntryPointTest extends AbstractSSOUserAwareTest {

    private TwoStepAuthenticationProcessingFilterEntryPoint entryPoint;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private HttpSession session;

    @Before
    public void setUp() {
        entryPoint = new TwoStepAuthenticationProcessingFilterEntryPoint("/login", "/login-step2");
        request = EasyMock.createMock(HttpServletRequest.class);
        response = EasyMock.createMock(HttpServletResponse.class);
        session = EasyMock.createMock(HttpSession.class);
    }

    @Test
    public void determineUrl_forOrdinaryFailure_returnsLoginUrl() {
        EasyMock.replay(request, response);

        assertEquals("/login", entryPoint.determineUrlToUseForThisRequest(
                request, response, new BadCredentialsException("bad")));
    }

    @Test
    public void determineUrl_forStepTwo_storesUserAndRolesInSessionAndReturnsStepTwoUrl() {
        var user = createSSOUser(2);
        StepTwoException exception = new StepTwoException("step two");
        exception.setAuthentication(new SSOUserTransportAuthenticationToken(user));
        EasyMock.expect(request.getSession()).andReturn(session).anyTimes();
        EasyMock.expect(request.getContextPath()).andReturn("/app");
        session.setAttribute(SSOUserTransportAuthenticationToken.SESSION_KEY, user);
        session.setAttribute("superflyRoles", user.getActionsMap().keySet());
        session.setAttribute("ctxPath", "/app");
        EasyMock.replay(request, response, session);

        assertEquals("/login-step2", entryPoint.determineUrlToUseForThisRequest(request, response, exception));

        EasyMock.verify(session);
    }

    @Test(expected = IllegalStateException.class)
    public void determineUrl_forStepTwoWithoutAuthentication_throwsIllegalState() {
        EasyMock.replay(request, response);

        entryPoint.determineUrlToUseForThisRequest(request, response, new StepTwoException("step two"));
    }
}
