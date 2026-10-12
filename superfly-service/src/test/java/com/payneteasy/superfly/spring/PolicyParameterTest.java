package com.payneteasy.superfly.spring;

import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.payneteasy.superfly.spring.PolicyParameterFixture.webEnvironment;
import static com.payneteasy.superfly.spring.PolicyParameterFixture.withEnvironmentVariable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class PolicyParameterTest {

    @Rule
    public final PolicyParameterFixture parameter = new PolicyParameterFixture();

    @Test
    public void noValueOrBlankValueIsNone() {
        for (String value : Arrays.asList(null, "", "   ", "\t", "\n ")) {
            assertEquals("'" + value + "'", Policy.NONE, PolicyParameter.parse(value));
        }
    }

    @Test
    public void caseAndSpacesAroundAreIgnored() {
        for (String value : List.of("none", "NONE", "None", " none ", "\t none\n")) {
            assertEquals("'" + value + "'", Policy.NONE, PolicyParameter.parse(value));
        }
        for (String value : List.of("pcidss", "PCIDSS", "PciDss", " pcidss ", "\t PCIDSS\n")) {
            assertEquals("'" + value + "'", Policy.PCIDSS, PolicyParameter.parse(value));
        }
    }

    @Test
    public void unknownValueIsRejectedWithTheNameOfTheParameter() {
        for (String value : List.of("pcidsss", "pci-dss", "pci dss", "default", "1", "none,pcidss", "pcidss;")) {
            IllegalArgumentException e = assertThrows("'" + value + "'", IllegalArgumentException.class,
                    () -> PolicyParameter.parse(value));

            assertEquals("Parameter 'superfly-policy' has invalid value '" + value + "', allowed values: none, pcidss",
                    e.getMessage());
        }
    }

    @Test
    public void longValueIsCutInTheErrorMessage() {
        String value = "x".repeat(1000);

        String message = assertThrows(IllegalArgumentException.class, () -> PolicyParameter.parse(value)).getMessage();

        assertTrue(message, message.length() < 200);
        assertTrue(message, message.startsWith("Parameter 'superfly-policy' has invalid value '" + "x".repeat(64) + "...'"));
        assertFalse(message, message.contains("x".repeat(65)));
    }

    @Test
    public void noValueAnywhereIsNull() {
        assertNull(PolicyParameter.rawValue(null));
        assertNull(PolicyParameter.rawValue(new StandardEnvironment()));
        assertNull(PolicyParameter.rawValue(webEnvironment(null)));
    }

    @Test
    public void contextParameterIsUsedWhenThereIsNoSystemProperty() {
        assertEquals("pcidss", PolicyParameter.rawValue(webEnvironment("pcidss")));
        assertEquals(" PCIDSS ", PolicyParameter.rawValue(webEnvironment(" PCIDSS ")));
    }

    @Test
    public void systemPropertyWinsOverContextParameter() {
        parameter.systemProperty("none");

        assertEquals("none", PolicyParameter.rawValue(webEnvironment("pcidss")));
        assertEquals("none", PolicyParameter.rawValue(new StandardEnvironment()));
        assertEquals("none", PolicyParameter.rawValue(null));
    }

    @Test
    public void blankSystemPropertyIsStillTheValueAsInTheWebResolver() {
        parameter.systemProperty("");

        assertEquals("", PolicyParameter.rawValue(webEnvironment("pcidss")));
        assertEquals(Policy.NONE, PolicyParameter.parse(PolicyParameter.rawValue(webEnvironment("pcidss"))));
    }

    @Test
    public void onlySystemPropertyAndContextParameterAreSources() {
        StandardEnvironment environment = withEnvironmentVariable(webEnvironment(null), "SUPERFLY_POLICY", "pcidss");
        environment.getPropertySources().addFirst(new MapPropertySource("other", Map.of(PolicyParameter.NAME, "pcidss")));

        assertNull(PolicyParameter.rawValue(environment));
        assertEquals("the environment itself would answer", "pcidss", environment.getProperty(PolicyParameter.NAME));
    }
}
