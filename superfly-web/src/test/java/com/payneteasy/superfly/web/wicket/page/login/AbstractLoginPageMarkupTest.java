package com.payneteasy.superfly.web.wicket.page.login;

import org.junit.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Not every login step page defines startup() (e.g. /login-setup), so the base template must not call it blindly.
 */
public class AbstractLoginPageMarkupTest {

    @Test
    public void bodyOnloadGuardsAgainstMissingStartupFunction() throws Exception {
        String markup;
        try (InputStream in = AbstractLoginPage.class.getResourceAsStream("AbstractLoginPage.html")) {
            markup = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        Matcher m = Pattern.compile("<body[^>]*onload=\"([^\"]*)\"").matcher(markup);
        assertTrue(markup, m.find());
        assertTrue(m.group(1), m.group(1).contains("typeof startup"));
    }
}
