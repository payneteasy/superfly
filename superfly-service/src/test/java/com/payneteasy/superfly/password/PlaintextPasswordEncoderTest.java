package com.payneteasy.superfly.password;

import junit.framework.TestCase;

public class PlaintextPasswordEncoderTest extends TestCase {
	private PlaintextPasswordEncoder encoder = new PlaintextPasswordEncoder();
	
	public void testEncode() {
		String encoded = encoder.encode("hello", "salt");
		assertEquals("hello{salt}", encoded);
	}
	
	public void testEncodeWithEmptySalt() {
		String encoded = encoder.encode("hello", "");
		assertEquals("hello", encoded);
		
		encoded = encoder.encode("hello", null);
		assertEquals("hello", encoded);
	}
}
