package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractAuthenticationProcessingFilterTest extends
		AbstractFilterTest {

	protected AuthenticationManager authenticationManager;

    @Before
	public void initAPFTest() {
		authenticationManager = EasyMock.createMock(AuthenticationManager.class);
	}

    @After
	public void cleanAPFTest() throws Exception {
		SecurityContextHolder.clearContext();
	}

    @Test
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