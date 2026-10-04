package com.payneteasy.superfly.hotp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HOTPServiceImplOtpAuthUriTest {

    private final HOTPServiceImpl service = new HOTPServiceImpl();

    @Test
    public void returnsLocalOtpAuthUri() {
        String uri = service.getUrlToGoogleAuthQrCode("ABCDEF", "Superfly", "alice");
        assertEquals("otpauth://totp/Superfly:alice?secret=ABCDEF&issuer=Superfly", uri);
        assertFalse(uri.contains("http"));
        assertFalse(uri.contains("qrserver"));
        assertFalse(uri.contains("google"));
    }

    @Test
    public void nullIssuerMatchesPaynetMockFormat() {
        assertEquals("otpauth://totp/alice?secret=ABCDEF", service.getUrlToGoogleAuthQrCode("ABCDEF", null, "alice"));
    }

    @Test
    public void spaceAndSpecialCharsArePercentEncoded() {
        String uri = service.getUrlToGoogleAuthQrCode("ABCDEF", "My Corp&Co", "a b@x.com");
        assertEquals("otpauth://totp/My%20Corp%26Co:a%20b%40x.com?secret=ABCDEF&issuer=My%20Corp%26Co", uri);
        assertTrue(uri.startsWith("otpauth://totp/"));
        assertFalse(uri.contains("+"));
    }
}
