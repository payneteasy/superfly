package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.common.SuperflyProperties;
import org.junit.After;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import static org.junit.Assert.assertEquals;

public class SpringAppPropertiesConfigurationTest {

    @After
    public void clear() {
        System.clearProperty("superfly-max-otp-failed");
    }

    private SuperflyProperties load(String maxLogins, String maxOtp) {
        MockServletContext ctx = new MockServletContext();
        if (maxLogins != null) {
            ctx.addInitParameter("superfly-max-logins-failed", maxLogins);
        }
        if (maxOtp != null) {
            ctx.addInitParameter("superfly-max-otp-failed", maxOtp);
        }
        ctx.addInitParameter("superfly-cryptoSecret", "dummy");
        ctx.addInitParameter("superfly-cryptoSalt", "dummy");
        return new SpringAppPropertiesConfiguration(new ApplicationParameterResolver(ctx)).superflyProperties();
    }

    @Test
    public void otpThresholdDefaultsToLoginsThreshold() {
        SuperflyProperties p = load("4", null);
        assertEquals(Long.valueOf(4), p.maxOtpFailed());
    }

    @Test
    public void otpThresholdDefaultsToSixWhenNothingSet() {
        assertEquals(Long.valueOf(6), load(null, null).maxOtpFailed());
    }

    @Test
    public void otpThresholdIsIndependent() {
        SuperflyProperties p = load("6", "3");
        assertEquals(Long.valueOf(3), p.maxOtpFailed());
        assertEquals(Long.valueOf(6), p.maxLoginsFailed());
    }
}
