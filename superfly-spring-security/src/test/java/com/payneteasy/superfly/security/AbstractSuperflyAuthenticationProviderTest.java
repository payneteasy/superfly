package com.payneteasy.superfly.security;

import java.util.Collections;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

import junit.framework.TestCase;

public abstract class AbstractSuperflyAuthenticationProviderTest extends
		TestCase {

	protected SSOUser createSSOUser() {
		return new SSOUser("pete", Collections.singletonMap(createSSORole(), new SSOAction[]{}), Collections.<String, String>emptyMap());
	}
	
	protected SSOUser createSSOUserWithNoRoles() {
		return new SSOUser("pete", Collections.<SSORole, SSOAction[]>emptyMap(), Collections.<String, String>emptyMap());
	}

	protected SSORole createSSORole() {
		return new SSORole("test-role");
	}

}