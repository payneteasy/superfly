package com.payneteasy.superfly.password;

import org.junit.Assert;
import org.junit.Test;

public class SHA256SaltGeneratorTest {
    @Test
	public void test() {
		SHA256RandomGUIDSaltGenerator generator = new SHA256RandomGUIDSaltGenerator();
		String salt = generator.generate();
        Assert.assertNotNull(salt);
        Assert.assertTrue(salt.length() > 0);
	}
}
