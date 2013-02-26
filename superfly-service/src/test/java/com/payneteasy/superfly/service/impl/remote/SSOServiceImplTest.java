package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.SSOAction;
import com.payneteasy.superfly.api.SSORole;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.UserDescription;
import junit.framework.Assert;
import junit.framework.TestCase;

import org.easymock.EasyMock;

import com.payneteasy.superfly.service.InternalSSOService;

import java.util.Arrays;
import java.util.Collections;

import static org.easymock.EasyMock.*;

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
		expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(true);
		replay(internalSSOService);
		assertTrue(ssoService.authenticateUsingHOTP("pete", "123456"));
		verify(internalSSOService);
		
		EasyMock.reset(internalSSOService);
		
		// failure
		expect(internalSSOService.authenticateHOTP(null, "pete", "123456")).andReturn(false);
		replay(internalSSOService);
		assertFalse(ssoService.authenticateUsingHOTP("pete", "123456"));
		verify(internalSSOService);
	}

    public void testExchangeSubsystemToken() {
        SSOUser user = new SSOUser("pete", Collections.singletonMap(
                new SSORole("test-role"), new SSOAction[]{new SSOAction("test-action", false)}
        ), null);
        expect(internalSSOService.exchangeSubsystemToken("token"))
                .andReturn(user);
        replay(internalSSOService);

        ssoService.exchangeSubsystemToken("token");

        verify(internalSSOService);
    }

    public void testTouchSessions() {
        internalSSOService.touchSessions(Arrays.asList(1L, 2L, 3L));
        expectLastCall();
        replay(internalSSOService);
        ssoService.touchSessions(Arrays.asList(1L, 2L, 3L));
        verify(internalSSOService);
    }

    public void testGetUserDescriptionNotExistingUser() {
        expect(internalSSOService.getUserDescription("no-such-user")).andReturn(null);
        replay(internalSSOService);
        UserDescription user = ssoService.getUserDescription("no-such-user");
        Assert.assertNull(user);
        verify(internalSSOService);
    }
}
