package com.payneteasy.superfly.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SubsystemTokenHasherTest {

    @Test
    public void hashHasPrefixAndHexDigest() {
        // sha256("abc")
        assertEquals("sha256:ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                SubsystemTokenHasher.hash("abc"));
    }

    @Test
    public void fitsVarchar80() {
        assertEquals(71, SubsystemTokenHasher.hash("x").length());
    }

    @Test
    public void matchesRawTokenAgainstItsHash() {
        assertTrue(SubsystemTokenHasher.matches("abc", SubsystemTokenHasher.hash("abc")));
    }

    @Test
    public void doesNotMatchOtherTokenOrTheHashItself() {
        String stored = SubsystemTokenHasher.hash("abc");
        assertFalse(SubsystemTokenHasher.matches("abd", stored));
        assertFalse(SubsystemTokenHasher.matches(stored, stored));
    }

    @Test
    public void doesNotMatchNullEmptyOrUnprefixedStoredValue() {
        String stored = SubsystemTokenHasher.hash("abc");
        assertFalse(SubsystemTokenHasher.matches(null, stored));
        assertFalse(SubsystemTokenHasher.matches("", stored));
        assertFalse(SubsystemTokenHasher.matches("abc", null));
        assertFalse(SubsystemTokenHasher.matches("abc", ""));
        assertFalse(SubsystemTokenHasher.matches("abc", "abc"));
        assertFalse(SubsystemTokenHasher.matches("abc", stored.substring("sha256:".length())));
        assertFalse(SubsystemTokenHasher.matches("", ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void hashOfEmptyTokenIsRefused() {
        SubsystemTokenHasher.hash("");
    }
}
