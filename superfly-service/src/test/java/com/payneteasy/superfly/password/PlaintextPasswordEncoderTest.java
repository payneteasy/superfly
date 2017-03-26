package com.payneteasy.superfly.password;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaintextPasswordEncoderTest {
    private PlaintextPasswordEncoder encoder = new PlaintextPasswordEncoder();

    @Test
    public void testEncode() {
        String encoded = encoder.encode("hello", "salt");
        assertEquals("hello{salt}", encoded);
    }

    @Test
    public void testEncodeWithEmptySalt() {
        String encoded = encoder.encode("hello", "");
        assertEquals("hello", encoded);

        encoded = encoder.encode("hello", null);
        assertEquals("hello", encoded);
    }
}
