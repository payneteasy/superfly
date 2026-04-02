package com.payneteasy.superfly.password;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PlaintextPasswordEncoderTest {
    private MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder();

    @Test
    public void testEncode() {
        encoder.setAlgorithm("SHA-256");
        String encoded = encoder.encode("64SejJ4DkWdt7cQ&", "04ec699c9ef3b67ab01293bc62274002579bd7cfa72decffc394c8b0597640e7");
        assertEquals("hello{salt}", encoded);
    }

    //GKPAXnU9rFux86A&

    @Test
    public void testEncodeWithEmptySalt() {
        String encoded = encoder.encode("hello", "");
        assertEquals("hello", encoded);

        encoded = encoder.encode("hello", null);
        assertEquals("hello", encoded);
    }
}
