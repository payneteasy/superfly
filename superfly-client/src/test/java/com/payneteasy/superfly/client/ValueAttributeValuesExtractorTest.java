package com.payneteasy.superfly.client;

import org.junit.Test;
import org.springframework.security.access.annotation.Secured;

import static org.junit.Assert.*;

/**
 * @author rpuch
 */
public class ValueAttributeValuesExtractorTest {
    @Test
    public void test() {
        Secured secured = Annotated.class.getAnnotation(Secured.class);
        assertArrayEquals(new String[]{"abc"}, new ValueAttributeValuesExtractor().extract(secured));
    }

    @Secured("abc")
    private static class Annotated {
    }
}