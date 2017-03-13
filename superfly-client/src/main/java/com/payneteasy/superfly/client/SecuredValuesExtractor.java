package com.payneteasy.superfly.client;

import org.springframework.security.access.annotation.Secured;

import java.lang.annotation.Annotation;

/**
 * Default ValuesExtractor implementation which checks that object is an array
 * of strings first. If not, it checks whether it a string and in this case
 * returns an array of one element. Otherwise, it returns an array of one
 * element using object.toString() return value.
 *
 * @author Roman Puchkovskiy
 */
public class SecuredValuesExtractor implements ValuesExtractor {

    @Override
    public String[] extract(Annotation annotation) {
        if (annotation == null) {
            throw new IllegalArgumentException("annotation is null");
        }
        if (!(annotation instanceof Secured)) {
            throw new IllegalArgumentException("Only instances of Secured are allowed, but it is an instance of " + annotation.getClass());
        }
        Secured secured = (Secured) annotation;
        return secured.value();
    }

}
