package com.payneteasy.superfly.security;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;

import org.easymock.EasyMock;
import org.springframework.security.authentication.BadCredentialsException;

import com.payneteasy.superfly.api.AuthenticationRequestInfo;
import com.payneteasy.superfly.api.SSOService;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;

public class SuperflyAuthenticationProviderTest extends AbstractSuperflyAuthenticationProviderTest {

	private SuperflyAuthenticationProvider provider;
	private SSOService ssoService;
	
	public void setUp() {
		ssoService = EasyMock.createMock(SSOService.class);
		provider = new SuperflyAuthenticationProvider();
		provider.setSsoService(ssoService);
	}
	
	public void testStep1Success() {
		EasyMock.expect(ssoService.authenticate(eq("pete"), eq("secret"), anyObject(AuthenticationRequestInfo.class)))
				.andReturn(createSSOUserWithOneRole());
		EasyMock.replay(ssoService);
		assertNotNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null)));
		EasyMock.verify(ssoService);
	}
	
	public void testStep1_5Success() {
		try {
			provider.authenticate(new SSOUserTransportAuthenticationToken(createSSOUserWithOneRole()));
			fail();
		} catch (StepTwoException e) {
			// expected
		}
	}

	public void testStep2Success() {
		assertNotNull(provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole())));
	}
	
	public void testBadPassword() {
		try {
			provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory", "whatisthepassword", null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
	
	public void testNoRoles() {
		try {
			provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("cory", "whatisthepassword", null));
			fail();
		} catch (BadCredentialsException e) {
			// expected
		}
	}
	
	public void testDisabled() {
		provider.setEnabled(false);
		assertNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null)));
	}
}
