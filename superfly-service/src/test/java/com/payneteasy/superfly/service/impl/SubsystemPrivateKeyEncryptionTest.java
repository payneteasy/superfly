package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SubsystemPrivateKey;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.NotificationService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.impl.remote.check.KeyPairData;
import com.payneteasy.superfly.service.impl.remote.check.RemoteAuthEncryptionAlgorithm;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Subsystem private keys reach the database only as CryptoService ciphertext; the startup task converts old rows.
 */
public class SubsystemPrivateKeyEncryptionTest {

    private static final String PEM = "-----BEGIN PRIVATE KEY-----\nfake-key-body\n-----END PRIVATE KEY-----";

    private CryptoServiceImpl crypto;
    private SubsystemDao dao;
    private SubsystemServiceImpl service;

    @Before
    public void setUp() {
        crypto = new CryptoServiceImpl("test-secret", "test-salt");
        dao = createStrictMock(SubsystemDao.class);
        service = new SubsystemServiceImpl();
        service.setSubsystemDao(dao);
        service.setCryptoService(crypto);
        service.setNotificationService(TrivialProxyFactory.createProxy(NotificationService.class));
        service.setLoggerSink(createNiceMock(LoggerSink.class));
        service.setJavaMailSenderPool(TrivialProxyFactory.createProxy(JavaMailSenderPool.class));
    }

    @Test
    public void createStoresOnlyCiphertext() throws Exception {
        Capture<UISubsystem> saved = newCapture();
        expect(dao.createSubsystem(capture(saved))).andAnswer(() -> {
            assertTrue(saved.getValue().getPrivateKey().startsWith("v2:"));
            assertFalse(saved.getValue().getPrivateKey().contains("fake-key-body"));
            assertEquals(PEM, crypto.decrypt(saved.getValue().getPrivateKey()));
            return RoutineResult.okResult();
        });
        replay(dao);
        UISubsystem subsystem = subsystem(PEM);

        service.createSubsystem(subsystem);

        verify(dao);
        assertNotEquals("the page model must not keep the plain key", PEM, subsystem.getPrivateKey());
    }

    @Test
    public void updateLeavesNoPlainKeyInTheModel() {
        expect(dao.updateSubsystem(anyObject())).andReturn(RoutineResult.okResult());
        replay(dao);
        UISubsystem subsystem = subsystem(PEM);

        service.updateSubsystem(subsystem);

        assertNotEquals(PEM, subsystem.getPrivateKey());
    }

    @Test
    public void generatedKeyPairHasEncryptedPrivateKey() throws Exception {
        RemoteAuthCryptoService remoteCrypto = createNiceMock(RemoteAuthCryptoService.class);
        expect(remoteCrypto.generateKeyPair(anyObject())).andReturn(new KeyPairData("public", PEM));
        replay(remoteCrypto);
        service.setRemoteAuthCryptoService(remoteCrypto);

        KeyPairData pair = service.generateKeyPair(RemoteAuthEncryptionAlgorithm.RSA_OAEP);

        assertEquals("public", pair.publicKey());
        assertTrue(pair.privateKey().startsWith("v2:"));
        assertEquals(PEM, crypto.decrypt(pair.privateKey()));
    }

    @Test
    public void alreadyEncryptedKeyIsNotEncryptedTwice() throws Exception {
        String encrypted = crypto.encrypt(PEM);
        expect(dao.updateSubsystem(anyObject())).andAnswer(() -> {
            assertEquals(encrypted, ((UISubsystem) getCurrentArguments()[0]).getPrivateKey());
            return RoutineResult.okResult();
        });
        replay(dao);

        service.updateSubsystem(subsystem(encrypted));

        verify(dao);
    }

    @Test
    public void updateStoresOnlyCiphertext() throws Exception {
        expect(dao.updateSubsystem(anyObject())).andAnswer(() -> {
            UISubsystem arg = (UISubsystem) getCurrentArguments()[0];
            assertEquals(PEM, crypto.decrypt(arg.getPrivateKey()));
            return RoutineResult.okResult();
        });
        replay(dao);

        service.updateSubsystem(subsystem(PEM));

        verify(dao);
    }

    @Test
    public void updateWithoutNewKeyPassesNullToKeepStoredKey() {
        expect(dao.updateSubsystem(anyObject())).andAnswer(() -> {
            assertNull(((UISubsystem) getCurrentArguments()[0]).getPrivateKey());
            return RoutineResult.okResult();
        });
        replay(dao);

        service.updateSubsystem(subsystem(null));

        verify(dao);
    }

    @Test
    public void readDecryptsStoredKey() throws Exception {
        expect(dao.getSubsystemPrivateKey("billing")).andReturn(crypto.encrypt(PEM));
        replay(dao);

        assertEquals(PEM, service.getSubsystemPrivateKey("billing"));
    }

    @Test
    public void readAcceptsNotYetMigratedPlainKey() {
        expect(dao.getSubsystemPrivateKey("billing")).andReturn(PEM);
        replay(dao);

        assertEquals(PEM, service.getSubsystemPrivateKey("billing"));
    }

    @Test
    public void readOfMissingKeyGivesNull() {
        expect(dao.getSubsystemPrivateKey("billing")).andReturn(null);
        replay(dao);

        assertNull(service.getSubsystemPrivateKey("billing"));
    }

    @Test
    public void startupTaskEncryptsPlainKeysAndSurvivesAFailure() throws Exception {
        SubsystemDao taskDao = EasyMock.createStrictMock(SubsystemDao.class);
        expect(taskDao.getSubsystemsWithPlainPrivateKey())
                .andReturn(List.of(plain(1L, "broken", PEM), plain(2L, "billing", PEM)));
        taskDao.encryptSubsystemPrivateKey(eq(1L), anyString());
        expectLastCall().andThrow(new IllegalStateException("db down"));
        Capture<String> stored = newCapture();
        taskDao.encryptSubsystemPrivateKey(eq(2L), capture(stored));
        replay(taskDao);

        new SubsystemPrivateKeyEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
        assertEquals(PEM, crypto.decrypt(stored.getValue()));
    }

    @Test
    public void startupTaskWithNothingToDoWritesNothing() {
        SubsystemDao taskDao = EasyMock.createStrictMock(SubsystemDao.class);
        expect(taskDao.getSubsystemsWithPlainPrivateKey()).andReturn(List.of());
        replay(taskDao);

        new SubsystemPrivateKeyEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
    }

    @Test
    public void startupTaskDoesNotFailTheStartWhenListingFails() {
        SubsystemDao taskDao = EasyMock.createStrictMock(SubsystemDao.class);
        expect(taskDao.getSubsystemsWithPlainPrivateKey()).andThrow(new IllegalStateException("db down"));
        replay(taskDao);

        new SubsystemPrivateKeyEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
    }

    private static UISubsystem subsystem(String privateKey) {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName("billing");
        subsystem.setPrivateKey(privateKey);
        return subsystem;
    }

    private static SubsystemPrivateKey plain(long id, String name, String key) {
        SubsystemPrivateKey result = new SubsystemPrivateKey();
        result.setId(id);
        result.setName(name);
        result.setPrivateKey(key);
        return result;
    }
}
