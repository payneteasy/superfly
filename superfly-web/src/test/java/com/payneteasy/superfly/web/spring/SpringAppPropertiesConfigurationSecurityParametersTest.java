package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * The parameters that switch security features must not change on a typo: an unknown value stops the start with a
 * message that names the parameter, an empty value is the default of the code.
 */
public class SpringAppPropertiesConfigurationSecurityParametersTest {

    private static final String MFA_ENABLED = "superfly-mfa-enabled";

    private static final List<String> OWN_PARAMETERS = List.of(MFA_ENABLED, "superfly-csrf-login-validator-enable",
            "superfly-cryptoLegacyDefaultKey", "superfly-mfa-method-force", "superfly-policy",
            "superfly-max-logins-failed", "superfly-max-otp-failed", "superfly-loginIpLimit");

    // The environment variables of the same parameters win over the context parameters used here
    private static final List<String> OVERRIDING_ENVIRONMENT = List.of("SUPERFLY_CRYPTO_LEGACY_DEFAULT_KEY",
            "SUPERFLY_LOGIN_IP_LIMIT");

    private static final List<String> TRUE_VALUES  = List.of("true", "TRUE", "True", " true", "true ", "\t true\n");
    private static final List<String> FALSE_VALUES = List.of("false", "FALSE", "False", " false", "false ", "\t false\n");
    private static final List<String> NOT_BOOLEANS = List.of("ture", "yes", "no", "on", "off", "1", "0", "t", "truee",
            "tru e", "enabled", "true;");
    private static final List<String> NOT_SET      = Arrays.asList(null, "", "   ", "\t");

    private static final List<Switch> SWITCHES = List.of(
            new Switch(MFA_ENABLED, true, SuperflyProperties::enableMultiFactorAuth),
            new Switch("superfly-csrf-login-validator-enable", true, SuperflyProperties::csrfLoginValidatorEnable),
            new Switch("superfly-cryptoLegacyDefaultKey", false, SuperflyProperties::cryptoLegacyDefaultKey));

    private static final List<Choice> CHOICES = List.of(
            new Choice("superfly-mfa-method-force", OTPType.NONE, SuperflyProperties::forceMultiFactorAuthMethod,
                    Map.of("none", OTPType.NONE, "NONE", OTPType.NONE, " none ", OTPType.NONE,
                            "google_auth", OTPType.GOOGLE_AUTH, "GOOGLE_AUTH", OTPType.GOOGLE_AUTH,
                            " Google_Auth\t", OTPType.GOOGLE_AUTH),
                    List.of("google-auth", "googleauth", "google", "totp", "google_auth2", "none,google_auth", "1")),
            new Choice("superfly-policy", "none", SuperflyProperties::policyName,
                    Map.of("none", "none", "None", "none", " none ", "none",
                            "pcidss", "pcidss", "PCIDSS", "pcidss", " PciDss\t", "pcidss"),
                    List.of("pcidsss", "pci-dss", "pci dss", "pcidss1", "default", "1")));

    private final Map<String, String> savedProperties = new HashMap<>();

    @Before
    public void saveAndClearSystemProperties() {
        for (String name : OVERRIDING_ENVIRONMENT) {
            Assume.assumeTrue(name + " is set and overrides the parameters under test", System.getenv(name) == null);
        }
        for (String name : OWN_PARAMETERS) {
            String value = System.getProperty(name);
            if (value != null) {
                savedProperties.put(name, value);
            }
            System.clearProperty(name);
        }
    }

    @After
    public void restoreSystemProperties() {
        for (String name : OWN_PARAMETERS) {
            System.clearProperty(name);
        }
        savedProperties.forEach(System::setProperty);
    }

    @Test
    public void switchesAcceptTrueAndFalseInAnyCaseWithSpacesAround() {
        for (Switch parameter : SWITCHES) {
            for (String value : TRUE_VALUES) {
                assertEquals(parameter.name + "='" + value + "'", Boolean.TRUE, parameter.read(load(parameter.name, value)));
            }
            for (String value : FALSE_VALUES) {
                assertEquals(parameter.name + "='" + value + "'", Boolean.FALSE, parameter.read(load(parameter.name, value)));
            }
        }
    }

    @Test
    public void switchesRejectAnyOtherValue() {
        for (Switch parameter : SWITCHES) {
            for (String value : NOT_BOOLEANS) {
                assertRejected(parameter.name, value);
            }
        }
    }

    @Test
    public void switchesFallBackToTheDefaultWhenNotSetOrBlank() {
        for (Switch parameter : SWITCHES) {
            for (String value : NOT_SET) {
                assertEquals(parameter.name + "='" + value + "'", parameter.defaultValue, parameter.read(load(parameter.name, value)));
            }
        }
    }

