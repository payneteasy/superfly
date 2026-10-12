package com.payneteasy.superfly.spring;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * The only place that reads and parses the {@code superfly-policy} parameter. The beans selected by
 * {@link com.payneteasy.superfly.spring.conditional.OnPolicyCondition}, the policy-dependent factory beans and
 * {@code SuperflyProperties} must work with the same policy, so each of them takes the raw value in the same order of
 * sources (system property, then context parameter, then the default) and parses it with {@link #parse(String)}.
 */
public final class PolicyParameter {

    public static final String NAME = "superfly-policy";

    // The property source that StandardServletEnvironment fills with the context parameters of the web application
    private static final String CONTEXT_PARAMETERS = "servletContextInitParams";

    private static final int MAX_VALUE_IN_ERROR = 64;

    private PolicyParameter() {
    }

    /**
     * Returns the raw value in the order of ApplicationParameterResolver of superfly-web: the system property wins
     * over the context parameter (an empty or blank system property is not set); {@code null} when neither is set.
     * {@link Environment#getProperty} is not used on purpose: it consults the context parameters before the system
     * properties and maps the environment variable SUPERFLY_POLICY to the name, which no other reader of this
     * parameter does.
     */
    public static String rawValue(Environment environment) {
        String systemProperty = System.getProperty(NAME);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }
        if (environment instanceof ConfigurableEnvironment configurable) {
            PropertySource<?> contextParameters = configurable.getPropertySources().get(CONTEXT_PARAMETERS);
            if (contextParameters != null) {
                Object value = contextParameters.getProperty(NAME);
                return value == null ? null : value.toString();
            }
        }
        return null;
    }

    /**
     * Returns the policy of a raw value: no value or a blank one is {@link Policy#NONE}, the case and the spaces around
     * are ignored, any other value is an error that names the parameter.
     */
    public static Policy parse(String value) {
        if (value == null || value.isBlank()) {
            return Policy.NONE;
        }
        String trimmed = value.strip();
        for (Policy policy : Policy.values()) {
            if (policy.getIdentifier().equalsIgnoreCase(trimmed)) {
                return policy;
            }
        }
        String shown = value.length() > MAX_VALUE_IN_ERROR ? value.substring(0, MAX_VALUE_IN_ERROR) + "..." : value;
        String allowed = Arrays.stream(Policy.values()).map(Policy::getIdentifier).collect(Collectors.joining(", "));
        throw new IllegalArgumentException(
                "Parameter '" + NAME + "' has invalid value '" + shown + "', allowed values: " + allowed);
    }
}
