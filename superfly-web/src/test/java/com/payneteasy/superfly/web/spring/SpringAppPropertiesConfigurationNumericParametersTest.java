package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.common.SuperflyProperties;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * The numeric security parameters: spaces around a number are ignored, anything that is not a number stops the start
 * with a message that names the parameter. Which numbers are accepted (zero, negative) is not decided here.
 */
public class SpringAppPropertiesConfigurationNumericParametersTest {

    private static final String MAX_LOGINS = "superfly-max-logins-failed";
    private static final String MAX_OTP    = "superfly-max-otp-failed";
    private static final String IP_LIMIT   = "superfly-loginIpLimit";

    // The environment variable of the IP limit wins over the context parameter used here
    private static final String IP_LIMIT_ENVIRONMENT = "SUPERFLY_LOGIN_IP_LIMIT";

    private static final List<String> OWN_PARAMETERS = List.of(MAX_LOGINS, MAX_OTP, IP_LIMIT);

    // The name in the message of the IP limit covers both the environment variable and the parameter
    private static final Map<String, String> NAMES_IN_MESSAGE = Map.of(MAX_LOGINS, MAX_LOGINS, MAX_OTP, MAX_OTP,
            IP_LIMIT, IP_LIMIT_ENVIRONMENT + "/" + IP_LIMIT);

    private static final List<String> NOT_NUMBERS = List.of("abc", "1.5", "6x", "6 7", "0x10", "--1", "6,0", "six",
            "99999999999999999999");

    private final Map<String, String> savedProperties = new HashMap<>();

    @Before
    public void saveAndClearSystemProperties() {
        Assume.assumeTrue(IP_LIMIT_ENVIRONMENT + " is set and overrides the parameter under test",
                System.getenv(IP_LIMIT_ENVIRONMENT) == null);
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
    public void spacesAroundANumberAreIgnored() {
        for (String value : List.of(" 4", "4 ", "\t4\n", "  4  ")) {
            assertEquals("'" + value + "'", Long.valueOf(4), load(MAX_LOGINS, value).maxLoginsFailed());
            assertEquals("'" + value + "'", Long.valueOf(4), load(MAX_OTP, value).maxOtpFailed());
            assertEquals("'" + value + "'", Integer.valueOf(4), load(IP_LIMIT, value).loginIpLimit());
        }
    }

    @Test
    public void anythingButANumberStopsTheStartWithTheNameOfTheParameter() {
        for (String name : OWN_PARAMETERS) {
            for (String value : NOT_NUMBERS) {
                assertEquals(name + "='" + value + "'",
                        "Parameter '" + NAMES_IN_MESSAGE.get(name) + "' has invalid value '" + value + "', expected an integer",
                        assertRejected(name, value).getMessage());
            }
        }
    }

    @Test
    public void numberBeyondTheRangeOfTheParameterStopsTheStart() {
        assertEquals(Long.valueOf(3000000000L), load(MAX_LOGINS, "3000000000").maxLoginsFailed());
        assertEquals("Parameter '" + NAMES_IN_MESSAGE.get(IP_LIMIT) + "' has invalid value '3000000000', expected an integer",
                assertRejected(IP_LIMIT, "3000000000").getMessage());
    }

    @Test
    public void blankValueOfAParameterWithADefaultStillStopsTheStart() {
        for (String value : List.of("", "   ", "\t")) {
            assertEquals("Parameter '" + MAX_LOGINS + "' has invalid value '" + value + "', expected an integer",
                    assertRejected(MAX_LOGINS, value).getMessage());
            assertEquals("Parameter '" + NAMES_IN_MESSAGE.get(IP_LIMIT) + "' has invalid value '" + value + "', expected an integer",
                    assertRejected(IP_LIMIT, value).getMessage());
        }
    }

    @Test
    public void notSetAndBlankKeepTheirDefaults() {
        SuperflyProperties notSet = new SpringAppPropertiesConfiguration(
                new ApplicationParameterResolver(new MockServletContext())).superflyProperties();
        assertEquals(Long.valueOf(6), notSet.maxLoginsFailed());
        assertEquals("the OTP limit follows the login limit", Long.valueOf(6), notSet.maxOtpFailed());
        assertEquals(Integer.valueOf(20), notSet.loginIpLimit());

        for (String value : List.of("", "   ")) {
            assertEquals("'" + value + "'", Long.valueOf(6), load(MAX_OTP, value).maxOtpFailed());
        }
        assertEquals(Long.valueOf(3), load(MAX_LOGINS, "3").maxOtpFailed());
    }

    @Test
    public void zeroAndNegativeNumbersAreStillAccepted() {
        for (String value : List.of("0", "-1", " -5 ")) {
            long expected = Long.parseLong(value.trim());
            assertEquals(value, Long.valueOf(expected), load(MAX_LOGINS, value).maxLoginsFailed());
            assertEquals(value, Long.valueOf(expected), load(MAX_OTP, value).maxOtpFailed());
            assertEquals(value, Integer.valueOf((int) expected), load(IP_LIMIT, value).loginIpLimit());
        }
    }

    @Test
    public void longValueIsCutInTheErrorMessage() {
        String value = "9".repeat(1000) + "x";

        String message = assertRejected(MAX_LOGINS, value).getMessage();

        assertTrue(message, message.length() < 200);
        assertTrue(message, message.startsWith("Parameter 'superfly-max-logins-failed' has invalid value '"));
        assertFalse(message, message.contains(value));
    }

    @Test
    public void systemPropertyStillWinsOverContextParameter() {
        System.setProperty(MAX_LOGINS, " 3 ");
        assertEquals(Long.valueOf(3), load(MAX_LOGINS, "9").maxLoginsFailed());

        System.setProperty(MAX_LOGINS, "abc");
        assertRejected(MAX_LOGINS, "9");
    }

    private static IllegalArgumentException assertRejected(String name, String value) {
        return assertThrows(name + "='" + value + "' must stop the start", IllegalArgumentException.class,
                () -> load(name, value));
    }

    private static SuperflyProperties load(String name, String value) {
        MockServletContext context = new MockServletContext();
        context.addInitParameter(name, value);
        return new SpringAppPropertiesConfiguration(new ApplicationParameterResolver(context)).superflyProperties();
    }
}
