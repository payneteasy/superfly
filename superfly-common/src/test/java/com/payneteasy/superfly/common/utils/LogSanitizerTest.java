package com.payneteasy.superfly.common.utils;

import org.junit.Test;

import static com.payneteasy.superfly.common.utils.LogSanitizer.forLog;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class LogSanitizerTest {

    @Test
    public void lineBreaksAndTabsAreReplaced() {
        assertEquals("a__b_c", forLog("a\r\nb\tc"));
    }

    @Test
    public void otherControlCharactersAreReplaced() {
        // ESC starts terminal escape sequences, NUL cuts the line in some viewers, DEL
        assertEquals("_[31mred_x_", forLog("\u001b[31mred\u0000x\u007f"));
    }

    @Test
    public void unicodeLineSeparatorsAreReplaced() {
        assertEquals("a_b_c_d", forLog("a\u0085b c d"));
    }

    @Test
    public void ordinaryTextIsKept() {
        assertEquals("Пётр O'Neil <user@example.com> 42", forLog("Пётр O'Neil <user@example.com> 42"));
    }

    @Test
    public void nullStaysNull() {
        assertNull(forLog(null));
        assertNull(forLog(null, 10));
    }

    @Test
    public void nonStringValueIsConvertedToString() {
        assertEquals("1_2", forLog(new Object() {
            @Override
            public String toString() {
                return "1\n2";
            }
        }));
    }

    @Test
    public void longValueIsCutAndMarked() {
        assertEquals("abc...", forLog("abcdef", 3));
        assertEquals("a_c...", forLog("a\nc\nef", 3));
    }

    @Test
    public void valueThatFitsIsNotMarked() {
        assertEquals("abc", forLog("abc", 3));
    }
}
