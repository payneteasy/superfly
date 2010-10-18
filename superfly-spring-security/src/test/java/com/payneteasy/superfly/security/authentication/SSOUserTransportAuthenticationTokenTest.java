package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

import junit.framework.TestCase;

public class SSOUserTransportAuthenticationTokenTest extends TestCase {
	public void testGetName() {
		SSOUserTransportAuthenticationToken token = new SSOUserTransportAuthenticationToken(new SSOUser("user", null, null));
		assertEquals("user", token.getName());
	}
}
