package com.payneteasy.superfly.client.test;

import org.springframework.security.access.annotation.Secured;

@Secured({"multiple1", "multiple2"})
public class AnnotatedClass2 {
    @SuppressWarnings("unused")
    @Secured(value = "nested")
    private class Nested {

    }
}
