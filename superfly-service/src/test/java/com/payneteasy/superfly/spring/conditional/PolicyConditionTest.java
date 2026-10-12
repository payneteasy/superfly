package com.payneteasy.superfly.spring.conditional;

import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.spring.PolicyParameterFixture;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.Arrays;
import java.util.List;

import static com.payneteasy.superfly.spring.PolicyParameterFixture.webEnvironment;
import static com.payneteasy.superfly.spring.PolicyParameterFixture.withEnvironmentVariable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class PolicyConditionTest {

    @Rule
    public final PolicyParameterFixture parameter = new PolicyParameterFixture();

    private interface Selected {
    }

    @OnPolicyCondition(Policy.NONE)
    static class OnNone implements Selected {
    }

    @OnPolicyCondition(Policy.PCIDSS)
    static class OnPcidss implements Selected {
    }

    @OnPolicyCondition({Policy.NONE, Policy.PCIDSS})
    static class OnAny implements Selected {
    }

    private static final List<String> NONE_SELECTED   = List.of("OnAny", "OnNone");
    private static final List<String> PCIDSS_SELECTED = List.of("OnAny", "OnPcidss");

    @Test
    public void systemPropertyWinsOverContextParameter() {
        parameter.systemProperty("none");
        assertEquals(NONE_SELECTED, selected(webEnvironment("pcidss")));
        assertEquals(List.of(NoneLockoutStrategy.class), strategies(webEnvironment("pcidss")));

        parameter.systemProperty("pcidss");
        assertEquals(PCIDSS_SELECTED, selected(webEnvironment("none")));
        assertEquals(List.of(PCIDSSLockoutStrategy.class), strategies(webEnvironment("none")));
    }

    @Test
    public void contextParameterIsUsedWithoutSystemProperty() {
        assertEquals(PCIDSS_SELECTED, selected(webEnvironment("pcidss")));
        assertEquals(List.of(PCIDSSLockoutStrategy.class), strategies(webEnvironment("pcidss")));

        assertEquals(NONE_SELECTED, selected(webEnvironment("none")));
        assertEquals(List.of(NoneLockoutStrategy.class), strategies(webEnvironment("none")));
    }

    @Test
    public void noneWhenThePolicyIsNotSetAnywhere() {
        assertEquals(NONE_SELECTED, selected(webEnvironment(null)));
        assertEquals(List.of(NoneLockoutStrategy.class), strategies(webEnvironment(null)));
    }

    @Test
    public void caseAndSpacesAroundAreIgnored() {
        for (String value : List.of("PCIDSS", "Pcidss", " pcidss ", "\tPCIDSS\n")) {
            assertEquals("context parameter '" + value + "'", PCIDSS_SELECTED, selected(webEnvironment(value)));
        }
        for (String value : List.of("NONE", "None", " none ", "\tNONE\n")) {
            assertEquals("context parameter '" + value + "'", NONE_SELECTED, selected(webEnvironment(value)));
        }

        parameter.systemProperty(" PCIDSS ");
        assertEquals(PCIDSS_SELECTED, selected(webEnvironment("none")));
    }

    @Test
    public void unknownValueStopsTheContextWithTheNameOfTheParameter() {
        for (String value : List.of("pcidsss", "pci-dss", "default", "1")) {
            IllegalArgumentException e = assertThrows("context parameter '" + value + "'",
                    IllegalArgumentException.class, () -> selected(webEnvironment(value)));

            assertEquals("Parameter 'superfly-policy' has invalid value '" + value + "', allowed values: none, pcidss",
                    e.getMessage());
        }

        parameter.systemProperty("pcidsss");
        assertThrows(IllegalArgumentException.class, () -> selected(webEnvironment("pcidss")));
    }

    @Test
    public void unknownValueOfTheOverriddenContextParameterDoesNotMatter() {
        parameter.systemProperty("none");

        assertEquals(NONE_SELECTED, selected(webEnvironment("pcidsss")));
    }

    @Test
    public void environmentVariableIsNotASourceOfThePolicy() {
        assertEquals(NONE_SELECTED, selected(withEnvironmentVariable(webEnvironment(null), "SUPERFLY_POLICY", "pcidss")));
        assertEquals(NONE_SELECTED, selected(withEnvironmentVariable(webEnvironment("none"), "SUPERFLY_POLICY", "pcidss")));
    }

    // The beans of the application are found by the component scan, not registered one by one
    private static List<Class<?>> strategies(ConfigurableEnvironment environment) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(environment);
            context.scan("com.payneteasy.superfly.lockout");
            return Arrays.stream(context.getBeanFactory().getBeanNamesForType(LockoutStrategy.class))
                    .<Class<?>>map(name -> context.getBeanFactory().getType(name))
                    .toList();
        }
    }

    private static List<String> selected(ConfigurableEnvironment environment) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(environment);
            context.register(OnNone.class, OnPcidss.class, OnAny.class);
            context.refresh();
            return context.getBeansOfType(Selected.class).values().stream()
                    .map(bean -> bean.getClass().getSimpleName())
                    .sorted()
                    .toList();
        }
    }
}
