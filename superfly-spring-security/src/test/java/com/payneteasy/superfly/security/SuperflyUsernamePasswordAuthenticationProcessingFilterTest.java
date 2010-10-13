package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;


public class SuperflyUsernamePasswordAuthenticationProcessingFilterTest extends
		AbstractAuthenticationProcessingFilterTest {
	
	private SuperflyUsernamePasswordAuthenticationProcessingFilter procFilter;

	public void setUp() {
		super.setUp();
		procFilter = new SuperflyUsernamePasswordAuthenticationProcessingFilter();
		procFilter.setAuthenticationManager(authenticationManager);
		procFilter.afterPropertiesSet();
		procFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login-failed"));
		procFilter.setSubsystemIdentifier("my-subsystem");
		filter = procFilter;
	}
	
	public void testAuthenticate() throws Exception {
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(UsernamePasswordAuthRequestInfoAuthenticationToken.class)))
				.andAnswer(new IAnswer<Authentication>() {
					public Authentication answer() throws Throwable {
						UsernamePasswordAuthRequestInfoAuthenticationToken token = (UsernamePasswordAuthRequestInfoAuthenticationToken) EasyMock.getCurrentArguments()[0];
						assertEquals("192.168.0.4", token.getAuthRequestInfo().getIpAddress());
						assertEquals("my-subsystem", token.getAuthRequestInfo().getSubsystemIdentifier());
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
		expect(request.getRequestURI()).andReturn("/j_superfly_password_security_check").anyTimes();
		expect(request.getParameter("j_username")).andReturn("user").anyTimes();
		expect(request.getParameter("j_password")).andReturn("password").anyTimes();
		expect(request.getSession(anyBoolean())).andReturn(null).anyTimes();
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getRemoteAddr()).andReturn("192.168.0.4").anyTimes();
		expect(request.getParameter(anyObject(String.class))).andReturn(null).anyTimes();
	}
}
