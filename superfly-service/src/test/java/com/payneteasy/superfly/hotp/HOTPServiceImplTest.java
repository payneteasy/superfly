package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.exceptions.SsoDecryptException;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.crypto.exception.EncryptException;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.service.impl.UserServiceImpl;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class HOTPServiceImplTest {
    public static final String USERNAME = "user";
    private HOTPServiceImpl service;
    private GoogleAuthenticatorKey credentials;

    @Before
    public void setup() {
        service = new HOTPServiceImpl();
        credentials = service.getGoogleAuthenticator().get().createCredentials();

        CryptoServiceImpl cryptoService = new CryptoServiceImpl(
                "GOOGLE_AUTH_OTP_SALT",
                "GOOGLE_AUTH_OTP_SECRET"
        );

        UserService userService = new UserServiceImpl() {
            @Override
            public String getOtpMasterKeyByUsername(String username) {
                if (USERNAME.equals(username)) {
                    try {
                        return cryptoService.encrypt(credentials.getKey());
                    } catch (EncryptException e) {
                        throw new RuntimeException(e);
                    }
                }
                return null;
            }
        };
        service.setCryptoService(cryptoService);
        service.setUserService(userService);
    }

    @Test
    public void testValidateGoogleTimePassword() throws SsoDecryptException {
        String totpPassword = String.valueOf(
                service.getGoogleAuthenticator().get().getTotpPassword(credentials.getKey())
        );

        CheckOtpResult.Status valid = service.validateGoogleTimePassword(USERNAME, totpPassword);

        Assert.assertEquals( "Not valid code", CheckOtpResult.Status.SUCCESS, valid);
    }

    @Test
    public void testUnValidateGoogleTimePassword() throws SsoDecryptException {
        String totpPassword = "123123";

        CheckOtpResult.Status valid = service.validateGoogleTimePassword(USERNAME, totpPassword);

        Assert.assertNotEquals( "Valid code", CheckOtpResult.Status.SUCCESS, valid);
    }
}
