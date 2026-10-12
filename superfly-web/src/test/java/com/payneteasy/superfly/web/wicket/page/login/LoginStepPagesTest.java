package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import com.payneteasy.superfly.web.security.FullAuthentication;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.security.SuperflyInitOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import jakarta.servlet.http.HttpSession;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.WebAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * A login step page opened in a state of the login that does not match it sends the user to the step the login is
 * at; the TOTP setup page shows the key that the session holds and sends only the code.
 */
public class LoginStepPagesTest extends AbstractPageTest {

    private final CsrfValidator csrfValidator = new CsrfValidatorImpl(true);

    @Before
    public void setUp() {
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
    public void setupPageOnTheCodeStepSendsToTheCodeStep() {
        loginAt(codeStep());

        tester.startPage(LoginSetupOTPStepPage.class);

        tester.assertRenderedPage(LoginOTPStepPage.class);
    }

    @Test
    public void setupPageWithoutLoginInProgressSendsToThePasswordStep() {
        tester.startPage(LoginSetupOTPStepPage.class);
        tester.assertRenderedPage(LoginPasswordStepPage.class);

        loginAt(new FullAuthentication("admin", AuthorityUtils.createAuthorityList("ROLE_ADMIN")));
        tester.startPage(LoginSetupOTPStepPage.class);
        tester.assertRenderedPage(LoginPasswordStepPage.class);
    }

    @Test
    public void codeStepPageDuringSetupSendsToTheSetupPage() {
        loginAt(setupStep());

        tester.startPage(LoginOTPStepPage.class);

        tester.assertRenderedPage(LoginSetupOTPStepPage.class);
    }

    @Test
    public void codeStepPageIsShownOnTheCodeStep() {
        loginAt(codeStep());

        tester.startPage(LoginOTPStepPage.class);

        tester.assertRenderedPage(LoginOTPStepPage.class);
        assertNotNull(TagTester.createTagByAttribute(tester.getLastResponseAsString(), "name", "j_otp"));
    }

    @Test
    public void setupPageShowsTheKeyOfTheSessionAndSendsOnlyTheCode() {
        loginAt(setupStep());

        tester.startPage(LoginSetupOTPStepPage.class);

        tester.assertRenderedPage(LoginSetupOTPStepPage.class);
        String key = SuperflyInitOTPAuthenticationProcessingFilter.pendingKey(session(), "admin");
        String markup = tester.getLastResponseAsString();
        assertTrue("the page shows the key that the session holds", markup.contains(key));
        assertNull("the key is not sent with the form", TagTester.createTagByAttribute(markup, "name", "j_key"));
        assertNotNull("the form sends a code", TagTester.createTagByAttribute(markup, "name", "j_otp"));
        assertTrue(TagTester.createTagByName(markup, "form").getAttribute("action").endsWith("j_superfly_otp_reset"));

        tester.startPage(LoginSetupOTPStepPage.class);
        assertTrue("a reload shows the same key", tester.getLastResponseAsString().contains(key));
    }

    @Test
    public void setupPageShowsTheMessageOfAWrongCode() {
        loginAt(setupStep());
        String key = SuperflyInitOTPAuthenticationProcessingFilter.pendingKey(session(), "admin");
        session().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new BadOTPValueException("wrong code"));

        tester.startPage(LoginSetupOTPStepPage.class);

        tester.assertRenderedPage(LoginSetupOTPStepPage.class);
        tester.assertLabel("form:reason", "The one-time password value you entered is incorrect.");
        assertTrue("the same key is shown again", tester.getLastResponseAsString().contains(key));
    }

    private static Authentication codeStep() {
        return new CompoundAuthentication(new Authentication[]{password()}, null);
    }

    private static Authentication setupStep() {
        return new CompoundAuthentication(new Authentication[]{
                password(), new LocalNeedOTPToken("admin", OTPType.GOOGLE_AUTH)}, null);
    }

    private static Authentication password() {
        return UsernamePasswordAuthenticationToken.authenticated("admin", null,
                AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
    }

    private void loginAt(Authentication authentication) {
        session().setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(authentication));
    }

    private HttpSession session() {
        return tester.getHttpSession();
    }
}
