package com.payneteasy.superfly.password;

import junit.framework.TestCase;

public class SHA256SaltGeneratorTest extends TestCase {
	public void test() {
		SHA256RandomGUIDSaltGenerator generator = new SHA256RandomGUIDSaltGenerator();
		String salt = generator.generate();
		assertNotNull(salt);
		assertTrue(salt.length() > 0);
	}
}
