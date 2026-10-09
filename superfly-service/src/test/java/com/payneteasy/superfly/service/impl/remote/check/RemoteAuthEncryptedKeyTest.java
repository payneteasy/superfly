package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.utils.SubsystemTokenHasher;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.SubsystemAuth;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthSession;
import com.payneteasy.superfly.service.impl.SubsystemServiceImpl;
import org.junit.Test;

import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotNull;

/**
 * Remote auth works with the key as it is stored: encrypted, decrypted only for the call.
 */
public class RemoteAuthEncryptedKeyTest {

    private static final String PEM = "-----BEGIN PRIVATE KEY-----\nfake-key-body\n-----END PRIVATE KEY-----";

    @Test
    public void checkPasswordDecryptsWithTheKeyFromEncryptedStorage() throws Exception {
        CryptoServiceImpl crypto = new CryptoServiceImpl("test-secret", "test-salt");
        SubsystemAuth auth = new SubsystemAuth();
        auth.setName("billing");
        auth.setSubsystemToken(SubsystemTokenHasher.hash("bearer"));
        auth.setEncryptionAlgorithm(RemoteAuthEncryptionAlgorithm.RSA.name());
        SubsystemDao dao = createNiceMock(SubsystemDao.class);
        expect(dao.getSubsystemAuth("billing")).andStubReturn(auth);
        expect(dao.getSubsystemPrivateKey("billing")).andStubReturn(crypto.encrypt(PEM));
        SubsystemServiceImpl subsystemService = new SubsystemServiceImpl();
        subsystemService.setSubsystemDao(dao);
        subsystemService.setCryptoService(crypto);

        RemoteAuthCryptoService remoteCrypto = createStrictMock(RemoteAuthCryptoService.class);
        expect(remoteCrypto.decryptPassword("enc-password", PEM, RemoteAuthEncryptionAlgorithm.RSA))
                .andReturn("password");
        InternalSSOService sso = createNiceMock(InternalSSOService.class);
        expect(sso.authenticate("admin", "password", "billing", "127.0.0.1", "agent"))
                .andReturn(new SSOUser("admin", Map.of(), Map.of()));
        replay(dao, remoteCrypto, sso);

        RemoteAuthSession session = new RemoteAuthServiceImpl(subsystemService, sso, remoteCrypto)
                .checkPassword("billing", "admin", "enc-password", "bearer", "127.0.0.1", "agent");

        verify(remoteCrypto, sso);
        assertNotNull(session.getSessionToken());
    }
}
