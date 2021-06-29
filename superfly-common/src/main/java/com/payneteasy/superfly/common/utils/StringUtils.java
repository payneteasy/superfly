package com.payneteasy.superfly.common.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class StringUtils {
    public static String[] commaDelimitedListToStringArray(String s) {
        StringTokenizer tokenizer = new StringTokenizer(s, ",");
        List<String> fragments = new ArrayList<String>();
        while (tokenizer.hasMoreTokens()) {
            fragments.add(tokenizer.nextToken().trim());
        }
        return fragments.toArray(new String[fragments.size()]);
    }

    public static String yesNoString(boolean condition){
        return condition?"Yes":"No";
    }

    public static String emptyStringIfNull(String s){
        return (s == null || s.equals(""))?"-":s;
    }

    public static String emptyStringIfObjectNull(Object o){
        return (o == null)?"-":String.valueOf(o);
    }

    /**
     * Check if string has some text
     * @param aText string to check
     * @return true if null or text has only space character
     */
    public static boolean isEmpty(String aText) {
        return !hasText(aText);
    }

    public static boolean hasLength(String str) {
        return str != null && str.length() > 0;
    }

    public static boolean hasText(String ...strs) {
        return Arrays.stream(strs).allMatch(StringUtils::hasText);
    }

    public static boolean hasText(String str) {
        if(!hasLength(str)) {
            return false;
        } else {
            int strLen = str.length();

            for(int i = 0; i < strLen; ++i) {
                if(!Character.isWhitespace(str.charAt(i))) {
                    return true;
                }
            }

            return false;
        }
    }

}
