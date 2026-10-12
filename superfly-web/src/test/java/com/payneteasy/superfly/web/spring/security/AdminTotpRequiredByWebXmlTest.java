package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.security.exception.BadOTPValueException;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.SecurityUtils;
import com.payneteasy.superfly.web.security.SpringSecurityAuthorizationStrategy;
import com.payneteasy.superfly.web.security.SuperflyInitOTPAuthenticationProcessingFilter;
import com.payneteasy.superfly.web.spring.ApplicationParameterResolver;
import com.payneteasy.superfly.web.spring.SpringAppPropertiesConfiguration;
import com.payneteasy.superfly.web.wicket.page.user.ChangePasswordPage;
import com.payneteasy.superfly.web.wicket.page.user.ListUsersPage;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import jakarta.servlet.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * The admin console login with the settings of the production {@code web.xml}: the context parameters go through
 * {@link SpringAppPropertiesConfiguration} into the real security filter chain, and every administrator has to
 * present a TOTP code. A user without a TOTP key sets one up first; until the last step is done no page of the
 * console is served.
 */
public class AdminTotpRequiredByWebXmlTest {

    private static final File   WEB_XML        = new File("src/main/webapp/WEB-INF/web.xml");
    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");
    private static final String MFA_ENABLED    = "superfly-mfa-enabled";
    private static final String MFA_FORCE      = "superfly-mfa-method-force";

    private static final String USERNAME   = "admin";
    private static final String PASSWORD   = "pw";
    private static final String VALID_CODE = "123456";

    private static final List<String> CONSOLE_PAGES = List.of("/", "/users", "/users/create", "/actions", "/groups",
            "/roles", "/subsystems", "/sessions", "/changepassword", "/smtp-servers");

    private String savedEnabled;
    private String savedForce;
    private AnnotationConfigWebApplicationContext context;

    @Before
    public void saveSystemProperties() {
        savedEnabled = System.getProperty(MFA_ENABLED);
        savedForce = System.getProperty(MFA_FORCE);
        System.clearProperty(MFA_ENABLED);
        System.clearProperty(MFA_FORCE);
    }

    @After
    public void tearDown() {
        restore(MFA_ENABLED, savedEnabled);
        restore(MFA_FORCE, savedForce);
        if (context != null) {
            context.close();
        }
    }

    @Test
    public void webXmlRequiresTotpForEveryAdministrator() throws Exception {
        SuperflyProperties properties = propertiesFromWebXml();

        assertEquals(Boolean.TRUE, properties.enableMultiFactorAuth());
        assertEquals(OTPType.GOOGLE_AUTH, properties.forceMultiFactorAuthMethod());
    }

    @Test
    public void systemPropertyTakesPrecedenceOverWebXml() throws Exception {
        System.setProperty(MFA_ENABLED, "false");
        System.setProperty(MFA_FORCE, "none");

        SuperflyProperties properties = propertiesFromWebXml();

        assertEquals(Boolean.FALSE, properties.enableMultiFactorAuth());
        assertEquals(OTPType.NONE, properties.forceMultiFactorAuthMethod());
    }

    @Test
    public void passwordAloneOpensNothingForUserWithoutTotpKey() throws Exception {
        Browser browser = passwordStep(startConsole(new AdminStore("ADMIN")));

        assertConsoleClosed(browser, "/login-setup");
        assertPassesFilterChain(browser, "/login-setup");
    }

