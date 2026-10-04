package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.common.SuperflyProperties;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class SpringServiceConfigurationCryptoTest {
    private static SpringServiceConfiguration config(String secret, String salt) {
        return new SpringServiceConfiguration(new SuperflyProperties().cryptoSecret(secret).cryptoSalt(salt));
    }

    @Test(expected = IllegalStateException.class)
    public void startFailsWithoutSecret() {
        config(null, "test-salt").cryptoService();
    }

    @Test(expected = IllegalStateException.class)
    public void startFailsWithPlaceholderSalt() {
        config("test-secret", "GOOGLE_AUTH_OTP_SALT").cryptoService();
    }

    @Test
    public void startsWithConfiguredKey() {
        assertNotNull(config("test-secret", "test-salt").cryptoService());
    }
}
