package com.payneteasy.superfly.spring;

import org.junit.rules.ExternalResource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

import java.util.Map;

/**
 * The superfly-policy parameter in a test: no system property unless the test sets one (the previous value comes back
 * afterwards), and the environment of a web application, where the context parameters are a property source that is
 * consulted before the system properties.
 */
public class PolicyParameterFixture extends ExternalResource {

    private String savedSystemProperty;

    @Override
    protected void before() {
        savedSystemProperty = System.getProperty(PolicyParameter.NAME);
        System.clearProperty(PolicyParameter.NAME);
    }

    @Override
    protected void after() {
        if (savedSystemProperty == null) {
            System.clearProperty(PolicyParameter.NAME);
        } else {
            System.setProperty(PolicyParameter.NAME, savedSystemProperty);
        }
    }

    public void systemProperty(String value) {
        System.setProperty(PolicyParameter.NAME, value);
    }

    /** The same position that StandardServletEnvironment gives to the context parameters; none when the value is null. */
    public static StandardEnvironment webEnvironment(String contextParameter) {
        StandardEnvironment environment = new StandardEnvironment();
        if (contextParameter != null) {
            environment.getPropertySources().addBefore(StandardEnvironment.SYSTEM_PROPERTIES_PROPERTY_SOURCE_NAME,
                    new MapPropertySource("servletContextInitParams", Map.of(PolicyParameter.NAME, contextParameter)));
        }
        return environment;
    }

    /** Replaces the environment variables of the process, which the tests must not depend on. */
    public static StandardEnvironment withEnvironmentVariable(StandardEnvironment environment, String name, String value) {
        environment.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        Map.of(name, value)));
        return environment;
    }
}