    @Test
    public void totpSetupIsConfirmedByCodeBeforeAnyPageOpens() throws Exception {
        AdminStore store = new AdminStore("ADMIN");
        Filter console = startConsole(store);
        Browser browser = passwordStep(console);
        String key = browser.setupPageKey();
        String code = codeOf(key);

        assertEquals(302, browser.post("/j_superfly_otp_reset", "j_otp", code).getStatus());

        assertEquals("the key is stored for the user, as Google Authenticator key", OTPType.GOOGLE_AUTH, store.otpType);
        assertEquals("the key shown on the setup page is stored", key, store.otpKey);
        assertEquals("the code goes through the check of the code step, which records its time step",
                List.of(code), store.checkedCodes);
        assertConsoleOpen(browser);
        assertEquals(List.of("ROLE_ADMIN"), authorities(browser));

        Browser next = passwordStep(console);
        assertConsoleClosed(next, "/login-step2");
        assertPassesFilterChain(next, "/login-step2");

        MockHttpServletResponse wrongCode = next.post("/j_superfly_otp_security_check", "j_otp", "000000");
        assertTrue(wrongCode.getRedirectedUrl(), wrongCode.getRedirectedUrl().endsWith("/login"));
        assertConsoleClosed(next, "/login");

        Browser retry = passwordStep(console);
        assertConsoleClosed(retry, "/login-step2");
        assertEquals(302, retry.post("/j_superfly_otp_security_check", "j_otp", VALID_CODE).getStatus());
        assertConsoleOpen(retry);
        assertEquals(List.of("ROLE_ADMIN"), authorities(retry));
    }

    @Test
    public void wrongSetupCodeStoresNothingAndKeepsTheUserOnTheSetupPage() throws Exception {
        AdminStore store = new AdminStore("ADMIN");
        Browser browser = passwordStep(startConsole(store));
        String key = browser.setupPageKey();

        MockHttpServletResponse wrongCode = browser.post("/j_superfly_otp_reset", "j_otp", wrongCodeOf(key));

        assertTrue(wrongCode.getRedirectedUrl(), wrongCode.getRedirectedUrl().endsWith("/login-setup"));
        assertEquals("no key is stored", 0, store.keysStored);
        assertTrue("the setup page shows the message of a wrong code",
                browser.session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof BadOTPValueException);
        assertConsoleClosed(browser, "/login-setup");
        assertEquals("the same key is shown again", key, browser.setupPageKey());

        String code = codeOf(key);
        assertEquals(302, browser.post("/j_superfly_otp_reset", "j_otp", code).getStatus());
        assertEquals(key, store.otpKey);
        assertConsoleOpen(browser);
    }

    @Test
    public void wrongSetupCodesAreLimitedLikeTheOtherLoginSteps() throws Exception {
        AdminStore store = new AdminStore("ADMIN");
        Browser browser = passwordStep(startConsole(store));
        String key = browser.setupPageKey();

        for (int i = 0; i < 5; i++) {
            assertEquals(302, browser.post("/j_superfly_otp_reset", "j_otp", wrongCodeOf(key)).getStatus());
        }

        assertEquals(429, browser.post("/j_superfly_otp_reset", "j_otp", codeOf(key)).getStatus());
        assertEquals(0, store.keysStored);
    }

    @Test
    public void setupKeyIsNotTakenFromTheRequest() throws Exception {
        AdminStore store = new AdminStore("ADMIN");
        Browser browser = passwordStep(startConsole(store));
        String key = browser.setupPageKey();
        String otherKey = new GoogleAuthenticator().createCredentials().getKey();

        MockHttpServletResponse otherKeyCode = browser.post("/j_superfly_otp_reset",
                "j_key", otherKey, "j_otp", codeOf(otherKey));
        assertTrue(otherKeyCode.getRedirectedUrl(), otherKeyCode.getRedirectedUrl().endsWith("/login-setup"));
        assertEquals("a key sent with the form is not stored", 0, store.keysStored);

        assertEquals(302, browser.post("/j_superfly_otp_reset", "j_key", otherKey, "j_otp", codeOf(key)).getStatus());
        assertEquals("the key shown on the setup page is stored", key, store.otpKey);
        assertConsoleOpen(browser);
    }

