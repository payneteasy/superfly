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

import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.HOTPCheckedToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordCheckedToken;


public class SuperflyHOTPAuthenticationProcessingFilterTest extends
		AbstractAuthenticationProcessingFilterTest {
	
	private SuperflyHOTPAuthenticationProcessingFilter procFilter;

	public void setUp() {
		super.setUp();
		procFilter = new SuperflyHOTPAuthenticationProcessingFilter();
		procFilter.setAuthenticationManager(authenticationManager);
		procFilter.afterPropertiesSet();
		procFilter.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler("/login-failed"));
		filter = procFilter;
	}
	
	public void testAuthenticate() throws Exception {
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(CheckHOTPToken.class)))
				.andAnswer(new IAnswer<Authentication>() {
					public Authentication answer() throws Throwable {
						CheckHOTPToken token = (CheckHOTPToken) EasyMock.getCurrentArguments()[0];
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
		assertTrue("Got " + SecurityContextHolder.getContext().getAuthentication().getClass(), SecurityContextHolder.getContext().getAuthentication() instanceof HOTPCheckedToken);
		
		verify(request, response, chain, authenticationManager);
	}

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
	
	public void testRequiredExistingAuthenticationOk() throws Exception {
		procFilter.setRequiredExistingAuthenticationClasses(new Class<?>[]{UsernamePasswordCheckedToken.class});
		
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(CheckHOTPToken.class)))
				.andReturn(new HOTPCheckedToken(createSSOUserWithOneRole()));
		// expecting a redirect to a failure page
		expectRedirectTo("/");
		replay(request, response, chain, authenticationManager);

		SecurityContextHolder.getContext().setAuthentication(createInputAuthentication());
		
		filter.doFilter(request, response, chain);
	}
	
	public void testRequiredExistingAuthenticationFailure() throws Exception {
		procFilter.setRequiredExistingAuthenticationClasses(new Class<?>[]{EmptyAuthenticationToken.class});
		
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting a redirect to a failure page
		expectRedirectTo("/login-failed");
		replay(request, response, chain, authenticationManager);

		SecurityContextHolder.getContext().setAuthentication(createInputAuthentication());
		
		filter.doFilter(request, response, chain);
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
