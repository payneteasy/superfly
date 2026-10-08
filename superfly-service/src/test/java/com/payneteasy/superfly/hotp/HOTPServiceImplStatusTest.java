package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.CheckOtpResult.Status;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.service.UserService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

/**
 * Codes of steps ±1 are accepted once, ±2..±3 are reported as clock skew without being stored, the rest is invalid.
 */
public class HOTPServiceImplStatusTest {

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

    private int codeOf(long step) {
        return service.getGoogleAuthenticator().get().getTotpPassword(otpSecret, step * STEP_MILLIS);
    }

    private String codeOfStep(long step) {
        return String.format("%06d", codeOf(step));
    }

    /** Skips the test in the unlikely case the code of the step equals a code of another step of the ±3 window. */
    private void assumeUniqueInSkewWindow(long step) {
        for (long other = STEP - 3; other <= STEP + 3; other++) {
            if (other != step) {
                assumeTrue(codeOf(other) != codeOf(step));
            }
        }
    }

    private void expectKeyLookup() {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(encryptedKey);
    }

    private void expectKeyLookupAndMark(long step, boolean stored) {
        expectKeyLookup();
        EasyMock.expect(userService.markOtpStepUsed(USER, step)).andReturn(stored);
    }

    @Test
    public void malformedCodeIsInvalidWithoutKeyLookup() throws Exception {
        EasyMock.replay(userService);

        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, null));
        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, "12345"));
        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, "abcdef"));

        EasyMock.verify(userService);
    }

    @Test
    public void codeOfCurrentStepSucceeds() throws Exception {
        assumeUniqueInSkewWindow(STEP);
        expectKeyLookupAndMark(STEP, true);
        EasyMock.replay(userService);

        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOfStep(STEP)));

        EasyMock.verify(userService);
    }

    @Test
    public void codesOfStepsPlusMinusOneSucceed() throws Exception {
        assumeUniqueInSkewWindow(STEP - 1);
        assumeUniqueInSkewWindow(STEP + 1);
        expectKeyLookupAndMark(STEP - 1, true);
        expectKeyLookupAndMark(STEP + 1, true);
        EasyMock.replay(userService);

        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOfStep(STEP - 1)));
        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOfStep(STEP + 1)));

        EasyMock.verify(userService);
    }

    @Test
    public void codeOfUsedStepIsAlreadyUsed() throws Exception {
        assumeUniqueInSkewWindow(STEP);
        expectKeyLookupAndMark(STEP, true);
        expectKeyLookupAndMark(STEP, false);
        EasyMock.replay(userService);

        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOfStep(STEP)));
        assertEquals(Status.ALREADY_USED, service.validateGoogleTimePassword(USER, codeOfStep(STEP)));

        EasyMock.verify(userService);
    }

    @Test
    public void codesOfStepsPlusMinusTwoAndThreeAreClockSkewWithoutStoring() throws Exception {
        long[] skewed = {STEP - 3, STEP - 2, STEP + 2, STEP + 3};
        for (long step : skewed) {
            assumeUniqueInSkewWindow(step);
            expectKeyLookup();
        }
        // a strict mock: any markOtpStepUsed call fails the test
        EasyMock.replay(userService);

        for (long step : skewed) {
            assertEquals("step offset " + (step - STEP), Status.CLOCK_SKEW,
                    service.validateGoogleTimePassword(USER, codeOfStep(step)));
        }

        EasyMock.verify(userService);
    }

    @Test
    public void codesOfStepsPlusMinusFourAreInvalid() throws Exception {
        for (long step : new long[]{STEP - 4, STEP + 4}) {
            assumeUniqueInSkewWindow(step);
            expectKeyLookup();
        }
        EasyMock.replay(userService);

        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, codeOfStep(STEP - 4)));
        assertEquals(Status.INVALID, service.validateGoogleTimePassword(USER, codeOfStep(STEP + 4)));

        EasyMock.verify(userService);
    }

    @Test
    public void clockSkewCodeSucceedsOnceTheClockCatchesUp() throws Exception {
        assumeUniqueInSkewWindow(STEP + 2);
        expectKeyLookup();
        expectKeyLookupAndMark(STEP + 2, true);
        EasyMock.replay(userService);

        assertEquals(Status.CLOCK_SKEW, service.validateGoogleTimePassword(USER, codeOfStep(STEP + 2)));
        clock.addAndGet(STEP_MILLIS);
        assertEquals(Status.SUCCESS, service.validateGoogleTimePassword(USER, codeOfStep(STEP + 2)));

        EasyMock.verify(userService);
    }
}
