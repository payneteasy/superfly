package com.payneteasy.superfly.security;

/**
 * Transforms a string.
 * 
 * @author Roman Puchkovskiy
 */
public interface StringTransformer {
    /**
     * Does transformation.
     *
     * @param s    string to transform
     * @return transformed string
     */
    String transform(String s);
}
