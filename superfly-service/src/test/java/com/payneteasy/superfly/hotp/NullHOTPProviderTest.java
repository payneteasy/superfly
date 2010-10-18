package com.payneteasy.superfly.hotp;

import junit.framework.TestCase;

public class NullHOTPProviderTest extends TestCase {
	private NullHOTPProvider provider;
	
	public void setUp() {
		provider = new NullHOTPProvider();
	}
	
	public void testAuthenticate() {
		assertTrue(provider.authenticate("user", "123456"));
	}
}
