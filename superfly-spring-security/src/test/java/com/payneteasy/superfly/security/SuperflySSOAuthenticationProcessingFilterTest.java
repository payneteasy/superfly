package com.payneteasy.superfly.security;

import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOAuthenticationRequest;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import static org.easymock.EasyMock.*;


public class SuperflySSOAuthenticationProcessingFilterTest extends
		AbstractAuthenticationProcessingFilterTest {

    public void setUp() {
		super.setUp();
        SuperflySSOAuthenticationProcessingFilter procFilter = new SuperflySSOAuthenticationProcessingFilter();
		procFilter.setAuthenticationManager(authenticationManager);
        procFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login-failed"));
		procFilter.afterPropertiesSet();
		filter = procFilter;
	}
	
	public void testAuthenticate() throws Exception {
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(SSOAuthenticationRequest.class)))
				.andAnswer(new IAnswer<Authentication>() {
					public Authentication answer() throws Throwable {
						CompoundAuthentication compound = (CompoundAuthentication) EasyMock.getCurrentArguments()[0];
						SSOAuthenticationRequest token = (SSOAuthenticationRequest) compound.getCurrentAuthenticationRequest();
						assertEquals("abcdef", token.getSubsystemToken());
						return new UsernamePasswordCheckedToken(createSSOUserWithOneRole());
					}
				});
		// expecting a redirect to a success
		expectRedirectTo("/");
		replay(request, response, chain, authenticationManager);
		
		filter.doFilter(request, response, chain);
		assertTrue(SecurityContextHolder.getContext().getAuthentication() instanceof UsernamePasswordCheckedToken);
		
		verify(request, response, chain, authenticationManager);
	}
	
	public void testBadCredentials() throws Exception {
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(UsernamePasswordAuthRequestInfoAuthenticationToken.class)))
				.andThrow(new BadCredentialsException("must fail here"));
		// expecting a redirect to a failure page
		expectRedirectTo("/login-failed");
		replay(request, response, chain, authenticationManager);
		
		filter.doFilter(request, response, chain);
		
		verify(request, response, chain, authenticationManager);
	}
	
	private void initExpectationsForAuthentication() {
		expect(request.getRequestURI()).andReturn("/j_superfly_sso_security_check").anyTimes();
        expect(request.getParameter("subsystemToken")).andReturn("abcdef").anyTimes();
		expect(request.getParameter("targetUrl")).andReturn("/my-target").anyTimes();
		expect(request.getSession(anyBoolean())).andReturn(null).anyTimes();
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("192.168.0.4").anyTimes();
		expect(request.getParameter(anyObject(String.class))).andReturn(null).anyTimes();
	}
}
