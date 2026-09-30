package com.payneteasy.superfly.client;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * Verifies that jakarta.servlet-api is not present in the EE8 compile/test classpath.
 * Guards against transitive leakage from dependencies like superfly-client-opt.
 */
public class JakartaServletIsolationTest {

    private static final Logger log = LoggerFactory.getLogger(JakartaServletIsolationTest.class);

    @Test
    public void jakartaServletMustNotBeOnClasspath() {
        log.debug("Checking that jakarta.servlet.http.HttpServletRequest is NOT on EE8 classpath");
        try {
            Class.forName("jakarta.servlet.http.HttpServletRequest");
            fail("jakarta.servlet-api must not be on the classpath of an EE8 module; " +
                    "a dependency is leaking jakarta into the EE8 chain");
        } catch (ClassNotFoundException expected) {
            log.debug("OK: jakarta.servlet.http.HttpServletRequest is absent from classpath");
        }
    }

    @Test
    public void javaxServletMustBeOnClasspath() throws ClassNotFoundException {
        log.debug("Checking that javax.servlet.http.HttpServletRequest IS on EE8 classpath");
        Class<?> clazz = Class.forName("javax.servlet.http.HttpServletRequest");
        log.debug("OK: javax.servlet.http.HttpServletRequest loaded — {}", clazz.getName());
        assertNotNull(clazz);
    }
}
