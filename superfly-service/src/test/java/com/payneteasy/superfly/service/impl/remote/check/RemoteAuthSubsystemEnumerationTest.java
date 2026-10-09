package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.service.SubsystemService;
import org.junit.Test;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Unknown subsystem and wrong token must be indistinguishable (no subsystem enumeration).
 */
public class RemoteAuthSubsystemEnumerationTest {

    @Test
    public void unknownSubsystemAndWrongTokenGiveSameError() {
        SubsystemService subsystemService = createNiceMock(SubsystemService.class);
        SubsystemAuth billing = new SubsystemAuth();
        billing.setName("billing");
        billing.setSubsystemToken(SubsystemTokenHasher.hash("secret"));
        expect(subsystemService.getSubsystemAuth("billing")).andStubReturn(billing);
        expect(subsystemService.getSubsystemAuth("nope")).andStubReturn(null);
        replay(subsystemService);
        RemoteAuthServiceImpl service = new RemoteAuthServiceImpl(subsystemService,
                createNiceMock(InternalSSOService.class), createNiceMock(RemoteAuthCryptoService.class));

        RemoteAuthException unknown = failure(service, "nope", "secret");
        RemoteAuthException wrongToken = failure(service, "billing", "wrong");
        RemoteAuthException nullToken = failure(service, "billing", null);

        assertEquals("UNAUTHORIZED", unknown.getErrorCode());
        assertEquals(unknown.getMessage(), wrongToken.getMessage());
        assertEquals(unknown.getErrorCode(), wrongToken.getErrorCode());
        assertEquals(unknown.getMessage(), nullToken.getMessage());
    }

    private static RemoteAuthException failure(RemoteAuthServiceImpl service, String subsystem, String token) {
        try {
            service.checkPassword(subsystem, "u", "p", token, "127.0.0.1", "ua");
        } catch (RemoteAuthException e) {
            return e;
        }
        fail("must be rejected");
        return null;
    }
}
