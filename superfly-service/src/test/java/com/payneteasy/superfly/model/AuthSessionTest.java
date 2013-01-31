package com.payneteasy.superfly.model;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author rpuch
 */
public class AuthSessionTest extends TestCase {
    public void test() {
        Assert.assertTrue(new AuthSession("abc", 1L).equals(new AuthSession("abc", 1L)));
        Assert.assertTrue(new AuthSession("abc", null).equals(new AuthSession("abc", null)));
        Assert.assertFalse(new AuthSession("abc", 1L).equals(new AuthSession("abc", null)));
        Assert.assertFalse(new AuthSession("abc", null).equals(new AuthSession("abc", 1L)));
        Assert.assertFalse(new AuthSession("abc", null).equals(null));
    }
}
