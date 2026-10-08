package com.payneteasy.superfly.web.security.ratelimit;

import com.payneteasy.superfly.api.OTPType;
import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.model.ui.user.OtpUserDescription;
import com.payneteasy.superfly.model.ui.user.UserForDescription;
import com.payneteasy.superfly.security.csrf.CsrfValidatorImpl;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.web.spring.security.SpringSecurityConfiguration;
import jakarta.servlet.Filter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import java.util.concurrent.atomic.AtomicInteger;

import static org.easymock.EasyMock.anyString;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/** Full admin security chain: login steps are throttled by IP and IP + username with 429, before the service. */
public class LoginRateLimitSecurityTest {

    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private final AtomicInteger authenticateCalls = new AtomicInteger();

    private AnnotationConfigWebApplicationContext context;
    private Filter                                securityFilterChain;

    @Before
    public void setUp() {
        LocalSecurityService localSecurityService = createNiceMock(LocalSecurityService.class);
        expect(localSecurityService.authenticate(anyString(), anyString())).andAnswer(() -> {
            authenticateCalls.incrementAndGet();
            boolean valid = "admin".equals(getCurrentArguments()[0]) && "pw".equals(getCurrentArguments()[1]);
            return valid ? new String[]{"ADMIN"} : null;
        }).anyTimes();
        UserForDescription user = new UserForDescription();
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        expect(localSecurityService.getOtpUserForDescription(anyString())).andReturn(
                new OtpUserDescription().setHasOtpMasterKey(true).setUserForDescription(user)).anyTimes();
        replay(localSecurityService);

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

    @After
    public void tearDown() {
        context.close();
    }

    @Test
    public void ipIsThrottledAcrossUsernamesWithoutReachingService() throws Exception {
        for (int i = 0; i < LoginAttemptLimiter.DEFAULT_MAX_FAILURES_PER_IP; i++) {
            assertEquals(302, login("10.0.0.1", "user" + i, "bad").getStatus());
        }
        int callsBefore = authenticateCalls.get();

        MockHttpServletResponse blocked = login("10.0.0.1", "another-user", "bad");

        assertEquals(429, blocked.getStatus());
        assertNotNull(blocked.getHeader("Retry-After"));
        assertEquals("service must not be called", callsBefore, authenticateCalls.get());
        assertEquals("other IP is not affected", 302, login("10.0.0.9", "another-user", "bad").getStatus());
    }

    @Test
    public void pairIsThrottledEarlierAndDoesNotAffectOthers() throws Exception {
        for (int i = 0; i < LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER; i++) {
            assertEquals(302, login("10.0.0.2", "Victim", "bad").getStatus());
        }
        int callsBefore = authenticateCalls.get();

        assertEquals("username is case-insensitive", 429, login("10.0.0.2", " victim", "bad").getStatus());
        assertEquals(callsBefore, authenticateCalls.get());
        assertEquals("other user, same IP", 302, login("10.0.0.2", "someone", "bad").getStatus());
        assertEquals("same user, other IP", 302, login("10.0.0.3", "victim", "bad").getStatus());
    }

    @Test
    public void successfulLoginResetsPairCounter() throws Exception {
        int attempts = LoginAttemptLimiter.MAX_FAILURES_PER_IP_USER - 1;
        for (int i = 0; i < attempts; i++) {
            login("10.0.0.4", "admin", "bad");
        }
        assertEquals(302, login("10.0.0.4", "admin", "pw").getStatus());
        for (int i = 0; i < attempts; i++) {
            login("10.0.0.4", "admin", "bad");
        }
        int callsBefore = authenticateCalls.get();

        login("10.0.0.4", "admin", "pw");

        assertEquals("not blocked: the failures before the success were forgotten", callsBefore + 1, authenticateCalls.get());
    }

    private MockHttpServletResponse login(String ip, String username, String password) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/j_superfly_password_security_check");
        request.setServletPath("/j_superfly_password_security_check");
        request.setRemoteAddr(ip);
        request.setParameter("_csrf", "token");
        request.setParameter("j_username", username);
        request.setParameter("j_password", password);
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CSRF_ATTRIBUTE, "token");
        request.setSession(session);
        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilterChain.doFilter(request, response, new MockFilterChain());
        return response;
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
