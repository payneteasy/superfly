package com.payneteasy.superfly.api;

import org.junit.Test;

import static org.junit.Assert.fail;

/**
 * Защита от случайного возврата observability/cross-cutting зависимостей
 * в classpath superfly-remote-api.
 *
 * <p>Owner observability — consumer (paynet и пр.). SSO-2 был REVERTED
 * именно по этой причине — см. .ai-factory/ROADMAP.md.
 */
public class SuperflyDepsAssertionTest {

    @Test
    public void micrometerMustNotBeOnClasspath() {
        try {
            Class.forName("io.micrometer.core.instrument.MeterRegistry");
            fail("io.micrometer.core.instrument.MeterRegistry must NOT be on "
                + "classpath of superfly-remote-api — observability is consumer's "
                + "responsibility (see ROADMAP.md SSO-2 REVERTED)");
        } catch (ClassNotFoundException expected) {
            // OK — desired behaviour
        }
    }
}
