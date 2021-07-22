package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.apache.wicket.util.tester.FormTester;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.*;

/**
 * @author rpuch
 */
public class SSOLoginHOTPPageTest extends AbstractPageTest {
    private InternalSSOService internalSSOService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        internalSSOService = EasyMock.createStrictMock(InternalSSOService.class);
        sessionService = EasyMock.createStrictMock(SessionService.class);
        subsystemService = EasyMock.createStrictMock(SubsystemService.class);
        csrfValidator = EasyMock.createStrictMock(CsrfValidator.class);

        expect(subsystemService.getSubsystemByName("test-subsystem"))
                .andReturn(createSubsystem()).anyTimes();

        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        csrfValidator.validateToken(anyObject());
        expectLastCall().anyTimes();
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (type == InternalSSOService.class) {
            return internalSSOService;
        }
        if (type == SessionService.class) {
            return sessionService;
        }
        if (type == SubsystemService.class) {
            return subsystemService;
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void testNoLoginData() {
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }

    @Test
    public void testSuccess() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "111111"))
                .andReturn(true);
        expect(sessionService.createSSOSession("known-user"))
                .andReturn(new SSOSession(1L, "super-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(internalSSOService, sessionService, subsystemService, csrfValidator);

        SSOLoginData loginData = createLoginData();
        tester.getSession().setSsoLoginData(loginData);
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", "111111");
        form.submit();
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        tester.assertHasCookie(SSOUtils.SSO_SESSION_ID_COOKIE_NAME, "super-session-id");

        verify(internalSSOService, sessionService, subsystemService, csrfValidator);
    }

    private UISubsystem createSubsystem() {
        UISubsystem subsystem = new UISubsystem();
        subsystem.setId(1L);
        subsystem.setName("test-subsystem");
        subsystem.setTitle("The Subsystem (tm)");
        return subsystem;
    }

    private SSOLoginData createLoginData() {
        SSOLoginData loginData = new SSOLoginData("test-subsystem", "/target");
        loginData.setUsername("known-user");
        return loginData;
    }

    @Test
    public void testFailure() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "222222"))
                        .andReturn(false);
        replay(internalSSOService, csrfValidator);

        tester.getSession().setSsoLoginData(createLoginData());
        tester.startPage(SSOLoginHOTPPage.class);
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        FormTester form = tester.newFormTester("form");
        form.setValue("hotp", "222222");
        form.submit();
        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        tester.assertLabel("form:message", "One-time password value did not match.");

        verify(internalSSOService, csrfValidator);
    }
}
