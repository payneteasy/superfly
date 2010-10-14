package com.payneteasy.superfly.security.processor;

import org.springframework.security.core.Authentication;

import com.payneteasy.superfly.security.AbstractSSOUserAwareTest;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.authentication.SSOUserAuthenticationToken;
import com.payneteasy.superfly.security.authentication.SSOUserTransportAuthenticationToken;

public class SuperflyAuthenticationPostProcessorTest extends AbstractSSOUserAwareTest {
	private SuperflyAuthenticationPostProcessor processor;
	
	public void setUp() {
		processor = new SuperflyAuthenticationPostProcessor();
	}
	
	public void testDoNothing() {
		CompoundAuthentication request = new CompoundAuthentication();
		request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(2)));
		Authentication auth = processor.postProcess(request);
		assertSame(request, auth);
		
		request = new CompoundAuthentication();
		request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(1)));
		auth = processor.postProcess(request);
		assertSame(request, auth);
	}
	
	public void testShortCircuit() {
		CompoundAuthentication request = new CompoundAuthentication();
		request.addReadyAuthentication(new SSOUserTransportAuthenticationToken(createSSOUser(1)));
		processor.setFinishWithSuperflyFinalAuthentication(true);
		Authentication auth = processor.postProcess(request);
		assertTrue("Got: " + auth.getClass(), auth instanceof SSOUserAuthenticationToken);
	}
}
