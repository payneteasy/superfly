package com.payneteasy.superfly.security;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.security.authentication.*;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Map;

import static org.junit.Assert.*;

public class SuperflyMultiMockAuthenticationProviderTest extends
		AbstractSuperflyAuthenticationProviderTest {
	private SuperflyMultiMockAuthenticationProvider theProvider;
	private AuthenticationProvider provider;

    @Before
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

    @Test
	public void testSupports() {
        assertTrue(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));
		assertTrue(provider.supports(CheckHOTPToken.class));
		assertTrue(provider.supports(SSOUserAndSelectedRoleAuthenticationToken.class));
        assertFalse(provider.supports(UsernamePasswordAuthenticationToken.class));
	}

    @Test
	public void testPasswordSuccess() {
		Authentication auth = provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "password", null));
        assertNotNull(auth);
		assertTrue(auth instanceof CompoundAuthentication);
	}

    @Test
	public void testHotpSuccess() {
		Authentication auth = provider.authenticate(new CompoundAuthentication(new CheckHOTPToken(createSSOUser(2), "123456")));
		assertNotNull(auth);
		assertTrue(auth instanceof CompoundAuthentication);
		
		// short-circuiting
		auth = provider.authenticate(new CheckHOTPToken(createSSOUserWithOneRole(), "123456"));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);
	}

    @Test
	public void testSelectRoleSuccess() {
		Authentication auth = provider.authenticate(new SSOUserAndSelectedRoleAuthenticationToken(createSSOUserWithOneRole(), createSSORole()));
		assertNotNull(auth);
		assertTrue(auth instanceof SSOUserAuthenticationToken);
	}

    @Test
	public void testUnsupportedAuthentication() {
        assertNull(provider.authenticate(new EmptyAuthenticationToken()));
	}

    @Test
	public void testDisabled() {
		theProvider.setEnabled(false);
		
		assertFalse(provider.supports(UsernamePasswordAuthRequestInfoAuthenticationToken.class));

		Authentication auth = provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "password", null));
		assertNull(auth);
	}

    @Test
	public void testSupportsCompound() {
		assertTrue(provider.supports(CompoundAuthentication.class));
	}
}
