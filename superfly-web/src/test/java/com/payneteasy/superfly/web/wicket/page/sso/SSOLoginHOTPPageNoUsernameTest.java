package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import org.easymock.EasyMock;
import org.junit.Test;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;

public class SSOLoginHOTPPageNoUsernameTest extends AbstractPageTest {
    private final CsrfValidator csrfValidator = EasyMock.createNiceMock(CsrfValidator.class);

    @Override
    protected Object getBean(Class<?> type) {
        if (type == InternalSSOService.class) {
            return EasyMock.createNiceMock(InternalSSOService.class);
        }
        if (type == SessionService.class) {
            return EasyMock.createNiceMock(SessionService.class);
        }
        if (type == SubsystemService.class) {
            return EasyMock.createNiceMock(SubsystemService.class);
        }
        if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void loginDataWithoutUsernameShowsErrorPage() {
        expect(csrfValidator.persistTokenIntoSession(anyObject())).andReturn("123").anyTimes();
        replay(csrfValidator);
        // state after anonymizeLoginData: username is cleared but the login data is still in the session
        tester.getSession().setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));

        tester.startPage(SSOLoginHOTPPage.class);

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "No login data found");
    }
}
