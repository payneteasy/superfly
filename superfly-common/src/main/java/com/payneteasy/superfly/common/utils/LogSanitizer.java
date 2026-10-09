package com.payneteasy.superfly.common.utils;

import java.util.regex.Pattern;

/**
 * Makes request and database values safe to put into a log record: a line break or another control character
 * would let the sender forge a record or hide a part of it.
 */
public final class LogSanitizer {

    private static final Pattern UNSAFE = Pattern.compile("[\\p{Cntrl}\\u0085\\u2028\\u2029]");

    private LogSanitizer() {
    }

    /**
     * @param value value to log, may be null
     * @return the value as a string with every control character and line separator replaced with {@code _},
     * or null for null
     */
    public static String forLog(Object value) {
        return value == null ? null : UNSAFE.matcher(String.valueOf(value)).replaceAll("_");
    }

    /**
     * Same as {@link #forLog(Object)}, and a value longer than {@code maxLength} is cut to it and marked with
     * {@code ...}.
     */
    public static String forLog(Object value, int maxLength) {
        if (value == null) {
            return null;
        }
        String s = String.valueOf(value);
        return forLog(s.length() > maxLength ? s.substring(0, maxLength) + "..." : s);
    }
}
