package com.payneteasy.superfly.service.impl;

import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.dao.SmtpServerDao;
import com.payneteasy.superfly.model.RoutineResult;
import com.payneteasy.superfly.model.SmtpServerPassword;
import com.payneteasy.superfly.model.ui.smtp_server.UISmtpServer;
import com.payneteasy.superfly.service.JavaMailSenderPool;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.SmtpServerService;
import org.easymock.Capture;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * SMTP server passwords reach the database only as CryptoService ciphertext; mail sending gets the plain password,
 * the UI models get none.
 */
public class SmtpServerPasswordEncryptionTest {

    private static final String PASSWORD = "fake-smtp-password";

    private CryptoServiceImpl crypto;
    private SmtpServerDao dao;
    private SmtpServerServiceImpl service;

    @Before
    public void setUp() {
        crypto = new CryptoServiceImpl("test-secret", "test-salt");
        dao = createStrictMock(SmtpServerDao.class);
        service = new SmtpServerServiceImpl();
        service.setSmtpServerDao(dao);
        service.setCryptoService(crypto);
        service.setLoggerSink(createNiceMock(LoggerSink.class));
        service.setJavaMailSenderPool(TrivialProxyFactory.createProxy(JavaMailSenderPool.class));
    }

    @Test
    public void createStoresOnlyCiphertext() {
        Capture<String> stored = newCapture();
        expect(dao.createSmtpServer(anyObject())).andAnswer(() -> {
            stored.setValue(((UISmtpServer) getCurrentArguments()[0]).getPassword());
            return RoutineResult.okResult();
        });
        replay(dao);
        UISmtpServer server = server(PASSWORD);

        assertTrue(service.createSmtpServer(server).isOk());

        verify(dao);
        assertTrue(stored.getValue().startsWith("v2:"));
        assertFalse(stored.getValue().contains(PASSWORD));
        assertEquals(PASSWORD, decrypt(stored.getValue()));
        assertNull("the page model must not keep the password", server.getPassword());
    }

    @Test
    public void updateStoresOnlyCiphertext() {
        Capture<String> stored = newCapture();
        expect(dao.updateSmtpServer(anyObject())).andAnswer(() -> {
            stored.setValue(((UISmtpServer) getCurrentArguments()[0]).getPassword());
            return RoutineResult.okResult();
        });
        replay(dao);
        UISmtpServer server = server(PASSWORD);

        assertTrue(service.updateSmtpServer(server).isOk());

        verify(dao);
        assertEquals(PASSWORD, decrypt(stored.getValue()));
        assertNull(server.getPassword());
    }

    @Test
    public void updateWithEmptyPasswordPassesNullToKeepStoredPassword() {
        for (String empty : new String[]{null, ""}) {
            reset(dao);
            expect(dao.updateSmtpServer(anyObject())).andAnswer(() -> {
                assertNull(((UISmtpServer) getCurrentArguments()[0]).getPassword());
                return RoutineResult.okResult();
            });
            replay(dao);

            assertTrue(service.updateSmtpServer(server(empty)).isOk());

            verify(dao);
        }
    }

    @Test
    public void passwordWhoseCiphertextDoesNotFitIsRejected() {
        replay(dao);
        UISmtpServer server = server("x".repeat(200));

        RoutineResult result = service.createSmtpServer(server);

        verify(dao);
        assertFalse(result.isOk());
        assertNull(server.getPassword());
    }

    @Test
    public void serverForDisplayHasNoPassword() throws Exception {
        expect(dao.getSmtpServer(1L)).andReturn(server(crypto.encrypt(PASSWORD)));
        replay(dao);

        assertNull(service.getSmtpServer(1L).getPassword());
    }

    @Test
    public void serverWithPasswordHasDecryptedPassword() throws Exception {
        expect(dao.getSmtpServer(1L)).andReturn(server(crypto.encrypt(PASSWORD)));
        expect(dao.getSmtpServerBySubsystemIdentifier("billing")).andReturn(server(crypto.encrypt(PASSWORD)));
        replay(dao);

        assertEquals(PASSWORD, service.getSmtpServerWithPassword(1L).getPassword());
        assertEquals(PASSWORD, service.getSmtpServerBySubsystemIdentifier("billing").getPassword());
    }

