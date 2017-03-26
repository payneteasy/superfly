package com.payneteasy.superfly.client;

/**
 * Transformer which removes a matching prefix if finds one.
 * 
 * @author Roman Puchkovskiy
 */
public class PrefixRemovingTransformer implements StringTransformer {

    private String[] prefixes = {};

    public void setPrefixes(String[] prefixes) {
        this.prefixes = prefixes;
    }

    public String transform(String s) {
        for (String prefix : prefixes) {
            if (s.startsWith(prefix)) {
                s = s.substring(prefix.length());
            }
        }
        return s;
    }

}
