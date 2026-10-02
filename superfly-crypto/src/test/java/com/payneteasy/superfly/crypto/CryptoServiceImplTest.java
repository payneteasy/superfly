package com.payneteasy.superfly.crypto;

import com.payneteasy.superfly.crypto.exception.DecryptException;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.*;

public class CryptoServiceImplTest {
    private static final String SECRET = "test-secret";
    private static final String SALT   = "test-salt";
    private static final String PLAIN  = "JBSWY3DPEHPK3PXP";

    // produced by the pre-GCM implementation (AES-CBC, zero IV) with SECRET/SALT/PLAIN
    private static final String LEGACY_CIPHERTEXT = "I/67rO8JQdPA4g30y8I7Qy05tbazsiaxSOmgzemj6oE=";

    private final CryptoServiceImpl service = new CryptoServiceImpl(SECRET, SALT);

    @Test
    public void gcmRoundtrip() throws Exception {
        String encrypted = service.encrypt(PLAIN);
        assertTrue(encrypted.startsWith("v2:"));
        assertEquals(PLAIN, service.decrypt(encrypted));
    }

    @Test
    public void encryptionIsRandomized() throws Exception {
        assertNotEquals(service.encrypt(PLAIN), service.encrypt(PLAIN));
    }

    @Test
    public void tamperedCiphertextIsRejected() throws Exception {
        byte[] raw = Base64.getDecoder().decode(service.encrypt(PLAIN).substring(3));
        raw[raw.length - 1] ^= 1;
        try {
            service.decrypt("v2:" + Base64.getEncoder().encodeToString(raw));
            fail("tampered ciphertext must not decrypt");
        } catch (DecryptException expected) {
            // ok
        }
    }

    @Test(expected = DecryptException.class)
    public void truncatedCiphertextIsRejected() throws Exception {
        service.decrypt("v2:" + Base64.getEncoder().encodeToString(new byte[5]));
    }

    @Test
    public void legacyCiphertextIsStillDecrypted() throws Exception {
        assertEquals(PLAIN, service.decrypt(LEGACY_CIPHERTEXT));
    }

    @Test
    public void requireConfiguredRejectsMissingBlankAndPlaceholders() {
        assertRejected(null, SALT, "SECRET_VAR");
        assertRejected(SECRET, "  ", "SALT_VAR");
        assertRejected("GOOGLE_AUTH_OTP_SECRET", SALT, "SECRET_VAR");
        assertRejected(SECRET, "GOOGLE_AUTH_OTP_SALT", "SALT_VAR");
    }

    @Test
    public void requireConfiguredAcceptsRealValues() {
        CryptoServiceImpl.requireConfigured("SECRET_VAR", SECRET, "SALT_VAR", SALT);
    }

    private static void assertRejected(String secret, String salt, String expectedName) {
        try {
            CryptoServiceImpl.requireConfigured("SECRET_VAR", secret, "SALT_VAR", salt);
            fail("expected IllegalStateException");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains(expectedName));
            assertFalse(e.getMessage().contains("GOOGLE_AUTH_OTP_S"));
        }
    }
}
