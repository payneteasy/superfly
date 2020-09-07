package com.payneteasy.superfly.client;

import java.lang.annotation.Annotation;

/**
 * Extracts an array of strings from annotation instance.
 * 
 * @author Roman Puchkovskiy
 */
public interface ValuesExtractor {
    /**
     * Performs extraction.
     *
     * @param annotation    annotation from which to extract
     * @return values
     */
    String[] extract(Annotation annotation);
}
