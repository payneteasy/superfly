package com.payneteasy.superfly.utils;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SecureTokensTest {

    @Test
    public void generatesPrefixedBase64UrlTokenFittingColumn() {
        String token = SecureTokens.generate("SSO-");

        assertTrue(token, token.matches("SSO-[A-Za-z0-9_-]{43}"));
        // sso_sessions.identifier and subsystem_tokens.token are varchar(64)
        assertTrue(token.length() <= 64);
    }

    @Test
    public void generatesDistinctTokens() {
        Set<String> tokens = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            tokens.add(SecureTokens.generate("ST-"));
        }
        assertEquals(1000, tokens.size());
    }
}
