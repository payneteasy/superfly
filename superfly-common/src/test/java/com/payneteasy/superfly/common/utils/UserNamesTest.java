package com.payneteasy.superfly.common.utils;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserNamesTest {

    @Test
    public void nameThatFitsTheColumnIsPossible() {
        assertTrue(UserNames.isPossible("a"));
        assertTrue(UserNames.isPossible("a".repeat(UserNames.MAX_LENGTH)));
    }

    @Test
    public void nameLongerThanTheColumnIsImpossible() {
        assertFalse(UserNames.isPossible("a".repeat(UserNames.MAX_LENGTH + 1)));
    }

    @Test
    public void emptyOrMissingNameIsImpossible() {
        assertFalse(UserNames.isPossible(""));
        assertFalse(UserNames.isPossible(null));
    }
}
