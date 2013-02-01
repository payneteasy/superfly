package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.service.InternalSSOService;

import java.util.Arrays;
import java.util.Collections;

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
		EasyMock.expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(true);
		EasyMock.replay(internalSSOService);
		assertTrue(ssoService.authenticateUsingHOTP("pete", "123456"));
		EasyMock.verify(internalSSOService);
		
		EasyMock.reset(internalSSOService);
		
		// failure
		EasyMock.expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(false);
		EasyMock.replay(internalSSOService);
		assertFalse(ssoService.authenticateUsingHOTP("pete", "123456"));
		EasyMock.verify(internalSSOService);
	}

    public void testExchangeSubsystemToken() {
        SSOUser user = new SSOUser("pete", Collections.singletonMap(
                new SSORole("test-role"), new SSOAction[]{new SSOAction("test-action", false)}
        ), null);
        EasyMock.expect(internalSSOService.exchangeSubsystemToken("token"))
                .andReturn(user);
        EasyMock.replay(internalSSOService);

        ssoService.exchangeSubsystemToken("token");

        EasyMock.verify(internalSSOService);
    }

    public void testTouchSessions() {
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L));
        EasyMock.expectLastCall();
        EasyMock.replay(internalSSOService);
        ssoService.touchSessions(Arrays.asList(1L, 2L, 3L));
        EasyMock.verify(internalSSOService);
    }
}
