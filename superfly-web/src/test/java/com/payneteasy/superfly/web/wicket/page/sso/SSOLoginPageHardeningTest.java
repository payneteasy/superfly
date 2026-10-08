package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.web.wicket.page.AbstractPageTest;
import com.payneteasy.superfly.web.wicket.utils.PageParametersBuilder;
import jakarta.servlet.http.Cookie;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.easymock.EasyMock.*;

public class SSOLoginPageHardeningTest extends AbstractPageTest {
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private CsrfValidator csrfValidator;

    @Before
    public void setUp() {
        sessionService = EasyMock.createNiceMock(SessionService.class);
        subsystemService = EasyMock.createNiceMock(SubsystemService.class);
        csrfValidator = EasyMock.createNiceMock(CsrfValidator.class);
    }

    @Override
    protected Object getBean(Class<?> type) {
        if (SessionService.class == type) {
            return sessionService;
        } else if (SubsystemService.class == type) {
            return subsystemService;
        } else if (type == CsrfValidator.class) {
            return csrfValidator;
        }
        return super.getBean(type);
    }

    @Test
    public void unknownSubsystemShowsErrorPageInsteadOfFailing() {
        expect(subsystemService.getSubsystemByName("nope")).andReturn(null);
        replay(sessionService, subsystemService, csrfValidator);

        start("nope", "/target");

        tester.assertRenderedPage(SSOLoginErrorPage.class);
        tester.assertLabel("message", "Can&#039;t login");
    }

    @Test
    public void protocolRelativeTargetUrlsAreReducedToRoot() {
        assertTargetUrlSanitized("//evil.com/x", "%2F");
        assertTargetUrlSanitized("/\\evil.com", "%2F");
        assertTargetUrlSanitized("\\\\evil.com", "%2F");
        assertTargetUrlSanitized("/\t/evil.com", "%2F");
        assertTargetUrlSanitized("/\n/evil.com", "%2F");
        assertTargetUrlSanitized("http://host//evil.com", "%2F");
    }

    @Test
    public void ordinaryTargetUrlsAreKept() {
        assertTargetUrlSanitized("/app/page?x=1", "%2Fapp%2Fpage%3Fx%3D1");
        assertTargetUrlSanitized("page", "%2Fpage");
        assertTargetUrlSanitized("http://host/a/b", "%2Fa%2Fb");
    }

    private void assertTargetUrlSanitized(String targetUrl, String expectedEncoded) {
        EasyMock.reset(sessionService, subsystemService, csrfValidator);
        expect(sessionService.getValidSSOSession("sid")).andReturn(new SSOSession(1, "sid"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
        replay(sessionService, subsystemService, csrfValidator);

        tester.getRequest().addCookie(new Cookie("SSOSESSIONID", "sid"));
        start("test-subsystem", targetUrl);

        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=" + expectedEncoded);
    }

    private void start(String subsystem, String targetUrl) {
        tester.startPage(SSOLoginPage.class, PageParametersBuilder.fromMap(new HashMap<String, Object>() {{
            put("subsystemIdentifier", subsystem);
            put("targetUrl", targetUrl);
        }}));
    }
}
