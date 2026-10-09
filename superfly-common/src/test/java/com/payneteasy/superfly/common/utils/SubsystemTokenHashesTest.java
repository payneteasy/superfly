package com.payneteasy.superfly.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SubsystemTokenHashesTest {

    @Test
    public void hashHasPrefixAndHexDigest() {
        // sha256("abc")
        assertEquals("sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                SubsystemTokenHashes.hash("abc"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hashOfNullTokenIsRefused() {
        SubsystemTokenHashes.hash(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void hashOfEmptyTokenIsRefused() {
        SubsystemTokenHashes.hash("");
    }
}
