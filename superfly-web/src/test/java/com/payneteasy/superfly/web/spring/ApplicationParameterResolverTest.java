package com.payneteasy.superfly.web.spring;

import com.payneteasy.superfly.common.SuperflyProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * The order of the sources: the system property, then the context parameter, then the default. An empty or blank
 * system property is not set, so it does not hide the context parameter.
 */
public class ApplicationParameterResolverTest {

    private static final String PARAMETER   = "superfly-resolver-test-parameter";
    private static final String MFA_ENABLED = "superfly-mfa-enabled";

    private static final List<String> BLANK = List.of("", "   ", "\t", " \n ");

    private String savedMfaEnabled;

    @Before
    public void saveAndClearSystemProperties() {
        savedMfaEnabled = System.getProperty(MFA_ENABLED);
        System.clearProperty(MFA_ENABLED);
        System.clearProperty(PARAMETER);
    }

    @After
    public void restoreSystemProperties() {
        System.clearProperty(PARAMETER);
        if (savedMfaEnabled == null) {
            System.clearProperty(MFA_ENABLED);
        } else {
            System.setProperty(MFA_ENABLED, savedMfaEnabled);
        }
    }

    @Test
    public void systemPropertyWinsOverContextParameter() {
        System.setProperty(PARAMETER, "fromSystem");

        assertEquals("fromSystem", resolver("fromContext").getParameter(PARAMETER, "default"));
    }

    @Test
    public void contextParameterIsUsedWithoutSystemProperty() {
        assertEquals("fromContext", resolver("fromContext").getParameter(PARAMETER, "default"));
    }

    @Test
    public void defaultIsUsedWhenNothingIsSet() {
        assertEquals("default", resolver(null).getParameter(PARAMETER, "default"));
        assertNull(resolver(null).getParameter(PARAMETER, null));
    }

    @Test
    public void blankSystemPropertyIsNotSetAndYieldsToContextParameterThenToDefault() {
        for (String blank : BLANK) {
            System.setProperty(PARAMETER, blank);

            assertEquals("'" + blank + "'", "fromContext", resolver("fromContext").getParameter(PARAMETER, "default"));
            assertEquals("'" + blank + "'", "default", resolver(null).getParameter(PARAMETER, "default"));
            assertNull("'" + blank + "'", resolver(null).getParameter(PARAMETER, null));
        }
    }

    @Test
    public void requiredParameterIgnoresBlankSystemProperty() {
        System.setProperty(PARAMETER, "  ");

        assertEquals("fromContext", resolver("fromContext").getRequiredParameter(PARAMETER));
        assertThrows(IllegalArgumentException.class, () -> resolver(null).getRequiredParameter(PARAMETER));
    }

    @Test
    public void blankContextParameterIsStillTheValue() {
        assertEquals("", resolver("").getParameter(PARAMETER, "default"));
    }

    @Test
    public void blankSystemPropertyOfASwitchYieldsToContextParameter() {
        for (String blank : BLANK) {
            System.setProperty(MFA_ENABLED, blank);

            assertEquals("'" + blank + "'", Boolean.FALSE, properties("false").enableMultiFactorAuth());
            assertEquals("'" + blank + "' without a context parameter", Boolean.TRUE, properties(null).enableMultiFactorAuth());
        }
    }

    @Test
    public void systemPropertyOfASwitchStillWinsOverContextParameter() {
        System.setProperty(MFA_ENABLED, "true");

        assertEquals(Boolean.TRUE, properties("false").enableMultiFactorAuth());
    }

    private static ApplicationParameterResolver resolver(String contextParameter) {
        MockServletContext context = new MockServletContext();
        if (contextParameter != null) {
            context.addInitParameter(PARAMETER, contextParameter);
        }
        return new ApplicationParameterResolver(context);
    }

    private static SuperflyProperties properties(String mfaEnabled) {
        MockServletContext context = new MockServletContext();
        if (mfaEnabled != null) {
            context.addInitParameter(MFA_ENABLED, mfaEnabled);
        }
        return new SpringAppPropertiesConfiguration(new ApplicationParameterResolver(context)).superflyProperties();
    }
}
