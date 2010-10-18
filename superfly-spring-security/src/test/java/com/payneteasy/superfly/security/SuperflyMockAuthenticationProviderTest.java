package com.payneteasy.superfly.security;

import java.util.Collections;
import java.util.Map;


import org.springframework.security.authentication.BadCredentialsException;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.security.authentication.SSOUserAndSelectedRoleAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;
import com.payneteasy.superfly.security.authentication.UsernamePasswordAuthRequestInfoAuthenticationToken;
import com.payneteasy.superfly.security.exception.StepTwoException;
import com.payneteasy.superfly.security.mapbuilder.ActionsMapBuilder;

public class SuperflyMockAuthenticationProviderTest extends AbstractSuperflyAuthenticationProviderTest {
	
	private SuperflyMockAuthenticationProvider provider;
	
	public void setUp() {
		provider = new SuperflyMockAuthenticationProvider();
		provider.setEnabled(true);
		provider.setUsername("pete");
		provider.setPassword("secret");
		provider.setActionsMapBuilder(new ActionsMapBuilder() {
			public Map<SSORole, SSOAction[]> build() throws Exception {
				return Collections.singletonMap(createSSORole(), new SSOAction[]{});
			}
		});
	}
	
	public void testStep1Success() {
		assertNotNull(provider.authenticate(new UsernamePasswordAuthRequestInfoAuthenticationToken("pete", "secret", null)));
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
		provider.setActionsMapBuilder(new ActionsMapBuilder() {
			public Map<SSORole, SSOAction[]> build() throws Exception {
				return Collections.emptyMap();
			}
		});
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
