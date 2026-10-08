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
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

/**
 * The hand-wired admin login filters must change the session id on a successful login step,
 * otherwise a session id planted before login (e.g. via ;jsessionid=) stays valid afterwards.
 */
public class SessionFixationSecurityTest {

    private static final String CSRF_ATTRIBUTE = CsrfValidatorImpl.class.getName().concat(".CSRF_TOKEN");

    private static AnnotationConfigWebApplicationContext context;
    private static Filter securityFilterChain;

    @BeforeClass
    public static void setUp() {
        LocalSecurityService localSecurityService = createNiceMock(LocalSecurityService.class);
        expect(localSecurityService.authenticate("admin", "pw")).andReturn(new String[]{"ADMIN"}).anyTimes();
        UserForDescription user = new UserForDescription();
        user.setOtpTypeCode(OTPType.GOOGLE_AUTH.code());
        expect(localSecurityService.getOtpUserForDescription("admin")).andReturn(
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

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void passwordStepChangesSessionId() throws Exception {
        MockHttpServletRequest request = post("/j_superfly_password_security_check");
        request.setParameter("j_username", "admin");
        request.setParameter("j_password", "pw");
        MockHttpSession session = session();
        request.setSession(session);
        String before = session.getId();

        securityFilterChain.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotEquals(before, session.getId());
        assertNotNull(session.getAttribute("SPRING_SECURITY_CONTEXT"));
        assertEquals("login CSRF token must survive the id change", "token", session.getAttribute(CSRF_ATTRIBUTE));
    }

    @Test
    public void otpInitStepChangesSessionId() throws Exception {
        MockHttpServletRequest request = post("/j_superfly_otp_reset");
        request.setParameter("j_key", "KEY");
        MockHttpSession session = session();
        session.setAttribute("SPRING_SECURITY_CONTEXT", new SecurityContextImpl(new CompoundAuthentication(new Authentication[]{
                new UsernamePasswordAuthenticationToken("admin", "pw"), new LocalNeedOTPToken("admin", OTPType.GOOGLE_AUTH)}, null)));
        request.setSession(session);
        String before = session.getId();

        securityFilterChain.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        assertNotEquals(before, session.getId());
        assertEquals("token", session.getAttribute(CSRF_ATTRIBUTE));
    }

    private static MockHttpServletRequest post(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setServletPath(uri);
        request.setParameter("_csrf", "token");
        return request;
    }

    private static MockHttpSession session() {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute(CSRF_ATTRIBUTE, "token");
        return session;
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
