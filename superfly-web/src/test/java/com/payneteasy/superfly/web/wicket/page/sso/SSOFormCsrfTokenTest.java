package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * The CSRF input of an SSO form is rendered with the token that the session holds.
 */
public class SSOFormCsrfTokenTest extends AbstractPageTest {
    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private UserService userService;
    private SettingsService settingsService;
    private final CsrfValidator csrfValidator = new CsrfValidatorImpl(true);

    @Before
    public void setUp() {
        userService = createNiceMock(UserService.class);
        settingsService = createNiceMock(SettingsService.class);
        expect(settingsService.getPolicy()).andStubReturn(Policy.NONE);
        replay(userService, settingsService);
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == UserService.class) {
            return userService;
        }
        if (type == SettingsService.class) {
            return settingsService;
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void tokenIsIssuedWhenTheFormIsRendered() {
        SSOChangePasswordPage page = new SSOChangePasswordPage("user");
        assertNull("the page must not touch the session token before it is rendered",
                tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));

        tester.startPage(page);

        assertEquals(renderedToken(), tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));
    }

    @Test
    public void formRenderedAfterValidationErrorCarriesTheTokenOfTheSession() {
        tester.startPage(new SSOChangePasswordPage("user"));

        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "password");
        form.setValue("password2", "another-password");
        tester.getRequest().getPostParameters().setParameterValue("_csrf", renderedToken());
        form.submit();

        tester.assertRenderedPage(SSOChangePasswordPage.class);
        assertEquals(renderedToken(), tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));
    }

    private String renderedToken() {
        TagTester input = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "name", "_csrf");
        assertNotNull("the form must contain the _csrf input", input);
        return input.getAttribute("value");
    }
}
