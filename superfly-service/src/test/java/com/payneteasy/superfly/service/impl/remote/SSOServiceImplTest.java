package com.payneteasy.superfly.service.impl.remote;

import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.service.InternalSSOService;

public class SSOServiceImplTest extends TestCase {
	private SSOServiceImpl ssoService;
	private InternalSSOService internalSSOService;
	
	public void setUp() {
		internalSSOService = EasyMock.createMock(InternalSSOService.class);
		ssoService = new SSOServiceImpl();
		ssoService.setInternalSSOService(internalSSOService);
	}
	
	public void testAuthenticateHOTP() {
		// success
		EasyMock.expect(internalSSOService.authenticateHOTP("pete", "123456")).andReturn(true);
		EasyMock.replay(internalSSOService);
		assertTrue(ssoService.authenticateUsingHOTP("pete", "123456"));
		EasyMock.verify(internalSSOService);
		
		EasyMock.reset(internalSSOService);
		
		// failure
		EasyMock.expect(internalSSOService.authenticateHOTP("pete", "123456")).andReturn(false);
		EasyMock.replay(internalSSOService);
		assertFalse(ssoService.authenticateUsingHOTP("pete", "123456"));
		EasyMock.verify(internalSSOService);
	}
}
