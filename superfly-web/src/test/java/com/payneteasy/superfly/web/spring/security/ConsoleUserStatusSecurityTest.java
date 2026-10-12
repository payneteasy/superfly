package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UIUserDetails;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.UserService;
import com.payneteasy.superfly.web.security.FullAuthentication;
import jakarta.servlet.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The admin console session through the real security filter chain: once the logged-in user is locked, suspended
 * or deleted, the next console request ends the session and goes to the login page. Anonymous requests, login
 * steps and public pages do not read the account status.
 */
public class ConsoleUserStatusSecurityTest {

    private static final String CONTEXT_PATH = "/superfly";
    private static final String USERNAME     = "admin";
    private static final String PASSWORD     = "pw";
    private static final String OTP_CODE     = "123456";
    private static final long   USER_ID      = 7;

    /** The user row the way get_user_for_description and ui_get_user read it; null once the user is deleted. */
    private UIUserDetails account;
    private int           statusReads;

    private final List<String> securityLog = new ArrayList<>();
    private AnnotationConfigWebApplicationContext context;

    @Before
    public void createActiveAccount() {
        account = new UIUserDetails();
    }

    @After
    public void tearDown() {
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void lockedUserIsLoggedOutOnTheNextPageRequest() throws Exception {
        Browser browser = loginWithOtp();
        account.setAccountLocked(true);

        assertLoggedOut(browser, browser.get("/users"));
        assertEquals(List.of("LOCAL_SESSION_CHECK admin failure status=locked"), securityLog);
    }

    @Test
    public void lockedUserIsLoggedOutOnTheNextWicketAjaxRequest() throws Exception {
        Browser browser = loginWithOtp();
        account.setAccountLocked(true);

        Reply reply = browser.ajax("/users");

        assertNull("the request reached the application", reply.chain.getRequest());
        assertTrue("the session is not invalidated", browser.session.isInvalid());
        assertNull("XHR would follow a redirect and hand the login page to Wicket", reply.response.getRedirectedUrl());
        assertEquals(200, reply.response.getStatus());
        assertEquals(CONTEXT_PATH + "/login", reply.response.getHeader("Ajax-Location"));
        assertEquals(List.of("LOCAL_SESSION_CHECK admin failure status=locked"), securityLog);
    }

    @Test
    public void suspendedUserIsLoggedOut() throws Exception {
        Browser browser = loginWithOtp();
        // suspension locks the account as well
        account.setAccountLocked(true);
        account.setAccountSuspended(true);

        assertLoggedOut(browser, browser.get("/users"));
        assertEquals(List.of("LOCAL_SESSION_CHECK admin failure status=suspended"), securityLog);
    }

    @Test
    public void deletedUserIsLoggedOut() throws Exception {
        Browser browser = loginWithOtp();
        account = null;

        assertLoggedOut(browser, browser.get("/users"));
        assertEquals(List.of("LOCAL_SESSION_CHECK admin failure status=not_found"), securityLog);
    }

    @Test
    public void lockedUserLoggedInWithPasswordOnlyIsLoggedOut() throws Exception {
        Browser browser = new Browser(startConsole(false));
        passwordStep(browser);
        assertTrue(authentication(browser) instanceof UsernamePasswordAuthenticationToken);
        assertNotNull(browser.get("/users").chain.getRequest());

        account.setAccountLocked(true);

        assertLoggedOut(browser, browser.get("/users"));
    }

    @Test
    public void activeUserKeepsTheSession() throws Exception {
        Browser browser = loginWithOtp();

        for (String uri : List.of("/", "/users", "/roles", "/sessions")) {
            Reply reply = browser.get(uri);
            assertNull(uri + " was redirected to " + reply.response.getRedirectedUrl(), reply.response.getRedirectedUrl());
            assertNotNull(uri + " did not reach the application", reply.chain.getRequest());
        }
        Reply ajax = browser.ajax("/users");
        assertNotNull("the ajax request did not reach the application", ajax.chain.getRequest());
        assertNull(ajax.response.getHeader("Ajax-Location"));

        assertFalse(browser.session.isInvalid());
        assertEquals("the status is read on every console request", 5, statusReads);
        assertEquals(List.of(), securityLog);
    }

    @Test
    public void anonymousRequestsAndLoginStepsDoNotReadTheStatus() throws Exception {
        Browser browser = new Browser(startConsole(true));
        Reply anonymous = browser.get("/users");
        assertTrue(anonymous.response.getRedirectedUrl(), anonymous.response.getRedirectedUrl().endsWith("/login"));

        passwordStep(browser);
        Reply otpPending = browser.get("/users");
        assertTrue(otpPending.response.getRedirectedUrl(), otpPending.response.getRedirectedUrl().endsWith("/login-step2"));
        assertNotNull(browser.get("/login-step2").chain.getRequest());

        assertEquals(302, browser.post("/j_superfly_otp_security_check", "j_otp", OTP_CODE).getStatus());
        assertEquals(0, statusReads);
    }

    @Test
    public void publicPagesDoNotReadTheStatus() throws Exception {
        Browser browser = loginWithOtp();
        account.setAccountLocked(true);

        for (String uri : List.of("/sso/login", "/login", "/css/style.css", "/favicon.ico", "/management/version.txt")) {
            assertNotNull(uri + " did not reach the application", browser.get(uri).chain.getRequest());
        }
        assertEquals(0, statusReads);
        assertFalse(browser.session.isInvalid());
    }

    private Browser loginWithOtp() throws Exception {
        Browser browser = new Browser(startConsole(true));
        passwordStep(browser);
        assertEquals(302, browser.post("/j_superfly_otp_security_check", "j_otp", OTP_CODE).getStatus());
        assertTrue(authentication(browser) instanceof FullAuthentication);
        return browser;
    }

    private static void passwordStep(Browser browser) throws Exception {
        assertEquals(302, browser.post("/j_superfly_password_security_check",
                "j_username", USERNAME, "j_password", PASSWORD).getStatus());
    }

    private static Authentication authentication(Browser browser) {
        SecurityContext securityContext = (SecurityContext) browser.session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertNotNull("no security context in the session", securityContext);
        return securityContext.getAuthentication();
    }

    private static void assertLoggedOut(Browser browser, Reply reply) throws Exception {
        assertNull("the request reached the application", reply.chain.getRequest());
        assertTrue("the session is not invalidated", browser.session.isInvalid());
        assertEquals(302, reply.response.getStatus());
        assertEquals(CONTEXT_PATH + "/login", reply.response.getRedirectedUrl());

        Reply next = browser.get("/users");
        assertNull("the next request reached the application", next.chain.getRequest());
        assertTrue(next.response.getRedirectedUrl(), next.response.getRedirectedUrl().endsWith("/login"));
    }

    private Filter startConsole(boolean multiFactorAuth) {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties()
                    .csrfLoginValidatorEnable(false)
                    .enableMultiFactorAuth(multiFactorAuth)
                    .forceMultiFactorAuthMethod(OTPType.GOOGLE_AUTH));
            beanFactory.registerSingleton("loggerSink", new RecordingLoggerSink());
            beanFactory.registerSingleton("localSecurityService", localSecurityService());
            beanFactory.registerSingleton("userService", userService());
            beanFactory.registerSingleton("userDetailsService", mock(UserDetailsService.class));
            beanFactory.registerSingleton("mvcHandlerMappingIntrospector", new HandlerMappingIntrospector());
        });
        context.refresh();
        return context.getBean("springSecurityFilterChain", Filter.class);
    }

    /** Password and TOTP of the administrator; the login steps read nothing else. */
    private static LocalSecurityService localSecurityService() {
        UserForDescription user = new UserForDescription();
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        LocalSecurityService service = niceMock(LocalSecurityService.class);
        expect(service.authenticate(USERNAME, PASSWORD)).andStubReturn(new String[]{"ADMIN"});
        expect(service.getOtpUserForDescription(USERNAME)).andStubReturn(
                new OtpUserDescription().setHasOtpMasterKey(true).setUserForDescription(user));
        expect(service.authenticateUsingOTP(USERNAME, OTP_CODE)).andStubReturn(true);
        replay(service);
        return service;
    }

    /** Reads {@link #account} at the moment of the call; any other call fails the test. */
    private UserService userService() {
        UserService service = createMock(UserService.class);
        expect(service.getUserForDescription(USERNAME)).andStubAnswer(() -> {
            statusReads++;
            if (account == null) {
                return null;
            }
            UserForDescription description = new UserForDescription();
            description.setUserId(USER_ID);
            description.setUsername(USERNAME);
            return description;
        });
        expect(service.getUser(USER_ID)).andStubAnswer(() -> account);
        replay(service);
        return service;
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }

    private final class RecordingLoggerSink implements LoggerSink {
        @Override
        public void info(Logger logger, String eventType, boolean success, String resourceIdentity) {
            info(logger, eventType, success, resourceIdentity, null);
        }

        @Override
        public void info(Logger logger, String eventType, boolean success, String resourceIdentity, String details) {
            securityLog.add(eventType + " " + resourceIdentity + " " + (success ? "success" : "failure") + " " + details);
        }
    }

    /** A browser: the session cookie lives across requests. */
    private static final class Browser {
        private final Filter          console;
        private final MockHttpSession session = new MockHttpSession();

        Browser(Filter console) {
            this.console = console;
        }

        MockHttpServletResponse post(String path, String... nameValues) throws Exception {
            MockHttpServletRequest request = request("POST", path);
            for (int i = 0; i < nameValues.length; i += 2) {
                request.setParameter(nameValues[i], nameValues[i + 1]);
            }
            return send(request).response;
        }

        Reply get(String path) throws Exception {
            return send(request("GET", path));
        }

        /** What Wicket's JavaScript sends for an ajax call of a page component. */
        Reply ajax(String path) throws Exception {
            MockHttpServletRequest request = request("GET", path);
            request.addHeader("Wicket-Ajax", "true");
            request.addHeader("Wicket-Ajax-BaseURL", path.substring(1));
            request.addHeader("X-Requested-With", "XMLHttpRequest");
            return send(request);
        }

        private MockHttpServletRequest request(String method, String path) {
            MockHttpServletRequest request = new MockHttpServletRequest(method, CONTEXT_PATH + path);
            request.setContextPath(CONTEXT_PATH);
            request.setServletPath(path);
            request.setSession(session);
            return request;
        }

        private Reply send(MockHttpServletRequest request) throws Exception {
            Reply reply = new Reply();
            console.doFilter(request, reply.response, reply.chain);
            return reply;
        }
    }

    private static final class Reply {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain         chain    = new MockFilterChain();
    }
}
