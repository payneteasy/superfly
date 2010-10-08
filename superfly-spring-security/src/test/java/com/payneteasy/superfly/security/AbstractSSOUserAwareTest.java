package com.payneteasy.superfly.security;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;

public abstract class AbstractSSOUserAwareTest extends TestCase {

	protected SSOUser createSSOUserWithOneRole() {
		return createSSOUser(1);
	}

	protected SSOUser createSSOUser(int roleCount) {
		Map<SSORole, SSOAction[]> actionMap = new HashMap<SSORole, SSOAction[]>();
		for (int i = 0; i < roleCount; i++) {
			actionMap.put(createSSORole("role" + i), new SSOAction[]{});
		}
		return new SSOUser("pete", actionMap, Collections.<String, String>emptyMap());
	}

	protected SSOUser createSSOUserWithNoRoles() {
		return new SSOUser("pete", Collections.<SSORole, SSOAction[]>emptyMap(), Collections.<String, String>emptyMap());
	}

	protected SSORole createSSORole() {
		return createSSORole("role0");
	}

	protected SSORole createSSORole(String name) {
		return new SSORole(name);
	}

}