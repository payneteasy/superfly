package com.payneteasy.superfly.web.wicket.page.login;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.web.security.FullAuthentication;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import jakarta.servlet.http.HttpSession;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextImpl;

import static org.junit.Assert.assertNotNull;

/**
 * A login step page opened in a state of the login that does not match it sends the user to the step the login is
 * at.
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
