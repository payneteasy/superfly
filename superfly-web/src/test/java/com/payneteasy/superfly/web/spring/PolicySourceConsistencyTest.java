package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.lockout.LockoutStrategy;
import com.payneteasy.superfly.lockout.none.NoneLockoutStrategy;
import com.payneteasy.superfly.lockout.pcidss.PCIDSSLockoutStrategy;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.AbstractPolicyDependingFactoryBean;
import com.payneteasy.superfly.spring.Policy;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.NestedRuntimeException;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * The policy of the application is one: the properties, the strategies selected by the policy conditions and the
 * policy-dependent factory beans take it from the same sources in the same order (system property, context parameter,
 * default) and read it the same way, in a context of a web application.
 */
public class PolicySourceConsistencyTest {

    private static final String POLICY = "superfly-policy";

    // The environment variables of the same parameters win over the defaults of the properties built here
    private static final List<String> OVERRIDING_ENVIRONMENT = List.of("SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY",
            "SUPERFLY_LOGIN_IP_LIMIT");

    private String savedSystemProperty;

    /**
     * Not annotated with {@code @Configuration}/{@code @Component} on purpose: the production component scan must not
     * pick up test classes; registered explicitly, its {@code @Bean} methods are still processed.
     */
    static class Stubs {
        @Bean
        public UserService userService() {
            UserService userService = niceMock(UserService.class);
            replay(userService);
            return userService;
        }

        @Bean
        public ActivePolicy activePolicy() {
            return new ActivePolicy();
        }
    }

    static class ActivePolicy extends AbstractPolicyDependingFactoryBean<Policy> {
        @Override
        public Policy getObject() {
            return findPolicyByIdentifier();
        }

        @Override
        public Class<?> getObjectType() {
            return Policy.class;
        }
    }

    @Before
    public void saveAndClearSystemProperty() {
        for (String name : OVERRIDING_ENVIRONMENT) {
            Assume.assumeTrue(name + " is set and overrides the parameters under test", System.getenv(name) == null);
        }
        savedSystemProperty = System.getProperty(POLICY);
        System.clearProperty(POLICY);
    }

    @After
    public void restoreSystemProperty() {
        System.clearProperty(POLICY);
        if (savedSystemProperty != null) {
            System.setProperty(POLICY, savedSystemProperty);
        }
    }

    @Test
    public void systemPropertyWinsOverContextParameterForEveryComponent() {
        assertEveryComponentUses("none", "none", "pcidss");
        assertEveryComponentUses("pcidss", "pcidss", "none");
        assertEveryComponentUses("none", "NONE", "PCIDSS");
        assertEveryComponentUses("pcidss", " Pcidss ", " none ");
    }

    @Test
    public void contextParameterIsUsedWithoutSystemProperty() {
        assertEveryComponentUses("pcidss", null, "pcidss");
        assertEveryComponentUses("none", null, "none");
    }

    @Test
    public void noneWhenThePolicyIsNotSetAnywhere() {
        assertEveryComponentUses("none", null, null);
    }

    @Test
    public void caseAndSpacesAroundAreIgnoredByEveryComponent() {
        assertEveryComponentUses("pcidss", null, "PCIDSS");
        assertEveryComponentUses("pcidss", null, " pcidss ");
        assertEveryComponentUses("none", null, "NONE");
        assertEveryComponentUses("pcidss", "PCIDSS", null);
        assertEveryComponentUses("pcidss", " pcidss\t", "none");
    }

    @Test
    public void unknownValueOfTheOverriddenContextParameterDoesNotMatter() {
        assertEveryComponentUses("none", "none", "pcidsss");
    }

    @Test
    public void blankSystemPropertyIsTheDefaultForEveryComponent() {
        assertEveryComponentUses("none", "", "pcidss");
        assertEveryComponentUses("none", "  ", "pcidss");
    }

    @Test
    public void everyCombinationOfSourcesGivesOnePolicy() {
        List<String> systemProperties = Arrays.asList(null, "", "none", " NONE ", "pcidss", "PCIDSS", " pcidss\t");
        List<String> contextParameters = Arrays.asList(null, "", "none", "PCIDSS", " pcidss ");

        for (String systemProperty : systemProperties) {
            for (String contextParameter : contextParameters) {
                assertEveryComponentUses(expectedPolicy(systemProperty, contextParameter), systemProperty, contextParameter);
            }
        }
    }

    @Test
    public void unknownValueStopsTheContextWithTheNameOfTheParameter() {
        for (String value : List.of("pcidsss", "pci-dss", "default", "1")) {
            String expected = "Parameter 'superfly-policy' has invalid value '" + value + "', allowed values: none, pcidss";

            assertEquals("context parameter '" + value + "'", expected, startFailure(null, value));
            assertEquals("system property '" + value + "'", expected, startFailure(value, "pcidss"));
        }
    }

    private static String expectedPolicy(String systemProperty, String contextParameter) {
        String effective = systemProperty != null ? systemProperty : contextParameter;
        return effective == null || effective.isBlank() ? "none" : effective.strip().toLowerCase(Locale.ROOT);
    }

    private static void assertEveryComponentUses(String expected, String systemProperty, String contextParameter) {
        String label = "system property '" + systemProperty + "', context parameter '" + contextParameter + "'";
        Policy expectedPolicy = Policy.fromIdentifier(expected);
        Class<?> expectedStrategy = expectedPolicy == Policy.PCIDSS ? PCIDSSLockoutStrategy.class : NoneLockoutStrategy.class;

        try (AnnotationConfigWebApplicationContext context = start(systemProperty, contextParameter)) {
            assertEquals(label, expected, context.getBean(SuperflyProperties.class).policyName());
            assertEquals(label, List.of(expectedStrategy), context.getBeansOfType(LockoutStrategy.class).values().stream()
                    .<Class<?>>map(Object::getClass).toList());
            assertEquals(label, expectedPolicy, context.getBean("activePolicy", Policy.class));
        }
    }

    private static String startFailure(String systemProperty, String contextParameter) {
        NestedRuntimeException e = assertThrows(NestedRuntimeException.class, () -> start(systemProperty, contextParameter));
        return e.getMostSpecificCause().getMessage();
    }

    private static AnnotationConfigWebApplicationContext start(String systemProperty, String contextParameter) {
        if (systemProperty == null) {
            System.clearProperty(POLICY);
        } else {
            System.setProperty(POLICY, systemProperty);
        }
        MockServletContext servletContext = new MockServletContext();
        if (contextParameter != null) {
            servletContext.addInitParameter(POLICY, contextParameter);
        }
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(servletContext);
        context.register(ApplicationParameterResolver.class, SpringAppPropertiesConfiguration.class, Stubs.class);
        // The strategies are found by the component scan, as in the application
        context.scan("com.payneteasy.superfly.lockout");
        context.refresh();
        return context;
    }
}
