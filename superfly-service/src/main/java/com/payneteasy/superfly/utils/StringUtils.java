package com.payneteasy.superfly.utils;

import java.util.Collection;

public class StringUtils {
   public static String collectionToCommaDelimitedString(Collection collection){
       if(collection==null||collection.size()==0){
           return null;
       }
    return org.springframework.util.StringUtils.collectionToCommaDelimitedString(collection);

   }

    /**
     * Prepares a user-supplied search string to be used as a needle inside a
     * <code>like '%needle%'</code> pattern: trims it, converts a blank string
     * to <code>null</code> and escapes the LIKE wildcards so that they are
     * matched literally. Action names are full of underscores, and an
     * unescaped underscore is a single-character wildcard, so without this a
     * search for <code>payment_configurator</code> also matches
     * <code>paymentXconfigurator</code>.
     *
     * @param search raw search string as it came from the UI
     * @return escaped needle or {@code null} if there is nothing to search for
     */
    public static String toLikeNeedle(String search) {
        if (search == null) {
            return null;
        }
        String trimmed = search.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        StringBuilder needle = new StringBuilder(trimmed.length() + 8);
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            if (c == '\\' || c == '%' || c == '_') {
                needle.append('\\');
            }
            needle.append(c);
        }
        return needle.toString();
    }
}