    @Test
    public void notYetMigratedPlainPasswordIsUsedAsIs() {
        expect(dao.getSmtpServer(1L)).andReturn(server(PASSWORD));
        expect(dao.getSmtpServerBySubsystemIdentifier("billing")).andReturn(server(PASSWORD));
        replay(dao);

        assertEquals(PASSWORD, service.getSmtpServerWithPassword(1L).getPassword());
        assertEquals(PASSWORD, service.getSmtpServerBySubsystemIdentifier("billing").getPassword());
    }

    @Test
    public void missingServerOrPasswordIsReturnedAsIs() {
        expect(dao.getSmtpServer(1L)).andReturn(null);
        expect(dao.getSmtpServer(2L)).andReturn(server(null));
        replay(dao);

        assertNull(service.getSmtpServerWithPassword(1L));
        assertNull(service.getSmtpServerWithPassword(2L).getPassword());
    }

    @Test
    public void undecryptablePasswordFailsWithoutRevealingIt() throws Exception {
        String foreign = new CryptoServiceImpl("other-secret", "other-salt").encrypt(PASSWORD);
        expect(dao.getSmtpServer(1L)).andReturn(server(foreign));
        replay(dao);

        try {
            service.getSmtpServerWithPassword(1L);
            fail();
        } catch (IllegalStateException e) {
            assertFalse(e.getMessage().contains(PASSWORD));
            assertFalse(e.getMessage().contains(foreign));
        }
    }

    @Test
    public void mailSenderReadsTheServerWithItsPassword() {
        SmtpServerService smtpService = createStrictMock(SmtpServerService.class);
        // no password: validation stops the sender before a mail session is created (no mail provider in tests)
        expect(smtpService.getSmtpServerWithPassword(1L)).andReturn(server(null));
        replay(smtpService);
        JavaMailSenderPoolImpl pool = new JavaMailSenderPoolImpl();
        pool.setSmtpServerService(smtpService);

        try {
            pool.get(1L);
            fail();
        } catch (NullPointerException e) {
            assertEquals("'password' cannot be empty", e.getMessage());
        }

        verify(smtpService);
    }

    @Test
    public void startupTaskEncryptsPlainPasswordsAndSurvivesAFailure() {
        SmtpServerDao taskDao = createStrictMock(SmtpServerDao.class);
        expect(taskDao.getSmtpServersWithPlainPassword())
                .andReturn(List.of(plain(1L, "broken"), plain(2L, "main")));
        taskDao.encryptSmtpServerPassword(eq(1L), anyString());
        expectLastCall().andThrow(new IllegalStateException("db down"));
        Capture<String> stored = newCapture();
        taskDao.encryptSmtpServerPassword(eq(2L), capture(stored));
        replay(taskDao);

        new SmtpServerPasswordEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
        assertTrue(stored.getValue().startsWith("v2:"));
        assertEquals(PASSWORD, decrypt(stored.getValue()));
    }

    @Test
    public void startupTaskWithNothingToDoWritesNothing() {
        SmtpServerDao taskDao = createStrictMock(SmtpServerDao.class);
        expect(taskDao.getSmtpServersWithPlainPassword()).andReturn(List.of());
        replay(taskDao);

        new SmtpServerPasswordEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
    }

    @Test
    public void startupTaskDoesNotFailTheStartWhenListingFails() {
        SmtpServerDao taskDao = createStrictMock(SmtpServerDao.class);
        expect(taskDao.getSmtpServersWithPlainPassword()).andThrow(new IllegalStateException("db down"));
        replay(taskDao);

        new SmtpServerPasswordEncryptionTask(taskDao, crypto).afterSingletonsInstantiated();

        verify(taskDao);
    }

    private String decrypt(String ciphertext) {
        try {
            return crypto.decrypt(ciphertext);
        } catch (Exception e) {
            throw new AssertionError("not a valid ciphertext", e);
        }
    }

    private static UISmtpServer server(String password) {
        UISmtpServer server = new UISmtpServer();
        server.setId(1L);
        server.setName("main");
        server.setHost("smtp.example.com");
        server.setUsername("mailer");
        server.setPassword(password);
        return server;
    }

    private static SmtpServerPassword plain(long id, String name) {
        SmtpServerPassword result = new SmtpServerPassword();
        result.setId(id);
        result.setName(name);
        result.setPassword(PASSWORD);
        return result;
    }
}
