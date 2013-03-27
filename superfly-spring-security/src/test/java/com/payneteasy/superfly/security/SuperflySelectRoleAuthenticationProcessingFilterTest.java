package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import static org.easymock.EasyMock.*;

public class SuperflySelectRoleAuthenticationProcessingFilterTest extends
		AbstractAuthenticationProcessingFilterTest {

    @Before
	public void setUp() {
        SuperflySelectRoleAuthenticationProcessingFilter procFilter = new SuperflySelectRoleAuthenticationProcessingFilter();
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
		expect(authenticationManager.authenticate(anyObject(CompoundAuthentication.class)))
				.andReturn(createResultAuthentication());
		// expecting a redirect to a success
		expectRedirectTo("/");
		replay(request, response, session, chain, authenticationManager);
		
		filter.doFilter(request, response, chain);
        Assert.assertTrue("Got " + SecurityContextHolder.getContext().getAuthentication().getClass(),
                SecurityContextHolder.getContext().getAuthentication() instanceof SSOUserAuthenticationToken);
		
		verify(request, response, session, chain, authenticationManager);
	}

	protected SSOUserAuthenticationToken createResultAuthentication() {
		return new SSOUserAuthenticationToken(createSSOUserWithOneRole(), null, "", null, new StringTransformer[]{}, new RoleSource() {
			public String[] getRoleNames(SSOUser ssoUser, SSORole ssoRole) {
				return new String[]{};
			}
		});
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
