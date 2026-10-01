package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.authentication.CompoundAuthentication;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.security.LocalNeedOTPToken;
import jakarta.servlet.Filter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Multi-step login through the real security filter chain: an incomplete (compound) authentication
 * must be redirected to the step page, not fail with 500.
 */
public class MfaLoginStepSecurityTest {

    private static AnnotationConfigWebApplicationContext context;
    private static Filter securityFilterChain;
    private static LocalSecurityService localSecurityService;

    @BeforeClass
    public static void setUp() {
        localSecurityService = createMock(LocalSecurityService.class);
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties()
                    .csrfLoginValidatorEnable(true)
                    .enableMultiFactorAuth(true));
            beanFactory.registerSingleton("loggerSink", mock(LoggerSink.class));
            beanFactory.registerSingleton("localSecurityService", localSecurityService);
            beanFactory.registerSingleton("userDetailsService", mock(UserDetailsService.class));
            beanFactory.registerSingleton("mvcHandlerMappingIntrospector", new HandlerMappingIntrospector());
        });
        context.refresh();
        securityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);
    }

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void protectedPageWithPendingOtpRedirectsToOtpStep() throws Exception {
        Result result = get("/users", otpPending());
        assertEquals(302, result.response.getStatus());
        assertTrue(result.response.getRedirectedUrl(), result.response.getRedirectedUrl().endsWith("/login-setup"));
    }

    @Test
    public void otpStepPageIsNotRedirectedAndKeepsHeaders() throws Exception {
        Result result = get("/login-setup", otpPending());
        assertNull(result.response.getRedirectedUrl());
        assertNotNull(result.chain.getRequest());
        assertNotNull(result.response.getHeader("Content-Security-Policy"));
    }

    @Test
    public void anonymousIsRedirectedToLogin() throws Exception {
        Result result = get("/users", null);
        assertEquals(302, result.response.getStatus());
        assertTrue(result.response.getRedirectedUrl(), result.response.getRedirectedUrl().endsWith("/login"));
    }

    @Test
    public void fullyAuthenticatedAdminPasses() throws Exception {
        Authentication admin = UsernamePasswordAuthenticationToken.authenticated(
                "admin", "n/a", AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        Result result = get("/users", admin);
        assertNull(result.response.getRedirectedUrl());
        assertNotNull(result.chain.getRequest());
    }

    @Test
    public void publicPathsAreNotTouchedByStepHandling() throws Exception {
        for (String uri : new String[]{"/sso/login", "/sso/check/check-password/billing/admin", "/css/style.css",
                "/favicon.ico", "/management/version.txt"}) {
            Result result = get(uri, otpPending());
            assertNull(uri, result.response.getRedirectedUrl());
            assertNotNull(uri, result.chain.getRequest());
        }
    }

    @Test
    public void otpInitStepPersistsKey() throws Exception {
        reset(localSecurityService);
        localSecurityService.persistOtpKey(OTPType.GOOGLE_AUTH, "admin", "KEY");
        expectLastCall().once();
        UserForDescription user = new UserForDescription();
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        expect(localSecurityService.getOtpUserForDescription("admin")).andReturn(
                new OtpUserDescription().setHasOtpMasterKey(true).setUserForDescription(user)).anyTimes();
        expect(localSecurityService.authenticate("admin", "pw")).andReturn(new String[]{"ADMIN"}).anyTimes();
        replay(localSecurityService);

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/j_superfly_otp_reset");
        request.setServletPath("/j_superfly_otp_reset");
        request.setParameter("j_key", "KEY");
        request.setParameter("_csrf", "token");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(new CompoundAuthentication(new Authentication[]{
                new UsernamePasswordAuthenticationToken("admin", "pw"), new LocalNeedOTPToken("admin", OTPType.GOOGLE_AUTH)}, null)));
        session.setAttribute(CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN"), "token");
        request.setSession(session);
        securityFilterChain.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        verify(localSecurityService);
    }

    @Test
    public void otpInitStepWithoutPendingOtpRedirectsToLogin() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/j_superfly_otp_reset");
        request.setServletPath("/j_superfly_otp_reset");
        request.setParameter("j_key", "KEY");
        request.setParameter("_csrf", "token");
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(
                UsernamePasswordAuthenticationToken.authenticated(
                        "admin", "n/a", AuthorityUtils.createAuthorityList("ROLE_ADMIN"))));
        session.setAttribute(CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN"), "token");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilterChain.doFilter(request, response, new MockFilterChain());

        assertEquals(302, response.getStatus());
        assertTrue(response.getRedirectedUrl(), response.getRedirectedUrl().contains("/login"));
    }

    private static Authentication otpPending() {
        return new CompoundAuthentication(
                new Authentication[]{new LocalNeedOTPToken("admin", OTPType.GOOGLE_AUTH)}, null);
    }

    private static Result get(String uri, Authentication authentication) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setServletPath(uri);
        if (authentication != null) {
            MockHttpSession session = new MockHttpSession();
            SecurityContext securityContext = new SecurityContextImpl(authentication);
            session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);
            request.setSession(session);
        }
        Result result = new Result();
        securityFilterChain.doFilter(request, result.response, result.chain);
        return result;
    }

    private static class Result {
        final MockHttpServletResponse response = new MockHttpServletResponse();
        final MockFilterChain chain = new MockFilterChain();
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
