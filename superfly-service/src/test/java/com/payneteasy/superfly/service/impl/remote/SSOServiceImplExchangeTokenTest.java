package com.payneteasy.superfly.service.impl.remote;

import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.api.request.ExchangeSubsystemTokenRequest;
import com.payneteasy.superfly.service.InternalSSOService;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * A subsystem token is bound to the subsystem that the caller authenticated as.
 */
public class SSOServiceImplExchangeTokenTest {
    private SSOServiceImpl     ssoService;
    private InternalSSOService internal;

    @Before
    public void setUp() {
        internal = createStrictMock(InternalSSOService.class);
        ssoService = new SSOServiceImpl(internal, null, null, null, null);
    }

    @Test
    public void passesCallerSubsystemToInternalService() {
        SSOUser user = new SSOUser("pete", Collections.emptyMap(), null);
        ssoService.setSubsystemIdentifierObtainer(hint -> "caller");
        expect(internal.exchangeSubsystemToken("token", "caller")).andReturn(user);
        replay(internal);

        assertSame(user, ssoService.exchangeSubsystemToken(new ExchangeSubsystemTokenRequest("token")));

        verify(internal);
    }

    @Test
    public void callerWithoutSubsystemGetsNullWithoutCallingInternalService() {
        ssoService.setSubsystemIdentifierObtainer(hint -> null);
        replay(internal);

        assertNull(ssoService.exchangeSubsystemToken(new ExchangeSubsystemTokenRequest("token")));

        verify(internal);
    }
}
