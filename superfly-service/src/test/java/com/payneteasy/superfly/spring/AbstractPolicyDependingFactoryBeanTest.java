package com.payneteasy.superfly.spring;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.List;

import static com.payneteasy.superfly.spring.PolicyParameterFixture.webEnvironment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class AbstractPolicyDependingFactoryBeanTest {

    @Rule
    public final PolicyParameterFixture parameter = new PolicyParameterFixture();

    /** Answers with the policy it has found, as a strategy-selecting factory bean would use it. */
    static class FoundPolicy extends AbstractPolicyDependingFactoryBean<Policy> {
        @Override
        public Policy getObject() {
            return findPolicyByIdentifier();
        }

        @Override
        public Class<?> getObjectType() {
            return Policy.class;
        }
    }

    @Test
    public void systemPropertyWinsOverContextParameter() {
        parameter.systemProperty("none");
        assertEquals(Policy.NONE, policy(webEnvironment("pcidss"), null));

        parameter.systemProperty("pcidss");
        assertEquals(Policy.PCIDSS, policy(webEnvironment("none"), null));
    }

    @Test
    public void contextParameterIsUsedWithoutSystemProperty() {
        assertEquals(Policy.PCIDSS, policy(webEnvironment("pcidss"), null));
        assertEquals(Policy.NONE, policy(webEnvironment("none"), null));
    }

    @Test
    public void noneWhenThePolicyIsNotSetAnywhere() {
        assertEquals(Policy.NONE, policy(webEnvironment(null), null));
    }

    @Test
    public void caseAndSpacesAroundAreIgnored() {
        for (String value : List.of("PCIDSS", " pcidss ", "\tPcidss\n")) {
            assertEquals("context parameter '" + value + "'", Policy.PCIDSS, policy(webEnvironment(value), null));
        }
    }

    @Test
    public void unknownValueIsRejectedWithTheNameOfTheParameter() {
        BeanCreationException e = assertThrows(BeanCreationException.class, () -> policy(webEnvironment("pcidsss"), null));

        assertEquals("Parameter 'superfly-policy' has invalid value 'pcidsss', allowed values: none, pcidss",
                e.getMostSpecificCause().getMessage());
    }

    @Test
    public void policyNameSetExplicitlyIsUsedAndParsedTheSameWay() {
        parameter.systemProperty("none");

        assertEquals(Policy.PCIDSS, policy(webEnvironment("none"), "pcidss"));
        assertEquals(Policy.PCIDSS, policy(webEnvironment("none"), " PCIDSS "));
        assertEquals(Policy.NONE, policy(webEnvironment("pcidss"), "NONE"));

        BeanCreationException e = assertThrows(BeanCreationException.class, () -> policy(webEnvironment("none"), "pcidsss"));
        assertEquals("Parameter 'superfly-policy' has invalid value 'pcidsss', allowed values: none, pcidss",
                e.getMostSpecificCause().getMessage());
    }

    @Test
    public void outsideOfTheContainerThereIsNoEnvironmentToAsk() {
        FoundPolicy bean = new FoundPolicy();
        assertEquals(Policy.NONE, bean.getObject());

        parameter.systemProperty("pcidss");
        assertEquals(Policy.PCIDSS, bean.getObject());
    }

    private static Policy policy(ConfigurableEnvironment environment, String policyName) {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.setEnvironment(environment);
            context.registerBean("policy", FoundPolicy.class, () -> {
                FoundPolicy bean = new FoundPolicy();
                bean.setPolicyName(policyName);
                return bean;
            });
            context.refresh();
            return context.getBean("policy", Policy.class);
        }
    }
}
