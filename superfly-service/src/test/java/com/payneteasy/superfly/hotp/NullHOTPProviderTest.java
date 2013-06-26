package com.payneteasy.superfly.hotp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NullHOTPProviderTest {
	private NullHOTPProvider provider;

    @Before
	public void setUp() {
		provider = new NullHOTPProvider();
	}

    @Test
	public void testAuthenticate() {
        Assert.assertTrue(provider.authenticate(null, "user", "123456"));
	}
}
