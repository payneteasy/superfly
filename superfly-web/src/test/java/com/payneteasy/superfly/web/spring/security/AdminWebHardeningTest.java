package com.payneteasy.superfly.web.spring.security;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;

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

import com.payneteasy.superfly.common.SuperflyProperties;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;

import jakarta.servlet.Filter;

/**
 * Response headers of the admin security chains, over plain http (TLS ends at a proxy).
 */
public class AdminWebHardeningTest {

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
    public void mainChainSendsHstsOverPlainHttp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/sso/login");
        request.setServletPath("/sso/login");

        assertHsts(run(request));
    }

    @Test
    public void remotingChainSendsHstsOverPlainHttp() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/remoting/sso.service/getUserDescription");
        request.setServletPath("/remoting");
        request.setPathInfo("/sso.service/getUserDescription");

        assertHsts(run(request));
    }

    private static void assertHsts(MockHttpServletResponse response) {
        String hsts = response.getHeader("Strict-Transport-Security");
        assertEquals("max-age=31536000", hsts);
    }

    private static MockHttpServletResponse run(MockHttpServletRequest request) throws Exception {
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
