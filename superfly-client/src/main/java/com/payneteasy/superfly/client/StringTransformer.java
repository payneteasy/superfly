package com.payneteasy.superfly.client;

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
