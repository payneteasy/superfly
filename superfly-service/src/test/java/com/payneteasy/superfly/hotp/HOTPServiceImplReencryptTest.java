package com.payneteasy.superfly.hotp;

import com.payneteasy.superfly.crypto.CryptoServiceImpl;
import com.payneteasy.superfly.service.UserService;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HOTPServiceImplReencryptTest {

    private static final String SECRET = "test-secret";
    private static final String SALT = "test-salt";
    private static final String USER = "user";

    private HOTPServiceImpl service;
    private CryptoServiceImpl crypto;
    private UserService userService;
    private String otpSecret;

    @Before
    public void setUp() {
        service = new HOTPServiceImpl();
        crypto = new CryptoServiceImpl(SECRET, SALT);
        userService = EasyMock.createStrictMock(UserService.class);
        service.setCryptoService(crypto);
        service.setUserService(userService);
        otpSecret = service.getGoogleAuthenticator().get().createCredentials().getKey();
    }

    private String validCode() {
        return String.format("%06d", service.getGoogleAuthenticator().get().getTotpPassword(otpSecret));
    }

    private static String legacyEncrypt(String plain) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] key = factory.generateSecret(new PBEKeySpec(SECRET.toCharArray(), SALT.getBytes(StandardCharsets.UTF_8), 65536, 256)).getEncoded();
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(new byte[16]));
        return Base64.getEncoder().encodeToString(cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void validCodeReencryptsLegacyKey() throws Exception {
        String legacy = legacyEncrypt(otpSecret);
        Capture<String> saved = Capture.newInstance();
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(legacy);
        userService.persistOtpMasterKeyForUsername(EasyMock.eq(USER), EasyMock.capture(saved));
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, validCode()));

        EasyMock.verify(userService);
        assertFalse(crypto.isLegacy(saved.getValue()));
        assertEquals(otpSecret, crypto.decrypt(saved.getValue()));
    }

    @Test
    public void wrongCodeSavesNothing() throws Exception {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(legacyEncrypt(otpSecret));
        EasyMock.replay(userService);

        String wrong = String.format("%06d", (Integer.parseInt(validCode()) + 500000) % 1000000);
        assertFalse(service.validateGoogleTimePassword(USER, wrong));

        EasyMock.verify(userService);
    }

    @Test
    public void v2KeyIsNotResaved() throws Exception {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(crypto.encrypt(otpSecret));
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, validCode()));

        EasyMock.verify(userService);
    }

    @Test
    public void persistFailureDoesNotBreakValidation() throws Exception {
        EasyMock.expect(userService.getOtpMasterKeyByUsername(USER)).andReturn(legacyEncrypt(otpSecret));
        userService.persistOtpMasterKeyForUsername(EasyMock.eq(USER), EasyMock.anyString());
        EasyMock.expectLastCall().andThrow(new RuntimeException("db down"));
        EasyMock.replay(userService);

        assertTrue(service.validateGoogleTimePassword(USER, validCode()));

        EasyMock.verify(userService);
    }
}
