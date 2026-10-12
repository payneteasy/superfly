package com.payneteasy.superfly.web.wicket.page.user;

import com.payneteasy.superfly.api.exceptions.PolicyValidationException;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.feedback.FeedbackMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * The change-password form of the admin console is rendered with the CSRF token that the session holds,
 * also after a submit was rejected because the session token had changed (e.g. in another tab).
 */
public class ChangePasswordPageCsrfTest extends AbstractPageTest {
    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");
    private static final String FORM = "change-password-panel:form";

    private UserService userService;
    private SettingsService settingsService;
    private final CsrfValidator csrfValidator = new CsrfValidatorImpl(true);

    @Before
    public void setUp() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("admin", "n/a", "ROLE_ADMIN"));
        userService = createMock(UserService.class);
        settingsService = createNiceMock(SettingsService.class);
        expect(settingsService.getSuperflyVersion()).andStubReturn("test");
        expect(settingsService.getPolicy()).andStubReturn(Policy.NONE);
        replay(settingsService);
        // an authenticated administrator always has an HTTP session
        tester.getHttpSession().setTemporary(false);
    }

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
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
        replay(userService);
        ChangePasswordPage page = new ChangePasswordPage(new PageParameters());
        assertNull("the page must not touch the session token before it is rendered",
                tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));

        tester.startPage(page);

        String rendered = renderedToken();
        assertNotNull(rendered);
        assertEquals(rendered, tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE));
    }

    @Test
    public void tokenOfTheRenderedFormIsAccepted() throws PolicyValidationException {
        expectSubmits(1, 1);
        replay(userService);

        tester.startPage(ChangePasswordPage.class);
        submit(renderedToken());

        verify(userService);
        tester.assertRenderedPage(tester.getApplication().getHomePage());
    }

    @Test
    public void foreignTokenIsRejected() throws PolicyValidationException {
        expectSubmits(1, 0);
        replay(userService);

        tester.startPage(ChangePasswordPage.class);
        submit("token-of-another-session");

        tester.assertRenderedPage(ChangePasswordPage.class);
        assertEquals(1, tester.getMessages(FeedbackMessage.ERROR).size());
        verify(userService);
    }

    @Test
    public void rejectionMessageIsRenderedOnce() throws PolicyValidationException {
        expectSubmits(1, 0);
        replay(userService);

        tester.startPage(ChangePasswordPage.class);
        String rendered = renderedToken();
        tester.getHttpSession().setAttribute(CSRF_ATTRIBUTE, "token-issued-to-another-tab");
        submit(rendered);

        tester.assertRenderedPage(ChangePasswordPage.class);
        List<Serializable> errors = tester.getMessages(FeedbackMessage.ERROR);
        assertEquals(errors.toString(), 1, errors.size());
        String message = errors.get(0).toString();
        String html = tester.getLastResponseAsString();
        int shown = 0;
        for (int at = html.indexOf(message); at >= 0; at = html.indexOf(message, at + message.length())) {
            shown++;
        }
        assertEquals("the message must be rendered once: " + message, 1, shown);
    }

    @Test
    public void rejectedFormIsRenderedWithTheCurrentTokenAndCanBeSentAgain() throws PolicyValidationException {
        expectSubmits(2, 1);
        replay(userService);

        tester.startPage(ChangePasswordPage.class);
        String renderedFirst = renderedToken();
        // another tab has rendered its own form: the session token is not the one of this form any more
        tester.getHttpSession().setAttribute(CSRF_ATTRIBUTE, "token-issued-to-another-tab");

        submit(renderedFirst);

        tester.assertRenderedPage(ChangePasswordPage.class);
        String renderedAfterRejection = renderedToken();
        assertNotEquals("the rejected token must not be rendered again", renderedFirst, renderedAfterRejection);
        assertEquals("the form carries the token the session holds now",
                tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE), renderedAfterRejection);

        submit(renderedAfterRejection);

        verify(userService);
    }

    /** The password is validated on every submit of the form and changed only when the CSRF token is accepted. */
    private void expectSubmits(int validations, int changes) throws PolicyValidationException {
        userService.validatePassword("admin", "new-password");
        expectLastCall().times(validations);
        if (changes > 0) {
            userService.changeTempPassword("admin", "new-password");
            expectLastCall().times(changes);
        }
    }

    /** Sends the form as a browser does: the value of the hidden input from the rendered markup is posted as "_csrf". */
    private void submit(String csrfToken) {
        FormTester form = tester.newFormTester(FORM);
        form.setValue("password", "new-password");
        form.setValue("password2", "new-password");
        tester.getRequest().getPostParameters().setParameterValue("_csrf", csrfToken);
        form.submit();
    }

    private String renderedToken() {
        TagTester input = TagTester.createTagByAttribute(tester.getLastResponseAsString(), "name", "_csrf");
        assertNotNull("the form must contain the _csrf input", input);
        return input.getAttribute("value");
    }
}
