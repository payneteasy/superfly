package com.payneteasy.superfly.client;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that HttpSessionWrapper, SessionMappingLocator, and HashMapBackedSessionMapping
 * are defined in superfly-common (com.payneteasy.superfly.common.session), not in superfly-client-core.
 * Guards against documentation drift and accidental class duplication.
 */
public class SessionApiLocationTest {

    private static final Logger log = LoggerFactory.getLogger(SessionApiLocationTest.class);

    private static final String EXPECTED_PACKAGE = "com.payneteasy.superfly.common.session";

    private static final String[] SESSION_CLASS_NAMES = {
            EXPECTED_PACKAGE + ".HttpSessionWrapper",
            EXPECTED_PACKAGE + ".SessionMappingLocator",
            EXPECTED_PACKAGE + ".HashMapBackedSessionMapping"
    };

    @Test
    public void sessionClassesMustBeLoadable() throws ClassNotFoundException {
        for (String className : SESSION_CLASS_NAMES) {
            log.debug("Checking that {} is loadable from superfly-common transitive dep", className);
            Class<?> clazz = Class.forName(className);
            log.debug("OK: {} loaded successfully", className);
            assertNotNull("Class must be loadable: " + className, clazz);
        }
    }

    @Test
    public void sessionClassesMustBeInCommonPackage() throws ClassNotFoundException {
        for (String className : SESSION_CLASS_NAMES) {
            log.debug("Checking package of {}", className);
            Class<?> clazz = Class.forName(className);
            String actualPackage = clazz.getPackage().getName();
            log.debug("Package of {}: {}", className, actualPackage);
            assertEquals(
                    className + " must be in " + EXPECTED_PACKAGE + " (not in superfly-client-core)",
                    EXPECTED_PACKAGE,
                    actualPackage
            );
        }
    }

    @Test
    public void sessionClassesMustOriginateFromSuperflyCommon() throws ClassNotFoundException {
        for (String className : SESSION_CLASS_NAMES) {
            log.debug("Checking code source of {}", className);
            Class<?> clazz = Class.forName(className);
            String codeSourcePath = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
            log.debug("Code source of {}: {}", className, codeSourcePath);
            assertTrue(
                    className + " must originate from superfly-common, but code source was: " + codeSourcePath,
                    codeSourcePath.contains("superfly-common")
            );
        }
    }
}
