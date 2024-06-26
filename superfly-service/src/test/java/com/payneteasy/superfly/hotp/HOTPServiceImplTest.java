package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.api.SsoDecryptException;
import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.crypto.exception.EncryptException;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.service.impl.UserServiceImpl;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HOTPServiceImplTest {
    public static final String USERNAME = "user";
    private HOTPServiceImpl service;
    private GoogleAuthenticatorKey credentials;

    @Before
    public void setup() {
        service = new HOTPServiceImpl();
        credentials = service.getGoogleAuthenticator().get().createCredentials();

        CryptoServiceImpl cryptoService = new CryptoServiceImpl();
        cryptoService.setCryptoSalt("GOOGLE_SALT");
        cryptoService.setCryptoSecret("GOOGLE_SECRET");

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

        boolean valid = service.validateGoogleTimePassword(USERNAME, totpPassword);

        Assert.assertTrue( "Not valid code", valid);
    }

    @Test
    public void testUnValidateGoogleTimePassword() throws SsoDecryptException {
        String totpPassword = "123123";

        boolean valid = service.validateGoogleTimePassword(USERNAME, totpPassword);

        Assert.assertFalse( "Valid code", valid);
    }
}