    @Test
    public void codeStepRequestDuringSetupEndsTheLoginWithoutServerError() throws Exception {
        AdminStore store = new AdminStore("ADMIN");
        Browser browser = passwordStep(startConsole(store));

        MockHttpServletResponse response = browser.post("/j_superfly_otp_security_check", "j_otp", VALID_CODE);

        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl(), response.getRedirectedUrl().endsWith("/login"));
        assertEquals(0, store.keysStored);
        assertConsoleClosed(browser, "/login");
    }

    @Test
    public void userWithTotpKeyIsAskedForTheCodeAfterPassword() throws Exception {
        AdminStore store = new AdminStore("ADMIN").withTotpKey("EXISTINGKEY");
        Browser browser = passwordStep(startConsole(store));

        assertConsoleClosed(browser, "/login-step2");

        assertEquals(302, browser.post("/j_superfly_otp_security_check", "j_otp", VALID_CODE).getStatus());
        assertConsoleOpen(browser);
        assertEquals("the stored key is not replaced by the login", "EXISTINGKEY", store.otpKey);
    }

    @Test
    public void temporaryPasswordUserSetsUpTotpFirstAndKeepsOnlyThePasswordChangeRole() throws Exception {
        // a user with a temporary password has the single action action_temp_password instead of the admin ones
        Browser browser = passwordStep(startConsole(new AdminStore("action_temp_password")));

        assertConsoleClosed(browser, "/login-setup");
        assertEquals(302, browser.post("/j_superfly_otp_reset", "j_otp", codeOf(browser.setupPageKey())).getStatus());

        assertEquals(List.of("ROLE_ACTION_TEMP_PASSWORD"), authorities(browser));
        // SuperflyApplication shows the password change page instead of any other page for this role
        SecurityContextHolder.setContext((SecurityContext) browser.session.getAttribute("SPRING_SECURITY_CONTEXT"));
        try {
            assertTrue(SecurityUtils.isTempPassword());
            SpringSecurityAuthorizationStrategy strategy = new SpringSecurityAuthorizationStrategy();
            assertFalse(strategy.isInstantiationAuthorized(ListUsersPage.class));
            assertTrue(strategy.isInstantiationAuthorized(ChangePasswordPage.class));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    /** The real security filter chain of the admin console, configured by the production descriptor. */
    private Filter startConsole(AdminStore store) throws Exception {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        SuperflyProperties properties = propertiesFromWebXml();
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", properties);
            beanFactory.registerSingleton("loggerSink", mock(LoggerSink.class));
            beanFactory.registerSingleton("localSecurityService", store);
            beanFactory.registerSingleton("userService", ActiveAccounts.userService());
            beanFactory.registerSingleton("userDetailsService", mock(UserDetailsService.class));
            beanFactory.registerSingleton("mvcHandlerMappingIntrospector", new HandlerMappingIntrospector());
        });
        context.refresh();
        return context.getBean("springSecurityFilterChain", Filter.class);
    }

    private static Browser passwordStep(Filter console) throws Exception {
        Browser browser = new Browser(console);
        assertEquals(302, browser.post("/j_superfly_password_security_check",
                "j_username", USERNAME, "j_password", PASSWORD).getStatus());
        return browser;
    }

    private static void assertConsoleClosed(Browser browser, String expectedStep) throws Exception {
        for (String uri : CONSOLE_PAGES) {
            Reply reply = browser.get(uri);
            assertNull(uri + " reached the application", reply.chain.getRequest());
            assertEquals(uri, 302, reply.response.getStatus());
            assertTrue(uri + " went to " + reply.response.getRedirectedUrl(),
                    reply.response.getRedirectedUrl().endsWith(expectedStep));
        }
    }

    private static void assertConsoleOpen(Browser browser) throws Exception {
        for (String uri : CONSOLE_PAGES) {
            assertPassesFilterChain(browser, uri);
        }
    }

    private static void assertPassesFilterChain(Browser browser, String uri) throws Exception {
        Reply reply = browser.get(uri);
        assertNull(uri + " was redirected to " + reply.response.getRedirectedUrl(), reply.response.getRedirectedUrl());
        assertNotNull(uri + " did not reach the application", reply.chain.getRequest());
    }

    private static List<String> authorities(Browser browser) {
        SecurityContext securityContext = (SecurityContext) browser.session.getAttribute("SPRING_SECURITY_CONTEXT");
        assertNotNull("no security context in the session", securityContext);
        Authentication authentication = securityContext.getAuthentication();
        assertTrue("not authenticated: " + authentication, authentication.isAuthenticated());
        return authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
    }

    /** What the container does with the production descriptor: its context parameters become init parameters. */
    private static SuperflyProperties propertiesFromWebXml() throws Exception {
        MockServletContext servletContext = new MockServletContext();
        Document webXml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(WEB_XML);
        NodeList params = webXml.getElementsByTagName("context-param");
        for (int i = 0; i < params.getLength(); i++) {
            Element param = (Element) params.item(i);
            servletContext.addInitParameter(text(param, "param-name"), text(param, "param-value"));
        }
        // placeholders: the key material comes from the environment in a real deployment
        servletContext.addInitParameter("superfly-cryptoSecret", "dummy");
        servletContext.addInitParameter("superfly-cryptoSalt", "dummy");
        return new SpringAppPropertiesConfiguration(new ApplicationParameterResolver(servletContext)).superflyProperties();
    }

    private static String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent().trim();
    }

    private static void restore(String name, String value) {
        if (value == null) {
            System.clearProperty(name);
        } else {
            System.setProperty(name, value);
        }
    }

    private static String codeOf(String key) {
        return String.format("%06d", new GoogleAuthenticator().getTotpPassword(key));
    }

    /** A well-formed code that the key does not accept now. */
    private static String wrongCodeOf(String key) {
        int code = 1;
        while (isCodeOf(key, String.format("%06d", code))) {
            code++;
        }
        return String.format("%06d", code);
    }

    private static boolean isCodeOf(String key, String code) {
        return code.matches("[0-9]{6}") && new GoogleAuthenticator().authorize(key, Integer.parseInt(code));
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }

    /** One administrator in memory: the password, the OTP type and the TOTP key the way the database holds them. */
    private static final class AdminStore implements LocalSecurityService {
        private final String[]     actions;
        private final List<String> checkedCodes = new ArrayList<>();
        private OTPType            otpType      = OTPType.NONE;
        private String             otpKey;
        private int                keysStored;

        AdminStore(String... actions) {
            this.actions = actions;
        }

        AdminStore withTotpKey(String key) {
            otpType = OTPType.GOOGLE_AUTH;
            otpKey = key;
            return this;
        }

        @Override
        public String[] authenticate(String username, String password) {
            return USERNAME.equals(username) && PASSWORD.equals(password) ? actions : null;
        }

        /** {@link #VALID_CODE} stands for the code of a key enrolled before the test; a key set up in the test is checked. */
        @Override
        public boolean authenticateUsingOTP(String username, String otp) {
            checkedCodes.add(otp);
            return otpKey != null && (VALID_CODE.equals(otp) || isCodeOf(otpKey, otp));
        }

        @Override
        public OtpUserDescription getOtpUserForDescription(String username) {
            UserForDescription user = new UserForDescription();
            user.setOtpTypeCode(otpType.code());
            return new OtpUserDescription().setHasOtpMasterKey(otpKey != null).setUserForDescription(user);
        }

        @Override
        public void persistOtpKey(OTPType type, String username, String key) {
            keysStored++;
            otpType = type;
            otpKey = key;
        }
    }

    /** A browser session: the cookie (session) lives across requests. */
    private static final class Browser {
        private final Filter          securityFilterChain;
        private final MockHttpSession session = new MockHttpSession();

        Browser(Filter securityFilterChain) {
            this.securityFilterChain = securityFilterChain;
            session.setAttribute(CSRF_ATTRIBUTE, "token");
        }

        MockHttpServletResponse post(String uri, String... nameValues) throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
            request.setParameter("_csrf", "token");
            for (int i = 0; i < nameValues.length; i += 2) {
                request.setParameter(nameValues[i], nameValues[i + 1]);
            }
            return send(request).response;
        }

        Reply get(String uri) throws Exception {
            return send(new MockHttpServletRequest("GET", uri));
        }

        /** What {@code /login-setup} shows: the page issues the key into the session. */
        String setupPageKey() {
            return SuperflyInitOTPAuthenticationProcessingFilter.pendingKey(session, USERNAME);
        }

        private Reply send(MockHttpServletRequest request) throws Exception {
            request.setServletPath(request.getRequestURI());
            request.setSession(session);
            Reply reply = new Reply();
            securityFilterChain.doFilter(request, reply.response, reply.chain);
            return reply;
        }
    }

    private static final class Reply {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain         chain    = new MockFilterChain();
    }
}
