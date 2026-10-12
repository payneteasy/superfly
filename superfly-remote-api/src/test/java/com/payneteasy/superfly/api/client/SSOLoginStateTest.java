package com.payneteasy.superfly.api.client;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class SSOLoginStateTest {

    @Test
    public void testGeneratedStateIsUrlSafeAndUnique() {
        String first = SSOLoginState.generate();
        String second = SSOLoginState.generate();
        assertTrue(first, first.matches("^[A-Za-z0-9_-]{43}$"));
        assertNotEquals(first, second);
    }

    @Test
    public void testMatches() {
        assertTrue(SSOLoginState.matches("abc", "abc"));
        assertFalse(SSOLoginState.matches("abc", "abd"));
        assertFalse(SSOLoginState.matches("abc", "abcd"));
        assertFalse(SSOLoginState.matches(null, "abc"));
        assertFalse(SSOLoginState.matches("abc", null));
        assertFalse(SSOLoginState.matches(null, null));
        assertFalse(SSOLoginState.matches("", ""));
    }

    @Test
    public void testAppendTo() {
        assertEquals("https://sso/login?a=%2Fx&state=s1", SSOLoginState.appendTo("https://sso/login?a=%2Fx", "s1"));
        assertEquals("/login?state=s1", SSOLoginState.appendTo("/login", "s1"));
    }
}
