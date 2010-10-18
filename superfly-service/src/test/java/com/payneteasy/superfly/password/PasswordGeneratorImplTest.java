package com.payneteasy.superfly.password;

import junit.framework.TestCase;

public class PasswordGeneratorImplTest extends TestCase {
	public void test() {
		PasswordGeneratorImpl generator = new PasswordGeneratorImpl();
		generator.setPasswordLength(8);
		for (int i = 0; i < 100; i++) {
			assertEquals(8, generator.generate().length());
		}
	}
}
