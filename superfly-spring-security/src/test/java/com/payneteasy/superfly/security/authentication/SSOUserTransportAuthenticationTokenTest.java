package com.payneteasy.superfly.security.authentication;

import com.payneteasy.superfly.api.SSOUser;
import org.junit.Assert;
import org.junit.Test;

public class SSOUserTransportAuthenticationTokenTest {
    @Test
    public void testGetName() {
        SSOUserTransportAuthenticationToken token = new SSOUserTransportAuthenticationToken(new SSOUser("user", null, null));
        Assert.assertEquals("user", token.getName());
    }
}
