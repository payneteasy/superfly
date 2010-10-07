package com.payneteasy.superfly.security;

import org.easymock.EasyMock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.api.SSOUser;

public abstract class AbstractAuthenticationProcessingFilterTest extends
		AbstractFilterTest {

	protected AuthenticationManager authenticationManager;
	
	public void setUp() {
		super.setUp();
		authenticationManager = EasyMock.createMock(AuthenticationManager.class);
	}
	
	public void tearDown() throws Exception {
		super.tearDown();
		SecurityContextHolder.clearContext();
	}

	protected SSOUser createSSOUser() {
		return new SSOUser("user", null, null);
	}

}