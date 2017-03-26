package com.payneteasy.superfly.security;

/**
 * Transforms a string to upper-case.
 * 
 * @author Roman Puchkovskiy
 */
public class UppercaseTransformer implements StringTransformer {

    public String transform(String s) {
        return s.toUpperCase();
    }

}
