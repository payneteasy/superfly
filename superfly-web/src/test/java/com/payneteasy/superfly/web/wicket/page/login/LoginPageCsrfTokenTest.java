package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * The CSRF input of a login step form is rendered with the token that the session holds.
 */
public class LoginPageCsrfTokenTest extends AbstractPageTest {
    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private final CsrfValidator csrfValidator = new CsrfValidatorImpl(true);

    @Before
    public void setUp() {
        // the login step forms are shown to a visitor that already has an HTTP session
        tester.getHttpSession().setTemporary(false);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void tokenIsIssuedWhenTheFormIsRendered() {
        LoginPasswordStepPage page = new LoginPasswordStepPage();
        assertNull("the page must not touch the session token before it is rendered",
                tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));

        tester.startPage(page);

        TagTester input = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "name", "_csrf");
        assertNotNull("the form must contain the _csrf input", input);
        assertEquals(input.getAttribute("value"), tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));
    }
}
