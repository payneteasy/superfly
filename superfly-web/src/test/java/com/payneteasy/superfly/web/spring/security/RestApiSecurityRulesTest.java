package com.payneteasy.superfly.web.spring.security;

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import jakarta.servlet.Filter;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Правила доступа для префикса публичного сервлета {@code rest-api} ({@code /sso/check/*}):
 * наружу — только remote-auth эндпоинты, всё остальное под префиксом запрещено,
 * даже если туда что-то смонтируется (ревью PR #125, раздел 0).
 */
public class RestApiSecurityRulesTest {

    private static AnnotationConfigWebApplicationContext context;
    private static Filter securityFilterChain;

    @BeforeClass
    public static void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.register(SpringSecurityConfiguration.class);
        context.addBeanFactoryPostProcessor(beanFactory -> {
            beanFactory.registerSingleton("superflyProperties", new SuperflyProperties()
                    .csrfLoginValidatorEnable(false)
                    .enableMultiFactorAuth(false));
            beanFactory.registerSingleton("loggerSink", mock(LoggerSink.class));
            beanFactory.registerSingleton("localSecurityService", mock(LocalSecurityService.class));
            beanFactory.registerSingleton("userDetailsService", mock(UserDetailsService.class));
            // В production его регистрирует @EnableWebMvc (WebConfig) в том же root-контексте.
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
    public void remoteAuthEndpointsArePublic() throws Exception {
        assertTrue(passes("/sso/check", "/check-password/billing/admin"));
        assertTrue(passes("/sso/check", "/check-otp/billing/admin"));
    }

    @Test
    public void everythingElseUnderRestApiPrefixIsDenied() throws Exception {
        assertFalse(passes("/sso/check", "/sso.service/getUserDescription"));
        assertFalse(passes("/sso/check", "/sso.service/registerUser"));
        assertFalse(passes("/sso/check", "/anything"));
        assertFalse(passes("/sso/check", null));
    }

    @Test
    public void ssoPagesStayPublic() throws Exception {
        assertTrue(passes("/sso/login", null));
    }

    @Test
    public void ssoPagesByClassNameAreDenied() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        securityFilterChain.doFilter(get("/sso/wicket/bookmarkable/"
                + "com.payneteasy.superfly.web.wicket.page.user.ListUsersPage"), response, chain);

        assertNull(chain.getRequest());
        // anonymous: denyAll goes through the entry point, as for /sso/check/**
        assertEquals(302, response.getStatus());
        assertEquals("http://localhost/login", response.getRedirectedUrl());
    }

    @Test
    public void ssoLoginPageStaysPublicForGet() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        securityFilterChain.doFilter(get("/sso/login"), response, chain);

        assertNotNull(chain.getRequest());
        assertEquals(200, response.getStatus());
    }

    @Test
    public void remotingRequiresSubsystemAuthentication() throws Exception {
        assertFalse(passes("/remoting", "/sso.service/getUserDescription"));
    }

    private static boolean passes(String servletPath, String pathInfo) throws Exception {
        String uri = pathInfo == null ? servletPath : servletPath + pathInfo;
        MockHttpServletRequest request = new MockHttpServletRequest("POST", uri);
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        MockFilterChain chain = new MockFilterChain();
        securityFilterChain.doFilter(request, new MockHttpServletResponse(), chain);
        return chain.getRequest() != null;
    }

    private static MockHttpServletRequest get(String servletPath) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", servletPath);
        request.setServletPath(servletPath);
        return request;
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
