package com.payneteasy.superfly.security;

import org.easymock.EasyMock;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;

public class InsufficientAuthenticationHandlingFilterTest extends AbstractFilterTest {
	public void setUp() {
		super.setUp();
		InsufficientAuthenticationHandlingFilter insufFilter = new InsufficientAuthenticationHandlingFilter();
		insufFilter.setInsufficientAuthenticationClasses(new Class<?>[]{Insuf.class});
		filter = insufFilter;
	}
	
	public void tearDown() {
		SecurityContextHolder.clearContext();
	}
	
	public void testThrowing() throws Exception {
		Insuf auth = new Insuf();
		SecurityContextHolder.getContext().setAuthentication(auth);
		try {
			filter.doFilter(request, response, chain);
			fail();
		} catch (InsufficientAuthenticationException e) {
			// expected
			assertSame(auth, e.getAuthentication());
		}
	}
	
	public void testNotThrowing() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(new Suf());
		chain.doFilter(request, response);
		EasyMock.expectLastCall();
		EasyMock.replay(chain);
		filter.doFilter(request, response, chain);
	}
	
	public void testNullAuthentication() throws Exception {
		SecurityContextHolder.getContext().setAuthentication(null);
		chain.doFilter(request, response);
		EasyMock.expectLastCall();
		EasyMock.replay(chain);
		filter.doFilter(request, response, chain);
	}
	
	private static class Insuf extends EmptyAuthenticationToken {
		private static final long serialVersionUID = 1L;
	}
	
	private static class Suf extends EmptyAuthenticationToken {
		private static final long serialVersionUID = 1L;
	}
}