    @Test
    public void choicesAcceptKnownCodesInAnyCaseWithSpacesAround() {
        for (Choice parameter : CHOICES) {
            parameter.valid.forEach((value, expected) ->
                    assertEquals(parameter.name + "='" + value + "'", expected, parameter.read(load(parameter.name, value))));
        }
    }

    @Test
    public void choicesRejectUnknownCodes() {
        for (Choice parameter : CHOICES) {
            for (String value : parameter.invalid) {
                assertRejected(parameter.name, value);
            }
        }
    }

    @Test
    public void choicesFallBackToTheDefaultWhenNotSetOrBlank() {
        for (Choice parameter : CHOICES) {
            for (String value : NOT_SET) {
                assertEquals(parameter.name + "='" + value + "'", parameter.defaultValue, parameter.read(load(parameter.name, value)));
            }
        }
    }

    @Test
    public void errorNamesTheParameterTheValueAndTheAllowedValues() {
        assertEquals("Parameter 'superfly-mfa-enabled' has invalid value 'ture', allowed values: true, false",
                assertRejected(MFA_ENABLED, "ture").getMessage());
        assertEquals("Parameter 'superfly-mfa-method-force' has invalid value 'totp', allowed values: none, google_auth",
                assertRejected("superfly-mfa-method-force", "totp").getMessage());
        assertEquals("Parameter 'superfly-policy' has invalid value 'pcidsss', allowed values: none, pcidss",
                assertRejected("superfly-policy", "pcidsss").getMessage());
    }

    @Test
    public void longValueIsCutInTheErrorMessage() {
        String value = "x".repeat(1000);

        String message = assertRejected(MFA_ENABLED, value).getMessage();

        assertTrue(message, message.length() < 200);
        assertTrue(message, message.contains("'superfly-mfa-enabled'"));
        assertFalse(message, message.contains(value));
    }

    @Test
    public void systemPropertyIsCheckedAndStillWinsOverContextParameter() {
        System.setProperty(MFA_ENABLED, "ture");
        assertRejected(MFA_ENABLED, "true");

        System.setProperty(MFA_ENABLED, "false");
        assertEquals(Boolean.FALSE, load(MFA_ENABLED, "ture").enableMultiFactorAuth());
    }

    @Test
    public void unknownValueStopsTheApplicationContext() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.addInitParameter(MFA_ENABLED, "ture");
        servletContext.addInitParameter("superfly-cryptoSecret", "dummy");
        servletContext.addInitParameter("superfly-cryptoSalt", "dummy");
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(servletContext);
        context.register(ApplicationParameterResolver.class, SpringAppPropertiesConfiguration.class);
        try {
            BeanCreationException e = assertThrows(BeanCreationException.class, context::refresh);

            assertTrue(e.getMostSpecificCause().getMessage(),
                    e.getMostSpecificCause().getMessage().startsWith("Parameter 'superfly-mfa-enabled' has invalid value"));
        } finally {
            context.close();
        }
    }

    @Test
    public void numericParametersStillStopTheStartOnGarbage() {
        for (String name : List.of("superfly-max-logins-failed", "superfly-max-otp-failed", "superfly-loginIpLimit")) {
            assertThrows(name, IllegalArgumentException.class, () -> load(name, "abc"));
        }
    }

    private static IllegalArgumentException assertRejected(String name, String value) {
        return assertThrows(name + "='" + value + "' must stop the start", IllegalArgumentException.class, () -> load(name, value));
    }

    private static SuperflyProperties load(String name, String value) {
        MockServletContext context = new MockServletContext();
        if (value != null) {
            context.addInitParameter(name, value);
        }
        context.addInitParameter("superfly-cryptoSecret", "dummy");
        context.addInitParameter("superfly-cryptoSalt", "dummy");
        return new SpringAppPropertiesConfiguration(new ApplicationParameterResolver(context)).superflyProperties();
    }

    private static final class Switch {
        final String                                name;
        final boolean                               defaultValue;
        final Function<SuperflyProperties, Boolean> reader;

        Switch(String name, boolean defaultValue, Function<SuperflyProperties, Boolean> reader) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.reader = reader;
        }

        Boolean read(SuperflyProperties properties) {
            return reader.apply(properties);
        }
    }

    private static final class Choice {
        final String                               name;
        final Object                               defaultValue;
        final Function<SuperflyProperties, Object> reader;
        final Map<String, Object>                  valid;
        final List<String>                         invalid;

        Choice(String name, Object defaultValue, Function<SuperflyProperties, Object> reader,
               Map<String, Object> valid, List<String> invalid) {
            this.name = name;
            this.defaultValue = defaultValue;
            this.reader = reader;
            this.valid = valid;
            this.invalid = invalid;
        }

        Object read(SuperflyProperties properties) {
            return reader.apply(properties);
        }
    }
}
