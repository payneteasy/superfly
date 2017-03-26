package com.payneteasy.superfly.client;

/**
 * Transformer which converts string to lowercase.
 * 
 * @author Roman Puchkovskiy
 */
public class LowercaseTransformer implements StringTransformer {

    public String transform(String s) {
        return s.toLowerCase();
    }

}
