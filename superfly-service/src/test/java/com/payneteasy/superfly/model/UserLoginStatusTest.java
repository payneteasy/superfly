package com.payneteasy.superfly.model;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author rpuch
 */
public class UserLoginStatusTest extends TestCase {
    public void test() {
        Assert.assertEquals("Y", UserLoginStatus.SUCCESS.getDbStatus());
        Assert.assertEquals("N", UserLoginStatus.FAILED.getDbStatus());
        Assert.assertEquals("T", UserLoginStatus.TEMP_PASSWORD.getDbStatus());

        Assert.assertSame(UserLoginStatus.SUCCESS, UserLoginStatus.findByDbStatus("Y"));
        Assert.assertSame(UserLoginStatus.FAILED, UserLoginStatus.findByDbStatus("N"));
        Assert.assertSame(UserLoginStatus.TEMP_PASSWORD, UserLoginStatus.findByDbStatus("T"));

        try {
            UserLoginStatus.findByDbStatus("no-such-status");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
