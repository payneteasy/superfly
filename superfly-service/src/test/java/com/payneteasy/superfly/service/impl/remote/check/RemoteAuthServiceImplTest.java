package com.payneteasy.superfly.service.impl.remote.check;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.api.SSOUser;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.RemoteAuthCryptoService;
import com.payneteasy.superfly.service.RemoteAuthService.RemoteAuthException;
import com.payneteasy.superfly.service.SubsystemService;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.BadPaddingException;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

/**
 * Сессионный токен remote-auth (ревью PR #125): одноразовый после успеха,
 * привязан к подсистеме и username, ограниченное число попыток OTP.
 */
public class RemoteAuthServiceImplTest {

    private static final String BILLING = "billing";
    private static final String CRM     = "crm";
    private static final String USER    = "admin";

    private SubsystemService subsystemService;
    private InternalSSOService internalSSOService;
    private RemoteAuthCryptoService cryptoService;
    private RemoteAuthServiceImpl service;

    @Before
    public void setUp() throws Exception {
        subsystemService = niceMock(SubsystemService.class);
        internalSSOService = niceMock(InternalSSOService.class);
        cryptoService = niceMock(RemoteAuthCryptoService.class);

        expect(subsystemService.getSubsystemByName(BILLING)).andStubReturn(subsystem(BILLING));
        expect(subsystemService.getSubsystemByName(CRM)).andStubReturn(subsystem(CRM));
        expect(cryptoService.decryptPassword(anyString(), anyString(), anyObject())).andStubReturn("password");
        expect(cryptoService.decryptOtp(eq("good-otp-enc"), anyString(), anyObject())).andStubReturn("123456");
        expect(cryptoService.decryptOtp(eq("bad-otp-enc"), anyString(), anyObject())).andStubReturn("000000");
        expect(cryptoService.decryptOtp(eq("garbage"), anyString(), anyObject()))
                .andStubThrow(new BadPaddingException("bad padding"));
        expect(internalSSOService.authenticate(eq(USER), eq("password"), anyString(), anyString(), anyString()))
                .andStubReturn(otpUser());
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, "123456")).andStubReturn(true);
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, USER, "000000")).andStubReturn(false);
        replay(subsystemService, internalSSOService, cryptoService);

        service = new RemoteAuthServiceImpl(subsystemService, internalSSOService, cryptoService);
    }

    @Test
    public void successfulOtpConsumesSessionToken() throws Exception {
        String token = checkPassword(BILLING);

        assertEquals("SUCCESS", checkOtp(BILLING, token, "good-otp-enc"));
        assertSessionRejected(BILLING, token, "good-otp-enc");
    }

    @Test
    public void sessionTokenIsBoundToSubsystem() throws Exception {
        String token = checkPassword(BILLING);

        assertSessionRejected(CRM, token, "good-otp-enc");
        // Чужая попытка сжигает токен — владелец тоже не сможет его использовать.
        assertSessionRejected(BILLING, token, "good-otp-enc");
    }

    @Test
    public void sessionTokenIsBoundToUsername() throws Exception {
        String token = checkPassword(BILLING);

        try {
            service.checkOtp(BILLING, "other", "good-otp-enc", token, token(BILLING));
            fail("username mismatch must be rejected");
        } catch (RemoteAuthException e) {
            assertEquals("BAD_USER_OR_PASSWORD_OR_OTP", e.getErrorCode());
        }
    }

    @Test
    public void wrongOtpCanBeRetriedUntilLimit() throws Exception {
        String token = checkPassword(BILLING);

        for (int i = 1; i < RemoteAuthServiceImpl.MAX_OTP_ATTEMPTS; i++) {
            assertEquals("BAD_USER_OR_PASSWORD_OR_OTP", checkOtp(BILLING, token, "bad-otp-enc"));
        }
        assertEquals("SUCCESS", checkOtp(BILLING, token, "good-otp-enc"));
    }

    @Test
    public void sessionIsInvalidatedAfterMaxFailedOtpAttempts() throws Exception {
        String token = checkPassword(BILLING);

        for (int i = 0; i < RemoteAuthServiceImpl.MAX_OTP_ATTEMPTS; i++) {
            assertEquals("BAD_USER_OR_PASSWORD_OR_OTP", checkOtp(BILLING, token, "bad-otp-enc"));
        }
        assertSessionRejected(BILLING, token, "good-otp-enc");
    }

    @Test
    public void undecryptableOtpCountsAsFailedAttempt() throws Exception {
        String token = checkPassword(BILLING);

        for (int i = 0; i < RemoteAuthServiceImpl.MAX_OTP_ATTEMPTS; i++) {
            try {
                checkOtp(BILLING, token, "garbage");
                fail("garbage OTP must not decrypt");
            } catch (RemoteAuthException e) {
                assertEquals("BAD_REQUEST", e.getErrorCode());
            }
        }
        assertSessionRejected(BILLING, token, "good-otp-enc");
    }

    private String checkPassword(String subsystemName) throws RemoteAuthException {
        return service.checkPassword(subsystemName, USER, "password-enc", token(subsystemName), "127.0.0.1", "test")
                .getSessionToken();
    }

    private String checkOtp(String subsystemName, String sessionToken, String otpEncrypted) throws RemoteAuthException {
        return service.checkOtp(subsystemName, USER, otpEncrypted, sessionToken, token(subsystemName));
    }

    private void assertSessionRejected(String subsystemName, String sessionToken, String otpEncrypted) {
        try {
            checkOtp(subsystemName, sessionToken, otpEncrypted);
            fail("session token must be rejected");
        } catch (RemoteAuthException e) {
            assertEquals("BAD_USER_OR_PASSWORD_OR_OTP", e.getErrorCode());
        }
    }

    private static String token(String subsystemName) {
        return subsystemName + "-bearer";
    }

    private static UISubsystem subsystem(String name) {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setName(name);
        subsystem.setSubsystemToken(token(name));
        subsystem.setPrivateKey("private-key");
        subsystem.setEncryptionAlgorithm(RemoteAuthEncryptionAlgorithm.RSA.name());
        return subsystem;
    }

    private static SSOUser otpUser() {
        SSOUser user = new SSOUser(USER, Map.of(), Map.of());
        user.setOtpType(OTPType.GOOGLE_AUTH);
        user.setOtpOptional(false);
        return user;
    }
}
