package org.payneteasy.superfly.client.test;

import org.springframework.security.annotation.Secured;

@Secured({"multiple1", "multiple2"})
public class AnnotatedClass2 {
	@SuppressWarnings("unused")
	@Secured(value = "nested")
	private class Nested {
		
	}
}
