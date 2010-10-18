package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

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

	public void testDoNothing() throws Exception {
		expect(request.getRequestURI()).andReturn("/").anyTimes();
		// expecting that chain will just proceed
		chain.doFilter(request, response);
		expectLastCall();
		replay(request, response, chain);
		
		filter.doFilter(request, response, chain);
		
		verify(request, response, chain);
	}

}