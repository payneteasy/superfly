package com.payneteasy.superfly.password;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class Pbkdf2PasswordEncoderTest {

    @Test
    public void knownVectorMatchesPythonHashlib() {
        // python3: hashlib.pbkdf2_hmac('sha256', b'password', b'salt', 1000, 32).hex()
        assertEquals("pbkdf2-sha256$1000$632c2812e46d4604102ba7618e9d6d7d2f8128f6266b4a03264d2a0460b7dcb3",
                new Pbkdf2PasswordEncoder(1000).encode("password", "salt"));
    }

    @Test
    public void productionIterationsKnownVectorWithNonAsciiPassword() {
        // python3: hashlib.pbkdf2_hmac('sha256', 'пароль'.encode(), b'c3po', 600000, 32).hex()
        assertEquals("pbkdf2-sha256$600000$07835eba4ba1e9ad32107122d86a5e11a2ebf4862b30c92bf802b981571a8811",
                new Pbkdf2PasswordEncoder().encode("пароль", "c3po"));
    }

    @Test
    public void deterministicAndSaltDependent() {
        Pbkdf2PasswordEncoder encoder = new Pbkdf2PasswordEncoder(10);
        assertEquals(encoder.encode("pw", "s1"), encoder.encode("pw", "s1"));
        assertNotEquals(encoder.encode("pw", "s1"), encoder.encode("pw", "s2"));
        assertTrue(encoder.encode("pw", "s1").length() <= 128);
    }

    @Test(expected = IllegalArgumentException.class)
    public void emptySaltRejected() {
        new Pbkdf2PasswordEncoder(10).encode("pw", "");
    }

    @Test
    public void matchesUsesIterationsFromStoredValue() {
        String stored = new Pbkdf2PasswordEncoder(7).encode("pw", "salt");
        assertTrue(Pbkdf2PasswordEncoder.matches("pw", "salt", stored));
        assertFalse(Pbkdf2PasswordEncoder.matches("other", "salt", stored));
        assertFalse(Pbkdf2PasswordEncoder.matches("pw", "salt", "pbkdf2-sha256$abc$00"));
        assertFalse(Pbkdf2PasswordEncoder.matches("pw", "salt", "pbkdf2-sha256$99999999$00"));
        assertFalse(Pbkdf2PasswordEncoder.matches("pw", "salt", "deadbeef"));
    }
}
