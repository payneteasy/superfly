package com.payneteasy.superfly.client;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author rpuch
 */
public class ReflectionInvokerTest {
    @Test
    public void testDirectInvocation() {
        String greeting = new ReflectionInvoker(new Target())
                .<String>method("greet", String.class)
                .invoke("Joe");
        assertEquals("Hello Joe", greeting);
    }

    @Test
    public void testChildInvocation() {
        String greeting = new ReflectionInvoker(new Child())
                .<String>method("greet", String.class)
                .invoke("Mary");
        assertEquals("Hello Mary", greeting);
    }

    private static class Target {
        public String greet(String name) {
            return "Hello " + name;
        }
    }

    private static class Child extends Target {
    }
}