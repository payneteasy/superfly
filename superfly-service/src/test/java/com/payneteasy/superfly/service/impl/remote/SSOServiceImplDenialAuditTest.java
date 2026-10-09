package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.request.HasOtpMasterKeyRequest;
import com.payneteasy.superfly.api.request.UpdateUserOtpTypeRequest;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.LoggerSink;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A subsystem that is denied access to a user leaves a record in the security log.
 */
public class SSOServiceImplDenialAuditTest {
    private InternalSSOService internal;
    private LoggerSink loggerSink;
    private SSOServiceImpl ssoService;

    @Before
    public void setUp() {
        internal = createStrictMock(InternalSSOService.class);
        loggerSink = createStrictMock(LoggerSink.class);
        ssoService = new SSOServiceImpl(internal, null, null, null, null, loggerSink);
        ssoService.setSubsystemIdentifierObtainer(hint -> "billing");
    }

    @Test
    public void deniedReadIsLogged() {
        expect(internal.isUserAccessibleFrom("victim", "billing")).andReturn(false);
        loggerSink.info(anyObject(Logger.class), eq("SUBSYSTEM_ACCESS_DENIED"), eq(false), eq("victim"),
                eq("method=hasOtpMasterKey, subsystem=billing"));
        replay(internal, loggerSink);

        assertFalse(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest("victim")));

        verify(internal, loggerSink);
    }

    @Test
    public void deniedChangeIsLogged() {
        expect(internal.isUserManageableFrom("victim", "billing")).andReturn(false);
        loggerSink.info(anyObject(Logger.class), eq("SUBSYSTEM_ACCESS_DENIED"), eq(false), eq("victim"),
                eq("method=updateUserOtpType, subsystem=billing"));
        replay(internal, loggerSink);

        ssoService.updateUserOtpType(new UpdateUserOtpTypeRequest("victim", "GOOGLE_AUTH"));

        verify(internal, loggerSink);
    }

    @Test
    public void allowedAccessIsNotLoggedAsDenied() {
        expect(internal.isUserAccessibleFrom("own", "billing")).andReturn(true);
        expect(internal.hasOtpMasterKey("own")).andReturn(true);
        replay(internal, loggerSink);

        assertTrue(ssoService.hasOtpMasterKey(new HasOtpMasterKeyRequest("own")));

        verify(internal, loggerSink);
    }
}
