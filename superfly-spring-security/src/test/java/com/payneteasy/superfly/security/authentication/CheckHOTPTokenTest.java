package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;

import junit.framework.TestCase;

public class CheckHOTPTokenTest extends TestCase {
	public void testGetCredentials() {
		CheckHOTPToken token = new CheckHOTPToken(new SSOUser("user", null, null), "hotp");
		assertEquals("hotp", token.getCredentials());
	}
}
