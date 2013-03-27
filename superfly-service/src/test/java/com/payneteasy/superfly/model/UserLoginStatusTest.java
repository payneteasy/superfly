package com.payneteasy.superfly.model;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * @author rpuch
 */
public class UserLoginStatusTest {
    @Test
    public void test() {
        assertEquals("Y", UserLoginStatus.SUCCESS.getDbStatus());
        assertEquals("N", UserLoginStatus.FAILED.getDbStatus());
        assertEquals("T", UserLoginStatus.TEMP_PASSWORD.getDbStatus());

        assertSame(UserLoginStatus.SUCCESS, UserLoginStatus.findByDbStatus("Y"));
        assertSame(UserLoginStatus.FAILED, UserLoginStatus.findByDbStatus("N"));
        assertSame(UserLoginStatus.TEMP_PASSWORD, UserLoginStatus.findByDbStatus("T"));

        try {
            UserLoginStatus.findByDbStatus("no-such-status");
            Assert.fail();
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
