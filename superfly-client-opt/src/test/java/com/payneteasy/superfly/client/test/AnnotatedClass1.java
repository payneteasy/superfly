package com.payneteasy.superfly.client.test;

import org.springframework.security.access.annotation.Secured;

@Secured("single")
public class AnnotatedClass1 {
	@Secured("method")
	public void annotatedMethod() {
		
	}
	
	@Secured("action_temp_password")
	public void with_temp_password() {
		
	}
}
