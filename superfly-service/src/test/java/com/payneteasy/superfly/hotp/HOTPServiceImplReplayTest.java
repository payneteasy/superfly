package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HOTPServiceImplReplayTest {

    private static final String USER = "user";
    private static final long STEP_MILLIS = 30_000L;
    private static final long NOW = 1_000_000 * STEP_MILLIS + 5_000;
    private static final long STEP = NOW / STEP_MILLIS;

    private HOTPServiceImpl service;
    private UserService userService;
    private AtomicLong clock;
    private String otpSecret;
    private String encryptedKey;

    @Before
    public void setUp() throws Exception {
        CryptoServiceImpl crypto = new CryptoServiceImpl("test-secret", "test-salt");
        service = new HOTPServiceImpl();
        service.setCryptoService(crypto);
        userService = EasyMock.createStrictMock(UserService.class);
        service.setUserService(userService);
        clock = new AtomicLong(NOW);
        service.setClock(clock::get);
        otpSecret = service.getGoogleAuthenticator().get().createCredentials().getKey();
        encryptedKey = crypto.encrypt(otpSecret);
    }

    private String codeOfStep(long step) {
        return String.format("%06d", service.getGoogleAuthenticator().get().getTotpPassword(otpSecret, step * STEP_MILLIS));
    }

    private void expectKeyLookupAndMark(long step, boolean stored) {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(encryptedKey);
        EasyMock.expect(userService.markOtpStepUsed(USER, step)).andReturn(stored);
    }

    @Test
    public void freshCodeIsAcceptedAndItsStepStored() throws Exception {
        expectKeyLookupAndMark(STEP, true);
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, codeOfStep(STEP)));

        EasyMock.verify(userService);
    }

    @Test
    public void replayedCodeIsRejected() throws Exception {
        expectKeyLookupAndMark(STEP, true);
        expectKeyLookupAndMark(STEP, false);
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, codeOfStep(STEP)));
        assertFalse(service.validateGoogleTimePassword(USER, codeOfStep(STEP)));

        EasyMock.verify(userService);
    }

    @Test
    public void codeOfNextStepIsAcceptedAfterPrevious() throws Exception {
        expectKeyLookupAndMark(STEP, true);
        expectKeyLookupAndMark(STEP + 1, true);
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, codeOfStep(STEP)));
        clock.addAndGet(STEP_MILLIS);
        assertTrue(service.validateGoogleTimePassword(USER, codeOfStep(STEP + 1)));

        EasyMock.verify(userService);
    }

    @Test
    public void codeOfEarlierStepInWindowStoresThatStep() throws Exception {
        expectKeyLookupAndMark(STEP - 1, true);
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, codeOfStep(STEP - 1)));

        EasyMock.verify(userService);
    }

    @Test
    public void codeOutsideWindowIsRejectedWithoutStoring() throws Exception {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(encryptedKey);
        EasyMock.replay(userService);

        assertFalse(service.validateGoogleTimePassword(USER, codeOfStep(STEP + 5)));

        EasyMock.verify(userService);
    }
}
