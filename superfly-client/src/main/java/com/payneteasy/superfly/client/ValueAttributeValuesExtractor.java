package com.payneteasy.superfly.client;

import java.lang.annotation.Annotation;

/**
 * Default ValuesExtractor implementation which invokes
 * value() method and returns its result.
 *
 * @author Roman Puchkovskiy
 */
public class ValueAttributeValuesExtractor implements ValuesExtractor {

    @Override
    public String[] extract(Annotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("annotation is null");
        }

        return new ReflectionInvoker(annotation)
                .<String[]>method("value")
                .invoke();
    }

}
