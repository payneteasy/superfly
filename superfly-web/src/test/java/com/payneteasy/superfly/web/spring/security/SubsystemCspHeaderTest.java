package com.payneteasy.superfly.web.spring.security;

import static org.easymock.EasyMock.niceMock;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.easymock.EasyMock;
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
import com.payneteasy.superfly.dao.SubsystemDao;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystem;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemView;
import com.payneteasy.superfly.model.ui.subsystem.UISubsystemForList;
import com.payneteasy.superfly.service.LocalSecurityService;
import com.payneteasy.superfly.service.LoggerSink;
import com.payneteasy.superfly.service.impl.SubsystemOriginCache;

import jakarta.servlet.Filter;

/**
 * CSP through the real security filter chain: subsystem origins come from the cache and follow invalidation.
 */
public class SubsystemCspHeaderTest {

    private static AnnotationConfigWebApplicationContext context;
    private static Filter securityFilterChain;
    private static SubsystemOriginCache originCache;
    private static SubsystemDao dao;
    private static UISubsystemView subsystem;

    @BeforeClass
    public static void setUp() {
        dao = EasyMock.createNiceMock(SubsystemDao.class);
        UISubsystemForList item = new UISubsystemForList();
        item.setId(1L);
        subsystem = new UISubsystemView();
        subsystem.setLandingUrl("https://first.example/landing");
        subsystem.setLoginFormCssUrl("https://css.example/login.css");
        EasyMock.expect(dao.getSubsystems()).andReturn(List.of(item)).anyTimes();
        EasyMock.expect(dao.getSubsystem(1L)).andAnswer(() -> subsystem).anyTimes();
        replay(dao);
        originCache = new SubsystemOriginCache();
        originCache.setSubsystemDao(dao);

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
            beanFactory.registerSingleton("subsystemOriginCache", originCache);
        });
        context.refresh();
        securityFilterChain = context.getBean("springSecurityFilterChain", Filter.class);
    }

    @AfterClass
    public static void tearDown() {
        context.close();
    }

    @Test
    public void headerFollowsSubsystemDataAndInvalidation() throws Exception {
        originCache.invalidate();
        String before = cspOf("/sso/login");
        assertTrue(before, before.contains("form-action 'self' https://first.example"));
        assertTrue(before, before.contains("style-src 'self' 'unsafe-inline' https://css.example;"));
        assertTrue(before, before.contains("default-src 'self'; script-src 'self' 'unsafe-inline';"));

        subsystem.setLandingUrl("https://second.example/landing");
        assertTrue("still cached", cspOf("/sso/login").contains("https://first.example"));

        originCache.invalidate();
        String after = cspOf("/sso/login");
        assertTrue(after, after.contains("https://second.example"));
        assertFalse(after, after.contains("https://first.example"));
    }

    private static String cspOf(String uri) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        request.setServletPath(uri);
        MockHttpServletResponse response = new MockHttpServletResponse();
        securityFilterChain.doFilter(request, response, new MockFilterChain());
        return response.getHeader("Content-Security-Policy");
    }

    private static <T> T mock(Class<T> type) {
        T mock = niceMock(type);
        replay(mock);
        return mock;
    }
}
