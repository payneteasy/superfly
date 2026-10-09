package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;

/**
 * The subsystem token is stored as a hash: the raw token is accepted, the stored value itself is not.
 */
public class RemoteAuthSubsystemTokenHashTest {

    private static final String RAW = "raw-subsystem-token";

    private SubsystemService subsystemService;
    private RemoteAuthServiceImpl service;

    @Before
    public void setUp() {
        subsystemService = createNiceMock(SubsystemService.class);
        service = new RemoteAuthServiceImpl(subsystemService, createNiceMock(InternalSSOService.class),
                createNiceMock(RemoteAuthCryptoService.class));
    }

    @Test
    public void rawTokenMatchingStoredHashPassesTokenCheck() {
        store(SubsystemTokenHasher.hash(RAW));

        // a later failure (no user, no key) is fine: only the token check is under test
        try {
            service.checkPassword("billing", "u", "p", RAW, "127.0.0.1", "ua");
        } catch (RemoteAuthException e) {
            assertNotEquals("UNAUTHORIZED", e.getErrorCode());
        }
    }

    @Test
    public void storedHashPresentedAsTokenIsRejected() {
        String hash = SubsystemTokenHasher.hash(RAW);
        store(hash);

        assertUnauthorized(hash);
    }

    @Test
    public void wrongTokenIsRejected() {
        store(SubsystemTokenHasher.hash(RAW));

        assertUnauthorized("other-token");
    }

    @Test
    public void plainStoredTokenIsRejected() {
        store(RAW);

        assertUnauthorized(RAW);
    }

    private void store(String stored) {
        UISubsystem billing = new UISubsystem();
        billing.setName("billing");
        billing.setSubsystemToken(stored);
        expect(subsystemService.getSubsystemByName("billing")).andStubReturn(billing);
        replay(subsystemService);
    }

    private void assertUnauthorized(String presented) {
        try {
            service.checkPassword("billing", "u", "p", presented, "127.0.0.1", "ua");
            fail("must be rejected");
        } catch (RemoteAuthException e) {
            assertEquals("UNAUTHORIZED", e.getErrorCode());
        }
    }
}
