package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;
import org.junit.Assert;
import org.junit.Test;

public class CheckHOTPTokenTest {
    @Test
    public void testGetCredentials() {
        CheckOTPToken token = new CheckOTPToken(new SSOUser("user", null, null), "hotp");
        Assert.assertEquals("hotp", token.getCredentials());
    }
}
