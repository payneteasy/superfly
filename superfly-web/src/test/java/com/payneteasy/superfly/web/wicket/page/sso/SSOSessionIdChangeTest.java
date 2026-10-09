package com.payneteasy.superfly.web.wicket.page.sso;

import com.payneteasy.superfly.api.CheckOtpResult;
import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.model.SSOSession;
import com.payneteasy.superfly.model.SubsystemTokenData;
import com.payneteasy.superfly.model.UserLoginStatus;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidator;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.InternalSSOService;
import com.payneteasy.superfly.service.SessionService;
import com.payneteasy.superfly.service.SettingsService;
import com.payneteasy.superfly.service.SubsystemService;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.spring.Policy;
import com.payneteasy.superfly.web.security.ratelimit.LoginAttemptLimiter;
import com.payneteasy.superfly.web.wicket.SuperflySession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpSession;
import org.apache.wicket.Component;
import org.apache.wicket.DefaultPageManagerProvider;
import org.apache.wicket.IPageManagerProvider;
import org.apache.wicket.Session;
import org.apache.wicket.application.IComponentInstantiationListener;
import org.apache.wicket.injection.IFieldValueFactory;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.pageStore.IPageStore;
import org.apache.wicket.pageStore.InSessionPageStore;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The HTTP session id changes after the password step and after a password change, while the
 * following OTP form still works.
 * <p>
 * Wicket's MockHttpServletRequest.changeSessionId() invalidates the session and drops all its
 * attributes, unlike a servlet container. Here the container request is wrapped so that the id
 * changes but the attributes stay, as in Jetty/Tomcat. Pages are kept in an HTTP session attribute
 * (as Wicket's in-session store does; the disk store keys pages by an identifier that is a session
 * attribute too) and the real CSRF validator is used, so pages and the CSRF token must survive the
 * id change. Pages are not serialized because the injected mocks are not serializable.
 */
public class SSOSessionIdChangeTest {

    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName() + ".CSRF_TOKEN";

    private final Map<Class<?>, Object> beans = new HashMap<>();
    private WicketTester tester;
    private UserService userService;
    private SessionService sessionService;
    private SubsystemService subsystemService;
    private InternalSSOService internalSSOService;
    private SettingsService settingsService;

    @Before
    public void setUp() {
        LoginAttemptLimiter.install(LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP);
        userService = createMock(UserService.class);
        sessionService = createMock(SessionService.class);
        subsystemService = createMock(SubsystemService.class);
        internalSSOService = createMock(InternalSSOService.class);
        settingsService = createNiceMock(SettingsService.class);
        beans.put(UserService.class, userService);
        beans.put(SessionService.class, sessionService);
        beans.put(SubsystemService.class, subsystemService);
        beans.put(InternalSSOService.class, internalSSOService);
        beans.put(SettingsService.class, settingsService);
        beans.put(CsrfValidator.class, new CsrfValidatorImpl(true));

        ContainerLikeApplication application = new ContainerLikeApplication();
        tester = new WicketTester(application) {
            @Override
            protected IPageManagerProvider newTestPageManagerProvider() {
                return new SessionPageManagerProvider(application);
            }
        };
        application.getComponentInstantiationListeners().add(new BeanInjector());
        expect(subsystemService.getSubsystemByName(anyString())).andReturn(null).anyTimes();
        expect(settingsService.getPolicy()).andReturn(Policy.NONE).anyTimes();
    }

    @Test
    public void testSessionIdChangesAfterPasswordAndOtpFormStillWorks() {
        expect(userService.checkUserCanLoginWithThisPassword("known-user", "password", "test-subsystem"))
                .andReturn(UserLoginStatus.SUCCESS);
        expect(userService.getUserForDescription("known-user")).andReturn(googleAuthUser()).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("key").anyTimes();
        expectOtpAndToken();
        replay(userService, sessionService, subsystemService, internalSSOService, settingsService);

        startWithLoginData(SSOLoginPasswordPage.class);
        String idBeforePassword = tester.getHttpSession().getId();
        FormTester form = tester.newFormTester("form");
        form.setValue("username", "known-user");
        form.setValue("password", "password");
        submitWithCsrf(form);

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        String idAfterPassword = tester.getHttpSession().getId();
        assertNotEquals("session id must change after the password step", idBeforePassword, idAfterPassword);

        submitOtpAndAssertRedirect(idAfterPassword);
        verify(userService, sessionService, subsystemService, internalSSOService);
    }

