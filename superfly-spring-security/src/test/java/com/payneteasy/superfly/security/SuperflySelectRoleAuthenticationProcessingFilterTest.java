package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.anyBoolean;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import org.springframework.security.core.context.SecurityContextHolder;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;

public class SuperflySelectRoleAuthenticationProcessingFilterTest extends
		AbstractAuthenticationProcessingFilterTest {
	public void setUp() {
		super.setUp();
		SuperflySelectRoleAuthenticationProcessingFilter theFilter = new SuperflySelectRoleAuthenticationProcessingFilter();
		theFilter.setAuthenticationManager(authenticationManager);
		theFilter.afterPropertiesSet();
		filter = theFilter;
	}
	
	public void testAuthenticate() throws Exception {
		// expecting some request examination...
		initExpectationsForAuthentication();
		// expecting authentication attempt
		expect(authenticationManager.authenticate(anyObject(SSOUserAndSelectedRoleAuthenticationToken.class)))
				.andReturn(new SSOUserAuthenticationToken(createSSOUserWithOneRole(), null, "", null, new StringTransformer[]{}, new RoleSource() {
					public String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole) {
						return new String[]{};
					}
				}));
		// expecting a redirect to a success
		expectRedirectTo("/");
		replay(request, response, session, chain, authenticationManager);
		
		filter.doFilter(request, response, chain);
		assertTrue("Got " + SecurityContextHolder.getContext().getAuthentication().getClass(), SecurityContextHolder.getContext().getAuthentication() instanceof SSOUserAuthenticationToken);
		
		verify(request, response, session, chain, authenticationManager);
	}
	
	private void initExpectationsForAuthentication() {
		expect(request.getRequestURI()).andReturn("/j_superfly_select_role").anyTimes();
		expect(request.getParameter("j_role")).andReturn("role0").anyTimes();
		expect(request.getSession(anyBoolean())).andReturn(null).anyTimes();
		expect(request.getSession()).andReturn(session).anyTimes();
		expect(request.getParameter(anyObject(String.class))).andReturn(null).anyTimes();
		expect(session.getAttribute(anyObject(String.class))).andReturn(createSSOUserWithOneRole()).anyTimes();
		session.removeAttribute(anyObject(String.class));
		expectLastCall().anyTimes();
	}
}
