package com.payneteasy.superfly.security;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.security.authentication.CheckHOTPToken;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.EmptyAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;

public class SuperflyMultiMockAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	private SuperflyMultiMockAuthenticationProvider theProvider;
	private AuthenticationProvider provider;
	
	public void setUp() {
		theProvider = new SuperflyMultiMockAuthenticationProvider();
		theProvider.setUsername("pete");
		theProvider.setPassword("password");
		theProvider.setHotp("123456");
		theProvider.setActionsMapBuilder(new ActionsMapBuilder() {
			public Map<SSORole, SSOAction[]> build() throws Exception {
				return createSSOUser(5).getActionsMap();
			}
		});
		provider = theProvider;
	}
	
	public void testSupports() {
		assertTrue(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		assertTrue(provider.supports(CheckHOTPToken.class));
		assertTrue(provider.supports(SSOUserAndSelectedRoleAuthenticationToken.class));
		assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}
	
	public void testPasswordSuccess() {
		Authentication auth = provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "password", null));
		assertNotNull(auth);
		assertTrue(auth instanceof CompoundAuthentication);
	}
	
	public void testHotpSuccess() {
		Authentication auth = provider.authenticate(new CompoundAuthentication(new CheckHOTPToken(createSSOUser(2), "123456")));
		assertNotNull(auth);
		assertTrue(auth instanceof CompoundAuthentication);
		
		// short-circuiting
		auth = provider.authenticate(new CheckHOTPToken(createSSOUserWithOneRole(), "123456"));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);
	}
	
	public void testSelectRoleSuccess() {
		Authentication auth = provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole()));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);
	}
	
	public void testUnsupportedAuthentication() {
		assertNull(provider.authenticate(new EmptyAuthenticationToken()));
	}
	
	public void testDisabled() {
		theProvider.setEnabled(false);
		
		assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));

		Authentication auth = provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "password", null));
		assertNull(auth);
	}
	
	public void testSupportsCompound() {
		assertTrue(provider.supports(CompoundAuthentication.class));
	}
}