    @Test
    public void testSessionIdChangesAfterPasswordChangeAndOtpFormStillWorks() throws Exception {
        userService.validatePassword("known-user", "new-password");
        expectLastCall();
        userService.changeTempPassword("known-user", "new-password");
        expectLastCall();
        expect(userService.getUserForDescription("known-user")).andReturn(googleAuthUser()).anyTimes();
        expect(userService.getOtpMasterKeyByUsername("known-user")).andReturn("key").anyTimes();
        expectOtpAndToken();
        replay(userService, sessionService, subsystemService, internalSSOService, settingsService);

        tester.getSession().bind();
        ((SuperflySession) tester.getSession()).setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(new SSOChangePasswordPage("known-user"));
        tester.assertRenderedPage(SSOChangePasswordPage.class);
        String idBeforeChange = tester.getHttpSession().getId();
        FormTester form = tester.newFormTester("change-password-panel:form");
        form.setValue("password", "new-password");
        form.setValue("password2", "new-password");
        submitWithCsrf(form);

        tester.assertRenderedPage(SSOLoginHOTPPage.class);
        String idAfterChange = tester.getHttpSession().getId();
        assertNotEquals("session id must change after the password change", idBeforeChange, idAfterChange);

        submitOtpAndAssertRedirect(idAfterChange);
        verify(userService, sessionService, subsystemService, internalSSOService);
    }

    private void expectOtpAndToken() {
        expect(internalSSOService.authenticateByOtpType(OTPType.GOOGLE_AUTH, "known-user", "111111"))
                .andReturn(CheckOtpResult.Status.SUCCESS);
        expect(sessionService.createSSOSession("known-user")).andReturn(new SSOSession(1L, "sso-session-id"));
        expect(subsystemService.issueSubsystemTokenIfCanLogin(1L, "test-subsystem"))
                .andReturn(new SubsystemTokenData("abcdef", "http://some.host.test/landing-url"));
    }

    private void submitOtpAndAssertRedirect(String idBeforeOtp) {
        FormTester otpForm = tester.newFormTester("form");
        otpForm.setValue("hotp", "111111");
        submitWithCsrf(otpForm);
        tester.assertRedirectUrl("http://some.host.test/landing-url?subsystemToken=abcdef&targetUrl=%2Ftarget");
        assertNotEquals("session id must change before the token is issued", idBeforeOtp,
                tester.getHttpSession().getId());
    }

    private void startWithLoginData(Class<? extends BaseSSOPage> pageClass) {
        tester.getSession().bind();
        ((SuperflySession) tester.getSession()).setSsoLoginData(new SSOLoginData("test-subsystem", "/target"));
        tester.startPage(pageClass);
        tester.assertRenderedPage(pageClass);
    }

    /** The hidden CSRF field is renamed to "_csrf" only in the markup, so the parameter is set explicitly. */
    private void submitWithCsrf(FormTester form) {
        Object token = tester.getHttpSession().getAttribute(CSRF_ATTRIBUTE);
        assertNotNull("CSRF token must be in the session", token);
        tester.getRequest().getPostParameters().setParameterValue("_csrf", (String) token);
        form.submit();
    }

    private static UserForDescription googleAuthUser() {
        UserForDescription user = new UserForDescription();
        user.setUsername("known-user");
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        user.setOtpOptional(false);
        return user;
    }

    private class ContainerLikeApplication extends MockApplication {
        @Override
        protected void init() {
            super.init();
            getResourceSettings().getResourceFinders().add(0, new Path("src/main/java"));
        }

        @Override
        public Session newSession(Request request, Response response) {
            return new SuperflySession(request);
        }

        @Override
        public WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath) {
            return super.newWebRequest(new AttributeKeepingRequest(servletRequest), filterPath);
        }
    }

    /** Wicket's page store chain reduced to pages kept as instances in the HTTP session. */
    private static class SessionPageManagerProvider extends DefaultPageManagerProvider {
        SessionPageManagerProvider(MockApplication application) {
            super(application);
        }

        @Override
        protected IPageStore newPersistentStore() {
            return new InSessionPageStore(10);
        }

        @Override
        protected IPageStore newSerializingStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newCryptingStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newAsynchronousStore(IPageStore pageStore) {
            return pageStore;
        }

        @Override
        protected IPageStore newCachingStore(IPageStore pageStore) {
            return pageStore;
        }
    }

    /** changeSessionId() as a servlet container does it: a new id, the same attributes. */
    private static class AttributeKeepingRequest extends HttpServletRequestWrapper {
        AttributeKeepingRequest(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String changeSessionId() {
            HttpSession session = getSession(false);
            if (session == null) {
                throw new IllegalStateException("No session");
            }
            Map<String, Object> attributes = new HashMap<>();
            for (String name : Collections.list(session.getAttributeNames())) {
                attributes.put(name, session.getAttribute(name));
            }
            String newId = super.changeSessionId();
            HttpSession renewed = getSession(false);
            attributes.forEach(renewed::setAttribute);
            return newId;
        }
    }

    private class BeanInjector extends Injector implements IComponentInstantiationListener {
        private final IFieldValueFactory factory = new IFieldValueFactory() {
            @Override
            public Object getFieldValue(Field field, Object fieldOwner) {
                return beans.get(field.getType());
            }

            @Override
            public boolean supportsField(Field field) {
                return beans.containsKey(field.getType());
            }
        };

        @Override
        public void onInstantiation(Component component) {
            inject(component);
        }

        @Override
        public void inject(Object object) {
            inject(object, factory);
        }
    }
}
