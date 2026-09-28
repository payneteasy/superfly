package com.payneteasy.superfly.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilsTest {

    @Test
    public void nothingToSearchForBecomesNull() {
        assertNull(StringUtils.toLikeNeedle(null));
        assertNull(StringUtils.toLikeNeedle(""));
        assertNull(StringUtils.toLikeNeedle("   "));
    }

    @Test
    public void surroundingWhitespaceIsIgnored() {
        assertEquals("action\\_page", StringUtils.toLikeNeedle("  action_page \n"));
    }

    @Test
    public void likeWildcardsAreEscaped() {
        assertEquals("action\\_settings", StringUtils.toLikeNeedle("action_settings"));
        assertEquals("100\\% done", StringUtils.toLikeNeedle("100% done"));
        assertEquals("back\\\\slash", StringUtils.toLikeNeedle("back\\slash"));
    }

    @Test
    public void ordinaryCharactersAreLeftAsIs() {
        assertEquals("ACTION-name.1", StringUtils.toLikeNeedle("ACTION-name.1"));
    }
}
