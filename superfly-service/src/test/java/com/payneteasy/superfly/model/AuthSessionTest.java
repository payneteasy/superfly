package com.payneteasy.superfly.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author rpuch
 */
public class AuthSessionTest {
    @Test
    public void test() {
        assertTrue(new AuthSession("abc", 1L).equals(new AuthSession("abc", 1L)));
        assertTrue(new AuthSession("abc", null).equals(new AuthSession("abc", null)));
        assertFalse(new AuthSession("abc", 1L).equals(new AuthSession("abc", null)));
        assertFalse(new AuthSession("abc", null).equals(new AuthSession("abc", 1L)));
        //noinspection ObjectEqualsNull
        assertFalse(new AuthSession("abc", null).equals(null));
    }
}
