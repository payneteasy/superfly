package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SuperflyHOTPAuthenticationProcessingFilterTest extends
        AbstractAuthenticationProcessingFilterTest {

    @Before
    public void setUp() {
        SuperflyHOTPAuthenticationProcessingFilter procFilter = new SuperflyHOTPAuthenticationProcessingFilter();
        procFilter.setAuthenticationManager(authenticationManager);
        procFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login-failed"));
        procFilter.afterPropertiesSet();
        filter = procFilter;
    }

    @Test
    public void testAuthenticate() throws Exception {
        // expecting some request examination...
        initExpectationsForAuthentication();
        // expecting authentication attempt
        expect(authenticationManager.authenticate(anyObject(CheckHOTPToken.class)))
                .andAnswer(new IAnswer<Authentication>() {
                    public Authentication answer() throws Throwable {
                        CompoundAuthentication compound = (CompoundAuthentication) EasyMock.getCurrentArguments()[0];
                        CheckHOTPToken token = (CheckHOTPToken) compound.getCurrentAuthenticationRequest();
                        assertEquals("pete", token.getName());
                        assertEquals("123456", token.getCredentials().toString());
                        return new HOTPCheckedToken(createSSOUserWithOneRole());
                    }
                });
        // expecting a redirect to a success
        expectRedirectTo("/");
        replay(request, response, chain, authenticationManager);

        SecurityContextHolder.getContext().setAuthentication(createInputAuthentication());
        filter.doFilter(request, response, chain);
        assertTrue("Got " + SecurityContextHolder.getContext().getAuthentication().getClass(),
                SecurityContextHolder.getContext().getAuthentication() instanceof HOTPCheckedToken);

        verify(request, response, chain, authenticationManager);
    }

    @Test
    public void testBadCredentials() throws Exception {
        // expecting some request examination...
        initExpectationsForAuthentication();
        // expecting authentication attempt
        expect(authenticationManager.authenticate(anyObject(CheckHOTPToken.class)))
                .andThrow(new BadCredentialsException("must fail here"));
        // expecting a redirect to a failure page
        expectRedirectTo("/login-failed");
        replay(request, response, chain, authenticationManager);

        SecurityContextHolder.getContext().setAuthentication(createInputAuthentication());
        filter.doFilter(request, response, chain);

        verify(request, response, chain, authenticationManager);
    }

    protected UsernamePasswordCheckedToken createInputAuthentication() {
        return new UsernamePasswordCheckedToken(createSSOUserWithOneRole());
    }

    private void initExpectationsForAuthentication() {
        expect(request.getRequestURI()).andReturn("/j_superfly_hotp_security_check").anyTimes();
        expect(request.getParameter("j_hotp")).andReturn("123456").anyTimes();
        expect(request.getSession(anyBoolean())).andReturn(null).anyTimes();
        expect(request.getSession()).andReturn(session).anyTimes();
        expect(request.getParameter(anyObject(String.class))).andReturn(null).anyTimes();
    }
}
